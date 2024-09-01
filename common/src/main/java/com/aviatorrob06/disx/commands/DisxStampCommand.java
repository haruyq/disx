package com.aviatorrob06.disx.commands;

import com.aviatorrob06.disx.config.DisxConfigHandler;
import com.aviatorrob06.disx.items.DisxRecordStamp;
import com.aviatorrob06.disx.utils.DisxYoutubeTitleScraper;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class DisxStampCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("disxstamp")
                            .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.players())
                            .then(Commands.argument("videoId", StringArgumentType.string())
                                    .executes(DisxStampCommand::run))));
        }));
    }
    private static int run(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You do not have permission to do that!"));
            return 1;
        } else {
            context.getSource().sendSystemMessage(Component.literal("Your stamp is generating, one moment please..."));
            String videoId = context.getArgument("videoId", String.class);
            Collection<ServerPlayer> playerCollection;
            try {
                playerCollection = EntityArgument.getPlayers(context, "player");
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                context.getSource().sendFailure(Component.literal("Disx Error: Unable to get player(s) provided!"));
                throw new RuntimeException(e);
            }
            String videoName = DisxYoutubeTitleScraper.getYouTubeVideoTitle(videoId);
            if (videoName.equals("Video Not Found") && DisxConfigHandler.SERVER.getProperty("video_existence_check").equals("true")) {
                context.getSource().sendFailure(Component.literal("Disx Error: Video not found!"));
                return 1;
            }
            Item item = DisxRecordStamp.getItemRegistration().get();
            ItemStack recordStampStack = new ItemStack(item);
            recordStampStack.setCount(1);
            CompoundTag tag = new CompoundTag();
            tag.putString("videoId", videoId);
            tag.putString("videoName", videoName);
            recordStampStack.setTag(tag);
            for (ServerPlayer plr : playerCollection){
                plr.playNotifySound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.MASTER, 1, 1);
                plr.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1f, 1f);
                plr.addItem(recordStampStack);
            }
            context.getSource().sendSystemMessage(Component.literal("Your stamp has been distributed!"));
        }
        return 1;
    }
}
