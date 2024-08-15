package com.aviatorrob06.disx.client_only;

import com.aviatorrob06.disx.DisxSystemMessages;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.AllocatingAudioFrameBuffer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBuffer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioProcessingContext;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.stylesheets.MediaList;

import javax.sound.sampled.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE;
import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_LE;

@Environment(EnvType.CLIENT)
public class DisxAudioPlayer {

    Logger logger = LoggerFactory.getLogger("Disx");

    AudioDataFormat format = COMMON_PCM_S16_BE;
    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    AudioPlayer player = playerManager.createPlayer();
    Line.Info info;
    SourceDataLine line;
    FloatControl volumeControl;
    FloatControl balanceControl;


    AudioInputStream inputStream;
    byte[] buffer = new byte[1024];
    BlockPos blockPos;

    long currentAudioPos;
    AudioTrack currentLoadedTrack;
    float volumeCalculation = 1F;



    boolean dynamicVolumeCalculations = false;
    boolean fromSoundCommand = false;

    boolean playerCanHear = false;

    boolean dynamicDistanceCalculations = false;

    YoutubeAudioSourceManager youtube;



    public DisxAudioPlayer(BlockPos blockPos, String videoId, Boolean boo, int seconds){
        DisxAudioPlayerRegistry.registerAudioPlayer(this, blockPos);
        fromSoundCommand = boo;
        initializeDisxAudioPlayer();
        String playAttempt = playTrack(videoId, seconds);
        if (playAttempt != null){
            if (playAttempt.equals("Video Not Found")){
                DisxSystemMessages.noVideoFound(Minecraft.getInstance().player);
            }
            if (playAttempt.equals("Playlist")){
                DisxSystemMessages.playlistError(Minecraft.getInstance().player);
            }
            if (playAttempt.equals("Failed")){
                DisxSystemMessages.errorLoading(Minecraft.getInstance().player);
            }
        }
    }

    //player setup; installs proper assignments to declared variables and opens line
    public void initializeDisxAudioPlayer(){
        blockPos = DisxAudioPlayerRegistry.getBlockPos(this);
        playerManager.getConfiguration().setOutputFormat(format);
        inputStream = AudioPlayerInputStream.createStream(player, format, 999999999999999999L, true);
        Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.RECORDS).get();
        double volumeConfig = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.RECORDS).get();
        double volumeConfigMaster = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MASTER).get();
        volumeConfig *= volumeConfigMaster;
        volumeConfig *= 100;
        player.setVolume((int) volumeConfig);
        try {
            //info = new DataLine.Info(SourceDataLine.class, inputStream.getFormat());
            line = AudioSystem.getSourceDataLine(inputStream.getFormat());
            openLine();

        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        youtube = new YoutubeAudioSourceManager();
        playerManager.registerSourceManager(youtube);
    }


    public void dynamicDistanceLoop(){
        while (dynamicDistanceCalculations == true){
            LocalPlayer plr = Minecraft.getInstance().player;
            Vec3 currentpos = Minecraft.getInstance().cameraEntity.getPosition(0.01f);
            Vec3 blockPosVec = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            double distance = currentpos.distanceTo(blockPosVec);
            if (distance > 50){
                line.stop();
            } else if (distance < 50){
                if (!line.isRunning()){
                    line.flush();
                    line.start();
                }
            }

        }
    }


    public void dynamicVolumeLoop(){
        if (volumeControl != null && balanceControl != null) {
            while (dynamicVolumeCalculations) {
                LocalPlayer plr = Minecraft.getInstance().player;
                Vec3 currentpos = Minecraft.getInstance().getCameraEntity().getPosition(0.01f);
                Vec3 blockPosVec = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                //Vec3d currentpos = new Vec3d(plr.lastRenderX, plr.lastRenderY, plr.lastRenderZ);
                double distance = currentpos.distanceTo(blockPosVec);
                float volumeCalc = 0f - (1.0f * (float) distance);
                if (volumeCalc < -80f) {
                    volumeCalc = -80f;
                }
                if (volumeCalc > 6.0206) {
                    volumeCalc = 6.0206f;
                }
                volumeControl.setValue(volumeCalc);
                float balance = 0.0f;
                double volumeConfig = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.RECORDS).get();
                double volumeConfigMaster = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MASTER).get();
                if (volumeConfig != 0.0){
                    if (volumeCalc > -80f && volumeConfigMaster != 0){
                        try {
                            Minecraft.getInstance().getMusicManager().stopPlaying();

                        } catch (NullPointerException e){
                            logger.error("Audio Player at " + blockPos.toString() + " was unsuccessful in pausing client music. Error: " + e);
                        }
                    }
                }
                volumeConfig *= volumeConfigMaster;
                volumeConfig *= 100;
                int volumeConfigInt = (int) volumeConfig;
                player.setVolume(volumeConfigInt);
                if (volumeConfigInt <= 0){
                    playerCanHear = false;
                } else {
                    playerCanHear = true;
                }

                /*try {
                    Vec3d relativePos = new Vec3d(blockPos.getX() - currentpos.x,
                            blockPos.getY() - currentpos.y,
                            blockPos.getZ() - currentpos.z);

                    double angle = Math.atan2(relativePos.z, relativePos.x) * (180.0 / Math.PI) - 90.0;
                    angle = (angle + 360.0) % 360.0;
                    float yaw = plr.getHeadYaw();
                    angle -= yaw;
                    angle = (angle + 180.0) % 360.0 - 180.0;
                    balance = (float) (angle / 180.0);
                    float currentBalance = balanceControl.getValue();
                    balanceControl.setValue((balance = (currentBalance/6)));

                } catch (Exception e){
                    System.out.println(e.getMessage() + e.getCause());
                }*/

            }
        }
    }

    public void playAudio(){
        try {
            if (line != null){
                line.start();
                int chunkSize;
                while ((chunkSize = inputStream.read(buffer)) >= 0 && player.isPaused() == false) {
                    try {
                        line.write(buffer, 0, chunkSize);
                    } catch (Exception e){

                    }
                }
                line.stop();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void openLine() throws LineUnavailableException {
        line.open(inputStream.getFormat());
        line.flush();
        if (!line.isRunning()){
            line.start();
        }
        volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        balanceControl = (FloatControl) line.getControl(FloatControl.Type.BALANCE);
        if (dynamicVolumeCalculations == false){
            dynamicVolumeCalculations = true;
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(this::dynamicVolumeLoop);
        }
        if (dynamicDistanceCalculations == false){
            dynamicDistanceCalculations = true;
            //CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(this::dynamicDistanceLoop);
        }
    }

    public void closeLine(){
        if (line != null){
            dynamicVolumeCalculations = false;
            line.stop();
            line.flush();
            line.close();
        }
    }

    public String playTrack(String videoId, int seconds){
        String url = "https://www.youtube.com/watch?v=" + videoId;
        final String[] exceptionStr = {null};
        AudioItem audItem = youtube.loadItem(playerManager, new AudioReference(videoId, ""));
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                initializeDisxAudioPlayer();
                track.setPosition((long) seconds);
                System.out.println(seconds);
                System.out.println((long) seconds);
                player.playTrack(track);
                DisxClientPacketIndex.ClientPackets.playerSuccessStatus("Success", blockPos, videoId, fromSoundCommand, playerCanHear);
                CompletableFuture.runAsync(DisxAudioPlayer.this::playAudio);
                double volumeConfig = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.RECORDS).get();
                if (volumeConfig != 0.0){
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                exceptionStr[0] = "Playlist";
            }

            @Override
            public void noMatches() {
                exceptionStr[0] = "Video Not Found";
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exceptionStr[0] = "Failed";
                System.out.println(exception.getMessage().toString() + ": " + exception.getCause().toString());
            }

        });

        if (exceptionStr[0] == null){
            return "success";
        } else {
            System.out.println(exceptionStr[0]);
            DisxClientPacketIndex.ClientPackets.playerSuccessStatus(exceptionStr[0], blockPos, videoId, fromSoundCommand, playerCanHear);
            return exceptionStr[0];
        }

    }

    public void pausePlayer(){
        if (player.isPaused() == false){
            player.setPaused(true);
        }
    }

    public void unpausePlayer(){
        if (player.isPaused() == true){
            player.setPaused(false);
            initializeDisxAudioPlayer();
            CompletableFuture<Void> future2 = CompletableFuture.runAsync(this::playAudio);
        }
    }

    public void deRegisterThis(){
        DisxAudioPlayerRegistry.deregisterAudioPlayer(this);
        closeLine();
    }

    public class DisxTrackScheduler extends AudioEventAdapter {
        public DisxTrackScheduler() {
            super();
        }

        @Override
        public void onPlayerPause(AudioPlayer player) {
            super.onPlayerPause(player);
        }

        @Override
        public void onPlayerResume(AudioPlayer player) {
            CompletableFuture<Void> future2 = CompletableFuture.runAsync(DisxAudioPlayer.this::playAudio);
            super.onPlayerResume(player);
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            super.onTrackStart(player, track);
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            deRegisterThis();
            super.onTrackEnd(player, track, endReason);
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
            super.onTrackException(player, track, exception);
        }

        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
            super.onTrackStuck(player, track, thresholdMs);
        }

        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
            super.onTrackStuck(player, track, thresholdMs, stackTrace);
        }

        @Override
        public void onEvent(AudioEvent event) {
            super.onEvent(event);
        }
    }

    public boolean stopAudio(){
        player.stopTrack();
        player.destroy();
        playerManager.shutdown();
        closeLine();
        dynamicDistanceCalculations = false;
        return true;
    }

}
