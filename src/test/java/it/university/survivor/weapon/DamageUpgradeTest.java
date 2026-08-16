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

        FlatDamageUpgrade upgrade =
                new FlatDamageUpgrade(5);

        WeaponStats upgraded =
                upgrade.apply(original);

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

        FlatDamageUpgrade upgrade =
                new FlatDamageUpgrade(5);

        WeaponStats upgraded =
                upgrade.apply(original);

        assertEquals(10, original.getDamage());
        assertEquals(15, upgraded.getDamage());
    }

    @Test
    void shouldRejectNonPositiveBonus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FlatDamageUpgrade(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new FlatDamageUpgrade(-5)
        );
    }
@Test
void shouldIncreaseDamageByPercentage() {
    WeaponStats original =
            new WeaponStats(
                    1.0,
                    10,
                    5.0
            );

    PercentDamageUpgrade upgrade =
            new PercentDamageUpgrade(0.10);

    WeaponStats upgraded =
            upgrade.apply(original);

    assertEquals(11, upgraded.getDamage());
  }
  @Test
void shouldRoundPercentageDamage() {
    WeaponStats original =
            new WeaponStats(
                    1.0,
                    15,
                    5.0
            );

    PercentDamageUpgrade upgrade =
            new PercentDamageUpgrade(0.10);

    WeaponStats upgraded =
            upgrade.apply(original);

    assertEquals(17, upgraded.getDamage());
}
}
