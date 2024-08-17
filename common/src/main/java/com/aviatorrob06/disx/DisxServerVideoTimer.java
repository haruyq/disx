package com.aviatorrob06.disx;

import net.minecraft.core.BlockPos;

import java.util.concurrent.CompletableFuture;

import static com.aviatorrob06.disx.DisxMain.debug;

public class DisxServerVideoTimer {
    long startTime = 0;
    long elapsedSeconds = 0;

    boolean forceStop = false;

    String videoId;
    DisxServerAudioPlayerDetails parent;

    public DisxServerVideoTimer(String videoId, DisxServerAudioPlayerDetails parent){
        this.videoId = videoId;
        this.parent = parent;
        CompletableFuture.runAsync(this::commenceTimer);
    }

    public void setForceStop(){
        this.forceStop = true;
    }
    public void commenceTimer(){
        if (debug) System.out.println("initializing timer");
        int length = DisxYoutubeLengthScraper.getYoutubeVideoLength(videoId);
        if (debug) System.out.println(length);
        startTime = System.currentTimeMillis();
        while (elapsedSeconds <= (length * 1000L) && !forceStop){
            int test = 0;
            elapsedSeconds = (System.currentTimeMillis() - startTime);
        }
        if (parent.getVideoTimer().equals(this)){
            DisxServerAudioPlayerRegistry.removeFromRegistry(this.parent);
        }
    }
}
