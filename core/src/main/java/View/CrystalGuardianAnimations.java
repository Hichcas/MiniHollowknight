package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class CrystalGuardianAnimations {
    private final Animation<TextureRegion> Idle;
    private final Animation<TextureRegion> Enraged;
    private final Animation<TextureRegion> Run;
    private final Animation<TextureRegion> Shoot;
    private final Animation<TextureRegion> Turn;
    private final Animation<TextureRegion> DeathLand;
    private final Animation<TextureRegion> DeathAir;
    private final ArrayList<Texture> loadedTextures = new ArrayList<>();

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration, boolean loop) {
        return AnimationLoader.load(
            folderName,
            frameDuration,
            loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL,
            loadedTextures
        );
    }

    public CrystalGuardianAnimations() {
        Idle = loadAnimation("CrystalGuardian/Idle", 0.08f, true);
        Enraged = loadAnimation("CrystalGuardian/Enraged", 0.08f, true);
        Run = loadAnimation("CrystalGuardian/Run", 0.08f, true);
        Shoot = loadAnimation("CrystalGuardian/Shoot", 0.08f, true);
        Turn = loadAnimation("CrystalGuardian/Turn", 0.08f, false);
        DeathAir = loadAnimation("CrystalGuardian/DeathAir", 0.08f, false);
        DeathLand = loadAnimation("CrystalGuardian/DeathLand", 0.08f, false);
    }

    public Animation<TextureRegion> getIdle() {
        return Idle;
    }

    public Animation<TextureRegion> getEnraged() {
        return Enraged;
    }

    public Animation<TextureRegion> getRun() {
        return Run;
    }

    public Animation<TextureRegion> getShoot() {
        return Shoot;
    }

    public Animation<TextureRegion> getTurn() {
        return Turn;
    }

    public Animation<TextureRegion> getDeathLand() {
        return DeathLand;
    }

    public Animation<TextureRegion> getDeathAir() {
        return DeathAir;
    }

    public ArrayList<Texture> getLoadedTextures() {
        return loadedTextures;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
        loadedTextures.clear();
    }
}
