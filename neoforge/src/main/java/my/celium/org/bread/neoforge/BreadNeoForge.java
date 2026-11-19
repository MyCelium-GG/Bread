package my.celium.org.bread.neoforge;

import my.celium.org.bread.BreadMod;
import my.celium.org.client.ClientHooks;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** NeoForge entrypoint. Everything else runs through mixins. */
@Mod(BreadMod.ID)
public final class BreadNeoForge {
    public BreadNeoForge(@SuppressWarnings("unused") IEventBus modBus, @SuppressWarnings("unused") ModContainer container) {
        if (!net.neoforged.fml.ModList.get().isLoaded("mycel")) {
            my.celium.org.bread.MissingMycel.logOnce();
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    (net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) -> {
                if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                    my.celium.org.bread.MissingMycel.tell(player);
                }
            });
            return;
        }
        my.celium.org.bread.ModGate.open();
        BreadMod.init();
        if (FMLEnvironment.getDist().isClient()) {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                    () -> (ModContainer ignored, Screen parent) -> ClientHooks.createConfigScreen(parent, BreadMod.ID));
            NeoForgeClient.register(modBus);
        }
    }
}
