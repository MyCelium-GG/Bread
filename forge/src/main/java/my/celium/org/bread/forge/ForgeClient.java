package my.celium.org.bread.forge;

import my.celium.org.bread.BreadMod;
import my.celium.org.bread.client.BreadCommand;
import my.celium.org.bread.client.FpsCounter;
import my.celium.org.bread.client.Zoom;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;

/** Forge client side: the FPS counter on the HUD and the /bread command. Only loaded on clients. */
final class ForgeClient {
    private ForgeClient() {
    }

    static void register() {
        RegisterKeyMappingsEvent.BUS.addListener(event -> event.register(Zoom.KEY));
        AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().add(
                Identifier.fromNamespaceAndPath(BreadMod.ID, "fps_counter"), (gfx, tracker) -> FpsCounter.render(gfx)));
        RegisterClientCommandsEvent.BUS.addListener(event ->
                event.getDispatcher().register(BreadCommand.<CommandSourceStack>build()));
    }
}
