package it.university.survivor.model.enemy;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

import java.util.ArrayList;
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

        List<Enemy> enemies = new ArrayList<>();

        int positionIndex = 0;

        for (EnemyWaveEntry entry : config.composition()) {

            EnemySpawner spawner = new EnemySpawner(
                    getHealth(config, entry.type()),
                    getSpeed(config, entry.type()),
                    entry.type()
            );

            List<Position> positions = spawnPositions.subList(
                    positionIndex,
                    positionIndex + entry.count()
            );

            enemies.addAll(spawner.spawn(positions));

            positionIndex += entry.count();
        }

        return new Wave(
                config.waveNumber(),
                enemies
        );
    }

    private static int getHealth(
            WaveConfig config,
            EnemyType type
    ) {
        if (type == EnemyType.BASIC) {
            return config.enemyHealth();
        }

        return type.maxHealth();
    }

    private static double getSpeed(
            WaveConfig config,
            EnemyType type
    ) {
        return config.enemySpeed() * type.speedMultiplier();
    }
}
