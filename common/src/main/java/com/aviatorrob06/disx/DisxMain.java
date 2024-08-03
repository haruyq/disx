package com.aviatorrob06.disx;

import com.aviatorrob06.disx.blocks.DisxAdvancedJukebox;
import com.aviatorrob06.disx.client_only.DisxClientMain;
import com.aviatorrob06.disx.commands.DisxGenCommand;
import com.aviatorrob06.disx.commands.DisxSoundCommand;
import com.aviatorrob06.disx.entities.DisxAdvancedJukeboxEntity;
import com.aviatorrob06.disx.items.DisxBlankDisc;
import com.aviatorrob06.disx.items.DisxCustomDisc;
import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DisxMain {
    public static final String MOD_ID = "disx";

    public static final Logger LOGGER = LoggerFactory.getLogger("disx");

    public static final boolean debug = true;


    public static final Supplier<RegistrarManager> REGISTRAR_MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));


    public static void init() {
        LOGGER.info("Success in Mod Launch!");
        Registrar<Item> itemsRegistry = REGISTRAR_MANAGER.get().get(Registries.ITEM);
        Registrar<Block> blocksRegistry = REGISTRAR_MANAGER.get().get(Registries.BLOCK);
        Registrar<BlockEntityType<?>> blockEntityRegistry = REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);
        DisxBlankDisc.registerItem(itemsRegistry);
        DisxCustomDisc.registerCustomDiscs(itemsRegistry);
        DisxAdvancedJukebox.registerBlock(blocksRegistry);
        DisxAdvancedJukebox.registerBlockItem(itemsRegistry);
        DisxAdvancedJukeboxEntity.registerEntity(blockEntityRegistry);
        DisxGenCommand.registerCommand();
        DisxSoundCommand.registerCommand();


        PlayerEvent.PLAYER_JOIN.register(player -> {
            DisxServerAudioPlayerRegistry.players.add(player);
        });

        PlayerEvent.PLAYER_QUIT.register(player -> {
            DisxServerAudioPlayerRegistry.players.remove(player);
        });

        LifecycleEvent.SERVER_STOPPING.register((instance -> {
            DisxServerAudioPlayerRegistry.onServerClose();
        }));

        TickEvent.SERVER_POST.register(instance -> {
            DisxJukeboxUsageCooldownManager.tickCooldowns();
        });

        DisxServerPacketIndex.registerServerPacketReceivers();

        System.out.println(ExampleExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
        DisxClientMain.onInitializeClient();
    }
}
