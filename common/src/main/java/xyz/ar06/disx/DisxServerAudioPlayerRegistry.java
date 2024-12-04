package xyz.ar06.disx;

import xyz.ar06.disx.client_only.DisxAudioPlayerDetails;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.utils.DisxInternetCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.core.jmx.Server;

import javax.management.timer.Timer;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static xyz.ar06.disx.DisxMain.debug;

public class DisxServerAudioPlayerRegistry {

    public static ArrayList<Player> players = new ArrayList<>();
    public static ArrayList<DisxServerAudioPlayerDetails> registry = new ArrayList<>();

    //Default variant
    public static void addToRegistry(BlockPos pos, String videoId, boolean serverOwned, Player player, ResourceKey<Level> dimension, boolean loop){
        ResourceLocation dimensionLocation = dimension.location();
        boolean timerEnabled = true;
        if (player != null){
            DisxSystemMessages.playingAtLocation(player.getServer(), player.getName().getString(), pos, videoId, dimensionLocation);
            if (player.getServer().isSingleplayer()){
                timerEnabled = false;
            }
        }
        DisxServerAudioPlayerDetails serverAudioPlayerDetails = new DisxServerAudioPlayerDetails(pos, dimensionLocation, player.getUUID(), serverOwned, videoId, timerEnabled, loop);
        registry.add(serverAudioPlayerDetails);
        if (player == null){
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, videoId, serverOwned, 0, dimensionLocation, UUID.randomUUID(), loop, pos, dimensionLocation);
            });
        } else {
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, videoId, serverOwned, 0, dimensionLocation, player.getUUID(), loop, pos, dimensionLocation);
            });
        }

    }

    //Variant for Sound Command - Takes server, dimension as resourcelocation, and start time
    public static void addToRegistry(BlockPos pos, String videoId, boolean serverOwned, Player player, MinecraftServer server, ResourceLocation dimension, int startTime, boolean loop){
        ResourceLocation dimensionLocation = dimension;
        boolean timerEnabled = true;
        if (player != null){
            DisxSystemMessages.playingAtLocation(player.getServer(), player.getName().getString(), pos, videoId, dimensionLocation);
        } else {
            DisxSystemMessages.playingAtLocation(server, "Server", pos, videoId, dimensionLocation);
        }
        DisxServerAudioPlayerDetails serverAudioPlayerDetails = new DisxServerAudioPlayerDetails(pos, dimensionLocation, player.getUUID(), serverOwned, videoId, timerEnabled, loop);
        registry.add(serverAudioPlayerDetails);
        if (player == null){
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, videoId, serverOwned, startTime, dimensionLocation, UUID.randomUUID(), loop, pos, dimensionLocation);
            });
        } else {
            players.forEach(plr -> {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, videoId, serverOwned, startTime, dimensionLocation, player.getUUID(), loop, pos, dimensionLocation);
            });
        }

    }

    public static void removeFromRegistry(DisxServerAudioPlayerDetails audioPlayerDetails){
        BlockPos pos = audioPlayerDetails.getBlockPos();
        ResourceLocation dimensionLocation = audioPlayerDetails.getDimension();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, null, false, 0, dimensionLocation, null, false, pos, dimensionLocation);
        });
        registry.remove(audioPlayerDetails);
        audioPlayerDetails.clearDetails();
    }

    public static void removeFromRegistry(BlockPos pos, ResourceKey<Level> dimension){
        ResourceLocation dimensionLocation = dimension.location();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, "", false, 0, dimensionLocation, UUID.randomUUID(), false, pos, dimensionLocation);
        });
        ArrayList<DisxServerAudioPlayerDetails> toRemove = new ArrayList<>();
        for (DisxServerAudioPlayerDetails details : registry){
            if (details.getBlockPos().equals(pos) && details.getDimension().equals(dimensionLocation)){
                toRemove.add(details);
            }
        }
        for (DisxServerAudioPlayerDetails details : toRemove){
            registry.remove(details);
        }
    }

    //RemoveFromRegistry variant for singleplayer track ending
    public static void removeFromRegistry(BlockPos pos, ResourceLocation dimensionLocation){
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, "", false, 0, dimensionLocation, UUID.randomUUID(), false, pos, dimensionLocation);
        });
        ArrayList<DisxServerAudioPlayerDetails> toRemove = new ArrayList<>();
        for (DisxServerAudioPlayerDetails details : registry){
            if (details.getBlockPos().equals(pos) && details.getDimension().equals(dimensionLocation)){
                toRemove.add(details);
            }
        }
        for (DisxServerAudioPlayerDetails details : toRemove){
            registry.remove(details);
        }
    }

    public static void modifyRegistryEntry(BlockPos blockPos, ResourceKey<Level> dimension, BlockPos newBlockPos, ResourceKey<Level> newDimension, boolean loop){
        ResourceLocation dimensionLocation = dimension.location();
        ResourceLocation newDimensionLocation = newDimension.location();
        players.forEach(plr -> {
           DisxServerPacketIndex.ServerPackets.playerRegistryEvent("modify", (Player) plr, blockPos, "", false, 0, dimensionLocation, UUID.randomUUID(), loop, newBlockPos, newDimensionLocation);
        });
        for (DisxServerAudioPlayerDetails details : registry){
            if (details.getBlockPos().equals(blockPos) && details.getDimension().equals(dimensionLocation)){
                details.setLoop(loop);
                details.changeBlockPos(newBlockPos);
                details.changeDimension(newDimensionLocation);
            }
        }
    }

    public static boolean isPlayingAtLocation(BlockPos blockPos, ResourceKey<Level> dimension){
        for (DisxServerAudioPlayerDetails details : registry){
            if (details.getBlockPos().equals(blockPos) && details.getDimension().equals(dimension)){
                return true;
            }
        }
        return false;
    }

    public static void onServerClose(){
        registry.clear();
    }

    public static int getRegistryCount(){
        int returnValue = 0;
        for (DisxServerAudioPlayerDetails details : registry){
            returnValue++;
        }
        return returnValue;
    }

    public static void forceStopAll(){
        for (DisxServerAudioPlayerDetails details : registry){
            removeFromRegistry(details);
        }
    }

    public static List<Player> getMcPlayers(){
        return players;
    }

}
