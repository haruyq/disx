package com.aviatorrob06.disx.client_only;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class DisxAudioPlayerRegistry{

    public static ArrayList<DisxAudioPlayerDetails> registry = new ArrayList<>();

    public static void grabServerRegistry(){
        DisxClientPacketIndex.ClientPackets.getServerPlayerRegistry();
    }

    public static void newAudioPlayer(BlockPos blockPos, String videoId, boolean serverOwned, int seconds, ResourceLocation dimension, UUID audioPlayerOwner){
        DisxAudioPlayer newAudioPlayer = new DisxAudioPlayer(blockPos, videoId, serverOwned, seconds, dimension, audioPlayerOwner);
    }

    public static void registerAudioPlayer(DisxAudioPlayerDetails playerDetails){
        registry.add(playerDetails);
    }

    public static void deregisterAudioPlayer(BlockPos blockPos, ResourceLocation dimension){
        ArrayList<DisxAudioPlayerDetails> toRemove = new ArrayList<>();
        for (DisxAudioPlayerDetails details : registry){
            if (details.getBlockPos().equals(blockPos) && details.getDimension().equals(dimension)){
                toRemove.add(details);
            }
        }
        for (DisxAudioPlayerDetails details : toRemove){
            details.getDisxAudioPlayer().dumpsterAudioPlayer();
            details.clearDetails();
            registry.remove(details);
        }
    };

    public static void callStopAudioPlayer(BlockPos blockPos, ResourceLocation dimension){
        for (DisxAudioPlayerDetails details : registry){
            if (details.getBlockPos().equals(blockPos) && details.getDimension().equals(dimension)){
                details.getDisxAudioPlayer().dumpsterAudioPlayer();
            }
        }
    }
    public static void clearAllRegisteredPlayers(){
        for (DisxAudioPlayerDetails details : registry){
            details.getDisxAudioPlayer().dumpsterAudioPlayer();
            details.clearDetails();
        }
        registry.clear();
    }

    public static void pauseAllRegisteredPlayers(){
        for (DisxAudioPlayerDetails details : registry){
            details.getDisxAudioPlayer().pausePlayer();
        }
    }

    public static void unpauseAllRegisteredPlayers(){
        for (DisxAudioPlayerDetails details : registry){
            details.getDisxAudioPlayer().unpausePlayer();
        }
    }

    public static void onPlayDisconnect() {
        clearAllRegisteredPlayers();
    }

    public static void onClientStopping(Minecraft client) {
        clearAllRegisteredPlayers();
    }

    public static void onClientPause(){
        pauseAllRegisteredPlayers();
    }

    public static void onClientUnpause(){
        unpauseAllRegisteredPlayers();
    }

}
