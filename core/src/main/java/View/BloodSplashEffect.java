package View;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BloodSplashEffect {
    private final Animation<TextureRegion> animation;

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final boolean flipX;

    private float timer = 0f;
    private boolean finished = false;

    public BloodSplashEffect(Animation<TextureRegion> animation, float x, float y,
                             float width, float height, boolean flipX) {
        this.animation = animation;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.flipX = flipX;
    }

    public void update(float delta) {
        if (finished) {
            return;
        }
        timer += delta;
        if (timer >= animation.getAnimationDuration()) {
            finished = true;
        }
    }

    public void render(Batch batch) {
        if (finished) {
            return;
        }
        TextureRegion frame = new TextureRegion(animation.getKeyFrame(timer, false));
        if (flipX) {
            frame.flip(true, false);
        }
        batch.draw(frame, x, y, width, height);
    }

    public boolean isFinished() {
        return finished;
    }
}
