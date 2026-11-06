package my.celium.org.bread.mixin.client;

import my.celium.org.bread.BreadMod;
import my.celium.org.bread.client.BreadInfo;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A lower ceiling for particles of one kind than vanilla's 16384, so explosions, farms and
 * particle-heavy builds cannot flood the frame. Particles with their own limit (spore blossoms)
 * are left alone so vanilla's counting stays right.
 */
@Mixin(ParticleGroup.class)
public abstract class ParticleGroupMixin {
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void bread$cap(Particle particle, CallbackInfoReturnable<Boolean> cir) {
        int cap = BreadMod.distance(BreadMod.maxParticles);
        if (cap > 0 && ((ParticleGroup<?>) (Object) this).size() >= cap && particle.getParticleLimit().isEmpty()) {
            BreadInfo.particle();
            cir.setReturnValue(false);
        }
    }
}
