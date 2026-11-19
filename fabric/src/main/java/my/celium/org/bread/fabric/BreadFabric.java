package my.celium.org.bread.fabric;

import my.celium.org.bread.BreadMod;
import net.fabricmc.api.ModInitializer;

/** Fabric entrypoint. Everything else runs through mixins. */
public final class BreadFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("mycel")) {
            my.celium.org.bread.MissingMycel.logOnce();
            net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
                    (handler, sender, server) -> my.celium.org.bread.MissingMycel.tell(handler.getPlayer()));
            return;
        }
        my.celium.org.bread.ModGate.open();
        BreadMod.init();
    }
}
