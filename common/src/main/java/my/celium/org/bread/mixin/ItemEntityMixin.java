package my.celium.org.bread.mixin;

import my.celium.org.bread.BreadMod;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Dropped items merge from farther apart, so farms keep fewer item entities alive. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Redirect(method = "mergeWithNeighbours", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    private AABB bread$mergeRadius(AABB box, double x, double y, double z) {
        double radius = BreadMod.radius(BreadMod.itemMergeRadius);
        return radius > 0 ? box.inflate(radius, y, radius) : box.inflate(x, y, z);
    }
}
