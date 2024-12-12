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
import dev.architectury.event.events.common.TickEvent;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.apache.http.client.config.RequestConfig;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DisxAudioStreamingNode {
    private static AudioDataFormat FORMAT = StandardAudioDataFormats.COMMON_PCM_S16_BE;
    private static DefaultAudioPlayerManager playerManager;
    private AudioPlayer audioPlayer = new DefaultAudioPlayer(playerManager);
    private AudioInputStream inputStream = AudioPlayerInputStream.createStream(audioPlayer, FORMAT, 99999999L, true);
    private BlockPos blockPos;
    private ResourceLocation dimension;
    private Player nodeOwner;
    private boolean loop;
    private String videoId;
    private AudioTrack cachedTrack;

    public DisxAudioStreamingNode(String videoId, BlockPos blockPos, ResourceLocation dimension, Player nodeOwner, boolean loop, int startTime){
        DisxLogger.debug("New Audio Streaming Node called for; setting details");
        this.videoId = videoId;
        this.blockPos = blockPos;
        this.dimension = dimension;
        this.nodeOwner = nodeOwner;
        this.loop = loop;
        this.audioPlayer.addListener(new TrackHandler());
        DisxLogger.debug("Track handler intialized");
        String url = "http://disxytsourceapi.ar06.xyz/stream_audio?id=" + videoId;
        DisxLogger.debug("Attempting to load requested video");
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                DisxLogger.debug("Loaded requested video");
                cachedTrack = track.makeClone();
                if (startTime != 0){
                    track.setPosition(startTime);
                }
                audioPlayer.playTrack(track);
                DisxLogger.debug("Playback starting");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                DisxLogger.error("Playlist loaded?? what is this heresy?");
            }

            @Override
            public void noMatches() {
                DisxLogger.error("Unable to load specified video");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                DisxLogger.error("Unable to load specified video:");
                exception.printStackTrace();
            }
        });
        this.sendAudioData();
    }

    private boolean audioDataLoopRunning = false;
    private void sendAudioData() {
        CompletableFuture.runAsync(() -> {
            try {
                byte[] buffer = new byte[882000];
                int bytesRead;
                if (inputStream != null){
                    while ((bytesRead = inputStream.read(buffer)) >= 0 && inputStream != null) {
                        if (this.blockPos != null && this.dimension != null){
                            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                            buf.writeBlockPos(this.blockPos);
                            buf.writeResourceLocation(this.dimension);
                            buf.writeBytes(buffer, 0, bytesRead);
                            for (Player p : DisxServerAudioRegistry.getMcPlayers()) {
                                DisxServerPacketIndex.ServerPackets.audioData(p, buf);
                            }
                        }
                        Thread.sleep(5000);
                    }
                }
            } catch (Exception e) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pausePlayer(){
        this.audioPlayer.setPaused(true);
    }

    public void resumePlayer(){
        this.audioPlayer.setPaused(false);
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

    public static void shutdownPlayerManager(){
        playerManager.shutdown();
    }

    public static void initPlayerManager(MinecraftServer server){
        playerManager = new DefaultAudioPlayerManager();
        playerManager.setHttpRequestConfigurator(requestConfig -> RequestConfig.copy(requestConfig)
                .setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .build()
        );
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        playerManager.getConfiguration().setOutputFormat(FORMAT);
    }

    public class TrackHandler extends AudioEventAdapter {
        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            DisxLogger.debug("Audio starting; registered audio data send loop");
            if (DisxAudioStreamingNode.this.getNodeOwner() != null){
                DisxServerPacketIndex.ServerPackets.playingVideoIdMessage(DisxAudioStreamingNode.this.videoId, DisxAudioStreamingNode.this.nodeOwner);
            }
            super.onTrackStart(player, track);
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            if (loop && endReason.equals(AudioTrackEndReason.FINISHED)){
                DisxLogger.debug("Track finished, loop = true; replaying track");
                AudioTrack toLoop = cachedTrack.makeClone();
                player.playTrack(toLoop);
            } else {
                DisxLogger.debug("Track finished, loop != true; unregistering send audio data loop and deregistering node");
                DisxServerAudioRegistry.removeFromRegistry(DisxAudioStreamingNode.this);
            }
            super.onTrackEnd(player, track, endReason);
        }
    }

}
