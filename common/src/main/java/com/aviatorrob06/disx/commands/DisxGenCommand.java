package com.aviatorrob06.disx.commands;

import com.aviatorrob06.disx.DisxMain;
import com.aviatorrob06.disx.commands.suggestionProviders.DisxTypeSuggestionProvider;
import com.aviatorrob06.disx.items.DisxCustomDisc;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisxGenCommand {
    static YoutubeDownloader ytDownloader = new YoutubeDownloader();

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(Commands.literal("disxgen")
                    .then(Commands.argument("discType", StringArgumentType.string()).suggests(DisxTypeSuggestionProvider::getSuggestions)
                            .then(Commands.argument("videoId", StringArgumentType.string()).executes(DisxGenCommand::run))));
        ;}));
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        Logger logger = LoggerFactory.getLogger("disx");
        logger.info("Command Run");
        String argumentResult = context.getArgument("discType", String.class);
        String videoId = context.getArgument("videoId", String.class);
        context.getSource().sendSystemMessage(Component.literal("Your disc is generating, one moment please..."));
        try {
            boolean validDisc = false;
            for (String element : DisxCustomDisc.validTypes) {
                if (element.equals(argumentResult)){
                    validDisc = true;
                }
            }
            Item disc = DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx", "custom_disc_" + argumentResult));
            if (!validDisc){
                throw new Exception("Invalid Disc Type");
            }
            ItemStack stack = disc.getDefaultInstance();
            stack.setCount(1);
            CompoundTag stackNbt = stack.getOrCreateTag();
            videoId.replace(" ", "");
            stackNbt.putString("videoId", videoId);
            System.out.println(videoId);
            RequestVideoInfo videoInfoRequest = new RequestVideoInfo(videoId);
            Response<VideoInfo> videoInfoResponse = ytDownloader.getVideoInfo(videoInfoRequest);
            VideoInfo videoInfo = videoInfoResponse.data();
            if (!videoInfoResponse.ok()){
                throw new Exception("Video Not Found");
            }
            String videoTitle = videoInfo.details().title();
            stack.setTag(stackNbt);
            stack.setHoverName(Component.literal(videoTitle));
            context.getSource().getPlayer().getInventory().add(stack);
            System.out.println("success??..." + "custom_disc_" + argumentResult);
        } catch (Exception e) {
            if (e.getMessage().equals("Video Not Found")) {
                context.getSource().sendFailure(Component.literal("Video Not Found!"));
            } else if (e.getMessage().equals("No Internet Connection")) {
                context.getSource().sendFailure(Component.literal("No Internet Connection"));
            } else if (e.getMessage().equals("Invalid Disc Type")){
                context.getSource().sendFailure(Component.literal("Invalid Disc Type!"));
            } else {
                System.out.println(e.getMessage() + e.getCause());
            }
        }
        return 1;
    }


}
