package com.aviatorrob06.disx;

import com.aviatorrob06.disx.blocks.DisxLacquerBlock;
import com.aviatorrob06.disx.blocks.DisxStampMaker;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.renderer.RenderType;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

public class DisxClientMainQuilt {
    public static void initializeClient(){
        if (Platform.getEnvironment().equals(Env.CLIENT)){
            BlockRenderLayerMap.put(RenderType.translucent(), DisxLacquerBlock.blockRegistration.get());
            BlockRenderLayerMap.put(RenderType.cutout(), DisxStampMaker.blockRegistration.get());
        }
    }
}
