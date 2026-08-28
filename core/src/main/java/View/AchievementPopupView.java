package View;

import Controller.AchievementManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

public class AchievementPopupView implements AchievementManager.Listener {

    private static class Info {
        final String title;
        final String desc;

        Info(String title, String desc) {
            this.title = title;
            this.desc = desc;
        }
    }

    private static final Map<String, Info> DEFS = new LinkedHashMap<>();

    static {
        DEFS.put(AchievementManager.COMPLETION, new Info("COMPLETION", "Successfully finished the game."));
        DEFS.put(AchievementManager.SPEEDRUN, new Info("SPEEDRUN", "Completed the game in under 20 minutes."));
        DEFS.put(AchievementManager.TRUE_HUNTER, new Info("TRUE HUNTER", "Slayed every unique enemy type."));
        DEFS.put(AchievementManager.FALSE_KNIGHT, new Info("DEFEAT FALSE KNIGHT", "Defeated the False Knight."));
        DEFS.put(AchievementManager.CUSTOM, new Info("SHARIFIAN KNIGHT", "Maxed out the soul vessel."));
    }

    private static final float SHOW_DURATION = 5f;
    private static final float SLIDE_TIME = 0.4f;
    private static final float PANEL_W = 420f;
    private static final float PANEL_H = 92f;
    private static final float MARGIN = 30f;
    private static final float ACCENT_W = 6f;

    private final Queue<String> pending = new ArrayDeque<>();
    private String current = null;
    private float timer = 0f;

    private final BitmapFont titleFont;
    private final BitmapFont nameFont;
    private final BitmapFont descFont;
    private final Texture panelTexture;
    private final Texture accentTexture;
    private final Texture glowTexture;
    private final OrthographicCamera camera = new OrthographicCamera();

    public AchievementPopupView() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 16;
        param.color = Color.LIGHT_GRAY;
        titleFont = generator.generateFont(param);

        param.size = 23;
        param.color = Color.GOLD;
        nameFont = generator.generateFont(param);

        param.size = 15;
        param.color = new Color(0.85f, 0.85f, 0.85f, 1f);
        descFont = generator.generateFont(param);

        generator.dispose();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.05f, 0.05f, 0.06f, 0.92f);
        pixmap.fill();
        panelTexture = new Texture(pixmap);
        pixmap.dispose();

        Pixmap accentPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        accentPixmap.setColor(Color.GOLD);
        accentPixmap.fill();
        accentTexture = new Texture(accentPixmap);
        accentPixmap.dispose();

        Pixmap glowPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        glowPixmap.setColor(1f, 0.85f, 0.4f, 0.16f);
        glowPixmap.fill();
        glowTexture = new Texture(glowPixmap);
        glowPixmap.dispose();
    }

    @Override
    public void onAchievementUnlocked(String achievementId) {
        pending.add(achievementId);
    }

    private static final String CHARM_FOUND_PREFIX = "ach_charm_found_";

    private Info resolveInfo(String id) {
        if (DEFS.containsKey(id)) {
            return DEFS.get(id);
        }
        if (id != null && id.startsWith(CHARM_FOUND_PREFIX)) {
            String charmName = id.substring(CHARM_FOUND_PREFIX.length()).replace('_', ' ');
            return new Info("NEW CHARM UNLOCKED", charmName);
        }
        return new Info(id, "");
    }

    public void update(float delta) {
        if (current == null) {
            current = pending.poll();
            timer = 0f;
            return;
        }
        timer += delta;
        if (timer >= SHOW_DURATION) {
            current = null;
        }
    }

    public void render(SpriteBatch batch, float screenWidth, float screenHeight) {
        if (current == null) return;

        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Info info = resolveInfo(current);
        float offscreenX = screenWidth + 10f;
        float targetX = screenWidth - PANEL_W - MARGIN;
        float y = screenHeight - PANEL_H - MARGIN;

        float x;
        float alpha = 1f;
        if (timer < SLIDE_TIME) {
            float t = Interpolation.pow3Out.apply(timer / SLIDE_TIME);
            x = offscreenX + (targetX - offscreenX) * t;
            alpha = t;
        } else if (timer > SHOW_DURATION - SLIDE_TIME) {
            float t = Interpolation.pow3In.apply((timer - (SHOW_DURATION - SLIDE_TIME)) / SLIDE_TIME);
            x = targetX + (offscreenX - targetX) * t;
            alpha = 1f - t;
        } else {
            x = targetX;
        }

        batch.begin();

        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(glowTexture, x - 8f, y - 8f, PANEL_W + 16f, PANEL_H + 16f);

        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(panelTexture, x, y, PANEL_W, PANEL_H);
        batch.draw(accentTexture, x, y, ACCENT_W, PANEL_H);

        titleFont.getColor().a = alpha;
        nameFont.getColor().a = alpha;
        descFont.getColor().a = alpha;

        titleFont.draw(batch, "ACHIEVEMENT UNLOCKED", x + 24, y + PANEL_H - 16);
        nameFont.draw(batch, info.title, x + 24, y + PANEL_H - 40);
        descFont.draw(batch, info.desc, x + 24, y + PANEL_H - 64, PANEL_W - 48, Align.left, true);

        titleFont.getColor().a = 1f;
        nameFont.getColor().a = 1f;
        descFont.getColor().a = 1f;
        batch.setColor(1f, 1f, 1f, 1f);

        batch.end();
    }

    public void dispose() {
        titleFont.dispose();
        nameFont.dispose();
        descFont.dispose();
        panelTexture.dispose();
        accentTexture.dispose();
        glowTexture.dispose();
    }
}
