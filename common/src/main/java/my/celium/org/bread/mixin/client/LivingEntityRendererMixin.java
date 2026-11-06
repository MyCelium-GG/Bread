package my.celium.org.bread.mixin.client;

import my.celium.org.bread.BreadMod;
import my.celium.org.bread.client.BreadInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Far-away mobs skip their extra layers: armor, held items, saddles, wool, eyes and so on. Each
 * layer is a separate model draw, and from far away they are a few pixels. Players override
 * shouldRenderLayers, so their armor and capes always show.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState> {
    @Inject(method = "shouldRenderLayers", at = @At("RETURN"), cancellable = true)
    private void bread$farMobDetail(S state, CallbackInfoReturnable<Boolean> cir) {
        int limit = my.celium.org.bread.client.RenderCulling.lod(BreadMod.farMobDetailDistance);
        if (limit > 0 && cir.getReturnValueZ() && state.distanceToCameraSq > (double) limit * limit) {
            cir.setReturnValue(false);
            BreadInfo.mobLayers();
        }
    }
}
