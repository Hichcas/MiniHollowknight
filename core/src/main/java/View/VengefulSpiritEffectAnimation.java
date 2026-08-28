package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class VengefulSpiritEffectAnimation {
    private final ArrayList<Texture> loadedTextures = new ArrayList<>();
    private final Animation<TextureRegion> bolt;

    public VengefulSpiritEffectAnimation() {
        bolt = loadAnimation("Knight/VengefulSpirit", 0.05f);
    }

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration) {
        return AnimationLoader.load(
            folderName,
            frameDuration,
            Animation.PlayMode.LOOP,
            loadedTextures
        );
    }

    public Animation<TextureRegion> getBolt() {
        return bolt;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
    }
}
