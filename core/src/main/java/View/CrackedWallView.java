package View;

import Model.CrackedWall;
import Model.Enums.CrackedWallState;
import Model.SecretRoom;
import Controller.CrackedWallController.CharmPickup;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CrackedWallView {

    private static class TimedEffect {
        final Animation<TextureRegion> animation;
        final float x, y, w, h;
        float timer = 0f;

        TimedEffect(Animation<TextureRegion> animation, float x, float y, float w, float h) {
            this.animation = animation;
            this.x = x; this.y = y; this.w = w; this.h = h;
        }

        boolean update(float delta) {
            timer += delta;
            return timer >= animation.getAnimationDuration();
        }

        void render(Batch batch) {
            TextureRegion frame = animation.getKeyFrame(timer, false);
            if (frame != null) {
                batch.draw(frame, x, y, w, h);
            }
        }
    }

    private final CrackedWallAnimations assets;
    private final List<TimedEffect> dustEffects = new ArrayList<>();
    private final List<TimedEffect> breakEffects = new ArrayList<>();
    private final Texture darknessTexture;

    private final SpriteBatch promptBatch = new SpriteBatch();
    private final OrthographicCamera promptCamera = new OrthographicCamera();
    private final GlyphLayout promptLayout = new GlyphLayout();
    private BitmapFont promptFont;

    public CrackedWallView(CrackedWallAnimations assets) {
        this.assets = assets;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        darknessTexture = new Texture(pixmap);
        pixmap.dispose();

        createPromptFont();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void createPromptFont() {
        com.badlogic.gdx.files.FileHandle fontFile = Gdx.files.internal("OptimusPrincepsSemiBold.ttf");
        if (!fontFile.exists()) {
            promptFont = new BitmapFont();
            return;
        }
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 20;
        param.color = Color.YELLOW;
        promptFont = generator.generateFont(param);
        generator.dispose();
    }

    public void resize(int width, int height) {
        promptCamera.setToOrtho(false, width, height);
        promptCamera.update();
    }

    public void renderPickupPrompts(OrthographicCamera worldCamera, Collection<CharmPickup> pickups) {
        promptBatch.setProjectionMatrix(promptCamera.combined);
        promptBatch.begin();

        for (CharmPickup pickup : pickups) {
            if (pickup.collected || pickup.collecting || !pickup.playerNearby) continue;

            Rectangle b = pickup.bounds;
            Vector3 screenPos = worldCamera.project(new Vector3(
                b.x + b.width / 2f,
                b.y + b.height + 0.6f,
                0f
            ));

            String text = "Press W";
            promptLayout.setText(promptFont, text);
            promptFont.draw(promptBatch, text, screenPos.x - promptLayout.width / 2f, screenPos.y);
        }

        promptBatch.end();
    }

    public void update(Collection<CrackedWall> walls, float delta) {
        for (CrackedWall wall : walls) {
            if (wall.consumePendingHitEffect()) {
                Rectangle b = wall.getBounds();
                dustEffects.add(new TimedEffect(assets.getDustBurst(), b.x, b.y, b.width, b.height));
                assets.playHitSound();
            }
            if (wall.consumePendingBreakEffect()) {
                Rectangle b = wall.getBounds();
                breakEffects.add(new TimedEffect(assets.getStoneBreak(), b.x, b.y, b.width, b.height));
                assets.playBreakSound();
            }
        }

        tick(dustEffects, delta);
        tick(breakEffects, delta);
    }

    private void tick(List<TimedEffect> effects, float delta) {
        Iterator<TimedEffect> it = effects.iterator();
        while (it.hasNext()) {
            TimedEffect e = it.next();
            if (e.update(delta)) {
                it.remove();
            }
        }
    }

    public void renderWalls(Batch batch, Collection<CrackedWall> walls) {
        for (CrackedWall wall : walls) {
            if (wall.isDestroyed()) continue;

            Rectangle b = wall.getBounds();

            drawFlippable(batch, assets.getCrackStage0(), b, wall.isFlipX());

            TextureRegion overlay = null;
            if (wall.getState() == CrackedWallState.CRACK_STAGE1) {
                overlay = assets.getCrackStage1();
            } else if (wall.getState() == CrackedWallState.CRACK_STAGE2) {
                overlay = assets.getCrackStage2();
            }
            drawFlippable(batch, overlay, b, wall.isFlipX());
        }

        for (TimedEffect e : dustEffects) e.render(batch);
    }

    private void drawFlippable(Batch batch, TextureRegion region, Rectangle b, boolean flipX) {
        if (region == null) return;
        if (flipX) {

            batch.draw(region, b.x + b.width, b.y, -b.width, b.height);
        } else {
            batch.draw(region, b.x, b.y, b.width, b.height);
        }
    }

    public void renderSecretRoomDarkness(Batch batch, Collection<SecretRoom> rooms) {
        for (SecretRoom room : rooms) {
            if (room.isRevealed()) continue;
            Rectangle b = room.getBounds();
            batch.draw(darknessTexture, b.x, b.y, b.width, b.height);
        }

        for (TimedEffect e : breakEffects) e.render(batch);
    }

    public void renderCharmPickups(Batch batch, Collection<CharmPickup> pickups) {
        Animation<TextureRegion> idle = assets.getCharmIdle();

        if (idle == null || idle.getKeyFrames().length == 0) {
            return;
        }

        for (CharmPickup pickup : pickups) {
            if (pickup.collected) continue;

            TextureRegion frame = idle.getKeyFrame(pickup.idleTimer, true);
            if (frame == null) continue;

            float alpha = 1f - pickup.getFadeProgress();
            if (alpha <= 0f) continue;

            Rectangle b = pickup.bounds;
            com.badlogic.gdx.graphics.Color prev = batch.getColor();
            batch.setColor(prev.r, prev.g, prev.b, alpha);
            batch.draw(frame, b.x, b.y, b.width, b.height);
            batch.setColor(prev);
        }
    }

    public void dispose() {
        darknessTexture.dispose();
        promptFont.dispose();
        promptBatch.dispose();
    }
}
