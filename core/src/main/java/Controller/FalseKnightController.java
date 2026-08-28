package Controller;

import Model.Enums.FalseKnightState;
import Model.FalseKnight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class FalseKnightController {

    private Rectangle arenaBounds;
    private float bodyHitCooldown = 0f;
    private float heavyDamageWindowTimer = 0f;
    private int recentHitCount = 0;
    private float recentHitWindowTimer = 0f;
    private static final float RECENT_HIT_WINDOW = 0.6f;
    private static final int RECENT_HIT_THRESHOLD = 3;
    private float jumpAttackShockwaveTimer = 0f;
    private boolean jumpBackward = false;
    private boolean jumpAttackShockwaveSpawned = false;
    private int stunCount = 0;
    private float jumpStartY = Float.NaN;
    private float jumpAttackStartY = Float.NaN;

    public FalseKnightController() {
        this.arenaBounds = new Rectangle();
    }

    public FalseKnightController(Rectangle arenaBounds) {
        setArenaBounds(arenaBounds);
    }

    public void setArenaBounds(Rectangle arenaBounds) {
        this.arenaBounds = arenaBounds == null ? new Rectangle() : new Rectangle(arenaBounds);
    }

    public void update(float delta, Vector2 knightPosition, FalseKnight falseKnight) {
        Body body = falseKnight.getBody();
        if (arenaBounds != null && arenaBounds.width > 0f && arenaBounds.height > 0f) {
            boolean knightInsideArena = arenaBounds.contains(knightPosition.x, knightPosition.y);

            if (!knightInsideArena && !falseKnight.isDead()) {
                body.setLinearVelocity(0f, 0f);
                falseKnight.setState(FalseKnightState.Idle);
                falseKnight.setStateTime(0f);
                falseKnight.setDecisionCooldown(0.1f);
                return;
            }
        }
        falseKnight.setStateTime(falseKnight.getStateTime() + delta);
        if (falseKnight.getStunTimer() > 0f) {
            falseKnight.setStunTimer(Math.max(0f, falseKnight.getStunTimer() - delta));
        }
        if (falseKnight.getAttackCooldown() > 0f) {
            falseKnight.setAttackCooldown(Math.max(0f, falseKnight.getAttackCooldown() - delta));
        }
        if (falseKnight.getDecisionCooldown() > 0f) {
            falseKnight.setDecisionCooldown(Math.max(0f, falseKnight.getDecisionCooldown() - delta));
        }
        if (falseKnight.getRageTimer() > 0f) {
            falseKnight.setRageTimer(Math.max(0f, falseKnight.getRageTimer() - delta));
        }
        if (falseKnight.getHurtCooldown() > 0f) {
            falseKnight.setHurtCooldown(Math.max(0f, falseKnight.getHurtCooldown() - delta));
        }

        if (bodyHitCooldown > 0f) {
            bodyHitCooldown = Math.max(0f, bodyHitCooldown - delta);
        }
        if (heavyDamageWindowTimer > 0f) {
            heavyDamageWindowTimer = Math.max(0f, heavyDamageWindowTimer - delta);
        }
        if (recentHitWindowTimer > 0f) {
            recentHitWindowTimer = Math.max(0f, recentHitWindowTimer - delta);
            if (recentHitWindowTimer <= 0f) {
                recentHitCount = 0;
            }
        }
        if (jumpAttackShockwaveTimer > 0f) {
            jumpAttackShockwaveTimer = Math.max(0f, jumpAttackShockwaveTimer - delta);
        }

        if (falseKnight.isDead()) {
            if (!isInDeathSequenceState(falseKnight.getState())) {
                startDeathSequence(falseKnight);
            }
            updateDeathSequence(falseKnight);
            return;
        }

        if (!isInStunSequenceState(falseKnight.getState()) && shouldStartStun(falseKnight)) {
            startStunSequence(falseKnight);
            return;
        }

        falseKnight.setSpeedMultiplier(falseKnight.isPhaseTwo() ? 1.4f : 1f);
        if (falseKnight.isPhaseTwo() && falseKnight.getRageTimer() > 0f) {
            falseKnight.setSpeedMultiplier(1.6f);
        }

        if (isInStunSequenceState(falseKnight.getState())) {
            updateStunSequence(delta, falseKnight);
            return;
        }

        keepBossOnArena(body, falseKnight);
        switch (falseKnight.getState()) {
            case Turn:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getTurnDuration()) {
                    falseKnight.setFacingRight(!falseKnight.isFacingRight());
                    falseKnight.setState(FalseKnightState.Idle);
                    falseKnight.setStateTime(0f);
                    falseKnight.setDecisionCooldown(0.08f);
                }
                break;
            case RunAntic:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getRunAnticDuration()) {
                    falseKnight.setState(FalseKnightState.Run);
                    falseKnight.setStateTime(0f);
                    falseKnight.setAttackCooldown(0.2f);
                }
                break;
            case Run:
                runTowardKnight(body, falseKnight, knightPosition,
                    falseKnight.getRunSpeed() * falseKnight.getSpeedMultiplier(), falseKnight);

                if (falseKnight.getStateTime() >= falseKnight.getRunDuration() || reachedArenaEdge(body)) {
                    body.setLinearVelocity(0f, 0f);
                    falseKnight.setState(FalseKnightState.AttackRecover);
                    falseKnight.setStateTime(0f);
                    falseKnight.setDecisionCooldown(0.35f);
                }
                break;
            case AttackRecover:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getAttackRecoverDuration()) {
                    falseKnight.setState(FalseKnightState.Idle);
                    falseKnight.setStateTime(0f);
                    falseKnight.setDecisionCooldown(0.1f);
                }
                break;
            case AttackAntic:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getAttackAnticDuration()) {
                    falseKnight.setState(FalseKnightState.Attack);
                    falseKnight.setStateTime(0f);
                }
                break;
            case Attack:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getAttackDuration()) {
                    falseKnight.setState(FalseKnightState.AttackRecover);
                    falseKnight.setStateTime(0f);
                    falseKnight.setDecisionCooldown(0.35f);
                }
                break;
            case Jump:
                if (falseKnight.getStateTime() <= delta + 0.001f) {
                    jumpStartY = body.getPosition().y;
                    float towardKnightDir = horizontalDirectionToKnight(body, knightPosition, falseKnight);
                    float dir = jumpBackward ? -towardKnightDir : towardKnightDir;

                    falseKnight.setFacingRight(towardKnightDir > 0f);
                    body.setGravityScale(1.1f);

                    float horizontalMul = jumpBackward ? 1.9f : 0.75f;
                    float verticalMul = jumpBackward ? 1.35f : 1.10f;
                    body.setLinearVelocity(dir * falseKnight.getRunSpeed() * horizontalMul * falseKnight.getSpeedMultiplier(),
                        falseKnight.getJumpSpeed() * verticalMul);
                }
                if (falseKnight.getStateTime() > 0.18f
                    && body.getLinearVelocity().y <= 0f
                    && !Float.isNaN(jumpStartY)
                    && body.getPosition().y <= jumpStartY + 0.12f) {
                    falseKnight.setState(FalseKnightState.Land);
                    falseKnight.setStateTime(0f);
                    jumpBackward = false;
                }
                break;

            case JumpAttack:
                if (falseKnight.getStateTime() <= delta + 0.001f) {
                    jumpAttackStartY = body.getPosition().y;
                    float dir = horizontalDirectionToKnight(body, knightPosition, falseKnight);
                    falseKnight.setFacingRight(dir > 0f);
                    body.setGravityScale(1.15f);
                    body.setLinearVelocity(dir * falseKnight.getRunSpeed() * 0.60f * falseKnight.getSpeedMultiplier(),
                        falseKnight.getJumpSpeed() * 1.18f);
                }
                boolean jumpAttackLanded = falseKnight.getStateTime() > 0.20f
                    && body.getLinearVelocity().y <= 0f
                    && !Float.isNaN(jumpAttackStartY)
                    && body.getPosition().y <= jumpAttackStartY + 0.14f;
                if (jumpAttackLanded || falseKnight.getStateTime() >= falseKnight.getJumpDuration() + 0.75f) {
                    falseKnight.setState(FalseKnightState.JumpAttackHit);
                    falseKnight.setStateTime(0f);
                    body.setLinearVelocity(body.getLinearVelocity().x * 0.12f, 0f);
                    jumpAttackShockwaveTimer = 0.42f;
                    jumpAttackShockwaveSpawned = false;
                }
                break;

            case Land:
            case JumpAttackHit:
                if (falseKnight.getStateTime() <= delta + 0.001f) {
                    body.setGravityScale(1f);
                }
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getLandDuration()) {
                    falseKnight.setState(FalseKnightState.Idle);
                    falseKnight.setStateTime(0f);
                    falseKnight.setDecisionCooldown(0.18f);
                }
                break;
            case Body:
                body.setLinearVelocity(0f, 0f);
                break;
            case Idle:
            default:
                body.setLinearVelocity(0f, 0f);
                falseKnight.setLastSafeX(body.getPosition().x);
                falseKnight.setLastSafeY(body.getPosition().y);

                if (falseKnight.getDecisionCooldown() > 0f) {
                    break;
                }

                if (needsTurn(body, falseKnight, knightPosition)) {
                    enterState(falseKnight, FalseKnightState.Turn);
                    break;
                }

                FalseKnightState nextMove = chooseNextMove(falseKnight, knightPosition);
                if (nextMove == FalseKnightState.Idle) {
                    break;
                }

                if (shouldAvoidSpam(falseKnight, nextMove)) {
                    nextMove = fallbackMove(falseKnight, knightPosition, nextMove);
                }

                enterState(falseKnight, nextMove);
                break;
        }
        keepBossOnArena(body, falseKnight);
    }

    private void startDeathSequence(FalseKnight falseKnight) {
        falseKnight.setDead(true);
        falseKnight.setStateTime(0f);
        falseKnight.setRemovePending(false);
        falseKnight.setRepeatedMoveCount(0);
        stunCount = 0;
        falseKnight.getBody().setGravityScale(1f);
        falseKnight.setLastState(FalseKnightState.Idle);
        falseKnight.setState(FalseKnightState.DeathFall);
        falseKnight.getBody().setLinearVelocity(0f, -5.5f);
        falseKnight.getBody().setAngularVelocity(0f);
        falseKnight.getBody().setActive(true);
    }

    private void updateDeathSequence(FalseKnight falseKnight) {
        Body body = falseKnight.getBody();

        switch (falseKnight.getState()) {
            case DeathFall:
                body.setLinearVelocity(0f, Math.min(body.getLinearVelocity().y, 0f));
                if (falseKnight.getStateTime() >= falseKnight.getDeathFallDuration()) {
                    falseKnight.setState(FalseKnightState.DeathLand);
                    falseKnight.setStateTime(0f);
                    body.setLinearVelocity(0f, 0f);
                }
                break;

            case DeathLand:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getDeathLandDuration()) {
                    falseKnight.setState(FalseKnightState.DeathHit);
                    falseKnight.setStateTime(0f);
                }
                break;

            case DeathHit:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getDeathHitDuration()) {
                    falseKnight.setState(FalseKnightState.DeathCorpse);
                    falseKnight.setStateTime(0f);
                    body.setLinearVelocity(0f, 0f);
                    body.setAngularVelocity(0f);
                    body.setGravityScale(0f);
                    body.setActive(false);
                }
                break;

            case DeathCorpse:
                body.setLinearVelocity(0f, 0f);
                body.setGravityScale(2f);
                break;

            default:
                falseKnight.setState(FalseKnightState.DeathFall);
                falseKnight.setStateTime(0f);
                body.setLinearVelocity(0f, -5.5f);
                break;
        }
    }

    private void startStunSequence(FalseKnight falseKnight) {
        falseKnight.setSpeedMultiplier(1f);
        falseKnight.setDecisionCooldown(0f);
        falseKnight.setAttackCooldown(0f);
        falseKnight.setRepeatedMoveCount(0);
        falseKnight.setLastState(FalseKnightState.Idle);
        falseKnight.setStunTimer(falseKnight.getStunDuration());
        falseKnight.setState(FalseKnightState.DeathFall);
        falseKnight.setStateTime(0f);
        falseKnight.getBody().setLinearVelocity(0f, 0f);
        falseKnight.getBody().setAngularVelocity(0f);
        falseKnight.getBody().setGravityScale(1f);
        falseKnight.getBody().setActive(true);
        stunCount = Math.min(2, stunCount + 1);
    }

    private void updateStunSequence(float delta, FalseKnight falseKnight) {
        Body body = falseKnight.getBody();

        switch (falseKnight.getState()) {
            case DeathFall:
                body.setLinearVelocity(0f, Math.min(body.getLinearVelocity().y, 0f));
                if (falseKnight.getStateTime() >= falseKnight.getDeathFallDuration()) {
                    falseKnight.setState(FalseKnightState.DeathLand);
                    falseKnight.setStateTime(0f);
                    body.setLinearVelocity(0f, 0f);
                }
                break;

            case DeathLand:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getDeathLandDuration()) {
                    falseKnight.setState(FalseKnightState.Body);
                    falseKnight.setStateTime(0f);
                }
                break;

            case Body:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStunTimer() <= 0f) {
                    falseKnight.setState(FalseKnightState.StunRecover);
                    falseKnight.setStateTime(0f);
                }
                break;

            case DeathHit:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= 0.12f) {
                    if (falseKnight.getStunTimer() <= 0f) {
                        falseKnight.setState(FalseKnightState.StunRecover);
                    } else {
                        falseKnight.setState(FalseKnightState.Body);
                    }
                    falseKnight.setStateTime(0f);
                }
                break;

            case StunRecover:
                body.setLinearVelocity(0f, 0f);
                if (falseKnight.getStateTime() >= falseKnight.getStunRecoverDuration()) {
                    falseKnight.setPhaseTwo(true);
                    falseKnight.setSpeedMultiplier(1.4f);
                    falseKnight.setRageTimer(1.0f);
                    falseKnight.setState(FalseKnightState.Idle);
                    falseKnight.setStateTime(0f);
                    falseKnight.setDecisionCooldown(0.15f);
                }
                break;
            case DeathCorpse:
                body.setLinearVelocity(0f, 0f);
                break;
        }
    }

    public boolean onHitDuringStun(Vector2 knightPos, FalseKnight falseKnight) {
        if (falseKnight == null || falseKnight.isDead()) {
            return false;
        }
        if (falseKnight.getState() != FalseKnightState.Body) {
            return false;
        }
        if (bodyHitCooldown > 0f) {
            return false;
        }

        bodyHitCooldown = 0.12f;

        float knockDir = Math.signum(falseKnight.getBody().getPosition().x - knightPos.x);
        if (knockDir == 0f) {
            knockDir = falseKnight.isFacingRight() ? 1f : -1f;
        }

        falseKnight.getBody().setLinearVelocity(0f, falseKnight.getBody().getLinearVelocity().y);
        falseKnight.getBody().applyLinearImpulse(new Vector2(knockDir * 0.45f, 0.0f),
            falseKnight.getBody().getWorldCenter(), true);

        if (falseKnight.getHealth() <= 0) {
            falseKnight.setDead(true);
            falseKnight.setState(FalseKnightState.DeathFall);
            falseKnight.setStateTime(0f);
            falseKnight.getBody().setLinearVelocity(0f, -5.5f);
            falseKnight.getBody().setGravityScale(1f);
            falseKnight.getBody().setActive(true);
            return true;
        }

        falseKnight.setState(FalseKnightState.DeathHit);
        falseKnight.setStateTime(0f);
        return true;
    }

    private boolean isInDeathSequenceState(FalseKnightState state) {
        return state == FalseKnightState.DeathFall
            || state == FalseKnightState.DeathLand
            || state == FalseKnightState.DeathHit
            || state == FalseKnightState.DeathCorpse;
    }

    private boolean isInStunSequenceState(FalseKnightState state) {
        return state == FalseKnightState.DeathFall
            || state == FalseKnightState.DeathLand
            || state == FalseKnightState.Body
            || state == FalseKnightState.DeathHit
            || state == FalseKnightState.StunRecover;
    }

    private boolean shouldStartStun(FalseKnight falseKnight) {
        if (falseKnight == null || falseKnight.getBody() == null) {
            return false;
        }

        float ratio = falseKnight.getHealth() / (float) Math.max(1, falseKnight.getMAX_HEALTH());
        if (stunCount <= 0) {
            return ratio <= 0.50f;
        }
        if (stunCount == 1) {
            return ratio <= 0.25f;
        }
        return false;
    }

    private void keepBossOnArena(Body body, FalseKnight boss) {
        if (arenaBounds == null || arenaBounds.width <= 0f || arenaBounds.height <= 0f) {
            return;
        }

        float minX = arenaBounds.x + FalseKnight.BODY_WIDTH / 2f;
        float maxX = arenaBounds.x + arenaBounds.width - FalseKnight.BODY_WIDTH / 2f;
        float minY = arenaBounds.y + FalseKnight.BODY_HEIGHT / 2f;
        float maxY = arenaBounds.y + arenaBounds.height - FalseKnight.BODY_HEIGHT / 2f;

        float x = MathUtils.clamp(body.getPosition().x, minX, maxX);
        float y = MathUtils.clamp(body.getPosition().y, minY, maxY);

        if (Math.abs(x - body.getPosition().x) > 0.001f || Math.abs(y - body.getPosition().y) > 0.001f) {
            body.setTransform(x, y, 0f);

            float vx = body.getLinearVelocity().x;
            float vy = body.getLinearVelocity().y;
            if (x <= minX || x >= maxX) {
                vx = 0f;
            }
            if (y <= minY || y >= maxY) {
                vy = 0f;
            }
            body.setLinearVelocity(vx, vy);
        }
    }

    private void runTowardKnight(Body body, FalseKnight boss, Vector2 knightPosition,
                                 float speed, FalseKnight falseKnight) {
        float dir = horizontalDirectionToKnight(body, knightPosition, falseKnight);
        boss.setFacingRight(dir > 0f);
        body.setLinearVelocity(dir * speed, body.getLinearVelocity().y);
    }

    private float horizontalDirectionToKnight(Body body, Vector2 knightPosition, FalseKnight falseKnight) {
        float dx = knightPosition.x - body.getPosition().x;
        if (Math.abs(dx) < 0.15f) {
            return falseKnight.isFacingRight() ? 1f : -1f;
        }
        return Math.signum(dx);
    }

    private boolean reachedArenaEdge(Body body) {
        if (arenaBounds == null || arenaBounds.width <= 0f || arenaBounds.height <= 0f) {
            return false;
        }

        float minX = arenaBounds.x + 0.6f;
        float maxX = arenaBounds.x + arenaBounds.width - 0.6f;
        float x = body.getPosition().x;
        return x <= minX || x >= maxX;
    }

    private boolean needsTurn(Body body, FalseKnight boss, Vector2 knightPosition) {
        float dx = knightPosition.x - body.getPosition().x;
        if (Math.abs(dx) < 0.15f) {
            return false;
        }
        boolean knightOnRight = dx > 0f;
        return knightOnRight != boss.isFacingRight();
    }

    private void enterState(FalseKnight boss, FalseKnightState state) {
        FalseKnightState previous = boss.getLastState();
        FalseKnightState previousNormalized = normalizeMove(previous);
        FalseKnightState nextNormalized = normalizeMove(state);

        if (nextNormalized == previousNormalized) {
            boss.setRepeatedMoveCount(boss.getRepeatedMoveCount() + 1);
        } else {
            boss.setRepeatedMoveCount(1);
        }

        boss.setLastState(state);
        boss.setState(state);
        boss.setStateTime(0f);
        boss.setDecisionCooldown(0f);

        if (state == FalseKnightState.Jump) {
            jumpBackward = heavyDamageWindowTimer > 0f;
            jumpStartY = boss.getBody() != null ? boss.getBody().getPosition().y : Float.NaN;
            if (jumpBackward) {
                heavyDamageWindowTimer = 0f;
            }
        } else if (state == FalseKnightState.JumpAttack) {
            jumpAttackStartY = boss.getBody() != null ? boss.getBody().getPosition().y : Float.NaN;
        } else {
            jumpBackward = false;
        }

        if (state == FalseKnightState.RunAntic) {
            boss.setAttackCooldown(0.4f);
        } else if (state == FalseKnightState.AttackAntic) {
            boss.setAttackCooldown(0.45f);
        } else if (state == FalseKnightState.Jump) {
            boss.setAttackCooldown(0.5f);
        } else if (state == FalseKnightState.Body) {
            boss.setAttackCooldown(0.65f);
        } else if (state == FalseKnightState.JumpAttack) {
            jumpAttackShockwaveSpawned = false;
            jumpAttackShockwaveTimer = 0f;
        } else if (state == FalseKnightState.DeathCorpse) {
            boss.getBody().setActive(false);
        }
    }

    private FalseKnightState normalizeMove(FalseKnightState state) {
        if (state == null) {
            return FalseKnightState.Idle;
        }
        switch (state) {
            case Run:
            case RunAntic:
                return FalseKnightState.RunAntic;
            case Attack:
            case AttackAntic:
            case AttackRecover:
                return FalseKnightState.AttackAntic;
            case Jump:
                return FalseKnightState.Jump;
            case JumpAttack:
            case JumpAttackHit:
            case Land:
                return FalseKnightState.JumpAttack;
            case Body:
                return FalseKnightState.Body;
            default:
                return state;
        }
    }

    private FalseKnightState chooseNextMove(FalseKnight falseKnight, Vector2 knightPosition) {
        float dx = knightPosition.x - falseKnight.getBody().getPosition().x;
        float distance = Math.abs(dx);

        if (heavyDamageWindowTimer > 0f) {
            jumpBackward = true;
            return FalseKnightState.Jump;
        }

        if (distance <= 2.75f) {
            if (falseKnight.isPhaseTwo()) {
                return Pick(new FalseKnightState[]{FalseKnightState.JumpAttack,
                        FalseKnightState.AttackAntic, FalseKnightState.Jump},
                    new int[]{72, 18, 10}
                );
            }
            return Pick(new FalseKnightState[]{FalseKnightState.JumpAttack,
                    FalseKnightState.AttackAntic, FalseKnightState.Jump},
                new int[]{55, 30, 15}
            );
        }

        if (distance <= 6f) {
            if (falseKnight.isPhaseTwo()) {
                return Pick(new FalseKnightState[]{FalseKnightState.JumpAttack,
                        FalseKnightState.RunAntic, FalseKnightState.AttackAntic},
                    new int[]{58, 24, 18}
                );
            }
            return Pick(new FalseKnightState[]{FalseKnightState.JumpAttack,
                    FalseKnightState.Jump, FalseKnightState.RunAntic},
                new int[]{38, 34, 28}
            );
        }

        return Pick(new FalseKnightState[]{FalseKnightState.JumpAttack,
                FalseKnightState.Jump, FalseKnightState.RunAntic},
            new int[]{34, 38, 28}
        );
    }

    private FalseKnightState Pick(FalseKnightState[] moves, int[] chance) {
        int total = 0;
        for (int chances : chance) {
            total += Math.max(0, chances);
        }

        if (total <= 0 || moves.length == 0) {
            return FalseKnightState.Idle;
        }

        int roll = MathUtils.random(total - 1);
        int cursor = 0;
        for (int i = 0; i < moves.length; i++) {
            cursor += Math.max(0, chance[i]);
            if (roll < cursor) {
                return moves[i];
            }
        }
        int randomized = MathUtils.random(moves.length - 1);
        return moves[randomized];
    }

    private boolean shouldAvoidSpam(FalseKnight falseKnight, FalseKnightState candidate) {
        return falseKnight.getRepeatedMoveCount() >= 2 &&
            normalizeMove(candidate) == normalizeMove(falseKnight.getLastState());
    }

    private FalseKnightState fallbackMove(FalseKnight falseKnight, Vector2 knightPosition, FalseKnightState blocked) {
        float distance = Math.abs(knightPosition.x - falseKnight.getBody().getPosition().x);

        if (distance <= 3f) {
            if (blocked != FalseKnightState.JumpAttack) {
                return falseKnight.isPhaseTwo() ? FalseKnightState.JumpAttack : FalseKnightState.Jump;
            }
            return FalseKnightState.AttackAntic;
        }

        if (distance <= 6f) {
            if (blocked != FalseKnightState.Jump) {
                return FalseKnightState.Jump;
            }
            return FalseKnightState.RunAntic;
        }

        if (blocked != FalseKnightState.Jump) {
            return FalseKnightState.Jump;
        }
        return FalseKnightState.RunAntic;
    }

    public boolean consumeJumpAttackShockwaveSpawn(FalseKnight boss) {
        if (boss == null || boss.getState() != FalseKnightState.JumpAttackHit) {
            return false;
        }
        if (jumpAttackShockwaveSpawned) {
            return false;
        }
        jumpAttackShockwaveSpawned = true;
        return true;
    }

    public boolean takeDamage(FalseKnight falseKnight, int amount, Vector2 knightPosition, float delta) {
        return takeDamage(falseKnight, amount, knightPosition, delta, 1f);
    }

    public boolean takeDamage(FalseKnight falseKnight, int amount, Vector2 knightPosition,
                              float delta, float knockbackMultiplier) {
        if (falseKnight == null || falseKnight.isRemovePending() || falseKnight.isDead()) {
            return false;
        }
        if (falseKnight.getState() == FalseKnightState.Body) {
            if (onHitDuringStun(knightPosition, falseKnight)) {
                return true;
            }
            return false;
        }
        if (falseKnight.getHurtCooldown() > 0f) {
            return false;
        }
        falseKnight.setHurtCooldown(falseKnight.isPhaseTwo() ? 0.14f : 0.22f);
        falseKnight.damage(amount);
        falseKnight.setDecisionCooldown(0.2f);
        falseKnight.setAttackCooldown(0.15f);

        float knockDir = Math.signum(falseKnight.getBody().getPosition().x - knightPosition.x);
        if (knockDir == 0f) {
            knockDir = falseKnight.isFacingRight() ? 1f : -1f;
        }

        falseKnight.getBody().setLinearVelocity(0f, falseKnight.getBody().getLinearVelocity().y);
        falseKnight.getBody().applyLinearImpulse(new Vector2(knockDir * 0.55f * knockbackMultiplier, 0.0f),
            falseKnight.getBody().getWorldCenter(), true);

        recentHitWindowTimer = RECENT_HIT_WINDOW;
        recentHitCount++;
        if (amount >= 2 || recentHitCount >= RECENT_HIT_THRESHOLD) {
            heavyDamageWindowTimer = 1.0f;
            recentHitCount = 0;
            recentHitWindowTimer = 0f;
        }

        if (falseKnight.getHealth() <= 0) {
            falseKnight.setDead(true);
            falseKnight.setState(FalseKnightState.DeathFall);
            falseKnight.setStateTime(0f);
            falseKnight.getBody().setLinearVelocity(0f, -5.5f);
            falseKnight.getBody().setGravityScale(1f);
            falseKnight.getBody().setActive(true);
            return true;
        }

        if (!isInStunSequenceState(falseKnight.getState()) && shouldStartStun(falseKnight)) {
            startStunSequence(falseKnight);
        }

        return true;
    }

    public Rectangle getMaceHitBox(FalseKnight boss) {
        Rectangle box = new Rectangle();
        if (boss == null || boss.getBody() == null) {
            return box;
        }

        if (boss.getState() == FalseKnightState.Attack) {
            float bodyWidth = FalseKnight.BODY_WIDTH;
            float bodyHeight = FalseKnight.BODY_HEIGHT;
            float width = boss.isPhaseTwo() ? 1.65f : 1.45f;
            float height = boss.isPhaseTwo() ? 1.10f : 0.95f;
            float forwardGap = 0.18f;

            float x = boss.isFacingRight()
                ? boss.getBody().getPosition().x + bodyWidth / 2f + forwardGap
                : boss.getBody().getPosition().x - bodyWidth / 2f - forwardGap - width;

            float y = boss.getBody().getPosition().y - bodyHeight / 2f + 0.55f;
            box.set(x, y, width, height);
        }
        return box;
    }

    public Rectangle getBodyHitBox(FalseKnight boss) {
        Rectangle box = new Rectangle();
        if (boss == null || boss.getBody() == null) {
            return box;
        }

        if (boss.getState() == FalseKnightState.Body) {
            box.set(boss.getBounds());
        }
        return box;
    }

    public Rectangle getJumpImpactHitBox(FalseKnight boss) {
        Rectangle box = new Rectangle();
        if (boss == null || boss.getBody() == null) {
            return box;
        }

        if (boss.getState() == FalseKnightState.JumpAttackHit || boss.getState() == FalseKnightState.Land) {
            float baseWidth = boss.isPhaseTwo() ? 3.3f : 2.7f;
            float height = boss.isPhaseTwo() ? 1.7f : 1.45f;
            float pulse = jumpAttackShockwaveTimer > 0f ? jumpAttackShockwaveTimer * 1.2f : 0f;
            float width = baseWidth + pulse;
            float x = boss.getBody().getPosition().x - width / 2f;
            float y = boss.getBody().getPosition().y - 0.95f;
            box.set(x, y, width, height);
        }
        return box;
    }

    public boolean isAttackActive(FalseKnight boss) {
        return boss != null && (
            boss.getState() == FalseKnightState.Attack
                || boss.getState() == FalseKnightState.JumpAttack
                || boss.getState() == FalseKnightState.JumpAttackHit
        );
    }
}
