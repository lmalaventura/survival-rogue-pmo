package it.university.survivor.weapon;

public final class WeaponStats {

    private final double cooldownSeconds;
    private final int damage;
    private final double projectileSpeed;
    private final int projectileCount;
    private final double spreadDegrees;


    public WeaponStats(double cooldownSeconds, int damage, double projectileSpeed) {
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
        this.damage          = damage;
        this.projectileSpeed = projectileSpeed;
        this.projectileCount = 1;
        this.spreadDegrees   = 0.0;
    }
    public WeaponStats(
        double cooldownSeconds,
        int damage,
        double projectileSpeed,
        int projectileCount,
        double spreadDegrees) {

    if (!Double.isFinite(cooldownSeconds) || cooldownSeconds <= 0) {
        throw new IllegalArgumentException("Cooldown non valido.");
    }

    if (damage <= 0) {
        throw new IllegalArgumentException("Danno non valido.");
    }

    if (!Double.isFinite(projectileSpeed) || projectileSpeed <= 0) {
        throw new IllegalArgumentException("Velocità non valida.");
    }

    if (projectileCount <= 0) {
        throw new IllegalArgumentException(
                "Numero di proiettili non valido."
        );
    }

    if (!Double.isFinite(spreadDegrees)
            || spreadDegrees < 0.0
            || spreadDegrees > 360.0) {

        throw new IllegalArgumentException(
                "Spread non valido."
        );
    }

    this.cooldownSeconds = cooldownSeconds;
    this.damage = damage;
    this.projectileSpeed = projectileSpeed;
    this.projectileCount = projectileCount;
    this.spreadDegrees = spreadDegrees;
}
    public WeaponStats withProjectilePattern(int projectileCount, double spreadDegrees){
        return new WeaponStats(this.cooldownSeconds,this.damage,this.projectileSpeed,projectileCount,spreadDegrees);
    }
    public double getCooldownSeconds() {
        return cooldownSeconds;
    }

    public int getDamage() {
        return damage;
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }
    public int getProjectileCount(){
        return this.projectileCount;
    }
    public double getSpreadDegrees(){
        return this.spreadDegrees;
    }
}
