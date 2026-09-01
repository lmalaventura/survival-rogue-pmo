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
    void itemOnlySessionOffersThreeDistinctItems() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(
                new UpgradeCatalog(),
                new Random(42L)
        );

        List<UpgradeOption> choices = session.getCurrentChoices();
        assertEquals(3, choices.size());
        assertTrue(choices.stream().allMatch(UpgradeOption::isItem));

        Set<String> names = new HashSet<>();
        for (UpgradeOption choice : choices) {
            assertTrue(names.add(choice.item().name()));
            assertNotNull(choice.item().rarity());
            assertNotNull(choice.item().baseModifier());
        }
        assertThrows(UnsupportedOperationException.class, () -> choices.clear());
    }

    @Test
    void fixedSeedProducesDeterministicChoices() {
        UpgradeChoiceSession first = new UpgradeChoiceSession(
                new UpgradeCatalog(),
                new Random(123L)
        );
        UpgradeChoiceSession second = new UpgradeChoiceSession(
                new UpgradeCatalog(),
                new Random(123L)
        );

        assertEquals(first.getCurrentChoices(), second.getCurrentChoices());
    }

    @Test
    void rarityRollMapsToExpectedTier() {
        Random epicRandom = new Random(0L) {
            @Override
            public double nextDouble() {
                return 0.80;
            }
        };
        UpgradeChoiceSession session = new UpgradeChoiceSession(
                new UpgradeCatalog(),
                epicRandom
        );

        assertTrue(session.getCurrentChoices().stream()
                .filter(UpgradeOption::isItem)
                .allMatch(choice -> choice.item().rarity() == Rarity.EPIC));
    }

    @Test
    void rerollsAreLimitedToTwoAndRegenerateChoices() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(
                new UpgradeCatalog(),
                new Random(42L)
        );
        List<UpgradeOption> firstOffer = session.getCurrentChoices();

        session.reroll();
        List<UpgradeOption> secondOffer = session.getCurrentChoices();
        session.reroll();

        assertAll(
                () -> assertNotEquals(firstOffer, secondOffer),
                () -> assertEquals(0, session.getRemainingRerolls()),
                () -> assertThrows(IllegalStateException.class, session::reroll)
        );
    }

    @Test
    void selectionAcceptsOnlyOneValidIndex() {
        UpgradeChoiceSession session = new UpgradeChoiceSession(
                new UpgradeCatalog(),
                new Random(42L)
        );

        assertThrows(IndexOutOfBoundsException.class, () -> session.selectChoice(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> session.selectChoice(3));

        UpgradeOption expected = session.getCurrentChoices().get(1);
        UpgradeOption selected = session.selectChoice(1);

        assertAll(
                () -> assertEquals(expected, selected),
                () -> assertEquals(selected, session.getSelectedChoice()),
                () -> assertTrue(session.isSelectionMade()),
                () -> assertThrows(IllegalStateException.class, () -> session.selectChoice(0)),
                () -> assertThrows(IllegalStateException.class, session::reroll)
        );
    }

    @Test
    void mixedSessionOffersTwoItemsAndOneWeapon() {
        WeaponUpgradeChoice weaponChoice = new WeaponUpgradeChoice(
                WeaponType.SHOTGUN,
                0,
                5
        );
        UpgradeChoiceSession session = new UpgradeChoiceSession(
                new UpgradeCatalog(),
                new Random(42L),
                List.of(weaponChoice)
        );

        long itemCount = session.getCurrentChoices().stream()
                .filter(UpgradeOption::isItem)
                .count();
        long weaponCount = session.getCurrentChoices().stream()
                .filter(UpgradeOption::isWeapon)
                .count();

        assertAll(
                () -> assertEquals(2L, itemCount),
                () -> assertEquals(1L, weaponCount),
                () -> assertTrue(session.getCurrentChoices().stream()
                        .filter(UpgradeOption::isWeapon)
                        .anyMatch(choice -> choice.weaponChoice().equals(weaponChoice)))
        );
    }
}
