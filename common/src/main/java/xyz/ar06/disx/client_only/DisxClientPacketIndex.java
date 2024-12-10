package xyz.ar06.disx.client_only;

import io.netty.buffer.ByteBuf;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.client_only.gui.screens.DisxStampMakerGUI;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class DisxClientPacketIndex  {

    static Logger logger = LoggerFactory.getLogger("disx");
    public static void registerClientPacketReceivers() {
        //NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","audioplayerplayevent"), (ClientPacketReceivers::receiveAudioPlayerPlayEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","serveraudioregistryevent"), (ClientPacketReceivers::receiveServerAudioPlayerRegistryEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","loadingvidmsg"), (ClientPacketReceivers::receiveLoadingMsgEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","openvideoidscreen"), (ClientPacketReceivers::receiveOpenVideoIdScreenEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx", "muteplayer"), ClientPacketReceivers::receivePlayerMute);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","audiodata"), ClientPacketReceivers::receiveAudioData);
    }

    public class ClientPacketReceivers{

        /* MARK FOR REMOVAL
        public static void receiveAudioPlayerPlayEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
            String videoId = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            Boolean fromSoundCommand = buf.readBoolean();
            int seconds = 0;
            DisxAudioPlayer newAudioPlayer = null;

            newAudioPlayer = new DisxAudioPlayer(blockPos, videoId, fromSoundCommand, seconds);
        }
         */

        public static void receiveServerAudioPlayerRegistryEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            DisxLogger.debug("got audio registry event");
            String type = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            String videoId = buf.readUtf();
            Boolean fromSoundCommand = buf.readBoolean();
            int seconds = buf.readInt();
            ResourceLocation dimensionLocation = buf.readResourceLocation();
            UUID playerOwner = buf.readUUID();
            Boolean loop = buf.readBoolean();
            BlockPos newBlockPos = buf.readBlockPos();
            ResourceLocation newDimLocation = buf.readResourceLocation();
            if (type.equals("add")){
                DisxLogger.debug("calling for add");
                CompletableFuture.runAsync(() -> {
                    DisxAudioPlayerRegistry.newAudioPlayer(blockPos, videoId, fromSoundCommand, seconds, dimensionLocation, playerOwner, loop);
                });
            }
            if (type.equals("remove")){
                CompletableFuture.runAsync(() -> {
                    DisxAudioPlayerRegistry.deregisterAudioPlayer(blockPos, dimensionLocation);
                });
            }
            if (type.equals("modify")){
                CompletableFuture.runAsync(() -> {
                    DisxAudioPlayerRegistry.modifyAudioPlayer(blockPos, dimensionLocation, newBlockPos, newDimLocation, loop);
                });
            }
        }

        public static void receiveLoadingMsgEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String videoId = buf.readUtf();
            DisxSystemMessages.loadingVideo(videoId);
        }

        public static void receiveOpenVideoIdScreenEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            BlockPos blockPos = buf.readBlockPos();
            Minecraft.getInstance().execute(() -> {
                DisxStampMakerGUI.setScreen(blockPos);
            });
        }

        public static void receivePlayerMute(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            UUID uuid = buf.readUUID();
            String action = buf.readUtf();
            if (action.equals("add")){
                DisxAudioPlayerRegistry.addToMuted(uuid);
            } else if (action.equals("remove")){
                DisxAudioPlayerRegistry.removeFromMuted(uuid);
            }
        }

        public static DisxAudioLine audioLine = new DisxAudioLine();

        public static void receiveAudioData(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            DisxLogger.debug("audio data packet received");
            //buf.readBytes(new byte[882000]);
            ByteBuf bufCopy = buf.copy();
            CompletableFuture.runAsync(() -> audioLine.writeToLine(bufCopy));
        }

    }

    public class ClientPackets{
        public static void playerSuccessStatus(String status, BlockPos blockPos, String videoId, Boolean fromSoundCommand, Boolean playerCanHear){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(status);
            buf.writeBlockPos(blockPos);
            buf.writeUtf(videoId);
            buf.writeBoolean(fromSoundCommand);
            buf.writeBoolean(playerCanHear);
            NetworkManager.sendToServer(new ResourceLocation("disx","playersuccessstatus"), buf);
        }

        public static void getServerPlayerRegistry(){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            NetworkManager.sendToServer(new ResourceLocation("disx","retrieveserverplayerregistry"), buf);
        }

        public static void pushVideoId(String videoId, BlockPos blockPos){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(videoId);
            buf.writeBlockPos(blockPos);
            NetworkManager.sendToServer(
                    new ResourceLocation("disx","videoidselection"),
                    buf
            );
        }

        public static void singleplayerTrackEnd(BlockPos blockPos, ResourceLocation dimension){
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(blockPos);
            buf.writeResourceLocation(dimension);
            NetworkManager.sendToServer(
                    new ResourceLocation("disx","singleplayertrackend"),
                    buf
            );
        }

    }

}
