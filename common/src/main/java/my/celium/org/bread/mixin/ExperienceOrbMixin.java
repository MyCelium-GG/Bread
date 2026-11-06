package my.celium.org.bread.mixin;

import my.celium.org.bread.BreadMod;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** XP orbs merge from farther apart: fewer orb entities to tick, send and draw. */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Redirect(method = "scanForMerges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
    private AABB bread$mergeRadius(AABB box, double amount) {
        double radius = BreadMod.radius(BreadMod.xpMergeRadius);
        return box.inflate(radius > 0 ? radius : amount);
    }
}
