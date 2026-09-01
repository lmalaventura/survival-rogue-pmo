package it.university.survivor.model.enemy;

public enum EnemyType {

    BASIC(100, 1.0, 6.0, 10),
    FAST(60, 1.8, 5.0, 12),
    TANK(250, 0.5, 8.0, 18),
    RANGED(80, 0.8, 6.0, 15),
    MINIBOSS(500, 0.7, 12.0, 75),
    BOSS(1000, 0.6, 18.0, 250);

    private final int maxHealth;
    private final double speedMultiplier;
    private final double collisionRadius;
    private final int experienceReward;

    EnemyType(
            int maxHealth,
            double speedMultiplier,
            double collisionRadius,
            int experienceReward
    ) {
        this.maxHealth = maxHealth;
        this.speedMultiplier = speedMultiplier;
        this.collisionRadius = collisionRadius;
        this.experienceReward = experienceReward;
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

    public int experienceReward() {
        return experienceReward;
    }
}
