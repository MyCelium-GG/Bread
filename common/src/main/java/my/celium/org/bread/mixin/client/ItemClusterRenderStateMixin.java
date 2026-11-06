package my.celium.org.bread.mixin.client;

import my.celium.org.bread.BreadMod;
import my.celium.org.bread.client.BreadInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla draws a dropped stack as up to 5 copies of the item model. Past the set distance the
 * pile is drawn as one copy, which a farm full of dropped stacks notices most.
 */
@Mixin(ItemClusterRenderState.class)
public abstract class ItemClusterRenderStateMixin {
    @Shadow
    public int count;

    @Inject(method = "extractItemGroupRenderState", at = @At("RETURN"))
    private void bread$onePerPile(Entity entity, ItemStack stack, ItemModelResolver resolver, CallbackInfo ci) {
        int limit = my.celium.org.bread.client.RenderCulling.lod(BreadMod.itemPileDistance);
        if (limit > 0 && count > 1
                && Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(entity) > (double) limit * limit) {
            count = 1;
            BreadInfo.pile();
        }
    }
}
