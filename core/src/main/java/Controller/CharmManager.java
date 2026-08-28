package Controller;

import Model.CharmDefinition;
import Model.CharmStats;
import Model.Enums.CharmState;
import Model.Knight;

import java.util.EnumMap;
import java.util.Map;

public class CharmManager {
    private final Knight knight;
    private final CharmStats baseStats = new CharmStats();
    private final CharmStats currentStats = new CharmStats();

    private final Map<CharmState, CharmDefinition> defs = new EnumMap<>(CharmState.class);
    private final Map<CharmState, Boolean> unlocked = new EnumMap<>(CharmState.class);
    private final Map<CharmState, Boolean> equipped = new EnumMap<>(CharmState.class);

    private final int maxNotches = 3;

    public CharmManager(Knight knight) {
        this.knight = knight;

        for (CharmState state : CharmState.values()) {

            unlocked.put(state, state != CharmState.VOID_HEART);
            equipped.put(state, false);
        }

        registerDefaults();
        rebuild();
    }

    private void registerDefaults() {
        defs.put(CharmState.SOUL_CATCHER, new CharmDefinition(
            CharmState.SOUL_CATCHER,
            "Catcher Soul",
            "increases the soul gained per hit",
            1,
            s -> s.soulPerHit *= 1.4f
        ));

        defs.put(CharmState.DASHMASTER, new CharmDefinition(
            CharmState.DASHMASTER,
            "Dashmaster",
            "decreases dash cooldown",
            1,
            s -> {
                s.dashCooldown *= 0.65f;
                s.dashDuration *= 0.95f;
            }
        ));

        defs.put(CharmState.UNBREAKABLE_STRENGTH, new CharmDefinition(
            CharmState.UNBREAKABLE_STRENGTH,
            "Strength Unbreakable",
            "multiplies basic attack damage",
            1,
            s -> s.attackDamageMultiplier *= 1.5f
        ));

        defs.put(CharmState.QUICK_SLASH, new CharmDefinition(
            CharmState.QUICK_SLASH,
            "Quick Slash",
            "decreases the attack cooldown",
            1,
            s -> s.attackCooldown *= 0.65f
        ));

        defs.put(CharmState.QUICK_FOCUS, new CharmDefinition(
            CharmState.QUICK_FOCUS,
            "Quick Focus",
            "decreases the focus time",
            1,
            s -> s.focusDuration *= 0.7f
        ));

        defs.put(CharmState.HEAVY_BLOW, new CharmDefinition(
            CharmState.HEAVY_BLOW,
            "Heavy Blow",
            "increases knockback force",
            1,
            s -> s.knockbackForce *= 1.45f
        ));

        defs.put(CharmState.SHARP_SHADOW, new CharmDefinition(
            CharmState.SHARP_SHADOW,
            "Sharp Shadow",
            "dash goes through enemies and lasts longer",
            1,
            s -> {
                s.dashThroughEnemies = true;
                s.dashDuration *= 1.5f;
                s.dashSpeedMultiplier *= 1.2f;
            }
        ));

        defs.put(CharmState.VOID_HEART, new CharmDefinition(
            CharmState.VOID_HEART,
            "Void Heart",
            "increases ability damage by 50%",
            1,
            s -> s.abilityDamageMultiplier *= 1.5f
        ));
    }

    public boolean toggle(CharmState state) {
        if (!Boolean.TRUE.equals(unlocked.get(state))) return false;

        boolean isEquipped = Boolean.TRUE.equals(equipped.get(state));
        if (isEquipped) {
            equipped.put(state, false);
            rebuild();
            return true;
        }

        if (usedNotches() + defs.get(state).notches > maxNotches) {
            return false;
        }

        equipped.put(state, true);
        rebuild();
        return true;
    }

    public boolean isEquipped(CharmState state) {
        return Boolean.TRUE.equals(equipped.get(state));
    }

    public boolean isUnlocked(CharmState state) {
        return Boolean.TRUE.equals(unlocked.get(state));
    }

    public void setUnlocked(CharmState state, boolean value) {
        unlocked.put(state, value);
    }

    public void unlockAll() {
        for (CharmState state : CharmState.values()) {
            unlocked.put(state, true);
        }
    }

    public CharmDefinition getDefinition(CharmState state) {
        return defs.get(state);
    }

    public int usedNotches() {
        int sum = 0;
        for (CharmState state : CharmState.values()) {
            if (isEquipped(state)) {
                sum += defs.get(state).notches;
            }
        }
        return sum;
    }

    public CharmStats getCurrentStats() {
        return currentStats;
    }

    public int getSoulPerHit() {
        return (int) currentStats.soulPerHit;
    }

    public void rebuild() {
        currentStats.soulPerHit = baseStats.soulPerHit;
        currentStats.attackDamageMultiplier = baseStats.attackDamageMultiplier;
        currentStats.dashCooldown = baseStats.dashCooldown;
        currentStats.dashDuration = baseStats.dashDuration;
        currentStats.attackCooldown = baseStats.attackCooldown;
        currentStats.focusDuration = baseStats.focusDuration;
        currentStats.knockbackForce = baseStats.knockbackForce;
        currentStats.abilityDamageMultiplier = baseStats.abilityDamageMultiplier;
        currentStats.dashThroughEnemies = baseStats.dashThroughEnemies;

        for (CharmState state : CharmState.values()) {
            if (isEquipped(state)) {
                defs.get(state).modifier.accept(currentStats);
            }
        }

        knight.applyCharmStats(currentStats);
    }
}
