package it.university.survivor.weapon;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

class NearestEnemyAttackStrategyTest {

    private final AttackStrategy strategy =
            new NearestEnemyAttackStrategy();

    private final WeaponStats stats =
            new WeaponStats(1.0, 20, 5.0);

    @Test
    void shouldAttackNearestEnemy() {
        Position playerPosition = new Position(0, 0);

        Enemy farEnemy =
                new Enemy(
                        new Position(10, 0),
                        100,
                        1.0
                );

        Enemy nearEnemy =
                new Enemy(
                        new Position(3, 0),
                        100,
                        1.0
                );

        Optional<ProjectileSpawnRequest> result =
                strategy.attack(
                        playerPosition,
                        List.of(farEnemy, nearEnemy),
                        stats
                );

        assertTrue(result.isPresent());

        ProjectileSpawnRequest request = result.get();

        assertEquals(playerPosition, request.origin());
        assertEquals(1.0, request.directionX(), 1e-9);
        assertEquals(0.0, request.directionY(), 1e-9);
        assertEquals(20, request.damage());
        assertEquals(5.0, request.speed());
    }

    @Test
    void shouldReturnEmptyWhenThereAreNoEnemies() {
        Optional<ProjectileSpawnRequest> result =
                strategy.attack(
                        new Position(0, 0),
                        List.of(),
                        stats
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreDeadEnemies() {
        Enemy deadEnemy =
                new Enemy(
                        new Position(1, 0),
                        100,
                        1.0
                );

        deadEnemy.takeDamage(100);

        Enemy aliveEnemy =
                new Enemy(
                        new Position(0, 5),
                        100,
                        1.0
                );

        Optional<ProjectileSpawnRequest> result =
                strategy.attack(
                        new Position(0, 0),
                        List.of(deadEnemy, aliveEnemy),
                        stats
                );

        assertTrue(result.isPresent());

        ProjectileSpawnRequest request = result.get();

        assertEquals(0.0, request.directionX(), 1e-9);
        assertEquals(1.0, request.directionY(), 1e-9);
    }

    @Test
    void shouldReturnEmptyWhenAllEnemiesAreDead() {
        Enemy enemy =
                new Enemy(
                        new Position(5, 0),
                        100,
                        1.0
                );

        enemy.takeDamage(100);

        Optional<ProjectileSpawnRequest> result =
                strategy.attack(
                        new Position(0, 0),
                        List.of(enemy),
                        stats
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCalculateNormalizedDirection() {
        Enemy enemy =
                new Enemy(
                        new Position(3, 4),
                        100,
                        1.0
                );

        Optional<ProjectileSpawnRequest> result =
                strategy.attack(
                        new Position(0, 0),
                        List.of(enemy),
                        stats
                );

        assertTrue(result.isPresent());

        ProjectileSpawnRequest request = result.get();

        assertEquals(0.6, request.directionX(), 1e-9);
        assertEquals(0.8, request.directionY(), 1e-9);
    }

    @Test
    void shouldHandleEnemyAtSamePositionAsPlayer() {
        Enemy enemy =
                new Enemy(
                        new Position(0, 0),
                        100,
                        1.0
                );

        Optional<ProjectileSpawnRequest> result =
                strategy.attack(
                        new Position(0, 0),
                        List.of(enemy),
                        stats
                );

        assertTrue(result.isPresent());

        ProjectileSpawnRequest request = result.get();

        assertEquals(1.0, request.directionX(), 1e-9);
        assertEquals(0.0, request.directionY(), 1e-9);
    }
}