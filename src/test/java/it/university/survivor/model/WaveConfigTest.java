package it.university.survivor.model;

import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.EnemyWaveEntry;
import it.university.survivor.model.enemy.WaveConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaveConfigTest {

    private static final List<EnemyWaveEntry> BASIC_THREE =
            List.of(new EnemyWaveEntry(EnemyType.BASIC, 3));

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
        assertEquals(100, config.enemyHealth());
        assertEquals(80.0, config.enemySpeed(), 1e-9);
        assertEquals(3, config.composition().size());
    }

    @Test
    void shouldRejectInvalidScalarValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new WaveConfig(0, 3, 100, 80.0, BASIC_THREE));
        assertThrows(IllegalArgumentException.class,
                () -> new WaveConfig(1, 0, 100, 80.0, BASIC_THREE));
        assertThrows(IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 0, 80.0, BASIC_THREE));
        assertThrows(IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 100, 0.0, BASIC_THREE));
        assertThrows(IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 100, Double.NaN, BASIC_THREE));
    }

    @Test
    void shouldRejectInvalidComposition() {
        assertThrows(NullPointerException.class,
                () -> new WaveConfig(1, 3, 100, 80.0, null));
        assertThrows(IllegalArgumentException.class,
                () -> new WaveConfig(1, 3, 100, 80.0, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new WaveConfig(
                        1,
                        3,
                        100,
                        80.0,
                        List.of(new EnemyWaveEntry(EnemyType.BASIC, 2))
                ));
    }

    @Test
    void shouldDefensivelyCopyComposition() {
        WaveConfig config = new WaveConfig(1, 3, 100, 80.0, BASIC_THREE);
        assertThrows(
                UnsupportedOperationException.class,
                () -> config.composition().add(new EnemyWaveEntry(EnemyType.FAST, 1))
        );
    }
}
