package it.university.survivor.weapon;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpreadAttackStrategyTest {

    private final AttackStrategy strategy = new SpreadAttackStrategy();

    @Test
    void shouldCreateConfiguredSpread() {
        Enemy target = new Enemy(new Position(10, 0), 100, 1.0);
        WeaponStats stats = new WeaponStats(1.0, 10, 5.0, 3, 30.0);

        List<ProjectileSpawnRequest> requests = strategy.attack(
                new Position(0, 0),
                List.of(target),
                stats
        );

        assertEquals(3, requests.size());
        assertTrue(requests.get(0).directionY() < 0.0);
        assertEquals(0.0, requests.get(1).directionY(), 1e-9);
        assertTrue(requests.get(2).directionY() > 0.0);
    }

    @Test
    void shouldRequireLivingTarget() {
        WeaponStats stats = new WeaponStats(1.0, 10, 5.0, 3, 30.0);
        assertTrue(strategy.attack(new Position(0, 0), List.of(), stats).isEmpty());
    }
}
