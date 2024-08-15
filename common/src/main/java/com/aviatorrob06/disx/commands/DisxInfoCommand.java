package com.aviatorrob06.disx.commands;

import com.aviatorrob06.disx.DisxModInfo;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;

public class DisxInfoCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(
                    Commands.literal("disxinfo")
                            .executes(DisxInfoCommand::run)
            );
        }));
    }

    public static int run(CommandContext<CommandSourceStack> context){
        CommandSourceStack source = context.getSource();
        ArrayList<MutableComponent> messages = new ArrayList<>();
        messages.add(Component.literal("Disx Info:").withStyle(ChatFormatting.BOLD));
        messages.add(Component.literal("Version: " + DisxModInfo.getVERSION()).withStyle(ChatFormatting.ITALIC));
        messages.add(Component.literal("--URLs--"));
        MutableComponent link1 = Component.literal("Modrinth")
                .withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.UNDERLINE).withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DisxModInfo.getModrinthUrl()))
        );
        MutableComponent link2 = Component.literal("CurseForge")
                .withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE).withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DisxModInfo.getCurseforgeUrl())));
        MutableComponent link3 = Component.literal("GitHub")
                .withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.UNDERLINE).withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DisxModInfo.getGithubUrl())));
        MutableComponent link4 = Component.literal("Discord")
                .withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE).withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DisxModInfo.getDiscordUrl())));
        MutableComponent link5 = Component.literal("Road Map")
                .withStyle(ChatFormatting.DARK_AQUA).withStyle(ChatFormatting.UNDERLINE).withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DisxModInfo.getRoadmapUrl())));
        MutableComponent spacing = Component.literal(" // ");
        MutableComponent linkMessage1 = Component.empty().append(link1).append(spacing).append(link2);
        MutableComponent linkMessage2 = Component.empty().append(link3).append(spacing).append(link5);
        MutableComponent linkMessage3 = Component.empty().append(link4);
        messages.add(linkMessage1);
        messages.add(Component.empty());
        messages.add(linkMessage2);
        messages.add(Component.empty());
        messages.add(linkMessage3);
        for (MutableComponent message : messages){
            source.sendSystemMessage(message);
        }
        return 1;
    }

}
