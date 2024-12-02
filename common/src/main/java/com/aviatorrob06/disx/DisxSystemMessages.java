package com.aviatorrob06.disx;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
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

    public static void noInternetErrorMessage(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: No internet connection!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void errorLoading(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: Unknown error loading video!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void errorLoading(LocalPlayer player){
        player.sendSystemMessage(Component.literal("Disx Error: Unknown error loading video!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void apiError(LocalPlayer player){
        player.sendSystemMessage(Component.literal("Disx Error: Invalid or incomplete response from Disx YT-SRC API. Check client logs for more information.").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void noVideoFound(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: No video found!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void noVideoFound(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: No video found!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void noVideoFound(LocalPlayer player){
        player.sendSystemMessage(Component.literal("Disx Error: No video found!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void playingAtLocation(MinecraftServer server, String playerName, BlockPos pos, String videoId, ResourceLocation dimension){
        server.sendSystemMessage(Component.literal("" + playerName + " is playing Video '" + videoId +"' at " + pos.toString() + " in " + dimension.toString()));
    }

    public static void invalidDiscType(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: Invalid disc type provided!").withStyle(ChatFormatting.RED));
    }

    public static void invalidDiscType(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: Invalid disc type provided!").withStyle(ChatFormatting.RED));
    }

    public static void blacklistedByServer(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: You've been blacklisted by server administration!").withStyle(ChatFormatting.RED));
    }

    public static void notWhitelistedByServer(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: Player-use whitelist is enabled, and you are not whitelisted.").withStyle(ChatFormatting.RED));
    }

    public static void dimensionBlacklisted(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: You are not allowed to play audio in this dimension, per server administration!").withStyle(ChatFormatting.RED));
    }

    public static void maxAudioPlayerCtReached(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: Maximum number of audio players reached in this server! Please contact a server admin if you believe this to be an error.").withStyle(ChatFormatting.RED));
    }

    public static void maxAudioPlayerCtReached(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: Maximum number of audio players defined in disx server config has been reached!").withStyle(ChatFormatting.RED));
    }

    public static void noInternetFoundStampMakerAsync(MinecraftServer server, BlockPos blockPos){
        server.sendSystemMessage(Component.literal("Disx Error: Tried to make Record Stamp at " + blockPos.toString() + " but no internet connection has been found!"));
    }

    public static void videoNotFoundStampMakerAsync(MinecraftServer server, BlockPos blockPos){
        server.sendSystemMessage(Component.literal("Disx Error: Tried to make Record Stamp at " + blockPos.toString() + " but no video has been found for provided video id!"));
    }

    public static void ageRestrictionEnabled(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: This video is age restricted, and age restricted playback is not allowed on this server!").withStyle(ChatFormatting.RED));
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
