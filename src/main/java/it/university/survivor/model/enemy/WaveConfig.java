package it.university.survivor.model.enemy;

import java.util.List;
import java.util.Objects;

public record WaveConfig(
        int waveNumber,
        int enemyCount,
        int enemyHealth,
        double enemySpeed,
        List<EnemyWaveEntry> composition
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

        Objects.requireNonNull(
                composition,
                "Composition must not be null"
        );

        if (composition.isEmpty()) {
            throw new IllegalArgumentException(
                    "Composition must contain at least one entry"
            );
        }

        composition = List.copyOf(composition);

        int compositionCount = composition.stream()
                .mapToInt(EnemyWaveEntry::count)
                .sum();

        if (compositionCount != enemyCount) {
            throw new IllegalArgumentException(
                    "Composition count must match enemy count"
            );
        }
    }

}