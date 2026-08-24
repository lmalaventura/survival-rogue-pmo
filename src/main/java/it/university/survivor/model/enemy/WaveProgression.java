package it.university.survivor.model.enemy;

import java.util.List;

public final class WaveProgression {

    private WaveProgression() {
    }

    public static WaveConfig getConfig(int waveNumber) {

        if (waveNumber <= 0) {
            throw new IllegalArgumentException(
                    "Wave number must be greater than zero"
            );
        }

        int enemyCount = waveNumber + 2;
        int enemyHealth = 100 + (waveNumber - 1) * 10;
        double enemySpeed = 80.0 + (waveNumber - 1) * 2.0;

        return new WaveConfig(
                waveNumber,
                enemyCount,
                enemyHealth,
                enemySpeed,
                createComposition(waveNumber, enemyCount)
        );
    }

    private static List<EnemyWaveEntry> createComposition(
            int waveNumber,
            int enemyCount
    ) {
        if (waveNumber == 1) {
            return List.of(
                    new EnemyWaveEntry(
                            EnemyType.BASIC,
                            enemyCount
                    )
            );
        }

        if (waveNumber == 2) {
            return List.of(
                    new EnemyWaveEntry(
                            EnemyType.BASIC,
                            2
                    ),
                    new EnemyWaveEntry(
                            EnemyType.FAST,
                            2
                    )
            );
        }

        if (waveNumber == 3) {
            return List.of(
                    new EnemyWaveEntry(
                            EnemyType.BASIC,
                            2
                    ),
                    new EnemyWaveEntry(
                            EnemyType.FAST,
                            2
                    ),
                    new EnemyWaveEntry(
                            EnemyType.TANK,
                            1
                    )
            );
        }

        if (waveNumber == 4) {
            return List.of(
                    new EnemyWaveEntry(
                            EnemyType.BASIC,
                            2
                    ),
                    new EnemyWaveEntry(
                            EnemyType.FAST,
                            2
                    ),
                    new EnemyWaveEntry(
                            EnemyType.TANK,
                            2
                    )
            );
        }

        if (waveNumber == 5) {
            return List.of(
                    new EnemyWaveEntry(
                            EnemyType.BASIC,
                            2
                    ),
                    new EnemyWaveEntry(
                            EnemyType.FAST,
                            2
                    ),
                    new EnemyWaveEntry(
                            EnemyType.TANK,
                            2
                    ),
                    new EnemyWaveEntry(
                            EnemyType.RANGED,
                            1
                    )
            );
        }

        return createAdvancedComposition(
                waveNumber,
                enemyCount
        );
    }

    private static List<EnemyWaveEntry> createAdvancedComposition(
            int waveNumber,
            int enemyCount
    ) {
        int remaining = enemyCount;

        int fast = Math.max(1, waveNumber / 4);
        int tank = Math.max(1, waveNumber / 5);

        if (fast + tank >= remaining) {
            fast = 1;
            tank = 1;
        }

        int basic = remaining - fast - tank;

        return List.of(
                new EnemyWaveEntry(
                        EnemyType.BASIC,
                        basic
                ),
                new EnemyWaveEntry(
                        EnemyType.FAST,
                        fast
                ),
                new EnemyWaveEntry(
                        EnemyType.TANK,
                        tank
                )
        );
    }
}