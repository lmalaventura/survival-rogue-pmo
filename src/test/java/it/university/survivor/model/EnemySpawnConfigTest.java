package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemySpawnConfig;
import it.university.survivor.model.enemy.EnemyType;

class EnemySpawnConfigTest {

    @Test
    void shouldCreateValidEnemySpawnConfig() {
        EnemySpawnConfig config = new EnemySpawnConfig(EnemyType.BASIC, 3);

        assertEquals(EnemyType.BASIC, config.type());
        assertEquals(3, config.count());
    }

    @Test
    void shouldRejectNullEnemyType() {
        assertThrows(
                NullPointerException.class,
                () -> new EnemySpawnConfig(null, 3)
        );
    }

    @Test
    void shouldRejectZeroCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpawnConfig(EnemyType.BASIC, 0)
        );
    }

    @Test
    void shouldRejectNegativeCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpawnConfig(EnemyType.BASIC, -1)
        );
    }
}