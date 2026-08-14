package it.university.survivor.model.enemy;

public record WaveConfig(
        int waveNumber,
        int enemyCount,
        int enemyHealth,
        double enemySpeed
) {
    public WaveConfig {
        if (waveNumber <= 0) {
            throw new IllegalArgumentException(
                    "Wave number must be greater than zero"
            );
        }

        if (enemyCount <= 0) {
            throw new IllegalArgumentException(
                    "Enemy count must be greater than zero"
            );
        }

        if (enemyHealth <= 0) {
            throw new IllegalArgumentException(
                    "Enemy health must be greater than zero"
            );
        }

        if (!Double.isFinite(enemySpeed) || enemySpeed <= 0.0) {
            throw new IllegalArgumentException(
                    "Enemy speed must be finite and greater than zero"
            );
        }
    }
}