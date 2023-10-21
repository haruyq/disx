package com.aviatorrob06.disx.commands;

import com.aviatorrob06.disx.DisxServerAudioPlayerRegistry;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DisxSoundCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(Commands.literal("disxsound")
            .then(Commands.argument("videoId", StringArgumentType.string()).then(Commands.argument("position", BlockPosArgument.blockPos()).executes(DisxSoundCommand::run))));
        }));
    }

    private static int run(CommandContext<CommandSourceStack> context) {

        Logger logger = LoggerFactory.getLogger("disx");
        String videoId = context.getArgument("videoId", String.class);
        BlockPos blockPos = BlockPosArgument.getBlockPos(context, "position");
        try {
            HttpRequest testRequest = HttpRequest.newBuilder().uri(new URI("http://www.google.com")).build();
            HttpResponse testResponse = null;
            testResponse = HttpClient.newHttpClient().send(testRequest, HttpResponse.BodyHandlers.ofString());
            if (testResponse == null){
                throw new Exception("No Internet Connection");
            }
            context.getSource().sendSystemMessage(Component.literal("One moment please..."));
            /*ServerPlayerEntity playerEntity = context.getSource().getPlayer();
            PacketByteBuf audioPlayBuf = PacketByteBufs.create();
            audioPlayBuf.writeString(videoId);
            audioPlayBuf.writeBlockPos(blockPos);
            audioPlayBuf.writeBoolean(true);
            ServerPlayNetworking.send(playerEntity, new Identifier("disx","audioplayerplayevent"), audioPlayBuf);
             */
            DisxServerAudioPlayerRegistry.addToRegistry(blockPos, videoId, true, context.getSource().getPlayer());
            //DisxServerPacketIndex.ServerPackets.playerRegistryEvent("add",context.getSource().getPlayer(), blockPos, videoId, true);
        } catch (Exception e){
            if (e.getMessage().equals("Video Not Found")) {
                context.getSource().sendFailure(Component.literal("Video Not Found!"));
            }
            if (e.getMessage().equals("No Internet Connection")) {
                context.getSource().sendFailure(Component.literal("No Internet Connection"));
            }
        }
        return 1;
    }

}
