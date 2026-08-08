package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemTest {

    @Test
    void calculatesEffectiveValueBasedOnRarity() {
        StatModifier modifier = new StatModifier(StatType.MAX_HEALTH, 20.0);
        Item commonItem = new Item("Elisir", Rarity.COMMON, modifier);
        Item rareItem = new Item("Elisir", Rarity.RARE, modifier);
        Item epicItem = new Item("elisi", Rarity.EPIC, modifier);

        assertAll(
            () -> assertEquals(20.0, commonItem.getEffectiveValue()),
            () -> assertEquals(30.0, rareItem.getEffectiveValue()),
            () -> assertEquals(40.0, epicItem.getEffectiveValue())
        );
    }

    @Test
    void rejectsInvalidConstructorParameters() {
        StatModifier modifier = new StatModifier(StatType.MAX_HEALTH, 10.0);

        assertAll(
            () -> assertThrows(NullPointerException.class, () -> new Item(null, Rarity.COMMON, modifier)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Item("", Rarity.COMMON, modifier)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Item("   ", Rarity.COMMON, modifier)),
            () -> assertThrows(NullPointerException.class, () -> new Item("Elisir", null, modifier)),
            () -> assertThrows(NullPointerException.class, () -> new Item("Elisir", Rarity.COMMON, null))
        );
    }
}
