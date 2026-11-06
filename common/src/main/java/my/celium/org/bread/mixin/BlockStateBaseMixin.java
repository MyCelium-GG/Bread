package my.celium.org.bread.mixin;

import my.celium.org.bread.tweak.BlockTweaks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** One entry point for every block click tweak, see {@link BlockTweaks}. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow
    protected abstract BlockState asState();

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void bread$useItemOn(ItemStack stack, Level level, Player player, InteractionHand hand, BlockHitResult hit,
                                 CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = BlockTweaks.useItemOn(stack, asState(), level, player, hand, hit);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void bread$useWithoutItem(Level level, Player player, BlockHitResult hit,
                                      CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = BlockTweaks.useWithoutItem(asState(), level, player, hit);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    private void bread$afterUse(Level level, Player player, BlockHitResult hit,
                                CallbackInfoReturnable<InteractionResult> cir) {
        if (asState().getBlock() instanceof DoorBlock && cir.getReturnValue().consumesAction()) {
            BlockTweaks.afterDoorToggled(level, player, hit.getBlockPos());
        }
    }
}
