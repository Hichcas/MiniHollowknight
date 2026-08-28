package Model;

import Model.Enums.WingedSentryState;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;

public class WingedSentry {
    private final Body body;
    private final Rectangle bounds = new Rectangle();

    private WingedSentryState state = WingedSentryState.Idle;

    private int health = 3;
    private boolean faceRight = true;
    private float stateTimer = 0f;
    private float stateTime = 0f;
    private final float detectionRange = 11f;
    private final float chargeAnticDuration = 0.35f;
    private final float chargeDuration = 1.15f;
    private final float chargeSpeed = 8.5f;
    private final float deathDuration = 0.6f;
    private final float chaseSpeedX = 3.2f;
    private final float chaseSpeedY = 2.0f;
    private final float alignSpeed = 4.5f;
    private final float attackCooldown = 0.8f;
    private float cooldownTimer = 0f;
    private final float attackYOffset = 0.5f;
    private float targetY = 0f;
    private float chargeDirX = 1f;
    private boolean removePending = false;
    private float hurtCooldown = 0f;
    private final float recoverDuration = 1f;

    private static final float DRAW_WIDTH = 5f;
    private static final float DRAW_HEIGHT = 4f;

    public WingedSentry(Body body) {
        this.body = body;
        this.body.setGravityScale(0f);
    }

    public Rectangle getBounds() {
        bounds.set(body.getPosition().x - 1.75f, body.getPosition().y - 1.4f, 3.5f, 2.8f);
        return bounds;
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

    public boolean isRemoved() {
        return removePending;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean isAlive() {
        return !removePending;
    }

    public WingedSentryState getState() {
        return state;
    }

    public boolean isFacingRight() {
        return faceRight;
    }

    public boolean isDead() {
        return state == WingedSentryState.DeathAir || state == WingedSentryState.DeathLand;
    }

    public void setState(WingedSentryState state) {
        this.state = state;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isFaceRight() {
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

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public float getDetectionRange() {
        return detectionRange;
    }

    public float getChargeAnticDuration() {
        return chargeAnticDuration;
    }

    public float getChargeDuration() {
        return chargeDuration;
    }

    public float getChargeSpeed() {
        return chargeSpeed;
    }

    public float getDeathDuration() {
        return deathDuration;
    }

    public float getChaseSpeedX() {
        return chaseSpeedX;
    }

    public float getChaseSpeedY() {
        return chaseSpeedY;
    }

    public float getAlignSpeed() {
        return alignSpeed;
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }

    public float getCooldownTimer() {
        return cooldownTimer;
    }

    public void setCooldownTimer(float cooldownTimer) {
        this.cooldownTimer = cooldownTimer;
    }

    public float getAttackYOffset() {
        return attackYOffset;
    }

    public float getTargetY() {
        return targetY;
    }

    public void setTargetY(float targetY) {
        this.targetY = targetY;
    }

    public float getChargeDirX() {
        return chargeDirX;
    }

    public void setChargeDirX(float chargeDirX) {
        this.chargeDirX = chargeDirX;
    }

    public boolean isRemovePending() {
        return removePending;
    }

    public void setRemovePending(boolean removePending) {
        this.removePending = removePending;
    }

    public float getHurtCooldown() {
        return hurtCooldown;
    }

    public void setHurtCooldown(float hurtCooldown) {
        this.hurtCooldown = hurtCooldown;
    }

    public float getRecoverDuration() {
        return recoverDuration;
    }
}
