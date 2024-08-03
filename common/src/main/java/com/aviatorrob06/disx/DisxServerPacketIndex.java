package com.aviatorrob06.disx;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aviatorrob06.disx.DisxMain.debug;

public class DisxServerPacketIndex {

    public static void registerServerPacketReceivers(){
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","playersuccessstatus"), ((buf, context) -> ServerPacketReceivers.onPlayerSuccessStatusReceive(buf, context)));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","retrieveserverplayerregistry"), (((buf, context) -> ServerPacketReceivers.onPlayerRegistryRequest(buf, context))));
    }

    public class ServerPacketReceivers {

        public static void onPlayerSuccessStatusReceive(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
            Logger logger = LoggerFactory.getLogger("disx");
            if (debug) logger.info("got packet");
            String status = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            String videoId = buf.readUtf();
            Boolean fromSoundCommand = buf.readBoolean();
            Boolean playerCanHear = buf.readBoolean();
            Player player = context.getPlayer();
            if (status.equals("Video Not Found") && (fromSoundCommand.equals(true) || playerCanHear.equals(true))){
                DisxSystemMessages.noVideoFound(player);
            }
            if (status.equals("Failed") && (fromSoundCommand.equals(true) || playerCanHear.equals(true))){
                DisxSystemMessages.errorLoading(player);
            }
            if (status.equals("Playlist") && (fromSoundCommand.equals(true) || playerCanHear.equals(true))){
                DisxSystemMessages.playlistError(player);
            }
            if (status.equals("Success") && (fromSoundCommand.equals(true))){
                DisxSystemMessages.playingAtLocation(player, blockPos, videoId);
            }
        }

        public static void onPlayerRegistryRequest(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String name = "retrieveserverplayerregistry";
            Player player = context.getPlayer();
            DisxServerAudioPlayerRegistry.registry.entrySet().forEach(entry -> {
                int seconds = 0;
                if (DisxServerAudioPlayerRegistry.timerRegistry.containsKey(entry.getKey())){
                    seconds = (int) DisxServerAudioPlayerRegistry.timerRegistry.get(entry.getKey()).elapsedSeconds;
                }
                ServerPackets.playerRegistryEvent("add", player, entry.getKey(), entry.getValue(), false, seconds);
            });
        }
    }

    public class ServerPackets {

        public static void playerRegistryEvent(String type, Player player, BlockPos pos, String videoId, Boolean fromSoundCommand, int seconds){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(type);
            buf.writeBlockPos(pos);
            buf.writeUtf(videoId);
            buf.writeBoolean(fromSoundCommand);
            buf.writeInt(seconds);
            NetworkManager.sendToPlayer((ServerPlayer) player, new ResourceLocation("disx","serveraudioregistryevent"), buf);
        }

        public static void nowPlayingMessage(String videoId, Player player){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(videoId);
            NetworkManager.sendToPlayer((ServerPlayer) player, new ResourceLocation("disx","nowplayingmsg"), buf);
        }

    }

}
