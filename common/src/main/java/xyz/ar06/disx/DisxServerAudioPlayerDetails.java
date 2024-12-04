package xyz.ar06.disx;

import xyz.ar06.disx.client_only.DisxAudioPlayer;
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

    private boolean loop;
    public DisxServerAudioPlayerDetails(BlockPos blockPos, ResourceLocation dimension,
                                        UUID audioPlayerOwner, boolean fromServer, String videoId, boolean timerEnabled, boolean loop){
        this.blockPos = blockPos;
        this.dimension = dimension;
        this.serverOwned = fromServer;
        this.videoId = videoId;
        this.loop = loop;
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
        this.loop = false;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void changeBlockPos(BlockPos pos){
        this.blockPos = pos;
    }

    public void changeDimension(ResourceLocation dimension){
        this.dimension = dimension;
    }
}
