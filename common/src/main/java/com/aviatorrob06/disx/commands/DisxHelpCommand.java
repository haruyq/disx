package com.aviatorrob06.disx.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class DisxHelpCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(
                    Commands.literal("disxhelp")
                            .executes(DisxHelpCommand::run)
            );
        }));
    }

    private static int run(CommandContext<CommandSourceStack> context){
        MutableComponent[] messageLinesNO_OP = {
                Component.literal("Disx Commands:")
                        .withStyle(ChatFormatting.BOLD),
                Component.literal("/disxhelp - Lists all Disx-related commands"),
                Component.literal("/disxinfo - Lists version number and all Disx-associated URLs")
        };
        MutableComponent[] messageLinesOP = {
                Component.literal("Disx Commands:")
                        .withStyle(ChatFormatting.BOLD),
                Component.literal("/disxhelp - Lists all Disx-related commands"),
                Component.literal("/disxinfo - Lists version number and all Disx-associated URLs"),
                Component.literal("/disxgen - Generates custom disc with provided video id"),
                Component.literal("Usage: /disxgen VARIANT VIDEO_ID")
                        .withStyle(ChatFormatting.GRAY),
                Component.literal("/disxsound - Starts playback of provided video id at provided block position"),
                Component.literal("Usage: /disxsound VIDEO_ID X Y Z")
                        .withStyle(ChatFormatting.GRAY)
        };
        Player player = context.getSource().getPlayer();
        if (player != null){
            if (player.hasPermissions(2)){
                for (MutableComponent component : messageLinesOP) {
                    player.sendSystemMessage(component);
                }
            } else {
                for (MutableComponent component : messageLinesNO_OP) {
                    player.sendSystemMessage(component);
                }
            }
        } else {
            for (MutableComponent component : messageLinesOP) {
                context.getSource().getServer().sendSystemMessage(component);
            }
        }
        return 1;
    }
}
