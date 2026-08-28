package View;

import Model.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class EnemyRenderer {
    private final MosscreepAnimations mosscreepAnimations;
    private final WingedSentryAnimations wingedSentryAnimations;
    private final HuskHornheadAnimations huskHornheadAnimations;
    private final CrystalGuardianAnimations crystalGuardianAnimations;
    private final FalseKnightAnimations falseKnightAnimations;

    public EnemyRenderer(MosscreepAnimations mosscreepAnimations,
                         WingedSentryAnimations wingedSentryAnimations,
                         HuskHornheadAnimations huskHornheadAnimations,
                         CrystalGuardianAnimations crystalGuardianAnimations) {
        this(mosscreepAnimations, wingedSentryAnimations, huskHornheadAnimations, crystalGuardianAnimations, null);
    }

    public EnemyRenderer(MosscreepAnimations mosscreepAnimations,
                         WingedSentryAnimations wingedSentryAnimations,
                         HuskHornheadAnimations huskHornheadAnimations,
                         CrystalGuardianAnimations crystalGuardianAnimations,
                         FalseKnightAnimations falseKnightAnimations) {
        this.mosscreepAnimations = mosscreepAnimations;
        this.wingedSentryAnimations = wingedSentryAnimations;
        this.huskHornheadAnimations = huskHornheadAnimations;
        this.crystalGuardianAnimations = crystalGuardianAnimations;
        this.falseKnightAnimations = falseKnightAnimations;
    }

    public void renderMosscreep(Batch batch, Mosscreep enemy, float delta) {
        if (enemy == null || mosscreepAnimations == null) {
            return;
        }

        TextureRegion frame;
        switch (enemy.getState()) {
            case TURN:
                frame = mosscreepAnimations.getTurn().getKeyFrame(enemy.getStateTime(), false);
                break;
            case DEAD:
                frame = mosscreepAnimations.getDeath().getKeyFrame(enemy.getStateTime(), false);
                break;
            case HIDDEN:
                return;
            case WALK:
            default:
                frame = mosscreepAnimations.getWalk().getKeyFrame(enemy.getStateTime(), true);
                break;
        }

        if (frame == null) {
            return;
        }

        TextureRegion draw = new TextureRegion(frame);
        if (enemy.isFacingRight()) {
            draw.flip(true, false);
        }

        float x = enemy.getBody().getPosition().x - 0.4f;
        float y = enemy.getBody().getPosition().y - 0.45f;

        batch.draw(draw, x, y, 1.2f, 1.1f);
    }

    public void renderWingedSentry(Batch batch, WingedSentry enemy, float delta) {
        if (enemy == null || wingedSentryAnimations == null) {
            return;
        }

        TextureRegion frame;
        switch (enemy.getState()) {
            case TurnToFly:
                frame = wingedSentryAnimations.getTurnToFly().getKeyFrame(enemy.getStateTime(), false);
                break;
            case ChargeAntic:
                frame = wingedSentryAnimations.getChargeAntic().getKeyFrame(enemy.getStateTime(), false);
                break;
            case ChargeHorizontal:
                frame = wingedSentryAnimations.getChargeHorizontal().getKeyFrame(enemy.getStateTime(), true);
                break;
            case DeathAir:
                frame = wingedSentryAnimations.getDeathAir().getKeyFrame(enemy.getStateTime(), false);
                break;
            case DeathLand:
                frame = wingedSentryAnimations.getDeathLand().getKeyFrame(enemy.getStateTime(), false);
                break;
            case Idle:
            default:
                frame = wingedSentryAnimations.getIdle().getKeyFrame(enemy.getStateTime(), true);
                break;
        }

        if (frame == null) {
            return;
        }

        TextureRegion draw = new TextureRegion(frame);
        if (enemy.isFacingRight()) {
            draw.flip(true, false);
        }

        batch.draw(draw, enemy.getDrawX(), enemy.getDrawY(), enemy.getDrawWidth(), enemy.getDrawHeight());
    }

    public void renderHuskHornhead(Batch batch, HuskHornhead husk) {
        if (husk == null || huskHornheadAnimations == null) {
            return;
        }

        TextureRegion frame;
        switch (husk.getState()) {
            case Walk:
                frame = huskHornheadAnimations.getWalk().getKeyFrame(husk.getStateTimer(), true);
                break;
            case Turn:
                frame = huskHornheadAnimations.getTurn().getKeyFrame(husk.getStateTimer(), false);
                break;
            case AttackAnticipate:
                frame = huskHornheadAnimations.getAttackAnticipate().getKeyFrame(husk.getStateTimer(), false);
                break;
            case AttackLunge:
                frame = huskHornheadAnimations.getAttackLunge().getKeyFrame(husk.getStateTimer(), true);
                break;
            case Recover:
                frame = huskHornheadAnimations.getRecover().getKeyFrame(husk.getStateTimer(), false);
                break;
            case Death:
                frame = huskHornheadAnimations.getDeath().getKeyFrame(husk.getStateTimer(), false);
                break;
            default:
                frame = huskHornheadAnimations.getWalk().getKeyFrame(husk.getStateTimer(), true);
                break;
        }

        if (frame == null) {
            return;
        }

        TextureRegion draw = new TextureRegion(frame);
        if (husk.isFacingRight()) {
            draw.flip(true, false);
        }

        batch.draw(draw, husk.getDrawX(), husk.getDrawY(), husk.getDrawWidth(), husk.getDrawHeight());
    }

    public void renderFalseKnight(Batch batch, FalseKnight boss) {
        if (boss == null || falseKnightAnimations == null) {
            return;
        }

        TextureRegion frame;
        switch (boss.getState()) {
            case Turn:
                frame = falseKnightAnimations.getTurn().getKeyFrame(boss.getStateTime(), false);
                break;
            case RunAntic:
            case Run:
                frame = falseKnightAnimations.getRun().getKeyFrame(boss.getStateTime(), true);
                break;
            case AttackAntic:
                frame = falseKnightAnimations.getAttackAntic().getKeyFrame(boss.getStateTime(), false);
                break;
            case Attack:
            case AttackRecover:
                frame = falseKnightAnimations.getAttack().getKeyFrame(boss.getStateTime(), false);
                break;
            case Jump:
                frame = falseKnightAnimations.getJump().getKeyFrame(boss.getStateTime(), false);
                break;
            case JumpAttack:
                frame = falseKnightAnimations.getJumpAttack().getKeyFrame(boss.getStateTime(), false);
                break;
            case JumpAttackHit:
                frame = falseKnightAnimations.getJumpAttackHit().getKeyFrame(boss.getStateTime(), false);
                break;
            case Land:
                frame = falseKnightAnimations.getLand().getKeyFrame(boss.getStateTime(), false);
                break;
            case Body:
                frame = falseKnightAnimations.getBody().getKeyFrame(boss.getStateTime(), true);
                break;
            case StunRecover:
                frame = falseKnightAnimations.getStunRecover().getKeyFrame(boss.getStateTime(), false);
                break;
            case DeathFall:
                frame = falseKnightAnimations.getDeathFall().getKeyFrame(boss.getStateTime(), false);
                break;
            case DeathHit:
                frame = falseKnightAnimations.getDeathHit().getKeyFrame(boss.getStateTime(), false);
                break;
            case DeathLand:
                frame = falseKnightAnimations.getDeathLand().getKeyFrame(boss.getStateTime(), false);
                break;
            case DeathCorpse:
                if (falseKnightAnimations.getDeathCorpse().getKeyFrames().length > 0) {
                    frame = falseKnightAnimations.getDeathCorpse().getKeyFrame(boss.getStateTime(), false);
                } else {
                    frame = falseKnightAnimations.getDeathLand().getKeyFrame(boss.getStateTime(), false);
                }
                break;
            case Idle:
            default:
                frame = falseKnightAnimations.getIdle().getKeyFrame(boss.getStateTime(), true);
                break;
        }

        if (frame == null) {
            return;
        }

        TextureRegion draw = new TextureRegion(frame);
        if (boss.isFacingRight()) {
            draw.flip(true, false);
        }

        if (boss.getState() == Model.Enums.FalseKnightState.DeathCorpse) {
            batch.draw(draw, boss.getCorpseDrawX(), boss.getCorpseDrawY(), boss.getCorpseDrawWidth(), boss.getCorpseDrawHeight());
        } else {
            batch.draw(draw, boss.getDrawX(), boss.getDrawY(), boss.getDrawWidth(), boss.getDrawHeight());
        }
    }

    public void renderCrystalGuardian(Batch batch, CrystalGuardian guardian) {
        if (guardian == null || crystalGuardianAnimations == null) {
            return;
        }

        TextureRegion frame;
        switch (guardian.getState()) {
            case Turn:
                frame = crystalGuardianAnimations.getTurn().getKeyFrame(guardian.getStateTime(), false);
                break;
            case Shoot:
                frame = crystalGuardianAnimations.getShoot().getKeyFrame(guardian.getStateTime(), false);
                break;
            case Enraged:
                frame = crystalGuardianAnimations.getEnraged().getKeyFrame(guardian.getStateTime(), false);
                break;
            case Run:
                frame = crystalGuardianAnimations.getRun().getKeyFrame(guardian.getStateTime(), true);
                break;
            case DeathAir:
                frame = crystalGuardianAnimations.getDeathAir().getKeyFrame(guardian.getStateTime(), false);
                break;
            case DeathLand:
                frame = crystalGuardianAnimations.getDeathLand().getKeyFrame(guardian.getStateTime(), false);
                break;
            case Idle:
            default:
                frame = crystalGuardianAnimations.getIdle().getKeyFrame(guardian.getStateTime(), true);
                break;
        }

        if (frame == null) {
            return;
        }

        TextureRegion drawFrame = new TextureRegion(frame);
        if (guardian.isFacingRight()) {
            drawFrame.flip(true, false);
        }

        batch.draw(
            drawFrame,
            guardian.getBody().getPosition().x - CrystalGuardian.DRAW_WIDTH / 2f,
            guardian.getBody().getPosition().y - CrystalGuardian.DRAW_HEIGHT / 2f,
            CrystalGuardian.DRAW_WIDTH,
            CrystalGuardian.DRAW_HEIGHT
        );
    }

    public void renderCrystalLasers(Batch batch, ArrayList<CrystalLaser> crystalLasers) {
        if (crystalLasers == null) {
            return;
        }

        for (CrystalLaser laser : crystalLasers) {
            if (laser != null) {
                laser.render(batch);
            }
        }
    }
}
