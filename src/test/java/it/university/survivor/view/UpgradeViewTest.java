package it.university.survivor.view;

import it.university.survivor.model.Item;
import it.university.survivor.model.ModifierType;
import it.university.survivor.model.Rarity;
import it.university.survivor.model.StatModifier;
import it.university.survivor.model.StatType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UpgradeViewTest {

    @Test
    void formatsEffectiveUpgradeValuesByStatAndModifierType() {
        assertAll(
                () -> assertEquals(
                        "+20 MAX HEALTH",
                        formatEffect(StatType.MAX_HEALTH, ModifierType.FLAT, 20.0)
                ),
                () -> assertEquals(
                        "+10% MAX HEALTH",
                        formatEffect(StatType.MAX_HEALTH, ModifierType.PERCENTAGE, 0.10)
                ),
                () -> assertEquals(
                        "+5 DAMAGE",
                        formatEffect(StatType.DAMAGE, ModifierType.FLAT, 5.0)
                ),
                () -> assertEquals(
                        "+8% DAMAGE",
                        formatEffect(StatType.DAMAGE, ModifierType.PERCENTAGE, 0.08)
                ),
                () -> assertEquals(
                        "-0.10s COOLDOWN",
                        formatEffect(StatType.COOLDOWN, ModifierType.FLAT, -0.10)
                ),
                () -> assertEquals(
                        "-10% COOLDOWN",
                        formatEffect(StatType.COOLDOWN, ModifierType.PERCENTAGE, -0.10)
                )
        );
    }

    @Test
    void formatsTheEffectiveValueAfterApplyingRarityMultiplier() {
        Item epicCooldown = new Item(
                "Epic cooldown",
                Rarity.EPIC,
                new StatModifier(StatType.COOLDOWN, ModifierType.FLAT, -0.10)
        );
        Item rarePercentage = new Item(
                "Rare percentage",
                Rarity.RARE,
                new StatModifier(
                        StatType.COOLDOWN,
                        ModifierType.PERCENTAGE,
                        -0.15
                )
        );
        Item rareFlatDamage = new Item(
                "Rare flat damage",
                Rarity.RARE,
                new StatModifier(StatType.DAMAGE, ModifierType.FLAT, 5.0)
        );

        assertAll(
                () -> assertEquals(
                        "-0.20s COOLDOWN",
                        UpgradeView.formatEffect(epicCooldown)
                ),
                () -> assertEquals(
                        "-22.5% COOLDOWN",
                        UpgradeView.formatEffect(rarePercentage)
                ),
                () -> assertEquals(
                        "+7.5 DAMAGE",
                        UpgradeView.formatEffect(rareFlatDamage)
                )
        );
    }

    @Test
    void doesNotDisplayNegativeZeroAfterRounding() {
        String formatted = formatEffect(
                StatType.COOLDOWN,
                ModifierType.FLAT,
                -0.001
        );

        assertAll(
                () -> assertEquals("+0.00s COOLDOWN", formatted),
                () -> assertFalse(formatted.contains("-0"))
        );
    }

    private static String formatEffect(
            StatType statType,
            ModifierType modifierType,
            double baseValue
    ) {
        Item item = new Item(
                "Test upgrade",
                Rarity.COMMON,
                new StatModifier(statType, modifierType, baseValue)
        );
        return UpgradeView.formatEffect(item);
    }
}
