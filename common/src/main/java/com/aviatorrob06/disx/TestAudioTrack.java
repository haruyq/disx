package com.aviatorrob06.disx;

import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.aviatorrob06.disx.DisxMain.debug;
import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.*;

public class TestAudioTrack {
    public static void run(){
        Logger LOGGER = LoggerFactory.getLogger("disx");
        AudioPlayerManager firstPlayerManager = new DefaultAudioPlayerManager();
        firstPlayerManager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);
        AudioPlayer firstPlayer = firstPlayerManager.createPlayer();
        AudioPlayerInputStream.createStream(firstPlayer, COMMON_PCM_S16_BE, 999999999999999999L, true);
        if (debug) LOGGER.info("Attempting to load item!!");
        String urlStr = "https://www.youtube.com/watch?v=dMSFqXGZ5TQ";
        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager();
        firstPlayerManager.registerSourceManager(youtube);
        firstPlayerManager.loadItem(urlStr, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                if (debug) LOGGER.info("sample track loaded successfully!");
                /*
                try{
                    firstPlayer.startTrack(audioTrack, true);
                } catch (UnsatisfiedLinkError e){
                    CompletableFuture.runAsync(TestAudioTrack::run);
                } catch (RuntimeException e){
                    CompletableFuture.runAsync(TestAudioTrack::run);
                }

                firstPlayer.setVolume(100);
                */
                firstPlayer.destroy();
                firstPlayerManager.shutdown();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @Override
            public void noMatches() {
                if (debug) LOGGER.info("No matches!");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                if (debug) LOGGER.info("load failed!");
                System.out.println(e.getCause());
                //CompletableFuture.runAsync(TestAudioTrack::run);
            }
        });
        if (debug) LOGGER.info("Item should've tried to load??");
        firstPlayer.destroy();
        firstPlayerManager.shutdown();
    }
}
