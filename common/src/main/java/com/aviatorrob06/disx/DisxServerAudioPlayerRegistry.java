package com.aviatorrob06.disx;

import com.aviatorrob06.disx.client_only.DisxAudioPlayerDetails;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.core.jmx.Server;

import javax.management.timer.Timer;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.aviatorrob06.disx.DisxMain.debug;

public class DisxServerAudioPlayerRegistry {

    public static ArrayList<Player> players = new ArrayList<>();
    public static ArrayList<DisxServerAudioPlayerDetails> registry = new ArrayList<>();

    public static void addToRegistry(BlockPos pos, String videoId, boolean serverOwned, Player player, ResourceKey<Level> dimension){
        ResourceLocation dimensionLocation = dimension.location();
        boolean timerEnabled = true;
        if (player.getServer().isSingleplayer()){
            timerEnabled = false;
        }
        DisxServerAudioPlayerDetails serverAudioPlayerDetails = new DisxServerAudioPlayerDetails(pos, dimensionLocation, player.getUUID(), serverOwned, videoId, timerEnabled);
        registry.add(serverAudioPlayerDetails);
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, videoId, serverOwned, 0, dimensionLocation, player.getUUID());
        });
    }

    public static void removeFromRegistry(DisxServerAudioPlayerDetails audioPlayerDetails){
        BlockPos pos = audioPlayerDetails.getBlockPos();
        ResourceLocation dimensionLocation = audioPlayerDetails.getDimension();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, null, false, 0, dimensionLocation, null);
        });
        registry.remove(audioPlayerDetails);
        audioPlayerDetails.clearDetails();
    }

    public static void removeFromRegistry(BlockPos pos, ResourceKey<Level> dimension){
        ResourceLocation dimensionLocation = dimension.location();
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, "", false, 0, dimensionLocation, UUID.randomUUID());
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

    public static void onServerClose(){
        registry.clear();
    }


}
