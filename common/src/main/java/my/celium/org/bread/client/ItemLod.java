package my.celium.org.bread.client;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import my.celium.org.bread.BreadMod;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

/**
 * Level of detail for items in the world. A flat item (a sword, an apple) is drawn as a front, a
 * back and one tiny face for every pixel edge, which is easily 100+ faces. From far away the edges
 * are a pixel or two thick, so past the set distance only the front and back are drawn.
 *
 * <p>Only lists where most faces are such 1/16-thin edges count as flat items, so block items
 * (cubes, stairs, fences) always keep every face. The trimmed lists are cached per model list.
 */
public final class ItemLod {
    private static final float EDGE = 1.0F / 16.0F + 1.0E-4F;
    private static final int MIN_QUADS = 12;
    private static final int MAX_CACHE = 4096;
    private static final Map<List<BakedQuad>, List<BakedQuad>> CACHE = new IdentityHashMap<>();

    private ItemLod() {
    }

    /** The faces to draw for an item at {@code pose} (camera-relative), shown in {@code context}. */
    public static List<BakedQuad> quads(List<BakedQuad> quads, Matrix4fc pose, ItemDisplayContext context) {
        if (quads.size() < MIN_QUADS || (context != ItemDisplayContext.GROUND && context != ItemDisplayContext.FIXED)) {
            return quads;
        }
        int limit = my.celium.org.bread.client.RenderCulling.lod(BreadMod.flatItemDistance);
        if (limit <= 0) {
            return quads;
        }
        float x = pose.m30(), y = pose.m31(), z = pose.m32();
        if (x * x + y * y + z * z <= (float) limit * limit) {
            return quads;
        }
        List<BakedQuad> flat = CACHE.get(quads);
        if (flat == null) {
            if (CACHE.size() >= MAX_CACHE) {
                CACHE.clear();
            }
            flat = flatten(quads);
            CACHE.put(quads, flat);
        }
        if (flat != quads) {
            BreadInfo.flatItem(quads.size() - flat.size());
        }
        return flat;
    }

    /** No enchantment shimmer on items in the world past {@code glint_distance}. */
    public static ItemStackRenderState.FoilType foil(ItemStackRenderState.FoilType type, Matrix4fc pose, ItemDisplayContext context) {
        if (type == ItemStackRenderState.FoilType.NONE || context == ItemDisplayContext.GUI || context.firstPerson()) {
            return type;
        }
        int limit = RenderCulling.lod(BreadMod.glintDistance);
        float x = pose.m30(), y = pose.m31(), z = pose.m32();
        if (limit <= 0 || x * x + y * y + z * z <= (float) limit * limit) {
            return type;
        }
        BreadInfo.glint();
        return ItemStackRenderState.FoilType.NONE;
    }

    private static List<BakedQuad> flatten(List<BakedQuad> quads) {
        List<BakedQuad> kept = new ArrayList<>();
        for (BakedQuad quad : quads) {
            if (!isPixelEdge(quad)) {
                kept.add(quad);
            }
        }
        // Not a flat item after all (a block model): keep everything.
        return kept.size() * 2 > quads.size() ? quads : List.copyOf(kept);
    }

    /** A side face (not north/south) whose shorter side is at most one pixel. */
    private static boolean isPixelEdge(BakedQuad quad) {
        Direction dir = quad.direction();
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            return false;
        }
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            Vector3fc p = quad.position(i);
            minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y()); maxY = Math.max(maxY, p.y());
            minZ = Math.min(minZ, p.z()); maxZ = Math.max(maxZ, p.z());
        }
        float a, b;
        switch (dir.getAxis()) {
            case X -> { a = maxY - minY; b = maxZ - minZ; }
            case Y -> { a = maxX - minX; b = maxZ - minZ; }
            default -> { a = maxX - minX; b = maxY - minY; }
        }
        return Math.min(a, b) <= EDGE;
    }
}
