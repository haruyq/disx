package com.aviatorrob06.disx.forge;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.format.OpusAudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.Pcm16AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.lava.common.natives.NativeLibraryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.*;

public class TestAudioTrack {
    public static void run(){
        Logger LOGGER = LoggerFactory.getLogger("disx");
        AudioPlayerManager firstPlayerManager = new DefaultAudioPlayerManager();
        firstPlayerManager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);
        AudioPlayer firstPlayer = firstPlayerManager.createPlayer();
        AudioPlayerInputStream.createStream(firstPlayer, COMMON_PCM_S16_BE, 999999999999999999L, true);
        LOGGER.info("Attempting to load item!!");
        String urlStr = "https://www.youtube.com/watch?v=otCpCn0l4Wo";
        AudioSourceManagers.registerRemoteSources(firstPlayerManager);
        firstPlayerManager.loadItem(urlStr, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                LOGGER.info("sample track loaded successfully!");
                try{
                    firstPlayer.startTrack(audioTrack, true);
                } catch (UnsatisfiedLinkError e){
                    CompletableFuture.runAsync(TestAudioTrack::run);
                } catch (RuntimeException e){
                    CompletableFuture.runAsync(TestAudioTrack::run);
                }

                firstPlayer.setVolume(100);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @Override
            public void noMatches() {
                LOGGER.info("No matches!");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                LOGGER.info("load failed!");
                System.out.println(e.getCause());
                CompletableFuture.runAsync(TestAudioTrack::run);
            }
        });
        LOGGER.info("Item should've tried to load??");
    }
}
