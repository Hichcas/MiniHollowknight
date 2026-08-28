package Controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CameraController {
    private final OrthographicCamera camera;

    private final Vector2 followTarget = new Vector2();

    private float followLerp = 5.5f;
    private float deadZoneWidth = 3.0f;
    private float deadZoneHeight = 1.8f;

    private float shakeTimeLeft = 0f;
    private float shakePower = 0f;

    public CameraController(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void setFollowLerp(float followLerp) {
        this.followLerp = Math.max(0f, followLerp);
    }

    public void setDeadZone(float width, float height) {
        this.deadZoneWidth = Math.max(0f, width);
        this.deadZoneHeight = Math.max(0f, height);
    }

    public void triggerShake(float duration, float power) {
        this.shakeTimeLeft = Math.max(this.shakeTimeLeft, duration);
        this.shakePower = Math.max(this.shakePower, power);
    }

    public void update(float delta, Vector2 targetPosition, Rectangle clampBounds) {
        if (camera == null || targetPosition == null) {
            return;
        }

        followTarget.set(targetPosition);

        float targetX = followTarget.x;
        float targetY = followTarget.y;

        float currentX = camera.position.x;
        float currentY = camera.position.y;

        float dx = targetX - currentX;
        float dy = targetY - currentY;

        float moveX = currentX;
        float moveY = currentY;

        if (Math.abs(dx) > deadZoneWidth * 0.5f) {
            moveX = MathUtils.lerp(currentX, targetX, MathUtils.clamp(delta * followLerp, 0f, 1f));
        }

        if (Math.abs(dy) > deadZoneHeight * 0.5f) {
            moveY = MathUtils.lerp(currentY, targetY, MathUtils.clamp(delta * followLerp, 0f, 1f));
        }

        if (shakeTimeLeft > 0f) {
            shakeTimeLeft -= delta;

            float shakeX = MathUtils.random(-shakePower, shakePower);
            float shakeY = MathUtils.random(-shakePower, shakePower);

            camera.position.set(moveX + shakeX, moveY + shakeY, camera.position.z);
        } else {
            camera.position.set(moveX, moveY, camera.position.z);
            shakePower = 0f;
        }

        clampCamera(clampBounds);
        camera.update();
    }

    private void clampCamera(Rectangle bounds) {
        if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            return;
        }

        float halfViewportWidth = camera.viewportWidth * 0.5f;
        float halfViewportHeight = camera.viewportHeight * 0.5f;

        float minX = bounds.x + halfViewportWidth;
        float maxX = bounds.x + bounds.width - halfViewportWidth;
        float minY = bounds.y + halfViewportHeight;
        float maxY = bounds.y + bounds.height - halfViewportHeight;

        if (minX <= maxX) {
            camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        }

        if (minY <= maxY) {
            camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
        }
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
