package it.university.survivor.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class WeaponStatsTest {

    @Test
    void shouldCreateValidWeaponStats() {
        WeaponStats stats = new WeaponStats(1.5, 20, 7.0);

        assertEquals(1.5, stats.getCooldownSeconds());
        assertEquals(20, stats.getDamage());
        assertEquals(7.0, stats.getProjectileSpeed());
    }

    @Test
    void shouldRejectZeroCooldown() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(0.0, 10, 5.0)
        );
    }

    @Test
    void shouldRejectNegativeCooldown() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(-1.0, 10, 5.0)
        );
    }

    @Test
    void shouldRejectInvalidCooldown() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(Double.NaN, 10, 5.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(Double.POSITIVE_INFINITY, 10, 5.0)
        );
    }

    @Test
    void shouldRejectZeroDamage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(1.0, 0, 5.0)
        );
    }

    @Test
    void shouldRejectNegativeDamage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(1.0, -10, 5.0)
        );
    }

    @Test
    void shouldRejectZeroProjectileSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(1.0, 10, 0.0)
        );
    }

    @Test
    void shouldRejectNegativeProjectileSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(1.0, 10, -5.0)
        );
    }

    @Test
    void shouldRejectInvalidProjectileSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(1.0, 10, Double.NaN)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new WeaponStats(1.0, 10, Double.POSITIVE_INFINITY)
        );
    }
}