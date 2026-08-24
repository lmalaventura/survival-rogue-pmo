package it.university.survivor.model.enemy;

public record EnemySpawnConfig(
        EnemyType type,
        int count
) {
    public EnemySpawnConfig {
        if (type == null) {
            throw new NullPointerException("Enemy type must not be null");
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Enemy count must be greater than zero"
            );
        }
    }
}