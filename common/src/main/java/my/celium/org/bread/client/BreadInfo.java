package my.celium.org.bread.client;


/**
 * Live counters for {@code /bread info}: how much drawing work Bread skipped. Render hooks bump
 * the counters (render thread only, plain ints); {@link #frame()} runs once per frame and keeps
 * the last full frame, and the per-second counters roll over once a second.
 */
public final class BreadInfo {
    // Per frame
    static int entities, entitiesFar, entitiesHidden;
    static int blockEntities, blockEntitiesFar, blockEntitiesHidden;
    static int flatItems, facesSaved, mobLayersSkipped, pilesSimplified, nameTags, glints;
    // Per second (particles and physics run per tick, not per frame)
    static int particlesBlocked, collisionChecksSkipped;

    /** The last finished frame / second. */
    public static final class Snapshot {
        public int entities, entitiesFar, entitiesHidden;
        public int blockEntities, blockEntitiesFar, blockEntitiesHidden;
        public int flatItems, facesSaved, mobLayersSkipped, pilesSimplified, nameTags, glints;
        public int particlesBlocked, collisionChecksSkipped;
    }

    public static final Snapshot LAST = new Snapshot();
    private static long secondStart = System.currentTimeMillis();

    private BreadInfo() {
    }

    /** Called once per frame (camera hook): store the frame that just ended and start a new one. */
    public static void frame() {
        my.celium.org.bread.BreadMod.refreshEnabled();
        Snapshot s = LAST;
        s.entities = entities; s.entitiesFar = entitiesFar; s.entitiesHidden = entitiesHidden;
        s.blockEntities = blockEntities; s.blockEntitiesFar = blockEntitiesFar; s.blockEntitiesHidden = blockEntitiesHidden;
        s.flatItems = flatItems; s.facesSaved = facesSaved; s.mobLayersSkipped = mobLayersSkipped; s.pilesSimplified = pilesSimplified; s.nameTags = nameTags; s.glints = glints;
        entities = entitiesFar = entitiesHidden = 0;
        blockEntities = blockEntitiesFar = blockEntitiesHidden = 0;
        flatItems = facesSaved = mobLayersSkipped = pilesSimplified = nameTags = glints = 0;
        long now = System.currentTimeMillis();
        if (now - secondStart >= 1000) {
            s.particlesBlocked = particlesBlocked;
            s.collisionChecksSkipped = collisionChecksSkipped;
            particlesBlocked = collisionChecksSkipped = 0;
            secondStart = now;
        }
    }

    public static void flatItem(int saved) {
        flatItems++;
        facesSaved += saved;
    }

    public static void mobLayers() {
        mobLayersSkipped++;
    }

    public static void glint() {
        glints++;
    }

    public static void nameTag() {
        nameTags++;
    }

    public static void pile() {
        pilesSimplified++;
    }

    public static void particle() {
        particlesBlocked++;
    }

    public static void collision() {
        collisionChecksSkipped++;
    }
}
