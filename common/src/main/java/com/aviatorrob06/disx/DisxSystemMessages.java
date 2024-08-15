package com.aviatorrob06.disx;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;

public class DisxSystemMessages {
    public static void onPlayReadyNoInternet(Player player) {
        player.sendSystemMessage(Component.literal("Disx Warning: no internet connection found!").withStyle(ChatFormatting.RED));
    }

    public static void noInternetErrorMessage(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: No internet connection!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void errorLoading(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: Unknown error loading video!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));

    }

    public static void noVideoFound(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: No video found!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));

    }

    public static void playlistError(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: Playlists cannot be used!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));

    }

    public static void playingAtLocation(Player player, BlockPos pos, String videoId){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Video '" + videoId +"' playing at " + pos.toString()));
        serverPlayer.server.sendSystemMessage(Component.literal(player.getName().getString() + " is playing video " + videoId + " at " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
    }

    public static void invalidDiscType(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: Invalid disc type provided!"));
    }

    public static void outdatedModVersion(MinecraftServer server){
        if (!DisxModInfo.getIsUpToDate()){
            ArrayList<MutableComponent> messages = new ArrayList<MutableComponent>();
            messages.add(Component.literal("Disx Notice: Mod is Outdated!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            messages.add(Component.literal(
                    "Installed Version: "
                            + DisxModInfo.getVERSION()
                            + ", Latest Version: " + DisxModInfo.getLatestVersion()
            ).withStyle(ChatFormatting.GRAY));
            messages.add(Component.literal("You are " + Integer.toString(DisxModInfo.getVersionsOutdated()) + " version(s) behind!").withStyle(ChatFormatting.ITALIC));
            messages.add(Component.literal("Please consider updating for the best mod functionality!"));
            messages.add(Component.literal("Use /disxinfo to get a download url if applicable!"));
            if (server.isSingleplayer()){
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    for (MutableComponent component : messages){
                        player.sendSystemMessage(component);
                    }
                });
            } else {
                for (MutableComponent component : messages){
                    server.sendSystemMessage(component);
                }
            }
        }
    }
}
