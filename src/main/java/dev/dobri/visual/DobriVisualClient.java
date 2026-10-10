package dev.dobri.visual;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DobriVisualClient implements ClientModInitializer {
    public static final String MOD_ID = "dobri_visual";

    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        Modules.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dobri_visual.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new MenuScreen());
                }
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(MOD_ID, "hud"),
                Hud::render
        );
    }
}

class Module {
    final String id;
    final String name;
    boolean enabled;
    float anim;

    Module(String id, String name, boolean enabledByDefault) {
        this.id = id;
        this.name = name;
        this.enabled = enabledByDefault;
        this.anim = enabledByDefault ? 1f : 0f;
    }
}

class Modules {
    static final Module FPS = new Module("fps", "FPS", true);
    static final Module COORDINATES = new Module("coordinates", "Coordinates", true);
    static final List<Module> LIST = List.of(FPS, COORDINATES);

    private static final Gson GSON = new Gson();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("dobri_visual.json");
    }

    static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            Map<String, Boolean> map = GSON.fromJson(r, new TypeToken<Map<String, Boolean>>() {}.getType());
            if (map == null) return;
            for (Module m : LIST) {
                Boolean v = map.get(m.id);
                if (v != null) {
                    m.enabled = v;
                    m.anim = v ? 1f : 0f;
                }
            }
        } catch (Exception e) {
            System.err.println("[Dobri Visual] config read failed: " + e);
        }
    }

    static void save() {
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (Module m : LIST) map.put(m.id, m.enabled);
        try {
            Path f = file();
            Files.createDirectories(f.getParent());
            try (Writer w = Files.newBufferedWriter(f)) {
                GSON.toJson(map, w);
            }
        } catch (Exception e) {
            System.err.println("[Dobri Visual] config save failed: " + e);
        }
    }
}

class Ui {
    static final int BG_DIM = 0x99000000;
    static final int PANEL_BORDER = 0xFF2E2E36;
    static final int PANEL_BG = 0xFF141417;
    static final int ROW_BG = 0xFF1C1C21;
    static final int ROW_HOVER = 0xFF25252C;
    static final int PURPLE = 0xFF8B5CF6;
    static final int TRACK_OFF = 0xFF3A3A44;
    static final int TEXT = 0xFFE6E6EA;
    static final int TEXT_DIM = 0xFF8E8E99;

    static void roundRect(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x + 2, y, x + w - 2, y + h, color);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
        c.fill(x, y + 2, x + w, y + h - 2, color);
    }

    static int lerpColor(int a, int b, float t) {
        int aa = (a >>> 24) & 255, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = (b >>> 24) & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        int ra = (int) (aa + (ba - aa) * t);
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }
}

class Hud {
    static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;

        TextRenderer tr = mc.textRenderer;
        int x = 6;
        int y = 6;

        if (Modules.FPS.enabled) {
            drawLine(context, tr, "FPS: " + mc.getCurrentFps(), x, y);
            y += 16;
        }
        if (Modules.COORDINATES.enabled) {
            String s = String.format(Locale.ROOT, "X: %.1f  Y: %.1f  Z: %.1f",
                    player.getX(), player.getY(), player.getZ());
            drawLine(context, tr, s, x, y);
            y += 16;
        }
    }

    private static void drawLine(DrawContext c, TextRenderer tr, String text, int x, int y) {
        Text t = Text.literal(text);
        int w = tr.getWidth(t);
        Ui.roundRect(c, x, y, w + 12, 14, 0xB0101014);
        c.fill(x + 1, y + 2, x + 3, y + 12, Ui.PURPLE);
        c.drawTextWithShadow(tr, t, x + 7, y + 3, Ui.TEXT);
    }
}

class MenuScreen extends Screen {
    private static final int PANEL_W = 220;
    private static final int HEADER_H = 34;
    private static final int ROW_H = 22;
    private static final int ROW_GAP = 4;
    private static final int PAD = 8;

    MenuScreen() {
        super(Text.literal("dobri visual"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private int panelH() {
        return HEADER_H + PAD + Modules.LIST.size() * (ROW_H + ROW_GAP) + PAD - ROW_GAP;
    }

    private int panelX() { return (this.width - PANEL_W) / 2; }
    private int panelY() { return (this.height - panelH()) / 2; }

    private int rowY(int index) {
        return panelY() + HEADER_H + PAD + index * (ROW_H + ROW_GAP);
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, this.width, this.height, Ui.BG_DIM);

        int px = panelX(), py = panelY(), ph = panelH();

        Ui.roundRect(c, px - 1, py - 1, PANEL_W + 2, ph + 2, Ui.PANEL_BORDER);
        Ui.roundRect(c, px, py, PANEL_W, ph, Ui.PANEL_BG);
        c.fill(px + 3, py, px + PANEL_W - 3, py + 2, Ui.PURPLE);

        Text title = Text.literal("dobri visual");
        int tw = this.textRenderer.getWidth(title);
        c.drawTextWithShadow(this.textRenderer, title, px + (PANEL_W - tw) / 2, py + 12, Ui.PURPLE);

        for (int i = 0; i < Modules.LIST.size(); i++) {
            Module m = Modules.LIST.get(i);
            int ry = rowY(i);
            int rx = px + PAD;
            int rw = PANEL_W - PAD * 2;
            boolean hover = mouseX >= rx && mouseX < rx + rw && mouseY >= ry && mouseY < ry + ROW_H;

            Ui.roundRect(c, rx, ry, rw, ROW_H, hover ? Ui.ROW_HOVER : Ui.ROW_BG);

            float target = m.enabled ? 1f : 0f;
            m.anim += (target - m.anim) * 0.25f;
            if (Math.abs(target - m.anim) < 0.01f) m.anim = target;

            c.drawTextWithShadow(this.textRenderer, Text.literal(m.name), rx + 8, ry + 7, Ui.TEXT);

            int tw2 = 34, th = 14;
            int tx = rx + rw - tw2 - 8;
            int ty = ry + (ROW_H - th) / 2;
            Ui.roundRect(c, tx, ty, tw2, th, Ui.lerpColor(Ui.TRACK_OFF, Ui.PURPLE, m.anim));
            int knobX = tx + 2 + Math.round((tw2 - th) * m.anim);
            Ui.roundRect(c, knobX, ty + 2, th - 4, th - 4, 0xFFFFFFFF);

            Text lt = Text.literal(m.enabled ? "ON" : "OFF");
            int lw = this.textRenderer.getWidth(lt);
            c.drawTextWithShadow(this.textRenderer, lt, tx - lw - 6, ry + 7, m.enabled ? Ui.PURPLE : Ui.TEXT_DIM);
        }

        Text hint = Text.literal("Right Shift - close");
        int hw = this.textRenderer.getWidth(hint);
        c.drawTextWithShadow(this.textRenderer, hint, (this.width - hw) / 2, py + ph + 6, Ui.TEXT_DIM);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            double mx = click.x(), my = click.y();
            int px = panelX();
            for (int i = 0; i < Modules.LIST.size(); i++) {
                int ry = rowY(i);
                int rx = px + PAD;
                int rw = PANEL_W - PAD * 2;
                if (mx >= rx && mx < rx + rw && my >= ry && my < ry + ROW_H) {
                    Module m = Modules.LIST.get(i);
                    m.enabled = !m.enabled;
                    Modules.save();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void removed() {
        Modules.save();
    }
}
