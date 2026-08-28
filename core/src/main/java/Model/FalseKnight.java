package Model;

import Model.Enums.FalseKnightState;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;

public class FalseKnight {
    private int health = 30;
    private int MAX_HEALTH = 30;
    private final Body body;
    private boolean dead = false;
    private boolean removePending = false;
    private boolean phaseTwo = false;
    private final Rectangle bounds = new Rectangle();
    private FalseKnightState state = FalseKnightState.Idle;
    private float moveSpeed = 3f;
    private float speedMultiplier = 1f;
    private float lastSafeX = 0f;
    private float lastSafeY = 0f;
    private int repeatedMoveCount = 0;
    private FalseKnightState lastState = FalseKnightState.Idle;
    private float deathDuration = 0.6f;
    private float stateTime = 0f;
    private float decisionCooldown = 0f;
    private float attackCooldown = 0f;
    private float stunTimer = 0f;
    private float rageTimer = 0f;
    private final float walkSpeed = 2.6f;
    private final float runSpeed = 5.4f;
    private final float jumpSpeed = 9.0f;
    private final float bodyRollSpeed = 6.2f;
    private final float slamShockwaveSpeed = 4.5f;
    private static final float DRAW_WIDTH = 7.5f;
    private static final float DRAW_HEIGHT = 4.2f;
    private static final float CORPSE_DRAW_WIDTH = DRAW_WIDTH * 0.25f;
    private static final float CORPSE_DRAW_HEIGHT = DRAW_HEIGHT * 0.25f;
    public static final float BODY_WIDTH = 2.4f;
    public static final float BODY_HEIGHT = 3.6f;
    private final float turnDuration = 0.2f;
    private final float attackAnticDuration = 0.4f;
    private final float attackDuration = 0.3f;
    private final float attackRecoverDuration = 0.5f;
    private final float runAnticDuration = 0.3f;
    private final float runDuration = 1.2f;
    private final float jumpAnticDuration = 0.3f;
    private final float jumpDuration = 0.6f;
    private final float landDuration = 0.25f;
    private final float bodyDuration = 1f;
    private final float stunDuration = 2.8f;
    private final float stunRecoverDuration = 0.75f;
    private final float deathFallDuration = 0.5f;
    private final float deathHitDuration = 0.25f;
    private final float deathLandDuration = 1f;
    private boolean isTurned = false;
    private boolean facingRight = true;
    private boolean deathSequenceStarted = false;
    private float BodyHitCooldown = 0f;
    private float hurtCooldown = 0f;
    private int stunCount = 0;

    public FalseKnight(Body body) {
        this.body = body;
        if (this.body != null) {
            this.body.setGravityScale(1f);
            this.body.setFixedRotation(true);
            this.body.setUserData(this);
            this.lastSafeX = this.body.getPosition().x;
            this.lastSafeY = this.body.getPosition().y;
        }
    }

    public Rectangle getBounds() {
        bounds.set(body.getPosition().x - BODY_WIDTH / 2f, body.getPosition().y - BODY_HEIGHT / 2f,
            BODY_WIDTH, BODY_HEIGHT);
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

    public float getCorpseDrawX() {
        return body.getPosition().x - CORPSE_DRAW_WIDTH / 2f;
    }

    public float getCorpseDrawY() {
        return body.getPosition().y - CORPSE_DRAW_HEIGHT / 2f;
    }

    public float getCorpseDrawWidth() {
        return CORPSE_DRAW_WIDTH;
    }

    public float getCorpseDrawHeight() {
        return CORPSE_DRAW_HEIGHT;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(MAX_HEALTH, health));
        if (this.health == 0) {
            dead = true;
        }
    }

    public void damage(int amount) {
        if (dead) {
            return;
        }
        setHealth(health - amount);
    }

    public float getDecisionCooldown() {
        return decisionCooldown;
    }

    public void setDecisionCooldown(float decisionCooldown) {
        this.decisionCooldown = Math.max(0f, decisionCooldown);
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = Math.max(0f, attackCooldown);
    }

    public int getHealth() {
        return health;
    }

    public int getMAX_HEALTH() {
        return MAX_HEALTH;
    }

    public void setMAX_HEALTH(int MAX_HEALTH) {
        this.MAX_HEALTH = MAX_HEALTH;
    }

    public Body getBody() {
        return body;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isRemovePending() {
        return removePending;
    }

    public void setRemovePending(boolean removePending) {
        this.removePending = removePending;
    }

    public boolean isPhaseTwo() {
        return phaseTwo;
    }

    public void setPhaseTwo(boolean phaseTwo) {
        this.phaseTwo = phaseTwo;
    }

    public FalseKnightState getState() {
        return state;
    }

    public void setState(FalseKnightState state) {
        this.state = state;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public float getLastSafeX() {
        return lastSafeX;
    }

    public void setLastSafeX(float lastSafeX) {
        this.lastSafeX = lastSafeX;
    }

    public float getLastSafeY() {
        return lastSafeY;
    }

    public void setLastSafeY(float lastSafeY) {
        this.lastSafeY = lastSafeY;
    }

    public int getRepeatedMoveCount() {
        return repeatedMoveCount;
    }

    public void setRepeatedMoveCount(int repeatedMoveCount) {
        this.repeatedMoveCount = repeatedMoveCount;
    }

    public FalseKnightState getLastState() {
        return lastState;
    }

    public void setLastState(FalseKnightState lastState) {
        this.lastState = lastState;
    }

    public float getDeathDuration() {
        return deathDuration;
    }

    public void setDeathDuration(float deathDuration) {
        this.deathDuration = deathDuration;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public float getStunTimer() {
        return stunTimer;
    }

    public void setStunTimer(float stunTimer) {
        this.stunTimer = stunTimer;
    }

    public float getRageTimer() {
        return rageTimer;
    }

    public void setRageTimer(float rageTimer) {
        this.rageTimer = rageTimer;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public float getRunSpeed() {
        return runSpeed;
    }

    public float getJumpSpeed() {
        return jumpSpeed;
    }

    public float getBodyRollSpeed() {
        return bodyRollSpeed;
    }

    public float getSlamShockwaveSpeed() {
        return slamShockwaveSpeed;
    }

    public float getTurnDuration() {
        return turnDuration;
    }

    public float getAttackAnticDuration() {
        return attackAnticDuration;
    }

    public float getAttackDuration() {
        return attackDuration;
    }

    public float getAttackRecoverDuration() {
        return attackRecoverDuration;
    }

    public float getRunAnticDuration() {
        return runAnticDuration;
    }

    public float getRunDuration() {
        return runDuration;
    }

    public float getJumpAnticDuration() {
        return jumpAnticDuration;
    }

    public float getJumpDuration() {
        return jumpDuration;
    }

    public float getLandDuration() {
        return landDuration;
    }

    public float getBodyDuration() {
        return bodyDuration;
    }

    public float getStunDuration() {
        return stunDuration;
    }

    public float getStunRecoverDuration() {
        return stunRecoverDuration;
    }

    public float getDeathFallDuration() {
        return deathFallDuration;
    }

    public float getDeathHitDuration() {
        return deathHitDuration;
    }

    public float getDeathLandDuration() {
        return deathLandDuration;
    }

    public boolean isTurned() {
        return isTurned;
    }

    public void setTurned(boolean turned) {
        isTurned = turned;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isDeathSequenceStarted() {
        return deathSequenceStarted;
    }

    public void setDeathSequenceStarted(boolean deathSequenceStarted) {
        this.deathSequenceStarted = deathSequenceStarted;
    }

    public float getBodyHitCooldown() {
        return BodyHitCooldown;
    }

    public void setBodyHitCooldown(float bodyHitCooldown) {
        BodyHitCooldown = bodyHitCooldown;
    }

    public float getHurtCooldown() {
        return hurtCooldown;
    }

    public void setHurtCooldown(float hurtCooldown) {
        this.hurtCooldown = Math.max(0f, hurtCooldown);
    }

    public int getStunCount() {
        return stunCount;
    }

    public void setStunCount(int stunCount) {
        this.stunCount = Math.max(0, stunCount);
    }
}
