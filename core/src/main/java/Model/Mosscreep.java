package Model;

import Model.Enums.MosscreepState;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;

public class Mosscreep {
    private final Body body;
    private float leftrestriction;
    private float rightrestriction;
    private int health = 3;
    private float speed = 1.5f;
    private boolean rightDirection = false;
    private boolean isHidden = true;
    private boolean isInvincible = false;
    private float invincibleTimer = 0f;
    private float attackRange = 6f;
    private MosscreepState state = MosscreepState.HIDDEN;
    private float turnTimer = 0f;
    private float stateTime = 0f;
    private final Rectangle bounds = new Rectangle();
    private boolean removePending = false;

    public Mosscreep(Body body, float leftrestriction, float rightrestriction) {
        this.body = body;
        this.leftrestriction = leftrestriction;
        this.rightrestriction = rightrestriction;
    }

    public Rectangle getBounds() {
        bounds.set(body.getPosition().x - 0.35f, body.getPosition().y - 0.45f, 0.7f, 0.9f);
        return bounds;
    }

    public Body getBody() {
        return body;
    }

    public MosscreepState getState() {
        return state;
    }

    public boolean isDead() {
        return state == MosscreepState.DEAD;
    }

    public boolean isFacingRight() {
        return rightDirection;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean shouldRemove() {
        return removePending;
    }

    public float getLeftrestriction() {
        return leftrestriction;
    }

    public void setLeftrestriction(float leftrestriction) {
        this.leftrestriction = leftrestriction;
    }

    public float getRightrestriction() {
        return rightrestriction;
    }

    public void setRightrestriction(float rightrestriction) {
        this.rightrestriction = rightrestriction;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isRightDirection() {
        return rightDirection;
    }

    public void setRightDirection(boolean rightDirection) {
        this.rightDirection = rightDirection;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public void setInvincible(boolean invincible) {
        isInvincible = invincible;
    }

    public float getInvincibleTimer() {
        return invincibleTimer;
    }

    public void setInvincibleTimer(float invincibleTimer) {
        this.invincibleTimer = invincibleTimer;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(float attackRange) {
        this.attackRange = attackRange;
    }

    public void setState(MosscreepState state) {
        this.state = state;
    }

    public float getTurnTimer() {
        return turnTimer;
    }

    public void setTurnTimer(float turnTimer) {
        this.turnTimer = turnTimer;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public boolean isRemovePending() {
        return removePending;
    }

    public void setRemovePending(boolean removePending) {
        this.removePending = removePending;
    }
}
