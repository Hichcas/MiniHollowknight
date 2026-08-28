package Model;

public class CharmStats {
    public float soulPerHit = 11f;
    public float attackDamageMultiplier = 1f;
    public float dashCooldown = 0.6f;
    public float dashDuration = 0.14f;
    public float dashSpeedMultiplier = 1f;
    public float attackCooldown = 0.25f;
    public float focusDuration = 1.5f;
    public float knockbackForce = 1f;
    public float abilityDamageMultiplier = 1f;
    public boolean dashThroughEnemies = false;

    public CharmStats copy() {
        CharmStats c = new CharmStats();
        c.soulPerHit = soulPerHit;
        c.attackDamageMultiplier = attackDamageMultiplier;
        c.dashCooldown = dashCooldown;
        c.dashDuration = dashDuration;
        c.dashSpeedMultiplier = dashSpeedMultiplier;
        c.attackCooldown = attackCooldown;
        c.focusDuration = focusDuration;
        c.knockbackForce = knockbackForce;
        c.abilityDamageMultiplier = abilityDamageMultiplier;
        c.dashThroughEnemies = dashThroughEnemies;
        return c;
    }
}
