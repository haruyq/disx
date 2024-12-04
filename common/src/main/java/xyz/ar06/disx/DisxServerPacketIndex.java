package xyz.ar06.disx;

import xyz.ar06.disx.blocks.DisxStampMaker;
import xyz.ar06.disx.entities.DisxStampMakerEntity;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.VolatileImage;
import java.util.UUID;

import static xyz.ar06.disx.DisxMain.debug;

public class DisxServerPacketIndex {

    public static void registerServerPacketReceivers(){
        //NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","playersuccessstatus"), (ServerPacketReceivers::onPlayerSuccessStatusReceive));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","retrieveserverplayerregistry"), ((ServerPacketReceivers::onPlayerRegistryRequest)));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","videoidselection"), ServerPacketReceivers::onVideoIdPushRequest);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation("disx","singleplayertrackend"), ServerPacketReceivers::onSingleplayerTrackEnd);

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
            if (status.equals("Success") && (fromSoundCommand.equals(true))){
                //DisxSystemMessages.playingAtLocation(player, blockPos, videoId);
            }
        }

        public static void onPlayerRegistryRequest(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String name = "retrieveserverplayerregistry";
            Player player = context.getPlayer();
            for (DisxServerAudioPlayerDetails details : DisxServerAudioPlayerRegistry.registry){
                DisxLogger.debug("got registry grab request");
                int seconds = (int) details.getVideoTimer().elapsedSeconds;
                BlockPos blockPos = details.getBlockPos();
                ResourceLocation dimensionLocation = details.getDimension();
                String videoId = details.getVideoId();
                UUID playerOwner = details.getAudioPlayerOwner();
                boolean loop = details.isLoop();
                ServerPackets.playerRegistryEvent("add", player, blockPos, videoId, false,  seconds, dimensionLocation, playerOwner, loop, blockPos, dimensionLocation);
                DisxLogger.debug("sent registry add event");
            }
        }

        public static void onVideoIdPushRequest(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String videoId = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            MinecraftServer server = context.getPlayer().getServer();
            server.executeIfPossible(() -> {
               BlockEntity entity = context.getPlayer().level().getBlockEntity(blockPos);
               if (entity == null){
                   DisxMain.LOGGER.info("ENTITY IS NULL");
               } else {
                   DisxMain.LOGGER.info("ENTITY IS NOT NULL");
                   if (entity instanceof DisxStampMakerEntity){
                       ((DisxStampMakerEntity) entity).setVideoId(videoId, context.getPlayer());
                       DisxMain.LOGGER.info("SET VIDEO ID TO " + videoId);
                   }
               }
            });
        }

        public static void onSingleplayerTrackEnd(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            BlockPos blockPos = buf.readBlockPos();
            ResourceLocation dimension = buf.readResourceLocation();
            DisxServerAudioPlayerRegistry.removeFromRegistry(blockPos, dimension);
        }
    }

    public class ServerPackets {

        public static void playerRegistryEvent(String type, Player player, BlockPos pos, String videoId, boolean serverOwned, int seconds, ResourceLocation dimension, UUID playerOwner, boolean loop,
                                               BlockPos newBlockPos, ResourceLocation newDimension){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(type);
            buf.writeBlockPos(pos);
            buf.writeUtf(videoId);
            buf.writeBoolean(serverOwned);
            buf.writeInt(seconds);
            buf.writeResourceLocation(dimension);
            buf.writeUUID(playerOwner);
            buf.writeBoolean(loop);
            buf.writeBlockPos(newBlockPos);
            buf.writeResourceLocation(newDimension);
            NetworkManager.sendToPlayer((ServerPlayer) player, new ResourceLocation("disx","serveraudioregistryevent"), buf);
        }

        public static void loadingVideoIdMessage(String videoId, Player player){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(videoId);
            NetworkManager.sendToPlayer((ServerPlayer) player, new ResourceLocation("disx","loadingvidmsg"), buf);
        }

        public static void openVideoIdScreen(Player player, BlockPos blockPos){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(blockPos);
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx","openvideoidscreen"),
                    buf
            );
        }

        public static void mutePlayer(Player player, UUID toMute){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(toMute);
            buf.writeUtf("add");
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx","muteplayer"),
                    buf
            );
        }

        public static void unmutePlayer(Player player, UUID toUnmute){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(toUnmute);
            buf.writeUtf("remove");
            NetworkManager.sendToPlayer(
                    (ServerPlayer) player,
                    new ResourceLocation("disx","muteplayer"),
                    buf
            );
        }

    }

}
