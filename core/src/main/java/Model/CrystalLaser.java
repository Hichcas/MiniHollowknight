package Model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;

public class CrystalLaser {

    private final Texture texture;
    private final Rectangle bounds;
    private final boolean facingRight;
    private float stateTime;
    private float damageCooldown;
    private boolean removePending;

    private static final float LIFE_TIME = 0.75f;
    private static final float DAMAGE_INTERVAL = 0.20f;

    public CrystalLaser(Texture texture, float x, float y, boolean facingRight) {
        this.texture = texture;
        this.facingRight = facingRight;
        float laserLength = 11f;
        float laserHeight = 0.4f;
        bounds = new Rectangle((facingRight ? x : x - laserLength), y, laserLength, laserHeight);
    }

    public void update(float delta) {
        stateTime += delta;
        if (damageCooldown > 0)
            damageCooldown -= delta;
        if (stateTime >= LIFE_TIME)
            removePending = true;
    }

    public void render(Batch batch) {
        if (facingRight)
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        else
            batch.draw(texture, bounds.x + bounds.width, bounds.y, -bounds.width, bounds.height);

    }

    public boolean canDamage() {
        return damageCooldown <= 0f;
    }

    public void resetDamageCooldown() {
        damageCooldown = DAMAGE_INTERVAL;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isRemovePending() {
        return removePending;
    }
}
