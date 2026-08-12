package it.university.survivor.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class UpgradeChoiceSessionTest {

    @Test
    void generateExactlyThreeValidNonDuplicateOptions() {
        UpgradeCatalog catalog = new UpgradeCatalog();
        UpgradeChoiceSession session = new UpgradeChoiceSession(catalog, new Random(42));

        List<Item> options = session.getCurrentOptions();
        assertEquals(3, options.size());

        for (Item item : options) {
            assertNotNull(item);
            assertNotNull(item.name());
            assertNotNull(item.rarity());
            assertNotNull(item.baseModifier());
        }

        Set<String> nameSet = new HashSet<>();
        for (Item item : options) {
            nameSet.add(item.name());
        }
        assertEquals(3, nameSet.size());
    }

    @Test
    void currentOptionsListIsUnmodifiable() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(42));
        List<Item> options = session.getCurrentOptions();

        assertThrows(UnsupportedOperationException.class, () -> options.clear());
    }

    @Test
    void deterministicBehaviorWithFixedSeed() {
        UpgradeChoiceSession session1 = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(12345));
        UpgradeChoiceSession session2 = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(12345));

        List<Item> options1 = session1.getCurrentOptions();
        List<Item> options2 = session2.getCurrentOptions();

        for (int i = 0; i < 3; i++) {
            assertEquals(options1.get(i).name(), options2.get(i).name());
            assertEquals(options1.get(i).rarity(), options2.get(i).rarity());
            assertEquals(options1.get(i).getEffectiveValue(), options2.get(i).getEffectiveValue());
        }
    }

    @Test
    void handlesRerollsCorrectly() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(42));

        assertEquals(1, session.getRemainingRerolls());

        session.reroll();

        assertEquals(0, session.getRemainingRerolls());
        List<Item> newOptions = session.getCurrentOptions();

        assertEquals(3, newOptions.size());
        for(Item item : newOptions) {
            assertNotNull(item);
            assertNotNull(item.rarity());
        }
        assertThrows(IllegalStateException.class, session::reroll);

    }

    @Test
    void handlesSelectionByValidAndInvalidIndices() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(42));
        List<Item> options = session.getCurrentOptions();

        assertThrows(IndexOutOfBoundsException.class, () -> session.selectOption(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> session.selectOption(3));

        Item expected = options.get(1);
        Item selected = session.selectOption(1);

        assertEquals(expected, selected);
        assertTrue(session.isSelectionMade());
        assertEquals(selected, session.getSelectedItem());

        assertThrows(IllegalStateException.class, () -> session.selectOption(0));
        assertThrows(IllegalStateException.class, session::reroll);
    }
}
