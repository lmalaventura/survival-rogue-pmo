package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatModifierTest {
    
    @Test
    void storesStatTypeAndValue() {
        StatModifier modifier = new StatModifier(StatType.MAX_HEALTH, 15.0);

        assertAll(
            () -> assertEquals(StatType.MAX_HEALTH, modifier.statType()),
            () -> assertEquals(15.0, modifier.baseValue())
        );
    }

    @Test 
    void rejectsInvalidValues() {
        assertAll(
            () -> assertThrows(NullPointerException.class, () -> new StatModifier(null, 10.0)),
            () -> assertThrows(IllegalArgumentException.class, () -> new StatModifier(StatType.MAX_HEALTH, Double.NaN)),
            () -> assertThrows(IllegalArgumentException.class, () -> new StatModifier(StatType.MAX_HEALTH, Double.POSITIVE_INFINITY))
        );
    }

}

