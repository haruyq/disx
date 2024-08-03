package com.aviatorrob06.disx.client_only;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.Set;

public class DisxClientPacketIndex  {

    static Logger logger = LoggerFactory.getLogger("disx");
    public static void registerClientPacketReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","audioplayerplayevent"), ((buf, context) -> ClientPacketReceivers.receiveAudioPlayerPlayEvent(buf, context)));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","serveraudioregistryevent"), ((buf, context) -> ClientPacketReceivers.receiveServerAudioPlayerRegistryEvent(buf, context)));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation("disx","nowplayingmsg"), ((buf, context) -> ClientPacketReceivers.receivePlayMsgEvent(buf, context)));
    }

    public class ClientPacketReceivers{

        public static void receiveAudioPlayerPlayEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
            String videoId = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            Boolean fromSoundCommand = buf.readBoolean();
            int seconds = 0;
            DisxAudioPlayer newAudioPlayer = null;

            newAudioPlayer = new DisxAudioPlayer(blockPos, videoId, fromSoundCommand, seconds);
        }

        public static void receiveServerAudioPlayerRegistryEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String type = buf.readUtf();
            BlockPos blockPos = buf.readBlockPos();
            String videoId = buf.readUtf();
            Boolean fromSoundCommand = buf.readBoolean();
            int seconds = buf.readInt();
            if (type.equals("add")){
                DisxAudioPlayerRegistry.newAudioPlayer(blockPos, videoId, fromSoundCommand, seconds);
            }
            if (type.equals("remove")){
                DisxAudioPlayerRegistry.deregisterAudioPlayer(blockPos);
            }
        }

        public static void receivePlayMsgEvent(FriendlyByteBuf buf, NetworkManager.PacketContext context){
            String videoId = buf.readUtf();
            Minecraft.getInstance().gui.setNowPlaying(Component.literal("Video '" + videoId + "'"));
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

    }

}
