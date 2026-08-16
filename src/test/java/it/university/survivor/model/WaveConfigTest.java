package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import it.university.survivor.model.enemy.WaveConfig;

class WaveConfigTest {

    @Test
    void shouldCreateValidWaveConfig() {
        WaveConfig config = new WaveConfig(1, 3, 100, 80.0);

        assertEquals(1, config.waveNumber());
        assertEquals(3, config.enemyCount());
        assertEquals(100, config.enemyHealth());
        assertEquals(80.0, config.enemySpeed());
    }

    @Test
    void shouldRejectZeroWaveNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(0, 3, 100, 80.0)
        );
    }

    @Test
    void shouldRejectNegativeWaveNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(-1, 3, 100, 80.0)
        );
    }

    @Test
    void shouldRejectZeroEnemyCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(1, 0, 100, 80.0)
        );
    }

    @Test
    void shouldRejectNegativeEnemyCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(1, -1, 100, 80.0)
        );
    }

    @Test
    void shouldRejectZeroEnemyHealth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 0, 80.0)
        );
    }

    @Test
    void shouldRejectNegativeEnemyHealth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, -1, 80.0)
        );
    }

    @Test
    void shouldRejectZeroEnemySpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 100, 0.0)
        );
    }

    @Test
    void shouldRejectNegativeEnemySpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 100, -1.0)
        );
    }

    @Test
    void shouldRejectInfiniteEnemySpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 100, Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void shouldRejectNaNEnemySpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 100, Double.NaN)
        );
    }
}