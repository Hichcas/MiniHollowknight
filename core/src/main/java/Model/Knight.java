package Model;

import Model.Enums.KnightState;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Knight {
    private boolean onGround;
    private boolean onWallLeft;
    private boolean onWallRight;
    private int wallHoldDir = 0;
    private final Body body;
    private boolean idDead = false;
    private int maxHealth = 7;
    private int health = 5;
    private int maxHealthAvaliable = 5;
    private int maxSoul = 99;
    private int soul = 0;
    private int maxJumps = 2;
    private int usedJumps = 0;
    private float moveSpeed = 4f;
    private float jumpPower = 7.5f;
    private final float baseDashSpeed = 15f;
    private float dashSpeed = 15f;
    private float dashDuration = 0.14f;
    private float dashCooldown = 0.6f;
    private float attackDuration = 0.2f;
    private float focusDuration = 1.5f;
    private float invincibleDuration = 1.0f;
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private float attackTimer = 0f;
    private float focusTimer = 0f;
    private float invincibleTimer = 0f;
    private float attackDamage = 1f;
    private float knockbackMultiplier = 1f;
    private float dashDamage = 1f;
    private float pogoImpulse = 8.2f;
    private float pogoStateDuration = 0.25f;
    private float pogoTimer = 0f;
    private boolean pogoUsedThisAir = false;
    private float wallSlideSpeed = -1.2f;
    private float wallJumpStateDuration = 0.14f;
    private float wallJumpTimer = 0f;
    private float cutoffJumpFactor = 0.45f;
    private float knightWidth = 1.7f;
    private float knightHeight = 3f;
    private float attackHitBoxWidth = 1.0f;
    private float attackHitBoxHeight = 0.55f;
    private float attackHitBoxOffsetX = 0.65f;
    private float attackHitBoxOffsetY = 0.15f;
    private int jahat = 1;
    private int attackSequence = 0;
    private int dashSequence = 0;
    private boolean dashing;
    private boolean attacking;
    private boolean casting;
    private boolean invincible;
    private boolean dashThroughEnemies;
    private final Rectangle attackBounds = new Rectangle();
    private KnightState state = KnightState.IDLE;
    private KnightState lastGroundState = KnightState.IDLE;
    private boolean doubleJumpStarted = false;
    private float attackCooldownDuration = 0.25f;
    private float attackCooldownTimer = 0f;
    private int soulGainPerHit = 11;
    private boolean godMode = false;
    private boolean noclip = false;
    private boolean emergencyHealArmed = false;
    private float noclipSavedGravityScale = 1f;
    private static final float NOCLIP_SPEED = 7f;

    private static final int SPELL_SOUL_COST = 33;
    private float vengefulSpiritCastDuration = 0.35f;
    private float howlingWraithsCastDuration = 0.45f;
    private float castTimer = 0f;
    private boolean castingSpell = false;
    private boolean collectingItem = false;
    private float collectItemTimer = 0f;
    private int vengefulSpiritSequence = 0;
    private int howlingWraithsSequence = 0;
    private float abilityDamageMultiplier = 1f;
    private static final float VENGEFUL_SPIRIT_BASE_DAMAGE = 1f;
    private static final float HOWLING_WRAITHS_BASE_DAMAGE_PER_TICK = 1f;

    public void setState(KnightState state) {
        this.state = state;
    }

    public Knight(Body body) {
        this.body = body;
    }

    public void update(float dt) {
        if (dt <= 0f || idDead) {
            return;
        }

        if (noclip) {

            tickTimers(dt);
            state = KnightState.IDLE;
            return;
        }

        tickTimers(dt);
        updateGroundAndWallMemory();
        updateStateByVelocity();

        if (onGround) {
            usedJumps = 0;
            lastGroundState = state;
        }

        if (collectingItem) {
            collectItemTimer -= dt;
            body.setLinearVelocity(0f, 0f);
            state = KnightState.ITEMGET;
            if (collectItemTimer <= 0f) {
                collectingItem = false;
                state = KnightState.IDLE;
            }
            return;
        }

        if (casting) {
            focusTimer += dt;
            body.setLinearVelocity(0f, 0f);
            state = KnightState.CAST;
            if (focusTimer >= focusDuration) {
                finishFocus();
            }
            return;
        }

        if (dashing) {
            state = KnightState.DASH;
            body.setLinearVelocity(jahat * dashSpeed, 0f);
            return;
        }

        if (attacking) {

            if (state != KnightState.UPSLASH &&
                state != KnightState.DOWNSLASH) {

                state = KnightState.ATTACK;
            }

            body.setLinearVelocity(0f, body.getLinearVelocity().y);
            return;
        }

        if (castingSpell) {
            body.setLinearVelocity(0f, body.getLinearVelocity().y);
            return;
        }

        boolean pressingIntoWall = (onWallLeft && wallHoldDir == -1) || (onWallRight && wallHoldDir == 1);
        float vy = body.getLinearVelocity().y;

        if (!onGround && pressingIntoWall && vy < -0.05f) {
            state = KnightState.WALLSLIDE;
            body.setLinearVelocity(0f, Math.max(vy, wallSlideSpeed));
        } else if (state == KnightState.WALLSLIDE) {
            if (!pressingIntoWall || onGround || vy > 0.05f) {
                state = onGround ? KnightState.IDLE : KnightState.AIRBORNE;
            } else {
                body.setLinearVelocity(0f, Math.max(vy, wallSlideSpeed));
            }
        }
    }

    private void tickTimers(float dt) {
        if (dashTimer > 0f) {
            dashTimer -= dt;
            if (dashTimer <= 0f) {
                dashing = false;
                dashCooldownTimer = dashCooldown;
            }
        }
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= dt;
        }
        if (dashCooldownTimer > 0f) {
            dashCooldownTimer -= dt;
        }

        if (attackTimer > 0f) {
            attackTimer -= dt;
            if (attackTimer <= 0f) {
                attacking = false;
                attackCooldownTimer = attackCooldownDuration;
            }
        }

        if (invincibleTimer > 0f) {
            invincibleTimer -= dt;
            if (invincibleTimer <= 0f) {
                invincible = false;
            }
        }

        if (castTimer > 0f) {
            castTimer -= dt;
            if (castTimer <= 0f) {
                castingSpell = false;
                state = onGround ? KnightState.IDLE : KnightState.AIRBORNE;
            }
        }

        if (pogoTimer > 0f) {
            pogoTimer -= dt;
        }

        if (wallJumpTimer > 0f) {
            wallJumpTimer -= dt;
            if (wallJumpTimer <= 0f && state == KnightState.WALLJUMP) {
                state = onGround ? KnightState.IDLE : KnightState.AIRBORNE;
            }
        }
    }

    private void updateGroundAndWallMemory() {
        if (onGround) {
            pogoUsedThisAir = false;
            return;
        }

        boolean pressingIntoWall = (onWallLeft && wallHoldDir == -1) || (onWallRight && wallHoldDir == 1);
        float vy = body.getLinearVelocity().y;

        if (state == KnightState.WALLSLIDE) {
            if (!pressingIntoWall || vy >= -0.05f) {
                state = vy > 0.05f ? KnightState.AIRBORNE : KnightState.FALL;
            }
            return;
        }

        boolean canEnterWallSlide = state == KnightState.AIRBORNE
            || state == KnightState.JUMP
            || state == KnightState.DOUBLEJUMP
            || state == KnightState.FALL
            || state == KnightState.WALLJUMP
            || state == KnightState.POGO;

        if (canEnterWallSlide && pressingIntoWall && vy < -0.05f) {
            state = KnightState.WALLSLIDE;
            body.setLinearVelocity(0f, Math.max(vy, wallSlideSpeed));
        }
    }

    private void updateStateByVelocity() {
        if (idDead) {
            state = KnightState.DEAD;
            return;
        }
        if (state == KnightState.LOOKUP || state == KnightState.LOOKDOWN) {
            return;
        }
        if (castingSpell) {
            return;
        }
        if (state == KnightState.WALLSLIDE) {
            return;
        }
        if (dashing || attacking || casting) {
            return;
        }
        if (pogoTimer > 0f || wallJumpTimer > 0f) {
            return;
        }

        float vx = body.getLinearVelocity().x;
        float vy = body.getLinearVelocity().y;

        if (onGround) {
            state = Math.abs(vx) > 0.05f ? KnightState.RUN : KnightState.IDLE;
        } else {
            if (vy > 0.1f) {
                if (state != KnightState.JUMP && state != KnightState.DOUBLEJUMP && state != KnightState.POGO) {
                    state = KnightState.AIRBORNE;
                }
            } else if (vy < -0.1f) {
                state = KnightState.FALL;
            }
        }
    }

    public boolean isDoubleJumpStarted() {
        return doubleJumpStarted;
    }

    public void consumeDoubleJump() {
        doubleJumpStarted = false;

        if (!onGround) {
            state = KnightState.AIRBORNE;
        }
    }

    public void moveLeft() {
        wallHoldDir = -1;
        if (canControl()) {
            jahat = -1;
            body.setLinearVelocity(-moveSpeed, body.getLinearVelocity().y);
        }
    }

    public void moveRight() {
        wallHoldDir = 1;
        if (canControl()) {
            jahat = 1;
            body.setLinearVelocity(moveSpeed, body.getLinearVelocity().y);
        }
    }

    public void stopHorizontalIfGrounded() {
        wallHoldDir = 0;
        if (canControl() && onGround) {
            body.setLinearVelocity(0f, body.getLinearVelocity().y);
        }
    }

    public void jump() {
        if (!canControl()) {
            return;
        }

        if (!onGround && (state == KnightState.WALLSLIDE || onWallLeft || onWallRight)) {
            int awayDir;
            if (onWallLeft && !onWallRight) {
                awayDir = 1;
            } else if (onWallRight && !onWallLeft) {
                awayDir = -1;
            } else if (wallHoldDir == -1 && onWallLeft) {
                awayDir = 1;
            } else if (wallHoldDir == 1 && onWallRight) {
                awayDir = -1;
            } else {
                awayDir = -jahat;
            }

            jahat = awayDir;
            usedJumps = Math.max(usedJumps, 1);
            doubleJumpStarted = false;
            dashing = false;
            dashTimer = 0f;
            dashCooldownTimer = 0f;
            wallJumpTimer = wallJumpStateDuration;
            body.setLinearVelocity(awayDir * moveSpeed * 1.35f, 0f);
            body.applyLinearImpulse(new Vector2(0f, jumpPower), body.getWorldCenter(), true);
            state = KnightState.WALLJUMP;
            return;
        }

        if (onGround) {
            body.setLinearVelocity(body.getLinearVelocity().x, 0f);
            body.applyLinearImpulse(new Vector2(0f, jumpPower), body.getWorldCenter(), true);

            usedJumps = 1;
            onGround = false;
            doubleJumpStarted = false;
            state = KnightState.JUMP;
            return;
        }

        if (usedJumps < maxJumps) {
            body.setLinearVelocity(body.getLinearVelocity().x, 0f);
            body.applyLinearImpulse(new Vector2(0f, jumpPower), body.getWorldCenter(), true);

            usedJumps++;
            doubleJumpStarted = true;
            state = KnightState.DOUBLEJUMP;
        }
    }

    public void cutJumpIfReleased() {
        if (!onGround && body.getLinearVelocity().y > 0f && !dashing && !attacking && !casting) {
            body.setLinearVelocity(body.getLinearVelocity().x, body.getLinearVelocity().y * cutoffJumpFactor);
        }
    }

    public void dash() {
        if (!canControl()) {
            return;
        }
        if (dashCooldownTimer > 0f) {
            return;
        }

        dashing = true;
        dashTimer = dashDuration;
        dashSequence++;
        body.setLinearVelocity(jahat * dashSpeed, 0f);
        state = KnightState.DASH;
    }

    public void attack() {
        if (!canControl()) {
            return;
        }
        if (attackTimer > 0f || attackCooldownTimer > 0f) {
            return;
        }
        attackSequence++;
        attacking = true;
        attackTimer = attackDuration;
        body.setLinearVelocity(0f, body.getLinearVelocity().y);
        state = KnightState.ATTACK;
    }

    public void startFocus() {
        if (!canControl()) {
            return;
        }
        if (!onGround || soul < 33 || health >= maxHealth) {
            return;
        }

        casting = true;
        focusTimer = 0f;
        body.setLinearVelocity(0f, 0f);
        state = KnightState.CAST;
    }

    private void finishFocus() {
        casting = false;
        focusTimer = 0f;
        state = KnightState.FOCUSGET;
        if (soul >= 33 && health < maxHealth) {
            soul -= 33;
            health++;
        }
    }

    public void cancelFocus() {
        if (!casting) return;

        casting = false;
        focusTimer = 0f;

        if (onGround) {
            state = KnightState.IDLE;
        }
    }

    public void lookUp() {
        if (!canControl() || !onGround) return;
        state = KnightState.LOOKUP;
    }

    public void lookDown() {
        if (!canControl() || !onGround) return;
        state = KnightState.LOOKDOWN;
    }

    public void attackUp() {
        if (!canControl()) return;
        if (attackTimer > 0f || attackCooldownTimer > 0f) return;

        attackSequence++;
        attacking = true;
        attackTimer = attackDuration;

        body.setLinearVelocity(0f, body.getLinearVelocity().y);
        state = KnightState.UPSLASH;
    }

    public void attackDown() {
        if (!canControl()) return;
        if (onGround) return;
        if (attackTimer > 0f || attackCooldownTimer > 0f) return;

        attackSequence++;
        attacking = true;
        attackTimer = attackDuration;

        body.setLinearVelocity(0f, body.getLinearVelocity().y);
        state = KnightState.DOWNSLASH;
    }

    public void castVengefulSpirit() {
        if (!canControl()) {
            return;
        }
        if (soul < SPELL_SOUL_COST) {
            return;
        }

        soul -= SPELL_SOUL_COST;
        castingSpell = true;
        castTimer = vengefulSpiritCastDuration;
        vengefulSpiritSequence++;

        body.setLinearVelocity(0f, body.getLinearVelocity().y);
        state = KnightState.VENGEFULSPIRIT;
    }

    public void castHowlingWraiths() {
        if (!canControl()) {
            return;
        }
        if (soul < SPELL_SOUL_COST) {
            return;
        }

        soul -= SPELL_SOUL_COST;
        castingSpell = true;
        castTimer = howlingWraithsCastDuration;
        howlingWraithsSequence++;

        body.setLinearVelocity(0f, body.getLinearVelocity().y);
        state = KnightState.HOWLINGWRAITHS;
    }

    public void takeDamage(int amount, Vector2 knockbackDir, float knockbackForce) {
        if (idDead || invincible || godMode || noclip) {
            return;
        }
        if (dashing && dashThroughEnemies) {
            return;
        }
        if (casting) {
            cancelFocus();
        }
        health = Math.max(0, health - amount);
        if (health == 0) {
            if (emergencyHealArmed) {

                emergencyHealArmed = false;
                health = 1;
                invincible = true;
                invincibleTimer = invincibleDuration;
                state = KnightState.HIT;
                pogoTimer = 0f;

                Vector2 dir = knockbackDir == null ? new Vector2(-jahat, 0.2f) : knockbackDir.cpy();
                if (dir.len2() < 0.0001f) {
                    dir.set(-jahat, 0.2f);
                }
                dir.nor().scl(knockbackForce);
                body.setLinearVelocity(dir.x, dir.y);
                return;
            }
            idDead = true;
            state = KnightState.DEAD;
            body.setLinearVelocity(0f, 0f);
            return;
        }

        invincible = true;
        invincibleTimer = invincibleDuration;
        state = KnightState.HIT;
        pogoTimer = 0f;

        Vector2 dir = knockbackDir == null ? new Vector2(-jahat, 0.2f) : knockbackDir.cpy();
        if (dir.len2() < 0.0001f) {
            dir.set(-jahat, 0.2f);
        }
        dir.nor().scl(knockbackForce);
        body.setLinearVelocity(0f, 0f);
        body.applyLinearImpulse(dir, body.getWorldCenter(), true);
    }

    public void performPogo() {
        if (idDead || pogoTimer > 0f || pogoUsedThisAir) {
            return;
        }

        pogoUsedThisAir = true;
        body.setLinearVelocity(body.getLinearVelocity().x, 0f);
        body.applyLinearImpulse(new Vector2(0f, pogoImpulse), body.getWorldCenter(), true);
        usedJumps = 0;
        dashing = false;
        dashTimer = 0f;
        dashCooldownTimer = 0f;
        doubleJumpStarted = false;
        attacking = false;
        attackTimer = 0f;
        pogoTimer = pogoStateDuration;
        wallJumpTimer = 0f;
        state = KnightState.POGO;
    }

    public void gainSoul(int amount) {
        soul = MathUtils.clamp(soul + Math.max(0, amount), 0, maxSoul);
    }

    public void healOneMask() {
        if (health < maxHealth && soul >= 33) {
            soul -= 33;
            health++;
        }
    }

    private Vector2 pendingRelocation = null;

    public void queueRelocation(Vector2 target) {
        if (target == null) {
            return;
        }
        pendingRelocation = new Vector2(target);
    }

    public boolean hasPendingRelocation() {
        return pendingRelocation != null;
    }

    public void applyPendingRelocationIfAny() {
        if (pendingRelocation == null) {
            return;
        }
        body.setTransform(pendingRelocation, 0f);
        body.setLinearVelocity(0f, 0f);
        pendingRelocation = null;
    }

    public void respawn(Vector2 spawnPoint) {
        body.setTransform(spawnPoint, 0f);
        body.setLinearVelocity(0f, 0f);
        health = 5;
        dashTimer = 0f;
        dashCooldownTimer = 0f;
        attackTimer = 0f;
        focusTimer = 0f;
        invincible = true;
        invincibleTimer = invincibleDuration;
        usedJumps = 0;
        soul = 0;
        idDead = false;
        dashing = false;
        attacking = false;
        casting = false;
        castingSpell = false;
        castTimer = 0f;
        state = KnightState.IDLE;
        pogoTimer = 0f;
        pogoUsedThisAir = false;
        wallJumpTimer = 0f;

    }

    public Rectangle getAttackBounds() {
        float x = jahat > 0f
            ? body.getPosition().x + attackHitBoxOffsetX
            : body.getPosition().x - attackHitBoxOffsetX - attackHitBoxWidth;
        float y = body.getPosition().y + attackHitBoxOffsetY;
        attackBounds.set(x, y, attackHitBoxWidth, attackHitBoxHeight);
        return attackBounds;
    }

    private boolean canControl() {
        return !idDead && !dashing && !attacking && !casting && !castingSpell && !collectingItem;
    }

    public void triggerItemPickupAnimation(float duration) {
        if (idDead) return;
        collectingItem = true;
        collectItemTimer = duration;
        body.setLinearVelocity(0f, body.getLinearVelocity().y);
        state = KnightState.ITEMGET;
    }

    public boolean isCollectingItem() {
        return collectingItem;
    }

    public void setGrounded(boolean value) {
        this.onGround = value;
        if (value) {
            usedJumps = 0;
            wallJumpTimer = 0f;
            pogoUsedThisAir = false;
        }
    }

    public void setWallContacts(boolean left, boolean right) {
        this.onWallLeft = left;
        this.onWallRight = right;
    }

    public Body getBody() {
        return body;
    }

    public KnightState getState() {
        return state;
    }

    public float getFacing() {
        return jahat;
    }

    public boolean isDead() {
        return idDead;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public boolean isCasting() {
        return casting;
    }

    public boolean isDashing() {
        return dashing;
    }

    public Rectangle getBodyBounds() {
        Vector2 pos = body.getPosition();
        return new Rectangle(pos.x - knightWidth / 2f, pos.y - knightHeight / 2f, knightWidth, knightHeight);
    }

    public boolean isAttacking() {
        return attacking;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getSoul() {
        return soul;
    }

    public int getMaxSoul() {
        return maxSoul;
    }

    public void setHealth(int health) {
        this.health = MathUtils.clamp(health, 0, maxHealth);
        this.idDead = this.health == 0;
        if (this.idDead) {
            state = KnightState.DEAD;
            body.setLinearVelocity(0f, 0f);
        }
    }

    public void setSoul(int soul) {
        this.soul = MathUtils.clamp(soul, 0, maxSoul);
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        this.health = Math.min(this.health, this.maxHealth);
    }

    public void setMaxSoul(int maxSoul) {
        this.maxSoul = Math.max(0, maxSoul);
        this.soul = Math.min(this.soul, this.maxSoul);
    }

    public void setDashThroughEnemies(boolean dashThroughEnemies) {
        this.dashThroughEnemies = dashThroughEnemies;
    }

    public boolean isGodMode() {
        return godMode;
    }

    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }

    public void toggleGodMode() {
        godMode = !godMode;
    }

    public boolean isEmergencyHealArmed() {
        return emergencyHealArmed;
    }

    public void setEmergencyHealArmed(boolean emergencyHealArmed) {
        this.emergencyHealArmed = emergencyHealArmed;
    }

    public void toggleEmergencyHeal() {
        emergencyHealArmed = !emergencyHealArmed;
    }

    public boolean isNoclip() {
        return noclip;
    }

    public void setNoclip(boolean value) {
        if (noclip == value) {
            return;
        }
        noclip = value;
        if (noclip) {
            noclipSavedGravityScale = body.getGravityScale();
            body.setGravityScale(0f);
            body.setLinearVelocity(0f, 0f);
            dashing = false;
            attacking = false;
            casting = false;
            castingSpell = false;
            setSolidFixturesSensor(true);
        } else {
            body.setGravityScale(noclipSavedGravityScale);
            body.setLinearVelocity(0f, 0f);
            state = onGround ? KnightState.IDLE : KnightState.AIRBORNE;
            setSolidFixturesSensor(false);
        }
    }

    private void setSolidFixturesSensor(boolean sensor) {
        for (com.badlogic.gdx.physics.box2d.Fixture fixture : body.getFixtureList()) {
            if ("PLAYER".equals(fixture.getUserData())) {
                fixture.setSensor(sensor);
            }
        }
    }

    public void toggleNoclip() {
        setNoclip(!noclip);
    }

    public void flyMove(float dx, float dy) {
        if (!noclip) {
            return;
        }
        body.setLinearVelocity(dx * NOCLIP_SPEED, dy * NOCLIP_SPEED);
        if (dx > 0.01f) {
            jahat = 1;
        } else if (dx < -0.01f) {
            jahat = -1;
        }
    }

    public boolean canDashThroughEnemies() {
        return dashThroughEnemies;
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public float getKnockbackMultiplier() {
        return knockbackMultiplier;
    }

    public int getSoulGainPerHit() {
        return soulGainPerHit;
    }

    public void setSoulGainPerHit(int soulGainPerHit) {
        this.soulGainPerHit = Math.max(0, soulGainPerHit);
    }

    public float getDashDamage() {
        return dashDamage;
    }

    public float getKnightWidth() {
        return knightWidth;
    }

    public float getKnightHeight() {
        return knightHeight;
    }

    public KnightState getLastGroundState() {
        return lastGroundState;
    }

    public void dispose() {

    }

    public int getAttackSequence() {
        return attackSequence;
    }

    public float getJahat() {
        return jahat;
    }

    public int getDashSequence() {
        return dashSequence;
    }

    public void clearLook() {
        if (state == KnightState.LOOKUP || state == KnightState.LOOKDOWN) {
            if (!onGround) {
                state = KnightState.AIRBORNE;
            } else {
                state = Math.abs(body.getLinearVelocity().x) > 0.05f
                    ? KnightState.RUN
                    : KnightState.IDLE;
            }
        }
    }

    public void applyCharmStats(CharmStats stats) {
        dashCooldown = stats.dashCooldown;
        dashDuration = stats.dashDuration;
        dashSpeed = baseDashSpeed * stats.dashSpeedMultiplier;
        attackCooldownDuration = stats.attackCooldown;
        focusDuration = stats.focusDuration;
        attackDamage = stats.attackDamageMultiplier;
        dashThroughEnemies = stats.dashThroughEnemies;
        abilityDamageMultiplier = stats.abilityDamageMultiplier;
        knockbackMultiplier = stats.knockbackForce;
        soulGainPerHit = Math.round(stats.soulPerHit);
    }

    public boolean isCastingSpell() {
        return castingSpell;
    }

    public int getVengefulSpiritSequence() {
        return vengefulSpiritSequence;
    }

    public int getHowlingWraithsSequence() {
        return howlingWraithsSequence;
    }

    public float getAbilityDamageMultiplier() {
        return abilityDamageMultiplier;
    }

    public float getVengefulSpiritDamage() {
        return 6f * abilityDamageMultiplier;
    }

    public float getHowlingWraithsDamagePerTick() {
        return HOWLING_WRAITHS_BASE_DAMAGE_PER_TICK * abilityDamageMultiplier;
    }

    public int getSpellSoulCost() {
        return SPELL_SOUL_COST;
    }
}
