package my.celium.org.bread.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Locale;
import my.celium.org.bread.BreadMod;
import my.celium.org.bread.BreadStats;
import my.celium.org.client.ClientHooks;
import my.celium.org.client.MycelScreens;
import my.celium.org.config.ConfigValue;
import my.celium.org.util.ChatVoice;
import my.celium.org.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * The client command {@code /bread}. Works on any server, because it only changes this
 * player's own Bread config (in singleplayer that includes the gameplay tweaks).
 *
 * <pre>
 * /bread                    status and the list of commands
 * /bread info               what Bread skipped in the last frame
 * /bread fps                FPS counter on/off
 * /bread on | off           master switch
 * /bread toggle &lt;setting&gt;   flip an on/off setting
 * /bread set &lt;setting&gt; &lt;n&gt;  set a number setting (0 = vanilla)
 * /bread config             open the config screen
 * </pre>
 *
 * Built against brigadier's generic source type, so every loader can register it with its own
 * client command source. Feedback goes straight to the local chat.
 */
public final class BreadCommand {
    private static final ChatVoice VOICE = TextUtil.voice("Bread");
    private BreadCommand() {
    }

    public static <S> LiteralArgumentBuilder<S> build() {
        return LiteralArgumentBuilder.<S>literal("bread")
                .executes(c -> status())
                .then(LiteralArgumentBuilder.<S>literal("info").executes(c -> info()))
                .then(LiteralArgumentBuilder.<S>literal("fps").executes(c -> {
                    BreadMod.fpsCounter.set(!BreadMod.fpsCounter.get());
                    return say(VOICE.ok("FPS counter ", state(BreadMod.fpsCounter.get()), "."));
                }))
                .then(LiteralArgumentBuilder.<S>literal("coords").executes(c -> {
                    BreadMod.coordinates.set(!BreadMod.coordinates.get());
                    return say(VOICE.ok("Coordinates ", state(BreadMod.coordinates.get()), "."));
                }))
                .then(LiteralArgumentBuilder.<S>literal("on").executes(c -> master(true)))
                .then(LiteralArgumentBuilder.<S>literal("off").executes(c -> master(false)))
                .then(LiteralArgumentBuilder.<S>literal("config").executes(c -> {
                    Minecraft mc = Minecraft.getInstance();
                    // The chat screen closes right after the command runs; open on the next frame.
                    mc.execute(() -> MycelScreens.open(ClientHooks.createConfigScreen(null, BreadMod.ID)));
                    return 1;
                }))
                .then(LiteralArgumentBuilder.<S>literal("toggle")
                        .then(RequiredArgumentBuilder.<S, String>argument("setting", StringArgumentType.word())
                                .suggests((c, b) -> {
                                    for (ConfigValue<?> v : BreadMod.config.values()) {
                                        if (v.get() instanceof Boolean && !v.key().equals("enabled")) {
                                            b.suggest(v.key());
                                        }
                                    }
                                    return b.buildFuture();
                                })
                                .executes(BreadCommand::toggle)))
                .then(LiteralArgumentBuilder.<S>literal("set")
                        .then(RequiredArgumentBuilder.<S, String>argument("setting", StringArgumentType.word())
                                .suggests((c, b) -> {
                                    for (ConfigValue<?> v : BreadMod.config.values()) {
                                        if (v.get() instanceof Number) {
                                            b.suggest(v.key());
                                        }
                                    }
                                    return b.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<S, Double>argument("value", DoubleArgumentType.doubleArg(0))
                                        .executes(BreadCommand::set))));
    }

    private static int status() {
        BreadStats.used("command");
        boolean on = BreadMod.isModEnabled();
        say(VOICE.header("Bread " + BreadMod.META.version(), (on ? "on" : "off") + " · " + Minecraft.getInstance().getFps() + " FPS"));
        say(TextUtil.msg(TextUtil.faint("  "),
                TextUtil.button(on ? "Turn off" : "Turn on", on ? "/bread off" : "/bread on", "Master switch for every Bread feature"),
                "  ", TextUtil.button("Info", "/bread info", "See what Bread is skipping right now"),
                "  ", TextUtil.button("FPS counter", "/bread fps", "Show or hide the FPS counter"),
                "  ", TextUtil.button("Coords", "/bread coords", "Show or hide your coordinates, facing and biome"),
                "  ", TextUtil.button("Settings", "/bread config", "Open the Bread settings"),
                "  ", TextUtil.suggest("Toggle", "/bread toggle ", "Flip one setting: /bread toggle <setting>"),
                "  ", TextUtil.suggest("Set", "/bread set ", "Set a number: /bread set <setting> <number>, 0 = vanilla")));
        return 1;
    }

    /** What Bread skipped in the last frame (and last second for particles and physics). */
    private static int info() {
        BreadStats.used("info");
        if (!BreadMod.isModEnabled()) {
            return say(VOICE.caution("Bread is ", state(false), ", so it is not skipping anything right now."));
        }
        BreadInfo.Snapshot s = BreadInfo.LAST;
        say(VOICE.header("Bread info", Minecraft.getInstance().getFps() + " FPS · last frame"));
        int entitySkipped = s.entitiesFar + s.entitiesHidden;
        int blockSkipped = s.blockEntitiesFar + s.blockEntitiesHidden;
        say(line("Entities", entitySkipped, s.entities, n(s.entitiesHidden) + " walls, " + n(s.entitiesFar) + " far"));
        say(line("Chests, signs", blockSkipped, s.blockEntities, n(s.blockEntitiesHidden) + " walls, " + n(s.blockEntitiesFar) + " far"));
        say(row("Flat items", n(s.flatItems), n(s.facesSaved) + " faces saved"));
        say(row("Mobs, no armor drawn", n(s.mobLayersSkipped), null));
        say(row("Piles drawn as one", n(s.pilesSimplified), null));
        say(row("Name tags hidden", n(s.nameTags), null));
        say(row("Glints skipped", n(s.glints), null));
        say(row("Particles held back", n(s.particlesBlocked) + "/s", null));
        say(row("Physics checks skipped", n(s.collisionChecksSkipped) + "/s", null));
        return 1;
    }

    private static Component line(String label, int skipped, int total, String detail) {
        int percent = total == 0 ? 0 : Math.round(skipped * 100f / total);
        return row(label, n(skipped) + "/" + n(total) + " skipped (" + percent + "%)", detail);
    }

    private static String n(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private static Component row(String label, String value, String detail) {
        MutableComponent out = TextUtil.msg(TextUtil.faint("  "), TextUtil.muted(label + ": "), TextUtil.infoText(value));
        if (detail != null) {
            out.append(TextUtil.faint(" · " + detail));
        }
        return out;
    }

    private static int master(boolean on) {
        BreadMod.enabled.set(on);
        return say(on ? VOICE.ok("Bread is ", state(true), ". Every feature uses its own setting again.")
                : VOICE.caution("Bread is ", state(false), ". Everything runs like vanilla until you turn it back on."));
    }

    @SuppressWarnings("unchecked")
    private static <S> int toggle(CommandContext<S> c) {
        ConfigValue<?> v = find(StringArgumentType.getString(c, "setting"));
        if (v == null || !(v.get() instanceof Boolean)) {
            return say(VOICE.fail("There is no on/off setting called ", TextUtil.infoText(StringArgumentType.getString(c, "setting")), "."));
        }
        ConfigValue<Boolean> b = (ConfigValue<Boolean>) v;
        b.set(!b.get());
        return say(VOICE.ok(TextUtil.infoText(label(v)), " ", state(b.get()), "."));
    }

    @SuppressWarnings("unchecked")
    private static <S> int set(CommandContext<S> c) {
        String key = StringArgumentType.getString(c, "setting");
        double value = DoubleArgumentType.getDouble(c, "value");
        ConfigValue<?> v = find(key);
        try {
            if (v != null && v.get() instanceof Integer) {
                ((ConfigValue<Integer>) v).set((int) Math.round(value));
            } else if (v != null && v.get() instanceof Double) {
                ((ConfigValue<Double>) v).set(value);
            } else {
                return say(VOICE.fail("There is no number setting called ", TextUtil.infoText(key), "."));
            }
        } catch (IllegalArgumentException e) {
            return say(VOICE.fail(TextUtil.infoText(value + ""), " is out of range for ", TextUtil.infoText(label(v)), "."));
        }
        return say(VOICE.ok(TextUtil.infoText(label(v)), " is now ", TextUtil.infoText(String.valueOf(v.get())), "."));
    }

    private static ConfigValue<?> find(String key) {
        String k = key.toLowerCase(Locale.ROOT);
        for (ConfigValue<?> v : BreadMod.config.values()) {
            if (v.key().equals(k)) {
                return v;
            }
        }
        return null;
    }

    private static MutableComponent state(boolean on) {
        return on ? TextUtil.success("on") : TextUtil.error("off");
    }

    private static String label(ConfigValue<?> v) {
        return v.key().replace('_', ' ');
    }

    private static int say(Component text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(text);
        }
        return 1;
    }
}
