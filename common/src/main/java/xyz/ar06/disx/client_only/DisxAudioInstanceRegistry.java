package xyz.ar06.disx.client_only;

import io.netty.buffer.ByteBuf;
import xyz.ar06.disx.DisxLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xyz.ar06.disx.DisxSystemMessages;

import java.util.*;

@Environment(EnvType.CLIENT)
public class DisxAudioInstanceRegistry {

    public static LinkedList<DisxAudioInstance> registry = new LinkedList<>();

    public static LinkedList<UUID> muted = new LinkedList<>();

    public static void grabServerRegistry(){
        DisxClientPacketIndex.ClientPackets.getServerAudioRegistry();
    }

    public static void newAudioPlayer(BlockPos blockPos, ResourceLocation dimension, UUID instanceOwner, boolean loop, int preferredVolume){
        registry.add(new DisxAudioInstance(blockPos, dimension, instanceOwner, loop, preferredVolume));
        DisxLogger.debug("New DisxAudioInstance registered");
    }

    public static void registerAudioPlayer(DisxAudioPlayerDetails playerDetails){
        //registry.add(playerDetails);
    }

    public static void deregisterAudioPlayer(BlockPos blockPos, ResourceLocation dimension){
        /*
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
        }*/
    };

    public static void removeAudioInstance(BlockPos blockPos, ResourceLocation dimension){
        DisxAudioInstance toRemove = null;
        try {
            for (DisxAudioInstance instance : registry){
                if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension)){
                    toRemove = instance;
                    instance.deconstruct();
                    break;
                }
            }
            if (toRemove != null){
                registry.remove(toRemove);
            }
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Encountered error in request to remove audio instance:");
            e.printStackTrace();
        }

    }

    public static void modifyAudioInstance(BlockPos blockPos, ResourceLocation dimension, BlockPos newBlockPos, ResourceLocation newDimension, boolean loop, int preferredVolume){
        try {
            for (DisxAudioInstance instance : registry){
                if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension)){
                    instance.setBlockPos(newBlockPos);
                    instance.setDimension(newDimension);
                    instance.setLoop(loop);
                    if (preferredVolume != -1){
                        instance.setPreferredVolume(preferredVolume);
                    }
                    break;
                }
            }
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Encountered error in request to remove audio instance:");
            e.printStackTrace();
        }
    }

    public static void clearAllRegisteredInstances(){
        for (DisxAudioInstance instance : registry){
            instance.deconstruct();
        }
        registry.clear();
    }

    public static void pauseAllRegisteredPlayers(){
        /*
        for (DisxAudioPlayerDetails details : registry){
            if (details != null){
                details.getDisxAudioPlayer().pausePlayer();
               }
        }
        */

    }

    public static void unpauseAllRegisteredPlayers(){
        /*
        for (DisxAudioPlayerDetails details : registry) {
            if (details != null) {
                details.getDisxAudioPlayer().unpausePlayer();
            }
        }
        */
    }

    public static void routeAudioData(ByteBuf buf, BlockPos blockPos, ResourceLocation dimension){
        if (!buf.isReadable()){
            DisxLogger.error("No readable data found in received audio data packet!");
            return;
        }
        try {
            for (DisxAudioInstance instance : registry){
                if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension)){
                    byte[] audioData = new byte[882000];
                    buf.readBytes(audioData);
                    instance.addToPacketDataQueue(audioData);
                    break;
                }
            }
        } catch (ConcurrentModificationException e){
            DisxLogger.error("Encountered error in request to route audio data to audio instance:");
            e.printStackTrace();
        }
    }

    public static void onPlayDisconnect() {
        clearAllRegisteredInstances();
    }

    public static void onClientStopping(Minecraft client) {
        clearAllRegisteredInstances();
        DisxAudioPlayer.shutdownPlayerManager();
    }

    public static void onClientPause(){
        pauseAllRegisteredPlayers();
    }

    public static void onClientUnpause(){
        unpauseAllRegisteredPlayers();
    }

    public static String addToMuted(UUID uuid){
        if (muted.contains(uuid)){
            DisxSystemMessages.mutedAlready();
            return "duplicate";
        } else {
            muted.add(uuid);
            DisxSystemMessages.successfulMutation();
            return "success";
        }
    }

    public static String removeFromMuted(UUID uuid){
        if (muted.contains(uuid)){
            muted.remove(uuid);
            DisxSystemMessages.successfulUnmutation();
            return "success";
        } else {
            DisxSystemMessages.notMuted();
            return "notfoundonit";
        }
    }

    public static boolean isMuted(UUID uuid){
        if (muted.contains(uuid)){
            return true;
        } else {
            return false;
        }
    }

    public static int getPreferredVolume(BlockPos blockPos, ResourceLocation dimension){
        for (DisxAudioInstance instance : registry){
            if (instance.getBlockPos().equals(blockPos) && instance.getDimension().equals(dimension)){
                return instance.getPreferredVolume();
            }
        }
        return -1;
    }
}
