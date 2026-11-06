package my.celium.org.bread;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import my.celium.org.config.ConfigValue;
import my.celium.org.stats.ModStats;

/**
 * Anonymous stats for the FastStats project {@code bread}, sent through Mycel under its consent:
 * which features players keep on, their number settings, the FPS they get, and how often each
 * feature is used.
 */
public final class BreadStats {
    /** Project token from faststats.dev (public, ships in the jar). */
    private static final String TOKEN = "866c03e9cd6ae158d7aa03f25396bf80";

    private static volatile ModStats stats;
    /** Last FPS seen on the client (0 on servers), set every frame by the camera hook. */
    public static volatile int fps;

    private BreadStats() {
    }

    static void start() {
        if (TOKEN.isEmpty()) {
            return;
        }
        stats = ModStats.builder(BreadMod.META, TOKEN)
                .bool("enabled", () -> BreadMod.enabled.get())
                .stringArray("features_on", BreadStats::featuresOn)
                .numberMap("settings", BreadStats::numbers)
                .number("fps", () -> fps > 0 ? fps : null)
                .counters("used")
                .errors("my.celium.org.bread.")
                .start();
    }

    private static String[] featuresOn() {
        List<String> on = new ArrayList<>();
        for (ConfigValue<?> v : BreadMod.config.values()) {
            if (v.get() instanceof Boolean b && b && !v.key().equals("enabled")) {
                on.add(v.key());
            }
        }
        return on.toArray(String[]::new);
    }

    private static Map<String, Number> numbers() {
        Map<String, Number> out = new LinkedHashMap<>();
        for (ConfigValue<?> v : BreadMod.config.values()) {
            if (v.get() instanceof Number n) {
                out.put(v.key(), n);
            }
        }
        return out;
    }

    /** Counts one use of a feature in the {@code used} counter map. */
    public static void used(String feature) {
        ModStats current = stats;
        if (current != null) {
            current.count("used", feature);
        }
    }
}
