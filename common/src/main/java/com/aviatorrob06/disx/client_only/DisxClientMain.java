package com.aviatorrob06.disx.client_only;

import com.aviatorrob06.disx.DisxMain;
import com.aviatorrob06.disx.DisxSystemMessages;
import com.aviatorrob06.disx.TestAudioTrack;
import com.aviatorrob06.disx.client_only.gui.screens.DisxStampMakerGUI;
import com.aviatorrob06.disx.client_only.renderers.DisxRecordPressEntityRenderer;
import com.aviatorrob06.disx.client_only.renderers.DisxStampMakerEntityRenderer;
import com.aviatorrob06.disx.config.DisxConfigHandler;
import com.aviatorrob06.disx.entities.DisxRecordPressEntity;
import com.aviatorrob06.disx.entities.DisxStampMakerEntity;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.*;
import dev.architectury.event.events.common.LootEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.utils.Env;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;


public class DisxClientMain {
    public static final boolean debug_AudioPlayer = false;
    public static void onInitializeClient() {
        if (Platform.getEnvironment().equals(Env.CLIENT)){
            DisxClientPacketIndex.registerClientPacketReceivers();
            ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> DisxAudioPlayerRegistry.onPlayDisconnect());
            ClientLifecycleEvent.CLIENT_STOPPING.register(instance -> DisxAudioPlayerRegistry.onClientStopping(instance));
            ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> DisxAudioPlayerRegistry.grabServerRegistry());
            ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((player -> ClientTickEvent.CLIENT_POST.register(plr -> {
                if (Minecraft.getInstance().isPaused() && Minecraft.getInstance().isSingleplayer()){
                    DisxAudioPlayerRegistry.onClientPause();
                } else {
                    if (!Minecraft.getInstance().isPaused() && Minecraft.getInstance().isSingleplayer()){
                        DisxAudioPlayerRegistry.onClientUnpause();
                    }
                }
            })));
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
            if (debug_AudioPlayer){
                CompletableFuture.runAsync(TestAudioTrack::run);
            }

            DisxMain.LOGGER.info("Success in Mod Launch (CLIENT)");
        }
    }

}
