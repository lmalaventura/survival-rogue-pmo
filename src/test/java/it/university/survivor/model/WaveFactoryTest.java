package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.Boss;
import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.MiniBoss;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveFactory;
import it.university.survivor.model.enemy.WaveProgression;

class WaveFactoryTest {

    private static final double TOLERANCE = 1.0e-9;

    @Test
    void shouldCreateBasicEnemiesUsingWaveBaseStats() {
        Wave wave = createWave(1);

        assertEquals(1, wave.getWaveNumber());
        assertEquals(3, wave.getEnemies().size());
        assertEquals(100, maxHealth(wave.getEnemies().get(0)));
        assertEquals(80.0, speed(wave.getEnemies().get(0)));
    }

    @Test
    void fastEnemyShouldMultiplyWaveBaseSpeedByOnePointEight() {
        Wave wave = createWave(2);
        Enemy fast = wave.getEnemies().get(3);

        assertEquals(EnemyType.FAST, fast.getType());
        assertEquals(60, maxHealth(fast));
        assertEquals(82.0 * 1.8, speed(fast), TOLERANCE);
    }

    @Test
    void tankEnemyShouldMultiplyWaveBaseSpeedByOneHalf() {
        Wave wave = createWave(4);
        Enemy tank = wave.getEnemies().get(5);

        assertEquals(EnemyType.TANK, tank.getType());
        assertEquals(250, maxHealth(tank));
        assertEquals(86.0 * 0.5, speed(tank), TOLERANCE);
    }

    @Test
    void waveFiveShouldCreateAStatisticallyDistinctMiniBoss() {
        Wave wave = createWave(5);
        Enemy miniBoss = wave.getEnemies().get(3);

        assertInstanceOf(MiniBoss.class, miniBoss);
        assertEquals(EnemyType.MINIBOSS, miniBoss.getType());
        assertEquals(500, maxHealth(miniBoss));
        assertEquals(500, miniBoss.getHealth().getCurrentHealth());
        assertEquals(88.0 * 0.7, speed(miniBoss), TOLERANCE);
    }

    @Test
    void waveTenShouldCreateAMiniBossUsingItsWaveBaseSpeed() {
        Wave wave = createWave(10);
        Enemy miniBoss = wave.getEnemies().get(7);

        assertInstanceOf(MiniBoss.class, miniBoss);
        assertEquals(500, maxHealth(miniBoss));
        assertEquals(98.0 * 0.7, speed(miniBoss), TOLERANCE);
    }

    @Test
    void waveFifteenShouldCreateTheFinalBoss() {
        Wave wave = createWave(15);
        Enemy boss = wave.getEnemies().get(9);

        assertInstanceOf(Boss.class, boss);
        assertEquals(EnemyType.BOSS, boss.getType());
        assertEquals(1000, maxHealth(boss));
        assertEquals(1000, boss.getHealth().getCurrentHealth());
        assertEquals(108.0 * 0.6, speed(boss), TOLERANCE);
    }

    @Test
    void shouldRejectNullOrWrongNumberOfSpawnPositions() {
        assertThrows(
                NullPointerException.class,
                () -> WaveFactory.createWave(1, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveFactory.createWave(1, createPositions(2))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveFactory.createWave(1, createPositions(4))
        );
    }

    @Test
    void shouldRejectWaveNumbersOutsideTheDemo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveFactory.createWave(0, createPositions(3))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveFactory.createWave(16, createPositions(3))
        );
    }

    private Wave createWave(int waveNumber) {
        WaveConfig config = WaveProgression.getConfig(waveNumber);
        return WaveFactory.createWave(
                waveNumber,
                createPositions(config.enemyCount())
        );
    }

    private List<Position> createPositions(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new Position(index * 10.0, 100.0))
                .toList();
    }

    private int maxHealth(Enemy enemy) {
        return enemy.getHealth().getMaxHealth();
    }

    private double speed(Enemy enemy) {
        return enemy.getMovementSpeed();
    }
}
