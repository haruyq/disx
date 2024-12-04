package xyz.ar06.disx.fabric;

import xyz.ar06.disx.DisxMain;
import net.fabricmc.api.ModInitializer;

public class DisxMainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DisxMain.init();
        DisxClientMainFabric.onInitializeClient();
    }
}
