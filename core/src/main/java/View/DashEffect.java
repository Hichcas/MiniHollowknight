package View;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class DashEffect {
    private final Animation<TextureRegion> animation;

    private final float x;
    private final float y;
    private final boolean facingRight;

    private final float width;
    private final float height;
    private final Color tint;

    private float timer = 0f;
    private boolean finished = false;

    public DashEffect(Animation<TextureRegion> animation, float x, float y, boolean facingRight, float width, float height) {
        this(animation, x, y, facingRight, width, height, Color.WHITE);
    }

    public DashEffect(Animation<TextureRegion> animation, float x, float y, boolean facingRight, float width, float height, Color tint) {
        this.animation = animation;
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.width = width;
        this.height = height;
        this.tint = tint == null ? Color.WHITE : tint;
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
        if (!facingRight) {
            frame.flip(true, false);
        }

        Color previous = batch.getColor().cpy();
        batch.setColor(tint);
        batch.draw(frame, x, y, width, height);
        batch.setColor(previous);
    }

    public boolean isFinished() {
        return finished;
    }
}
