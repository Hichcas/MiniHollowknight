package View.Screen;

import Model.Knight;
import View.SoulMaskShader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.ArrayList;
import java.util.List;

public class HUDScreen {
    private static final float SOUL_VESSEL_INTRO_DURATION = 1f;
    private static final float SOUL_MOTION_ANIM_DURATION = 0.12f;
    private static final float SOUL_CAPACITY = 99f;
    private static final float SOUL_FILL_LERP_SPEED = 10f;
    private Texture soulLiquidMask;
    private Texture whitePixel;
    private final List<Texture> loadedTextures = new ArrayList<>();
    private BitmapFont cheatStatusFont;
    private final Knight knight;
    private final OrthographicCamera hudCamera;

    private float hudStateTime = 0f;
    private ShaderProgram soulMaskShader;

    private Animation<TextureRegion> maskBreakAnim;
    private Animation<TextureRegion> maskHealAnim;
    private TextureRegion maskFullSprite;
    private TextureRegion maskEmptySprite;

    private Animation<TextureRegion> soulVesselIntroAnim;
    private TextureRegion soulVesselFallback;
    private float soulVesselIntroTimer = 0f;
    private boolean soulVesselIntroPlaying = true;

    private Animation<TextureRegion> soulGrowAnim;
    private Animation<TextureRegion> soulIdleAnim;
    private Animation<TextureRegion> soulShrinkAnim;

    private float soulMotionTimer = 0f;
    private float displayedSoulProgress = 0f;
    private int lastRecordedSoul;
    private SoulMotion currentSoulMotion = SoulMotion.IDLE;

    private static final float SOUL_CIRCLE_X_OFFSET = 0.02f;
    private static final float SOUL_CIRCLE_Y_OFFSET = 0.08f;
    private static final float SOUL_CIRCLE_SIZE_SCALE = 0.7f;
    private static final float SOUL_EMPTY_ALPHA = 0.35f;
    private static final float SOUL_CENTERPIECE_SCALE = 0.42f;
    private TextureRegion soulCenterpieceSprite;

    private enum SoulMotion {
        GROW,
        IDLE,
        SHRINK
    }

    private enum MaskState {
        FULL,
        BREAKING,
        EMPTY,
        HEALING
    }

    private final MaskState[] maskStates;
    private final float[] maskTimers;
    private int lastRecordedHealth;

    public HUDScreen(Knight knight, int screenWidth, int screenHeight) {
        this.knight = knight;
        this.hudCamera = new OrthographicCamera();
        this.hudCamera.setToOrtho(false, screenWidth, screenHeight);

        FreeTypeFontGenerator cheatFontGen = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter cheatFontParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        cheatFontParam.size = 20;
        cheatFontParam.color = Color.YELLOW;
        this.cheatStatusFont = cheatFontGen.generateFont(cheatFontParam);
        cheatFontGen.dispose();

        int maxHealth = knight.getMaxHealth();
        this.maskStates = new MaskState[maxHealth];
        this.maskTimers = new float[maxHealth];
        this.lastRecordedHealth = knight.getHealth();
        this.lastRecordedSoul = knight.getSoul();

        for (int i = 0; i < maxHealth; i++) {
            maskStates[i] = (i < lastRecordedHealth) ? MaskState.FULL : MaskState.EMPTY;
            maskTimers[i] = 0f;
        }

        initAnimations();
        displayedSoulProgress = clamp01(knight.getSoul() / SOUL_CAPACITY);
        syncSoulMotion(true);
    }

    private Animation<TextureRegion> loadAnimationFromFolder(String folderName, float frameDuration) {
        if (!View.AnimationLoader.hasFrames(folderName)) {
            return null;
        }
        return View.AnimationLoader.load(folderName, frameDuration, Animation.PlayMode.NORMAL, loadedTextures);
    }

    private Animation<TextureRegion> loadAnimationFromFirstExistingFolder(String[] folderNames, float frameDuration) {
        for (String folderName : folderNames) {
            Animation<TextureRegion> animation = loadAnimationFromFolder(folderName, frameDuration);
            if (animation != null) {
                return animation;
            }
        }
        return null;
    }

    private void initAnimations() {
        maskBreakAnim = loadAnimationFromFolder("ui/MaskBreak", 0.05f);
        maskHealAnim = loadAnimationFromFolder("ui/MaskHeal", 0.05f);

        soulVesselIntroAnim = loadAnimationFromFirstExistingFolder(
            new String[]{
                "ui/SoulVessel/Intro",
                "ui/SoulVessel/intro",
            },
            SOUL_VESSEL_INTRO_DURATION
        );

        soulGrowAnim = loadAnimationFromFirstExistingFolder(
            new String[]{"ui/SoulLiquid/Grow", "ui/SoulLiquid/grow"},
            SOUL_MOTION_ANIM_DURATION
        );

        soulIdleAnim = loadAnimationFromFirstExistingFolder(
            new String[]{"ui/SoulLiquid/Idle", "ui/SoulLiquid/idle"},
            SOUL_MOTION_ANIM_DURATION
        );

        soulShrinkAnim = loadAnimationFromFirstExistingFolder(
            new String[]{"ui/SoulLiquid/Shrink", "ui/SoulLiquid/shrink"},
            SOUL_MOTION_ANIM_DURATION
        );

        Texture fullTex = new Texture("ui/mask_full.png");
        maskFullSprite = new TextureRegion(fullTex);

        Texture emptyTex = new Texture("ui/mask_empty.png");
        maskEmptySprite = new TextureRegion(emptyTex);

        Texture fallbackTex = new Texture("ui/soul_vessel.png");
        soulVesselFallback = new TextureRegion(fallbackTex);
        soulLiquidMask = new Texture(Gdx.files.internal("ui/SoulLiquid/mask_circle.png"));
        soulLiquidMask.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        soulLiquidMask.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        soulMaskShader = SoulMaskShader.create();

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(
            1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        FileHandle centerpieceFile = Gdx.files.internal("ui/SoulLiquid/centerpiece.png");
        if (centerpieceFile.exists()) {
            Texture centerpieceTex = new Texture(centerpieceFile);
            centerpieceTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            soulCenterpieceSprite = new TextureRegion(centerpieceTex);
        }
    }

    public void playVesselIntro() {
        soulVesselIntroTimer = 0f;
        soulVesselIntroPlaying = true;
    }

    private float clamp01(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }

    private float lerp(float from, float to, float alpha) {
        return from + (to - from) * alpha;
    }

    private Animation<TextureRegion> getCurrentSoulMotionAnimation() {
        switch (currentSoulMotion) {
            case GROW:
                if (soulGrowAnim != null) return soulGrowAnim;
                if (soulIdleAnim != null) return soulIdleAnim;
                return soulShrinkAnim;
            case SHRINK:
                if (soulShrinkAnim != null) return soulShrinkAnim;
                if (soulIdleAnim != null) return soulIdleAnim;
                return soulGrowAnim;
            case IDLE:
            default:
                if (soulIdleAnim != null) return soulIdleAnim;
                if (soulGrowAnim != null) return soulGrowAnim;
                return soulShrinkAnim;
        }
    }

    private void syncSoulMotion(boolean forceImmediate) {
        int currentSoul = knight.getSoul();

        if (forceImmediate) {
            currentSoulMotion = SoulMotion.IDLE;
            soulMotionTimer = 0f;
            lastRecordedSoul = currentSoul;
            return;
        }

        if (currentSoul != lastRecordedSoul) {
            currentSoulMotion = currentSoul > lastRecordedSoul ? SoulMotion.GROW : SoulMotion.SHRINK;
            soulMotionTimer = 0f;
            lastRecordedSoul = currentSoul;
        }
    }

    public void update(float delta) {
        int currentHealth = knight.getHealth();
        hudStateTime += delta;

        if (currentHealth < lastRecordedHealth) {
            for (int i = currentHealth; i < lastRecordedHealth; i++) {
                if (i >= 0 && i < maskStates.length && maskStates[i] == MaskState.FULL) {
                    maskStates[i] = MaskState.BREAKING;
                    maskTimers[i] = 0f;
                }
            }
        }

        if (currentHealth > lastRecordedHealth) {
            for (int i = lastRecordedHealth; i < currentHealth; i++) {
                if (i >= 0 && i < maskStates.length) {
                    maskStates[i] = MaskState.HEALING;
                    maskTimers[i] = 0f;
                }
            }
        }

        for (int i = 0; i < maskStates.length; i++) {
            if (maskStates[i] == MaskState.BREAKING) {
                maskTimers[i] += delta;
                if (maskBreakAnim == null || maskBreakAnim.isAnimationFinished(maskTimers[i])) {
                    maskStates[i] = MaskState.EMPTY;
                }
            } else if (maskStates[i] == MaskState.HEALING) {
                maskTimers[i] += delta;
                if ((maskHealAnim == null || maskHealAnim.isAnimationFinished(maskTimers[i])) || knight.isDead()) {
                    maskStates[i] = MaskState.FULL;
                }
            }
        }

        if (soulVesselIntroPlaying) {
            soulVesselIntroTimer += delta;
            if (soulVesselIntroAnim == null || soulVesselIntroAnim.isAnimationFinished(soulVesselIntroTimer)) {
                soulVesselIntroPlaying = false;
            }
        }

        syncSoulMotion(false);

        float targetSoulProgress = clamp01(knight.getSoul() / SOUL_CAPACITY);
        float alpha = Math.min(1f, SOUL_FILL_LERP_SPEED * delta);
        displayedSoulProgress = lerp(displayedSoulProgress, targetSoulProgress, alpha);

        Animation<TextureRegion> motionAnim = getCurrentSoulMotionAnimation();
        if (motionAnim != null) {
            soulMotionTimer += delta;
            if (currentSoulMotion != SoulMotion.IDLE && motionAnim.isAnimationFinished(soulMotionTimer)) {
                currentSoulMotion = SoulMotion.IDLE;
                soulMotionTimer = 0f;
            }
        }

        lastRecordedHealth = currentHealth;
    }

    public void render(SpriteBatch batch, float delta) {
        update(delta);

        batch.setProjectionMatrix(hudCamera.combined);

        batch.setShader(null);
        batch.begin();
        drawSoulVessel(batch);
        drawAnimatedMasks(batch);
        batch.end();

        batch.setShader(soulMaskShader);
        batch.begin();
        bindSoulMask();
        drawSoulLiquid(batch);
        batch.end();
        batch.setShader(null);

        batch.begin();
        drawSoulCenterpiece(batch);
        batch.end();
    }

    public void renderCheatStatus(SpriteBatch batch, boolean godMode, boolean noclip, boolean emergencyHeal) {
        if (!godMode && !noclip && !emergencyHeal) {
            return;
        }
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        float y = hudCamera.viewportHeight - 20f;
        if (godMode) {
            cheatStatusFont.draw(batch, "GOD MODE: ON", 20f, y);
            y -= 24f;
        }
        if (noclip) {
            cheatStatusFont.draw(batch, "NOCLIP: ON", 20f, y);
            y -= 24f;
        }
        if (emergencyHeal) {
            cheatStatusFont.draw(batch, "EMERGENCY HEAL: ARMED", 20f, y);
        }
        batch.end();
    }

    private void drawAnimatedMasks(SpriteBatch batch) {
        float startX = 80f;
        float startY = hudCamera.viewportHeight - 180f;
        float padding = 70f;

        for (int i = 0; i < maskStates.length; i++) {
            TextureRegion frame = null;

            switch (maskStates[i]) {
                case FULL:
                    frame = maskFullSprite;
                    break;
                case EMPTY:
                    frame = maskEmptySprite;
                    break;
                case BREAKING:
                    if (maskBreakAnim != null) {
                        frame = maskBreakAnim.getKeyFrame(maskTimers[i], false);
                    }
                    break;
                case HEALING:
                    if (maskHealAnim != null) {
                        frame = maskHealAnim.getKeyFrame(maskTimers[i], false);
                    }
                    break;
            }

            if (frame != null) {
                batch.draw(frame, startX + (i * padding), startY, 160f, 160f);
            }
        }
    }

    private void drawSoulVessel(SpriteBatch batch) {
        float vesselX = 18f;
        float vesselY = hudCamera.viewportHeight - 260f;
        float vesselWidth = 273f;
        float vesselHeight = 215f;
        TextureRegion vesselFrame;
        if (soulVesselIntroAnim != null) {
            vesselFrame = soulVesselIntroAnim.getKeyFrame(soulVesselIntroTimer, false);
        } else {
            vesselFrame = soulVesselFallback;
        }

        if (vesselFrame != null) {
            batch.draw(vesselFrame, vesselX, vesselY, vesselWidth, vesselHeight);
        }
    }

    private void bindSoulMask() {
        if (soulLiquidMask == null || soulMaskShader == null) {
            return;
        }

        soulLiquidMask.bind(1);
        soulMaskShader.setUniformi("u_mask", 1);
        Gdx.gl.glActiveTexture(com.badlogic.gdx.graphics.GL20.GL_TEXTURE0);
    }

    private com.badlogic.gdx.math.Rectangle getSoulCircleRect() {
        float vesselX = 14f;
        float vesselY = hudCamera.viewportHeight - 260f;
        float vesselSize = 220f;

        float circleSize = vesselSize * SOUL_CIRCLE_SIZE_SCALE;
        float circleX = vesselX + vesselSize * SOUL_CIRCLE_X_OFFSET;
        float circleY = vesselY + vesselSize * SOUL_CIRCLE_Y_OFFSET;
        return new com.badlogic.gdx.math.Rectangle(circleX, circleY, circleSize, circleSize);
    }

    private void drawSoulLiquid(SpriteBatch batch) {
        com.badlogic.gdx.math.Rectangle circle = getSoulCircleRect();

        soulMaskShader.setUniformf("u_maskRect", circle.x, circle.y, circle.width, circle.height);

        batch.setColor(1f, 1f, 1f, SOUL_EMPTY_ALPHA);
        batch.draw(whitePixel, circle.x, circle.y, circle.width, circle.height);
        batch.setColor(1f, 1f, 1f, 1f);

        Animation<TextureRegion> motionAnim = getCurrentSoulMotionAnimation();
        if (motionAnim == null) {
            return;
        }

        TextureRegion liquidFrame = motionAnim.getKeyFrame(soulMotionTimer, currentSoulMotion == SoulMotion.IDLE);
        if (liquidFrame == null) {
            return;
        }

        float liquidHeight = circle.height * displayedSoulProgress;
        if (liquidHeight < 1f) {
            return;
        }

        batch.draw(liquidFrame, circle.x, circle.y, circle.width, liquidHeight);
    }

    private void drawSoulCenterpiece(SpriteBatch batch) {
        if (soulCenterpieceSprite == null) {
            return;
        }
        com.badlogic.gdx.math.Rectangle circle = getSoulCircleRect();
        float size = circle.width * SOUL_CENTERPIECE_SCALE;
        float x = circle.x + (circle.width - size) / 2f;
        float y = circle.y + (circle.height - size) / 2f;
        batch.draw(soulCenterpieceSprite, x, y, size, size);
    }

    public void resize(int width, int height) {
        hudCamera.setToOrtho(false, width, height);
    }

    public void dispose() {
        if (cheatStatusFont != null) {
            cheatStatusFont.dispose();
        }
        if (soulMaskShader != null) {
            soulMaskShader.dispose();
        }
        if (maskFullSprite != null && maskFullSprite.getTexture() != null) {
            maskFullSprite.getTexture().dispose();
        }
        if (maskEmptySprite != null && maskEmptySprite.getTexture() != null) {
            maskEmptySprite.getTexture().dispose();
        }
        if (soulVesselFallback != null && soulVesselFallback.getTexture() != null) {
            soulVesselFallback.getTexture().dispose();
        }
        if (soulLiquidMask != null) {
            soulLiquidMask.dispose();
        }
        if (whitePixel != null) {
            whitePixel.dispose();
        }
        if (soulCenterpieceSprite != null && soulCenterpieceSprite.getTexture() != null) {
            soulCenterpieceSprite.getTexture().dispose();
        }
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
        loadedTextures.clear();
    }
}
