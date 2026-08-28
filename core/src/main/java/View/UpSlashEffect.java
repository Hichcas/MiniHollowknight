package View;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class UpSlashEffect {
    private final Animation<TextureRegion> animation;
    private final Rectangle hitBox = new Rectangle();

    private final float x;
    private final float y;

    private float timer = 0f;
    private boolean finished = false;

    private final float width;
    private final float height;

    public UpSlashEffect(Animation<TextureRegion> animation, float x, float y,
                         float width, float height) {
        this.animation = animation;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void update(float delta) {
        if (finished) {
            return;
        }

        timer += delta;

        if (timer >= animation.getAnimationDuration()) {
            finished = true;
            return;
        }

        float hitX = x - width / 2f;
        float hitY = y;
        hitBox.set(hitX, hitY, width, height);
    }

    public void render(Batch batch) {
        if (finished) {
            return;
        }

        TextureRegion frame = animation.getKeyFrame(timer, false);

        float originX = width / 2f;
        float originY = height / 2f;

        batch.draw(
            frame,
            x - width / 2f, y,
            originX, originY,
            width, height,
            1f, 1f,
            90f
        );
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public boolean isFinished() {
        return finished;
    }
}
