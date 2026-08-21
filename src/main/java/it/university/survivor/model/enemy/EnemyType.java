package it.university.survivor.model.enemy;

public enum EnemyType {

    BASIC(100, 1.0),
    FAST(60, 1.8),
    TANK(250, 0.5),
    RANGED(80, 0.8),
    MINIBOSS(500, 0.7),
    BOSS(1000, 0.6);

    private final int maxHealth;
    private final double movementSpeed;

    EnemyType(int maxHealth, double movementSpeed) {
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
    }

    public int maxHealth() {
        return maxHealth;
    }

    public double movementSpeed() {
        return movementSpeed;
    }
}