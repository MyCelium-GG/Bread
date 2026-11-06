package my.celium.org.bread.mixin.client;

import java.util.List;
import my.celium.org.bread.client.ItemLod;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Far-away items are drawn flat, see {@link ItemLod}. 26.1 draws items in renderItem. */
@Mixin(ItemFeatureRenderer.class)
public abstract class ItemFeatureRendererMixin {
    @Redirect(method = "renderItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$ItemSubmit;quads()Ljava/util/List;"))
    private List<BakedQuad> bread$lod(SubmitNodeStorage.ItemSubmit submit) {
        return ItemLod.quads(submit.quads(), submit.pose().pose(), submit.displayContext());
    }
}
