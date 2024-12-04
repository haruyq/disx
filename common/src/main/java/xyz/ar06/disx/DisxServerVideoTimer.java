package xyz.ar06.disx;

import xyz.ar06.disx.utils.DisxYoutubeLengthScraper;

import java.util.concurrent.CompletableFuture;

import static xyz.ar06.disx.DisxMain.debug;

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
        DisxLogger.debug("initializing timer");
        int length = DisxYoutubeLengthScraper.getYoutubeVideoLength(videoId);
        DisxLogger.debug(length);
        startTime = System.currentTimeMillis();
        elapsedSeconds = 0;
        while (elapsedSeconds <= (length * 1000L) && !forceStop){
            elapsedSeconds = (System.currentTimeMillis() - startTime);
        }
        if (parent.getVideoTimer().equals(this) && parent.isLoop()){
            this.commenceTimer();
        } else {
            if (parent.getVideoTimer().equals(this)){
                DisxServerAudioPlayerRegistry.removeFromRegistry(this.parent);
            }
        }
    }
}
