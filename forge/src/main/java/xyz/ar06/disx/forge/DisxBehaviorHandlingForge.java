package xyz.ar06.disx.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;
import xyz.ar06.disx.config.DisxConfigHandler;

public class DisxBehaviorHandlingForge {
    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent event){
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null){
            if (hitResult.getType().equals(HitResult.Type.BLOCK)){
                Level level = Minecraft.getInstance().level;
                if (level != null){
                    BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                    if (level.getBlockState(blockPos).getBlock().equals(DisxAdvancedJukebox.blockRegistration.get())){
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    String debugChatKeyFallback = "vegan vegan vegan";
    String undebugChatKeyFallback = "steak steak steak";
    @SubscribeEvent
    public void onChatSend(ClientChatEvent event){
        String s = event.getMessage();
        String debugChatKey = Component.translatableWithFallback("key.disx.debug", debugChatKeyFallback).getString();
        String undebugChatKey = Component.translatableWithFallback("key.disx.undebug", undebugChatKeyFallback).getString();
        if (s.equalsIgnoreCase(debugChatKey)){
            boolean currentValue = Boolean.parseBoolean(DisxConfigHandler.CLIENT.getProperty("debug_mode"));
            if (!currentValue){
                DisxConfigHandler.CLIENT.updateProperty("debug_mode", "true");
                DisxSystemMessages.debugStatus(true);
                event.setCanceled(true);
            }
        }
        if (s.equalsIgnoreCase(undebugChatKey)){
            boolean currentValue = Boolean.parseBoolean(DisxConfigHandler.CLIENT.getProperty("debug_mode"));
            if (currentValue){
                DisxConfigHandler.CLIENT.updateProperty("debug_mode", "false");
                DisxSystemMessages.debugStatus(false);
                event.setCanceled(true);
            }
        }
    }
}
