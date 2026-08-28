package Controller;

import Model.*;
import Model.Enums.KnightState;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.function.Function;

public class GroundContactListener implements ContactListener {

    private final Knight knight;
    private int groundContacts = 0;
    private int wallLeftContacts = 0;
    private int wallRightContacts = 0;
    private final Runnable onDamageShake;
    private final Function<Vector2, Vector2> nearestSafeSpawnResolver;

    public GroundContactListener(Knight knight, Runnable onDamageShake, Function<Vector2, Vector2> nearestSafeSpawnResolver) {
        this.knight = knight;
        this.onDamageShake = onDamageShake;
        this.nearestSafeSpawnResolver = nearestSafeSpawnResolver;
    }

    private static boolean isEnemyTag(Object tag) {
        return "MOSSCREEP".equals(tag) || "WINGEDSENTRY".equals(tag) || "HUSKHORNHEAD".equals(tag)
            || "CRYSTALGUARDIAN".equals(tag) || "FALSE_KNIGHT".equals(tag);
    }

    @Override
    public void beginContact(Contact contact) {

        Object a = contact.getFixtureA().getUserData();
        Object b = contact.getFixtureB().getUserData();

        boolean playerA = "PLAYER".equals(a);
        boolean playerB = "PLAYER".equals(b);
        if ((playerA && isEnemyTag(b)) || (playerB && isEnemyTag(a))) {
            if (knight.isAttacking() && knight.getState() == KnightState.DOWNSLASH) {
                knight.performPogo();
                return;
            }
            if ((knight.isDashing() && knight.canDashThroughEnemies())
                || knight.isGodMode() || knight.isNoclip()) {
                contact.setEnabled(false);
                return;
            }
        }

        if (isGroundPlayer(a, b)) {
            groundContacts++;
            knight.setGrounded(true);
        }

        if (isWallSensorAgainstSolid("PLAYER_WALL_LEFT", a, b)) {
            wallLeftContacts++;
            knight.setWallContacts(wallLeftContacts > 0, wallRightContacts > 0);
        }
        if (isWallSensorAgainstSolid("PLAYER_WALL_RIGHT", a, b)) {
            wallRightContacts++;
            knight.setWallContacts(wallLeftContacts > 0, wallRightContacts > 0);
        }

        if (("PLAYER".equals(a) && "DEADLY".equals(b)) || ("DEADLY".equals(a) && "PLAYER".equals(b))) {
            if (knight.isAttacking() && knight.getState() == KnightState.DOWNSLASH) {
                knight.performPogo();
            } else {
                knight.takeDamage(1, null, 0f);

                if (!knight.isDead() && nearestSafeSpawnResolver != null) {
                    Vector2 safeSpawn = nearestSafeSpawnResolver.apply(knight.getBody().getPosition());
                    if (safeSpawn != null) {
                        knight.queueRelocation(safeSpawn);
                    }
                }
            }
        }
        if (("PLAYER".equals(a) && "MOSSCREEP".equals(b)) || ("MOSSCREEP".equals(a) && "PLAYER".equals(b))) {
            Body enemyBody = "MOSSCREEP".equals(a) ? contact.getFixtureA().getBody() : contact.getFixtureB().getBody();
            Mosscreep enemy = (Mosscreep) enemyBody.getUserData();

            if (enemy != null && !knight.isDead() && !enemy.isDead() && !(knight.isDashing() && knight.canDashThroughEnemies())) {
                knight.takeDamage(1, enemy.getBody().getPosition(), 6f);
                if (onDamageShake != null) onDamageShake.run();
            }
        }
        if (("PLAYER".equals(a) && "WINGEDSENTRY".equals(b)) ||
            ("WINGEDSENTRY".equals(a) && "PLAYER".equals(b))) {
            Body enemyBody = "WINGEDSENTRY".equals(a) ? contact.getFixtureA().getBody() : contact.getFixtureB().getBody();
            WingedSentry enemy = (WingedSentry) enemyBody.getUserData();
            if (enemy != null && !knight.isDead() && !enemy.isDead() && !(knight.isDashing() && knight.canDashThroughEnemies())) {
                knight.takeDamage(1, enemy.getBody().getPosition(), 6f);
                if (onDamageShake != null) onDamageShake.run();
            }
        }
        if (("PLAYER".equals(a) && "HUSKHORNHEAD".equals(b)) || ("HUSKHORNHEAD".equals(a) && "PLAYER".equals(b))) {
            Body enemyBody = "HUSKHORNHEAD".equals(a) ? contact.getFixtureA().getBody() : contact.getFixtureB().getBody();
            HuskHornhead enemy = (HuskHornhead) enemyBody.getUserData();
            if (enemy != null && !knight.isDead() && !enemy.isDead() && !(knight.isDashing() && knight.canDashThroughEnemies())) {
                knight.takeDamage(1, enemy.getBody().getPosition(), 6f);
                if (onDamageShake != null) onDamageShake.run();
            }
        }
        if (("PLAYER".equals(a) && "CRYSTALGUARDIAN".equals(b)) || ("CRYSTALGUARDIAN".equals(a) && "PLAYER".equals(b))) {
            Body enemyBody = "CRYSTALGUARDIAN".equals(a) ? contact.getFixtureA().getBody() : contact.getFixtureB().getBody();
            CrystalGuardian enemy = (CrystalGuardian) enemyBody.getUserData();
            if (enemy != null && !knight.isDead() && !enemy.isDead() && !(knight.isDashing() && knight.canDashThroughEnemies())) {
                knight.takeDamage(1, enemy.getBody().getPosition(), 6f);
                if (onDamageShake != null) onDamageShake.run();
            }
        }
        if (("PLAYER".equals(a) && "FALSE_KNIGHT".equals(b)) || ("FALSE_KNIGHT".equals(a) && "PLAYER".equals(b))) {
            Body enemyBody = "FALSE_KNIGHT".equals(a) ? contact.getFixtureA().getBody() : contact.getFixtureB().getBody();
            FalseKnight boss = (FalseKnight) enemyBody.getUserData();
            if (boss != null && !knight.isDead() && !boss.isDead() && !boss.isRemovePending() && !(knight.isDashing() && knight.canDashThroughEnemies())) {
                Model.Enums.FalseKnightState st = boss.getState();
                boolean dangerous = st == Model.Enums.FalseKnightState.Attack
                    || st == Model.Enums.FalseKnightState.AttackAntic
                    || st == Model.Enums.FalseKnightState.AttackRecover
                    || st == Model.Enums.FalseKnightState.Run
                    || st == Model.Enums.FalseKnightState.RunAntic
                    || st == Model.Enums.FalseKnightState.Jump
                    || st == Model.Enums.FalseKnightState.JumpAttack
                    || st == Model.Enums.FalseKnightState.JumpAttackHit
                    || st == Model.Enums.FalseKnightState.Land
                    || st == Model.Enums.FalseKnightState.Body;
                if (dangerous) {
                    knight.takeDamage(1, boss.getBody().getPosition(), 8f);
                    if (onDamageShake != null) onDamageShake.run();
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {

        Object a = contact.getFixtureA().getUserData();
        Object b = contact.getFixtureB().getUserData();

        if (isGroundPlayer(a, b)) {
            groundContacts = Math.max(0, groundContacts - 1);
            if (groundContacts == 0) {
                knight.setGrounded(false);
            }
        }

        if (isWallSensorAgainstSolid("PLAYER_WALL_LEFT", a, b)) {
            wallLeftContacts = Math.max(0, wallLeftContacts - 1);
            knight.setWallContacts(wallLeftContacts > 0, wallRightContacts > 0);
        }
        if (isWallSensorAgainstSolid("PLAYER_WALL_RIGHT", a, b)) {
            wallRightContacts = Math.max(0, wallRightContacts - 1);
            knight.setWallContacts(wallLeftContacts > 0, wallRightContacts > 0);
        }
    }

    private boolean isGroundPlayer(Object a, Object b) {
        return ("PLAYER".equals(a) && "GROUND".equals(b)) || ("GROUND".equals(a) && "PLAYER".equals(b));
    }

    private boolean isSolidTag(Object tag) {
        return "GROUND".equals(tag) || "DEADLY".equals(tag);
    }

    private boolean isWallSensorAgainstSolid(String sensorTag, Object a, Object b) {
        return (sensorTag.equals(a) && isSolidTag(b)) || (sensorTag.equals(b) && isSolidTag(a));
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Object a = contact.getFixtureA().getUserData();
        Object b = contact.getFixtureB().getUserData();

        boolean playerA = "PLAYER".equals(a);
        boolean playerB = "PLAYER".equals(b);
        if ((playerA && isEnemyTag(b)) || (playerB && isEnemyTag(a))) {
            if ((knight.isDashing() && knight.canDashThroughEnemies())
                || knight.isGodMode() || knight.isNoclip()) {
                contact.setEnabled(false);
            }
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
