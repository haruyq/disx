package xyz.ar06.disx.client_only;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import org.apache.http.client.config.RequestConfig;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.utils.DisxYoutubeAudioURLScraper;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ar06.disx.utils.DisxYoutubeInfoScraper;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE;
import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_LE;

@Environment(EnvType.CLIENT)
public class DisxAudioPlayer {

    Logger logger = LoggerFactory.getLogger("Disx");

    private final AudioDataFormat format = COMMON_PCM_S16_BE;
    private static final DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private static boolean playerManagerConfigured = false;

    private final AudioPlayer player = new DefaultAudioPlayer(playerManager);
    private Line.Info info;
    private SourceDataLine line;
    private FloatControl volumeControl;
    private FloatControl balanceControl;


    private final AudioInputStream inputStream = AudioPlayerInputStream.createStream(player, format, 999999999999999999L, true);
    private final byte[] buffer = new byte[1024];
    private final float volumeCalculation = 1F;


    private boolean dynamicVolumeCalculations = false;
    private final DisxAudioPlayerDetails audioPlayerDetails;
    private final TrackHandler trackHandler = new TrackHandler();

    private boolean playerCanHear = false;

    private AudioTrack cachedTrack;

    private String cachedAudioDeviceName = null;

    public DisxAudioPlayer(BlockPos blockPos, String videoId, boolean serverOwned, int seconds, ResourceLocation dimension, UUID audioPlayerOwner, boolean loop) {
        DisxLogger.debug("generating new audio player");
        this.audioPlayerDetails = new DisxAudioPlayerDetails(this, blockPos, dimension, audioPlayerOwner, serverOwned, loop, videoId, seconds);
        DisxLogger.debug("generated audio player details");
        DisxAudioPlayerRegistry.registerAudioPlayer(audioPlayerDetails);
        DisxLogger.debug("registered audio player on client side");
        if (!playerManagerConfigured){
            //playerManager.registerSourceManager(youtube);
            AudioSourceManagers.registerRemoteSources(playerManager);
            AudioSourceManagers.registerLocalSource(playerManager);
            playerManager.getConfiguration().setOutputFormat(format);
            playerManager.setHttpRequestConfigurator(config -> RequestConfig.copy(config)
                    .setSocketTimeout(10000)
                    .setConnectTimeout(10000)
                    .build());
            playerManagerConfigured = true;
        }
        DisxLogger.debug("configured playermanager");
        player.addListener(trackHandler);
        DisxLogger.debug("added listener to player");
        try {
            createAndOpenLine();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        DisxLogger.debug("created and opened line");
        DisxLogger.debug("PLAYERCANHEAR: " + playerCanHear);
        if (playerCanHear){
            DisxSystemMessages.loadingVideo(videoId);
            DisxLogger.debug("sent loading video message to client");
        }
        String playAttempt = playTrack(videoId, seconds);
        DisxLogger.debug("ATTEMPT PLAY RESULT: " + playAttempt);
        if (playAttempt != null && audioPlayerDetails.getAudioPlayerOwner().equals(Minecraft.getInstance().player.getUUID())) {
            if (playAttempt.equals("Video Not Found")) {
                DisxSystemMessages.noVideoFound(Minecraft.getInstance().player);
            }
            if (playAttempt.equals("Failed")) {
                DisxSystemMessages.errorLoading(Minecraft.getInstance().player);
            }
            if (playAttempt.equals("API FAILURE")){
                DisxSystemMessages.apiError(Minecraft.getInstance().player);
            }
        }
    }


    private void dynamicVolumeLoop(Minecraft minecraft) {
        if (volumeControl != null && balanceControl != null) {
            LocalPlayer plr = Minecraft.getInstance().player;
            ResourceLocation dimension = this.audioPlayerDetails.getDimension();

            if (plr != null){
                if (plr.level() != null){
                    if (plr.level().dimension().location().equals(dimension)) {
                        if (DisxAudioPlayerRegistry.isMuted(this.audioPlayerDetails.getAudioPlayerOwner())){
                            DisxLogger.debug("is muted");
                            player.setVolume(0);
                            volumeControl.setValue(-80f);
                            playerCanHear = false;
                        } else {
                            Vec3 currentpos = Minecraft.getInstance().getCameraEntity().getPosition(0.01f);
                            BlockPos blockPos = audioPlayerDetails.getBlockPos();
                            Vec3 blockPosVec = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                            double distance = currentpos.distanceTo(blockPosVec);
                            float volumeCalc_line = 0f - (1.0f * (float) distance);
                            if (volumeCalc_line < -80f) {
                                volumeCalc_line = -80f;
                            }
                            if (volumeCalc_line > 6.0206) {
                                volumeCalc_line = 6.0206f;
                            }
                            volumeControl.setValue(volumeCalc_line);

                            double plrVolumeConfig_RECORDS = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.RECORDS).get();
                            double plrVolumeConfig_MASTER = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MASTER).get();

                            int volumeCalc_audioPlayer = (int) (plrVolumeConfig_RECORDS * plrVolumeConfig_MASTER * 100);
                            player.setVolume(volumeCalc_audioPlayer);

                            if (volumeCalc_audioPlayer <= 0 && volumeCalc_line == -80f) {
                                playerCanHear = false;
                            } else {
                                playerCanHear = true;
                            }

                            if (plrVolumeConfig_RECORDS != 0.0 && volumeCalc_line > -80f && plrVolumeConfig_MASTER != 0 && playerCanHear) {
                                try {
                                    MusicManager musicManager = Minecraft.getInstance().getMusicManager();
                                    Minecraft.getInstance().getMusicManager().tick();
                                    musicManager.stopPlaying();
                                } catch (NullPointerException e) {
                                    logger.error("Audio Player at " + blockPos + " was unsuccessful in pausing client music. Error: " + e);
                                }
                            }

                        }
                    } else {
                        player.setVolume(0);
                        volumeControl.setValue(-80f);
                        playerCanHear = false;
                    }

                }

                if (getPreferredAudioDeviceName() != null){
                    if (!getPreferredAudioDeviceName().equals(cachedAudioDeviceName)){
                        try {
                            DisxLogger.debug("Preferred audio device changed; rebuilding SourceDataLine");
                            modifyLine();
                        } catch (LineUnavailableException e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (cachedAudioDeviceName != null){
                        try {
                            DisxLogger.debug("Preferred audio device changed; rebuilding SourceDataLine");
                            modifyLine();
                        } catch (LineUnavailableException e){
                            e.printStackTrace();
                        }
                    }
                }

            }

        }
    }
    private void streamToAudioLine(){
        DisxLogger.debug("streaming to audio line");
        try {
            if (line != null){
                line.start();
                int chunkSize;
                while ((chunkSize = inputStream.read(buffer)) >= 0 && !player.isPaused() && line.isOpen()) {
                    try {
                        line.write(buffer, 0, chunkSize);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                line.stop();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DisxLogger.debug("stopped streaming to audio line");
    }

    private String getPreferredAudioDeviceName(){
        String soundDevice = Minecraft.getInstance().options.soundDevice().get();
        if (!soundDevice.isEmpty()){
            return soundDevice.substring(15);
        } else {
            return null;
        }
    }

    private Mixer getPreferredAudioDevice(){
        String preferredSoundDevice = getPreferredAudioDeviceName();
        cachedAudioDeviceName = preferredSoundDevice;
        DisxLogger.debug("Preferred audio device: " + preferredSoundDevice);
        if (preferredSoundDevice != null){
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            for (Mixer.Info mixerInfo : mixers){
                DisxLogger.debug("Audio Device in Loop: " + mixerInfo.getName());
                if (mixerInfo.getName().equals(preferredSoundDevice)){
                    DisxLogger.debug("Matches user setting; returning to caller");
                    return AudioSystem.getMixer(mixerInfo);
                }
            }
        }
        DisxLogger.debug("Did not find an audio device matching user preference; returning null to caller");
        return null;
    }

    private void modifyLine() throws LineUnavailableException {
        Mixer audioDevice = getPreferredAudioDevice();
        SourceDataLine newLine = null;
        if (audioDevice != null){
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, inputStream.getFormat());
            newLine = (SourceDataLine) audioDevice.getLine(dataLineInfo);
        } else {
            newLine = AudioSystem.getSourceDataLine(inputStream.getFormat());
        }
        newLine.open(inputStream.getFormat());
        newLine.flush();
        if (!newLine.isRunning()){
            newLine.start();
        }
        SourceDataLine oldLine = line;
        line = newLine;
        volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        balanceControl = (FloatControl) line.getControl(FloatControl.Type.BALANCE);
        oldLine.stop();
        oldLine.flush();
        oldLine.close();
    }

    private void createAndOpenLine() throws LineUnavailableException {
        Mixer audioDevice = getPreferredAudioDevice();
        if (audioDevice != null){
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, inputStream.getFormat());
            line = (SourceDataLine) audioDevice.getLine(dataLineInfo);
        } else {
            line = AudioSystem.getSourceDataLine(inputStream.getFormat());
        }
        line.open(inputStream.getFormat());
        line.flush();
        if (!line.isRunning()){
            line.start();
        }
        volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        balanceControl = (FloatControl) line.getControl(FloatControl.Type.BALANCE);
        ClientTickEvent.CLIENT_POST.register(this::dynamicVolumeLoop);
    }

    private void closeLine(){
        if (line != null){
            dynamicVolumeCalculations = false;
            line.stop();
            line.flush();
            line.close();
        }
    }

    private void startAudioLineStreaming(){
        CompletableFuture.runAsync(this::streamToAudioLine);
        double volumeConfig = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.RECORDS).get();
        if (volumeConfig != 0.0){
            Minecraft.getInstance().getMusicManager().stopPlaying();
        }
    }

    private void replayTrack(){
        AudioTrack track = this.cachedTrack.makeClone();
        this.player.playTrack(track);
        //startAudioLineStreaming();
    }

    private String playTrack(String videoId, int seconds){
        String url = "http://disxytsourceapi.ar06.xyz/stream_audio?id=" + videoId;
        final String[] exceptionStr = {null};
        playerManager.loadItemSync(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                //DisxLogger.debug("setting track at " + (seconds * 1000L));
                DisxAudioPlayer.this.cachedTrack = track.makeClone();
                track.setPosition(seconds);
                player.playTrack(track);
                //BlockPos blockPos = audioPlayerDetails.getBlockPos();
                //boolean serverOwned = audioPlayerDetails.isServerOwned();
                //DisxClientPacketIndex.ClientPackets.playerSuccessStatus("Success", blockPos, videoId, serverOwned, playerCanHear);
                DisxAudioPlayer.this.startAudioLineStreaming();
                DisxLogger.debug("ran track loaded function");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {
                exceptionStr[0] = "Video Not Found";
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exceptionStr[0] = "Failed";
                DisxLogger.debug(exception.getMessage().toString() + ": " + exception.getCause().toString());
            }

        });
        if (exceptionStr[0] == null){
            return "success";
        } else {
            return exceptionStr[0];
        }

    }

    public void pausePlayer(){
        if (!player.isPaused()){
            player.setPaused(true);
        }
    }

    public void unpausePlayer(){
        if (player.isPaused()){
            player.setPaused(false);
            this.startAudioLineStreaming();
        }
    }
    public void dumpsterAudioPlayer(){
        player.stopTrack();
        player.removeListener(trackHandler);
        player.destroy();
        closeLine();
        ClientTickEvent.CLIENT_POST.unregister(this::dynamicVolumeLoop);
    }

    public static void shutdownPlayerManager(){
        playerManager.shutdown();
    }

    public class TrackHandler extends AudioEventAdapter {
        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            if (audioPlayerDetails.isLoop() && endReason.equals(AudioTrackEndReason.FINISHED)){
                DisxLogger.debug("Loop enabled; playing cached track");
                DisxAudioPlayer.this.replayTrack();
            } else {
                if (Minecraft.getInstance().isSingleplayer() && endReason.equals(AudioTrackEndReason.FINISHED)){
                    DisxClientPacketIndex.ClientPackets.singleplayerTrackEnd(audioPlayerDetails.getBlockPos(), audioPlayerDetails.getDimension());
                }
            }
            super.onTrackEnd(player, track, endReason);
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            if (audioPlayerDetails.getVideoId() != null){
                DisxSystemMessages.playingVideo(audioPlayerDetails.getVideoId());
            }
            super.onTrackStart(player, track);
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
            exception.printStackTrace();
            super.onTrackException(player, track, exception);
        }
    }

}
