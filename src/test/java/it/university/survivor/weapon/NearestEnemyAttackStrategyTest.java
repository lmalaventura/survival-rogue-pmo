package it.university.survivor.weapon;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NearestEnemyAttackStrategyTest {

    private final AttackStrategy strategy = new NearestEnemyAttackStrategy();
    private final WeaponStats stats = new WeaponStats(1.0, 20, 5.0);

    @Test
    void shouldAttackNearestLivingEnemy() {
        Position player = new Position(0, 0);
        Enemy far = new Enemy(new Position(10, 0), 100, 1.0);
        Enemy dead = new Enemy(new Position(1, 0), 100, 1.0);
        Enemy near = new Enemy(new Position(0, 3), 100, 1.0);
        dead.takeDamage(100);

        List<ProjectileSpawnRequest> requests = strategy.attack(
                player,
                List.of(far, dead, near),
                stats
        );

        assertEquals(1, requests.size());
        ProjectileSpawnRequest request = requests.get(0);
        assertEquals(player, request.origin());
        assertEquals(0.0, request.directionX(), 1e-9);
        assertEquals(1.0, request.directionY(), 1e-9);
        assertEquals(20, request.damage());
        assertEquals(5.0, request.speed(), 1e-9);
    }

    @Test
    void shouldReturnEmptyWhenNoLivingTargetExists() {
        Enemy dead = new Enemy(new Position(5, 0), 100, 1.0);
        dead.takeDamage(100);

        assertTrue(strategy.attack(new Position(0, 0), List.of(), stats).isEmpty());
        assertTrue(strategy.attack(new Position(0, 0), List.of(dead), stats).isEmpty());
    }

    @Test
    void shouldNormalizeDirection() {
        Enemy enemy = new Enemy(new Position(3, 4), 100, 1.0);

        ProjectileSpawnRequest request = strategy.attack(
                new Position(0, 0),
                List.of(enemy),
                stats
        ).get(0);

        assertEquals(0.6, request.directionX(), 1e-9);
        assertEquals(0.8, request.directionY(), 1e-9);
    }

    @Test
    void shouldUseStableDirectionWhenTargetOverlapsPlayer() {
        Enemy enemy = new Enemy(new Position(0, 0), 100, 1.0);

        ProjectileSpawnRequest request = strategy.attack(
                new Position(0, 0),
                List.of(enemy),
                stats
        ).get(0);

        assertEquals(1.0, request.directionX(), 1e-9);
        assertEquals(0.0, request.directionY(), 1e-9);
    }
}
