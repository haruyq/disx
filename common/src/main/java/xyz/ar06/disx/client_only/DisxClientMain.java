package xyz.ar06.disx.client_only;

import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.client_only.renderers.DisxRecordPressEntityRenderer;
import xyz.ar06.disx.client_only.renderers.DisxStampMakerEntityRenderer;
import xyz.ar06.disx.entities.DisxRecordPressEntity;
import xyz.ar06.disx.entities.DisxStampMakerEntity;
import dev.architectury.event.events.client.*;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.utils.Env;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class DisxClientMain {
    public static void onInitializeClient() {
        if (Platform.getEnvironment().equals(Env.CLIENT)){
            DisxClientPacketIndex.registerClientPacketReceivers();
            ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> DisxAudioInstanceRegistry.clearAllRegisteredInstances());
            ClientLifecycleEvent.CLIENT_STOPPING.register(instance -> DisxAudioInstanceRegistry.clearAllRegisteredInstances());
            ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> DisxAudioInstanceRegistry.grabServerRegistry());
            /*
            ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((player -> ClientTickEvent.CLIENT_POST.register(plr -> {
                if (Minecraft.getInstance().isPaused() && Minecraft.getInstance().isSingleplayer()){
                    DisxAudioPlayerRegistry.onClientPause();
                } else {
                    if (!Minecraft.getInstance().isPaused() && Minecraft.getInstance().isSingleplayer()){
                        DisxAudioPlayerRegistry.onClientUnpause();
                    }
                }
            })));
             */
            ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
                BlockEntityRendererProvider<DisxStampMakerEntity> provider0 = DisxStampMakerEntityRenderer::new;
                BlockEntityRendererRegistry.register(
                        (BlockEntityType<DisxStampMakerEntity>) DisxMain.REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE).get(new ResourceLocation("disx","stamp_maker_entity")),
                        provider0);
                BlockEntityRendererProvider<DisxRecordPressEntity> provider1 = DisxRecordPressEntityRenderer::new;
                BlockEntityRendererRegistry.register(
                        (BlockEntityType<DisxRecordPressEntity>) DisxMain.REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE).get(new ResourceLocation("disx", "record_press_entity")),
                provider1);
            });
            //ClientLifecycleEvent.CLIENT_SETUP.register(DisxConfigHandler.CLIENT::initializeConfig);

            DisxLogger.info("Success in Mod Launch (CLIENT)");
        }
    }

}
