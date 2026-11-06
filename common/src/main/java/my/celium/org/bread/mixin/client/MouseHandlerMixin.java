package my.celium.org.bread.mixin.client;

import my.celium.org.bread.client.Zoom;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** While zooming, the wheel changes the zoom (not the hotbar) and the mouse turns slower. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void bread$zoomScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (Zoom.scroll(yOffset)) {
            ci.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void bread$zoomSensitivity(double movementTime, CallbackInfo ci) {
        double scale = Zoom.mouseScale();
        if (scale < 1.0) {
            accumulatedDX *= scale;
            accumulatedDY *= scale;
        }
    }
}
