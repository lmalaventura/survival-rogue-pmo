package it.university.survivor.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class DamageUpgradeTest {

    @Test
    void shouldIncreaseDamage() {
        WeaponStats original =
                new WeaponStats(
                        1.0,
                        10,
                        5.0
                );

        DamageUpgrade upgrade =
                new DamageUpgrade(5);

        WeaponStats upgraded =
                upgrade.applyFlat(original);

        assertEquals(1.0, upgraded.getCooldownSeconds());
        assertEquals(15, upgraded.getDamage());
        assertEquals(5.0, upgraded.getProjectileSpeed());
    }

    @Test
    void shouldNotModifyOriginalStats() {
        WeaponStats original =
                new WeaponStats(
                        1.0,
                        10,
                        5.0
                );

        DamageUpgrade upgrade =
                new DamageUpgrade(5);

        WeaponStats upgraded =
                upgrade.applyFlat(original);

        assertEquals(10, original.getDamage());
        assertEquals(15, upgraded.getDamage());
    }

    @Test
    void shouldRejectNonPositiveBonus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DamageUpgrade(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new DamageUpgrade(-5)
        );
    }
    @Test
