package my.celium.org.bread.mixin.client;

import my.celium.org.bread.BreadStats;
import my.celium.org.bread.client.Zoom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Narrows the field of view while zooming ({@link Zoom}); also notes the FPS for the stats. */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void bread$zoom(float partialTicks, CallbackInfoReturnable<Float> cir) {
        BreadStats.fps = Minecraft.getInstance().getFps();
        my.celium.org.bread.client.BreadInfo.frame();
        float multiplier = Zoom.fovMultiplier();
        if (multiplier != 1.0F) {
            cir.setReturnValue(cir.getReturnValueF() * multiplier);
        }
    }
}
