package it.university.survivor.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class WeaponStatsTest {

@Test
void shouldCreateStatsWithDefaultProjectilePattern() {
    WeaponStats stats = new WeaponStats(1.0, 20, 5.0);

    assertEquals(1, stats.getProjectileCount());
    assertEquals(0.0, stats.getSpreadDegrees());
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
    @Test
void shouldRejectZeroProjectileCount() {
    assertThrows(
            IllegalArgumentException.class,
            () -> new WeaponStats(
                    1.0,
                    20,
                    5.0,
                    0,
                    30.0
            )
    );
}
@Test
void shouldRejectNegativeProjectileCount() {
    assertThrows(
            IllegalArgumentException.class,
            () -> new WeaponStats(
                    1.0,
                    20,
                    5.0,
                    -1,
                    30.0
            )
    );
}
@Test
void shouldRejectNegativeSpread() {
    assertThrows(
            IllegalArgumentException.class,
            () -> new WeaponStats(
                    1.0,
                    20,
                    5.0,
                    3,
                    -1.0
            )
    );
}
@Test
void shouldRejectSpreadGreaterThan360() {
    assertThrows(
            IllegalArgumentException.class,
            () -> new WeaponStats(
                    1.0,
                    20,
                    5.0,
                    3,
                    361.0
            )
    );
}
@Test
void shouldStoreProjectilePattern() {
    WeaponStats stats =
            new WeaponStats(1.0, 20, 5.0, 5, 60.0);

    assertEquals(5, stats.getProjectileCount());
    assertEquals(60.0, stats.getSpreadDegrees());
}
@Test
void shouldCreateStatsWithNewProjectilePattern() {
    WeaponStats original =
            new WeaponStats(1.0, 20, 5.0, 1, 0.0);

    WeaponStats updated =
            original.withProjectilePattern(5, 60.0);

    assertEquals(5, updated.getProjectileCount());
    assertEquals(60.0, updated.getSpreadDegrees());

    assertEquals(1.0, updated.getCooldownSeconds());
    assertEquals(20, updated.getDamage());
    assertEquals(5.0, updated.getProjectileSpeed());
}
}
