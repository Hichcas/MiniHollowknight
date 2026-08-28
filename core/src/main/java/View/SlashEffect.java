package View;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class SlashEffect {
    private final Animation<TextureRegion> animation;
    private final Rectangle hitBox = new Rectangle();

    private final float x;
    private final float y;
    private final boolean rightDirection;

    private float timer = 0f;
    private boolean finished = false;

    private final float width;
    private final float height;
    private final float offsetX;
    private final float offsetY;

    public SlashEffect(Animation<TextureRegion> animation, float x, float y, boolean rightDirection,
                       float width, float height, float offsetX, float offsetY) {
        this.animation = animation;
        this.x = x;
        this.y = y;
        this.rightDirection = rightDirection;
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
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

        float hitX = rightDirection ? x + offsetX : x - offsetX - width;
        float hitY = y + offsetY;
        hitBox.set(hitX, hitY, width, height);
    }

    public void render(Batch batch) {
        if (finished) {
            return;
        }

        TextureRegion frame = new TextureRegion(animation.getKeyFrame(timer, false));
        if (rightDirection) {
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
}
