package my.celium.org.bread.mixin;

import java.util.List;
import my.celium.org.bread.BreadMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * On the client, dropped items and XP orbs predict their own movement every tick, and each move
 * looks for entities to bump into. With many items in one spot every item searches all the
 * others. The server does the real physics and sends positions, so the client can skip that
 * search; items still collide with blocks.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Redirect(method = "collide", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<VoxelShape> bread$skipEntityCollisions(Level level, Entity entity, AABB box) {
        if (level.isClientSide() && (entity instanceof ItemEntity || entity instanceof ExperienceOrb)
                && BreadMod.onFast(BreadMod.fastDroppedItems)) {
            my.celium.org.bread.client.BreadInfo.collision();
            return List.of();
        }
        return level.getEntityCollisions(entity, box);
    }
}
