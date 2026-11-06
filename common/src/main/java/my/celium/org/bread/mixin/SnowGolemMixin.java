package my.celium.org.bread.mixin;

import my.celium.org.bread.BreadMod;
import my.celium.org.bread.BreadStats;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Right-click a sheared snow golem with a carved pumpkin to give it its head back. */
@Mixin(SnowGolem.class)
public abstract class SnowGolemMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bread$putPumpkinBack(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        SnowGolem golem = (SnowGolem) (Object) this;
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.CARVED_PUMPKIN) || golem.hasPumpkin() || !BreadMod.on(BreadMod.snowGolemPumpkin)) {
            return;
        }
        if (!golem.level().isClientSide()) {
            golem.setPumpkin(true);
            golem.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
            stack.consume(1, player);
            BreadStats.used("snow_golem");
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
