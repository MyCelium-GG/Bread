package my.celium.org.bread.mixin.client;

import my.celium.org.bread.client.RenderCulling;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Skips far-away decoration entities, dropped items and entities behind walls, see {@link RenderCulling#skip}. */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void bread$cullFar(T entity, Frustum culler, double camX, double camY, double camZ, float partialTicks,
                               CallbackInfoReturnable<Boolean> cir) {
        if (RenderCulling.skip(entity, camX, camY, camZ)) {
            cir.setReturnValue(false);
        }
    }
}
