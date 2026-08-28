package Controller;

import Model.CrystalLaser;
import Model.Enums.KnightState;
import Model.Knight;
import Model.Mosscreep;
import View.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class CombatVfxController {
    private final ArrayList<SlashEffect> slashEffects = new ArrayList<>();
    private final ArrayList<UpSlashEffect> upSlashEffects = new ArrayList<>();
    private final ArrayList<DashEffect> dashEffects = new ArrayList<>();
    private final ArrayList<JumpWaveEffect> jumpWaveEffects = new ArrayList<>();
    private final ArrayList<BloodSplashEffect> bloodSplashEffects = new ArrayList<>();
    private final ArrayList<CrystalLaser> crystalLasers;

    private final Animation<TextureRegion> slashEffectAnimation;
    private final Animation<TextureRegion> dashEffectAnimation;
    private final Animation<TextureRegion> jumpWaveAnimation;
    private final Animation<TextureRegion> bloodSplashAnimation;

    private int lastSlashSequenceSpawned = -1;
    private int lastUpSlashSequenceSpawned = -1;
    private int lastDashSequenceSpawned = -1;
    private int lastJumpWaveSequenceSpawned = -1;

    public CombatVfxController(Animation<TextureRegion> slashEffectAnimation, Animation<TextureRegion> dashEffectAnimation,
                               Animation<TextureRegion> jumpWaveAnimation, Animation<TextureRegion> bloodSplashAnimation,
                               ArrayList<CrystalLaser> crystalLasers) {
        this.slashEffectAnimation = slashEffectAnimation;
        this.dashEffectAnimation = dashEffectAnimation;
        this.jumpWaveAnimation = jumpWaveAnimation;
        this.bloodSplashAnimation = bloodSplashAnimation;
        this.crystalLasers = crystalLasers;
    }

    public void update(float delta, Knight knight, MosscreepController mosscreepController, ArrayList<Mosscreep> mosscreeps) {
        handleSlashSpawn(knight);
        updateSlashEffects(delta);
        handleSlashHits(knight, mosscreepController, mosscreeps);
        handleUpSlashSpawn(knight);
        updateUpSlashEffects(delta);
        handleUpSlashHits(knight, mosscreepController, mosscreeps);
        handleDashEffectSpawn(knight);
        updateDashEffects(delta);
        handleJumpWaveSpawn(knight);
        updateJumpWaveEffects(delta);
        updateBloodSplashEffects(delta);
        updateCrystalLasers(delta, knight);
    }

    public void handleSlashSpawn(Knight knight) {
        if (!knight.isAttacking() || knight.getState() == KnightState.DOWNSLASH
            || knight.getState() == KnightState.UPSLASH) {
            lastSlashSequenceSpawned = -1;
            return;
        }

        int seq = knight.getAttackSequence();
        if (seq == lastSlashSequenceSpawned) {
            return;
        }

        lastSlashSequenceSpawned = seq;

        Vector2 pos = knight.getBody().getPosition();
        boolean rightDirection = knight.getJahat() > 0f;

        float effectX = rightDirection ? pos.x + 0.25f : pos.x - 0.95f;
        float effectY = pos.y - 0.65f;

        slashEffects.add(new SlashEffect(slashEffectAnimation, effectX, effectY, rightDirection, 2.15f, 1.75f, 0f,
            0f
        ));
    }

    public void updateSlashEffects(float delta) {
        for (int i = slashEffects.size() - 1; i >= 0; i--) {
            SlashEffect effect = slashEffects.get(i);
            effect.update(delta);
            if (effect.isFinished()) {
                slashEffects.remove(i);
            }
        }
    }

    public void handleSlashHits(Knight knight, MosscreepController mosscreepController, ArrayList<Mosscreep> mosscreeps) {
        if (slashEffects.isEmpty() || mosscreeps.isEmpty()) {
            return;
        }

        for (SlashEffect effect : slashEffects) {
            Rectangle hitBox = effect.getHitBox();

            for (Mosscreep enemy : mosscreeps) {
                if (enemy == null || enemy.isDead()) {
                    continue;
                }

                if (hitBox.overlaps(enemy.getBounds())) {
                    mosscreepController.damaged(knight.getBody().getPosition(), enemy);
                }
            }
        }
    }

    public void handleUpSlashSpawn(Knight knight) {
        if (knight == null || knight.getState() != KnightState.UPSLASH) {
            lastUpSlashSequenceSpawned = -1;
            return;
        }

        int seq = knight.getAttackSequence();
        if (seq == lastUpSlashSequenceSpawned) {
            return;
        }

        lastUpSlashSequenceSpawned = seq;

        Vector2 pos = knight.getBody().getPosition();

        float effectX = pos.x;
        float effectY = pos.y + 0.35f;

        upSlashEffects.add(new UpSlashEffect(slashEffectAnimation, effectX, effectY, 2.15f, 1.75f));
    }

    public void updateUpSlashEffects(float delta) {
        for (int i = upSlashEffects.size() - 1; i >= 0; i--) {
            UpSlashEffect effect = upSlashEffects.get(i);
            effect.update(delta);
            if (effect.isFinished()) {
                upSlashEffects.remove(i);
            }
        }
    }

    public void handleUpSlashHits(Knight knight, MosscreepController mosscreepController, ArrayList<Mosscreep> mosscreeps) {
        if (upSlashEffects.isEmpty() || mosscreeps.isEmpty()) {
            return;
        }

        for (UpSlashEffect effect : upSlashEffects) {
            Rectangle hitBox = effect.getHitBox();

            for (Mosscreep enemy : mosscreeps) {
                if (enemy == null || enemy.isDead()) {
                    continue;
                }

                if (hitBox.overlaps(enemy.getBounds())) {
                    mosscreepController.damaged(knight.getBody().getPosition(), enemy);
                }
            }
        }
    }

    public void renderUpSlashEffects(Batch batch) {
        for (UpSlashEffect effect : upSlashEffects) {
            effect.render(batch);
        }
    }

    public ArrayList<UpSlashEffect> getUpSlashEffects() {
        return upSlashEffects;
    }

    public void handleDashEffectSpawn(Knight knight) {
        if (!knight.isDashing()) {
            lastDashSequenceSpawned = -1;
            return;
        }

        int seq = knight.getDashSequence();
        if (seq == lastDashSequenceSpawned) {
            return;
        }

        lastDashSequenceSpawned = seq;

        Vector2 pos = knight.getBody().getPosition();
        boolean facingRight = knight.getFacing() > 0f;

        float effectX = facingRight ? pos.x - 0.85f : pos.x + 0.15f;
        float effectY = pos.y - 0.95f;

        dashEffects.add(
            new DashEffect(dashEffectAnimation, effectX, effectY, facingRight, 2.25f, 1.75f,
                knight.canDashThroughEnemies()
                    ? new com.badlogic.gdx.graphics.Color(0.55f, 0.25f, 0.85f, 0.85f)
                    : com.badlogic.gdx.graphics.Color.WHITE)
        );
    }

    public void handleJumpWaveSpawn(Knight knight) {
        if (knight == null || knight.getState() != KnightState.DOWNSLASH) {
            lastJumpWaveSequenceSpawned = -1;
            return;
        }

        int seq = knight.getAttackSequence();
        if (seq == lastJumpWaveSequenceSpawned) {
            return;
        }

        lastJumpWaveSequenceSpawned = seq;

        Vector2 pos = knight.getBody().getPosition();
        boolean rightDirection = knight.getFacing() > 0f;

        float waveWidth = 3.0f;
        float waveHeight = 1.15f;
        float waveX = pos.x - waveWidth / 2f;
        float waveY = pos.y - 0.75f;

        jumpWaveEffects.add(new JumpWaveEffect(
            jumpWaveAnimation, waveX, waveY, rightDirection, waveWidth, waveHeight, 10.5f, false, 0f
        ));
    }

    public void updateJumpWaveEffects(float delta) {
        for (int i = jumpWaveEffects.size() - 1; i >= 0; i--) {
            JumpWaveEffect effect = jumpWaveEffects.get(i);
            effect.update(delta);
            if (effect.isFinished()) {
                jumpWaveEffects.remove(i);
            }
        }
    }

    public void updateDashEffects(float delta) {
        for (int i = dashEffects.size() - 1; i >= 0; i--) {
            DashEffect effect = dashEffects.get(i);
            effect.update(delta);

            if (effect.isFinished()) {
                dashEffects.remove(i);
            }
        }
    }

    public void spawnBloodSplash(float x, float y) {
        if (bloodSplashAnimation == null || bloodSplashAnimation.getKeyFrames().length == 0) {
            return;
        }

        float width = 1.3f;
        float height = 1.3f;
        boolean flip = Math.random() < 0.5;

        bloodSplashEffects.add(new BloodSplashEffect(
            bloodSplashAnimation, x - width / 2f, y - height / 2f, width, height, flip
        ));
    }

    public void spawnBloodBurst(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float offsetX = (float) (Math.random() - 0.5) * 2.4f;
            float offsetY = (float) (Math.random() - 0.5) * 1.6f;
            spawnBloodSplash(x + offsetX, y + offsetY);
        }
    }

    public void updateBloodSplashEffects(float delta) {
        for (int i = bloodSplashEffects.size() - 1; i >= 0; i--) {
            BloodSplashEffect effect = bloodSplashEffects.get(i);
            effect.update(delta);
            if (effect.isFinished()) {
                bloodSplashEffects.remove(i);
            }
        }
    }

    public void renderBloodSplashEffects(Batch batch) {
        for (BloodSplashEffect effect : bloodSplashEffects) {
            effect.render(batch);
        }
    }

    public ArrayList<BloodSplashEffect> getBloodSplashEffects() {
        return bloodSplashEffects;
    }

    public void updateCrystalLasers(float delta, Knight knight) {
        Rectangle knightBounds = knight.getAttackBounds();

        for (int i = crystalLasers.size() - 1; i >= 0; i--) {
            CrystalLaser laser = crystalLasers.get(i);

            laser.update(delta);

            if (laser.canDamage()
                && laser.getBounds().overlaps(knightBounds)) {

                knight.takeDamage(1, null, 0f);
                laser.resetDamageCooldown();
            }

            if (laser.isRemovePending()) {
                crystalLasers.remove(i);
            }
        }
    }

    public void renderSlashEffects(Batch batch) {
        for (SlashEffect effect : slashEffects) {
            effect.render(batch);
        }
    }

    public void renderDashEffects(Batch batch) {
        for (DashEffect effect : dashEffects) {
            effect.render(batch);
        }
    }

    public void renderJumpWaveEffects(Batch batch) {
        for (JumpWaveEffect effect : jumpWaveEffects) {
            effect.render(batch);
        }
    }

    public ArrayList<CrystalLaser> getCrystalLasers() {
        return crystalLasers;
    }

    public ArrayList<SlashEffect> getSlashEffects() {
        return slashEffects;
    }

    public ArrayList<DashEffect> getDashEffects() {
        return dashEffects;
    }

    public ArrayList<JumpWaveEffect> getJumpWaveEffects() {
        return jumpWaveEffects;
    }
}
