package it.university.survivor.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CooldownUpgradeTest {

    @Test
    void shouldReduceCooldown() {
        WeaponStats original =
                new WeaponStats(
                        1.0,
                        10,
                        5.0
                );

        FlatCooldownUpgrade upgrade =
                new FlatCooldownUpgrade(0.2);

        WeaponStats upgraded =
                upgrade.apply(original);

        assertEquals(0.8, upgraded.getCooldownSeconds(), 1e-9);
        assertEquals(10, upgraded.getDamage());
        assertEquals(5.0, upgraded.getProjectileSpeed());
    }

    @Test
    void shouldKeepOtherStatsUnchanged() {
        WeaponStats original =
                new WeaponStats(
                        1.0,
                        10,
                        5.0
                );

        FlatCooldownUpgrade upgrade =
                new FlatCooldownUpgrade(0.2);

        WeaponStats upgraded =
                upgrade.apply(original);

        assertEquals(original.getDamage(), upgraded.getDamage());
        assertEquals(original.getProjectileSpeed(),
                upgraded.getProjectileSpeed());
    }

    @Test
    void shouldRespectMinimumCooldown() {
        WeaponStats original =
                new WeaponStats(
                        0.1,
                        10,
                        5.0
                );

        FlatCooldownUpgrade upgrade =
                new FlatCooldownUpgrade(1.0);

        WeaponStats upgraded =
                upgrade.apply(original);

        assertEquals(0.05, upgraded.getCooldownSeconds(), 1e-9);
    }
    @Test
void shouldReduceCooldownByPercentage() {
    WeaponStats original =
            new WeaponStats(
                    1.0,
                    10,
                    5.0
            );

    PercentCooldownUpgrade upgrade =
            new PercentCooldownUpgrade(0.05);

    WeaponStats upgraded =
            upgrade.apply(original);

    assertEquals(0.95, upgraded.getCooldownSeconds(), 1e-9);
    
}

}