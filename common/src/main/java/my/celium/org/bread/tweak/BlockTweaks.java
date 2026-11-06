package my.celium.org.bread.tweak;

import java.util.List;
import my.celium.org.bread.BreadMod;
import my.celium.org.bread.BreadStats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Tweaks that react to a player clicking a block. Called from {@code BlockStateBaseMixin} before
 * vanilla handles the click; a non-null result means Bread handled it.
 */
public final class BlockTweaks {
    /** Sugar cane and cactus stop growing at this height in vanilla. */
    private static final int MAX_COLUMN = 3;
    /** How far a ladder column is followed to find its end. */
    private static final int MAX_LADDER = 64;

    private BlockTweaks() {
    }

    /** Click with an item in hand. */
    public static @Nullable InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, Player player,
                                                        InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.BONE_MEAL) && BreadMod.on(BreadMod.boneMealCaneAndCactus)
                && (state.getBlock() instanceof SugarCaneBlock || state.getBlock() instanceof CactusBlock)) {
            return growColumn(stack, state, level, player, hit.getBlockPos());
        }
        if (state.getBlock() instanceof LadderBlock && stack.is(state.getBlock().asItem())
                && BreadMod.on(BreadMod.ladderExtend)) {
            return extendLadder(stack, state, level, player, hit.getBlockPos());
        }
        if (stack.is(ItemTags.SHOVELS) && BreadMod.on(BreadMod.shovelPathToDirt)
                && (state.is(Blocks.DIRT_PATH) || state.is(Blocks.FARMLAND))) {
            return pathToDirt(stack, level, player, hand, hit.getBlockPos());
        }
        return null;
    }

    /** Click with an empty hand, or with an item that did nothing on this block. */
    public static @Nullable InteractionResult useWithoutItem(BlockState state, Level level, Player player, BlockHitResult hit) {
        if (BreadMod.on(BreadMod.rightClickHarvest) && isGrownCrop(state)) {
            return harvest(state, level, player, hit.getBlockPos());
        }
        return null;
    }

    /**
     * After vanilla toggled a door: open or close the other half of a double door with it. The
     * partner is the door next to the hinge-free side, facing the same way with the opposite hinge.
     */
    public static void afterDoorToggled(Level level, Player player, BlockPos pos) {
        if (level.isClientSide() || !BreadMod.on(BreadMod.doubleDoors) || player.isSecondaryUseActive()) {
            return;
        }
        BlockState door = level.getBlockState(pos);
        if (!(door.getBlock() instanceof DoorBlock)) {
            return;
        }
        Direction facing = door.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = door.getValue(DoorBlock.HINGE);
        BlockPos otherPos = pos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
        BlockState other = level.getBlockState(otherPos);
        if (other.getBlock() instanceof DoorBlock otherDoor
                && other.getBlock() == door.getBlock()
                && other.getValue(DoorBlock.FACING) == facing
                && other.getValue(DoorBlock.HINGE) != hinge
                && other.getValue(DoorBlock.HALF) == door.getValue(DoorBlock.HALF)) {
            otherDoor.setOpen(player, level, other, otherPos, door.getValue(DoorBlock.OPEN));
            BreadStats.used("double_door");
        }
    }

    private static boolean isGrownCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }
        return false;
    }

    /** Drops the crop's loot minus one seed and puts the crop back at age 0. */
    private static InteractionResult harvest(BlockState state, Level level, Player player, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        Block block = state.getBlock();
        List<ItemStack> drops = Block.getDrops(state, server, pos, null, player, player.getMainHandItem());
        boolean seedTaken = false;
        for (ItemStack drop : drops) {
            if (!seedTaken && drop.is(block.asItem())) {
                drop.shrink(1);
                seedTaken = true;
            }
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }
        BlockState replanted = block instanceof CropBlock crop
                ? crop.getStateForAge(0)
                : state.setValue(NetherWartBlock.AGE, 0);
        level.setBlock(pos, replanted, Block.UPDATE_ALL);
        level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        BreadStats.used("harvest");
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Grows sugar cane or cactus by one block on top, up to the vanilla height of 3. */
    private static InteractionResult growColumn(ItemStack stack, BlockState state, Level level, Player player, BlockPos pos) {
        Block block = state.getBlock();
        BlockPos bottom = pos;
        while (level.getBlockState(bottom.below()).is(block)) {
            bottom = bottom.below();
        }
        BlockPos top = pos;
        while (level.getBlockState(top.above()).is(block)) {
            top = top.above();
        }
        int height = top.getY() - bottom.getY() + 1;
        BlockPos target = top.above();
        BlockState grown = block.defaultBlockState();
        if (height >= MAX_COLUMN || !level.isEmptyBlock(target) || !grown.canSurvive(level, target)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        level.setBlockAndUpdate(target, grown);
        level.levelEvent(1505, target, 15); // bone meal particles and sound
        BreadStats.used("bone_meal");
        stack.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Places a ladder at the end of the clicked column: below, or above when looking up. */
    private static InteractionResult extendLadder(ItemStack stack, BlockState state, Level level, Player player, BlockPos pos) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        Direction step = player.getXRot() < 0 ? Direction.UP : Direction.DOWN;
        Direction facing = state.getValue(LadderBlock.FACING);
        BlockPos end = pos;
        for (int i = 0; i < MAX_LADDER; i++) {
            BlockState next = level.getBlockState(end.relative(step));
            if (!next.is(state.getBlock()) || next.getValue(LadderBlock.FACING) != facing) {
                break;
            }
            end = end.relative(step);
        }
        BlockPos target = end.relative(step);
        BlockState there = level.getBlockState(target);
        boolean water = there.getFluidState().isSourceOfType(net.minecraft.world.level.material.Fluids.WATER);
        BlockState ladder = state.getBlock().defaultBlockState()
                .setValue(LadderBlock.FACING, facing)
                .setValue(LadderBlock.WATERLOGGED, water);
        if (!(there.isAir() || water) || !ladder.canSurvive(level, target)
                || !level.isUnobstructed(ladder, target, net.minecraft.world.phys.shapes.CollisionContext.empty())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        level.setBlockAndUpdate(target, ladder);
        SoundType sound = ladder.getSoundType();
        level.playSound(null, target, sound.getPlaceSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.gameEvent(player, GameEvent.BLOCK_PLACE, target);
        BreadStats.used("ladder");
        stack.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Dirt path or farmland back to dirt. Farmland only when nothing grows on it. */
    private static InteractionResult pathToDirt(ItemStack stack, Level level, Player player, InteractionHand hand, BlockPos pos) {
        if (!level.isEmptyBlock(pos.above())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        level.setBlockAndUpdate(pos, dirt);
        level.playSound(null, pos, dirt.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        stack.hurtAndBreak(1, player, hand);
        BreadStats.used("path_to_dirt");
        return InteractionResult.SUCCESS_SERVER;
    }
}
