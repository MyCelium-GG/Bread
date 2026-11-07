package my.celium.org.bread.mixin.client;

import java.util.List;
import my.celium.org.bread.client.ItemLod;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Far-away items are drawn flat, see {@link ItemLod}. */
@Mixin(ItemFeatureRenderer.class)
public abstract class ItemFeatureRendererMixin {
    @Redirect(method = "prepareMainSubmit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer$Submit;quads()Ljava/util/List;"))
    private List<BakedQuad> bread$lod(ItemFeatureRenderer.Submit submit) {
        return ItemLod.quads(submit.quads(), submit.pose().pose(), submit.displayContext());
    }

    @Redirect(method = "prepareMainSubmit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer$Submit;foilType()Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;"))
    private ItemStackRenderState.FoilType bread$glint(ItemFeatureRenderer.Submit submit) {
        return ItemLod.foil(submit.foilType(), submit.pose().pose(), submit.displayContext());
    }
}
