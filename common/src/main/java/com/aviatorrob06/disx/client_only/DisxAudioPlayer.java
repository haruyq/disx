package com.aviatorrob06.disx.client_only;

import com.aviatorrob06.disx.DisxSystemMessages;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.injectables.annotations.PlatformOnly;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
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

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE;

@Environment(EnvType.CLIENT)
public class DisxAudioPlayer {

    Logger logger = LoggerFactory.getLogger("Disx");

    private final AudioDataFormat format = COMMON_PCM_S16_BE;
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private final AudioPlayer player = playerManager.createPlayer();
    private Line.Info info;
    private SourceDataLine line;
    private FloatControl volumeControl;
    private FloatControl balanceControl;


    private final AudioInputStream inputStream = AudioPlayerInputStream.createStream(player, format, 999999999999999999L, true);
    private final byte[] buffer = new byte[1024];
    private long currentAudioPos;
    private AudioTrack currentLoadedTrack;
    private final float volumeCalculation = 1F;


    private boolean dynamicVolumeCalculations = false;
    private final DisxAudioPlayerDetails audioPlayerDetails;

    private boolean playerCanHear = false;

    private boolean dynamicDistanceCalculations = false;

    private final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager();


    public DisxAudioPlayer(BlockPos blockPos, String videoId, boolean serverOwned, int seconds, ResourceLocation dimension, UUID audioPlayerOwner) {
        this.audioPlayerDetails = new DisxAudioPlayerDetails(this, blockPos, dimension, audioPlayerOwner, serverOwned);
        DisxAudioPlayerRegistry.registerAudioPlayer(audioPlayerDetails);

        playerManager.registerSourceManager(youtube);
        playerManager.getConfiguration().setOutputFormat(format);
        try {
            createAndOpenLine();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        String playAttempt = playTrack(videoId, seconds);
        if (playAttempt != null) {
            if (playAttempt.equals("Video Not Found")) {
                DisxSystemMessages.noVideoFound(Minecraft.getInstance().player);
            }
            if (playAttempt.equals("Playlist")) {
                DisxSystemMessages.playlistError(Minecraft.getInstance().player);
            }
            if (playAttempt.equals("Failed")) {
                DisxSystemMessages.errorLoading(Minecraft.getInstance().player);
            }
        }
    }


    private void dynamicDistanceLoop() {
        if (dynamicDistanceCalculations == true) {
            LocalPlayer plr = Minecraft.getInstance().player;
            Vec3 currentpos = Minecraft.getInstance().cameraEntity.getPosition(0.01f);
            BlockPos blockPos = audioPlayerDetails.getBlockPos();
            Vec3 blockPosVec = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            double distance = currentpos.distanceTo(blockPosVec);
            if (distance > 50) {
                line.stop();
            } else if (distance < 50) {
                if (!line.isRunning()) {
                    line.flush();
                    line.start();
                }
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
                        Vec3 currentpos = Minecraft.getInstance().getCameraEntity().getPosition(0.01f);
                        BlockPos blockPos = audioPlayerDetails.getBlockPos();
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
                        if (volumeConfig != 0.0) {
                            if (volumeCalc > -80f && volumeConfigMaster != 0) {
                                try {
                                    MusicManager musicManager = Minecraft.getInstance().getMusicManager();
                                    SoundManager soundManager = Minecraft.getInstance().getSoundManager();
                                    Minecraft.getInstance().getMusicManager().tick();
                                    if (playerCanHear) {
                                        musicManager.stopPlaying();
                                    }

                                } catch (NullPointerException e) {
                                    logger.error("Audio Player at " + blockPos.toString() + " was unsuccessful in pausing client music. Error: " + e);
                                }
                            }
                        }
                        volumeConfig *= volumeConfigMaster;
                        volumeConfig *= 100;
                        int volumeConfigInt = (int) volumeConfig;
                        player.setVolume(volumeConfigInt);
                        if (volumeConfigInt <= 0) {
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
                    } else {
                        player.setVolume(0);
                        volumeControl.setValue(-80f);
                        playerCanHear = false;
                    }

                }
            }

        }
    }
    private void streamToAudioLine(){
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

    private void createAndOpenLine() throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(inputStream.getFormat());
        line.open(inputStream.getFormat());
        line.flush();
        if (!line.isRunning()){
            line.start();
        }
        volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        balanceControl = (FloatControl) line.getControl(FloatControl.Type.BALANCE);
        ClientTickEvent.CLIENT_POST.register(this::dynamicVolumeLoop);
        if (dynamicDistanceCalculations == false){
            //dynamicDistanceCalculations = true;
            //CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(this::dynamicDistanceLoop);
        }
    }

    private void closeLine(){
        if (line != null){
            dynamicVolumeCalculations = false;
            line.stop();
            line.flush();
            line.close();
        }
    }

    private String playTrack(String videoId, int seconds){
        String url = "https://www.youtube.com/watch?v=" + videoId;
        final String[] exceptionStr = {null};
        AudioItem audItem = youtube.loadItem(playerManager, new AudioReference(videoId, ""));
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                //track.setPosition((long) seconds);
                //System.out.println(seconds);
                player.playTrack(track);
                BlockPos blockPos = audioPlayerDetails.getBlockPos();
                boolean serverOwned = audioPlayerDetails.isServerOwned();
                DisxClientPacketIndex.ClientPackets.playerSuccessStatus("Success", blockPos, videoId, serverOwned, playerCanHear);
                CompletableFuture.runAsync(DisxAudioPlayer.this::streamToAudioLine);
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
            BlockPos blockPos = audioPlayerDetails.getBlockPos();
            boolean serverOwned = audioPlayerDetails.isServerOwned();
            DisxClientPacketIndex.ClientPackets.playerSuccessStatus(exceptionStr[0], blockPos, videoId, serverOwned, playerCanHear);
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
            CompletableFuture<Void> future2 = CompletableFuture.runAsync(this::streamToAudioLine);
        }
    }
    public void dumpsterAudioPlayer(){
        player.stopTrack();
        player.destroy();
        playerManager.shutdown();
        closeLine();
        dynamicDistanceCalculations = false;
        ClientTickEvent.CLIENT_POST.unregister(this::dynamicVolumeLoop);
    }

}
