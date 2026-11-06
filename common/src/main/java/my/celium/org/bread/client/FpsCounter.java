package my.celium.org.bread.client;

import my.celium.org.bread.BreadMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** The FPS counter: a small box in the chosen corner, green at 60+, yellow at 30+, red below. */
public final class FpsCounter {
    private static final int PAD = 3;
    private static final int MARGIN = 4;

    private FpsCounter() {
    }

    public static void render(GuiGraphicsExtractor gfx) {
        Minecraft mc = Minecraft.getInstance();
        if (!BreadMod.onFast(BreadMod.fpsCounter) || mc.player == null) {
            return;
        }
        int fps = mc.getFps();
        String text = fps + " FPS";
        Font font = mc.font;
        int w = font.width(text) + PAD * 2;
        int h = font.lineHeight + PAD * 2 - 1;
        String corner = BreadMod.fpsCounterCorner.get().trim().toLowerCase(java.util.Locale.ROOT);
        boolean right = corner.endsWith("right");
        boolean bottom = corner.startsWith("bottom");
        int x = right ? gfx.guiWidth() - w - MARGIN : MARGIN;
        int y = bottom ? gfx.guiHeight() - h - MARGIN : MARGIN;
        int color = fps >= 60 ? 0xFF55FF55 : fps >= 30 ? 0xFFFFFF55 : 0xFFFF5555;
        gfx.fill(x, y, x + w, y + h, 0x90000000);
        gfx.text(font, text, x + PAD, y + PAD, color);
    }
}
