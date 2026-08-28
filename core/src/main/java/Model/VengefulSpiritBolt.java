package Model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;

import java.util.HashMap;
import java.util.Map;

public class VengefulSpiritBolt {

    private static final float SPEED = 9f;
    private static final float LIFE_TIME = 1.2f;
    private static final float WIDTH = 5f;
    private static final float HEIGHT = 2.5f;

    private static final int MAX_HITS_PER_ENEMY = 3;
    private static final float HIT_INTERVAL = 0.2f;

    private final Animation<TextureRegion> animation;
    private final Rectangle bounds;
    private final boolean facingRight;
    private final float damage;
    private final Map<Object, Integer> hitCounts = new HashMap<>();
    private final Map<Object, Float> hitCooldowns = new HashMap<>();

    private float stateTime = 0f;
    private boolean removePending = false;

    public VengefulSpiritBolt(Animation<TextureRegion> animation, float x, float y, boolean facingRight, float damage) {
        this.animation = animation;
        this.facingRight = facingRight;
        this.damage = damage;
        this.bounds = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public void update(float delta, World world) {
        stateTime += delta;
        bounds.x += (facingRight ? SPEED : -SPEED) * delta;

        if (!hitCooldowns.isEmpty()) {
            for (Map.Entry<Object, Float> entry : hitCooldowns.entrySet()) {
                entry.setValue(Math.max(0f, entry.getValue() - delta));
            }
        }

        if (stateTime >= LIFE_TIME) {
            removePending = true;
            return;
        }

        if (world != null && hitsSolidGeometry(world)) {
            removePending = true;
        }
    }

    private boolean hitsSolidGeometry(World world) {
        final boolean[] hit = {false};
        world.QueryAABB(fixture -> {
            Object data = fixture.getUserData();
            if ("GROUND".equals(data) || "DEADLY".equals(data)) {
                hit[0] = true;
                return false;
            }
            return true;
        }, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
        return hit[0];
    }

    public void render(Batch batch) {
        if (animation == null || animation.getKeyFrames().length == 0) {
            return;
        }
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        if (frame == null) {
            return;
        }

        if (facingRight) {
            batch.draw(frame, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.draw(frame, bounds.x + bounds.width, bounds.y, -bounds.width, bounds.height);
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isRemovePending() {
        return removePending;
    }

    public float getDamage() {
        return damage;
    }

    public boolean hasHit(Object enemyKey) {
        Integer count = hitCounts.get(enemyKey);
        if (count != null && count >= MAX_HITS_PER_ENEMY) {
            return true;
        }
        Float cooldown = hitCooldowns.get(enemyKey);
        return cooldown != null && cooldown > 0f;
    }

    public void markHit(Object enemyKey) {
        hitCounts.put(enemyKey, hitCounts.getOrDefault(enemyKey, 0) + 1);
        hitCooldowns.put(enemyKey, HIT_INTERVAL);
    }
}
