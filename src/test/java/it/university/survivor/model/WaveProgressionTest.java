package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveProgression;

class WaveProgressionTest {

    @Test
    void waveOneShouldHaveCorrectConfiguration() {
        WaveConfig config = WaveProgression.getConfig(1);

        assertEquals(1, config.waveNumber());
        assertEquals(3, config.enemyCount());
        assertEquals(100, config.enemyHealth());
        assertEquals(80.0, config.enemySpeed());
    }

    @Test
    void waveTwoShouldHaveCorrectConfiguration() {
        WaveConfig config = WaveProgression.getConfig(2);

        assertEquals(2, config.waveNumber());
        assertEquals(4, config.enemyCount());
        assertEquals(110, config.enemyHealth());
        assertEquals(82.0, config.enemySpeed());
    }

    @Test
    void waveFiveShouldHaveCorrectConfiguration() {
        WaveConfig config = WaveProgression.getConfig(5);

        assertEquals(5, config.waveNumber());
        assertEquals(7, config.enemyCount());
        assertEquals(140, config.enemyHealth());
        assertEquals(88.0, config.enemySpeed());
    }

    @Test
    void enemyCountShouldIncreaseWithWaveNumber() {
        WaveConfig waveOne = WaveProgression.getConfig(1);
        WaveConfig waveTwo = WaveProgression.getConfig(2);
        WaveConfig waveFive = WaveProgression.getConfig(5);

        assertEquals(3, waveOne.enemyCount());
        assertEquals(4, waveTwo.enemyCount());
        assertEquals(7, waveFive.enemyCount());
    }

    @Test
    void enemyHealthShouldIncreaseWithWaveNumber() {
        WaveConfig waveOne = WaveProgression.getConfig(1);
        WaveConfig waveTwo = WaveProgression.getConfig(2);
        WaveConfig waveFive = WaveProgression.getConfig(5);

        assertEquals(100, waveOne.enemyHealth());
        assertEquals(110, waveTwo.enemyHealth());
        assertEquals(140, waveFive.enemyHealth());
    }

    @Test
    void enemySpeedShouldIncreaseWithWaveNumber() {
        WaveConfig waveOne = WaveProgression.getConfig(1);
        WaveConfig waveTwo = WaveProgression.getConfig(2);
        WaveConfig waveFive = WaveProgression.getConfig(5);

        assertEquals(80.0, waveOne.enemySpeed());
        assertEquals(82.0, waveTwo.enemySpeed());
        assertEquals(88.0, waveFive.enemySpeed());
    }

    @Test
    void waveTwoShouldHaveCorrectEnemyComposition() {
        WaveConfig config = WaveProgression.getConfig(2);

        assertEquals(
                List.of(
                        EnemyType.BASIC,
                        EnemyType.FAST
                ),
                config.composition().stream()
                        .map(entry -> entry.type())
                        .toList()
        );

        assertEquals(2, config.composition().get(0).count());
        assertEquals(2, config.composition().get(1).count());
    }

    @Test
    void waveThreeShouldHaveCorrectEnemyComposition() {
        WaveConfig config = WaveProgression.getConfig(3);

        assertEquals(
                List.of(
                        EnemyType.BASIC,
                        EnemyType.FAST,
                        EnemyType.TANK
                ),
                config.composition().stream()
                        .map(entry -> entry.type())
                        .toList()
        );

        assertEquals(2, config.composition().get(0).count());
        assertEquals(2, config.composition().get(1).count());
        assertEquals(1, config.composition().get(2).count());
    }

    @Test
    void waveFourShouldHaveCorrectEnemyComposition() {
        WaveConfig config = WaveProgression.getConfig(4);

        assertEquals(
                List.of(
                        EnemyType.BASIC,
                        EnemyType.FAST,
                        EnemyType.TANK
                ),
                config.composition().stream()
                        .map(entry -> entry.type())
                        .toList()
        );

        assertEquals(2, config.composition().get(0).count());
        assertEquals(2, config.composition().get(1).count());
        assertEquals(2, config.composition().get(2).count());
    }

    @Test
    void waveFiveShouldContainRangedAndMiniBoss() {
        WaveConfig config = WaveProgression.getConfig(5);

        assertEquals(
                List.of(
                        EnemyType.BASIC,
                        EnemyType.FAST,
                        EnemyType.TANK,
                        EnemyType.RANGED,
                        EnemyType.MINIBOSS
                ),
                config.composition().stream()
                        .map(entry -> entry.type())
                        .toList()
        );

        assertEquals(1, config.composition().get(0).count());
        assertEquals(2, config.composition().get(1).count());
        assertEquals(2, config.composition().get(2).count());
        assertEquals(1, config.composition().get(3).count());
        assertEquals(1, config.composition().get(4).count());
    }

    @Test
    void compositionCountShouldMatchEnemyCount() {
        for (int waveNumber = 1; waveNumber <= 20; waveNumber++) {
            WaveConfig config = WaveProgression.getConfig(waveNumber);

            assertEquals(
                    config.enemyCount(),
                    config.composition().stream()
                            .mapToInt(entry -> entry.count())
                            .sum()
            );
        }
    }

    @Test
    void advancedWaveShouldContainDifferentEnemyTypes() {
        WaveConfig config = WaveProgression.getConfig(6);

        assertEquals(3, config.composition().size());
        assertEquals(EnemyType.BASIC, config.composition().get(0).type());
        assertEquals(EnemyType.FAST, config.composition().get(1).type());
        assertEquals(EnemyType.TANK, config.composition().get(2).type());
    }

    @Test
    void waveTenShouldContainMiniBoss() {
        WaveConfig config = WaveProgression.getConfig(10);

        assertEquals(12, config.enemyCount());
        assertEquals(
                EnemyType.MINIBOSS,
                config.composition()
                        .get(config.composition().size() - 1)
                        .type()
        );
        assertEquals(
                1,
                config.composition()
                        .get(config.composition().size() - 1)
                        .count()
        );
    }

    @Test
    void waveFifteenShouldContainFinalBoss() {
        WaveConfig config = WaveProgression.getConfig(15);

        assertEquals(17, config.enemyCount());
        assertEquals(
                EnemyType.BOSS,
                config.composition()
                        .get(config.composition().size() - 1)
                        .type()
        );
        assertEquals(
                1,
                config.composition()
                        .get(config.composition().size() - 1)
                        .count()
        );
    }

    @Test
    void waveFifteenShouldNotContainMiniBoss() {
        WaveConfig config = WaveProgression.getConfig(15);

        assertEquals(
                0,
                config.composition().stream()
                        .filter(entry -> entry.type() == EnemyType.MINIBOSS)
                        .count()
        );
    }

    @Test
    void waveNumberZeroShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveProgression.getConfig(0)
        );
    }

    @Test
    void negativeWaveNumberShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveProgression.getConfig(-1)
        );
    }
}