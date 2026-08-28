package View;

import Model.Enums.ZoteState;
import Model.Zote;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ZoteView {
    private final Zote zote;
    private final ZoteAnimations animations;
    private ZoteState previousState = ZoteState.Idle;
    private float stateTime = 0f;

    public ZoteView(Zote zote, ZoteAnimations animations) {
        this.zote = zote;
        this.animations = animations;
    }

    public void render(Batch batch, float delta) {
        ZoteState state = zote.getState();
        if (state != previousState) {
            stateTime = 0f;
            previousState = state;
        }
        stateTime += delta;
        zote.setStateTime(stateTime);

        TextureRegion frame = getFrame(state);
        if (frame == null) {
            return;
        }

        boolean faceLeft = zote.isFacingRight();
        if (frame.isFlipX() != faceLeft) {
            frame.flip(true, false);
        }

        batch.draw(frame, zote.getPosition().x - zote.getWidth() / 2f,
            zote.getPosition().y - zote.getHeight() / 2f, zote.getWidth(), zote.getHeight());
    }

    private TextureRegion getFrame(ZoteState state) {
        Animation<TextureRegion> anim;
        boolean loop;

        switch (state) {
            case Talk:
                anim = animations.getTalk();
                loop = true;
                break;
            case Attack:
                anim = animations.getAttack();
                loop = true;
                break;
            case Fall:
                anim = animations.getFall();
                loop = false;
                break;
            case GetUp:
                anim = animations.getGetUp();
                loop = false;
                break;
            case Roll:
                anim = animations.getRoll();
                loop = false;
                break;
            case Turn:
                anim = animations.getTurn();
                loop = false;
                break;
            case Idle:
            default:
                anim = animations.getIdle();
                loop = true;
                break;
        }

        if (anim == null || anim.getKeyFrames().length == 0) {
            return null;
        }
        return anim.getKeyFrame(stateTime, loop);
    }
}
