package my.celium.org.bread.forge;

import my.celium.org.bread.BreadMod;
import my.celium.org.client.ClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Forge entrypoint. Everything else runs through mixins. */
@Mod(BreadMod.ID)
public final class BreadForge {
    public BreadForge(@SuppressWarnings("unused") FMLJavaModLoadingContext context) {
        BreadMod.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (Minecraft minecraft, Screen parent) -> ClientHooks.createConfigScreen(parent, BreadMod.ID)));
            ForgeClient.register();
        }
    }
}
