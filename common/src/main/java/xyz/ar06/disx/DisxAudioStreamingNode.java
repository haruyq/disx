package xyz.ar06.disx;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.apache.http.client.config.RequestConfig;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DisxAudioStreamingNode {
    public static AudioDataFormat FORMAT = StandardAudioDataFormats.COMMON_PCM_S16_BE;
    private static DefaultAudioPlayerManager playerManager;
    private static double streamInterval = 5;
    private AudioPlayer audioPlayer = new DefaultAudioPlayer(playerManager);
    private AudioInputStream inputStream = AudioPlayerInputStream.createStream(audioPlayer, FORMAT, 99999999L, true);
    private ByteArrayOutputStream audioDataBridge = new ByteArrayOutputStream();
    private BlockPos blockPos;
    private ResourceLocation dimension;
    private Player nodeOwner;
    private boolean loop;
    private String videoId;
    private AudioTrack cachedTrack;

    private int preferredVolume;
    private int lastPosition;


    public DisxAudioStreamingNode(String videoId, BlockPos blockPos, ResourceLocation dimension, Player nodeOwner, boolean loop, int startTime){
        DisxLogger.debug("New Audio Streaming Node called for; setting details");
        this.videoId = videoId;
        this.blockPos = blockPos;
        this.dimension = dimension;
        this.nodeOwner = nodeOwner;
        this.loop = loop;
        this.audioPlayer.addListener(new TrackHandler());
        this.preferredVolume = 100;
        DisxLogger.debug("Track handler intialized");
        String url = "http://disxytsourceapi.ar06.xyz/stream_audio?id=" + videoId;
        DisxLogger.debug("Attempting to load requested video");
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                DisxLogger.debug("Loaded requested video");
                if (startTime != 0){
                    DisxLogger.debug("Setting position to " + startTime + " seconds");
                    track.setPosition(startTime * 1000L);
                    DisxLogger.debug("Is track seekable?: " + track.isSeekable());
                }
                cachedTrack = track.makeClone();
                DisxLogger.debug("Track length: " + track.getDuration());
                audioPlayer.playTrack(track);
                DisxLogger.debug("Ordering audio data bridge to be built");
                DisxAudioStreamingNode.this.readAudioInputStream();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                DisxLogger.error("Playlist loaded?? what is this heresy?");
            }

            @Override
            public void noMatches() {
                if (nodeOwner != null){
                    DisxSystemMessages.noVideoFound(nodeOwner);
                }
                DisxLogger.error("Unable to load specified video");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                DisxLogger.error("Unable to load specified video:");
                DisxLogger.error("MESSAGE: " + exception.getMessage());
                exception.printStackTrace();
                DisxSystemMessages.errorLoading(nodeOwner);
            }
        });
    }

    private void streamAudioData() {
        CompletableFuture.runAsync(() -> {
            try {
                int bitDepth = 16;
                int frameSize = (bitDepth / 8) * FORMAT.channelCount;
                int sampleRate = FORMAT.sampleRate;
                int chunkSize = (int) (sampleRate * frameSize * streamInterval); //(calculates to 882000)
                byte[] buffer = new byte[chunkSize];
                if (audioDataBridge != null){
                    ByteArrayInputStream audioDataGate = new ByteArrayInputStream(audioDataBridge.toByteArray());
                    int bytesRead;
                    int currentPosition = 0;
                    if (lastPosition != 0){
                        DisxLogger.debug("Audio was previously paused, skipping to last known position");
                        audioDataGate.skip(lastPosition);
                        currentPosition = lastPosition;
                        lastPosition = 0;
                    }
                    while ((bytesRead = audioDataGate.read(buffer)) >= 0 && audioDataBridge != null && !this.audioPlayer.isPaused()){
                        if (audioDataBridge.size() > 0){
                            if (bytesRead != 0){
                                currentPosition += chunkSize;
                                for (Player p : DisxServerAudioRegistry.getMcPlayers()) {
                                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                                    buf.writeBlockPos(this.blockPos);
                                    buf.writeResourceLocation(this.dimension);
                                    buf.writeBytes(buffer, 0, bytesRead);
                                    DisxServerPacketIndex.ServerPackets.audioData(p, buf);
                                }
                                Thread.sleep((long) (streamInterval * 1000L));
                            }
                            audioDataGate.close();
                        }
                    }
                    if (this.audioPlayer.isPaused()){
                        DisxLogger.debug("Audio was paused, saving paused position");
                        lastPosition = currentPosition;
                    } else if (this.blockPos == null || this.dimension == null){
                        DisxLogger.debug("Audio was stopped; deregistering audio node if not already done");
                        DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
                    } else {
                        try {
                            DisxLogger.debug("Audio streaming finished; waiting to deregister audio node if not already done so");
                            Thread.sleep(((long) streamInterval * 1000L) + 1000L);
                            DisxLogger.debug("Finish window is closed; deregistering audio node if not already done so");
                            DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
                        } catch (Exception e) {
                            DisxLogger.error("Failed to remove DisxAudioStreamingNode from server registry:");
                            e.printStackTrace();
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void readAudioInputStream(){
        CompletableFuture.runAsync(() -> {
           try {
               int bitDepth = 16;
               int frameSize = (bitDepth / 8) * FORMAT.channelCount;
               int sampleRate = FORMAT.sampleRate;
               int bytesPerSecond = sampleRate * frameSize;
               int bufferSize = frameSize * 480;
               byte[] buffer = new byte[bufferSize];
               int bytesRead;
               if (inputStream != null){
                   while ((bytesRead = inputStream.read(buffer)) >= 0 && inputStream != null && !this.audioPlayer.isPaused() && this.audioDataBridge != null) {
                       if (this.blockPos != null && this.dimension != null){
                           audioDataBridge.write(buffer, 0, bytesRead);
                           //long sleepTimeMs = (bytesRead * 1000L) / bytesPerSecond;
                           //Thread.sleep(sleepTimeMs);
                       }
                   }
                   DisxLogger.debug("Audio data bridge built successfully");
               }
           } catch (Exception e){
               e.printStackTrace();
           }
        });
    }

    public void setDimension(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void deconstruct(){
        if (this.audioPlayer != null){
            this.audioPlayer.stopTrack();
            this.audioPlayer.destroy();
        }
        this.blockPos = null;
        this.dimension = null;
        this.loop = false;
        this.nodeOwner = null;
        this.videoId = null;
        try {
            if (this.inputStream != null){
                this.inputStream.close();
            }
            if (this.audioDataBridge != null){
                this.audioDataBridge.close();
                this.audioDataBridge = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pausePlayer(){
        this.audioPlayer.setPaused(true);
    }

    public void resumePlayer(){
        this.audioPlayer.setPaused(false);
        this.streamAudioData();
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public String getVideoId() {
        return videoId;
    }

    public Player getNodeOwner() {
        return nodeOwner;
    }

    public boolean isLoop() {
        return loop;
    }

    public boolean isPaused(){
        return this.audioPlayer.isPaused();
    }

    public static void shutdownPlayerManager(){
        playerManager.shutdown();
    }

    public static void initPlayerManager(MinecraftServer server){
        playerManager = new DefaultAudioPlayerManager();
        playerManager.setHttpRequestConfigurator(requestConfig -> RequestConfig.copy(requestConfig)
                .setSocketTimeout(20000)
                .setConnectTimeout(20000)
                .build()
        );
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        playerManager.getConfiguration().setOutputFormat(FORMAT);
    }

    public int getVolume(){
        return this.audioPlayer.getVolume();
    }

    public int incrementVolume(double amount){
        int currentVolume = this.preferredVolume;
        int castedAmount = (int) amount;
        int newVolume = currentVolume + (castedAmount * 10);
        if (newVolume > 200){
            newVolume = 200;
        }
        if (newVolume < 0){
            newVolume = 0;
        }
        return this.preferredVolume = newVolume;
    }

    public int getPreferredVolume() {
        return preferredVolume;
    }

    public static double getStreamInterval() {
        return streamInterval;
    }

    public class TrackHandler extends AudioEventAdapter {
        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            /*
            DisxLogger.debug("Calling for now playing message packet to be sent");
            DisxServerPacketIndex.ServerPackets.playingVideoIdMessage(DisxAudioStreamingNode.this.videoId, DisxAudioStreamingNode.this.nodeOwner);
            DisxLogger.debug("Audio starting; registered audio data read and send loops");
*/
            super.onTrackStart(player, track);
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            if (endReason.equals(AudioTrackEndReason.FINISHED)){
                DisxLogger.debug("Audio read into memory; sending now playing message, registering data stream loop");
            }
            DisxServerPacketIndex.ServerPackets.playingVideoIdMessage(DisxAudioStreamingNode.this.videoId, DisxAudioStreamingNode.this.nodeOwner);
            DisxAudioStreamingNode.this.streamAudioData();
            /*
            if (loop && endReason.equals(AudioTrackEndReason.FINISHED)){
                DisxLogger.debug("Track finished, loop = true; replaying track");
                AudioTrack toLoop = cachedTrack.makeClone();
                player.playTrack(toLoop);
            } else if (endReason.equals(AudioTrackEndReason.FINISHED)){
                    DisxLogger.debug("Track finished, loop != true; unregistering send audio data loop and deregistering node in 6 second delay");
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(6000);
                            DisxLogger.debug("6 second finish window is closed; deregistering audio node");
                            DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
                        } catch (Exception e) {
                            DisxLogger.error("Failed to remove DisxAudioStreamingNode from server registry:");
                            e.printStackTrace();
                        }

                    });
            }
             */
            super.onTrackEnd(player, track, endReason);
        }
    }

}
