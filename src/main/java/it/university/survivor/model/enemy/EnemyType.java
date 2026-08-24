package it.university.survivor.model.enemy;

public enum EnemyType {

    BASIC(100, 1.0, 6.0),
    FAST(60, 1.8, 5.0),
    TANK(250, 0.5, 8.0),
    RANGED(80, 0.8, 6.0),
    MINIBOSS(500, 0.7, 12.0),
    BOSS(1000, 0.6, 18.0);

    private final int maxHealth;
    private final double speedMultiplier;
    private final double collisionRadius;

    EnemyType(
            int maxHealth,
            double speedMultiplier,
            double collisionRadius
    ) {
        this.maxHealth = maxHealth;
        this.speedMultiplier = speedMultiplier;
        this.collisionRadius = collisionRadius;
    }

    public int maxHealth() {
        return maxHealth;
    }

    public double speedMultiplier() {
        return speedMultiplier;
    }

    public double collisionRadius() {
        return collisionRadius;
    }
}
