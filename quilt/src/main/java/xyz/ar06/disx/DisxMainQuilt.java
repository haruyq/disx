package xyz.ar06.disx;


import dev.architectury.registry.fuel.FuelRegistry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.items.DisxLacquerDrop;

public class DisxMainQuilt implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
        DisxMain.init();
        FuelRegistry.register(100, DisxLacquerDrop.itemRegistration.get());
        FuelRegistry.register(900, DisxLacquerBlock.itemRegistration.get());
        DisxClientMainQuilt.initializeClient();
    }
}
