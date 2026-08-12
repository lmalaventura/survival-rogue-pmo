package it.university.survivor.weapon;

public final class WeaponStats {

    private final double cooldownSeconds;
    private final double damage;
    private final double projectileSpeed;

    public WeaponStats(double cooldownSeconds, double damage, double projectileSpeed) {
        if (!Double.isFinite(cooldownSeconds) || cooldownSeconds <= 0) {
            throw new IllegalArgumentException("Cooldown non valido.");
        }
        if (damage <= 0) {
            throw new IllegalArgumentException("Danno non valido.");
        }
        if (!Double.isFinite(projectileSpeed) || projectileSpeed <= 0) {
            throw new IllegalArgumentException("Velocità non valida.");
        }

        this.cooldownSeconds = cooldownSeconds;
        this.damage = damage;
        this.projectileSpeed = projectileSpeed;
    }

    public double getCooldownSeconds() {
        return cooldownSeconds;
    }

    public double getDamage() {
        return damage;
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }
}