package my.celium.org.bread.client;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import my.celium.org.bread.BreadMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * Skips drawing entities and block entities that are completely behind walls.
 *
 * <p>Vanilla already skips whole 16x16x16 sections it cannot see, but everything inside a section
 * you can see is drawn, even a chest room or an item frame wall behind solid stone. Here the
 * render hooks ask "is this hidden?" and get the last known answer straight away (unknown counts
 * as visible, so nothing pops out of existence while it waits). A background thread works out the
 * answers by casting rays from the camera to the corners and center of each thing's box through
 * the block grid; one ray that gets through means visible.
 *
 * <p>Answers are recomputed all the time, so walking around a corner shows things again within a
 * few milliseconds. Players, glowing entities, huge entities and things right next to the camera
 * are never hidden. Switched off when the Entity Culling mod is installed, which does the same.
 */
public final class OcclusionCuller {
    private static final double NEAR = 3.0;
    private static final double FAR = 160.0;
    private static final double MAX_SIZE = 8.0;
    /** A target not asked about for this long is forgotten. */
    private static final long FORGET_MS = 2000;
    /** Pause between passes over all targets. */
    private static final long PASS_PAUSE_MS = 8;

    private static final boolean OTHER_CULLER = classExists("dev.tr7zw.entityculling.EntityCullingModBase");

    private static final Map<Integer, Target> ENTITIES = new ConcurrentHashMap<>();
    private static final Map<Long, Target> BLOCKS = new ConcurrentHashMap<>();

    private static volatile double camX, camY, camZ;
    private static volatile Thread worker;

    private OcclusionCuller() {
    }

    /** One thing to test: its box, when it was last asked about, and the last answer. */
    private static final class Target {
        volatile double minX, minY, minZ, maxX, maxY, maxZ;
        volatile long askedAt;
        volatile boolean hidden;
        /** The thing's own block (block entities), ignored by the rays. */
        final long ownBlock;

        Target(long ownBlock) {
            this.ownBlock = ownBlock;
        }

        void box(AABB box) {
            minX = box.minX; minY = box.minY; minZ = box.minZ;
            maxX = box.maxX; maxY = box.maxY; maxZ = box.maxZ;
        }
    }

    private static boolean active() {
        return !OTHER_CULLER && BreadMod.onFast(BreadMod.hideUnseen);
    }

    /** True when the entity is known to be behind walls. Called by the entity render hook. */
    public static boolean hidden(Entity entity, double cx, double cy, double cz) {
        if (!active()) {
            return false;
        }
        camX = cx; camY = cy; camZ = cz;
        if (entity instanceof Player || entity.isCurrentlyGlowing()) {
            return false;
        }
        AABB box = entity.getBoundingBox();
        if (box.getXsize() > MAX_SIZE || box.getYsize() > MAX_SIZE || box.getZsize() > MAX_SIZE) {
            return false;
        }
        Target target = ENTITIES.computeIfAbsent(entity.getId(), id -> new Target(Long.MIN_VALUE));
        target.box(box.inflate(0.25));
        target.askedAt = System.currentTimeMillis();
        ensureWorker();
        return target.hidden;
    }

    /** True when the block entity is known to be behind walls. Called by the block entity render hook. */
    public static boolean hidden(BlockEntity blockEntity, double cx, double cy, double cz) {
        if (!active()) {
            return false;
        }
        camX = cx; camY = cy; camZ = cz;
        BlockPos pos = blockEntity.getBlockPos();
        long key = pos.asLong();
        Target target = BLOCKS.computeIfAbsent(key, k -> {
            Target t = new Target(k);
            t.box(new AABB(pos).inflate(0.05));
            return t;
        });
        target.askedAt = System.currentTimeMillis();
        ensureWorker();
        return target.hidden;
    }

    private static void ensureWorker() {
        if (worker != null) {
            return;
        }
        synchronized (OcclusionCuller.class) {
            if (worker == null) {
                Thread thread = new Thread(OcclusionCuller::run, "Bread occlusion culling");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                worker = thread;
                thread.start();
            }
        }
    }

    private static void run() {
        Long2ByteOpenHashMap opaque = new Long2ByteOpenHashMap();
        while (true) {
            try {
                Thread.sleep(PASS_PAUSE_MS);
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null || !active()) {
                    ENTITIES.clear();
                    BLOCKS.clear();
                    Thread.sleep(200);
                    continue;
                }
                long now = System.currentTimeMillis();
                double x = camX, y = camY, z = camZ;
                opaque.clear();
                pass(ENTITIES, level, opaque, now, x, y, z);
                pass(BLOCKS, level, opaque, now, x, y, z);
            } catch (InterruptedException e) {
                return;
            } catch (Throwable t) {
                // The client level can change under us (a chunk unloads, the world closes); the
                // next pass starts fresh, and anything unknown counts as visible.
                opaque.clear();
            }
        }
    }

    private static <K> void pass(Map<K, Target> targets, ClientLevel level, Long2ByteOpenHashMap opaque,
                                 long now, double x, double y, double z) {
        var it = targets.values().iterator();
        while (it.hasNext()) {
            Target t = it.next();
            if (now - t.askedAt > FORGET_MS) {
                it.remove();
                continue;
            }
            t.hidden = isHidden(t, level, opaque, x, y, z);
        }
    }

    private static boolean isHidden(Target t, ClientLevel level, Long2ByteOpenHashMap opaque, double x, double y, double z) {
        double cx = Mth.clamp(x, t.minX, t.maxX), cy = Mth.clamp(y, t.minY, t.maxY), cz = Mth.clamp(z, t.minZ, t.maxZ);
        double dx = cx - x, dy = cy - y, dz = cz - z;
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq < NEAR * NEAR || distSq > FAR * FAR) {
            return false;
        }
        // Center first (the usual way through), then the 8 corners.
        if (clear(level, opaque, x, y, z, (t.minX + t.maxX) / 2, (t.minY + t.maxY) / 2, (t.minZ + t.maxZ) / 2, t.ownBlock)) {
            return false;
        }
        for (int i = 0; i < 8; i++) {
            double px = (i & 1) == 0 ? t.minX : t.maxX;
            double py = (i & 2) == 0 ? t.minY : t.maxY;
            double pz = (i & 4) == 0 ? t.minZ : t.maxZ;
            if (clear(level, opaque, x, y, z, px, py, pz, t.ownBlock)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Walks the blocks on the line from (x0, y0, z0) to (x1, y1, z1), one block at a time
     * (Amanatides and Woo). False as soon as a full, opaque block is in the way.
     */
    private static boolean clear(ClientLevel level, Long2ByteOpenHashMap opaque,
                                 double x0, double y0, double z0, double x1, double y1, double z1, long ownBlock) {
        int bx = Mth.floor(x0), by = Mth.floor(y0), bz = Mth.floor(z0);
        int ex = Mth.floor(x1), ey = Mth.floor(y1), ez = Mth.floor(z1);
        double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        int sx = dx > 0 ? 1 : dx < 0 ? -1 : 0;
        int sy = dy > 0 ? 1 : dy < 0 ? -1 : 0;
        int sz = dz > 0 ? 1 : dz < 0 ? -1 : 0;
        double tdx = sx == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dx);
        double tdy = sy == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dy);
        double tdz = sz == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dz);
        double tx = sx == 0 ? Double.MAX_VALUE : tdx * (sx > 0 ? (bx + 1 - x0) : (x0 - bx));
        double ty = sy == 0 ? Double.MAX_VALUE : tdy * (sy > 0 ? (by + 1 - y0) : (y0 - by));
        double tz = sz == 0 ? Double.MAX_VALUE : tdz * (sz > 0 ? (bz + 1 - z0) : (z0 - bz));
        int steps = Math.abs(ex - bx) + Math.abs(ey - by) + Math.abs(ez - bz);
        for (int i = 0; i < steps; i++) {
            if (tx < ty && tx < tz) {
                bx += sx;
                tx += tdx;
            } else if (ty < tz) {
                by += sy;
                ty += tdy;
            } else {
                bz += sz;
                tz += tdz;
            }
            if (bx == ex && by == ey && bz == ez) {
                return true; // reached the target's block
            }
            long key = BlockPos.asLong(bx, by, bz);
            if (key != ownBlock && isOpaque(level, opaque, key, bx, by, bz)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOpaque(ClientLevel level, Long2ByteOpenHashMap cache, long key, int x, int y, int z) {
        byte known = cache.getOrDefault(key, (byte) -1);
        if (known >= 0) {
            return known == 1;
        }
        boolean solid = level.getBlockState(new BlockPos(x, y, z)).isSolidRender();
        cache.put(key, solid ? (byte) 1 : (byte) 0);
        return solid;
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, OcclusionCuller.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
