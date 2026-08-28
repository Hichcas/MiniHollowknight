package View;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ShockwaveEffect {
    private final Animation<TextureRegion> animation;
    private final float x;
    private final float y;
    private final boolean facingRight;
    private final float width = 5.5f;
    private final float height = 1.8f;

    private float timer = 0f;
    private boolean finished = false;

    public ShockwaveEffect(Animation<TextureRegion> animation, float x, float y, boolean facingRight) {
        this.animation = animation;
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
    }

    public void update(float delta) {
        if (finished) return;
        timer += delta;
        if (timer >= animation.getAnimationDuration()) {
            finished = true;
        }
    }

    public void render(Batch batch) {
        if (finished) return;

        TextureRegion frame = animation.getKeyFrame(timer, false);
        if (!facingRight) {
            frame = new TextureRegion(frame);
            frame.flip(true, false);
        }

        batch.draw(frame, x - width / 2, y, width, height);
    }

    public boolean isFinished() {
        return finished;
    }

    public float getX() {
        return x;
    }
}
