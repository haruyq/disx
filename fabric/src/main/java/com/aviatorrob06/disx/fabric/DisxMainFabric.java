package com.aviatorrob06.disx.fabric;

import com.aviatorrob06.disx.DisxMain;
import net.fabricmc.api.ModInitializer;

public class DisxMainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DisxMain.init();
        DisxClientMainFabric.onInitializeClient();
    }
}
