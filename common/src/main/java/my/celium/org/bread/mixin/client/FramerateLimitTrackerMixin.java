package my.celium.org.bread.mixin.client;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import my.celium.org.bread.BreadMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Caps the FPS while the window is open but not focused (alt-tabbed, game on a second screen).
 * Vanilla only throttles a minimized window, so a game in the background still runs at full speed.
 */
@Mixin(FramerateLimitTracker.class)
public abstract class FramerateLimitTrackerMixin {
    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void bread$backgroundLimit(CallbackInfoReturnable<Integer> cir) {
        int limit = BreadMod.distance(BreadMod.backgroundFps);
        if (limit > 0 && cir.getReturnValueI() > limit && !Minecraft.getInstance().getWindow().isFocused()) {
            cir.setReturnValue(limit);
        }
    }
}
