package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class BloodSplashAnimation {
    private final Animation<TextureRegion> bloodSplash;
    private final ArrayList<Texture> loadedTextures = new ArrayList<>();

    public BloodSplashAnimation() {
        bloodSplash = loadAnimation("vfx/BloodSplash", 0.09f, false);
    }

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration, boolean loop) {
        return AnimationLoader.load(
            folderName,
            frameDuration,
            loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL,
            loadedTextures
        );
    }

    public Animation<TextureRegion> getBloodSplash() {
        return bloodSplash;
    }
}
