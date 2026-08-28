package Model;

import Model.Enums.CrackedWallState;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public class CrackedWall {

    private final String id;
    private final Rectangle bounds;
    private final String roomId;
    private final boolean flipX;

    private final int maxHealth = 3;
    private int health = maxHealth;
    private CrackedWallState state = CrackedWallState.INTACT;

    private Body body;
    private Fixture fixture;

    private float hitCooldown = 0f;

    private boolean pendingHitEffect = false;
    private boolean pendingBreakEffect = false;

    public CrackedWall(String id, Rectangle bounds, String roomId, boolean flipX) {
        this.id = id;
        this.bounds = bounds;
        this.roomId = roomId;
        this.flipX = flipX;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public String getId() {
        return id;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public CrackedWallState getState() {
        return state;
    }

    public boolean isDestroyed() {
        return state == CrackedWallState.DESTROYED;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }

    public float getHitCooldown() {
        return hitCooldown;
    }

    public void setHitCooldown(float hitCooldown) {
        this.hitCooldown = hitCooldown;
    }

    public boolean canBeHit() {
        return !isDestroyed() && hitCooldown <= 0f;
    }

    public boolean applyHit() {
        if (isDestroyed()) {
            return false;
        }
        health = Math.max(0, health - 1);
        pendingHitEffect = true;

        if (health == 2) {
            state = CrackedWallState.CRACK_STAGE1;
        } else if (health == 1) {
            state = CrackedWallState.CRACK_STAGE2;
        } else if (health <= 0) {
            state = CrackedWallState.DESTROYED;
            pendingBreakEffect = true;
            return true;
        }
        return false;
    }

    public boolean consumePendingHitEffect() {
        boolean v = pendingHitEffect;
        pendingHitEffect = false;
        return v;
    }

    public boolean consumePendingBreakEffect() {
        boolean v = pendingBreakEffect;
        pendingBreakEffect = false;
        return v;
    }
}
