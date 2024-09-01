package com.aviatorrob06.disx.client_only;

import com.aviatorrob06.disx.client_only.gui.screens.DisxStampMakerGUI;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

@Environment(EnvType.CLIENT)
public class DisxClientPacketIndex  {

    static Logger logger = LoggerFactory.getLogger("disx");
    public static void registerClientPacketReceivers() {
        //NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","audioplayerplayevent"), (ClientPacketReceivers::receiveAudioPlayerPlayEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","serveraudioregistryevent"), (ClientPacketReceivers::receiveServerAudioPlayerRegistryEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","nowplayingmsg"), (ClientPacketReceivers::receivePlayMsgEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","openvideoidscreen"), (ClientPacketReceivers::receiveOpenVideoIdScreenEvent));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx", "muteplayer"), ClientPacketReceivers::receivePlayerMute);
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
            System.out.println("got audio registry event");
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
                System.out.println("calling for add");
                DisxAudioPlayerRegistry.newAudioPlayer(blockPos, videoId, fromSoundCommand, seconds, dimensionLocation, playerOwner, loop);
            }
            if (type.equals("remove")){
                DisxAudioPlayerRegistry.deregisterAudioPlayer(blockPos, dimensionLocation);
            }
            if (type.equals("modify")){
                DisxAudioPlayerRegistry.modifyAudioPlayer(blockPos, dimensionLocation, newBlockPos, newDimLocation, loop);
            }
        }

        public static void receivePlayMsgEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String videoId = buf.readUtf();
            Minecraft.getInstance().gui.setNowPlaying(Component.literal("Video '" + videoId + "'"));
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

    }

}
