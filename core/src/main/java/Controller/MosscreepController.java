package Controller;

import Model.Enums.MosscreepState;
import Model.Mosscreep;
import com.badlogic.gdx.math.Vector2;

public class MosscreepController {
    public void update(float delta, Vector2 knightPosition, Mosscreep Mosscreep) {
        if (Mosscreep.getState() == MosscreepState.DEAD) {
            Mosscreep.setStateTime(Mosscreep.getStateTime() + delta);

            if (Mosscreep.getStateTime() >= 0.6f) {
                Mosscreep.setRemovePending(true);
            }
            return;
        }

        Mosscreep.setStateTime(Mosscreep.getStateTime() + delta);

        if (Mosscreep.isInvincible()) {
            Mosscreep.setInvincibleTimer(Mosscreep.getInvincibleTimer() - delta);
            if (Mosscreep.getInvincibleTimer() <= 0f) {
                Mosscreep.setInvincible(false);
            }
        }

        if (Mosscreep.isHidden()) {
            if (knightPosition.dst(Mosscreep.getBody().getPosition()) <= Mosscreep.getAttackRange()) {
                Mosscreep.setHidden(false);
                Mosscreep.setState(MosscreepState.WALK);
            } else {
                return;
            }
        }

        if (Mosscreep.getState() == MosscreepState.TURN) {
            Mosscreep.getBody().setLinearVelocity(0f, Mosscreep.getBody().getLinearVelocity().y);
            Mosscreep.setTurnTimer(Mosscreep.getTurnTimer() - delta);
            if (Mosscreep.getTurnTimer() <= 0f) {
                Mosscreep.setRightDirection(!Mosscreep.isRightDirection());
                Mosscreep.setState(MosscreepState.WALK);
                Mosscreep.setStateTime(0f);
            }
            return;
        }

        walkLogic(Mosscreep);
    }

    private void walkLogic(Mosscreep Mosscreep) {
        float x = Mosscreep.getBody().getPosition().x;

        if (Mosscreep.isRightDirection()) {
            Mosscreep.getBody().setLinearVelocity(Mosscreep.getSpeed(), Mosscreep.getBody().getLinearVelocity().y);
            if (x >= Mosscreep.getRightrestriction()) {
                startTurn(Mosscreep);
            }
        } else {
            Mosscreep.getBody().setLinearVelocity(-Mosscreep.getSpeed(), Mosscreep.getBody().getLinearVelocity().y);
            if (x <= Mosscreep.getLeftrestriction()) {
                startTurn(Mosscreep);
            }
        }
    }

    private void startTurn(Mosscreep Mosscreep) {
        Mosscreep.setState(MosscreepState.TURN);
        Mosscreep.setTurnTimer(0.3f);
        Mosscreep.getBody().setLinearVelocity(0f, Mosscreep.getBody().getLinearVelocity().y);
        Mosscreep.setStateTime(0f);
    }

    private void die(Mosscreep Mosscreep) {
        Mosscreep.setState(MosscreepState.DEAD);
        Mosscreep.getBody().setLinearVelocity(0f, 0f);
        Mosscreep.setStateTime(0f);
        Mosscreep.setInvincible(false);
        Mosscreep.setInvincibleTimer(0f);
    }

    public boolean damaged(
        Vector2 knightPosition,
        Mosscreep Mosscreep
    ) {
        return damaged(knightPosition, Mosscreep, 1, 1f);
    }

    public boolean damaged(
        Vector2 knightPosition,
        Mosscreep Mosscreep,
        int amount,
        float knockbackMultiplier
    ) {
        if (Mosscreep.isInvincible() || Mosscreep.getHealth() <= 0 || Mosscreep.getState() == MosscreepState.DEAD) {
            return false;
        }

        Mosscreep.setHealth(Mosscreep.getHealth() - Math.max(1, amount));
        Mosscreep.setInvincible(true);
        Mosscreep.setInvincibleTimer(1f);

        float dir = Math.signum(Mosscreep.getBody().getPosition().x - knightPosition.x);
        if (dir == 0f) {
            dir = Mosscreep.isRightDirection() ? 1f : -1f;
        }

        Mosscreep.getBody().setLinearVelocity(0f, 0f);
        Mosscreep.getBody().applyLinearImpulse(new Vector2(dir * 5f * knockbackMultiplier, 1.2f * knockbackMultiplier), Mosscreep.getBody().getWorldCenter(), true);

        if (Mosscreep.getHealth() <= 0) {
            die(Mosscreep);
        }

        return true;
    }
}
