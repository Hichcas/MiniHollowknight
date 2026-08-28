package View;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class JumpWaveEffect {
    private final Animation<TextureRegion> animation;
    private final Rectangle hitBox = new Rectangle();

    private final boolean rightDirection;
    private final boolean moving;
    private final float width;
    private final float height;
    private final float speed;
    private final float maxTravelDistance;
    private final float startX;

    private float x;
    private float y;
    private float timer = 0f;
    private boolean finished = false;
    private static final float FALLBACK_DURATION = 0.38f;

    public JumpWaveEffect(Animation<TextureRegion> animation, float x, float y, boolean rightDirection, float width,
                          float height, float speed) {
        this(animation, x, y, rightDirection, width, height, speed, true, Math.max(4.8f, 6f));
    }

    public JumpWaveEffect(Animation<TextureRegion> animation, float x, float y, boolean rightDirection, float width,
                          float height, float speed, float maxTravelDistance) {
        this(animation, x, y, rightDirection, width, height, speed, true, maxTravelDistance);
    }

    public JumpWaveEffect(Animation<TextureRegion> animation, float x, float y, boolean rightDirection, float width,
                          float height, float speed, boolean moving, float maxTravelDistance) {
        this.animation = animation;
        this.x = x;
        this.y = y;
        this.rightDirection = rightDirection;
        this.moving = moving;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.maxTravelDistance = Math.max(0.5f, maxTravelDistance);
        this.startX = x;
        this.hitBox.set(x, y, width, height);
    }

    public void update(float delta) {
        if (finished) {
            return;
        }

        timer += delta;
        if (moving) {
            x += (rightDirection ? 1f : -1f) * speed * delta;
            if (Math.abs(x - startX) >= maxTravelDistance) {
                finished = true;
            }
        }

        hitBox.set(x, y, width, height);

        if (animation == null || animation.getKeyFrames().length == 0) {
            if (timer >= FALLBACK_DURATION) {
                finished = true;
            }
            return;
        }

        if (timer >= animation.getAnimationDuration()) {
            finished = true;
        }
    }

    public void render(Batch batch) {
        if (finished || animation == null || animation.getKeyFrames().length == 0) {
            return;
        }

        TextureRegion frame = new TextureRegion(animation.getKeyFrame(timer, false));
        if (!rightDirection) {
            frame.flip(true, false);
        }

        batch.draw(frame, x, y, width, height);
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finish() {
        finished = true;
    }
}
