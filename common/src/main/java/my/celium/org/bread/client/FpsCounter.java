package my.celium.org.bread.client;

import my.celium.org.bread.BreadMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** The FPS counter and coordinates: one small box in the chosen corner. FPS is green at 60+, yellow at 30+, red below. */
public final class FpsCounter {
    private static final int PAD = 3;
    private static final int MARGIN = 4;

    private FpsCounter() {
    }

    public static void render(GuiGraphicsExtractor gfx) {
        Minecraft mc = Minecraft.getInstance();
        boolean fps = BreadMod.onFast(BreadMod.fpsCounter);
        boolean coords = BreadMod.onFast(BreadMod.coordinates);
        if ((!fps && !coords) || mc.player == null) {
            return;
        }
        String corner = BreadMod.fpsCounterCorner.get().trim().toLowerCase(java.util.Locale.ROOT);
        boolean right = corner.endsWith("right");
        boolean bottom = corner.startsWith("bottom");
        java.util.List<String> lines = new java.util.ArrayList<>();
        java.util.List<Integer> colors = new java.util.ArrayList<>();
        if (fps) {
            int value = mc.getFps();
            lines.add(value + " FPS");
            colors.add(value >= 60 ? 0xFF55FF55 : value >= 30 ? 0xFFFFFF55 : 0xFFFF5555);
        }
        if (coords) {
            net.minecraft.core.BlockPos pos = mc.player.blockPosition();
            lines.add(pos.getX() + " " + pos.getY() + " " + pos.getZ());
            colors.add(0xFFFFFFFF);
            lines.add(facing(mc.player.getDirection()) + " · " + biome(mc, pos));
            colors.add(0xFFC4C4CC);
        }
        Font font = mc.font;
        int w = 0;
        for (String line : lines) {
            w = Math.max(w, font.width(line));
        }
        w += PAD * 2;
        int lineH = font.lineHeight + 1;
        int h = lines.size() * lineH + PAD * 2 - 2;
        int x = right ? gfx.guiWidth() - w - MARGIN : MARGIN;
        int y = bottom ? gfx.guiHeight() - h - MARGIN : MARGIN;
        gfx.fill(x, y, x + w, y + h, 0x90000000);
        for (int i = 0; i < lines.size(); i++) {
            gfx.text(font, lines.get(i), x + PAD, y + PAD + i * lineH, colors.get(i));
        }
    }

    private static String facing(net.minecraft.core.Direction dir) {
        String n = dir.getName();
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }

    /** "Dark Forest" from minecraft:dark_forest; modded biomes keep their path the same way. */
    private static String biome(Minecraft mc, net.minecraft.core.BlockPos pos) {
        return mc.level.getBiome(pos).unwrapKey().map(key -> {
            StringBuilder out = new StringBuilder();
            for (String part : key.identifier().getPath().split("_")) {
                if (!part.isEmpty()) {
                    out.append(out.length() == 0 ? "" : " ").append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
                }
            }
            return out.toString();
        }).orElse("?");
    }
}
