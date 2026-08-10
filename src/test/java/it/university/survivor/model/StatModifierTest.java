package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatModifierTest {

    @Test 
    void testStatModifierCreation() {
        StatModifier modifierFlat = new StatModifier(StatType.MAX_HEALTH, ModifierType.FLAT, 20.0);
        assertEquals(StatType.MAX_HEALTH, modifierFlat.statType());
        assertEquals(ModifierType.FLAT, modifierFlat.modifierType());
        assertEquals(20.0, modifierFlat.baseValue());

        StatModifier modifierPercentage = new StatModifier(StatType.DAMAGE, ModifierType.PERCENTAGE, 0.05);
        assertEquals(ModifierType.PERCENTAGE, modifierPercentage.modifierType());
        assertEquals(0.05, modifierPercentage.baseValue());
    }
    
    @Test
    void storesStatTypeAndValue() {
        StatModifier modifier = new StatModifier(StatType.MAX_HEALTH, ModifierType.FLAT, 15.0);

        assertAll(
            () -> assertEquals(StatType.MAX_HEALTH, modifier.statType()),
            () -> assertEquals(15.0, modifier.baseValue())
        );
    }

    @Test 
    void rejectsInvalidValues() {
        assertAll(
            () -> assertThrows(NullPointerException.class, () -> new StatModifier(null, ModifierType.FLAT, 10.0)),
            () -> assertThrows(NullPointerException.class, () -> new StatModifier(StatType.MAX_HEALTH, null, 10.0)),
            () -> assertThrows(IllegalArgumentException.class, () -> new StatModifier(StatType.MAX_HEALTH, ModifierType.FLAT, Double.NaN)),
            () -> assertThrows(IllegalArgumentException.class, () -> new StatModifier(StatType.MAX_HEALTH, ModifierType.FLAT, Double.POSITIVE_INFINITY))
        );
    }

}

