package it.university.survivor.weapon;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

class WeaponTest {

    private Weapon weapon;
    private WeaponStats stats;

    @BeforeEach
    void setUp() {
        stats = new WeaponStats(
                1.0,
                10,
                5.0
        );

        weapon = new Weapon(
                stats,
                new NearestEnemyAttackStrategy()
        );
    }

    @Test
    void shouldBeAbleToAttackInitially() {
        assertTrue(weapon.canAttack());
        assertEquals(0.0, weapon.getCooldown(), 1e-9);
    }

    @Test
    void shouldReturnCurrentStats() {
        assertSame(stats, weapon.getCurrentStats());
    }

    @Test
    void shouldAttackEnemy() {
        Enemy enemy =
                new Enemy(
                        new Position(5, 0),
                        100,
                        1.0
                );

        Optional<ProjectileSpawnRequest> result =
                weapon.attack(
                        new Position(0, 0),
                        List.of(enemy)
                );

        assertTrue(result.isPresent());

        ProjectileSpawnRequest request = result.get();

        assertEquals(10, request.damage());
        assertEquals(5.0, request.speed());
    }

    @Test
    void shouldEnterCooldownAfterSuccessfulAttack() {
        Enemy enemy =
                new Enemy(
                        new Position(5, 0),
                        100,
                        1.0
                );

        weapon.attack(
                new Position(0, 0),
                List.of(enemy)
        );

        assertFalse(weapon.canAttack());
        assertEquals(1.0, weapon.getCooldown(), 1e-9);
    }

    @Test
    void shouldNotAttackDuringCooldown() {
        Enemy enemy =
                new Enemy(
                        new Position(5, 0),
                        100,
                        1.0
                );

        Optional<ProjectileSpawnRequest> first =
                weapon.attack(
                        new Position(0, 0),
                        List.of(enemy)
                );

        Optional<ProjectileSpawnRequest> second =
                weapon.attack(
                        new Position(0, 0),
                        List.of(enemy)
                );

        assertTrue(first.isPresent());
        assertTrue(second.isEmpty());
    }

    @Test
    void shouldReduceCooldownAfterUpdate() {
        Enemy enemy =
                new Enemy(
                        new Position(5, 0),
                        100,
                        1.0
                );

        weapon.attack(
                new Position(0, 0),
                List.of(enemy)
        );

        weapon.update(0.4);

        assertEquals(0.6, weapon.getCooldown(), 1e-9);
    }

    @Test
    void shouldReachZeroCooldown() {
        Enemy enemy =
                new Enemy(
                        new Position(5, 0),
                        100,
                        1.0
                );

        weapon.attack(
                new Position(0, 0),
                List.of(enemy)
        );

        weapon.update(1.0);

        assertEquals(0.0, weapon.getCooldown(), 1e-9);
        assertTrue(weapon.canAttack());
    }

    @Test
    void shouldNeverHaveNegativeCooldown() {
        Enemy enemy =
                new Enemy(
                        new Position(5, 0),
                        100,
                        1.0
                );

        weapon.attack(
                new Position(0, 0),
                List.of(enemy)
        );

        weapon.update(5.0);

        assertEquals(0.0, weapon.getCooldown(), 1e-9);
    }

    @Test
    void shouldNotStartCooldownWhenThereIsNoValidTarget() {
        Optional<ProjectileSpawnRequest> result =
                weapon.attack(
                        new Position(0, 0),
                        List.of()
                );

        assertTrue(result.isEmpty());
        assertTrue(weapon.canAttack());
        assertEquals(0.0, weapon.getCooldown(), 1e-9);
    }

    @Test
    void shouldRejectNegativeDeltaTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> weapon.update(-0.1)
        );
    }

    @Test
    void shouldRejectNaNDeltaTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> weapon.update(Double.NaN)
        );
    }
    @Test
void shouldUseUpgradedDamageForNextAttack() {
    weapon.upgrade(new PercentDamageUpgrade(0.10));

    Enemy enemy =
            new Enemy(
                    new Position(5, 0),
                    100,
                    1.0
            );

    Optional<ProjectileSpawnRequest> result =
            weapon.attack(
                    new Position(0, 0),
                    List.of(enemy)
            );

    assertTrue(result.isPresent());
    assertEquals(11, result.get().damage());
}
@Test
void shouldUseUpgradedCooldownForNextAttack() {
    weapon.upgrade(new PercentCooldownUpgrade(0.05));

    Enemy enemy =
            new Enemy(
                    new Position(5, 0),
                    100,
                    1.0
            );

    Optional<ProjectileSpawnRequest> result =
            weapon.attack(
                    new Position(0, 0),
                    List.of(enemy)
            );

    assertTrue(result.isPresent());
    assertEquals(0.95, weapon.getCooldown(), 1e-9);
}
}