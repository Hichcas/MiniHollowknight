package Model;

import Model.Enums.HuskHornheadState;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;

public class HuskHornhead {
    private final Body body;
    private final Rectangle bounds = new Rectangle();
    private final Rectangle sightBounds = new Rectangle();
    private HuskHornheadState state = HuskHornheadState.Walk;
    private int health = 3;
    private boolean faceRight = true;
    private float stateTimer = 0f;
    private float leftBound;
    private float rightBound;
    private float cooldownTimer = 0f;
    private float hurtCooldown = 0f;
    private float chargeDir = 1f;
    private boolean removePending = false;
    private final float walkSpeed = 1.6f;
    private final float chargeSpeed = 7.5f;
    private final float anticipateDuration = 0.20f;
    private final float chargeDuration = 0.85f;
    private final float recoverDuration = 0.35f;
    private final float attackCooldown = 0.85f;
    private final float deathDuration = 0.55f;
    private static final float DRAW_WIDTH = 3f;
    private static final float DRAW_HEIGHT = 1.5f;
    private boolean isTurned = false;
    private final float spawnY;

    public HuskHornhead(Body body) {
        this.body = body;
        this.body.setGravityScale(1f);
        this.spawnY = body.getPosition().y;
    }

    public Rectangle getBounds() {
        bounds.set(body.getPosition().x - 0.55f, body.getPosition().y - 0.55f, 1.1f, 1.1f);
        return bounds;
    }

    public Rectangle getSightBounds() {
        if (faceRight) {
            sightBounds.set(body.getPosition().x + 0.15f, body.getPosition().y - 0.45f, 8.0f, 1.2f);
        } else {
            sightBounds.set(body.getPosition().x - 8.15f, body.getPosition().y - 0.45f, 8.0f, 1.2f);
        }
        return sightBounds;
    }

    public float getDrawX() {
        return body.getPosition().x - DRAW_WIDTH / 2f;
    }

    public float getDrawY() {
        return body.getPosition().y - DRAW_HEIGHT / 2f;
    }

    public float getDrawWidth() {
        return DRAW_WIDTH;
    }

    public float getDrawHeight() {
        return DRAW_HEIGHT;
    }

    public Body getBody() {
        return body;
    }

    public HuskHornheadState getState() {
        return state;
    }

    public void setState(HuskHornheadState state) {
        this.state = state;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isFacingRight() {
        return faceRight;
    }

    public void setFaceRight(boolean faceRight) {
        this.faceRight = faceRight;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public void setStateTimer(float stateTimer) {
        this.stateTimer = stateTimer;
    }

    public float getLeftBound() {
        return leftBound;
    }

    public void setLeftBound(float leftBound) {
        this.leftBound = leftBound;
    }

    public float getRightBound() {
        return rightBound;
    }

    public void setRightBound(float rightBound) {
        this.rightBound = rightBound;
    }

    public float getLeftrestriction() {
        return leftBound;
    }

    public void setLeftrestriction(float v) {
        leftBound = v;
    }

    public float getRightrestriction() {
        return rightBound;
    }

    public void setRightrestriction(float v) {
        rightBound = v;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public float getChargeSpeed() {
        return chargeSpeed;
    }

    public float getAnticipateDuration() {
        return anticipateDuration;
    }

    public float getChargeDuration() {
        return chargeDuration;
    }

    public float getRecoverDuration() {
        return recoverDuration;
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }

    public float getDeathDuration() {
        return deathDuration;
    }

    public float getCooldownTimer() {
        return cooldownTimer;
    }

    public void setCooldownTimer(float cooldownTimer) {
        this.cooldownTimer = cooldownTimer;
    }

    public float getHurtCooldown() {
        return hurtCooldown;
    }

    public void setHurtCooldown(float hurtCooldown) {
        this.hurtCooldown = hurtCooldown;
    }

    public float getChargeDir() {
        return chargeDir;
    }

    public void setChargeDir(float chargeDir) {
        this.chargeDir = chargeDir;
    }

    public boolean isRemovePending() {
        return removePending;
    }

    public void setRemovePending(boolean removePending) {
        this.removePending = removePending;
    }

    public boolean isDead() {
        return state == HuskHornheadState.Death;
    }

    public boolean isAlive() {
        return !removePending;
    }

    public boolean isFaceRight() {
        return faceRight;
    }

    public boolean isTurned() {
        return isTurned;
    }

    public void setTurned(boolean turned) {
        isTurned = turned;
    }

    public float getSpawnY() {
        return spawnY;
    }
}
