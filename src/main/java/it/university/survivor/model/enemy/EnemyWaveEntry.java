package it.university.survivor.model.enemy;

import java.util.Objects;

public record EnemyWaveEntry(EnemyType type, int count) {

    public EnemyWaveEntry {
        Objects.requireNonNull(type, "Enemy type must not be null");

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Enemy count must be greater than zero"
            );
        }
    }
}