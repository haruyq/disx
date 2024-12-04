package xyz.ar06.disx;


import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class DisxMainQuilt implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {

        DisxMain.init();
        DisxClientMainQuilt.initializeClient();
    }
}
