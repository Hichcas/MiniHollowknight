package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class DashEffectAnimation {
    private final Animation<TextureRegion> dashEffect;

    public DashEffectAnimation() {
        dashEffect = loadAnimation("Knight/DashEffect", 0.04f);
    }

    private Animation<TextureRegion> loadAnimation(String folder, float frameDuration) {
        Array<TextureRegion> frames = new Array<>();
        int index = 0;

        while (true) {
            String path = String.format("%s/Dash Effect_%03d.png", folder, index);
            if (!Gdx.files.internal(path).exists()) {
                break;
            }

            frames.add(new TextureRegion(new Texture(Gdx.files.internal(path))));
            index++;
        }
        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        return animation;
    }

    public Animation<TextureRegion> getDash() {
        return dashEffect;
    }
}
