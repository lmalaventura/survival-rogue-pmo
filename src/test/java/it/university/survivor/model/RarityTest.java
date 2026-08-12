package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RarityTest {

    @Test
    void verifiesAllFiveRaritiesAndMultipliers() {
        assertEquals(5, Rarity.values().length);
        assertEquals(1.0, Rarity.COMMON.getMultiplier());
        assertEquals(1.5, Rarity.RARE.getMultiplier());
        assertEquals(2.0, Rarity.EPIC.getMultiplier());
        assertEquals(2.5, Rarity.LEGENDARY.getMultiplier());
        assertEquals(3.0, Rarity.ULTRA.getMultiplier());
    }
}
