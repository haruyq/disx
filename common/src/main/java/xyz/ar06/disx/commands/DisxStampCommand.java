package xyz.ar06.disx.commands;

import net.minecraft.ChatFormatting;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.items.DisxRecordStamp;
import xyz.ar06.disx.utils.DisxYoutubeInfoScraper;
import xyz.ar06.disx.utils.DisxYoutubeTitleScraper;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

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
            CompletableFuture.runAsync(() -> runAsync(context));
        }
        return 1;
    }

    private static void runAsync(CommandContext<CommandSourceStack> context){
        String videoId = context.getArgument("videoId", String.class);
        Collection<ServerPlayer> playerCollection;
        try {
            playerCollection = EntityArgument.getPlayers(context, "player");
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            context.getSource().sendFailure(Component.literal("Disx Error: Unable to get player(s) provided!"));
            throw new RuntimeException(e);
        }
        ArrayList<String> title_and_length = DisxYoutubeInfoScraper.scrapeLengthAndTitle(videoId);
        String videoName = title_and_length.get(0);
        if (videoName.equals("Video Not Found") && DisxConfigHandler.SERVER.getProperty("video_existence_check").equals("true")) {
            context.getSource().sendFailure(Component.literal("Disx Error: Video not found!"));
            return;
        }
        int videoLength = Integer.valueOf(title_and_length.get(1));
        if (videoLength > 1800) {
            context.getSource().sendFailure(Component.literal("Disx Error: Video length too long! (Max Length: 30 m)"));
            return;
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
}
