package com.aviatorrob06.disx;

import com.aviatorrob06.disx.blocks.DisxAdvancedJukebox;
import com.aviatorrob06.disx.blocks.DisxLacquerBlock;
import com.aviatorrob06.disx.blocks.DisxRecordPress;
import com.aviatorrob06.disx.blocks.DisxStampMaker;
import com.aviatorrob06.disx.client_only.DisxClientMain;
import com.aviatorrob06.disx.commands.*;
import com.aviatorrob06.disx.config.DisxConfigHandler;
import com.aviatorrob06.disx.entities.DisxAdvancedJukeboxEntity;
import com.aviatorrob06.disx.items.*;
import com.aviatorrob06.disx.recipe_types.DisxCustomDiscRecipe;
import com.aviatorrob06.disx.recipe_types.DisxStampRecipe;
import com.aviatorrob06.disx.utils.DisxJukeboxUsageCooldownManager;
import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class DisxMain {
    public static final String MOD_ID = "disx";

    public static final Logger LOGGER = LoggerFactory.getLogger("disx");

    public static final boolean debug = false;


    public static final Supplier<RegistrarManager> REGISTRAR_MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static void init() {
        //Registrar Declarations and Initializations
        Registrar<Item> itemsRegistrar = REGISTRAR_MANAGER.get().get(Registries.ITEM);
        Registrar<Block> blocksRegistrar = REGISTRAR_MANAGER.get().get(Registries.BLOCK);
        Registrar<BlockEntityType<?>> blockEntityRegistrar = REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);
        Registrar<CreativeModeTab> tabRegistrar = REGISTRAR_MANAGER.get().get(Registries.CREATIVE_MODE_TAB);
        Registrar<RecipeSerializer<?>> serializerRegistrar = REGISTRAR_MANAGER.get().get(Registries.RECIPE_SERIALIZER);
        Registrar<RecipeType<?>> recipeTypeRegistrar = REGISTRAR_MANAGER.get().get(Registries.RECIPE_TYPE);
        //Creative Mode Tab Registration
        RegistrySupplier<CreativeModeTab> creativeModeTab = tabRegistrar.register(new ResourceLocation("disx", "creativemodetab.disx"), () -> CreativeTabRegistry.create(Component.translatable("category.disx.tab"), () -> new ItemStack(itemsRegistrar.get(new ResourceLocation("disx", "blank_disc")))));
        //Item Registration Calls
        DisxVinylShard.registerItem(itemsRegistrar, creativeModeTab);
        DisxBlankDisc.registerItem(itemsRegistrar, creativeModeTab);
        DisxCustomDisc.registerCustomDiscs(itemsRegistrar);
        DisxLacquerDrop.registerItem(itemsRegistrar, creativeModeTab);
        DisxRecordStamp.registerItem(itemsRegistrar);
        //Block Registration Calls
        DisxAdvancedJukebox.registerBlock(blocksRegistrar);
        DisxAdvancedJukebox.registerBlockItem(itemsRegistrar, creativeModeTab);
        DisxAdvancedJukeboxEntity.registerEntity(blockEntityRegistrar);
        DisxStampMaker.registerBlock(blocksRegistrar);
        DisxStampMaker.registerBlockItem(itemsRegistrar, creativeModeTab);
        DisxStampMaker.registerBlockEntity(blockEntityRegistrar);
        DisxRecordPress.registerBlock(blocksRegistrar);
        DisxRecordPress.registerBlockItem(itemsRegistrar, creativeModeTab);
        DisxRecordPress.registerBlockEntity(blockEntityRegistrar);
        DisxLacquerBlock.registerBlock(blocksRegistrar);
        DisxLacquerBlock.registerBlockItem(itemsRegistrar, creativeModeTab);
        //Command Registration Calls
        DisxConfigCommand.registerCommand();
        DisxForceStopCommand.registerCommand();
        DisxGenCommand.registerCommand();
        DisxHelpCommand.registerCommand();
        DisxInfoCommand.registerCommand();
        DisxMuteCommand.registerCommand();
        DisxSoundCommand.registerCommand();
        DisxStampCommand.registerCommand();
        //Loot Modification Call
        DisxLootModifiers.modifyLootTables(itemsRegistrar);
        //Recipe & RecipeType Registration Calls
        DisxCustomDiscRecipe.DisxCustomDiscRecipeType.registerRecipeType(recipeTypeRegistrar);
        DisxCustomDiscRecipe.DisxCustomDiscRecipeSerializer.registerRecipeSerializer(serializerRegistrar);
        DisxStampRecipe.DisxStampRecipeType.registerRecipeType(recipeTypeRegistrar);
        DisxStampRecipe.DisxStampRecipeSerializer.registerSerializer(serializerRegistrar);

        //Pull Mod Info
        DisxModInfo.pullLatestVersion();

        //Event Registrations
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

        LifecycleEvent.SERVER_STARTED.register(DisxSystemMessages::outdatedModVersion);

        LifecycleEvent.SERVER_STARTING.register(DisxConfigHandler.SERVER::initializeConfig);

        //Register Server Packets
        DisxServerPacketIndex.registerServerPacketReceivers();

        //Call Common Client Initialization
        if (Platform.getEnv().equals(EnvType.CLIENT)){
            DisxClientMain.onInitializeClient();
        }

        LOGGER.info("Success in Mod Launch (SERVER)");
    }
}
