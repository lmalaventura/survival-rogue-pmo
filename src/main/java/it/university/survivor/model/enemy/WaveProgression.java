package it.university.survivor.model.enemy;

import java.util.ArrayList;
import java.util.List;

public final class WaveProgression {

    public static final int MAX_WAVES = 15;

    private WaveProgression() {
        // Utility class
    }

    public static WaveConfig getConfig(int waveNumber) {
        if (waveNumber < 1 || waveNumber > MAX_WAVES) {
            throw new IllegalArgumentException(
                    "Wave number must be between 1 and " + MAX_WAVES
            );
        }

        List<EnemyWaveEntry> composition = getComposition(waveNumber);
        int enemyCount = composition.stream()
                .mapToInt(EnemyWaveEntry::count)
                .sum();

        return new WaveConfig(
                waveNumber,
                enemyCount,
                calculateEnemyHealth(waveNumber),
                calculateEnemySpeed(waveNumber),
                composition
        );
    }

    private static List<EnemyWaveEntry> getComposition(int waveNumber) {
        return switch (waveNumber) {
            case 1 -> composition(3, 0, 0, 0, null);
            case 2 -> composition(3, 1, 0, 0, null);
            case 3 -> composition(3, 2, 0, 0, null);
            case 4 -> composition(3, 2, 1, 0, null);
            case 5 -> composition(3, 0, 0, 0, EnemyType.MINIBOSS);
            case 6 -> composition(2, 2, 1, 1, null);
            case 7 -> composition(2, 2, 1, 2, null);
            case 8 -> composition(2, 2, 2, 2, null);
            case 9 -> composition(3, 2, 2, 2, null);
            case 10 -> composition(2, 2, 2, 1, EnemyType.MINIBOSS);
            case 11 -> composition(2, 2, 2, 3, null);
            case 12 -> composition(2, 2, 3, 3, null);
            case 13 -> composition(2, 2, 3, 3, null);
            case 14 -> composition(2, 2, 3, 3, null);
            case 15 -> composition(2, 2, 3, 2, EnemyType.BOSS);
            default -> throw new IllegalStateException(
                    "Unsupported wave number: " + waveNumber
            );
        };
    }

    private static List<EnemyWaveEntry> composition(
            int basicCount,
            int fastCount,
            int tankCount,
            int rangedCount,
            EnemyType specialType
    ) {
        List<EnemyWaveEntry> entries = new ArrayList<>();

        addEntry(entries, EnemyType.BASIC, basicCount);
        addEntry(entries, EnemyType.FAST, fastCount);
        addEntry(entries, EnemyType.TANK, tankCount);
        addEntry(entries, EnemyType.RANGED, rangedCount);

        if (specialType != null) {
            addEntry(entries, specialType, 1);
        }

        return List.copyOf(entries);
    }

    private static void addEntry(
            List<EnemyWaveEntry> entries,
            EnemyType type,
            int count
    ) {
        if (count > 0) {
            entries.add(new EnemyWaveEntry(type, count));
        }
    }

    private static int calculateEnemyHealth(int waveNumber) {
        return 100 + 10 * (waveNumber - 1);
    }

    private static double calculateEnemySpeed(int waveNumber) {
        return 80.0 + 2.0 * (waveNumber - 1);
    }
}
