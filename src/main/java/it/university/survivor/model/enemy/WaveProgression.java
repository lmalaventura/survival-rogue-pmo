package it.university.survivor.model.enemy;

import java.util.List;

public final class WaveProgression {

    private WaveProgression() {
        // Utility class
    }

    public static WaveConfig getConfig(int waveNumber) {
        if (waveNumber < 1) {
            throw new IllegalArgumentException(
                    "Wave number must be >= 1"
            );
        }

        int enemyCount = calculateEnemyCount(waveNumber);
        int enemyHealth = calculateEnemyHealth(waveNumber);
        double enemySpeed = calculateEnemySpeed(waveNumber);

        if (waveNumber == 1) {
            return new WaveConfig(
                    waveNumber,
                    enemyCount,
                    enemyHealth,
                    enemySpeed,
                    List.of(
                            new EnemyWaveEntry(
                                    EnemyType.BASIC,
                                    3
                            )
                    )
            );
        }

        if (waveNumber == 2) {
            return new WaveConfig(
                    waveNumber,
                    enemyCount,
                    enemyHealth,
                    enemySpeed,
                    List.of(
                            new EnemyWaveEntry(
                                    EnemyType.BASIC,
                                    2
                            ),
                            new EnemyWaveEntry(
                                    EnemyType.FAST,
                                    2
                            )
                    )
            );
        }

        if (waveNumber == 3) {
            return new WaveConfig(
                    waveNumber,
                    enemyCount,
                    enemyHealth,
                    enemySpeed,
                    List.of(
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
                    )
            );
        }

        if (waveNumber == 4) {
            return new WaveConfig(
                    waveNumber,
                    enemyCount,
                    enemyHealth,
                    enemySpeed,
                    List.of(
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
                    )
            );
        }

        if (waveNumber == 5) {
            return new WaveConfig(
                    waveNumber,
                    enemyCount,
                    enemyHealth,
                    enemySpeed,
                    List.of(
                            new EnemyWaveEntry(
                                    EnemyType.BASIC,
                                    1
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
                            ),
                            new EnemyWaveEntry(
                                    EnemyType.MINIBOSS,
                                    1
                            )
                    )
            );
        }

        return new WaveConfig(
                waveNumber,
                enemyCount,
                enemyHealth,
                enemySpeed,
                createAdvancedComposition(
                        waveNumber,
                        enemyCount
                )
        );
    }

    private static int calculateEnemyCount(int waveNumber) {
        return waveNumber + 2;
    }

    private static int calculateEnemyHealth(int waveNumber) {
        return 100 + (waveNumber - 1) * 10;
    }

    private static double calculateEnemySpeed(int waveNumber) {
        return 80.0 + (waveNumber - 1) * 2.0;
    }

    private static List<EnemyWaveEntry> createAdvancedComposition(
            int waveNumber,
            int enemyCount
    ) {
        int fast = Math.max(1, waveNumber / 4);
        int tank = Math.max(1, waveNumber / 5);

        if (waveNumber == 15) {
            int basic = enemyCount - fast - tank - 1;

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
                    ),
                    new EnemyWaveEntry(
                            EnemyType.BOSS,
                            1
                    )
            );
        }

        if (waveNumber % 5 == 0) {
            int basic = enemyCount - fast - tank - 1;

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
                    ),
                    new EnemyWaveEntry(
                            EnemyType.MINIBOSS,
                            1
                    )
            );
        }

        int basic = enemyCount - fast - tank;

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