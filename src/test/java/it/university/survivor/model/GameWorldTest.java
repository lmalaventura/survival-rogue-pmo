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
    void replacesEnemiesCompletelyPreservingOrderAndIdentity() {
        Enemy previousEnemy = enemyAt(10.0, 20.0);
        Enemy firstReplacement = enemyAt(30.0, 40.0);
        Enemy secondReplacement = enemyAt(60.0, 70.0);
        List<Enemy> replacements = new ArrayList<>(
                List.of(firstReplacement, secondReplacement)
        );
        GameWorld world = worldWithEnemies(List.of(previousEnemy));

        world.replaceEnemies(replacements);
        replacements.clear();

        assertAll(
                () -> assertEquals(2, world.getEnemies().size()),
                () -> assertSame(firstReplacement, world.getEnemies().get(0)),
                () -> assertSame(secondReplacement, world.getEnemies().get(1))
        );
    }

    @Test
    void enemyListRemainsStructurallyUnmodifiableAfterReplacement() {
        Enemy replacement = enemyAt(30.0, 40.0);
        GameWorld world = worldWithEnemies(List.of(enemyAt(10.0, 20.0)));

        world.replaceEnemies(List.of(replacement));

        assertAll(
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getEnemies().add(enemyAt(50.0, 60.0))),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getEnemies().remove(0)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getEnemies().set(0, enemyAt(50.0, 60.0))),
                () -> assertSame(replacement, world.getEnemies().get(0))
        );
    }

    @Test
    void rejectsNullReplacementEnemyListWithoutChangingCurrentEnemies() {
        Enemy currentEnemy = enemyAt(10.0, 20.0);
        GameWorld world = worldWithEnemies(List.of(currentEnemy));

        assertThrows(NullPointerException.class, () -> world.replaceEnemies(null));

        assertAll(
                () -> assertEquals(1, world.getEnemies().size()),
                () -> assertSame(currentEnemy, world.getEnemies().get(0))
        );
    }

    @Test
    void rejectsNullReplacementEnemyAtomically() {
        Enemy currentEnemy = enemyAt(10.0, 20.0);
        List<Enemy> replacements = new ArrayList<>();
        replacements.add(enemyAt(30.0, 40.0));
        replacements.add(null);
        GameWorld world = worldWithEnemies(List.of(currentEnemy));

        assertThrows(NullPointerException.class, () -> world.replaceEnemies(replacements));

        assertAll(
                () -> assertEquals(1, world.getEnemies().size()),
                () -> assertSame(currentEnemy, world.getEnemies().get(0))
        );
    }

    @Test
    void rejectsOutOfBoundsReplacementEnemyAtomically() {
        Enemy currentEnemy = enemyAt(10.0, 20.0);
        GameWorld world = worldWithEnemies(List.of(currentEnemy));

        assertThrows(
                IllegalArgumentException.class,
                () -> world.replaceEnemies(List.of(
                        enemyAt(30.0, 40.0),
                        enemyAt(101.0, 40.0)
                ))
        );

        assertAll(
                () -> assertEquals(1, world.getEnemies().size()),
                () -> assertSame(currentEnemy, world.getEnemies().get(0))
        );
    }

    @Test
    void addsEnemyAfterExistingEnemiesPreservingOrderAndIdentity() {
        Enemy existingEnemy = enemyAt(10.0, 20.0);
        Enemy addedEnemy = enemyAt(30.0, 40.0);
        GameWorld world = worldWithEnemies(List.of(existingEnemy));

        world.addEnemy(addedEnemy);

        assertAll(
                () -> assertEquals(2, world.getEnemies().size()),
                () -> assertSame(existingEnemy, world.getEnemies().get(0)),
                () -> assertSame(addedEnemy, world.getEnemies().get(1))
        );
    }

    @Test
    void acceptsEnemiesAddedOnInclusiveWorldBoundaries() {
        GameWorld world = worldWithEnemies(List.of());
        Enemy topLeft = enemyAt(0.0, 0.0);
        Enemy bottomRight = enemyAt(100.0, 80.0);

        assertAll(
                () -> assertDoesNotThrow(() -> world.addEnemy(topLeft)),
                () -> assertDoesNotThrow(() -> world.addEnemy(bottomRight))
        );
        assertAll(
                () -> assertSame(topLeft, world.getEnemies().get(0)),
                () -> assertSame(bottomRight, world.getEnemies().get(1))
        );
    }

    @Test
    void rejectsInvalidEnemyAdditionsWithoutChangingCurrentEnemies() {
        Enemy currentEnemy = enemyAt(10.0, 20.0);
        GameWorld world = worldWithEnemies(List.of(currentEnemy));

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> world.addEnemy(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.addEnemy(enemyAt(-1.0, 40.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.addEnemy(enemyAt(101.0, 40.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.addEnemy(enemyAt(50.0, -1.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.addEnemy(enemyAt(50.0, 81.0)))
        );
        assertAll(
                () -> assertEquals(1, world.getEnemies().size()),
                () -> assertSame(currentEnemy, world.getEnemies().get(0)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getEnemies().add(enemyAt(30.0, 40.0)))
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

    @Test
    void startsWithoutProjectilesForEveryExistingConstructor() {
        Player playerWithoutEnemies = new Player(
                new Position(50.0, 40.0),
                100,
                200.0
        );
        Player playerWithEnemies = new Player(
                new Position(50.0, 40.0),
                100,
                200.0
        );

        GameWorld worldWithoutEnemies = new GameWorld(100.0, 80.0, playerWithoutEnemies);
        GameWorld worldWithEnemies = new GameWorld(
                100.0,
                80.0,
                playerWithEnemies,
                List.of(enemyAt(10.0, 20.0))
        );

        assertAll(
                () -> assertEquals(List.of(), worldWithoutEnemies.getProjectiles()),
                () -> assertEquals(List.of(), worldWithEnemies.getProjectiles())
        );
    }

    @Test
    void addsProjectilePreservingItsIdentity() {
        GameWorld world = worldWithEnemies(List.of());
        Projectile projectile = projectileAt(20.0, 30.0);

        world.addProjectile(projectile);

        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertSame(projectile, world.getProjectiles().get(0))
        );
    }

    @Test
    void addingTheSameProjectileTwiceDoesNotDuplicateIt() {
        GameWorld world = worldWithEnemies(List.of());
        Projectile projectile = projectileAt(20.0, 30.0);

        world.addProjectile(projectile);
        world.addProjectile(projectile);

        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertSame(projectile, world.getProjectiles().get(0))
        );
    }

    @Test
    void rejectsNullProjectileWhenAdding() {
        GameWorld world = worldWithEnemies(List.of());

        assertThrows(NullPointerException.class, () -> world.addProjectile(null));
        assertEquals(List.of(), world.getProjectiles());
    }

    @Test
    void rejectsProjectileAddedOutsideEachWorldBoundary() {
        GameWorld world = worldWithEnemies(List.of());

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.addProjectile(projectileAt(-1.0, 40.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.addProjectile(projectileAt(101.0, 40.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.addProjectile(projectileAt(50.0, -1.0))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.addProjectile(projectileAt(50.0, 81.0)))
        );
        assertEquals(List.of(), world.getProjectiles());
    }

    @Test
    void acceptsProjectilesAddedOnWorldBoundaries() {
        GameWorld world = worldWithEnemies(List.of());
        Projectile topLeft = projectileAt(0.0, 0.0);
        Projectile bottomRight = projectileAt(100.0, 80.0);

        assertAll(
                () -> assertDoesNotThrow(() -> world.addProjectile(topLeft)),
                () -> assertDoesNotThrow(() -> world.addProjectile(bottomRight))
        );
        assertAll(
                () -> assertSame(topLeft, world.getProjectiles().get(0)),
                () -> assertSame(bottomRight, world.getProjectiles().get(1))
        );
    }

    @Test
    void exposesAStructurallyUnmodifiableProjectileList() {
        GameWorld world = worldWithEnemies(List.of());
        Projectile firstProjectile = projectileAt(20.0, 30.0);
        Projectile secondProjectile = projectileAt(40.0, 50.0);
        world.addProjectile(firstProjectile);

        assertAll(
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getProjectiles().add(secondProjectile)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getProjectiles().remove(0)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> world.getProjectiles().set(0, secondProjectile))
        );
        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertSame(firstProjectile, world.getProjectiles().get(0))
        );
    }

    @Test
    void removesProjectileThatBelongsToWorld() {
        GameWorld world = worldWithEnemies(List.of());
        Projectile projectile = projectileAt(20.0, 30.0);
        world.addProjectile(projectile);

        world.removeProjectile(projectile);

        assertEquals(List.of(), world.getProjectiles());
    }

    @Test
    void clearsAllProjectiles() {
        GameWorld world = worldWithEnemies(List.of());
        world.addProjectile(projectileAt(20.0, 30.0));
        world.addProjectile(projectileAt(40.0, 50.0));

        world.clearProjectiles();

        assertEquals(List.of(), world.getProjectiles());
    }

    @Test
    void rejectsNullProjectileWhenMovingOrRemoving() {
        GameWorld world = worldWithEnemies(List.of());

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> world.moveProjectileBy(null, 1.0, 1.0)),
                () -> assertThrows(NullPointerException.class,
                        () -> world.removeProjectile(null))
        );
    }

    @Test
    void rejectsForeignProjectileWhenMovingOrRemoving() {
        GameWorld world = worldWithEnemies(List.of());
        Projectile worldProjectile = projectileAt(20.0, 30.0);
        Projectile foreignProjectile = projectileAt(20.0, 30.0);
        world.addProjectile(worldProjectile);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveProjectileBy(foreignProjectile, 5.0, 5.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.removeProjectile(foreignProjectile))
        );
        assertAll(
                () -> assertEquals(new Position(20.0, 30.0),
                        worldProjectile.getPosition()),
                () -> assertEquals(new Position(20.0, 30.0),
                        foreignProjectile.getPosition()),
                () -> assertSame(worldProjectile, world.getProjectiles().get(0))
        );
    }

    @Test
    void movesProjectileWithoutClampingAtWorldBoundaries() {
        GameWorld world = worldWithEnemies(List.of());
        Projectile projectile = projectileAt(95.0, 75.0);
        world.addProjectile(projectile);

        world.moveProjectileBy(projectile, 10.0, 15.0);

        assertAll(
                () -> assertEquals(new Position(105.0, 90.0),
                        projectile.getPosition()),
                () -> assertSame(projectile, world.getProjectiles().get(0))
        );
    }

    @Test
    void rejectsNonFiniteProjectileDeltasWithoutChangingPosition() {
        GameWorld world = worldWithEnemies(List.of());
        Projectile projectile = projectileAt(20.0, 30.0);
        Position initialPosition = projectile.getPosition();
        world.addProjectile(projectile);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveProjectileBy(projectile, Double.NaN, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveProjectileBy(
                                projectile,
                                Double.POSITIVE_INFINITY,
                                0.0
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveProjectileBy(
                                projectile,
                                Double.NEGATIVE_INFINITY,
                                0.0
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveProjectileBy(projectile, 0.0, Double.NaN)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveProjectileBy(
                                projectile,
                                0.0,
                                Double.POSITIVE_INFINITY
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> world.moveProjectileBy(
                                projectile,
                                0.0,
                                Double.NEGATIVE_INFINITY
                        ))
        );
        assertEquals(initialPosition, projectile.getPosition());
    }

    private static GameWorld worldWithEnemies(List<Enemy> enemies) {
        Player player = new Player(new Position(50.0, 40.0), 100, 200.0);
        return new GameWorld(100.0, 80.0, player, enemies);
    }

    private static Enemy enemyAt(double x, double y) {
        return new Enemy(new Position(x, y), 100, 80.0);
    }

    private static Projectile projectileAt(double x, double y) {
        return new Projectile(new Position(x, y), 1.0, 0.0, 10, 300.0);
    }
}
