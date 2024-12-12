package xyz.ar06.disx.commands;

import xyz.ar06.disx.DisxServerAudioRegistry;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DisxForceStopCommand {
    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("disxforcestop")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .executes(DisxForceStopCommand::run));
        }));
    }
    private static int run(CommandContext<CommandSourceStack> context){
        if (context.getSource().hasPermission(2)){
            context.getSource().sendSystemMessage(Component.literal("You do not have permission to do that!").withStyle(ChatFormatting.RED));
            return 1;
        }
        DisxServerAudioRegistry.forceStopAll();
        context.getSource().sendSystemMessage(Component.literal("Force stopping all audios currently playing!"));
        return 1;
    }
}
