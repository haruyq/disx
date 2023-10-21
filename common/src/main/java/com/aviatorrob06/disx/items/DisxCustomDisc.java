package com.aviatorrob06.disx.items;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class DisxCustomDisc extends Item {
    private String videoId = "";
    private String discName = "";


    private String discType;

    public static String[] validTypes = {"blue", "diamond", "gold", "green", "iron", "orange", "pink", "purple", "red", "yellow"};

    public DisxCustomDisc(Properties properties, String type) {
        super(properties);
        setDiscType(type);
    }

    public void setDiscType(String type){
        discType = type;
    }

    public String getVideoId(){
        return videoId;
    }

    public String getDiscType() {
        return discType;
    }

    public String getDiscName() {
        return discName;
    }

    public void setDiscName(String discName) {
        this.discName = discName;
    }

    public static void registerCustomDiscs(Registrar<Item> itemRegistrar){
        RegistrySupplier<Item> diamond = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_diamond"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "diamond"));
        RegistrySupplier<Item> gold = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_gold"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "gold"));
        RegistrySupplier<Item> iron = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_iron"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "iron"));
        RegistrySupplier<Item> blue = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_blue"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "blue"));
        RegistrySupplier<Item> green = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_green"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "green"));
        RegistrySupplier<Item> orange = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_orange"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "orange"));
        RegistrySupplier<Item> pink = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_pink"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "pink"));
        RegistrySupplier<Item> purple = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_purple"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "purple"));
        RegistrySupplier<Item> red = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_red"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "red"));
        RegistrySupplier<Item> yellow = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_yellow"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "yellow"));
    }
}
