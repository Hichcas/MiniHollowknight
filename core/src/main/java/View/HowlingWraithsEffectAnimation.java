package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class HowlingWraithsEffectAnimation {
    private final ArrayList<Texture> loadedTextures = new ArrayList<>();
    private final Animation<TextureRegion> burst;

    public HowlingWraithsEffectAnimation() {
        burst = loadAnimation("Knight/HowlingWraiths", 0.05f);
    }

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration) {
        return AnimationLoader.load(
            folderName,
            frameDuration,
            Animation.PlayMode.NORMAL,
            loadedTextures
        );
    }

    public Animation<TextureRegion> getBurst() {
        return burst;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
    }
}
