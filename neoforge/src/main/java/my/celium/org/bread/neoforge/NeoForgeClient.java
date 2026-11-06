package my.celium.org.bread.neoforge;

import my.celium.org.bread.client.BreadCommand;
import my.celium.org.bread.client.FpsCounter;
import my.celium.org.bread.client.Zoom;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

/** NeoForge client side: the FPS counter on the HUD and the /bread command. Only loaded on clients. */
final class NeoForgeClient {
    private NeoForgeClient() {
    }

    static void register(IEventBus modBus) {
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(Zoom.KEY));
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) -> FpsCounter.render(event.getGuiGraphics()));
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) ->
                event.getDispatcher().register(BreadCommand.<CommandSourceStack>build()));
    }
}
