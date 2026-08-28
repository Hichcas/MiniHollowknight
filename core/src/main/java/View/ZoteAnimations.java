package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

public class ZoteAnimations {

    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>();
    private final List<Texture> loadedTextures = new ArrayList<>();

    private Animation<TextureRegion> idle;
    private Animation<TextureRegion> talk;
    private Animation<TextureRegion> attack;
    private Animation<TextureRegion> fall;
    private Animation<TextureRegion> getUp;
    private Animation<TextureRegion> roll;
    private Animation<TextureRegion> turn;

    public ZoteAnimations(float frameDuration) {
        idle = loadAnimation("Zote/Idle", frameDuration, Animation.PlayMode.LOOP);
        talk = loadAnimation("Zote/Talk", frameDuration, Animation.PlayMode.LOOP);
        attack = loadAnimation("Zote/Attack", frameDuration, Animation.PlayMode.LOOP);
        fall = loadAnimation("Zote/Fall", frameDuration, Animation.PlayMode.NORMAL);
        getUp = loadAnimation("Zote/GetUp", frameDuration, Animation.PlayMode.NORMAL);
        roll = loadAnimation("Zote/Roll", frameDuration, Animation.PlayMode.NORMAL);
        turn = loadAnimation("Zote/Turn", frameDuration, Animation.PlayMode.NORMAL);
    }

    public ZoteAnimations() {
        this(0.1f);
    }

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration, Animation.PlayMode mode) {
        Animation<TextureRegion> animation = AnimationLoader.load(
            folderName,
            frameDuration,
            mode,
            loadedTextures
        );
        animations.put(folderName, animation);
        return animation;
    }

    public Animation<TextureRegion> getIdle() {
        return idle;
    }

    public Animation<TextureRegion> getTalk() {
        return talk;
    }

    public Animation<TextureRegion> getAttack() {
        return attack;
    }

    public Animation<TextureRegion> getFall() {
        return fall;
    }

    public Animation<TextureRegion> getGetUp() {
        return getUp;
    }

    public Animation<TextureRegion> getRoll() {
        return roll;
    }

    public Animation<TextureRegion> getTurn() {
        return turn;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
        loadedTextures.clear();
        animations.clear();
    }
}
