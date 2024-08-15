package com.aviatorrob06.disx.commands;

import com.aviatorrob06.disx.DisxInternetCheck;
import com.aviatorrob06.disx.DisxMain;
import com.aviatorrob06.disx.DisxSystemMessages;
import com.aviatorrob06.disx.DisxYoutubeTitleScraper;
import com.aviatorrob06.disx.commands.suggestionProviders.DisxTypeSuggestionProvider;
import com.aviatorrob06.disx.items.DisxCustomDisc;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.aviatorrob06.disx.DisxMain.debug;

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
                if (debug) System.out.println(videoId);
                //OLD TITLE SCRAPER
                //RequestVideoInfo videoInfoRequest = new RequestVideoInfo(videoId);
                //if (debug) System.out.println("videoInfoRequest Generated");
                //Response<VideoInfo> videoInfoResponse = null;
                //if (debug) System.out.println("videoInfoResponse initialized");
                //videoInfoResponse = ytDownloader.getVideoInfo(videoInfoRequest);
                String videoTitle = DisxYoutubeTitleScraper.getYouTubeVideoTitle(videoId);
                if (videoTitle.equals("Video Not Found")){
                    throw new Exception("Video Not Found");
                }
                stackNbt.putString("discName", videoTitle);
                stack.setTag(stackNbt);
                Collection<ServerPlayer> playerCollection = EntityArgument.getPlayers(context, "player");
                for (ServerPlayer player : playerCollection){
                    player.playNotifySound(SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1, 1);
                    player.getInventory().add(stack);
                }
                context.getSource().sendSystemMessage(Component.literal("Your disc has been distributed!"));
                if (debug) System.out.println("success??..." + "custom_disc_" + argumentResult);
            } catch (Exception e) {
                if (e.getMessage().equals("Video Not Found")) {
                    System.out.println("Video Not Found");
                    DisxSystemMessages.noVideoFound(context.getSource().getPlayer());
                } else if (e.getMessage().equals("No Internet Connection")) {
                    System.out.println("No Internet Connection");
                    DisxSystemMessages.noInternetErrorMessage(context.getSource().getPlayer());
                } else if (e.getMessage().equals("Invalid Disc Type")){
                    System.out.println("Invalid Disc Type");
                    DisxSystemMessages.invalidDiscType(context.getSource().getPlayer());
                } else {
                    System.out.println(e.getMessage() + e.getCause());
                }
            }
        }
        return 1;
    }


}
