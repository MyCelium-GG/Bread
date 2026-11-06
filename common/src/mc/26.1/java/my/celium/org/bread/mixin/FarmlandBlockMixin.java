package my.celium.org.bread.mixin;

import my.celium.org.bread.BreadMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Feather Falling boots do not trample farmland. */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {
    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable = true)
    private static void bread$keepFarmland(@Nullable Entity source, BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (source instanceof LivingEntity living && BreadMod.on(BreadMod.featherFallingSavesCrops)) {
            for (var entry : living.getItemBySlot(EquipmentSlot.FEET).getEnchantments().entrySet()) {
                if (entry.getKey().is(Enchantments.FEATHER_FALLING) && entry.getIntValue() > 0) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
