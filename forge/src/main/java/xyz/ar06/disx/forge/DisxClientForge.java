package xyz.ar06.disx.forge;

import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.blocks.DisxStampMaker;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.function.Consumer;

public class DisxClientForge {
    public static void onClientInitialize(final FMLCommonSetupEvent event){
        if (Platform.getEnvironment().equals(Env.CLIENT)){
            ItemBlockRenderTypes.setRenderLayer(DisxLacquerBlock.blockRegistration.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(DisxStampMaker.blockRegistration.get(), RenderType.cutout());
        }
    }
}
