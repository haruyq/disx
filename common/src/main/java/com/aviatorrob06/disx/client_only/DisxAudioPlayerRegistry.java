package com.aviatorrob06.disx.client_only;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class DisxAudioPlayerRegistry{

    public static Map<BlockPos, DisxAudioPlayer> registry = new HashMap<BlockPos, DisxAudioPlayer>();


    public static void grabServerRegistry(){
        DisxClientPacketIndex.ClientPackets.getServerPlayerRegistry();
    }

    public static void newAudioPlayer(BlockPos blockPos, String videoId, Boolean fromSoundCommand, int seconds){
        DisxAudioPlayer newAudioPlayer = null;
        newAudioPlayer = new DisxAudioPlayer(blockPos, videoId, fromSoundCommand, seconds);
    }

    public static void registerAudioPlayer(DisxAudioPlayer player, BlockPos blockPos){
        registry.put(blockPos, player);
    }

    public static void deregisterAudioPlayer(BlockPos blockPos){
        if (registry.containsKey(blockPos)){
            stopAudioPlayer(blockPos);
            registry.remove(blockPos);
        }
    };

    public static void stopAudioPlayer(BlockPos blockPos){
        if (registry.get(blockPos) != null){
            registry.get(blockPos).stopAudio();
        }
    }

    public static void deregisterAudioPlayer(DisxAudioPlayer player){
        if (registry.entrySet() != null){
            registry.entrySet().forEach(entrySet -> {
                if (entrySet.getValue().equals(player)){
                    BlockPos toDelete = entrySet.getKey();
                    entrySet.getValue().dynamicVolumeCalculations = false;
                    entrySet.getValue().stopAudio();
                    registry.remove(toDelete);
                }
            });
        }
    }

    public static void getPlayerInstance(BlockPos blockPos){

    }

    public static BlockPos getBlockPos(DisxAudioPlayer player){
        BlockPos blockPos= null;
        for (Map.Entry<BlockPos, DisxAudioPlayer> entry : registry.entrySet()){
            if (entry.getValue().equals(player)){
                blockPos = entry.getKey();
            }
        }
        return blockPos;
    }
    public static void clearAllRegisteredPlayers(){
        registry.entrySet().forEach(map -> {
            DisxAudioPlayer currentPlayer = map.getValue();
            currentPlayer.stopAudio();
        });
        registry.clear();
    }

    public static void pauseAllRegisteredPlayers(){
        registry.entrySet().forEach(map -> {
            DisxAudioPlayer currentPlayer = map.getValue();
            if (currentPlayer.player != null){
                currentPlayer.pausePlayer();
            }
        });
    }

    public static void unpauseAllRegisteredPlayers(){
        registry.entrySet().forEach(map -> {
            DisxAudioPlayer currentPlayer = map.getValue();
            if (currentPlayer.player != null){
                currentPlayer.unpausePlayer();
            }
        });
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

    public static void dynamicVolume(){
        registry.entrySet().forEach(map -> {
            DisxAudioPlayer currentPlayer = map.getValue();
            currentPlayer.dynamicVolumeLoop();
        });
    }

}
