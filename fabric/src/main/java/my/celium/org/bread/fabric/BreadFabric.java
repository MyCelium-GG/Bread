package my.celium.org.bread.fabric;

import my.celium.org.bread.BreadMod;
import net.fabricmc.api.ModInitializer;

/** Fabric entrypoint. Everything else runs through mixins. */
public final class BreadFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BreadMod.init();
    }
}
