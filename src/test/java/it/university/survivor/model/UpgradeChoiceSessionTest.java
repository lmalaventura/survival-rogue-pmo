package it.university.survivor.model;

import it.university.survivor.weapon.WeaponType;
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
    void generatesEpicRarityForRollInEpicRange() {
        Random epicRandom = new Random(0) {
            @Override
            public double nextDouble() {
                return 0.80;
            }
        };
        UpgradeChoiceSession session = new UpgradeChoiceSession(new UpgradeCatalog(), epicRandom);

        assertTrue(session.getCurrentOptions().stream()
                .allMatch(item -> item.rarity() == Rarity.EPIC));
    }

    @Test
    void handlesRerollsCorrectly() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(42));

        assertEquals(2, session.getRemainingRerolls());

        List<Item> firstOffer = session.getCurrentOptions();
        session.reroll();

        assertEquals(1, session.getRemainingRerolls());
        List<Item> secondOffer = session.getCurrentOptions();
        assertNotEquals(firstOffer, secondOffer);
        session.reroll();
        assertEquals(0, session.getRemainingRerolls());

        assertThrows(IllegalStateException.class, session::reroll);

    }
    @Test
    void handlesRerollsCorrectly2To1To0AndRejectsThird() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(42));

        assertEquals(2, session.getRemainingRerolls());

        List<Item> firstOffer = session.getCurrentOptions();
        session.reroll();
        assertEquals(1, session.getRemainingRerolls());
        List<Item> secondOffer = session.getCurrentOptions();
        assertNotEquals(firstOffer, secondOffer);

        session.reroll();
        assertEquals(0, session.getRemainingRerolls());

        assertThrows(IllegalStateException.class, session::reroll);
    }

    @Test
    void handlesSelectionByValidAndInvalidIndices() {
        UpgradeChoiceSession sessionForBoundTest = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(42));
        assertThrows(IndexOutOfBoundsException.class, () -> sessionForBoundTest.selectOption(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> sessionForBoundTest.selectOption(3));

        for(int index = 0; index < 3; index++) {
        UpgradeChoiceSession session = new UpgradeChoiceSession(new UpgradeCatalog(), new Random(42));
        List<Item> options = session.getCurrentOptions();


        Item expected = options.get(index);
        Item selected = session.selectOption(index);

        assertEquals(expected, selected);
        assertTrue(session.isSelectionMade());
        assertEquals(selected, session.getSelectedItem());

        assertThrows(IllegalStateException.class, () -> session.selectOption(0));
        assertThrows(IllegalStateException.class, session::reroll);
    }
 }

    @Test
    void mixedSessionOffersTwoItemsAndOneWeapon() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(
                new UpgradeCatalog(),
                new Random(42),
                List.of(new WeaponUpgradeChoice(WeaponType.SHOTGUN, 0, 5))
        );

        List<UpgradeOption> choices = session.getCurrentChoices();

        assertAll(
                () -> assertEquals(3, choices.size()),
                () -> assertEquals(2, choices.stream().filter(UpgradeOption::isItem).count()),
                () -> assertEquals(1, choices.stream().filter(UpgradeOption::isWeapon).count()),
                () -> assertEquals(
                        WeaponType.SHOTGUN,
                        choices.stream()
                                .filter(UpgradeOption::isWeapon)
                                .findFirst()
                                .orElseThrow()
                                .weaponChoice()
                                .weaponType()
                )
        );
    }


}
