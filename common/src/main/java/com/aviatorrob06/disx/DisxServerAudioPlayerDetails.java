package com.aviatorrob06.disx;

import com.aviatorrob06.disx.client_only.DisxAudioPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class DisxServerAudioPlayerDetails {
    private BlockPos blockPos;
    private ResourceLocation dimension;
    private UUID audioPlayerOwner;
    private boolean serverOwned;
    private String videoId;

    private DisxServerVideoTimer videoTimer;
    public DisxServerAudioPlayerDetails(BlockPos blockPos, ResourceLocation dimension,
                                        UUID audioPlayerOwner, boolean fromServer, String videoId, boolean timerEnabled){
        this.blockPos = blockPos;
        this.dimension = dimension;
        this.serverOwned = fromServer;
        this.videoId = videoId;
        if (timerEnabled){
            this.videoTimer = new DisxServerVideoTimer(videoId, this);
        }
        if (!fromServer){
            this.audioPlayerOwner = audioPlayerOwner;
        } else {
            this.audioPlayerOwner = UUID.randomUUID();
        }
    }

    public UUID getAudioPlayerOwner() {
        return audioPlayerOwner;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public boolean isServerOwned() {
        return serverOwned;
    }

    public DisxServerVideoTimer getVideoTimer() {
        return videoTimer;
    }

    public String getVideoId() {
        return videoId;
    }

    public void clearDetails(){
        this.blockPos = null;
        this.dimension = null;
        this.audioPlayerOwner = null;
        this.serverOwned = false;
        this.videoTimer = null;
        this.videoId = null;
    }
}
