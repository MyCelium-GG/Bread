package my.celium.org.bread;

import my.celium.org.Mycel;
import my.celium.org.config.ConfigBuilder;
import my.celium.org.config.ConfigValue;
import my.celium.org.config.MycelConfig;
import my.celium.org.metadata.ModMetadata;

/**
 * Bread: the small things vanilla should do, plus client-side optimizations. Every tweak has its
 * own switch in the config.
 *
 * <p>Gameplay tweaks run on the server (or in singleplayer), so a server needs Bread for them.
 * The performance options are client-only and work everywhere.
 */
public final class BreadMod {
    public static final String ID = "bread";

    public static final ModMetadata META = ModMetadata.builder(ID, "Bread", "1.1.0")
            .author("MyCelium")
            .website("https://www.curseforge.com/minecraft/mc-mods/bread-fps")
            .updateUrl("https://wiki.mycelium-gg.org/updates/bread.json")
            .build();

    public static ConfigValue<Boolean> enabled;

    // Tweaks (server side)
    public static ConfigValue<Boolean> rightClickHarvest;
    public static ConfigValue<Boolean> doubleDoors;
    public static ConfigValue<Boolean> ladderExtend;
    public static ConfigValue<Boolean> boneMealCaneAndCactus;
    public static ConfigValue<Boolean> shovelPathToDirt;
    public static ConfigValue<Boolean> leashVillagers;
    public static ConfigValue<Boolean> featherFallingSavesCrops;
    public static ConfigValue<Boolean> snowGolemPumpkin;

    // Performance (client side)
    public static ConfigValue<Integer> decorEntityDistance;
    public static ConfigValue<Integer> droppedItemDistance;
    public static ConfigValue<Integer> signDistance;
    public static ConfigValue<Integer> backgroundFps;
    public static ConfigValue<Integer> farMobDetailDistance;
    public static ConfigValue<Integer> itemPileDistance;
    public static ConfigValue<Integer> maxParticles;
    public static ConfigValue<Integer> flatItemDistance;
    public static ConfigValue<Boolean> fastDroppedItems;
    public static ConfigValue<Integer> nameTagDistance;
    public static ConfigValue<Integer> glintDistance;
    public static ConfigValue<Boolean> hideUnseen;

    // Performance (server side)
    public static ConfigValue<Double> itemMergeRadius;
    public static ConfigValue<Double> xpMergeRadius;

    // HUD (client side)
    public static ConfigValue<Boolean> fpsCounter;
    public static ConfigValue<String> fpsCounterCorner;
    public static ConfigValue<Boolean> coordinates;

    // Zoom (client side)
    public static ConfigValue<Boolean> zoom;
    public static ConfigValue<Integer> zoomLevel;
    public static ConfigValue<Boolean> zoomScroll;
    public static ConfigValue<Boolean> zoomSmooth;

    public static MycelConfig config;

    private BreadMod() {
    }

    /** Called once from each loader's entrypoint. */
    public static void init() {
        Mycel.initialize();
        Mycel.registerMod(META);

        ConfigBuilder builder = Mycel.config(META);
        enabled = builder.booleanValue("enabled", true, "Master switch for Bread.");

        builder.category("tweaks");
        rightClickHarvest = builder.booleanValue("right_click_harvest", true,
                "Right-click a grown crop to harvest and replant it.");
        doubleDoors = builder.booleanValue("double_doors", true,
                "Double doors open and close together.");
        ladderExtend = builder.booleanValue("ladder_extend", true,
                "Click a ladder with a ladder to extend it down (or up when you look up).");
        boneMealCaneAndCactus = builder.booleanValue("bone_meal_cane_and_cactus", true,
                "Bone meal grows sugar cane and cactus by one block.");
        shovelPathToDirt = builder.booleanValue("shovel_path_to_dirt", true,
                "Right-click a dirt path or farmland with a shovel to turn it back into dirt.");
        leashVillagers = builder.booleanValue("leash_villagers", true,
                "Villagers and wandering traders can be put on a lead.");
        featherFallingSavesCrops = builder.booleanValue("feather_falling_saves_crops", true,
                "Boots with Feather Falling do not trample farmland.");
        snowGolemPumpkin = builder.booleanValue("snow_golem_pumpkin", true,
                "Put a carved pumpkin back on a sheared snow golem.");

        builder.category("performance");
        hideUnseen = builder.booleanValue("hide_unseen", true,
                "Skip drawing mobs, chests, signs and frames fully behind walls (checked on a spare thread).");
        decorEntityDistance = builder.intValue("decor_entity_distance", 64, 0, 512,
                "Blocks away to stop drawing item frames, paintings and armor stands. 0 = vanilla.");
        droppedItemDistance = builder.intValue("dropped_item_distance", 48, 0, 512,
                "Blocks away to stop drawing dropped items and XP orbs. 0 = vanilla.");
        signDistance = builder.intValue("sign_distance", 32, 0, 512,
                "Blocks away to stop drawing signs, banners and heads. 0 = vanilla.");
        backgroundFps = builder.intValue("background_fps", 30, 0, 260,
                "FPS limit while the game window is not focused. 0 = no limit.");
        farMobDetailDistance = builder.intValue("far_mob_detail_distance", 32, 0, 512,
                "Blocks away to stop drawing armor and held items on mobs (not players). 0 = vanilla.");
        itemPileDistance = builder.intValue("item_pile_distance", 16, 0, 512,
                "Blocks away to draw a dropped stack as one item instead of a pile. 0 = vanilla.");
        maxParticles = builder.intValue("max_particles", 4000, 0, 16384,
                "Most particles of one kind at once (vanilla allows 16384). 0 = vanilla.");
        flatItemDistance = builder.intValue("flat_item_distance", 20, 0, 512,
                "Blocks away to draw dropped and framed items flat, without their pixel edges. 0 = vanilla.");
        nameTagDistance = builder.intValue("name_tag_distance", 32, 0, 512,
                "Blocks away to stop drawing name tags of mobs (not players). 0 = vanilla.");
        glintDistance = builder.intValue("glint_distance", 24, 0, 512,
                "Blocks away to stop drawing the enchantment shimmer on items. 0 = vanilla.");
        fastDroppedItems = builder.booleanValue("fast_dropped_items", true,
                "Your game skips entity collision checks for dropped items and XP orbs (the server still does them).");
        itemMergeRadius = builder.doubleValue("item_merge_radius", 1.5, 0.0, 8.0,
                "Server: how close dropped items merge into one stack (vanilla 0.5). 0 = vanilla.");
        xpMergeRadius = builder.doubleValue("xp_merge_radius", 2.0, 0.0, 8.0,
                "Server: how close XP orbs merge into one orb (vanilla 0.5). 0 = vanilla.");

        builder.category("hud");
        fpsCounter = builder.booleanValue("fps_counter", false,
                "Show your FPS in a corner of the screen.");
        coordinates = builder.booleanValue("coordinates", false,
                "Show your coordinates, facing and biome under the FPS counter's corner.");
        fpsCounterCorner = builder.stringValue("fps_counter_corner", "top_left", 16,
                "Where the FPS counter sits: top_left, top_right, bottom_left or bottom_right.");

        builder.category("zoom");
        zoom = builder.booleanValue("zoom", true,
                "Hold the zoom key (C, change it in Controls) to look closer.");
        zoomLevel = builder.intValue("zoom_level", 4, 2, 50,
                "How many times closer the zoom starts.");
        zoomScroll = builder.booleanValue("zoom_scroll", true,
                "Scroll while zooming to zoom further in or out.");
        zoomSmooth = builder.booleanValue("zoom_smooth", true,
                "Ease the zoom in and out instead of snapping.");
        config = builder.build();
        enabled.onChange(v -> refreshEnabled());
        refreshEnabled();
        BreadStats.start();
    }

    public static boolean isModEnabled() {
        return Mycel.isEnabled(ID) && enabled != null && enabled.get();
    }

    /** {@link #isModEnabled()} cached for hot render paths; refreshed every frame and on change. */
    private static volatile boolean enabledFast = true;

    public static boolean enabledFast() {
        return enabledFast;
    }

    /** Re-reads the master switch into the cache. Called once per frame and when it changes. */
    public static void refreshEnabled() {
        enabledFast = isModEnabled();
    }

    /** True when Bread is on and {@code toggle} is on. */
    public static boolean on(ConfigValue<Boolean> toggle) {
        return toggle != null && toggle.get() && isModEnabled();
    }

    /** The number setting {@code value} if Bread is on, else 0 (vanilla). */
    public static int distance(ConfigValue<Integer> value) {
        return value != null && enabledFast ? value.get() : 0;
    }

    /** {@link #on} for per-frame client paths: uses the cached master switch. */
    public static boolean onFast(ConfigValue<Boolean> toggle) {
        return enabledFast && toggle != null && toggle.get();
    }

    /** The radius setting {@code value} if Bread is on, else 0 (vanilla). */
    public static double radius(ConfigValue<Double> value) {
        return value != null && isModEnabled() ? value.get() : 0.0;
    }
}
