package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

public class KnightAnimations {

    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>();
    private final List<Texture> loadedTextures = new ArrayList<>();

    private Animation<TextureRegion> idle;
    private Animation<TextureRegion> run;
    private Animation<TextureRegion> fall;
    private Animation<TextureRegion> dash;
    private Animation<TextureRegion> slash;
    private Animation<TextureRegion> upSlash;
    private Animation<TextureRegion> downSlash;
    private Animation<TextureRegion> pogo;
    private Animation<TextureRegion> wallSlide;
    private Animation<TextureRegion> wallJump;
    private Animation<TextureRegion> doubleJump;
    private Animation<TextureRegion> focusStart;
    private Animation<TextureRegion> focus;
    private Animation<TextureRegion> focusEnd;
    private Animation<TextureRegion> focusGet;
    private Animation<TextureRegion> fireBallCast;
    private Animation<TextureRegion> death;
    private Animation<TextureRegion> lookUp;
    private Animation<TextureRegion> lookDown;
    private Animation<TextureRegion> airBorne;
    private Animation<TextureRegion> landing;
    private Animation<TextureRegion> runToIdle;
    private Animation<TextureRegion> idleHurt;
    private Animation<TextureRegion> scream;
    private Animation<TextureRegion> itemGet;

    public KnightAnimations(float frameDuration) {
        idle = loadAnimation("Knight/IDLE", frameDuration);
        run = loadAnimation("Knight/Run", frameDuration);
        fall = loadAnimation("Knight/Fall", frameDuration);
        dash = loadAnimation("Knight/Dash", frameDuration);
        slash = loadAnimation("Knight/Slash", frameDuration);
        upSlash = loadAnimation("Knight/UpSlash", frameDuration);
        downSlash = loadAnimation("Knight/DownSlash", frameDuration);
        pogo = loadAnimation("Knight/Pogo", frameDuration);
        wallSlide = loadAnimation("Knight/WallSlide", frameDuration);
        wallJump = loadAnimation("Knight/WallJump", frameDuration);
        doubleJump = loadAnimation("Knight/DoubleJump", frameDuration);
        focusStart = loadAnimation("Knight/FocusStart", frameDuration);
        focus = loadAnimation("Knight/Focus", frameDuration);
        focusEnd = loadAnimation("Knight/FocusEnd", frameDuration);
        focusGet = loadAnimation("Knight/FocusGet", frameDuration);
        fireBallCast = loadAnimation("Knight/FireBallCast", frameDuration);
        death = loadAnimation("Knight/Death", frameDuration);
        if (death != null) {
            death.setPlayMode(Animation.PlayMode.NORMAL);
        }
        lookUp = loadAnimation("Knight/LookUp", 1f);
        lookDown = loadAnimation("Knight/LookDown", 1f);
        airBorne = loadAnimation("Knight/AirBorne", frameDuration);
        landing = loadAnimation("Knight/Landing", frameDuration);
        runToIdle = loadAnimation("Knight/RunToIDLE", frameDuration);
        idleHurt = loadAnimation("Knight/IDLEHurt", frameDuration);
        scream = loadAnimation("Knight/Scream", frameDuration);
        itemGet = loadAnimation("Knight/ItemGet", frameDuration);
        if (itemGet.getKeyFrames().length == 0) {
            itemGet = focusGet;
        }
        if (itemGet.getKeyFrames().length == 0) {
            itemGet = idle;
        }
    }

    public KnightAnimations() {
        this(0.08f);
    }

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration) {
        Animation<TextureRegion> animation = AnimationLoader.load(
            folderName,
            frameDuration,
            Animation.PlayMode.LOOP,
            loadedTextures
        );
        animations.put(folderName, animation);
        return animation;
    }

    public Animation<TextureRegion> get(String name) {
        return animations.get(name);
    }

    public Animation<TextureRegion> getIdle() {
        return idle;
    }

    public Animation<TextureRegion> getRun() {
        return run;
    }

    public Animation<TextureRegion> getFall() {
        return fall;
    }

    public Animation<TextureRegion> getDash() {
        return dash;
    }

    public Animation<TextureRegion> getSlash() {
        return slash;
    }

    public Animation<TextureRegion> getUpSlash() {
        return upSlash;
    }

    public Animation<TextureRegion> getDownSlash() {
        return downSlash;
    }

    public Animation<TextureRegion> getPogo() {
        return pogo;
    }

    public Animation<TextureRegion> getWallSlide() {
        return wallSlide;
    }

    public Animation<TextureRegion> getWallJump() {
        return wallJump;
    }

    public Animation<TextureRegion> getDoubleJump() {
        return doubleJump;
    }

    public Animation<TextureRegion> getFocusStart() {
        return focusStart;
    }

    public Animation<TextureRegion> getFocus() {
        return focus;
    }

    public Animation<TextureRegion> getFocusEnd() {
        return focusEnd;
    }

    public Animation<TextureRegion> getFocusGet() {
        return focusGet;
    }

    public Animation<TextureRegion> getFireBallCast() {
        return fireBallCast;
    }

    public Animation<TextureRegion> getDeath() {
        return death;
    }

    public Animation<TextureRegion> getLookUp() {
        return lookUp;
    }

    public Animation<TextureRegion> getLookDown() {
        return lookDown;
    }

    public Animation<TextureRegion> getAirBorne() {
        return airBorne;
    }

    public Animation<TextureRegion> getLanding() {
        return landing;
    }

    public Animation<TextureRegion> getRunToIdle() {
        return runToIdle;
    }

    public Animation<TextureRegion> getIdleHurt() {
        return idleHurt;
    }

    public Animation<TextureRegion> getScream() {
        return scream;
    }

    public Animation<TextureRegion> getItemGet() {
        return itemGet;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
        loadedTextures.clear();
        animations.clear();
    }
}
