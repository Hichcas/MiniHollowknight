package Controller;

import Model.Enums.WingedSentryState;
import Model.WingedSentry;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class WingedSentryController {
    public void update(float delta, Vector2 knightPosition, WingedSentry WingedSentry) {
        if (WingedSentry.isRemovePending()) {
            return;
        }

        WingedSentry.setStateTime(WingedSentry.getStateTime() + delta);

        if (WingedSentry.getCooldownTimer() > 0f) {
            WingedSentry.setCooldownTimer(WingedSentry.getCooldownTimer() - delta);
        }

        if (WingedSentry.getHurtCooldown() > 0f) {
            WingedSentry.setHurtCooldown(WingedSentry.getHurtCooldown() - delta);
        }

        switch (WingedSentry.getState()) {

            case Idle: {
                float dx = knightPosition.x - WingedSentry.getBody().getPosition().x;
                WingedSentry.setFaceRight(dx >= 0f);

                WingedSentry.getBody().setLinearVelocity(
                    MathUtils.clamp(dx, -1f, 1f) * WingedSentry.getChaseSpeedX(),
                    0f
                );

                if (Math.abs(dx) <= WingedSentry.getDetectionRange() && WingedSentry.getCooldownTimer() <= 0f) {
                    WingedSentry.setTargetY(knightPosition.y - 0.6f);
                    WingedSentry.setChargeDirX(dx >= 0f ? 1f : -1f);
                    WingedSentry.setState(WingedSentryState.TurnToFly);
                    WingedSentry.setStateTimer(0f);
                    WingedSentry.setStateTime(0f);
                    WingedSentry.getBody().setLinearVelocity(0f, 0f);
                }
                break;
            }

            case TurnToFly: {
                float yDiff = WingedSentry.getTargetY() - WingedSentry.getBody().getPosition().y;

                if (Math.abs(yDiff) <= 0.08f) {
                    WingedSentry.getBody().setLinearVelocity(0f, 0f);
                    WingedSentry.setState(WingedSentryState.ChargeAntic);
                    WingedSentry.setStateTimer(0f);
                    WingedSentry.setStateTime(0f);
                } else {
                    WingedSentry.getBody().setLinearVelocity(0f, Math.signum(yDiff) * WingedSentry.getAlignSpeed());
                }
                break;
            }

            case ChargeAntic: {

                WingedSentry.getBody().setLinearVelocity(0f, 0f);
                WingedSentry.setStateTimer(WingedSentry.getStateTimer() + delta);
                if (WingedSentry.getStateTimer() >= WingedSentry.getChargeAnticDuration()) {
                    WingedSentry.setState(WingedSentryState.ChargeHorizontal);
                    WingedSentry.setStateTimer(0f);
                    WingedSentry.setStateTime(0f);

                }
                break;
            }

            case ChargeHorizontal: {
                WingedSentry.setFaceRight(WingedSentry.getChargeDirX() > 0f);
                WingedSentry.getBody().setLinearVelocity(WingedSentry.getChargeDirX()
                    * WingedSentry.getChargeSpeed(), 0f);
                WingedSentry.setStateTimer(WingedSentry.getStateTimer() + delta);
                if (WingedSentry.getStateTimer() >= WingedSentry.getChargeDuration()) {
                    WingedSentry.getBody().setLinearVelocity(0f, 0f);
                    WingedSentry.setState(WingedSentryState.Recover);
                    WingedSentry.setStateTimer(0f);
                    WingedSentry.setStateTime(0f);

                    WingedSentry.setCooldownTimer(WingedSentry.getAttackCooldown());
                }

                break;
            }
            case Recover:
                WingedSentry.getBody().setLinearVelocity(0f, 0f);
                WingedSentry.setStateTimer(WingedSentry.getStateTimer() + delta);

                if (WingedSentry.getStateTimer() >= WingedSentry.getRecoverDuration()) {
                    WingedSentry.setState(WingedSentryState.Idle);
                    WingedSentry.setStateTimer(0f);
                    WingedSentry.setStateTime(0f);

                }

                break;
            case DeathAir:
            case DeathLand:
                WingedSentry.getBody().setLinearVelocity(0f, 0f);
                WingedSentry.setStateTimer(WingedSentry.getStateTimer() + delta);
                if (WingedSentry.getStateTimer() >= WingedSentry.getDeathDuration()) {
                    WingedSentry.setRemovePending(true);
                }
                break;
        }
    }

    public boolean damage(
        Vector2 knightPosition,
        WingedSentry WingedSentry
    ) {
        return damage(knightPosition, WingedSentry, 1, 1f);
    }

    public boolean damage(
        Vector2 knightPosition,
        WingedSentry WingedSentry,
        int amount,
        float knockbackMultiplier
    ) {

        if (WingedSentry.isRemovePending() || WingedSentry.getHurtCooldown() > 0f ||
            WingedSentry.getState() == WingedSentryState.DeathAir ||
            WingedSentry.getState() == WingedSentryState.DeathLand) {
            return false;
        }

        WingedSentry.setHurtCooldown(0.25f);

        WingedSentry.setHealth(WingedSentry.getHealth() - Math.max(1, amount));

        if (WingedSentry.getHealth() <= 0) {

            WingedSentry.setState(WingedSentryState.DeathAir);
            WingedSentry.setStateTimer(0f);
            WingedSentry.setStateTime(0f);
            WingedSentry.getBody().setLinearVelocity(0f, 0f);

            return true;
        }

        float dir = Math.signum(WingedSentry.getBody().getPosition().x - knightPosition.x);

        if (dir == 0f) {
            dir = WingedSentry.isFaceRight() ? 1f : -1f;
        }

        WingedSentry.getBody().setLinearVelocity(0f, 0f);

        WingedSentry.getBody().applyLinearImpulse(new Vector2(dir * 4.5f * knockbackMultiplier, 1.2f * knockbackMultiplier),
            WingedSentry.getBody().getWorldCenter(), true);

        WingedSentry.setState(WingedSentryState.Idle);

        WingedSentry.setStateTimer(0f);
        WingedSentry.setStateTime(0f);
        WingedSentry.setCooldownTimer(WingedSentry.getAttackCooldown());
        return true;
    }
}
