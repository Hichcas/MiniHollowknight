package Controller;

import Model.CrystalGuardian;
import Model.Enums.CrystalGuardianState;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import static Model.CrystalGuardian.ENEMY_HEIGHT;
import static Model.CrystalGuardian.ENEMY_WIDTH;

public class CrystalGuardianController {
    public CrystalGuardianController(laserSpawner laserSpawner) {
        this.laserSpawner = laserSpawner;
    }

    public interface laserSpawner {
        void spawn(float x, float y, boolean faceRight);
    }

    private final laserSpawner laserSpawner;

    public void update(float delta, Vector2 knightPosition, CrystalGuardian crystalGuardian) {
        if (crystalGuardian.isDead()) {
            if (crystalGuardian.getState() == CrystalGuardianState.DeathAir) {
                crystalGuardian.getBody().setLinearVelocity(0f,
                    crystalGuardian.getBody().getLinearVelocity().y);

                if (crystalGuardian.getStateTime() >= 0.2f) {
                    crystalGuardian.setState(CrystalGuardianState.DeathLand);
                    crystalGuardian.setStateTime(0f);
                    crystalGuardian.getBody().setLinearVelocity(0f, 0f);
                }
            } else if (crystalGuardian.getState() == CrystalGuardianState.DeathLand) {
                crystalGuardian.getBody().setLinearVelocity(0f, 0f);
                if (crystalGuardian.getStateTime() >= CrystalGuardian.DEATH_DURATION) {
                    crystalGuardian.setRemovePending(true);
                }
            }
            return;
        }
        if (crystalGuardian.getDamageCooldown() > 0) {
            crystalGuardian.setDamageCooldown(crystalGuardian.getDamageCooldown() - delta);
        }

        if (crystalGuardian.getRunHitCooldown() > 0f) {
            crystalGuardian.setRunHitCooldown(crystalGuardian.getRunHitCooldown() - delta);
        }

        switch (crystalGuardian.getState()) {
            case Idle:
                Body body = crystalGuardian.getBody();
                body.setLinearVelocity(0f, 0f);

                if (knightBehind(crystalGuardian, knightPosition)) {
                    crystalGuardian.setState(CrystalGuardianState.Turn);
                    crystalGuardian.setStateTime(0f);
                    return;
                }

                if (canSeeKnight(crystalGuardian, knightPosition) && crystalGuardian.getAttackCooldown() <= 0f) {
                    crystalGuardian.setState(CrystalGuardianState.Shoot);
                    crystalGuardian.setStateTime(0f);
                    crystalGuardian.setShootLaserSpawned(false);
                }
                break;
            case Turn:
                crystalGuardian.getBody().setLinearVelocity(0f, 0f);

                if (crystalGuardian.getStateTime() >= CrystalGuardian.TURN_DURATION) {
                    crystalGuardian.setFacingRight(!crystalGuardian.isFacingRight());
                    crystalGuardian.setState(CrystalGuardianState.Idle);
                    crystalGuardian.setStateTime(0f);
                }
                break;
            case Shoot:

                crystalGuardian.getBody().setLinearVelocity(0f, 0f);

                if (!crystalGuardian.isShootLaserSpawned()
                    && crystalGuardian.getStateTime() >= 0.25f) {

                    laserSpawner.spawn(
                        crystalGuardian.getBody().getPosition().x,
                        crystalGuardian.getBody().getPosition().y,
                        crystalGuardian.isFacingRight()
                    );

                    crystalGuardian.setShootLaserSpawned(true);
                }

                if (crystalGuardian.getStateTime()
                    >= CrystalGuardian.SHOOT_DURATION) {

                    crystalGuardian.setAttackCooldown(2.0f);

                    crystalGuardian.setState(
                        CrystalGuardianState.Enraged
                    );

                    crystalGuardian.setStateTime(0f);
                }

                break;
            case Enraged:
                crystalGuardian.getBody().setLinearVelocity(0f, 0f);
                if (crystalGuardian.getStateTime() >= CrystalGuardian.EVADE_DURATION) {
                    crystalGuardian.setState(CrystalGuardianState.Run);
                    crystalGuardian.setStateTime(0f);
                    crystalGuardian.setAttackCooldown(1.5f);
                }
                break;
            case Run:
                crystalGuardian.getBody().setLinearVelocity(
                    (crystalGuardian.isFacingRight() ? 1f : -1f) * CrystalGuardian.RUN_SPEED, 0f);

                if (crystalGuardian.getStateTime() >= CrystalGuardian.RUN_DURATION) {
                    crystalGuardian.getBody().setLinearVelocity(0f, 0f);
                    crystalGuardian.setState(CrystalGuardianState.Idle);
                    crystalGuardian.setStateTime(0f);
                }
                break;
            case DeathAir:
                crystalGuardian.getBody().setLinearVelocity
                    (0f, crystalGuardian.getBody().getLinearVelocity().y);
                if (crystalGuardian.getStateTime() >= 0.2f) {
                    crystalGuardian.setState(CrystalGuardianState.DeathLand);
                    crystalGuardian.setStateTime(0f);
                    crystalGuardian.getBody().setLinearVelocity(0f, 0f);
                }
                break;
            case DeathLand:
                crystalGuardian.getBody().setLinearVelocity(0f, 0f);
                if (crystalGuardian.getStateTime() >= CrystalGuardian.DEATH_DURATION) {
                    crystalGuardian.setRemovePending(true);
                }
        }

    }

    private boolean knightBehind(CrystalGuardian guardian, Vector2 knightPos) {
        float dx = knightPos.x - guardian.getBody().getPosition().x;

        if (guardian.isFacingRight()) {
            return dx < -0.15f;
        }

        return dx > 0.15f;
    }

    private boolean canSeeKnight(CrystalGuardian guardian, Vector2 knightPos) {
        float dx = knightPos.x - guardian.getBody().getPosition().x;

        if (guardian.isFacingRight()) {
            return dx > 0f && dx < CrystalGuardian.DETECTION_RANGE;
        }

        return dx < 0f && Math.abs(dx) < CrystalGuardian.DETECTION_RANGE;
    }

    private Rectangle getEnemyBounds(CrystalGuardian crystalGuardian) {
        Vector2 p = crystalGuardian.getBody().getPosition();
        return new Rectangle(
            p.x - ENEMY_WIDTH / 2f,
            p.y - ENEMY_HEIGHT / 2f,
            ENEMY_WIDTH,
            ENEMY_HEIGHT
        );
    }

    public boolean takeDamage(
        CrystalGuardian crystalGuardian
    ) {
        return takeDamage(crystalGuardian, 1);
    }

    public boolean takeDamage(
        CrystalGuardian crystalGuardian,
        int amount
    ) {

        if (crystalGuardian.getDamageCooldown() > 0) {
            return false;
        }
        crystalGuardian.setDamageCooldown(0.3f);
        crystalGuardian.setHealth(crystalGuardian.getHealth() - Math.max(1, amount));
        if (crystalGuardian.getHealth() <= 0) {
            crystalGuardian.setHealth(0);
            crystalGuardian.setDead(true);
            crystalGuardian.setDamageCooldown(0f);
            crystalGuardian.setState(CrystalGuardianState.DeathAir);
            crystalGuardian.setStateTime(0f);
            crystalGuardian.getBody().setLinearVelocity(0f, 0f);
        }
        return true;
    }
}
