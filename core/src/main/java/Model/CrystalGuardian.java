package Model;

import Model.Enums.CrystalGuardianState;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;

public class CrystalGuardian {
    private final Body body;
    private final Rectangle bounds = new Rectangle();
    private CrystalGuardianState state = CrystalGuardianState.Idle;
    private boolean facingRight = true;
    private boolean isDead = false;
    private boolean removePending = false;
    private float attackCooldown = 0f;
    private int health = 6;
    private float stateTime = 0f;
    private final float attackSpeed = 5f;
    public static final float DRAW_WIDTH = 3f;
    public static final float DRAW_HEIGHT = 1.5f;
    public static final float DETECTION_RANGE = 11f;
    public static final float RUN_SPEED = 6f;
    public static final float TURN_DURATION = 0.45f;
    public static final float SHOOT_DURATION = 0.55f;
    public static final float EVADE_DURATION = 0.40f;
    public static final float RUN_DURATION = 1.2f;
    public static final float ATTACK_COOLDOWN = 1f;
    public static final float DEATH_DURATION = 0.80f;
    public static final float ENEMY_WIDTH = 1.2f;
    public static final float ENEMY_HEIGHT = 2.0f;
    public static final float RUN_HIT_COOLDOWN = 0.35f;
    private boolean shootLaserSpawned = false;
    private float runHitCooldown = 0f;
    private boolean isLaserSpawned = false;
    private float damageCooldown = 0f;

    public CrystalGuardian(Body body) {
        this.body = body;
        this.body.setGravityScale(1f);
    }

    public Rectangle getBounds() {
        bounds.set(body.getPosition().x - 0.55f, body.getPosition().y - 0.55f, 1.1f, 1.1f);
        return bounds;
    }

    public Body getBody() {
        return body;
    }

    public CrystalGuardianState getState() {
        return state;
    }

    public void setState(CrystalGuardianState state) {
        this.state = state;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public boolean isRemovePending() {
        return removePending;
    }

    public void setRemovePending(boolean removePending) {
        this.removePending = removePending;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public boolean isShootLaserSpawned() {
        return shootLaserSpawned;
    }

    public void setShootLaserSpawned(boolean shootLaserSpawned) {
        this.shootLaserSpawned = shootLaserSpawned;
    }

    public float getRunHitCooldown() {
        return runHitCooldown;
    }

    public void setRunHitCooldown(float runHitCooldown) {
        this.runHitCooldown = runHitCooldown;
    }

    public boolean isLaserSpawned() {
        return isLaserSpawned;
    }

    public void setLaserSpawned(boolean laserSpawned) {
        isLaserSpawned = laserSpawned;
    }

    public float getDamageCooldown() {
        return damageCooldown;
    }

    public void setDamageCooldown(float damageCooldown) {
        this.damageCooldown = damageCooldown;
    }
}
