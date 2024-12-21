package xyz.ar06.disx.forge;

import net.minecraftforge.common.MinecraftForge;
import xyz.ar06.disx.DisxMain;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(DisxMain.MOD_ID)
public class DisxMainForge {
    public DisxMainForge() {
        // Submit our event bus to let architectury register our content on the right time
        Logger LOGGER = LoggerFactory.getLogger("disx");
        EventBuses.registerModEventBus(DisxMain.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        DisxMain.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DisxClientForge::onClientInitialize);
        MinecraftForge.EVENT_BUS.register(new DisxBehaviorHandlingForge());
    }
}
