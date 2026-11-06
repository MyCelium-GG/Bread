package my.celium.org.bread.mixin;

import my.celium.org.bread.BreadMod;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Villagers and wandering traders can be put on a lead. */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {
    @Inject(method = "canBeLeashed", at = @At("HEAD"), cancellable = true)
    private void bread$canBeLeashed(CallbackInfoReturnable<Boolean> cir) {
        if (BreadMod.on(BreadMod.leashVillagers)) {
            cir.setReturnValue(true);
        }
    }
}
