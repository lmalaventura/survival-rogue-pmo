package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.EnemyWaveEntry;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveProgression;

class WaveProgressionTest {

    @Test
    void shouldDefineTheCompleteFifteenWaveDemoProgression() {
        List<List<EnemyWaveEntry>> expected = List.of(
                entries(3, 0, 0, 0, null),
                entries(3, 1, 0, 0, null),
                entries(3, 2, 0, 0, null),
                entries(3, 2, 1, 0, null),
                entries(3, 0, 0, 0, EnemyType.MINIBOSS),
                entries(2, 2, 1, 1, null),
                entries(2, 2, 1, 2, null),
                entries(2, 2, 2, 2, null),
                entries(3, 2, 2, 2, null),
                entries(2, 2, 2, 1, EnemyType.MINIBOSS),
                entries(2, 2, 2, 3, null),
                entries(2, 2, 3, 3, null),
                entries(2, 2, 3, 3, null),
                entries(2, 2, 3, 3, null),
                entries(2, 2, 3, 2, EnemyType.BOSS)
        );

        assertEquals(15, WaveProgression.MAX_WAVES);

        for (int waveNumber = 1;
                waveNumber <= WaveProgression.MAX_WAVES;
                waveNumber++) {
            WaveConfig config = WaveProgression.getConfig(waveNumber);

            assertEquals(waveNumber, config.waveNumber());
            assertEquals(expected.get(waveNumber - 1), config.composition());
            assertEquals(
                    config.enemyCount(),
                    config.composition().stream()
                            .mapToInt(EnemyWaveEntry::count)
                            .sum()
            );
            assertTrue(config.enemyCount() <= 10);
        }
    }

    @Test
    void shouldIncreaseBaseHealthAndSpeedAcrossTheDemo() {
        for (int waveNumber = 1;
                waveNumber <= WaveProgression.MAX_WAVES;
                waveNumber++) {
            WaveConfig config = WaveProgression.getConfig(waveNumber);

            assertEquals(
                    100 + 10 * (waveNumber - 1),
                    config.enemyHealth()
            );
            assertEquals(
                    80.0 + 2.0 * (waveNumber - 1),
                    config.enemySpeed()
            );
        }
    }

    @Test
    void minibossShouldAppearOnlyInWavesFiveAndTen() {
        for (int waveNumber = 1;
                waveNumber <= WaveProgression.MAX_WAVES;
                waveNumber++) {
            boolean containsMiniBoss = contains(
                    WaveProgression.getConfig(waveNumber),
                    EnemyType.MINIBOSS
            );

            assertEquals(
                    waveNumber == 5 || waveNumber == 10,
                    containsMiniBoss
            );
        }
    }

    @Test
    void bossShouldAppearOnlyInFinalWave() {
        for (int waveNumber = 1;
                waveNumber <= WaveProgression.MAX_WAVES;
                waveNumber++) {
            boolean containsBoss = contains(
                    WaveProgression.getConfig(waveNumber),
                    EnemyType.BOSS
            );

            assertEquals(waveNumber == 15, containsBoss);
        }
    }

    @Test
    void rangedShouldAppearFromWaveSixOnward() {
        for (int waveNumber = 1;
                waveNumber <= WaveProgression.MAX_WAVES;
                waveNumber++) {
            boolean containsRanged = contains(
                    WaveProgression.getConfig(waveNumber),
                    EnemyType.RANGED
            );

            if (waveNumber < 6) {
                assertFalse(containsRanged);
            } else {
                assertTrue(containsRanged);
            }
        }
    }

    @Test
    void shouldRejectWaveNumbersOutsideTheDemo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveProgression.getConfig(0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveProgression.getConfig(-1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveProgression.getConfig(16)
        );
    }

    private boolean contains(WaveConfig config, EnemyType type) {
        return config.composition().stream()
                .anyMatch(entry -> entry.type() == type);
    }

    private List<EnemyWaveEntry> entries(
            int basicCount,
            int fastCount,
            int tankCount,
            int rangedCount,
            EnemyType specialType
    ) {
        java.util.ArrayList<EnemyWaveEntry> entries =
                new java.util.ArrayList<>();

        add(entries, EnemyType.BASIC, basicCount);
        add(entries, EnemyType.FAST, fastCount);
        add(entries, EnemyType.TANK, tankCount);
        add(entries, EnemyType.RANGED, rangedCount);

        if (specialType != null) {
            add(entries, specialType, 1);
        }

        return List.copyOf(entries);
    }

    private void add(
            List<EnemyWaveEntry> entries,
            EnemyType type,
            int count
    ) {
        if (count > 0) {
            entries.add(new EnemyWaveEntry(type, count));
        }
    }
}
