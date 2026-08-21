package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.EnemyWaveEntry;
import it.university.survivor.model.enemy.WaveConfig;

class WaveConfigTest {

    @Test
    void shouldCreateValidWaveConfig() {
        WaveConfig config = new WaveConfig(1, 3, 100, 80.0);

        assertEquals(1, config.waveNumber());
        assertEquals(3, config.enemyCount());
        assertEquals(100, config.enemyHealth());
        assertEquals(80.0, config.enemySpeed());
        assertEquals(1, config.composition().size());
        assertEquals(EnemyType.BASIC, config.composition().get(0).type());
        assertEquals(3, config.composition().get(0).count());
    }

    @Test
    void shouldCreateMixedWaveConfig() {
        WaveConfig config = new WaveConfig(
                3,
                5,
                100,
                80.0,
                List.of(
                        new EnemyWaveEntry(EnemyType.BASIC, 2),
                        new EnemyWaveEntry(EnemyType.FAST, 2),
                        new EnemyWaveEntry(EnemyType.TANK, 1)
                )
        );

        assertEquals(3, config.waveNumber());
        assertEquals(5, config.enemyCount());
        assertEquals(3, config.composition().size());
        assertEquals(EnemyType.BASIC, config.composition().get(0).type());
        assertEquals(EnemyType.FAST, config.composition().get(1).type());
        assertEquals(EnemyType.TANK, config.composition().get(2).type());
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

    @Test
    void shouldRejectNullComposition() {
        assertThrows(
                NullPointerException.class,
                () -> new WaveConfig(
                        1,
                        3,
                        100,
                        80.0,
                        null
                )
        );
    }

    @Test
    void shouldRejectEmptyComposition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(
                        1,
                        3,
                        100,
                        80.0,
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectCompositionWithWrongEnemyCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveConfig(
                        1,
                        3,
                        100,
                        80.0,
                        List.of(
                                new EnemyWaveEntry(
                                        EnemyType.BASIC,
                                        2
                                )
                        )
                )
        );
    }

    @Test
    void shouldKeepCompositionUnmodifiable() {
        WaveConfig config = new WaveConfig(
                1,
                3,
                100,
                80.0,
                List.of(
                        new EnemyWaveEntry(
                                EnemyType.BASIC,
                                3
                        )
                )
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> config.composition().add(
                        new EnemyWaveEntry(
                                EnemyType.FAST,
                                1
                        )
                )
        );
    }
}