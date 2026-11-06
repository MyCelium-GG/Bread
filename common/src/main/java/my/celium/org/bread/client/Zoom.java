package my.celium.org.bread.client;

import com.mojang.blaze3d.platform.InputConstants;
import my.celium.org.bread.BreadMod;
import my.celium.org.bread.BreadStats;
import net.minecraft.client.KeyMapping;
import my.celium.org.client.MycelScreens;
import net.minecraft.util.Util;

/**
 * Zoom: hold the key (C by default, rebindable in Controls) to look closer. The scroll wheel
 * changes how far while zoomed, the zoom eases in and out, and the mouse slows down with the
 * zoom so aiming stays steady. Client side only.
 */
public final class Zoom {
    public static final KeyMapping KEY = new KeyMapping("key.bread.zoom",
            KeyInput.KEYBOARD, InputConstants.KEY_C, KeyMapping.Category.MISC);

    private static final double MIN = 1.5;
    private static final double MAX = 50.0;

    /** Magnification while the key is held; scroll changes it, it resets to the config value on release. */
    private static double level = -1;
    /** The FOV multiplier being shown: 1 = no zoom. */
    private static double current = 1.0;
    private static long lastMs = -1;

    private Zoom() {
    }

    public static boolean zooming() {
        return BreadMod.onFast(BreadMod.zoom) && MycelScreens.current() == null && KEY.isDown();
    }

    /** The multiplier for the camera's FOV this frame. */
    public static float fovMultiplier() {
        boolean held = zooming();
        if (!held) {
            level = -1;
        } else if (level < 0) {
            level = BreadMod.zoomLevel.get();
            BreadStats.used("zoom");
        }
        double target = held ? 1.0 / level : 1.0;
        long now = Util.getMillis();
        double dt = lastMs < 0 ? 1.0 : Math.min(1.0, (now - lastMs) / 1000.0);
        lastMs = now;
        if (BreadMod.zoomSmooth.get()) {
            current += (target - current) * Math.min(1.0, dt * 14.0);
            if (Math.abs(current - target) < 0.001) {
                current = target;
            }
        } else {
            current = target;
        }
        return (float) current;
    }

    /** The mouse turns slower by the same amount the view is zoomed. */
    public static double mouseScale() {
        return current;
    }

    /** Scroll while zoomed changes the zoom; true when the scroll was used here. */
    public static boolean scroll(double amount) {
        if (!zooming() || !BreadMod.zoomScroll.get() || amount == 0) {
            return false;
        }
        if (level < 0) {
            level = BreadMod.zoomLevel.get();
        }
        level = Math.max(MIN, Math.min(MAX, level * (amount > 0 ? 1.25 : 0.8)));
        return true;
    }
}
