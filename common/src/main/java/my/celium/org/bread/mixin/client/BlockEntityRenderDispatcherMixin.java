package my.celium.org.bread.mixin.client;

import my.celium.org.bread.client.RenderCulling;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stops drawing far-away signs, banners and heads and block entities behind walls, see
 * {@link RenderCulling#skip}. Renderers that draw outside their block (beacon beams) are never
 * hidden by walls.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "tryExtractRenderState*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z"))
    private boolean bread$shouldRender(BlockEntityRenderer renderer, BlockEntity blockEntity, Vec3 camera) {
        return renderer.shouldRender(blockEntity, camera)
                && !RenderCulling.skip(blockEntity, camera, renderer.shouldRenderOffScreen());
    }
}
