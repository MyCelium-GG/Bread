package my.celium.org.bread.fabric;

import my.celium.org.bread.BreadMod;
import my.celium.org.bread.client.BreadCommand;
import my.celium.org.bread.client.FpsCounter;
import my.celium.org.bread.client.Zoom;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

/** Fabric client entrypoint: the FPS counter on the HUD and the /bread command. */
public final class BreadFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(BreadMod.ID, "fps_counter"),
                (gfx, tracker) -> FpsCounter.render(gfx));
        KeyMappingHelper.registerKeyMapping(Zoom.KEY);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) ->
                dispatcher.register(BreadCommand.<FabricClientCommandSource>build()));
    }
}
