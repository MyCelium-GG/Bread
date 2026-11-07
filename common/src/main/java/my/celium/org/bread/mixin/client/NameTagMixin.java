package my.celium.org.bread.mixin.client;

import my.celium.org.bread.BreadMod;
import my.celium.org.bread.client.BreadInfo;
import my.celium.org.bread.client.RenderCulling;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mob name tags past {@code name_tag_distance} are not drawn; text is costly in farms of named mobs. Players keep theirs. */
@Mixin(EntityRenderer.class)
public abstract class NameTagMixin<T extends Entity> {
    @Inject(method = "shouldShowName", at = @At("RETURN"), cancellable = true)
    private void bread$farNameTag(T entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || entity instanceof Player) {
            return;
        }
        int limit = RenderCulling.lod(BreadMod.nameTagDistance);
        if (limit > 0 && distanceToCameraSq > (double) limit * limit) {
            BreadInfo.nameTag();
            cir.setReturnValue(false);
        }
    }
}
