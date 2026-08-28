package View;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class RainEffect {

    private static class Drop {
        float x, y, speed;
        float animOffset;
    }

    private final Rectangle zone;
    private final Drop[] drops;
    private final Animation<TextureRegion> animation;
    private final float dropWidth;
    private final float dropHeight;
    private float time = 0f;

    public RainEffect(Rectangle zone, int dropCount, Animation<TextureRegion> animation,
                       float dropWidth, float dropHeight) {
        this.zone = zone;
        this.animation = animation;
        this.dropWidth = dropWidth;
        this.dropHeight = dropHeight;
        this.drops = new Drop[dropCount];
        for (int i = 0; i < dropCount; i++) {
            drops[i] = new Drop();
            resetDrop(drops[i], true);
        }
    }

    private void resetDrop(Drop d, boolean randomStartY) {
        d.x = zone.x + MathUtils.random(zone.width);
        d.y = randomStartY
            ? zone.y + MathUtils.random(zone.height)
            : zone.y + zone.height + MathUtils.random(1f);
        d.speed = MathUtils.random(8f, 13f);
        d.animOffset = MathUtils.random(0f, 1f);
    }

    public void update(float delta) {
        time += delta;
        for (Drop d : drops) {
            d.y -= d.speed * delta;
            d.x -= d.speed * 0.18f * delta;
            if (d.y < zone.y || d.x < zone.x - zone.width) {
                resetDrop(d, false);
            }
        }
    }

    public void render(Batch batch) {
        if (animation == null) return;
        for (Drop d : drops) {
            TextureRegion frame = animation.getKeyFrame(time + d.animOffset, true);
            batch.draw(frame, d.x, d.y, dropWidth, dropHeight);
        }
    }

    public boolean containsPoint(float x, float y) {
        return zone.contains(x, y);
    }

    public Rectangle getZone() {
        return zone;
    }
}
