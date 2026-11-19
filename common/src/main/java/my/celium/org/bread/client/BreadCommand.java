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
                    return say(VOICE.ok(t("cmd.fps", state(BreadMod.fpsCounter.get()))));
                }))
                .then(LiteralArgumentBuilder.<S>literal("coords").executes(c -> {
                    BreadMod.coordinates.set(!BreadMod.coordinates.get());
                    return say(VOICE.ok(t("cmd.coords", state(BreadMod.coordinates.get()))));
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
        say(VOICE.header("Bread " + BreadMod.META.version(), s(on ? "on" : "off") + " · " + Minecraft.getInstance().getFps() + " FPS"));
        say(TextUtil.msg(TextUtil.faint("  "),
                TextUtil.button(s(on ? "cmd.turn_off" : "cmd.turn_on"), on ? "/bread off" : "/bread on", s("cmd.master.tip")),
                "  ", TextUtil.button(s("cmd.info"), "/bread info", s("cmd.info.tip")),
                "  ", TextUtil.button(s("cmd.fps_button"), "/bread fps", s("cmd.fps.tip")),
                "  ", TextUtil.button(s("cmd.coords_button"), "/bread coords", s("cmd.coords.tip")),
                "  ", TextUtil.button(s("cmd.settings"), "/bread config", s("cmd.settings.tip")),
                "  ", TextUtil.suggest(s("cmd.toggle"), "/bread toggle ", s("cmd.toggle.tip")),
                "  ", TextUtil.suggest(s("cmd.set"), "/bread set ", s("cmd.set.tip"))));
        return 1;
    }

    /** What Bread skipped in the last frame (and last second for particles and physics). */
    private static int info() {
        BreadStats.used("info");
        if (!BreadMod.isModEnabled()) {
            return say(VOICE.caution(t("info.off", state(false))));
        }
        BreadInfo.Snapshot s = BreadInfo.LAST;
        say(VOICE.header(s("info.title"), s("info.subtitle", Minecraft.getInstance().getFps())));
        int entitySkipped = s.entitiesFar + s.entitiesHidden;
        int blockSkipped = s.blockEntitiesFar + s.blockEntitiesHidden;
        say(line(s("info.entities"), entitySkipped, s.entities, s("info.walls_far", n(s.entitiesHidden), n(s.entitiesFar))));
        say(line(s("info.block_entities"), blockSkipped, s.blockEntities, s("info.walls_far", n(s.blockEntitiesHidden), n(s.blockEntitiesFar))));
        say(row(s("info.flat_items"), n(s.flatItems), s("info.faces_saved", n(s.facesSaved))));
        say(row(s("info.mob_layers"), n(s.mobLayersSkipped), null));
        say(row(s("info.piles"), n(s.pilesSimplified), null));
        say(row(s("info.name_tags"), n(s.nameTags), null));
        say(row(s("info.glints"), n(s.glints), null));
        say(row(s("info.particles"), n(s.particlesBlocked) + "/s", null));
        say(row(s("info.physics"), n(s.collisionChecksSkipped) + "/s", null));
        return 1;
    }

    private static Component line(String label, int skipped, int total, String detail) {
        int percent = total == 0 ? 0 : Math.round(skipped * 100f / total);
        return row(label, s("info.skipped", n(skipped) + "/" + n(total), percent), detail);
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
        return say(on ? VOICE.ok(t("master.on", state(true)))
                : VOICE.caution(t("master.off", state(false))));
    }

    @SuppressWarnings("unchecked")
    private static <S> int toggle(CommandContext<S> c) {
        ConfigValue<?> v = find(StringArgumentType.getString(c, "setting"));
        if (v == null || !(v.get() instanceof Boolean)) {
            return say(VOICE.fail(t("cmd.no_toggle", TextUtil.infoText(StringArgumentType.getString(c, "setting")))));
        }
        ConfigValue<Boolean> b = (ConfigValue<Boolean>) v;
        b.set(!b.get());
        return say(VOICE.ok(t("cmd.toggled", TextUtil.infoText(label(v)), state(b.get()))));
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
                return say(VOICE.fail(t("cmd.no_number", TextUtil.infoText(key))));
            }
        } catch (IllegalArgumentException e) {
            return say(VOICE.fail(t("cmd.out_of_range", TextUtil.infoText(value + ""), TextUtil.infoText(label(v)))));
        }
        return say(VOICE.ok(t("cmd.now", TextUtil.infoText(label(v)), TextUtil.infoText(String.valueOf(v.get())))));
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
        return on ? TextUtil.success(s("on")) : TextUtil.error(s("off"));
    }

    /** The setting's name in the game's language (the same text as in the settings screen). */
    private static String label(ConfigValue<?> v) {
        String key = "bread.config." + v.key();
        return net.minecraft.locale.Language.getInstance().has(key) ? s("config." + v.key()) : v.key().replace('_', ' ');
    }

    private static String s(String path, Object... args) {
        return net.minecraft.client.resources.language.I18n.get("bread." + path, args);
    }

    /** A message whose {@code %s} slots keep the style of component arguments. */
    private static MutableComponent t(String path, Object... args) {
        return my.celium.org.translation.ModLang.component(s(path, keep(args)), args);
    }

    /** Leaves %s in place so {@link #t} can fill them with styled components. */
    private static Object[] keep(Object[] args) {
        Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            out[i] = "%" + (i + 1) + "$s";
        }
        return out;
    }

    private static int say(Component text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(text);
        }
        return 1;
    }
}
