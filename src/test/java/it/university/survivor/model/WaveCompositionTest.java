package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.EnemyWaveEntry;
import it.university.survivor.model.enemy.WaveComposition;

class WaveCompositionTest {

    @Test
    void shouldCreateCompositionWithDifferentEnemyTypes() {
        WaveComposition composition = new WaveComposition(List.of(
                new EnemyWaveEntry(EnemyType.BASIC, 3),
                new EnemyWaveEntry(EnemyType.FAST, 2),
                new EnemyWaveEntry(EnemyType.TANK, 1)
        ));

        assertEquals(3, composition.entries().get(0).count());
        assertEquals(EnemyType.FAST, composition.entries().get(1).type());
        assertEquals(6, composition.totalEnemyCount());
    }

    @Test
    void shouldCalculateTotalEnemyCount() {
        WaveComposition composition = new WaveComposition(List.of(
                new EnemyWaveEntry(EnemyType.BASIC, 2),
                new EnemyWaveEntry(EnemyType.FAST, 3),
                new EnemyWaveEntry(EnemyType.TANK, 1)
        ));

        assertEquals(6, composition.totalEnemyCount());
    }

    @Test
    void shouldRejectNullEntries() {
        assertThrows(
                NullPointerException.class,
                () -> new WaveComposition(null)
        );
    }

    @Test
    void shouldRejectEmptyComposition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaveComposition(List.of())
        );
    }

    @Test
    void shouldKeepEntriesUnmodifiable() {
        WaveComposition composition = new WaveComposition(List.of(
                new EnemyWaveEntry(EnemyType.BASIC, 3)
        ));

        assertThrows(
                UnsupportedOperationException.class,
                () -> composition.entries().add(
                        new EnemyWaveEntry(EnemyType.FAST, 1)
                )
        );
    }

    @Test
    void shouldSupportBossComposition() {
        WaveComposition composition = new WaveComposition(List.of(
                new EnemyWaveEntry(EnemyType.TANK, 2),
                new EnemyWaveEntry(EnemyType.FAST, 2),
                new EnemyWaveEntry(EnemyType.MINIBOSS, 1),
                new EnemyWaveEntry(EnemyType.BOSS, 1)
        ));

        assertEquals(6, composition.totalEnemyCount());
    }
}