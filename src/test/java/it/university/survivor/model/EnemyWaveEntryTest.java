package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.EnemyWaveEntry;

class EnemyWaveEntryTest {

    @Test
    void shouldCreateValidEntry() {
        EnemyWaveEntry entry =
                new EnemyWaveEntry(EnemyType.FAST, 3);

        assertEquals(EnemyType.FAST, entry.type());
        assertEquals(3, entry.count());
    }

    @Test
    void shouldRejectNullEnemyType() {
        assertThrows(
                NullPointerException.class,
                () -> new EnemyWaveEntry(null, 3)
        );
    }

    @Test
    void shouldRejectZeroCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemyWaveEntry(EnemyType.BASIC, 0)
        );
    }

    @Test
    void shouldRejectNegativeCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemyWaveEntry(EnemyType.BASIC, -1)
        );
    }

    @Test
    void shouldSupportBossEntry() {
        EnemyWaveEntry entry =
                new EnemyWaveEntry(EnemyType.BOSS, 1);

        assertEquals(EnemyType.BOSS, entry.type());
        assertEquals(1, entry.count());
    }
}