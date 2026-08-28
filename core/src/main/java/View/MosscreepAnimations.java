package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class MosscreepAnimations {
    private final Animation<TextureRegion> walk;
    private final Animation<TextureRegion> turn;
    private final Animation<TextureRegion> death;

    public MosscreepAnimations() {
        walk = loadAnimation("Mosscreep/Walk", 0.5f, true);
        turn = loadAnimation("Mosscreep/Turn", 0.3f, false);
        death = loadAnimation("Mosscreep/Death", 0.5f, false);
    }

    private Animation<TextureRegion> loadAnimation(String folder, float frameDuration, boolean loop) {
        Array<TextureRegion> frames = new Array<>();
        int index = 0;

        while (true) {
            String path;
            if (folder.equals("Mosscreep/Walk")) {
                path = String.format("%s/Walk_%03d.png", folder, index);
            } else if (folder.equals("Mosscreep/Turn")) {
                path = String.format("%s/Turn_%03d.png", folder, index);
            } else {
                path = String.format("%s/Death Air_%03d.png", folder, index);
            }

            if (!Gdx.files.internal(path).exists()) {
                break;
            }
            frames.add(new TextureRegion(new Texture(Gdx.files.internal(path))));
            index++;
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        return animation;
    }

    public Animation<TextureRegion> getWalk() {
        return walk;
    }

    public Animation<TextureRegion> getTurn() {
        return turn;
    }

    public Animation<TextureRegion> getDeath() {
        return death;
    }
}
