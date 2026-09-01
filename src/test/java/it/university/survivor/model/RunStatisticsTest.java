package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class RunStatisticsTest {
    @Test
    void initialStateIsZeroEmpty() {
        RunStatistics stats = new RunStatistics();
        assertEquals(0, stats.getEnemiesDefeated());
        assertEquals(0, stats.getWavesCompleted());
        assertEquals(0, stats.getExperienceGained());
        assertEquals(0, stats.getUpgradesChosen());
        assertEquals(0, stats.getWeaponChoicesMade());
        assertEquals(0, stats.getRerollsUsed());
        assertEquals(0.0, stats.getElapsedTime(), 0.0001);
        assertTrue(stats.getChosenItems().isEmpty());
    }
    @Test
    void recordsEventsCorrectly() {
        RunStatistics stats = new RunStatistics();
        StatModifier mod = new StatModifier(StatType.DAMAGE, ModifierType.FLAT,5.0);
        Item item = new Item("Modulo Potenza", Rarity.COMMON, mod);

        stats.recordEnemyDefeated();
        stats.recordWaveCompleted();
        stats.recordExperienceGained(150);
        stats.recordUpgradeSelected(item);
        stats.recordWeaponChoice();
        stats.recordReroll();
        stats.addElapsedTime(45.5);

        assertEquals(1, stats.getEnemiesDefeated());
        assertEquals(1, stats.getWavesCompleted());
        assertEquals(150, stats.getExperienceGained());
        assertEquals(1, stats.getRerollsUsed());
        assertEquals(1, stats.getUpgradesChosen());
        assertEquals(1, stats.getWeaponChoicesMade());
        assertEquals(45.5, stats.getElapsedTime(), 0.0001);

        assertEquals(1, stats.getChosenItems().size());
        assertEquals(item, stats.getChosenItems().get(0));
    }
    @Test
    void rejectsInvalidRecordInputs(){
        RunStatistics stats = new RunStatistics();

        assertThrows(IllegalArgumentException.class, () -> stats.recordExperienceGained(-5));
        assertThrows(NullPointerException.class, () -> stats.recordUpgradeSelected(null));
        assertThrows(IllegalArgumentException.class, () -> stats.addElapsedTime(-1.0));
        assertThrows(IllegalArgumentException.class, () -> stats.addElapsedTime(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> stats.addElapsedTime(Double.POSITIVE_INFINITY));
        }
        @Test
        void chosenItemsListIsUnmodifiable() {
            RunStatistics stats = new RunStatistics();
            assertThrows(UnsupportedOperationException.class, () -> stats.getChosenItems().clear());
        }


}
