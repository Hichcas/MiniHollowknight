package Controller;

import Model.*;
import Model.Enums.FalseKnightState;
import Model.Enums.KnightState;
import View.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.*;

public class EnemyManager {
    private static final float TILE_SIZE = 16f;

    private final World world;

    private final Set<Body> frozenCorpseBodies = Collections.newSetFromMap(new IdentityHashMap<>());
    private AudioManager audioManager;

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    private final MosscreepAnimations mosscreepAnimations;
    private final WingedSentryAnimations wingedSentryAnimations;
    private final HuskHornheadAnimations huskHornheadAnimations;
    private final CrystalGuardianAnimations crystalGuardianAnimations;
    private final FalseKnightAnimations falseKnightAnimations;
    private final Texture crystalLaserTexture;
    private final ArrayList<Mosscreep> mosscreeps = new ArrayList<>();
    private final ArrayList<WingedSentry> wingedSentries = new ArrayList<>();
    private final ArrayList<HuskHornhead> huskHornheads = new ArrayList<>();
    private final ArrayList<CrystalGuardian> crystalGuardians = new ArrayList<>();
    private final ArrayList<CrystalLaser> crystalLasers = new ArrayList<>();
    private final ArrayList<FalseKnight> falseKnights = new ArrayList<>();
    private final ArrayList<JumpWaveEffect> falseKnightJumpWaveEffects = new ArrayList<>();
    private final ArrayList<Body> bossGateBodies = new ArrayList<>();
    private final ArrayList<Rectangle> bossGateRects = new ArrayList<>();
    private final Animation<TextureRegion> bossGateCloseAnimation;
    private final Animation<TextureRegion> bossGateOpenAnimation;
    private static final float BOSS_GATE_TRANSITION_DURATION = 0.56f;
    private float bossGateTransitionTime = BOSS_GATE_TRANSITION_DURATION;
    private final MosscreepController mosscreepController = new MosscreepController();
    private final WingedSentryController wingedSentryController = new WingedSentryController();
    private final HuskHornheadController huskHornheadController = new HuskHornheadController();
    private final CrystalGuardianController crystalGuardianController;
    private final FalseKnightController falseKnightController;
    private CombatVfxController combatVfxController;
    private final Animation<TextureRegion> falseKnightJumpWaveAnimation;
    private final Rectangle falseKnightArenaBounds = new Rectangle();
    private boolean bossArenaLocked = false;

    public EnemyManager(World world,
                        MosscreepAnimations mosscreepAnimations,
                        WingedSentryAnimations wingedSentryAnimations,
                        HuskHornheadAnimations huskHornheadAnimations,
                        CrystalGuardianAnimations crystalGuardianAnimations,
                        FalseKnightAnimations falseKnightAnimations,
                        Animation<TextureRegion> falseKnightJumpWaveAnimation,
                        Texture crystalLaserTexture) {
        this.world = world;
        this.mosscreepAnimations = mosscreepAnimations;
        this.wingedSentryAnimations = wingedSentryAnimations;
        this.huskHornheadAnimations = huskHornheadAnimations;
        this.crystalGuardianAnimations = crystalGuardianAnimations;
        this.falseKnightAnimations = falseKnightAnimations;
        this.falseKnightJumpWaveAnimation = falseKnightJumpWaveAnimation;
        this.crystalLaserTexture = crystalLaserTexture;
        this.bossGateCloseAnimation = loadAnimation("BossGate/Close", 0.08f, false);
        this.bossGateOpenAnimation = loadAnimation("BossGate/Open", 0.08f, false);

        this.falseKnightController = new FalseKnightController();

        this.crystalGuardianController = new CrystalGuardianController((x, y, faceRight) -> {
            if (this.crystalLaserTexture != null) {
                crystalLasers.add(new CrystalLaser(this.crystalLaserTexture, x, y, faceRight));
            }
        });
    }

    public void setCombatVfxController(CombatVfxController combatVfxController) {
        this.combatVfxController = combatVfxController;
    }

    public void spawnFromMap(TiledMap map) {
        spawnMosscreepsFromMap(map);
        spawnWingedSentriesFromMap(map);
        spawnHuskHornheadsFromMap(map);
        spawnCrystalGuardiansFromMap(map);
        spawnFalseKnightsFromMap(map);
        spawnBossGatesFromMap(map);
    }

    private boolean falseKnightRunningThisFrame = false;
    private boolean falseKnightSlamPulsePending = false;

    public boolean isFalseKnightRunning() {
        return falseKnightRunningThisFrame;
    }

    public boolean consumeFalseKnightSlamPulse() {
        if (!falseKnightSlamPulsePending) {
            return false;
        }
        falseKnightSlamPulsePending = false;
        return true;
    }

    public void update(float delta, Knight knight) {
        if (knight == null || knight.isDead()) {
            return;
        }

        Vector2 knightPos = knight.getBody().getPosition();

        for (Mosscreep mosscreep : mosscreeps) {
            mosscreepController.update(delta, knightPos, mosscreep);
        }

        for (WingedSentry sentry : wingedSentries) {
            wingedSentryController.update(delta, knightPos, sentry);
        }

        for (HuskHornhead husk : huskHornheads) {
            huskHornheadController.update(delta, knightPos, husk);
        }

        falseKnightRunningThisFrame = false;
        for (FalseKnight falseKnight : falseKnights) {
            falseKnightController.update(delta, knightPos, falseKnight);
            if (falseKnightController.consumeJumpAttackShockwaveSpawn(falseKnight)) {
                spawnFalseKnightJumpWave(falseKnight, knightPos);
                falseKnightSlamPulsePending = true;
            }
            if (!falseKnight.isDead() && !falseKnight.isRemovePending()
                && falseKnight.getState() == FalseKnightState.Run) {
                falseKnightRunningThisFrame = true;
            }
        }

        updateFalseKnightJumpWaveEffects(delta, knight);
        handleFalseKnightAttackHits(knight);
        updateBossArenaState(knight);
        enforceBossGateForFlyers();

        for (CrystalGuardian guardian : crystalGuardians) {
            guardian.setStateTime(guardian.getStateTime() + delta);
            guardian.setAttackCooldown(Math.max(0f, guardian.getAttackCooldown() - delta));
            crystalGuardianController.update(delta, knightPos, guardian);
        }

        updateCrystalLasers(delta, knight);
    }

    private int lastDashSequenceHandled = -1;
    private final java.util.Set<Object> dashHitEnemies = new java.util.HashSet<>();

    public void handleDashThroughHits(Knight knight) {
        if (knight == null || knight.isDead() || !knight.isDashing() || !knight.canDashThroughEnemies()) {
            return;
        }

        if (knight.getDashSequence() != lastDashSequenceHandled) {
            lastDashSequenceHandled = knight.getDashSequence();
            dashHitEnemies.clear();
        }

        Rectangle hitBox = knight.getBodyBounds();
        Vector2 knightPos = knight.getBody().getPosition();
        int amount = Math.max(1, Math.round(knight.getDashDamage()));

        for (Mosscreep enemy : mosscreeps) {
            if (enemy == null || enemy.isDead() || enemy.shouldRemove() || dashHitEnemies.contains(enemy)) {
                continue;
            }
            if (hitBox.overlaps(enemy.getBounds())) {
                dashHitEnemies.add(enemy);
                if (mosscreepController.damaged(knightPos, enemy, amount, 1f)) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                }
            }
        }

        for (WingedSentry enemy : wingedSentries) {
            if (enemy == null || !enemy.isAlive() || dashHitEnemies.contains(enemy)) {
                continue;
            }
            if (hitBox.overlaps(enemy.getBounds())) {
                dashHitEnemies.add(enemy);
                if (wingedSentryController.damage(knightPos, enemy, amount, 1f)) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                }
            }
        }

        for (HuskHornhead husk : huskHornheads) {
            if (husk == null || !husk.isAlive() || dashHitEnemies.contains(husk)) {
                continue;
            }
            if (hitBox.overlaps(husk.getBounds())) {
                dashHitEnemies.add(husk);
                if (huskHornheadController.damaged(knightPos, husk, amount, 1f)) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                }
            }
        }

        for (CrystalGuardian guardian : crystalGuardians) {
            if (guardian == null || guardian.isDead() || guardian.isRemovePending() || dashHitEnemies.contains(guardian)) {
                continue;
            }
            if (hitBox.overlaps(guardian.getBounds())) {
                dashHitEnemies.add(guardian);
                if (crystalGuardianController.takeDamage(guardian, amount)) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                }
            }
        }

        for (FalseKnight falseKnight : falseKnights) {
            if (falseKnight == null || falseKnight.isDead() || falseKnight.isRemovePending()
                || dashHitEnemies.contains(falseKnight)) {
                continue;
            }
            if (!hitBox.overlaps(falseKnight.getBounds())) {
                continue;
            }
            dashHitEnemies.add(falseKnight);
            boolean hitLanded;
            if (falseKnight.getState() == FalseKnightState.Body) {
                hitLanded = falseKnightController.onHitDuringStun(knightPos, falseKnight);
            } else {
                hitLanded = falseKnightController.takeDamage(falseKnight, amount, knightPos, 0f);
            }
            if (hitLanded) {
                knight.gainSoul(knight.getSoulGainPerHit());
                spawnBossHitBlood(falseKnight);
            }
        }
    }

    public void handleKnightAttackHits(Knight knight) {
        if (knight == null || knight.isDead() || !knight.isAttacking()) {
            return;
        }

        Rectangle attackBox = knight.getAttackBounds();
        Vector2 knightPos = knight.getBody().getPosition();
        int amount = Math.max(1, Math.round(knight.getAttackDamage()));
        float knockback = knight.getKnockbackMultiplier();
        boolean isPogoAttack = knight.getState() == KnightState.DOWNSLASH;
        boolean anyHitLanded = false;

        for (Mosscreep enemy : mosscreeps) {
            if (enemy == null || enemy.isDead() || enemy.shouldRemove()) {
                continue;
            }
            if (attackBox.overlaps(enemy.getBounds())) {
                if (mosscreepController.damaged(knightPos, enemy, amount, knockback)) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                    anyHitLanded = true;
                }
            }
        }

        for (WingedSentry enemy : wingedSentries) {
            if (enemy == null || !enemy.isAlive()) {
                continue;
            }
            if (attackBox.overlaps(enemy.getBounds())) {
                if (wingedSentryController.damage(knightPos, enemy, amount, knockback)) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                    anyHitLanded = true;
                }
            }
        }

        for (HuskHornhead husk : huskHornheads) {
            if (husk == null || !husk.isAlive()) {
                continue;
            }
            if (attackBox.overlaps(husk.getBounds())) {
                if (huskHornheadController.damaged(knightPos, husk, amount, knockback)) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                    anyHitLanded = true;
                }
            }
        }

        for (CrystalGuardian guardian : crystalGuardians) {
            if (guardian == null || guardian.isDead() || guardian.isRemovePending()) {
                continue;
            }
            if (attackBox.overlaps(guardian.getBounds())) {
                if (crystalGuardianController.takeDamage(guardian, amount)) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                    anyHitLanded = true;
                }
            }
        }
        for (FalseKnight falseKnight : falseKnights) {
            if (falseKnight == null || falseKnight.isDead() || falseKnight.isRemovePending()) {
                continue;
            }
            if (!attackBox.overlaps(falseKnight.getBounds())) {
                continue;
            }

            boolean hitLanded;
            if (falseKnight.getState() == FalseKnightState.Body) {
                hitLanded = falseKnightController.onHitDuringStun(knightPos, falseKnight);
            } else {
                hitLanded = falseKnightController.takeDamage(falseKnight, amount, knightPos, 0f, knockback);
            }

            if (hitLanded) {
                knight.gainSoul(knight.getSoulGainPerHit());
                spawnBossHitBlood(falseKnight);
                anyHitLanded = true;
            }
        }

        if (anyHitLanded && audioManager != null) {
            audioManager.playSfx("sfx/damage.wav");
        }

        if (isPogoAttack && anyHitLanded) {
            knight.performPogo();
        }
    }

    public void handleKnightJumpWaveHits(Knight knight, ArrayList<JumpWaveEffect> jumpWaveEffects) {
        if (knight == null || knight.isDead() || jumpWaveEffects == null || jumpWaveEffects.isEmpty()) {
            return;
        }

        Vector2 knightPos = knight.getBody().getPosition();

        for (int i = jumpWaveEffects.size() - 1; i >= 0; i--) {
            JumpWaveEffect wave = jumpWaveEffects.get(i);
            Rectangle hitBox = wave.getHitBox();
            boolean hitSomething = false;

            for (Mosscreep enemy : mosscreeps) {
                if (enemy == null || enemy.isDead() || enemy.shouldRemove()) {
                    continue;
                }
                if (hitBox.overlaps(enemy.getBounds())) {
                    if (mosscreepController.damaged(knightPos, enemy)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                    }
                    hitSomething = true;
                }
            }

            for (WingedSentry enemy : wingedSentries) {
                if (enemy == null || !enemy.isAlive()) {
                    continue;
                }
                if (hitBox.overlaps(enemy.getBounds())) {
                    if (wingedSentryController.damage(knightPos, enemy)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                    }
                    hitSomething = true;
                }
            }

            for (HuskHornhead husk : huskHornheads) {
                if (husk == null || !husk.isAlive()) {
                    continue;
                }
                if (hitBox.overlaps(husk.getBounds())) {
                    if (huskHornheadController.damaged(knightPos, husk)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                    }
                    hitSomething = true;
                }
            }

            for (CrystalGuardian guardian : crystalGuardians) {
                if (guardian == null || guardian.isDead() || guardian.isRemovePending()) {
                    continue;
                }
                if (hitBox.overlaps(guardian.getBounds())) {
                    if (crystalGuardianController.takeDamage(guardian)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                    }
                    hitSomething = true;
                }
            }

            for (FalseKnight falseKnight : falseKnights) {
                if (falseKnight == null || falseKnight.isDead() || falseKnight.isRemovePending()) {
                    continue;
                }
                if (!hitBox.overlaps(falseKnight.getBounds())) {
                    continue;
                }

                boolean hitLanded;
                if (falseKnight.getState() == FalseKnightState.Body) {
                    hitLanded = falseKnightController.onHitDuringStun(knightPos, falseKnight);
                } else {
                    hitLanded = falseKnightController.takeDamage(falseKnight, 1, knightPos, 0f);
                }

                if (hitLanded) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                    spawnBossHitBlood(falseKnight);
                }
                hitSomething = true;
            }

            if (hitSomething) {
                wave.finish();
                if (knight.getState() == KnightState.DOWNSLASH) {
                    knight.performPogo();
                }
            }
        }
    }

    public boolean handleVengefulSpiritHits(Knight knight, ArrayList<VengefulSpiritBolt> bolts) {
        if (knight == null || knight.isDead() || bolts == null || bolts.isEmpty()) {
            return false;
        }

        boolean anyHitLanded = false;
        Vector2 knightPos = knight.getBody().getPosition();

        for (VengefulSpiritBolt bolt : bolts) {
            Rectangle hitBox = bolt.getBounds();

            for (Mosscreep enemy : mosscreeps) {
                if (enemy == null || enemy.isDead() || enemy.shouldRemove() || bolt.hasHit(enemy)) {
                    continue;
                }
                if (hitBox.overlaps(enemy.getBounds())) {
                    bolt.markHit(enemy);
                    if (mosscreepController.damaged(knightPos, enemy)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                        anyHitLanded = true;
                    }
                }
            }

            for (WingedSentry enemy : wingedSentries) {
                if (enemy == null || !enemy.isAlive() || bolt.hasHit(enemy)) {
                    continue;
                }
                if (hitBox.overlaps(enemy.getBounds())) {
                    bolt.markHit(enemy);
                    if (wingedSentryController.damage(knightPos, enemy)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                        anyHitLanded = true;
                    }
                }
            }

            for (HuskHornhead husk : huskHornheads) {
                if (husk == null || !husk.isAlive() || bolt.hasHit(husk)) {
                    continue;
                }
                if (hitBox.overlaps(husk.getBounds())) {
                    bolt.markHit(husk);
                    if (huskHornheadController.damaged(knightPos, husk)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                        anyHitLanded = true;
                    }
                }
            }

            for (CrystalGuardian guardian : crystalGuardians) {
                if (guardian == null || guardian.isDead() || guardian.isRemovePending() || bolt.hasHit(guardian)) {
                    continue;
                }
                if (hitBox.overlaps(guardian.getBounds())) {
                    bolt.markHit(guardian);
                    if (crystalGuardianController.takeDamage(guardian)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                        anyHitLanded = true;
                    }
                }
            }

            for (FalseKnight falseKnight : falseKnights) {
                if (falseKnight == null || falseKnight.isDead() || falseKnight.isRemovePending()
                    || bolt.hasHit(falseKnight)) {
                    continue;
                }
                if (!hitBox.overlaps(falseKnight.getBounds())) {
                    continue;
                }

                bolt.markHit(falseKnight);
                int amount = Math.max(1, Math.round(bolt.getDamage()));

                boolean hitLanded;
                if (falseKnight.getState() == FalseKnightState.Body) {
                    hitLanded = falseKnightController.onHitDuringStun(knightPos, falseKnight);
                } else {
                    hitLanded = falseKnightController.takeDamage(falseKnight, amount, knightPos, 0f);
                }

                if (hitLanded) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                    spawnBossHitBlood(falseKnight);
                    anyHitLanded = true;
                }
            }
        }

        if (anyHitLanded && audioManager != null) {
            audioManager.playSfx("sfx/damage.wav");
        }

        return anyHitLanded;
    }

    public boolean handleHowlingWraithsHits(Knight knight, ArrayList<HowlingWraithsBurst> bursts) {
        if (knight == null || knight.isDead() || bursts == null || bursts.isEmpty()) {
            return false;
        }

        boolean anyHitLanded = false;
        Vector2 knightPos = knight.getBody().getPosition();

        for (HowlingWraithsBurst burst : bursts) {
            if (!burst.consumeTickIfReady()) {
                continue;
            }

            Rectangle hitBox = burst.getBounds();
            int amount = Math.max(1, Math.round(burst.getDamagePerTick()));

            for (Mosscreep enemy : mosscreeps) {
                if (enemy == null || enemy.isDead() || enemy.shouldRemove()) {
                    continue;
                }
                if (hitBox.overlaps(enemy.getBounds())) {
                    if (mosscreepController.damaged(knightPos, enemy)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                        anyHitLanded = true;
                    }
                }
            }

            for (WingedSentry enemy : wingedSentries) {
                if (enemy == null || !enemy.isAlive()) {
                    continue;
                }
                if (hitBox.overlaps(enemy.getBounds())) {
                    if (wingedSentryController.damage(knightPos, enemy)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                        anyHitLanded = true;
                    }
                }
            }

            for (HuskHornhead husk : huskHornheads) {
                if (husk == null || !husk.isAlive()) {
                    continue;
                }
                if (hitBox.overlaps(husk.getBounds())) {
                    if (huskHornheadController.damaged(knightPos, husk)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                        anyHitLanded = true;
                    }
                }
            }

            for (CrystalGuardian guardian : crystalGuardians) {
                if (guardian == null || guardian.isDead() || guardian.isRemovePending()) {
                    continue;
                }
                if (hitBox.overlaps(guardian.getBounds())) {
                    if (crystalGuardianController.takeDamage(guardian)) {
                        knight.gainSoul(knight.getSoulGainPerHit());
                        anyHitLanded = true;
                    }
                }
            }

            for (FalseKnight falseKnight : falseKnights) {
                if (falseKnight == null || falseKnight.isDead() || falseKnight.isRemovePending()) {
                    continue;
                }
                if (!hitBox.overlaps(falseKnight.getBounds())) {
                    continue;
                }

                boolean hitLanded;
                if (falseKnight.getState() == FalseKnightState.Body) {
                    hitLanded = falseKnightController.onHitDuringStun(knightPos, falseKnight);
                } else {
                    hitLanded = falseKnightController.takeDamage(falseKnight, amount, knightPos, 0f);
                }

                if (hitLanded) {
                    knight.gainSoul(knight.getSoulGainPerHit());
                    spawnBossHitBlood(falseKnight);
                    anyHitLanded = true;
                }
            }
        }

        if (anyHitLanded && audioManager != null) {
            audioManager.playSfx("sfx/damage.wav");
        }

        return anyHitLanded;
    }

    private void spawnBossHitBlood(FalseKnight falseKnight) {
        if (combatVfxController == null || falseKnight.getBody() == null) {
            return;
        }
        Vector2 bossPos = falseKnight.getBody().getPosition();
        if (falseKnight.isDead()) {
            combatVfxController.spawnBloodBurst(bossPos.x, bossPos.y, 30);
        } else {
            combatVfxController.spawnBloodSplash(bossPos.x, bossPos.y);
        }
    }

    private void handleFalseKnightAttackHits(Knight knight) {
        if (knight == null || knight.isDead()) return;

        Rectangle playerBounds = new Rectangle(
            knight.getBody().getPosition().x - knight.getKnightWidth() / 2f,
            knight.getBody().getPosition().y - knight.getKnightHeight() / 2f,
            knight.getKnightWidth(),
            knight.getKnightHeight()
        );

        Vector2 knightPos = knight.getBody().getPosition();

        for (FalseKnight boss : falseKnights) {
            if (boss == null || boss.isDead() || boss.isRemovePending()) continue;

            Rectangle mace = falseKnightController.getMaceHitBox(boss);

            if (mace.overlaps(playerBounds)) {
                knight.takeDamage(1, new Vector2(
                    Math.signum(knightPos.x - boss.getBody().getPosition().x),
                    0.25f
                ), 7f);
            }
        }
    }

    private void spawnFalseKnightJumpWave(FalseKnight boss, Vector2 knightPos) {
        if (boss == null || falseKnightJumpWaveAnimation == null || falseKnightJumpWaveAnimation.getKeyFrames().length == 0) {
            return;
        }

        Vector2 bossPos = boss.getBody().getPosition();
        boolean rightDirection = knightPos == null || knightPos.x >= bossPos.x;

        float bodyLeft = bossPos.x - FalseKnight.BODY_WIDTH / 2f;
        float bodyRight = bossPos.x + FalseKnight.BODY_WIDTH / 2f;
        float footY = bossPos.y - FalseKnight.BODY_HEIGHT / 2f + 0.1f;

        float width = boss.isPhaseTwo() ? 5f : 4f;
        float height = boss.isPhaseTwo() ? 3f : 2f;
        float speed = boss.isPhaseTwo() ? 8f : 6f;

        float x = rightDirection
            ? bodyRight - 0.02f
            : bodyLeft - width + 0.02f;

        falseKnightJumpWaveEffects.add(new JumpWaveEffect(
            falseKnightJumpWaveAnimation,
            x,
            footY,
            rightDirection,
            width,
            height,
            speed,
            true,
            width
        ));
    }

    private void updateFalseKnightJumpWaveEffects(float delta, Knight knight) {
        if (falseKnightJumpWaveEffects.isEmpty()) {
            return;
        }

        if (knight == null || knight.isDead()) {
            for (int i = falseKnightJumpWaveEffects.size() - 1; i >= 0; i--) {
                JumpWaveEffect wave = falseKnightJumpWaveEffects.get(i);
                wave.update(delta);
                if (wave.isFinished()) {
                    falseKnightJumpWaveEffects.remove(i);
                }
            }
            return;
        }

        Rectangle playerBounds = new Rectangle(
            knight.getBody().getPosition().x - knight.getKnightWidth() / 2f,
            knight.getBody().getPosition().y - knight.getKnightHeight() / 2f,
            knight.getKnightWidth(),
            knight.getKnightHeight()
        );
        Vector2 knightPos = knight.getBody().getPosition();

        for (int i = falseKnightJumpWaveEffects.size() - 1; i >= 0; i--) {
            JumpWaveEffect wave = falseKnightJumpWaveEffects.get(i);
            wave.update(delta);
            if (wave.getHitBox().overlaps(playerBounds)) {
                float knockDir = Math.signum(knightPos.x - (wave.getHitBox().x + wave.getHitBox().width / 2f));
                if (knockDir == 0f) {
                    knockDir = knightPos.x >= wave.getHitBox().x ? 1f : -1f;
                }
                knight.takeDamage(1, new Vector2(knockDir, 0.25f), 7f);
                wave.finish();
            }
            if (wave.isFinished()) {
                falseKnightJumpWaveEffects.remove(i);
            }
        }
    }

    private void renderFalseKnightJumpWaveEffects(Batch batch) {
        for (JumpWaveEffect effect : falseKnightJumpWaveEffects) {
            effect.render(batch);
        }
    }

    public void render(Batch batch, float delta) {
        renderMosscreeps(batch);
        renderWingedSentries(batch);
        renderHuskHornheads(batch);
        renderFalseKnights(batch);
        renderBossGates(batch, delta);
        renderFalseKnightJumpWaveEffects(batch);
        renderCrystalGuardians(batch);
        renderCrystalLasers(batch);
    }

    private void freezeCorpse(Body body) {
        if (body == null || frozenCorpseBodies.contains(body)) {
            return;
        }
        Array<Fixture> fixturesCopy = new Array<>(body.getFixtureList());

        for (Fixture fixture : fixturesCopy) {
            body.destroyFixture(fixture);
        }
        body.setLinearVelocity(0f, 0f);
        body.setAngularVelocity(0f);
        body.setType(BodyDef.BodyType.StaticBody);
        frozenCorpseBodies.add(body);
    }

    public void cleanupDestroyedBodies() {
        for (int i = mosscreeps.size() - 1; i >= 0; i--) {
            Mosscreep enemy = mosscreeps.get(i);
            if (enemy != null && enemy.shouldRemove()) {
                freezeCorpse(enemy.getBody());
            }
        }

        for (int i = wingedSentries.size() - 1; i >= 0; i--) {
            WingedSentry enemy = wingedSentries.get(i);
            if (enemy != null && enemy.isRemoved()) {
                freezeCorpse(enemy.getBody());
            }
        }

        for (int i = huskHornheads.size() - 1; i >= 0; i--) {
            HuskHornhead husk = huskHornheads.get(i);
            if (husk != null && husk.isRemovePending()) {
                freezeCorpse(husk.getBody());
            }
        }

        for (int i = falseKnights.size() - 1; i >= 0; i--) {
            FalseKnight boss = falseKnights.get(i);
            if (boss != null && boss.isRemovePending()) {
                world.destroyBody(boss.getBody());
                falseKnights.remove(i);
            }
        }

        for (int i = crystalGuardians.size() - 1; i >= 0; i--) {
            CrystalGuardian guardian = crystalGuardians.get(i);
            if (guardian != null && guardian.isRemovePending()) {
                freezeCorpse(guardian.getBody());
            }
        }

        for (int i = crystalLasers.size() - 1; i >= 0; i--) {
            CrystalLaser laser = crystalLasers.get(i);
            if (laser != null && laser.isRemovePending()) {
                crystalLasers.remove(i);
            }
        }
    }

    public ArrayList<Mosscreep> getMosscreeps() {
        return mosscreeps;
    }

    public ArrayList<WingedSentry> getWingedSentries() {
        return wingedSentries;
    }

    public ArrayList<HuskHornhead> getHuskHornheads() {
        return huskHornheads;
    }

    public ArrayList<CrystalGuardian> getCrystalGuardians() {
        return crystalGuardians;
    }

    public ArrayList<CrystalLaser> getCrystalLasers() {
        return crystalLasers;
    }

    public ArrayList<FalseKnight> getFalseKnights() {
        return falseKnights;
    }

    public void killAllEnemiesOnScreen(Knight knight) {
        if (knight == null) {
            return;
        }
        Vector2 knightPos = knight.getBody().getPosition();

        for (Mosscreep enemy : mosscreeps) {
            if (enemy == null || enemy.isDead() || enemy.shouldRemove()) continue;
            mosscreepController.damaged(knightPos, enemy, 9999, 0f);
        }
        for (WingedSentry enemy : wingedSentries) {
            if (enemy == null || !enemy.isAlive()) continue;
            wingedSentryController.damage(knightPos, enemy, 9999, 0f);
        }
        for (HuskHornhead husk : huskHornheads) {
            if (husk == null || !husk.isAlive()) continue;
            huskHornheadController.damaged(knightPos, husk, 9999, 0f);
        }
        for (CrystalGuardian guardian : crystalGuardians) {
            if (guardian == null || guardian.isDead() || guardian.isRemovePending()) continue;
            crystalGuardianController.takeDamage(guardian, 9999);
        }
        for (FalseKnight falseKnight : falseKnights) {
            if (falseKnight == null || falseKnight.isDead() || falseKnight.isRemovePending()) continue;
            falseKnightController.takeDamage(falseKnight, 9999, knightPos, 0f, 0f);
        }
    }

    private void enforceBossGateForFlyers() {
        if (!bossArenaLocked || falseKnightArenaBounds.width <= 0f) {
            return;
        }

        float left = falseKnightArenaBounds.x;
        float right = falseKnightArenaBounds.x + falseKnightArenaBounds.width;
        float margin = 0.35f;

        for (WingedSentry sentry : wingedSentries) {
            if (sentry == null || sentry.isRemovePending()) {
                continue;
            }
            Body body = sentry.getBody();
            if (body == null) {
                continue;
            }

            Vector2 pos = body.getPosition();
            Vector2 vel = body.getLinearVelocity();

            if (pos.x >= left && pos.x <= right) {
                float distToLeft = pos.x - left;
                float distToRight = right - pos.x;
                if (distToLeft <= distToRight) {
                    body.setTransform(left - margin, pos.y, 0f);
                    body.setLinearVelocity(Math.min(vel.x, 0f), vel.y);
                } else {
                    body.setTransform(right + margin, pos.y, 0f);
                    body.setLinearVelocity(Math.max(vel.x, 0f), vel.y);
                }
            } else if (pos.x < left && pos.x > left - margin && vel.x > 0f) {
                body.setTransform(left - margin, pos.y, 0f);
                body.setLinearVelocity(0f, vel.y);
            } else if (pos.x > right && pos.x < right + margin && vel.x < 0f) {
                body.setTransform(right + margin, pos.y, 0f);
                body.setLinearVelocity(0f, vel.y);
            }
        }
    }

    public Rectangle getFalseKnightArenaBounds() {
        return new Rectangle(falseKnightArenaBounds);
    }

    public void updateBossArenaState(Knight knight) {
        if (knight == null || falseKnightArenaBounds.width <= 0f || falseKnightArenaBounds.height <= 0f) {
            return;
        }

        boolean bossAlive = hasLivingFalseKnight();
        if (!bossAlive) {
            if (bossArenaLocked) {
                unlockBossArena();
            }
            return;
        }

        Vector2 knightPos = knight.getBody().getPosition();
        boolean knightInside = falseKnightArenaBounds.contains(knightPos.x, knightPos.y);
        if (!knightInside && bossArenaLocked) {
            unlockBossArena();
        }
        if (knightInside && !bossArenaLocked) {
            lockBossArena();
        } else if (!knightInside && !bossArenaLocked) {
            setBossGateBodiesActive(false);
        }
    }

    public void resetBossArena() {
        unlockBossArena();
    }

    private void lockBossArena() {
        bossArenaLocked = true;
        bossGateTransitionTime = 0f;
        setBossGateBodiesActive(true);
    }

    private void unlockBossArena() {
        bossArenaLocked = false;
        bossGateTransitionTime = 0f;
        setBossGateBodiesActive(false);
    }

    private void setBossGateBodiesActive(boolean active) {
        for (Body body : bossGateBodies) {
            if (body != null) {
                body.setActive(active);
            }
        }
    }

    private boolean hasLivingFalseKnight() {
        for (FalseKnight boss : falseKnights) {
            if (boss != null && !boss.isDead() && !boss.isRemovePending()) {
                return true;
            }
        }
        return false;
    }

    private Rectangle extractBossArenaBounds(TiledMap map) {
        Rectangle bounds = findRectangleObjectByName(map, "BossArenaBounds");
        if (bounds != null) {
            return bounds;
        }

        bounds = findRectangleObjectByName(map, "ArenaBounds");
        if (bounds != null) {
            return bounds;
        }

        return null;
    }

    private void spawnMosscreepsFromMap(TiledMap map) {
        MapLayer layer = map.getLayers().get("MosscreepSpawns");
        if (layer == null) {
            return;
        }

        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) {
                continue;
            }

            Rectangle rect = ((RectangleMapObject) obj).getRectangle();

            float x = (rect.x + rect.width / 2f) / TILE_SIZE;
            float y = (rect.y + rect.height / 2f) / TILE_SIZE;

            float left = x - 2.5f;
            float right = x + 2.5f;

            if (obj.getProperties().containsKey("leftBound")) {
                left = ((Number) obj.getProperties().get("leftBound")).floatValue() / TILE_SIZE;
            }
            if (obj.getProperties().containsKey("rightBound")) {
                right = ((Number) obj.getProperties().get("rightBound")).floatValue() / TILE_SIZE;
            }

            spawnMosscreep(x, y, left, right);
        }
    }

    private void spawnMosscreep(float x, float y, float left, float right) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.3f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("MOSSCREEP");
        shape.dispose();

        Mosscreep mosscreep = new Mosscreep(body, left, right);
        body.setUserData(mosscreep);
        mosscreeps.add(mosscreep);
    }

    private void spawnWingedSentriesFromMap(TiledMap map) {
        MapLayer layer = map.getLayers().get("WingedSentrySpawn");
        if (layer == null) {
            return;
        }

        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) {
                continue;
            }

            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            float x = (rect.x + rect.width / 2f) / TILE_SIZE;
            float y = (rect.y + rect.height / 2f) / TILE_SIZE;

            spawnWingedSentry(x, y);
        }
    }

    private void spawnWingedSentry(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.bullet = true;
        bodyDef.fixedRotation = true;
        bodyDef.allowSleep = false;

        Body body = world.createBody(bodyDef);
        body.setGravityScale(0f);
        body.setLinearDamping(0f);
        body.setAngularDamping(0f);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.7f, 0.45f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.12f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("WINGEDSENTRY");
        shape.dispose();

        WingedSentry enemy = new WingedSentry(body);
        body.setUserData(enemy);
        wingedSentries.add(enemy);
    }

    private void spawnHuskHornheadsFromMap(TiledMap map) {
        MapLayer layer = map.getLayers().get("HuskHornheadSpawns");
        if (layer == null) {
            return;
        }

        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) {
                continue;
            }

            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            float x = (rect.x + rect.width / 2f) / TILE_SIZE;
            float y = (rect.y + rect.height / 2f) / TILE_SIZE;

            float left = x - 2.5f;
            float right = x + 2.5f;

            if (obj.getProperties().containsKey("leftBound")) {
                left = ((Number) obj.getProperties().get("leftBound")).floatValue() / TILE_SIZE;
            }
            if (obj.getProperties().containsKey("rightBound")) {
                right = ((Number) obj.getProperties().get("rightBound")).floatValue() / TILE_SIZE;
            }

            spawnHuskHornhead(x, y, left, right);
        }
    }

    private void spawnHuskHornhead(float x, float y, float left, float right) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("HUSKHORNHEAD");
        shape.dispose();

        HuskHornhead husk = new HuskHornhead(body);
        husk.setLeftBound(left);
        husk.setRightBound(right);
        body.setUserData(husk);
        huskHornheads.add(husk);
    }

    private void spawnCrystalGuardiansFromMap(TiledMap map) {
        MapLayer layer = map.getLayers().get("CrystalGuardianSpawn");
        if (layer == null) {
            return;
        }

        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) {
                continue;
            }

            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            float x = (rect.x + rect.width / 2f) / TILE_SIZE;
            float y = (rect.y + rect.height / 2f) / TILE_SIZE;

            spawnCrystalGuardian(x, y);
        }
    }

    private void spawnFalseKnightsFromMap(TiledMap map) {
        falseKnightArenaBounds.set(0f, 0f, 0f, 0f);

        Rectangle bounds = extractBossArenaBounds(map);
        if (bounds != null) {
            Rectangle worldBounds = new Rectangle(
                bounds.x / TILE_SIZE,
                bounds.y / TILE_SIZE,
                bounds.width / TILE_SIZE,
                bounds.height / TILE_SIZE
            );
            falseKnightArenaBounds.set(worldBounds);
            falseKnightController.setArenaBounds(worldBounds);
        }

        MapLayer layer = map.getLayers().get("FalseKnightSpawn");
        if (layer == null) {
            return;
        }

        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) {
                continue;
            }

            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            float x = (rect.x + rect.width / 2f) / TILE_SIZE;
            float y = (rect.y + rect.height / 2f) / TILE_SIZE;

            spawnFalseKnight(x, y);
        }
    }

    private void spawnFalseKnight(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;
        bodyDef.bullet = true;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(FalseKnight.BODY_WIDTH / 2f, FalseKnight.BODY_HEIGHT / 2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 2f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("FALSE_KNIGHT");
        shape.dispose();

        FalseKnight boss = new FalseKnight(body);
        boss.setMAX_HEALTH(20);
        body.setUserData(boss);
        falseKnights.add(boss);
    }

    private void spawnBossGatesFromMap(TiledMap map) {
        spawnBossGatesFromLayer(map, "BossGateLeft");
        spawnBossGatesFromLayer(map, "BossGateRight");
        spawnBossGatesFromLayer(map, "BossGates");
        spawnBossGatesFromLayer(map, "BossGate");

    }

    private void spawnBossGatesFromLayer(TiledMap map, String layerName) {
        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) {
            return;
        }

        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) {
                continue;
            }

            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            spawnBossGate(rect);
        }
    }

    private void spawnBossGate(Rectangle rect) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(
            (rect.x + rect.width / 2f) / TILE_SIZE,
            (rect.y + rect.height / 2f) / TILE_SIZE
        );

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((rect.width / 2f) / TILE_SIZE, (rect.height / 2f) / TILE_SIZE);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("BOSS_GATE");
        shape.dispose();

        body.setUserData("BOSS_GATE");
        body.setActive(false);
        bossGateBodies.add(body);
        bossGateRects.add(new Rectangle(rect));
    }

    private void spawnCrystalGuardian(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1.1f, 0.75f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("CRYSTALGUARDIAN");
        shape.dispose();

        CrystalGuardian guardian = new CrystalGuardian(body);
        body.setUserData(guardian);
        crystalGuardians.add(guardian);
    }

    private void updateCrystalLasers(float delta, Knight knight) {
        Rectangle knightBounds = new Rectangle(
            knight.getBody().getPosition().x - knight.getKnightWidth() / 2f,
            knight.getBody().getPosition().y - knight.getKnightHeight() / 2f + 1f,
            knight.getKnightWidth(),
            knight.getKnightHeight()
        );

        for (int i = crystalLasers.size() - 1; i >= 0; i--) {
            CrystalLaser laser = crystalLasers.get(i);
            laser.update(delta);

            if (laser.canDamage() && laser.getBounds().overlaps(knightBounds)) {
                knight.takeDamage(1, null, 0f);
                laser.resetDamageCooldown();
            }

            if (laser.isRemovePending()) {
                crystalLasers.remove(i);
            }
        }
    }

    private void renderMosscreeps(Batch batch) {
        if (mosscreepAnimations == null) {
            return;
        }

        for (Mosscreep enemy : mosscreeps) {
            if (enemy == null) {
                continue;
            }

            TextureRegion frame;
            switch (enemy.getState()) {
                case TURN:
                    frame = mosscreepAnimations.getTurn().getKeyFrame(enemy.getStateTime(), false);
                    break;
                case DEAD:
                    frame = mosscreepAnimations.getDeath().getKeyFrame(enemy.getStateTime(), false);
                    break;
                case WALK:
                default:
                    frame = mosscreepAnimations.getWalk().getKeyFrame(enemy.getStateTime(), true);
                    break;
            }

            if (frame == null) {
                continue;
            }

            TextureRegion draw = new TextureRegion(frame);
            if (enemy.isFacingRight()) {
                draw.flip(true, false);
            }

            float x = enemy.getBody().getPosition().x - 0.4f;
            float y = enemy.getBody().getPosition().y - 0.45f;
            float blinkTimer = (enemy.isInvincible() ? enemy.getInvincibleTimer() : 0f);
            drawBlinking(batch, frame, enemy.getBody().getPosition().x - 0.4f, enemy.getBody().getPosition().y - 0.45f,
                1.2f, 1.1f, enemy.isFacingRight(), blinkTimer, enemy.getStateTime());
        }
    }

    private void renderWingedSentries(Batch batch) {
        if (wingedSentryAnimations == null) {
            return;
        }

        for (WingedSentry enemy : wingedSentries) {
            if (enemy == null) {
                continue;
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
                continue;
            }

            TextureRegion draw = new TextureRegion(frame);
            if (enemy.isFacingRight()) {
                draw.flip(true, false);
            }
            drawBlinking(batch, frame, enemy.getDrawX(), enemy.getDrawY(), enemy.getDrawWidth(), enemy.getDrawHeight(),
                enemy.isFacingRight(), enemy.getHurtCooldown(), enemy.getStateTime());
        }
    }

    private void renderHuskHornheads(Batch batch) {
        if (huskHornheadAnimations == null) {
            return;
        }

        for (HuskHornhead husk : huskHornheads) {
            if (husk == null) {
                continue;
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
                continue;
            }

            TextureRegion draw = new TextureRegion(frame);
            if (husk.isFacingRight()) {
                draw.flip(true, false);
            }
            drawBlinking(batch, frame, husk.getDrawX(), husk.getDrawY(), husk.getDrawWidth(), husk.getDrawHeight(),
                husk.isFacingRight(), husk.getHurtCooldown(), husk.getStateTimer());
        }
    }

    private void renderFalseKnights(Batch batch) {
        if (falseKnightAnimations == null) {
            return;
        }

        for (FalseKnight boss : falseKnights) {
            if (boss == null || boss.isRemovePending()) {
                continue;
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
                continue;
            }

            TextureRegion draw = new TextureRegion(frame);
            if (boss.isFacingRight()) {
                draw.flip(true, false);
            }
            drawBlinking(batch, frame, boss.getState() == FalseKnightState.DeathCorpse ? boss.getCorpseDrawX() : boss.getDrawX(),
                boss.getState() == FalseKnightState.DeathCorpse ? boss.getCorpseDrawY() : boss.getDrawY(),
                boss.getState() == FalseKnightState.DeathCorpse ? boss.getCorpseDrawWidth() : boss.getDrawWidth(),
                boss.getState() == FalseKnightState.DeathCorpse ? boss.getCorpseDrawHeight() : boss.getDrawHeight(),
                boss.isFacingRight(), boss.getHurtCooldown(), boss.getStateTime());
        }
    }

    private void renderCrystalGuardians(Batch batch) {
        if (crystalGuardianAnimations == null) {
            return;
        }

        for (CrystalGuardian guardian : crystalGuardians) {
            if (guardian == null) {
                continue;
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
                continue;
            }

            TextureRegion drawFrame = new TextureRegion(frame);
            if (guardian.isFacingRight()) {
                drawFrame.flip(true, false);
            }
            drawBlinking(batch, frame, guardian.getBody().getPosition().x - 1.5f,
                guardian.getBody().getPosition().y - 1.0f, 3f, 2f, guardian.isFacingRight(), guardian.getDamageCooldown(),
                guardian.getStateTime());
        }
    }

    private void renderCrystalLasers(Batch batch) {
        for (CrystalLaser laser : crystalLasers) {
            if (laser != null) {
                laser.render(batch);
            }
        }
    }

    private Rectangle findRectangleObjectByName(TiledMap map, String objectName) {
        for (MapLayer layer : map.getLayers()) {
            MapObject obj = layer.getObjects().get(objectName);
            if (obj instanceof RectangleMapObject) {
                return ((RectangleMapObject) obj).getRectangle();
            }
        }

        String normalizedTarget = objectName.replaceAll("\\s+", "").toLowerCase();
        for (MapLayer layer : map.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if (!(obj instanceof RectangleMapObject)) {
                    continue;
                }
                String name = obj.getName();
                if (name == null) {
                    continue;
                }
                if (name.replaceAll("\\s+", "").toLowerCase().equals(normalizedTarget)) {

                    return ((RectangleMapObject) obj).getRectangle();
                }
            }
        }

        for (MapLayer layer : map.getLayers()) {
            String layerName = layer.getName();
            if (layerName == null
                || !layerName.replaceAll("\\s+", "").toLowerCase().equals(normalizedTarget)) {
                continue;
            }

            RectangleMapObject onlyRectangle = null;
            int rectangleCount = 0;
            for (MapObject obj : layer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    onlyRectangle = (RectangleMapObject) obj;
                    rectangleCount++;
                }
            }

            if (rectangleCount == 1) {
                return onlyRectangle.getRectangle();
            }
        }

        return null;
    }

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration, boolean loop) {
        FileHandle folder = Gdx.files.internal(folderName);
        if (!folder.exists()) {
            return new Animation<>(frameDuration, new TextureRegion[0]);
        }

        FileHandle[] files = folder.list((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
        });
        Arrays.sort(files, Comparator.comparing(FileHandle::name));

        TextureRegion[] frames = new TextureRegion[files.length];
        for (int i = 0; i < files.length; i++) {
            Texture texture = new Texture(files[i]);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            frames[i] = new TextureRegion(texture);
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        return animation;
    }

    private TextureRegion getProgressFrame(Animation<TextureRegion> animation, float progress, boolean reverse) {
        if (animation == null) {
            return null;
        }

        TextureRegion[] frames = animation.getKeyFrames();
        if (frames == null || frames.length == 0) {
            return null;
        }

        float clamped = MathUtils.clamp(progress, 0f, 1f);
        int index = MathUtils.clamp(Math.round(clamped * (frames.length - 1)), 0, frames.length - 1);
        if (reverse) {
            index = frames.length - 1 - index;
        }
        return frames[index];
    }

    private void renderBossGates(Batch batch, float delta) {
        if (bossGateRects.isEmpty()) {
            return;
        }

        if (bossGateTransitionTime < BOSS_GATE_TRANSITION_DURATION) {
            bossGateTransitionTime = Math.min(BOSS_GATE_TRANSITION_DURATION, bossGateTransitionTime + delta);
        }

        float progress = bossGateTransitionTime / BOSS_GATE_TRANSITION_DURATION;

        for (Rectangle rect : bossGateRects) {
            TextureRegion frame;

            if (bossArenaLocked) {
                if (bossGateCloseAnimation != null && bossGateCloseAnimation.getKeyFrames().length > 0) {
                    frame = getProgressFrame(bossGateCloseAnimation, progress, false);
                } else {
                    frame = getProgressFrame(bossGateOpenAnimation, progress, true);
                }
            } else {
                if (bossGateOpenAnimation != null && bossGateOpenAnimation.getKeyFrames().length > 0) {
                    frame = getProgressFrame(bossGateOpenAnimation, progress, false);
                } else {
                    frame = getProgressFrame(bossGateCloseAnimation, progress, true);
                }
            }

            if (frame == null) {
                continue;
            }

            batch.draw(frame, rect.x / TILE_SIZE, rect.y / TILE_SIZE, rect.width / TILE_SIZE, rect.height / TILE_SIZE);
        }
    }

    private void drawBlinking(Batch batch, TextureRegion frame, float x, float y, float w, float h, boolean faceRight,
                              float blinkTimer, float stateTime) {
        if (frame == null) {
            return;
        }

        TextureRegion draw = new TextureRegion(frame);
        if (faceRight) {
            draw.flip(true, false);
        }

        Color old = batch.getColor().cpy();

        if (blinkTimer > 0f) {
            float alpha = 0.22f + 0.78f * Math.abs(MathUtils.sin(stateTime * 28f));
            batch.setColor(1f, 1f, 1f, alpha);
        }

        batch.draw(draw, x, y, w, h);
        batch.setColor(old);
    }
}
