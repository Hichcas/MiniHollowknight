package Model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class HowlingWraithsBurst {

    private static final float LIFE_TIME = 0.45f;
    private static final float TICK_INTERVAL = 0.15f;
    private static final int MAX_TICKS = 3;
    private static final float WIDTH = 5f;
    private static final float HEIGHT = 3f;

    private final Animation<TextureRegion> animation;
    private final Rectangle bounds;
    private final float damagePerTick;

    private float stateTime = 0f;
    private float tickTimer = 0f;
    private int ticksApplied = 0;
    private boolean removePending = false;

    public HowlingWraithsBurst(Animation<TextureRegion> animation, float centerX, float bottomY, float damagePerTick) {
        this.animation = animation;
        this.damagePerTick = damagePerTick;
        this.bounds = new Rectangle(centerX - WIDTH / 2f, bottomY, WIDTH, HEIGHT);
    }

    public void update(float delta) {
        stateTime += delta;
        tickTimer += delta;

        if (stateTime >= LIFE_TIME) {
            removePending = true;
        }
    }

    public boolean consumeTickIfReady() {
        if (ticksApplied >= MAX_TICKS || tickTimer < TICK_INTERVAL) {
            return false;
        }

        tickTimer = 0f;
        ticksApplied++;
        return true;
    }

    public void render(Batch batch) {
        if (animation == null || animation.getKeyFrames().length == 0) {
            return;
        }
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        if (frame == null) {
            return;
        }
        batch.draw(frame, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isRemovePending() {
        return removePending;
    }

    public float getDamagePerTick() {
        return damagePerTick;
    }
}
