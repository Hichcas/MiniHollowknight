package View;

import Model.Enums.KnightState;
import Model.Knight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class KnightView {
    private KnightState previousState = KnightState.IDLE;
    private final Knight knight;
    private final KnightAnimations animations;

    private float stateTime = 0f;

    public KnightView(Knight knight,
                      KnightAnimations animations) {
        this.knight = knight;
        this.animations = animations;
    }

    public void render(Batch batch, float delta) {
        KnightState currentState = knight.getState();

        if (currentState != previousState) {
            stateTime = 0f;
            previousState = currentState;
        }

        stateTime += delta;

        TextureRegion currentFrame = getCurrentFrame();
        if (currentFrame == null) {
            return;
        }

        boolean faceLeft = knight.getFacing() > 0;
        if (currentFrame.isFlipX() != faceLeft) {
            currentFrame.flip(true, false);
        }

        float width = knight.getKnightWidth();
        float height = knight.getKnightHeight();

        float x = knight.getBody().getPosition().x - width / 2f;
        float y = knight.getBody().getPosition().y - height / 2f + 1f;

        Color old = batch.getColor().cpy();

        if (knight.isInvincible() && !knight.isDead()) {
            float blink = 0.35f + 0.65f * Math.abs(MathUtils.sin(stateTime * 28f));
            batch.setColor(1f, 1f, 1f, blink);
        }

        batch.draw(currentFrame, x, y, width, height);
        batch.setColor(old);
    }

    private TextureRegion getCurrentFrame() {
        KnightState state = knight.getState();

        switch (state) {
            case RUN:
                return getFrame(animations.getRun(), true);

            case JUMP:
                return getFrame(animations.getAirBorne(), true);

            case DOUBLEJUMP:
                return getFrame(animations.getDoubleJump(), true);

            case FALL:
                return getFrame(animations.getFall(), true);

            case DASH:
                return getFrame(animations.getDash(), true);

            case ATTACK:
                return getFrame(animations.getSlash(), false);

            case WALLSLIDE:
                return getFrame(animations.getWallSlide(), true);

            case WALLJUMP:
                return getFrame(animations.getWallJump(), false);

            case CAST:
                return getFrame(animations.getFocus(), true);

            case HIT:
                return getFrame(animations.getIdleHurt(), true);

            case DEAD:
                return getFrame(animations.getDeath(), false);
            case LOOKUP:
                return getFrame(animations.getLookUp(), true);

            case LOOKDOWN:
                return getFrame(animations.getLookDown(), true);

            case UPSLASH:
                return getFrame(animations.getUpSlash(), false);

            case DOWNSLASH:
                return getFrame(animations.getDownSlash(), false);

            case POGO:
                return getFrame(animations.getPogo(), false);

            case FOCUSSTART:
                return getFrame(animations.getFocusStart(), false);

            case FOCUS:
                return getFrame(animations.getFocus(), true);

            case FOCUSEND:
                return getFrame(animations.getFocusEnd(), false);

            case FOCUSGET:
                return getFrame(animations.getFocusGet(), false);

            case ITEMGET:
                return getFrame(animations.getItemGet(), false);

            case VENGEFULSPIRIT:
                return getFrame(animations.getFireBallCast(), false);

            case HOWLINGWRAITHS:
                return getFrame(animations.getScream(), false);
            case IDLE:
            default:
                return getFrame(animations.getIdle(), true);
        }
    }

    private TextureRegion getFrame(Animation<TextureRegion> animation, boolean loop) {
        if (animation == null) {
            return null;
        }

        if (animation.getKeyFrames().length == 0) {
            return null;
        }

        return animation.getKeyFrame(stateTime, loop);
    }

    public void resetAnimationTime() {
        stateTime = 0f;
        previousState = knight.getState();
    }
}
