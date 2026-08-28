package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class CrackedWallAnimations {

    private final ArrayList<Texture> loadedTextures = new ArrayList<>();

    private TextureRegion crackStage0;
    private TextureRegion crackStage1;
    private TextureRegion crackStage2;

    private final Animation<TextureRegion> dustBurst;
    private final Animation<TextureRegion> stoneBreak;
    private final Animation<TextureRegion> charmIdle;

    private Sound hitSound;
    private Sound breakSound;

    public CrackedWallAnimations() {
        crackStage0 = loadOptionalTexture("walls/cracked_wall_stage0.png");
        crackStage1 = loadOptionalTexture("walls/cracked_wall_stage1.png");
        crackStage2 = loadOptionalTexture("walls/cracked_wall_stage2.png");

        dustBurst = loadAnimation("vfx/WallDust", 0.06f);
        stoneBreak = loadAnimation("vfx/StoneBreak", 0.08f);

        charmIdle = loadAnimation("vfx/CharmIdle", 0.1f);
        charmIdle.setPlayMode(Animation.PlayMode.LOOP);

        hitSound = loadOptionalSound("sfx/wall/wall_hit.wav");
        breakSound = loadOptionalSound("sfx/wall/wall_break.wav");
    }

    private TextureRegion loadOptionalTexture(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            return null;
        }
        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        loadedTextures.add(texture);
        return new TextureRegion(texture);
    }

    private Sound loadOptionalSound(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            return null;
        }
        return Gdx.audio.newSound(file);
    }

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration) {
        return AnimationLoader.load(
            folderName,
            frameDuration,
            Animation.PlayMode.NORMAL,
            loadedTextures
        );
    }

    private Animation<TextureRegion> createFallbackAnimation(float frameDuration) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        loadedTextures.add(texture);

        Animation<TextureRegion> fallback =
            new Animation<>(frameDuration, new TextureRegion(texture));
        fallback.setPlayMode(Animation.PlayMode.LOOP);
        return fallback;
    }

    public TextureRegion getCrackStage0() {
        return crackStage0;
    }

    public TextureRegion getCrackStage1() {
        return crackStage1;
    }

    public TextureRegion getCrackStage2() {
        return crackStage2;
    }

    public Animation<TextureRegion> getDustBurst() {
        return dustBurst;
    }

    public Animation<TextureRegion> getStoneBreak() {
        return stoneBreak;
    }

    public Animation<TextureRegion> getCharmIdle() {
        return charmIdle;
    }

    public void playHitSound() {
        if (hitSound != null) {
            hitSound.play(0.7f);
        }
    }

    public void playBreakSound() {
        if (breakSound != null) {
            breakSound.play(0.9f);
        }
    }

    public void dispose() {
        for (Texture t : loadedTextures) {
            t.dispose();
        }
        loadedTextures.clear();
        if (hitSound != null) hitSound.dispose();
        if (breakSound != null) breakSound.dispose();
    }
}
