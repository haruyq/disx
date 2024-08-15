package com.aviatorrob06.disx.items;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class DisxLacquerDrop extends Item {
    public DisxLacquerDrop(Properties properties) {
        super(properties);
    }

    public static void registerItem(Registrar<Item> registrar, RegistrySupplier<CreativeModeTab> tab){
        registrar.register(
                new ResourceLocation("disx","lacquer_drop"),
                () -> new DisxLacquerDrop(
                        new Item.Properties().arch$tab(tab)
                )
        );
    }
}
