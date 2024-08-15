package com.aviatorrob06.disx.fabric;

import com.aviatorrob06.disx.blocks.DisxLacquerBlock;
import com.aviatorrob06.disx.blocks.DisxStampMaker;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.utils.Env;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

import java.util.logging.Logger;

public class DisxClientMainFabric {
    public static void onInitializeClient() {
        if (Platform.getEnvironment().equals(Env.CLIENT)){
            BlockRenderLayerMap.INSTANCE.putBlock(DisxLacquerBlock.blockRegistration.get(), RenderType.translucent());
            BlockRenderLayerMap.INSTANCE.putBlock(DisxStampMaker.blockRegistration.get(), RenderType.cutout());
        }
    }
}
