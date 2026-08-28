package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class JumpWaveAnimation {
    private final Animation<TextureRegion> jumpWave;
    private final List<Texture> loadedTextures = new ArrayList<>();

    public JumpWaveAnimation() {
        this("Knight/JumpWave");
    }

    public JumpWaveAnimation(String folder) {
        jumpWave = loadFirstAvailableAnimation(0.06f,
            folder,
            "FalseKnight/JumpWaveEffect",
            "FalseKnight/JumpShockwave",
            "FalseKnight/Wave"
        );
    }

    private Animation<TextureRegion> loadFirstAvailableAnimation(float frameDuration, String... folders) {
        for (String folder : folders) {
            Animation<TextureRegion> candidate = loadAnimation(folder, frameDuration);
            if (candidate.getKeyFrames().length > 0) {
                return candidate;
            }
        }
        return loadAnimation(folders.length > 0 ? folders[0] : "", frameDuration);
    }

    private Animation<TextureRegion> loadAnimation(String folder, float frameDuration) {
        return AnimationLoader.load(
            folder,
            frameDuration,
            Animation.PlayMode.NORMAL,
            loadedTextures
        );
    }

    public Animation<TextureRegion> getJumpWave() {
        return jumpWave;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
        loadedTextures.clear();
    }
}
