package xyz.ar06.disx.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;

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
}
