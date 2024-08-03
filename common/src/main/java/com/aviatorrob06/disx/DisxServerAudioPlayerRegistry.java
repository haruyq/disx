package com.aviatorrob06.disx;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.core.jmx.Server;

import javax.management.timer.Timer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.aviatorrob06.disx.DisxMain.debug;

public class DisxServerAudioPlayerRegistry {

    public static List players = new ArrayList();
    public static Map<BlockPos, String> registry = new HashMap<BlockPos, String>();

    public static Map<BlockPos, videoTimer> timerRegistry = new HashMap<BlockPos, videoTimer>();

    public static void sendPlayerRegistryEvent(BlockPos pos, String videoId, boolean fromSoundCommand, Player player) {

    }

    public static void addToRegistry(BlockPos pos, String videoId, boolean fromSoundCommand, Player player){
        registry.put(pos, videoId);
        players.forEach(plr -> {
            if (plr.equals(player) && fromSoundCommand){
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, videoId, fromSoundCommand, 0);
            } else {
                DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add", (Player) plr, pos, videoId, false, 0);
            }
        });
        if (!player.getServer().isSingleplayer()){
            videoTimer timer = new videoTimer();
            timerRegistry.put(pos, timer);
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    timer.videoTimer(pos, videoId);
                }
            };
            CompletableFuture.runAsync(run);
        }
    }

    public static void removeFromRegistry(BlockPos pos, String videoId){
        players.forEach(plr -> {
            DisxServerPacketIndex.ServerPackets.playerRegistryEvent("remove", (Player) plr, pos, videoId, false, 0);
        });
        registry.remove(pos);
        if (timerRegistry.containsKey(pos)){
            timerRegistry.remove(pos);
        }
    }

    public static void onServerClose(){
        registry.clear();
    }

    public static class videoTimer{
        long startTime = 0;
        long elapsedSeconds = 0;

        boolean forceStop = false;
        public void videoTimer(BlockPos pos, String videoId){
            if (debug) System.out.println("initializing timer");
            int length = DisxYoutubeLengthScraper.getYoutubeVideoLength(videoId);
            if (debug) System.out.println(length);
            startTime = System.currentTimeMillis();
            while (elapsedSeconds <= (length * 1000) && forceStop == false){
                int test = 0;
                elapsedSeconds = (System.currentTimeMillis() - startTime);
            }
            if (timerRegistry.containsValue(this)) {
                removeFromRegistry(pos, videoId);
            }
        }
    }


}
