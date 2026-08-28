package Controller;

import Model.Enums.HuskHornheadState;
import Model.HuskHornhead;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class HuskHornheadController {

    public void update(float delta, Vector2 knightPosition, HuskHornhead husk) {
        Vector2 pos = husk.getBody().getPosition();
        husk.getBody().setTransform(pos.x, husk.getSpawnY(), 0f);
        if (husk.isRemovePending()) {
            return;
        }
        husk.setStateTimer(husk.getStateTimer() + delta);

        if (husk.getCooldownTimer() > 0f) {
            husk.setCooldownTimer(Math.max(0f, husk.getCooldownTimer() - delta));
        }

        if (husk.getHurtCooldown() > 0f) {
            husk.setHurtCooldown(Math.max(0f, husk.getHurtCooldown() - delta));
        }

        if (husk.getState() == HuskHornheadState.Death) {
            husk.getBody().setLinearVelocity(0f, 0f);
            if (husk.getStateTimer() >= husk.getDeathDuration()) {
                husk.setRemovePending(true);
            }
            return;
        }

        if (husk.getState() == HuskHornheadState.Walk && husk.getCooldownTimer() <= 0f
            && canSeeKnight(husk, knightPosition)) {

            husk.setState(HuskHornheadState.AttackAnticipate);
            husk.setStateTimer(0f);
            husk.getBody().setLinearVelocity(0f, 0f);
            husk.setChargeDir(knightPosition.x >= husk.getBody().getPosition().x ? 1f : -1f);
            husk.setFaceRight(husk.getChargeDir() > 0f);
        }

        switch (husk.getState()) {
            case Walk:
                float x = husk.getBody().getPosition().x;
                husk.getBody().setLinearVelocity((husk.isFacingRight() ? 1 : -1) * husk.getWalkSpeed(), 0f);
                if ((x >= husk.getRightBound() || x <= husk.getLeftBound())) {
                    husk.setState(HuskHornheadState.Turn);
                    husk.setStateTimer(0f);
                    husk.getBody().setLinearVelocity(0f, 0f);
                }

                break;

            case Turn:
                husk.getBody().setLinearVelocity(0f, 0f);

                if (husk.getStateTimer() >= 0.1f) {

                    husk.setFaceRight(!husk.isFacingRight());

                    float x2 = husk.getBody().getPosition().x;

                    if (x2 >= husk.getRightBound()) {
                        husk.getBody().setTransform(
                            husk.getRightBound() - 0.15f,
                            husk.getBody().getPosition().y,
                            0f
                        );
                    }

                    if (x2 <= husk.getLeftBound()) {
                        husk.getBody().setTransform(
                            husk.getLeftBound() + 0.15f,
                            husk.getBody().getPosition().y,
                            0f
                        );
                    }

                    husk.setState(HuskHornheadState.Walk);
                    husk.setStateTimer(0f);
                }
                break;

            case AttackAnticipate:
                husk.getBody().setLinearVelocity(0f, 0f);
                if (husk.getStateTimer() >= husk.getAnticipateDuration()) {
                    husk.setState(HuskHornheadState.AttackLunge);
                    husk.setStateTimer(0f);
                }
                break;

            case AttackLunge:
                husk.getBody().setLinearVelocity(husk.getChargeDir() * husk.getChargeSpeed(), 0f);
                if (husk.getStateTimer() >= husk.getChargeDuration() || reachedPatrolEnd(husk)) {
                    husk.setState(HuskHornheadState.Recover);
                    husk.setStateTimer(0f);
                    husk.setCooldownTimer(husk.getAttackCooldown());
                    husk.getBody().setLinearVelocity(0f, 0f);
                }
                break;

            case Recover:
                husk.getBody().setLinearVelocity(0f, 0f);
                if (husk.getStateTimer() >= husk.getRecoverDuration()) {
                    husk.setState(HuskHornheadState.Walk);
                    husk.setStateTimer(0f);
                }
                break;

            default:
                break;
        }
    }

    private boolean canSeeKnight(HuskHornhead husk, Vector2 knightPosition) {
        Rectangle sight = husk.getSightBounds();
        Rectangle knightRect = new Rectangle(knightPosition.x - 0.25f,
            knightPosition.y - 0.45f, 0.5f, 0.9f);
        return sight.overlaps(knightRect);
    }

    private boolean reachedPatrolEnd(HuskHornhead husk) {
        float x = husk.getBody().getPosition().x;
        return x <= husk.getLeftBound() - 0.1f || x >= husk.getRightBound() + 0.1f;
    }

    public boolean damaged(
        Vector2 knightPosition,
        HuskHornhead husk
    ) {
        return damaged(knightPosition, husk, 1, 1f);
    }

    public boolean damaged(
        Vector2 knightPosition,
        HuskHornhead husk,
        int amount,
        float knockbackMultiplier
    ) {
        if (husk.isRemovePending() || husk.getHurtCooldown() > 0f || husk.isDead()) {
            return false;
        }

        husk.setHealth(husk.getHealth() - Math.max(1, amount));
        husk.setHurtCooldown(0.3f);
        husk.setCooldownTimer(husk.getAttackCooldown());

        float dir = Math.signum(husk.getBody().getPosition().x - knightPosition.x);
        if (dir == 0f) {
            dir = husk.isFacingRight() ? 1f : -1f;
        }

        husk.getBody().setLinearVelocity(0f, 0f);
        husk.getBody().applyLinearImpulse(new Vector2(dir * 4.8f * knockbackMultiplier, 0f),
            husk.getBody().getWorldCenter(), true);

        if (husk.getHealth() <= 0) {
            husk.setState(HuskHornheadState.Death);
            husk.setStateTimer(0f);
            husk.getBody().setLinearVelocity(0f, 0f);
        } else {
            husk.setState(HuskHornheadState.Recover);
            husk.setStateTimer(0f);
        }

        return true;
    }
}
