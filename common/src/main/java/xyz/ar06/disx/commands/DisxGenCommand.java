package xyz.ar06.disx.commands;

import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.utils.DisxInternetCheck;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.utils.DisxYoutubeTitleScraper;
import xyz.ar06.disx.commands.suggestionProviders.DisxTypeSuggestionProvider;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.items.DisxCustomDisc;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static xyz.ar06.disx.DisxMain.debug;

public class DisxGenCommand {
    //OLD TITLE SCRAPER
    //static YoutubeDownloader ytDownloader = new YoutubeDownloader();

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(Commands.literal("disxgen")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                            .then(Commands.argument("player", EntityArgument.players())
                                    .then(Commands.argument("discType", StringArgumentType.string()).suggests(DisxTypeSuggestionProvider::getSuggestions)
                                            .then(Commands.argument("videoId", StringArgumentType.string())
                                                    .executes(DisxGenCommand::run)))));
        ;}));
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        Logger logger = LoggerFactory.getLogger("disx");
        if (debug) logger.info("Command Run");
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else
        {
            String argumentResult = context.getArgument("discType", String.class);
            String videoId = context.getArgument("videoId", String.class);
            try {
                boolean hasInternet = DisxInternetCheck.checkInternet();
                if (!hasInternet){
                    throw new Exception("No Internet Connection");
                }
                boolean validDisc = false;
                for (String element : DisxCustomDisc.validTypes) {
                    if (element.equals(argumentResult)){
                        validDisc = true;
                    }
                }
                if (!validDisc){
                    throw new Exception("Invalid Disc Type");
                }
                context.getSource().sendSystemMessage(Component.literal("Your disc is generating, one moment please..."));
                Item disc = DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx", "custom_disc_" + argumentResult));
                ItemStack stack = disc.getDefaultInstance();
                stack.setCount(1);
                CompoundTag stackNbt = stack.getOrCreateTag();
                videoId.replace(" ", "");
                stackNbt.putString("videoId", videoId);
                DisxLogger.debug(videoId);
                //OLD TITLE SCRAPER
                //RequestVideoInfo videoInfoRequest = new RequestVideoInfo(videoId);
                //DisxLogger.debug("videoInfoRequest Generated");
                //Response<VideoInfo> videoInfoResponse = null;
                //DisxLogger.debug("videoInfoResponse initialized");
                //videoInfoResponse = ytDownloader.getVideoInfo(videoInfoRequest);
                String videoTitle = DisxYoutubeTitleScraper.getYouTubeVideoTitle(videoId);
                if (videoTitle.equals("Video Not Found") && DisxConfigHandler.SERVER.getProperty("video_existence_check").equals("true")){
                    throw new Exception("Video Not Found");
                }
                stackNbt.putString("discName", videoTitle);
                stack.setTag(stackNbt);
                Collection<ServerPlayer> playerCollection = EntityArgument.getPlayers(context, "player");
                for (ServerPlayer player : playerCollection){
                    player.playNotifySound(SoundEvents.PISTON_EXTEND, SoundSource.MASTER, 1, 1);
                    player.getInventory().add(stack);
                }
                context.getSource().sendSystemMessage(Component.literal("Your disc has been distributed!"));
                DisxLogger.debug("success??..." + "custom_disc_" + argumentResult);
            } catch (Exception e) {
                if (e.getMessage().equals("Video Not Found")) {
                    if (context.getSource().isPlayer()){
                        DisxSystemMessages.noVideoFound(context.getSource().getPlayer());
                    } else {
                        DisxSystemMessages.noVideoFound(context.getSource().getServer());
                    }
                } else if (e.getMessage().equals("No Internet Connection")) {
                    if (context.getSource().isPlayer()){
                        DisxSystemMessages.noInternetErrorMessage(context.getSource().getPlayer());
                    } else {
                        DisxSystemMessages.noInternetErrorMessage(context.getSource().getServer());
                    }
                } else if (e.getMessage().equals("Invalid Disc Type")){
                    if (context.getSource().isPlayer()){
                        DisxSystemMessages.invalidDiscType(context.getSource().getPlayer());
                    } else {
                        DisxSystemMessages.invalidDiscType(context.getSource().getServer());
                    }
                } else {
                    DisxLogger.debug(e.getMessage() + e.getCause());
                }
            }
        }
        return 1;
    }


}
