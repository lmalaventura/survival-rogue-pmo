package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        assertEquals(2, config.composition().get(0).count());
        assertEquals(
                EnemyType.BASIC,
                config.composition().get(0).type()
        );

        assertEquals(2, config.composition().get(1).count());
        assertEquals(
                EnemyType.FAST,
                config.composition().get(1).type()
        );
    }

    @Test
    void waveThreeShouldHaveCorrectEnemyComposition() {
        WaveConfig config = WaveProgression.getConfig(3);

        assertEquals(2, config.composition().get(0).count());
        assertEquals(
                EnemyType.BASIC,
                config.composition().get(0).type()
        );

        assertEquals(2, config.composition().get(1).count());
        assertEquals(
                EnemyType.FAST,
                config.composition().get(1).type()
        );

        assertEquals(1, config.composition().get(2).count());
        assertEquals(
                EnemyType.TANK,
                config.composition().get(2).type()
        );
    }

    @Test
    void waveFourShouldHaveCorrectEnemyComposition() {
        WaveConfig config = WaveProgression.getConfig(4);

        assertEquals(2, config.composition().get(0).count());
        assertEquals(
                EnemyType.BASIC,
                config.composition().get(0).type()
        );

        assertEquals(2, config.composition().get(1).count());
        assertEquals(
                EnemyType.FAST,
                config.composition().get(1).type()
        );

        assertEquals(2, config.composition().get(2).count());
        assertEquals(
                EnemyType.TANK,
                config.composition().get(2).type()
        );
    }

    @Test
    void waveFiveShouldHaveCorrectEnemyComposition() {
        WaveConfig config = WaveProgression.getConfig(5);

        assertEquals(1, config.composition().get(0).count());
        assertEquals(
                EnemyType.BASIC,
                config.composition().get(0).type()
        );

        assertEquals(2, config.composition().get(1).count());
        assertEquals(
                EnemyType.FAST,
                config.composition().get(1).type()
        );

        assertEquals(2, config.composition().get(2).count());
        assertEquals(
                EnemyType.TANK,
                config.composition().get(2).type()
        );

        assertEquals(1, config.composition().get(3).count());
        assertEquals(
                EnemyType.RANGED,
                config.composition().get(3).type()
        );

        assertEquals(1, config.composition().get(4).count());
        assertEquals(
                EnemyType.MINIBOSS,
                config.composition().get(4).type()
        );
    }

    @Test
    void compositionCountShouldMatchEnemyCount() {
        for (int waveNumber = 1; waveNumber <= 5; waveNumber++) {
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