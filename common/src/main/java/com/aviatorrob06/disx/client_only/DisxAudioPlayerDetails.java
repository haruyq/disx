package com.aviatorrob06.disx.client_only;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class DisxAudioPlayerDetails {
    private DisxAudioPlayer disxAudioPlayer;
    private BlockPos blockPos;
    private ResourceLocation dimension;
    private UUID audioPlayerOwner;
    private boolean serverOwned;
    public DisxAudioPlayerDetails(DisxAudioPlayer disxAudioPlayer, BlockPos blockPos, ResourceLocation dimension,
                                  UUID audioPlayerOwner, boolean fromServer){
        this.disxAudioPlayer = disxAudioPlayer;
        this.blockPos = blockPos;
        this.dimension = dimension;
        this.serverOwned = fromServer;
        if (!fromServer){
            this.audioPlayerOwner = audioPlayerOwner;
        } else {
            this.audioPlayerOwner = null;
        }
    }

    public DisxAudioPlayer getDisxAudioPlayer() {
        return disxAudioPlayer;
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

    public void clearDetails(){
        this.disxAudioPlayer = null;
        this.blockPos = null;
        this.dimension = null;
        this.audioPlayerOwner = null;
        this.serverOwned = false;
    }
}
