package Model;

import Model.Enums.ZoteState;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Zote {
    private final Body body;
    private float Width = 2f;
    private float Height = 3f;
    private float attackCooldownTimer = 0f;
    private float fallingTimer = 0f;
    private float getUpTimer = 0f;
    private float rollTimer = 0f;
    private float turnTimer = 0f;
    private float angerTimer = 0f;
    private ZoteState state = ZoteState.Idle;
    private float stateTime = 0f;
    private boolean facingRight = false;
    private final Rectangle bounds = new Rectangle();
    private boolean mainDialogueFinished = false;
    private int mainDialogueIndex = 0;
    private int lastPreceptIndex = -1;
    private float interactionRange = 5f;
    private boolean playerNearby = false;
    private final float fallDuration = 0.5f;
    private final float getUpDuration = 0.4f;
    private final float turnDuration = 0.25f;
    private final float rollDuration = 0.3f;
    private final float angerDuration = 1.5f;
    private final float rollSpeed = 6f;
    private final float attackMoveSpeed = 2.2f;

    public Zote(Body body) {
        this.body = body;
        body.setUserData(this);
    }

    public Body getBody() {
        return body;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public Rectangle getBounds() {
        Vector2 pos = body.getPosition();
        bounds.set(pos.x - Width / 2f, pos.y - Height / 2f, Width, Height);
        return bounds;
    }

    public float getWidth() {
        return Width;
    }

    public float getHeight() {
        return Height;
    }

    public ZoteState getState() {
        return state;
    }

    public void setState(ZoteState state) {
        if (this.state != state) {
            this.state = state;
            this.stateTime = 0f;
        }
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public float getAttackCooldownTimer() {
        return attackCooldownTimer;
    }

    public void setAttackCooldownTimer(float attackCooldownTimer) {
        this.attackCooldownTimer = attackCooldownTimer;
    }

    public float getFallingTimer() {
        return fallingTimer;
    }

    public void setFallingTimer(float fallingTimer) {
        this.fallingTimer = fallingTimer;
    }

    public float getGetUpTimer() {
        return getUpTimer;
    }

    public void setGetUpTimer(float getUpTimer) {
        this.getUpTimer = getUpTimer;
    }

    public float getRollTimer() {
        return rollTimer;
    }

    public void setRollTimer(float rollTimer) {
        this.rollTimer = rollTimer;
    }

    public float getTurnTimer() {
        return turnTimer;
    }

    public void setTurnTimer(float turnTimer) {
        this.turnTimer = turnTimer;
    }

    public float getAngerTimer() {
        return angerTimer;
    }

    public void setAngerTimer(float angerTimer) {
        this.angerTimer = angerTimer;
    }

    public boolean isMainDialogueFinished() {
        return mainDialogueFinished;
    }

    public void setMainDialogueFinished(boolean mainDialogueFinished) {
        this.mainDialogueFinished = mainDialogueFinished;
    }

    public int getMainDialogueIndex() {
        return mainDialogueIndex;
    }

    public void setMainDialogueIndex(int mainDialogueIndex) {
        this.mainDialogueIndex = mainDialogueIndex;
    }

    public int getLastPreceptIndex() {
        return lastPreceptIndex;
    }

    public void setLastPreceptIndex(int lastPreceptIndex) {
        this.lastPreceptIndex = lastPreceptIndex;
    }

    public float getInteractionRange() {
        return interactionRange;
    }

    public void setInteractionRange(float interactionRange) {
        this.interactionRange = interactionRange;
    }

    public boolean isPlayerNearby() {
        return playerNearby;
    }

    public void setPlayerNearby(boolean playerNearby) {
        this.playerNearby = playerNearby;
    }

    public float getFallDuration() {
        return fallDuration;
    }

    public float getGetUpDuration() {
        return getUpDuration;
    }

    public float getTurnDuration() {
        return turnDuration;
    }

    public float getRollDuration() {
        return rollDuration;
    }

    public float getAngerDuration() {
        return angerDuration;
    }

    public float getRollSpeed() {
        return rollSpeed;
    }

    public float getAttackMoveSpeed() {
        return attackMoveSpeed;
    }
}
