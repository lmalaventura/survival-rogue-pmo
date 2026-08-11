package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameWorldTest {

    @Test
    void exposesDimensionsAndPlayer() {
        Player player = new Player(new Position(10.0, 20.0), 100, 200.0);
        GameWorld world = new GameWorld(800.0, 600.0, player);

        assertAll(
                () -> assertEquals(800.0, world.getWidth()),
                () -> assertEquals(600.0, world.getHeight()),
                () -> assertSame(player, world.getPlayer())
        );
    }

    @Test
    void rejectsInvalidWidth() {
        Player player = new Player(new Position(0.0, 0.0), 100, 200.0);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(0.0, 600.0, player)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(-1.0, 600.0, player)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(Double.NaN, 600.0, player)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(Double.POSITIVE_INFINITY, 600.0, player)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(Double.NEGATIVE_INFINITY, 600.0, player))
        );
    }

    @Test
    void rejectsInvalidHeight() {
        Player player = new Player(new Position(0.0, 0.0), 100, 200.0);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(800.0, 0.0, player)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(800.0, -1.0, player)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(800.0, Double.NaN, player)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(800.0, Double.POSITIVE_INFINITY, player)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(800.0, Double.NEGATIVE_INFINITY, player))
        );
    }

    @Test
    void rejectsNullPlayer() {
        assertThrows(NullPointerException.class,
                () -> new GameWorld(800.0, 600.0, null));
    }

    @Test
    void movesWithinBoundsWithPositiveAndNegativeDeltas() {
        Player player = new Player(new Position(20.0, 30.0), 100, 200.0);
        GameWorld world = new GameWorld(100.0, 80.0, player);

        world.movePlayerBy(10.0, 15.0);
        assertEquals(new Position(30.0, 45.0), player.getPosition());

        world.movePlayerBy(-5.0, -10.0);
        assertEquals(new Position(25.0, 35.0), player.getPosition());
    }

    @Test
    void clampsMovementAtWorldBoundaries() {
        Player player = new Player(new Position(50.0, 40.0), 100, 200.0);
        GameWorld world = new GameWorld(100.0, 80.0, player);

        world.movePlayerBy(-100.0, 0.0);
        assertEquals(new Position(0.0, 40.0), player.getPosition());

        world.movePlayerBy(200.0, 0.0);
        assertEquals(new Position(100.0, 40.0), player.getPosition());

        world.movePlayerBy(0.0, -100.0);
        assertEquals(new Position(100.0, 0.0), player.getPosition());

        world.movePlayerBy(0.0, 200.0);
        assertEquals(new Position(100.0, 80.0), player.getPosition());

        world.movePlayerBy(-200.0, -200.0);
        assertEquals(new Position(0.0, 0.0), player.getPosition());
    }

    @Test
    void handlesZeroExactBoundaryAndSingleAxisMovement() {
        Player player = new Player(new Position(20.0, 30.0), 100, 200.0);
        GameWorld world = new GameWorld(100.0, 80.0, player);

        world.movePlayerBy(0.0, 0.0);
        assertEquals(new Position(20.0, 30.0), player.getPosition());

        world.movePlayerBy(15.0, 0.0);
        assertEquals(new Position(35.0, 30.0), player.getPosition());

        world.movePlayerBy(0.0, 10.0);
        assertEquals(new Position(35.0, 40.0), player.getPosition());

        world.movePlayerBy(65.0, 40.0);
        assertEquals(new Position(100.0, 80.0), player.getPosition());
    }

    @Test
    void rejectsNonFiniteDeltasWithoutChangingPosition() {
        Position initialPosition = new Position(20.0, 30.0);
        Player player = new Player(initialPosition, 100, 200.0);
        GameWorld world = new GameWorld(100.0, 80.0, player);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.movePlayerBy(Double.NaN, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.movePlayerBy(Double.POSITIVE_INFINITY, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.movePlayerBy(Double.NEGATIVE_INFINITY, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.movePlayerBy(0.0, Double.NaN)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.movePlayerBy(0.0, Double.POSITIVE_INFINITY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.movePlayerBy(0.0, Double.NEGATIVE_INFINITY))
        );

        assertEquals(initialPosition, player.getPosition());
    }

    @Test
    void rejectsInitialPlayerPositionOutsideWorld() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(100.0, 80.0,
                                new Player(new Position(-1.0, 40.0), 100, 200.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(100.0, 80.0,
                                new Player(new Position(101.0, 40.0), 100, 200.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(100.0, 80.0,
                                new Player(new Position(50.0, -1.0), 100, 200.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GameWorld(100.0, 80.0,
                                new Player(new Position(50.0, 81.0), 100, 200.0)))
        );
    }

    @Test
    void acceptsInitialPlayerPositionOnWorldBoundaries() {
        assertAll(
                () -> assertDoesNotThrow(() -> new GameWorld(100.0, 80.0,
                        new Player(new Position(0.0, 0.0), 100, 200.0))),
                () -> assertDoesNotThrow(() -> new GameWorld(100.0, 80.0,
                        new Player(new Position(100.0, 80.0), 100, 200.0)))
        );
    }

    @Test
    void threeArgumentConstructorCreatesWorldWithoutEnemies() {
        GameWorld world = new GameWorld(
                100.0,
                80.0,
                new Player(new Position(50.0, 40.0), 100, 200.0)
        );

        assertEquals(List.of(), world.getEnemies());
    }

    @Test
    void exposesMultipleEnemiesPreservingOrderAndIdentity() {
        Enemy firstEnemy = enemyAt(10.0, 20.0);
        Enemy secondEnemy = enemyAt(30.0, 40.0);
        GameWorld world = worldWithEnemies(List.of(firstEnemy, secondEnemy));

        assertAll(
                () -> assertEquals(2, world.getEnemies().size()),
                () -> assertSame(firstEnemy, world.getEnemies().get(0)),
                () -> assertSame(secondEnemy, world.getEnemies().get(1))
        );
    }

    @Test
    void acceptsAnEmptyEnemyList() {
        GameWorld world = assertDoesNotThrow(() -> worldWithEnemies(List.of()));

        assertEquals(List.of(), world.getEnemies());
    }

    @Test
    void rejectsNullEnemyListAndNullEnemyElements() {
        Player player = new Player(new Position(50.0, 40.0), 100, 200.0);
        List<Enemy> enemiesWithNull = new ArrayList<>();
        enemiesWithNull.add(enemyAt(10.0, 20.0));
        enemiesWithNull.add(null);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new GameWorld(100.0, 80.0, player, null)),
                () -> assertThrows(NullPointerException.class,
                        () -> new GameWorld(100.0, 80.0, player, enemiesWithNull))
        );
    }

    @Test
    void exposesAStructurallyUnmodifiableEnemyList() {
        Enemy firstEnemy = enemyAt(10.0, 20.0);
        Enemy secondEnemy = enemyAt(30.0, 40.0);
        GameWorld world = worldWithEnemies(List.of(firstEnemy, secondEnemy));

        assertAll(
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getEnemies().add(enemyAt(50.0, 60.0))),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getEnemies().remove(0)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getEnemies().set(0, secondEnemy))
        );
    }

    @Test
    void copiesTheSourceEnemyList() {
        Enemy initialEnemy = enemyAt(10.0, 20.0);
        List<Enemy> sourceEnemies = new ArrayList<>();
        sourceEnemies.add(initialEnemy);
        GameWorld world = worldWithEnemies(sourceEnemies);

        sourceEnemies.clear();
        sourceEnemies.add(enemyAt(30.0, 40.0));

        assertAll(
                () -> assertEquals(1, world.getEnemies().size()),
                () -> assertSame(initialEnemy, world.getEnemies().get(0))
        );
    }

    @Test
    void acceptsEnemiesInsideTheWorldAndOnItsBoundaries() {
        List<Enemy> validEnemies = List.of(
                enemyAt(50.0, 40.0),
                enemyAt(0.0, 40.0),
                enemyAt(100.0, 40.0),
                enemyAt(50.0, 0.0),
                enemyAt(50.0, 80.0)
        );

        GameWorld world = assertDoesNotThrow(() -> worldWithEnemies(validEnemies));

        assertEquals(validEnemies, world.getEnemies());
    }

    @Test
    void rejectsEnemiesOutsideEachWorldBoundary() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> worldWithEnemies(List.of(enemyAt(-1.0, 40.0)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> worldWithEnemies(List.of(enemyAt(101.0, 40.0)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> worldWithEnemies(List.of(enemyAt(50.0, -1.0)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> worldWithEnemies(List.of(enemyAt(50.0, 81.0))))
        );
    }

    @Test
    void movesEnemyWithPositiveAndNegativeDeltas() {
        Enemy enemy = enemyAt(20.0, 30.0);
        GameWorld world = worldWithEnemies(List.of(enemy));

        world.moveEnemyBy(enemy, 10.0, 15.0);
        assertEquals(new Position(30.0, 45.0), enemy.getPosition());

        world.moveEnemyBy(enemy, -5.0, -10.0);
        assertEquals(new Position(25.0, 35.0), enemy.getPosition());
    }

    @Test
    void supportsZeroAndSingleAxisEnemyMovement() {
        Enemy enemy = enemyAt(20.0, 30.0);
        GameWorld world = worldWithEnemies(List.of(enemy));

        world.moveEnemyBy(enemy, 0.0, 0.0);
        assertEquals(new Position(20.0, 30.0), enemy.getPosition());

        world.moveEnemyBy(enemy, 15.0, 0.0);
        assertEquals(new Position(35.0, 30.0), enemy.getPosition());

        world.moveEnemyBy(enemy, 0.0, 10.0);
        assertEquals(new Position(35.0, 40.0), enemy.getPosition());
    }

    @Test
    void clampsEnemyMovementAtAllWorldBoundaries() {
        Enemy enemy = enemyAt(50.0, 40.0);
        GameWorld world = worldWithEnemies(List.of(enemy));

        world.moveEnemyBy(enemy, -100.0, 0.0);
        assertEquals(new Position(0.0, 40.0), enemy.getPosition());

        world.moveEnemyBy(enemy, 200.0, 0.0);
        assertEquals(new Position(100.0, 40.0), enemy.getPosition());

        world.moveEnemyBy(enemy, 0.0, -100.0);
        assertEquals(new Position(100.0, 0.0), enemy.getPosition());

        world.moveEnemyBy(enemy, 0.0, 200.0);
        assertEquals(new Position(100.0, 80.0), enemy.getPosition());
    }

    @Test
    void rejectsNonFiniteEnemyDeltasWithoutChangingPosition() {
        Position initialPosition = new Position(20.0, 30.0);
        Enemy enemy = new Enemy(initialPosition, 100, 80.0);
        GameWorld world = worldWithEnemies(List.of(enemy));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveEnemyBy(enemy, Double.NaN, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveEnemyBy(enemy, Double.POSITIVE_INFINITY, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveEnemyBy(enemy, Double.NEGATIVE_INFINITY, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveEnemyBy(enemy, 0.0, Double.NaN)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveEnemyBy(enemy, 0.0, Double.POSITIVE_INFINITY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveEnemyBy(enemy, 0.0, Double.NEGATIVE_INFINITY))
        );

        assertEquals(initialPosition, enemy.getPosition());
    }

    @Test
    void rejectsNullEnemyMovementTarget() {
        Enemy enemy = enemyAt(20.0, 30.0);
        GameWorld world = worldWithEnemies(List.of(enemy));

        assertThrows(NullPointerException.class, () -> world.moveEnemyBy(null, 1.0, 1.0));
        assertEquals(new Position(20.0, 30.0), enemy.getPosition());
    }

    @Test
    void rejectsEnemyThatDoesNotBelongToWorldWithoutMutation() {
        Enemy worldEnemy = enemyAt(20.0, 30.0);
        Enemy foreignEnemy = enemyAt(20.0, 30.0);
        Position worldEnemyPosition = worldEnemy.getPosition();
        Position foreignEnemyPosition = foreignEnemy.getPosition();
        GameWorld world = worldWithEnemies(List.of(worldEnemy));

        assertThrows(IllegalArgumentException.class,
                () -> world.moveEnemyBy(foreignEnemy, 10.0, 10.0));

        assertAll(
                () -> assertEquals(worldEnemyPosition, worldEnemy.getPosition()),
                () -> assertEquals(foreignEnemyPosition, foreignEnemy.getPosition())
        );
    }

    private static GameWorld worldWithEnemies(List<Enemy> enemies) {
        Player player = new Player(new Position(50.0, 40.0), 100, 200.0);
        return new GameWorld(100.0, 80.0, player, enemies);
    }

    private static Enemy enemyAt(double x, double y) {
        return new Enemy(new Position(x, y), 100, 80.0);
    }
}
