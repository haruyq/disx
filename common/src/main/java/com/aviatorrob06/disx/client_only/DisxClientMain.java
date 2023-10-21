package com.aviatorrob06.disx.client_only;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

public class DisxClientMain {
    public static void onInitializeClient(){
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
        }
    }

}
