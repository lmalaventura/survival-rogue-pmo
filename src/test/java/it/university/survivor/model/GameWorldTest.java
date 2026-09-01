package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameWorldTest {

    @Test
    void shouldExposeDimensionsPlayerAndInitialEnemies() {
        Player player = playerAt(50, 50);
        Enemy first = enemyAt(10, 10);
        Enemy second = enemyAt(20, 20);
        GameWorld world = new GameWorld(100, 80, player, List.of(first, second));

        assertEquals(100.0, world.getWidth(), 1e-9);
        assertEquals(80.0, world.getHeight(), 1e-9);
        assertSame(player, world.getPlayer());
        assertEquals(List.of(first, second), world.getEnemies());
        assertTrue(world.getProjectiles().isEmpty());
    }

    @Test
    void shouldValidateWorldAndInitialEntities() {
        Player player = playerAt(50, 50);
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(0, 100, player));
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(100, Double.NaN, player));
        assertThrows(NullPointerException.class, () -> new GameWorld(100, 100, null));
        assertThrows(IllegalArgumentException.class,
                () -> new GameWorld(100, 100, playerAt(101, 50)));
        assertThrows(NullPointerException.class,
                () -> new GameWorld(100, 100, player, null));
    }

    @Test
    void shouldDefensivelyCopyAndProtectEnemyList() {
        Player player = playerAt(50, 50);
        Enemy enemy = enemyAt(10, 10);
        List<Enemy> source = new ArrayList<>(List.of(enemy));
        GameWorld world = new GameWorld(100, 100, player, source);

        source.clear();
        assertEquals(1, world.getEnemies().size());
        assertThrows(UnsupportedOperationException.class,
                () -> world.getEnemies().add(enemyAt(20, 20)));
    }

    @Test
    void shouldRejectInvalidInitialEnemyPositions() {
        Player player = playerAt(50, 50);
        assertThrows(NullPointerException.class,
                () -> new GameWorld(100, 100, player, java.util.Arrays.asList(enemyAt(10, 10), null)));
        assertThrows(IllegalArgumentException.class,
                () -> new GameWorld(100, 100, player, List.of(enemyAt(-1, 10))));
    }

    @Test
    void shouldMovePlayerAndClampToBounds() {
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50));
        world.movePlayerBy(10, -20);
        assertEquals(new Position(60, 30), world.getPlayer().getPosition());

        world.movePlayerBy(1000, -1000);
        assertEquals(new Position(100, 0), world.getPlayer().getPosition());
    }

    @Test
    void shouldRejectNonFinitePlayerMovement() {
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50));
        assertThrows(IllegalArgumentException.class,
                () -> world.movePlayerBy(Double.NaN, 0));
        assertEquals(new Position(50, 50), world.getPlayer().getPosition());
    }

    @Test
    void shouldReplaceEnemiesAtomically() {
        Enemy original = enemyAt(10, 10);
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50), List.of(original));
        Enemy first = enemyAt(20, 20);
        Enemy second = enemyAt(30, 30);

        world.replaceEnemies(List.of(first, second));
        assertEquals(List.of(first, second), world.getEnemies());

        assertThrows(IllegalArgumentException.class,
                () -> world.replaceEnemies(List.of(enemyAt(101, 20))));
        assertEquals(List.of(first, second), world.getEnemies());
    }

    @Test
    void shouldAddValidEnemyAndRejectInvalidOne() {
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50));
        Enemy enemy = enemyAt(100, 100);
        world.addEnemy(enemy);
        assertSame(enemy, world.getEnemies().get(0));

        assertThrows(NullPointerException.class, () -> world.addEnemy(null));
        assertThrows(IllegalArgumentException.class, () -> world.addEnemy(enemyAt(101, 0)));
    }

    @Test
    void shouldMoveOwnedEnemyAndClampIt() {
        Enemy enemy = enemyAt(10, 10);
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50), List.of(enemy));

        world.moveEnemyBy(enemy, 20, -30);
        assertEquals(new Position(30, 0), enemy.getPosition());
        world.moveEnemyBy(enemy, 1000, 1000);
        assertEquals(new Position(100, 100), enemy.getPosition());
    }

    @Test
    void shouldRejectInvalidEnemyMovement() {
        Enemy enemy = enemyAt(10, 10);
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50), List.of(enemy));

        assertThrows(NullPointerException.class, () -> world.moveEnemyBy(null, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> world.moveEnemyBy(enemyAt(10, 10), 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> world.moveEnemyBy(enemy, Double.NaN, 0));
    }

    @Test
    void shouldManageProjectileLifecycleByIdentity() {
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50));
        Projectile projectile = projectileAt(10, 10);

        world.addProjectile(projectile);
        world.addProjectile(projectile);
        assertEquals(1, world.getProjectiles().size());
        assertSame(projectile, world.getProjectiles().get(0));

        world.removeProjectile(projectile);
        assertTrue(world.getProjectiles().isEmpty());
    }

    @Test
    void shouldValidateProjectileAddition() {
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50));
        assertThrows(NullPointerException.class, () -> world.addProjectile(null));
        assertThrows(IllegalArgumentException.class, () -> world.addProjectile(projectileAt(-1, 0)));
        assertThrows(UnsupportedOperationException.class,
                () -> world.getProjectiles().add(projectileAt(10, 10)));
    }

    @Test
    void shouldMoveOwnedProjectileWithoutClamping() {
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50));
        Projectile projectile = projectileAt(90, 90);
        world.addProjectile(projectile);

        world.moveProjectileBy(projectile, 20, -100);
        assertEquals(new Position(110, -10), projectile.getPosition());
    }

    @Test
    void shouldRejectInvalidProjectileOperations() {
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50));
        Projectile owned = projectileAt(10, 10);
        world.addProjectile(owned);
        Projectile foreign = projectileAt(20, 20);

        assertThrows(IllegalArgumentException.class, () -> world.removeProjectile(foreign));
        assertThrows(IllegalArgumentException.class, () -> world.moveProjectileBy(foreign, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> world.moveProjectileBy(owned, Double.POSITIVE_INFINITY, 0));
    }

    @Test
    void shouldClearProjectiles() {
        GameWorld world = new GameWorld(100, 100, playerAt(50, 50));
        world.addProjectile(projectileAt(10, 10));
        world.addProjectile(projectileAt(20, 20));

        world.clearProjectiles();
        assertTrue(world.getProjectiles().isEmpty());
    }

    private static Player playerAt(double x, double y) {
        return new Player(new Position(x, y), 100, 100.0);
    }

    private static Enemy enemyAt(double x, double y) {
        return new Enemy(new Position(x, y), 100, 80.0);
    }

    private static Projectile projectileAt(double x, double y) {
        return new Projectile(new Position(x, y), 1, 0, 10, 100.0);
    }
}
