package it.university.survivor.model.enemy;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

import java.util.List;
import java.util.Objects;

public final class WaveFactory {

    private WaveFactory() {
    }

    public static Wave createWave(
            int waveNumber,
            List<Position> spawnPositions
    ) {
        Objects.requireNonNull(
                spawnPositions,
                "Spawn positions must not be null"
        );

        WaveConfig config = WaveProgression.getConfig(waveNumber);

        if (spawnPositions.size() != config.enemyCount()) {
            throw new IllegalArgumentException(
                    "Number of spawn positions must match enemy count"
            );
        }

        EnemySpawner spawner = new EnemySpawner(
                config.enemyHealth(),
                config.enemySpeed()
        );

        List<Enemy> enemies = spawner.spawn(spawnPositions);

        return new Wave(config.waveNumber(), enemies);
    }
}