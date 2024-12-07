package xyz.ar06.disx.commands;

import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.utils.DisxInternetCheck;
import xyz.ar06.disx.DisxServerAudioPlayerRegistry;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.utils.DisxYoutubeInfoScraper;
import xyz.ar06.disx.utils.DisxYoutubeTitleScraper;
import xyz.ar06.disx.config.DisxConfigHandler;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class DisxSoundCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(Commands.literal("disxsound")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .then(Commands.argument("videoId", StringArgumentType.string())
                            .then(Commands.argument("dimension", DimensionArgument.dimension())
                                    .then(Commands.argument("position", BlockPosArgument.blockPos())
                                            .then(Commands.argument("startTime", IntegerArgumentType.integer()).executes(DisxSoundCommand::run))))));
        }));
    }

    private static int run(CommandContext<CommandSourceStack> context) {

        Logger logger = LoggerFactory.getLogger("disx");
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else
        {
            if (context.getSource().isPlayer()){
                if (DisxConfigHandler.SERVER.isOnUseBlacklist(context.getSource().getPlayer().getUUID())){
                    DisxSystemMessages.blacklistedByServer(context.getSource().getPlayer());
                    return 1;
                }
                if (!DisxConfigHandler.SERVER.isOnUseWhitelist(context.getSource().getPlayer().getUUID())){
                    DisxSystemMessages.notWhitelistedByServer(context.getSource().getPlayer());
                    return 1;
                }
            }
            int currentAudioPlayerCount = DisxServerAudioPlayerRegistry.getRegistryCount();
            int maxAudioPlayerCount = Integer.valueOf(DisxConfigHandler.SERVER.getProperty("max_audio_players"));
            if (currentAudioPlayerCount >= maxAudioPlayerCount){
                if (context.getSource().isPlayer()){
                    DisxSystemMessages.maxAudioPlayerCtReached(context.getSource().getPlayer());
                } else {
                    DisxSystemMessages.maxAudioPlayerCtReached(context.getSource().getServer());
                }
            }
            String videoId = context.getArgument("videoId", String.class);
            ResourceLocation dimension = context.getArgument("dimension", ResourceLocation.class);
            BlockPos blockPos = BlockPosArgument.getBlockPos(context, "position");
            Integer startTime = context.getArgument("startTime", Integer.class);
            DisxLogger.debug("START TIME PROVIDED: " + startTime);
            try {
                boolean hasInternet = DisxInternetCheck.checkInternet();
                if (!hasInternet){
                    throw new Exception("No Internet Connection");
                }
                String videoTitle = DisxYoutubeInfoScraper.scrapeTitle(videoId);
                if (videoTitle.equals("Video Not Found") && DisxConfigHandler.SERVER.getProperty("video_existence_check").equals("true")){
                    throw new Exception("Video Not Found");
                }
                CompletableFuture.runAsync(() -> context.getSource().sendSystemMessage(Component.literal("One moment please...")));
                if (!context.getSource().isPlayer()){
                    DisxServerAudioPlayerRegistry.addToRegistry(blockPos, videoId, true, null, context.getSource().getServer(), dimension, startTime.intValue(), false);
                } else {
                    DisxServerAudioPlayerRegistry.addToRegistry(blockPos, videoId, true, context.getSource().getPlayer(), context.getSource().getServer(), dimension, startTime.intValue(), false);
                }
                context.getSource().sendSystemMessage(Component.literal("Attempting to start playback of Video Id '" + videoId + "' at " + blockPos + " in " + dimension));
            } catch (Exception e){
                if (e.getMessage().equals("Video Not Found")) {
                    if (context.getSource().isPlayer()){
                        DisxSystemMessages.noVideoFound(context.getSource().getPlayer());
                    } else {
                        DisxSystemMessages.noVideoFound(context.getSource().getServer());
                    }
                }
                if (e.getMessage().equals("No Internet Connection")) {
                    if (context.getSource().isPlayer()){
                        DisxSystemMessages.noInternetErrorMessage(context.getSource().getPlayer());
                    } else {
                        DisxSystemMessages.noInternetErrorMessage(context.getSource().getServer());
                    }
                }
            }
        }
        return 1;
    }

}
