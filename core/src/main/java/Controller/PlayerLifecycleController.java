package Controller;

import com.badlogic.gdx.math.Vector2;

public class PlayerLifecycleController {
    public interface RespawnHandler {
        void respawn(Vector2 respawnPoint);
    }

    private final Vector2 respawnPoint = new Vector2();

    private boolean respawnPending = false;
    private float respawnTimer = 0f;
    private float respawnDelay = 1.0f;

    private float invincibilityTimer = 0f;

    public void setRespawnPoint(float x, float y) {
        respawnPoint.set(x, y);
    }

    public Vector2 getRespawnPoint() {
        return new Vector2(respawnPoint);
    }

    public void setRespawnDelay(float respawnDelay) {
        this.respawnDelay = Math.max(0f, respawnDelay);
    }

    public void startDeathRespawn() {
        respawnPending = true;
        respawnTimer = respawnDelay;
    }

    public void cancelRespawn() {
        respawnPending = false;
        respawnTimer = 0f;
    }

    public void grantInvincibility(float seconds) {
        invincibilityTimer = Math.max(invincibilityTimer, seconds);
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0f;
    }

    public boolean isRespawnPending() {
        return respawnPending;
    }

    public void update(float delta, RespawnHandler handler) {
        if (invincibilityTimer > 0f) {
            invincibilityTimer -= delta;
            if (invincibilityTimer < 0f) {
                invincibilityTimer = 0f;
            }
        }

        if (!respawnPending) {
            return;
        }

        respawnTimer -= delta;

        if (respawnTimer <= 0f) {
            respawnPending = false;
            respawnTimer = 0f;

            if (handler != null) {
                handler.respawn(respawnPoint);
            }

            grantInvincibility(1.0f);
        }
    }
}
