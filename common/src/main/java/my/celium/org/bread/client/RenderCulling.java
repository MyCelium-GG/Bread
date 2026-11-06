package my.celium.org.bread.client;

import my.celium.org.bread.BreadMod;
import my.celium.org.config.ConfigValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Distance limits for things that are expensive to draw but too small to see from far away:
 * item frames (they draw a whole item model), paintings, armor stands, dropped items, XP orbs,
 * signs (text), banners (layered patterns) and heads. A limit of 0 keeps vanilla's distance.
 */
public final class RenderCulling {
    private RenderCulling() {
    }

    /**
     * A distance setting, stretched while zooming: through a 4x zoom something 100 blocks away
     * looks 25 blocks away, so it keeps its full detail. 0 stays 0 (vanilla).
     */
    public static int lod(ConfigValue<Integer> setting) {
        int limit = BreadMod.distance(setting);
        double zoom = Zoom.mouseScale();
        return limit > 0 && zoom < 1.0 ? (int) Math.min(4096, limit / zoom) : limit;
    }

    /** The entity render hook's question: skip this entity? Counts the answer for /bread info. */
    public static boolean skip(Entity entity, double camX, double camY, double camZ) {
        BreadInfo.entities++;
        if (tooFar(entity, camX, camY, camZ)) {
            BreadInfo.entitiesFar++;
            return true;
        }
        if (OcclusionCuller.hidden(entity, camX, camY, camZ)) {
            BreadInfo.entitiesHidden++;
            return true;
        }
        return false;
    }

    /** The block entity render hook's question: skip it? {@code offScreen} renderers (beacons) are never hidden by walls. */
    public static boolean skip(BlockEntity blockEntity, Vec3 camera, boolean offScreen) {
        BreadInfo.blockEntities++;
        if (tooFar(blockEntity, camera)) {
            BreadInfo.blockEntitiesFar++;
            return true;
        }
        if (!offScreen && OcclusionCuller.hidden(blockEntity, camera.x, camera.y, camera.z)) {
            BreadInfo.blockEntitiesHidden++;
            return true;
        }
        return false;
    }

    public static boolean tooFar(Entity entity, double camX, double camY, double camZ) {
        int limit;
        if (entity instanceof HangingEntity || entity instanceof ArmorStand) {
            limit = lod(BreadMod.decorEntityDistance);
        } else if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
            limit = lod(BreadMod.droppedItemDistance);
        } else {
            return false;
        }
        return limit > 0 && entity.distanceToSqr(camX, camY, camZ) > (double) limit * limit;
    }

    public static boolean tooFar(BlockEntity blockEntity, Vec3 camera) {
        if (!(blockEntity instanceof SignBlockEntity || blockEntity instanceof BannerBlockEntity
                || blockEntity instanceof SkullBlockEntity)) {
            return false;
        }
        int limit = lod(BreadMod.signDistance);
        return limit > 0 && !Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(camera, limit);
    }
}
