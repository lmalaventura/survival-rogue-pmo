package it.university.survivor.weapon;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponTest {

    private Weapon weapon;
    private WeaponStats stats;

    @BeforeEach
    void setUp() {
        stats = new WeaponStats(1.0, 10, 5.0);
        weapon = new Weapon(stats, new NearestEnemyAttackStrategy());
    }

    @Test
    void shouldStartReadyWithConfiguredStats() {
        assertTrue(weapon.canAttack());
        assertEquals(0.0, weapon.getCooldownRemaining(), 1e-9);
        assertSame(stats, weapon.getCurrentStats());
    }

    @Test
    void shouldAttackAndStartCooldown() {
        List<ProjectileSpawnRequest> requests = weapon.attack(
                new Position(0, 0),
                List.of(enemyAt(5, 0))
        );

        assertEquals(1, requests.size());
        assertEquals(10, requests.get(0).damage());
        assertEquals(5.0, requests.get(0).speed(), 1e-9);
        assertFalse(weapon.canAttack());
        assertEquals(1.0, weapon.getCooldownRemaining(), 1e-9);
        assertTrue(weapon.attack(new Position(0, 0), List.of(enemyAt(5, 0))).isEmpty());
    }

    @Test
    void shouldNotStartCooldownWhenAttackProducesNothing() {
        assertTrue(weapon.attack(new Position(0, 0), List.of()).isEmpty());
        assertTrue(weapon.canAttack());
        assertEquals(0.0, weapon.getCooldownRemaining(), 1e-9);
    }

    @Test
    void shouldUpdateCooldownWithoutGoingNegative() {
        weapon.attack(new Position(0, 0), List.of(enemyAt(5, 0)));
        weapon.update(0.4);
        assertEquals(0.6, weapon.getCooldownRemaining(), 1e-9);

        weapon.update(5.0);
        assertEquals(0.0, weapon.getCooldownRemaining(), 1e-9);
        assertTrue(weapon.canAttack());
    }

    @Test
    void shouldRejectInvalidDeltaTime() {
        assertThrows(IllegalArgumentException.class, () -> weapon.update(-0.1));
        assertThrows(IllegalArgumentException.class, () -> weapon.update(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> weapon.update(Double.POSITIVE_INFINITY));
    }

    @Test
    void shouldUseUpgradedDamage() {
        weapon.upgrade(new PercentDamageUpgrade(0.10));

        ProjectileSpawnRequest request = weapon.attack(
                new Position(0, 0),
                List.of(enemyAt(5, 0))
        ).get(0);

        assertEquals(11, request.damage());
    }

    @Test
    void shouldUseUpgradedCooldown() {
        weapon.upgrade(new PercentCooldownUpgrade(0.05));
        weapon.attack(new Position(0, 0), List.of(enemyAt(5, 0)));

        assertEquals(0.95, weapon.getCooldownRemaining(), 1e-9);
    }

    private static Enemy enemyAt(double x, double y) {
        return new Enemy(new Position(x, y), 100, 1.0);
    }
}
