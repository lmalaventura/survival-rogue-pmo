package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Item;
import it.university.survivor.model.ModifierType;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.Projectile;
import it.university.survivor.model.Rarity;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.StatModifier;
import it.university.survivor.model.StatType;
import it.university.survivor.model.UpgradeCatalog;
import it.university.survivor.model.UpgradeChoiceSession;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveProgression;
import it.university.survivor.weapon.NearestEnemyAttackStrategy;
import it.university.survivor.weapon.Weapon;
import it.university.survivor.weapon.WeaponStats;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {

    private static final double WORLD_SIZE = 1_000.0;
    private static final double MOVEMENT_SPEED = 100.0;
    private static final double PLAYER_ENEMY_MIN_DISTANCE = 14.0;
    private static final double ENEMY_MIN_SEPARATION = 13.0;
    private static final double TOLERANCE = 1.0e-9;

    @Test
    void doesNotMoveWithoutActiveDirections() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(new Position(500.0, 500.0), world.getPlayer().getPosition());
    }

    @Test
    void movesInEveryCardinalDirection() {
        assertAll(
                () -> assertMovement(MovementDirection.UP, 500.0, 490.0),
                () -> assertMovement(MovementDirection.DOWN, 500.0, 510.0),
                () -> assertMovement(MovementDirection.LEFT, 490.0, 500.0),
                () -> assertMovement(MovementDirection.RIGHT, 510.0, 500.0)
        );
    }

    @Test
    void continuesMovingAcrossUpdatesWhileDirectionIsActive() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.05);
        controller.update(0.05);

        assertPosition(510.0, 500.0, world.getPlayer().getPosition());
    }

    @Test
    void stopsAfterDirectionIsDeactivated() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);
        controller.update(0.05);

        controller.setDirectionActive(MovementDirection.RIGHT, false);
        controller.update(0.05);

        assertPosition(505.0, 500.0, world.getPlayer().getPosition());
    }

    @Test
    void repeatedActivationIsIdempotent() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);

        controller.setDirectionActive(MovementDirection.RIGHT, true);
        controller.setDirectionActive(MovementDirection.RIGHT, true);
        controller.update(0.1);

        assertPosition(510.0, 500.0, world.getPlayer().getPosition());
    }

    @Test
    void oppositeDirectionsCancelEachOther() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);

        controller.setDirectionActive(MovementDirection.LEFT, true);
        controller.setDirectionActive(MovementDirection.RIGHT, true);
        controller.setDirectionActive(MovementDirection.UP, true);
        controller.setDirectionActive(MovementDirection.DOWN, true);
        controller.update(0.1);

        assertEquals(new Position(500.0, 500.0), world.getPlayer().getPosition());
    }

    @Test
    void normalizesDiagonalMovement() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);
        controller.setDirectionActive(MovementDirection.DOWN, true);

        controller.update(0.1);

        double component = 10.0 / Math.sqrt(2.0);
        assertPosition(500.0 + component, 500.0 + component,
                world.getPlayer().getPosition());
    }

    @Test
    void diagonalDistanceMatchesCardinalDistance() {
        GameWorld cardinalWorld = createWorld(500.0, 500.0);
        GameController cardinalController = new GameController(cardinalWorld);
        cardinalController.setDirectionActive(MovementDirection.RIGHT, true);

        GameWorld diagonalWorld = createWorld(500.0, 500.0);
        GameController diagonalController = new GameController(diagonalWorld);
        diagonalController.setDirectionActive(MovementDirection.RIGHT, true);
        diagonalController.setDirectionActive(MovementDirection.DOWN, true);

        cardinalController.update(0.1);
        diagonalController.update(0.1);

        Position cardinal = cardinalWorld.getPlayer().getPosition();
        Position diagonal = diagonalWorld.getPlayer().getPosition();
        double cardinalDistance = Math.hypot(cardinal.x() - 500.0, cardinal.y() - 500.0);
        double diagonalDistance = Math.hypot(diagonal.x() - 500.0, diagonal.y() - 500.0);

        assertEquals(cardinalDistance, diagonalDistance, TOLERANCE);
    }

    @Test
    void acceptsZeroDeltaWithoutMoving() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.0);

        assertEquals(new Position(500.0, 500.0), world.getPlayer().getPosition());
    }

    @Test
    void rejectsNegativeDeltaWithoutMoving() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        assertThrows(IllegalArgumentException.class, () -> controller.update(-0.01));
        assertEquals(new Position(500.0, 500.0), world.getPlayer().getPosition());
    }

    @Test
    void rejectsNonFiniteDeltasWithoutMoving() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> controller.update(Double.NaN)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> controller.update(Double.POSITIVE_INFINITY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> controller.update(Double.NEGATIVE_INFINITY))
        );
        assertEquals(new Position(500.0, 500.0), world.getPlayer().getPosition());
    }

    @Test
    void capsLargeDeltaAtPointOneSeconds() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(5.0);

        assertPosition(510.0, 500.0, world.getPlayer().getPosition());
    }

    @Test
    void delegatesBoundaryClampingToGameWorld() {
        Player player = new Player(new Position(95.0, 50.0), 100, MOVEMENT_SPEED);
        GameWorld world = new GameWorld(100.0, 100.0, player);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        assertEquals(new Position(100.0, 50.0), player.getPosition());
    }

    @Test
    void playerStopsBeforeLivingEnemy() {
        Enemy enemy = new Enemy(new Position(520.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        Position playerPosition = world.getPlayer().getPosition();
        assertAll(
                () -> assertTrue(playerPosition.x() > 500.0),
                () -> assertTrue(playerPosition.x() < 510.0),
                () -> assertTrue(playerPosition.x() < enemy.getPosition().x()),
                () -> assertTrue(
                        distanceBetween(playerPosition, enemy.getPosition())
                                >= PLAYER_ENEMY_MIN_DISTANCE - TOLERANCE
                )
        );
    }

    @Test
    void playerCannotTunnelThroughEnemyWithCappedDelta() {
        Player player = new Player(new Position(500.0, 500.0), 100, 1_000.0);
        Enemy enemy = new Enemy(new Position(540.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(enemy)
        );
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(5.0);

        Position playerPosition = player.getPosition();
        assertAll(
                () -> assertTrue(playerPosition.x() > 500.0),
                () -> assertTrue(playerPosition.x() < enemy.getPosition().x()),
                () -> assertTrue(
                        distanceBetween(playerPosition, enemy.getPosition())
                                >= PLAYER_ENEMY_MIN_DISTANCE - TOLERANCE
                ),
                () -> assertTrue(Double.isFinite(playerPosition.x())),
                () -> assertTrue(Double.isFinite(playerPosition.y()))
        );
    }

    @Test
    void multipleEnemiesFormBarrierForPlayer() {
        Player player = new Player(new Position(500.0, 500.0), 100, 1_000.0);
        Enemy upperEnemy = new Enemy(new Position(540.0, 490.0), 100, 1.0);
        Enemy lowerEnemy = new Enemy(new Position(540.0, 510.0), 100, 1.0);
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(upperEnemy, lowerEnemy)
        );
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        Position playerPosition = player.getPosition();
        assertAll(
                () -> assertTrue(playerPosition.x() > 500.0),
                () -> assertTrue(playerPosition.x() < upperEnemy.getPosition().x()),
                () -> assertTrue(playerPosition.x() < lowerEnemy.getPosition().x()),
                () -> assertTrue(
                        distanceBetween(playerPosition, upperEnemy.getPosition())
                                >= PLAYER_ENEMY_MIN_DISTANCE - TOLERANCE
                ),
                () -> assertTrue(
                        distanceBetween(playerPosition, lowerEnemy.getPosition())
                                >= PLAYER_ENEMY_MIN_DISTANCE - TOLERANCE
                ),
                () -> assertTrue(Double.isFinite(playerPosition.x())),
                () -> assertTrue(Double.isFinite(playerPosition.y()))
        );
    }

    @Test
    void playerCanMoveOutOfExactOverlapWithoutNonFinitePosition() {
        Player player = new Player(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, 1.0);
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(enemy)
        );
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);
        controller.update(0.1);

        Position playerPosition = player.getPosition();
        assertAll(
                () -> assertPosition(520.0, 500.0, playerPosition),
                () -> assertTrue(
                        distanceBetween(playerPosition, enemy.getPosition())
                                > PLAYER_ENEMY_MIN_DISTANCE
                ),
                () -> assertTrue(Double.isFinite(playerPosition.x())),
                () -> assertTrue(Double.isFinite(playerPosition.y())),
                () -> assertTrue(Double.isFinite(enemy.getPosition().x())),
                () -> assertTrue(Double.isFinite(enemy.getPosition().y()))
        );
    }

    @Test
    void playerCannotMoveDeeperIntoExistingOverlap() {
        Player player = new Player(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy enemy = new Enemy(new Position(510.0, 500.0), 100, 1.0);
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(enemy)
        );
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(500.0, 500.0, player.getPosition()),
                () -> assertEquals(
                        10.0,
                        distanceBetween(player.getPosition(), enemy.getPosition()),
                        TOLERANCE
                )
        );
    }

    @Test
    void deadEnemyDoesNotBlockPlayerMovement() {
        Player player = new Player(new Position(500.0, 500.0), 100, 1_000.0);
        Enemy enemy = new Enemy(new Position(540.0, 500.0), 100, MOVEMENT_SPEED);
        enemy.takeDamage(100);
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(enemy)
        );
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(600.0, 500.0, player.getPosition()),
                () -> assertPosition(540.0, 500.0, enemy.getPosition())
        );
    }

    @Test
    void movesEnemyTowardPlayerWithoutPlayerInput() {
        Enemy enemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertPosition(410.0, 500.0, enemy.getPosition());
    }

    @Test
    void enemyFollowsPlayersUpdatedPositionInTheSameUpdate() {
        Enemy enemy = new Enemy(new Position(500.0, 400.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        double targetDeltaX = 10.0;
        double targetDeltaY = 100.0;
        double targetDistance = Math.hypot(targetDeltaX, targetDeltaY);
        assertPosition(
                500.0 + targetDeltaX / targetDistance * 10.0,
                400.0 + targetDeltaY / targetDistance * 10.0,
                enemy.getPosition()
        );
    }

    @Test
    void movesEnemyContinuouslyAcrossUpdates() {
        Enemy enemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.05);
        controller.update(0.05);

        assertPosition(410.0, 500.0, enemy.getPosition());
    }

    @Test
    void twoEnemyUpdatesMatchOneUpdateWithTheSameTotalDelta() {
        Enemy enemyUpdatedTwice = new Enemy(
                new Position(400.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        GameController controllerUpdatedTwice = new GameController(
                createWorld(500.0, 500.0, enemyUpdatedTwice)
        );
        Enemy enemyUpdatedOnce = new Enemy(
                new Position(400.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        GameController controllerUpdatedOnce = new GameController(
                createWorld(500.0, 500.0, enemyUpdatedOnce)
        );

        controllerUpdatedTwice.update(0.05);
        controllerUpdatedTwice.update(0.05);
        controllerUpdatedOnce.update(0.1);

        assertPosition(
                enemyUpdatedOnce.getPosition().x(),
                enemyUpdatedOnce.getPosition().y(),
                enemyUpdatedTwice.getPosition()
        );
    }

    @Test
    void updatesMultipleEnemiesInTheSameFrame() {
        Enemy leftEnemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy rightEnemy = new Enemy(new Position(600.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, leftEnemy, rightEnemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(410.0, 500.0, leftEnemy.getPosition()),
                () -> assertPosition(590.0, 500.0, rightEnemy.getPosition())
        );
    }

    @Test
    void keepsConvergingEnemiesSeparatedAndOutsidePlayerStandoff() {
        Enemy upperEnemy = new Enemy(
                new Position(480.0, 490.0),
                100,
                MOVEMENT_SPEED
        );
        Enemy lowerEnemy = new Enemy(
                new Position(480.0, 510.0),
                100,
                MOVEMENT_SPEED
        );
        Position playerPosition = new Position(500.0, 500.0);
        GameWorld world = createWorld(
                playerPosition.x(),
                playerPosition.y(),
                upperEnemy,
                lowerEnemy
        );
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertTrue(
                        distanceBetween(
                                upperEnemy.getPosition(),
                                lowerEnemy.getPosition()
                        ) >= ENEMY_MIN_SEPARATION - TOLERANCE
                ),
                () -> assertTrue(
                        distanceBetween(
                                upperEnemy.getPosition(),
                                playerPosition
                        ) >= 14.0 - TOLERANCE
                ),
                () -> assertTrue(
                        distanceBetween(
                                lowerEnemy.getPosition(),
                                playerPosition
                        ) >= 14.0 - TOLERANCE
                )
        );
    }

    @Test
    void deadEnemyDoesNotBlockLivingEnemyMovement() {
        Enemy livingEnemy = new Enemy(
                new Position(400.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        Enemy deadEnemy = new Enemy(
                new Position(410.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        deadEnemy.takeDamage(100);
        GameWorld world = createWorld(500.0, 500.0, livingEnemy, deadEnemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(410.0, 500.0, livingEnemy.getPosition()),
                () -> assertEquals(
                        new Position(410.0, 500.0),
                        deadEnemy.getPosition()
                )
        );
    }

    @Test
    void keepsSeveralConvergingEnemiesDistinct() {
        List<Enemy> enemies = List.of(
                new Enemy(new Position(460.0, 476.0), 100, MOVEMENT_SPEED),
                new Enemy(new Position(460.0, 492.0), 100, MOVEMENT_SPEED),
                new Enemy(new Position(460.0, 508.0), 100, MOVEMENT_SPEED),
                new Enemy(new Position(460.0, 524.0), 100, MOVEMENT_SPEED)
        );
        List<Position> initialPositions = enemies.stream()
                .map(Enemy::getPosition)
                .toList();
        GameWorld world = createWorld(
                500.0,
                500.0,
                enemies.toArray(Enemy[]::new)
        );
        GameController controller = new GameController(world);

        for (int update = 0; update < 5; update++) {
            controller.update(0.1);
            assertEnemiesAreSeparated(enemies);
        }

        assertAll(
                () -> assertTrue(enemies.stream().allMatch(enemy ->
                        Double.isFinite(enemy.getPosition().x())
                                && Double.isFinite(enemy.getPosition().y())
                )),
                () -> assertTrue(
                        enemies.stream().map(Enemy::getPosition).toList()
                                .stream()
                                .anyMatch(position ->
                                        !initialPositions.contains(position)
                                )
                )
        );
    }

    @Test
    void initiallyOverlappingEnemiesSeparateProgressivelyAndDeterministically() {
        List<Position> firstResult = moveInitiallyOverlappingEnemies();
        List<Position> secondResult = moveInitiallyOverlappingEnemies();

        assertAll(
                () -> assertEquals(firstResult, secondResult),
                () -> assertTrue(
                        distanceBetween(firstResult.get(0), firstResult.get(1)) > 0.0
                ),
                () -> assertTrue(firstResult.stream().allMatch(position ->
                        Double.isFinite(position.x()) && Double.isFinite(position.y())
                ))
        );
    }

    @Test
    void invalidDeltaLeavesConvergingEnemiesUnchanged() {
        Enemy upperEnemy = new Enemy(
                new Position(480.0, 490.0),
                100,
                MOVEMENT_SPEED
        );
        Enemy lowerEnemy = new Enemy(
                new Position(480.0, 510.0),
                100,
                MOVEMENT_SPEED
        );
        GameWorld world = createWorld(500.0, 500.0, upperEnemy, lowerEnemy);
        GameController controller = new GameController(world);
        List<Position> initialPositions = world.getEnemies().stream()
                .map(Enemy::getPosition)
                .toList();

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.update(-0.01)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.update(Double.NaN)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.update(Double.POSITIVE_INFINITY)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.update(Double.NEGATIVE_INFINITY)
                )
        );
        assertEquals(
                initialPositions,
                world.getEnemies().stream().map(Enemy::getPosition).toList()
        );
    }

    @Test
    void leavesEnemyAtRestWhenItCoincidesWithPlayer() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(new Position(500.0, 500.0), enemy.getPosition());
    }

    @Test
    void doesNotMoveDeadEnemy() {
        Enemy enemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        enemy.takeDamage(100);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(new Position(400.0, 500.0), enemy.getPosition());
    }

    @Test
    void capsLargeDeltaForEnemyMovement() {
        Enemy enemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(5.0);

        assertPosition(410.0, 500.0, enemy.getPosition());
    }

    @Test
    void zeroDeltaMovesNeitherPlayerNorEnemy() {
        Enemy enemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.0);

        assertAll(
                () -> assertEquals(new Position(500.0, 500.0),
                        world.getPlayer().getPosition()),
                () -> assertEquals(new Position(400.0, 500.0), enemy.getPosition())
        );
    }

    @Test
    void invalidDeltaMovesNeitherPlayerNorEnemy() {
        Enemy enemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> controller.update(-0.01)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> controller.update(Double.NaN)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> controller.update(Double.POSITIVE_INFINITY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> controller.update(Double.NEGATIVE_INFINITY))
        );
        assertAll(
                () -> assertEquals(new Position(500.0, 500.0),
                        world.getPlayer().getPosition()),
                () -> assertEquals(new Position(400.0, 500.0), enemy.getPosition())
        );
    }

    @Test
    void keepsEnemyAtContactDistanceNearWorldBoundary() {
        Player player = new Player(new Position(100.0, 50.0), 100, MOVEMENT_SPEED);
        Enemy enemy = new Enemy(new Position(80.0, 50.0), 100, MOVEMENT_SPEED);
        GameWorld world = new GameWorld(100.0, 100.0, player, List.of(enemy));
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(new Position(86.0, 50.0), enemy.getPosition());
    }

    @Test
    void damagesPlayerOnceWhenLivingEnemyIsInContact() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(90, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void doesNotDamagePlayerWhenEnemyIsOutsideContactDistance() {
        Enemy enemy = new Enemy(new Position(470.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(480.0, 500.0, enemy.getPosition()),
                () -> assertEquals(100, world.getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void enemyExactlyAtContactDistanceDoesNotMoveCloser() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(new Position(486.0, 500.0), enemy.getPosition()),
                () -> assertEquals(90, world.getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void diagonalMovementStopsAtContactDistanceAndDealsDamage() {
        Enemy enemy = new Enemy(new Position(490.0, 490.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        Position enemyPosition = enemy.getPosition();
        double distanceToPlayer = Math.hypot(
                500.0 - enemyPosition.x(),
                500.0 - enemyPosition.y()
        );
        assertAll(
                () -> assertEquals(14.0, distanceToPlayer, TOLERANCE),
                () -> assertEquals(90, world.getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void enemyInsideContactDistanceDoesNotMoveCloser() {
        Enemy enemy = new Enemy(new Position(490.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(new Position(490.0, 500.0), enemy.getPosition()),
                () -> assertEquals(90, world.getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void twoLivingEnemiesInContactDealTwentyDamage() {
        Enemy firstEnemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy secondEnemy = new Enemy(new Position(514.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, firstEnemy, secondEnemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(80, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void queuedEnemyOutsidePlayerContactDistanceDoesNotDealDamage() {
        Enemy contactingEnemy = new Enemy(
                new Position(514.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        Enemy queuedEnemy = new Enemy(
                new Position(527.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        GameWorld world = createWorld(
                500.0,
                500.0,
                contactingEnemy,
                queuedEnemy
        );
        GameController controller = new GameController(world);

        controller.update(0.1);

        Position playerPosition = world.getPlayer().getPosition();
        assertAll(
                () -> assertEquals(
                        90,
                        world.getPlayer().getHealth().getCurrentHealth()
                ),
                () -> assertTrue(
                        distanceBetween(contactingEnemy.getPosition(), playerPosition)
                                <= PLAYER_ENEMY_MIN_DISTANCE + TOLERANCE
                ),
                () -> assertTrue(
                        distanceBetween(queuedEnemy.getPosition(), playerPosition)
                                > PLAYER_ENEMY_MIN_DISTANCE
                )
        );
    }

    @Test
    void threeLivingEnemiesInContactDealThirtyDamage() {
        Enemy firstEnemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy secondEnemy = new Enemy(new Position(490.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy thirdEnemy = new Enemy(new Position(510.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(
                500.0,
                500.0,
                firstEnemy,
                secondEnemy,
                thirdEnemy
        );
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(70, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void blocksContactDamageDuringInvulnerability() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);
        for (int update = 0; update < 4; update++) {
            controller.update(0.1);
        }

        assertEquals(90, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void largeDeltaConsumesOnlyTheCappedInvulnerabilityTime() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);
        controller.update(5.0);

        assertEquals(90, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void appliesContactDamageAgainWhenInvulnerabilityExpires() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);
        for (int update = 0; update < 5; update++) {
            controller.update(0.1);
        }

        assertEquals(80, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void threeEnemiesDealThirtyDamageAgainWhenInvulnerabilityExpires() {
        Enemy firstEnemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy secondEnemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy thirdEnemy = new Enemy(new Position(514.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(
                500.0,
                500.0,
                firstEnemy,
                secondEnemy,
                thirdEnemy
        );
        GameController controller = new GameController(world);

        controller.update(0.1);
        for (int update = 0; update < 5; update++) {
            controller.update(0.1);
        }

        assertEquals(40, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void deadEnemyInContactDoesNotDamagePlayer() {
        Enemy deadEnemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        deadEnemy.takeDamage(100);
        Enemy livingEnemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, deadEnemy, livingEnemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(100, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void deadEnemyDoesNotContributeToContactDamageCount() {
        Enemy firstLivingEnemy = new Enemy(
                new Position(486.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        Enemy secondLivingEnemy = new Enemy(
                new Position(514.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        Enemy deadEnemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        deadEnemy.takeDamage(100);
        GameWorld world = createWorld(
                500.0,
                500.0,
                firstLivingEnemy,
                secondLivingEnemy,
                deadEnemy
        );
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(80, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void allDeadEnemiesCauseNoContactDamage() {
        Enemy firstEnemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy secondEnemy = new Enemy(new Position(510.0, 500.0), 100, MOVEMENT_SPEED);
        firstEnemy.takeDamage(100);
        secondEnemy.takeDamage(100);
        GameWorld world = createWorld(500.0, 500.0, firstEnemy, secondEnemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(100, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void contactDamageIsClampedAtZeroHealth() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        world.getPlayer().getHealth().takeDamage(95);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(0, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void contactDoesNotAlterAnAlreadyDeadPlayer() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        world.getPlayer().getHealth().takeDamage(100);
        GameController controller = new GameController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(500.0, 500.0,
                        world.getPlayer().getPosition()),
                () -> assertEquals(0,
                        world.getPlayer().getHealth().getCurrentHealth()),
                () -> assertEquals(RunState.DEFEAT, controller.getRunState())
        );
    }

    @Test
    void invalidDeltaChangesNeitherHealthNorInvulnerabilityTimer() {
        assertAll(
                () -> assertInvalidDeltaDoesNotChangeContactState(-0.01),
                () -> assertInvalidDeltaDoesNotChangeContactState(Double.NaN),
                () -> assertInvalidDeltaDoesNotChangeContactState(
                        Double.POSITIVE_INFINITY),
                () -> assertInvalidDeltaDoesNotChangeContactState(
                        Double.NEGATIVE_INFINITY)
        );
    }

    @Test
    void limitsMovementAndAppliesDamageWhenEnemyReachesContactDistance() {
        Enemy enemy = new Enemy(new Position(480.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(486.0, 500.0, enemy.getPosition()),
                () -> assertEquals(90, world.getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void zeroDeltaDoesNotApplyContactDamage() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);

        controller.update(0.0);

        assertAll(
                () -> assertEquals(new Position(500.0, 500.0), enemy.getPosition()),
                () -> assertEquals(100, world.getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void movesProjectileHorizontally() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(100.0, 100.0, 1.0, 0.0, 10, 100.0);
        world.addProjectile(projectile);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(new Position(110.0, 100.0), projectile.getPosition());
    }

    @Test
    void movesProjectileAlongNormalizedDiagonal() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(100.0, 100.0, 3.0, 4.0, 10, 100.0);
        world.addProjectile(projectile);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertPosition(106.0, 108.0, projectile.getPosition());
    }

    @Test
    void projectileMovementIsFrameIndependent() {
        GameWorld singleUpdateWorld = createWorld(500.0, 500.0);
        Projectile singleUpdateProjectile = projectileAt(
                100.0, 100.0, 1.0, 0.0, 10, 100.0
        );
        singleUpdateWorld.addProjectile(singleUpdateProjectile);
        GameController singleUpdateController = new GameController(singleUpdateWorld);

        GameWorld splitUpdateWorld = createWorld(500.0, 500.0);
        Projectile splitUpdateProjectile = projectileAt(
                100.0, 100.0, 1.0, 0.0, 10, 100.0
        );
        splitUpdateWorld.addProjectile(splitUpdateProjectile);
        GameController splitUpdateController = new GameController(splitUpdateWorld);

        singleUpdateController.update(0.1);
        splitUpdateController.update(0.05);
        splitUpdateController.update(0.05);

        assertPosition(
                singleUpdateProjectile.getPosition().x(),
                singleUpdateProjectile.getPosition().y(),
                splitUpdateProjectile.getPosition()
        );
    }

    @Test
    void capsLargeDeltaForProjectileMovement() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(100.0, 100.0, 1.0, 0.0, 10, 100.0);
        world.addProjectile(projectile);
        GameController controller = new GameController(world);

        controller.update(5.0);

        assertEquals(new Position(110.0, 100.0), projectile.getPosition());
    }

    @Test
    void removesProjectileAfterItLeavesArena() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(995.0, 500.0, 1.0, 0.0, 10, 100.0);
        world.addProjectile(projectile);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertEquals(List.of(), world.getProjectiles());
    }

    @Test
    void keepsProjectileExactlyOnArenaBoundary() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(990.0, 500.0, 1.0, 0.0, 10, 100.0);
        world.addProjectile(projectile);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(new Position(1_000.0, 500.0),
                        projectile.getPosition()),
                () -> assertEquals(List.of(projectile), world.getProjectiles())
        );
    }

    @Test
    void projectileDamagesLivingEnemyAndIsRemoved() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile projectile = projectileAt(476.0, 500.0, 1.0, 0.0, 25, 100.0);
        world.addProjectile(projectile);
        ExperienceProgression experienceProgression = new ExperienceProgression();
        RunStatistics runStatistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                experienceProgression,
                runStatistics
        );

        controller.update(0.1);

        assertAll(
                () -> assertEquals(75, enemy.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(0, experienceProgression.getCurrentExperience()),
                () -> assertEquals(0, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(0, runStatistics.getExperienceGained())
        );
    }

    @Test
    void projectileCanKillEnemy() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 20, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile projectile = projectileAt(476.0, 500.0, 1.0, 0.0, 25, 100.0);
        world.addProjectile(projectile);
        ExperienceProgression experienceProgression = new ExperienceProgression();
        RunStatistics runStatistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                experienceProgression,
                runStatistics
        );

        controller.update(0.1);

        assertAll(
                () -> assertEquals(0, enemy.getHealth().getCurrentHealth()),
                () -> assertTrue(enemy.isDead()),
                () -> assertEquals(100,
                        world.getPlayer().getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertSame(experienceProgression,
                        controller.getExperienceProgression()),
                () -> assertSame(runStatistics, controller.getRunStatistics()),
                () -> assertEquals(25,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(1, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(25, runStatistics.getExperienceGained())
        );
    }

    @Test
    void deadEnemyCannotBeRewardedTwice() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 20, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile killingProjectile = projectileAt(
                476.0, 500.0, 1.0, 0.0, 25, 100.0
        );
        Projectile laterProjectile = projectileAt(
                476.0, 500.0, 1.0, 0.0, 25, 100.0
        );
        world.addProjectile(killingProjectile);
        world.addProjectile(laterProjectile);
        ExperienceProgression experienceProgression = new ExperienceProgression();
        RunStatistics runStatistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                experienceProgression,
                runStatistics
        );

        controller.update(0.1);

        assertAll(
                () -> assertTrue(enemy.isDead()),
                () -> assertEquals(List.of(laterProjectile), world.getProjectiles()),
                () -> assertEquals(25,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(1, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(25, runStatistics.getExperienceGained())
        );
    }

    @Test
    void twoEnemyKillsAccumulateRewards() {
        Enemy firstEnemy = new Enemy(new Position(486.0, 500.0), 20, MOVEMENT_SPEED);
        Enemy secondEnemy = new Enemy(new Position(500.0, 486.0), 20, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, firstEnemy, secondEnemy);
        world.addProjectile(projectileAt(
                476.0, 500.0, 1.0, 0.0, 25, 100.0
        ));
        world.addProjectile(projectileAt(
                500.0, 476.0, 0.0, 1.0, 25, 100.0
        ));
        ExperienceProgression experienceProgression = new ExperienceProgression();
        RunStatistics runStatistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                experienceProgression,
                runStatistics
        );

        controller.update(0.1);

        assertAll(
                () -> assertTrue(firstEnemy.isDead()),
                () -> assertTrue(secondEnemy.isDead()),
                () -> assertEquals(50,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(2, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(50, runStatistics.getExperienceGained())
        );
    }

    @Test
    void multipleNonLethalHitsAwardOnlyOnDeath() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        ExperienceProgression experienceProgression = new ExperienceProgression();
        RunStatistics runStatistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                experienceProgression,
                runStatistics
        );

        for (int hit = 0; hit < 3; hit++) {
            world.addProjectile(projectileAt(
                    476.0, 500.0, 1.0, 0.0, 25, 100.0
            ));
            controller.update(0.1);

            assertAll(
                    () -> assertEquals(0,
                            experienceProgression.getCurrentExperience()),
                    () -> assertEquals(0, runStatistics.getEnemiesDefeated()),
                    () -> assertEquals(0, runStatistics.getExperienceGained())
            );
        }

        world.addProjectile(projectileAt(
                476.0, 500.0, 1.0, 0.0, 25, 100.0
        ));
        controller.update(0.1);

        assertAll(
                () -> assertTrue(enemy.isDead()),
                () -> assertEquals(25,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(1, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(25, runStatistics.getExperienceGained())
        );
    }

    @Test
    void levelUpProducedByEnemyRewardRemainsPending() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 20, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        world.addProjectile(projectileAt(
                476.0, 500.0, 1.0, 0.0, 25, 100.0
        ));
        ExperienceProgression experienceProgression = new ExperienceProgression();
        experienceProgression.addExperience(75);
        RunStatistics runStatistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                experienceProgression,
                runStatistics
        );

        controller.update(0.1);

        assertAll(
                () -> assertEquals(2, experienceProgression.getLevel()),
                () -> assertEquals(0,
                        experienceProgression.getCurrentExperience()),
                () -> assertTrue(experienceProgression.hasPendingLevelUp()),
                () -> assertEquals(1,
                        experienceProgression.getPendingLevelUps()),
                () -> assertEquals(1, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(25, runStatistics.getExperienceGained())
        );
    }

    @Test
    void projectileIgnoresDeadEnemy() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        enemy.takeDamage(100);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile projectile = projectileAt(476.0, 500.0, 1.0, 0.0, 25, 100.0);
        world.addProjectile(projectile);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(0, enemy.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(projectile), world.getProjectiles())
        );
    }

    @Test
    void projectileHitsOnlyFirstLivingEnemyInWorldOrder() {
        Enemy firstEnemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy secondEnemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, firstEnemy, secondEnemy);
        Projectile projectile = projectileAt(476.0, 500.0, 1.0, 0.0, 30, 100.0);
        world.addProjectile(projectile);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(70, firstEnemy.getHealth().getCurrentHealth()),
                () -> assertEquals(100, secondEnemy.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(), world.getProjectiles())
        );
    }

    @Test
    void multipleProjectilesCanDamageSameEnemy() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile firstProjectile = projectileAt(
                476.0, 500.0, 1.0, 0.0, 10, 100.0
        );
        Projectile secondProjectile = projectileAt(
                496.0, 500.0, -1.0, 0.0, 10, 100.0
        );
        world.addProjectile(firstProjectile);
        world.addProjectile(secondProjectile);
        GameController controller = new GameController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(80, enemy.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(), world.getProjectiles())
        );
    }

    @Test
    void zeroDeltaDoesNotChangeProjectileState() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile projectile = projectileAt(486.0, 500.0, 1.0, 0.0, 25, 100.0);
        world.addProjectile(projectile);
        GameController controller = new GameController(world);

        controller.update(0.0);

        assertAll(
                () -> assertEquals(new Position(486.0, 500.0),
                        projectile.getPosition()),
                () -> assertEquals(100, enemy.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(projectile), world.getProjectiles())
        );
    }

    @Test
    void invalidDeltaDoesNotChangeProjectileEnemyOrRewardState() {
        assertAll(
                () -> assertInvalidDeltaDoesNotChangeProjectileState(-0.01),
                () -> assertInvalidDeltaDoesNotChangeProjectileState(Double.NaN),
                () -> assertInvalidDeltaDoesNotChangeProjectileState(
                        Double.POSITIVE_INFINITY),
                () -> assertInvalidDeltaDoesNotChangeProjectileState(
                        Double.NEGATIVE_INFINITY)
        );
    }

    @Test
    void weaponDoesNotCreateProjectileWithoutEnemies() {
        GameWorld world = createWorld(500.0, 500.0);
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = createControllerWithWeapon(world, weapon);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(0.0, weapon.getCooldown(), TOLERANCE),
                () -> assertTrue(weapon.canAttack())
        );
    }

    @Test
    void readyWeaponCreatesProjectileWithRequestProperties() {
        Enemy enemy = new Enemy(new Position(800.0, 900.0), 100, 1.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = createControllerWithWeapon(world, weapon);

        controller.update(0.1);

        assertEquals(1, world.getProjectiles().size());
        Projectile projectile = world.getProjectiles().get(0);
        assertAll(
                () -> assertEquals(25, projectile.getDamage()),
                () -> assertEquals(300.0,
                        projectile.getMovementSpeed(), TOLERANCE),
                () -> assertEquals(0.6,
                        projectile.getDirectionX(), TOLERANCE),
                () -> assertEquals(0.8,
                        projectile.getDirectionY(), TOLERANCE),
                () -> assertPosition(518.0, 524.0, projectile.getPosition())
        );
    }

    @Test
    void weaponRespectsCooldownAcrossMultipleUpdates() {
        Enemy enemy = new Enemy(new Position(900.0, 500.0), 100, 1.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Weapon weapon = createWeapon(0.25, 25, 10.0);
        GameController controller = createControllerWithWeapon(world, weapon);

        controller.update(0.1);
        assertEquals(1, world.getProjectiles().size());

        controller.update(0.1);
        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertEquals(0.15, weapon.getCooldown(), TOLERANCE)
        );

        controller.update(0.1);
        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertEquals(0.05, weapon.getCooldown(), TOLERANCE)
        );

        controller.update(0.1);
        assertAll(
                () -> assertEquals(2, world.getProjectiles().size()),
                () -> assertEquals(0.25, weapon.getCooldown(), TOLERANCE)
        );
    }

    @Test
    void weaponIgnoresDeadEnemy() {
        Enemy enemy = new Enemy(new Position(600.0, 500.0), 100, 1.0);
        enemy.takeDamage(100);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = createControllerWithWeapon(world, weapon);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(0.0, weapon.getCooldown(), TOLERANCE),
                () -> assertTrue(weapon.canAttack())
        );
    }

    @Test
    void weaponCooldownUsesCappedDelta() {
        Enemy enemy = new Enemy(new Position(900.0, 500.0), 100, 1.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Weapon weapon = createWeapon(0.15, 25, 10.0);
        GameController controller = createControllerWithWeapon(world, weapon);

        controller.update(0.1);
        controller.update(5.0);

        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertEquals(0.05, weapon.getCooldown(), TOLERANCE)
        );

        controller.update(0.05);

        assertAll(
                () -> assertEquals(2, world.getProjectiles().size()),
                () -> assertEquals(0.15, weapon.getCooldown(), TOLERANCE)
        );
    }

    @Test
    void weaponProjectileKillUpdatesExperienceAndStatistics() {
        Enemy enemy = new Enemy(new Position(538.0, 500.0), 25, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        ExperienceProgression experienceProgression = new ExperienceProgression();
        RunStatistics runStatistics = new RunStatistics();
        Weapon weapon = createWeapon(0.75, 25, 200.0);
        GameController controller = new GameController(
                world,
                experienceProgression,
                runStatistics,
                weapon
        );

        controller.update(0.1);

        assertAll(
                () -> assertTrue(enemy.isDead()),
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(25,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(1, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(25, runStatistics.getExperienceGained())
        );
    }

    @Test
    void invalidDeltaDoesNotUpdateWeaponOrCreateProjectile() {
        Enemy enemy = new Enemy(new Position(900.0, 500.0), 100, 1.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = createControllerWithWeapon(world, weapon);

        assertThrows(IllegalArgumentException.class,
                () -> controller.update(Double.NaN));
        assertAll(
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(0.0, weapon.getCooldown(), TOLERANCE),
                () -> assertTrue(weapon.canAttack())
        );
    }

    @Test
    void incompleteWaveDoesNotTransitionOrRecordCompletion() {
        Enemy enemy = new Enemy(new Position(100.0, 100.0), 100, 1.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Wave wave = new Wave(1, List.of(enemy));
        GameController controller = new GameController(world, wave);

        controller.update(0.1);

        assertAll(
                () -> assertSame(wave, controller.getCurrentWave()),
                () -> assertEquals(List.of(enemy), world.getEnemies()),
                () -> assertEquals(0,
                        controller.getRunStatistics().getWavesCompleted()),
                () -> assertEquals(
                        RunState.ACTIVE_WAVE,
                        controller.getRunState()
                ),
                () -> assertNull(controller.getCurrentUpgradeSession())
        );
    }

    @Test
    void killingLastEnemyWithProjectileStartsWaveTwoInSameUpdate() {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 25, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Wave waveOne = new Wave(1, List.of(enemy));
        world.addProjectile(projectileAt(
                476.0,
                500.0,
                1.0,
                0.0,
                25,
                100.0
        ));
        GameController controller = new GameController(world, waveOne);

        controller.update(0.1);

        assertAll(
                () -> assertTrue(enemy.isDead()),
                () -> assertEquals(2,
                        controller.getCurrentWave().getWaveNumber()),
                () -> assertEquals(4,
                        controller.getCurrentWave().getEnemies().size()),
                () -> assertEquals(
                        controller.getCurrentWave().getEnemies(),
                        world.getEnemies()
                ),
                () -> assertEquals(1,
                        controller.getRunStatistics().getWavesCompleted()),
                () -> assertEquals(
                        RunState.ACTIVE_WAVE,
                        controller.getRunState()
                ),
                () -> assertNull(controller.getCurrentUpgradeSession())
        );
    }

    @Test
    void completedWaveIsRecordedExactlyOnce() {
        Enemy enemy = deadEnemyAt(100.0, 100.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(
                world,
                new Wave(1, List.of(enemy))
        );

        controller.update(0.1);
        controller.update(0.1);

        assertAll(
                () -> assertEquals(1,
                        controller.getRunStatistics().getWavesCompleted()),
                () -> assertEquals(2,
                        controller.getCurrentWave().getWaveNumber())
        );
    }

@Test
void successiveTransitionsFollowWaveProgressionThroughWaveFive() {
    Enemy enemy = deadEnemyAt(100.0, 100.0);
    GameWorld world = createWorld(500.0, 500.0, enemy);
    GameController controller = new GameController(
            world,
            new Wave(1, List.of(enemy))
    );

    for (int expectedWaveNumber = 2;
            expectedWaveNumber <= 5;
            expectedWaveNumber++) {
        controller.update(0.1);

        int waveNumber = expectedWaveNumber;
        WaveConfig expectedConfig = WaveProgression.getConfig(waveNumber);

        assertAll(
                () -> assertEquals(
                        waveNumber,
                        controller.getCurrentWave().getWaveNumber()
                ),
                () -> assertEquals(
                        expectedConfig.enemyCount(),
                        controller.getCurrentWave().getEnemies().size()
                ),
                () -> assertEquals(
                        expectedConfig.composition().stream()
                                .mapToInt(entry -> entry.count())
                                .sum(),
                        controller.getCurrentWave().getEnemies().size()
                ),
                () -> assertEquals(
                        waveNumber - 1,
                        controller.getRunStatistics().getWavesCompleted()
                ),
                () -> assertEquals(
                        RunState.ACTIVE_WAVE,
                        controller.getRunState()
                )
        );

        if (expectedWaveNumber < 5) {
            killAllEnemies(controller.getCurrentWave());
        }
    }
}

    @Test
    void waveTransitionClearsProjectilesFromPreviousWave() {
        Enemy enemy = deadEnemyAt(900.0, 900.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile projectile = projectileAt(
                100.0,
                100.0,
                1.0,
                0.0,
                10,
                1.0
        );
        world.addProjectile(projectile);
        GameController controller = new GameController(
                world,
                new Wave(1, List.of(enemy))
        );

        controller.update(0.1);

        assertAll(
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(2,
                        controller.getCurrentWave().getWaveNumber())
        );
    }

    @Test
    void completedWaveWithPendingLevelUpEntersUpgradeSelection() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0,
                1
        );

        UpgradeChoiceSession session = fixture.controller()
                .getCurrentUpgradeSession();
        assertAll(
                () -> assertEquals(
                        RunState.UPGRADE_SELECTION,
                        fixture.controller().getRunState()
                ),
                () -> assertSame(
                        fixture.completedWave(),
                        fixture.controller().getCurrentWave()
                ),
                () -> assertEquals(
                        fixture.completedWave().getEnemies(),
                        fixture.world().getEnemies()
                ),
                () -> assertEquals(1,
                        fixture.statistics().getWavesCompleted()),
                () -> assertEquals(1,
                        fixture.progression().getPendingLevelUps()),
                () -> assertNotNull(session),
                () -> assertEquals(3, session.getCurrentOptions().size())
        );
    }

    @Test
    void updateFreezesEntireSimulationDuringUpgradeSelection() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0,
                1
        );
        Enemy livingEnemy = new Enemy(
                fixture.world().getPlayer().getPosition(),
                100,
                MOVEMENT_SPEED
        );
        Projectile projectile = projectileAt(
                100.0,
                100.0,
                1.0,
                0.0,
                10,
                100.0
        );
        fixture.world().replaceEnemies(List.of(livingEnemy));
        fixture.world().addProjectile(projectile);
        fixture.controller().setDirectionActive(MovementDirection.RIGHT, true);

        Position playerPosition = fixture.world().getPlayer().getPosition();
        Position enemyPosition = livingEnemy.getPosition();
        Position projectilePosition = projectile.getPosition();
        double weaponCooldown = fixture.weapon().getCooldown();

        fixture.controller().update(0.1);
        fixture.controller().update(Double.NaN);

        assertAll(
                () -> assertEquals(
                        playerPosition,
                        fixture.world().getPlayer().getPosition()
                ),
                () -> assertEquals(enemyPosition, livingEnemy.getPosition()),
                () -> assertEquals(projectilePosition, projectile.getPosition()),
                () -> assertEquals(
                        List.of(projectile),
                        fixture.world().getProjectiles()
                ),
                () -> assertEquals(100,
                        fixture.world().getPlayer().getHealth().getCurrentHealth()),
                () -> assertEquals(
                        weaponCooldown,
                        fixture.weapon().getCooldown(),
                        TOLERANCE
                ),
                () -> assertEquals(
                        RunState.UPGRADE_SELECTION,
                        fixture.controller().getRunState()
                ),
                () -> assertEquals(1,
                        fixture.statistics().getWavesCompleted())
        );
    }

    @Test
    void successfulRerollsAreRecordedButUnavailableRerollIsNot() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0,
                1
        );
        UpgradeChoiceSession session = fixture.controller()
                .getCurrentUpgradeSession();

        fixture.controller().rerollUpgradeChoices();
        assertAll(
                () -> assertEquals(1, session.getRemainingRerolls()),
                () -> assertEquals(1,
                        fixture.statistics().getRerollsUsed())
        );

        fixture.controller().rerollUpgradeChoices();
        assertAll(
                () -> assertEquals(0, session.getRemainingRerolls()),
                () -> assertEquals(2,
                        fixture.statistics().getRerollsUsed())
        );

        assertThrows(
                IllegalStateException.class,
                fixture.controller()::rerollUpgradeChoices
        );
        assertEquals(2, fixture.statistics().getRerollsUsed());
    }

    @Test
    void selectedUpgradeIsRecordedConsumesPendingAndStartsNextWave() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0,
                1
        );
        Item selectedItem = firstUpgrade(fixture);

        fixture.controller().selectUpgrade(selectedItem);

        assertAll(
                () -> assertEquals(1,
                        fixture.statistics().getUpgradesChosen()),
                () -> assertEquals(
                        List.of(selectedItem),
                        fixture.statistics().getChosenItems()
                ),
                () -> assertFalse(
                        fixture.progression().hasPendingLevelUp()),
                () -> assertEquals(0,
                        fixture.progression().getPendingLevelUps()),
                () -> assertEquals(
                        RunState.ACTIVE_WAVE,
                        fixture.controller().getRunState()
                ),
                () -> assertNull(
                        fixture.controller().getCurrentUpgradeSession()),
                () -> assertEquals(2,
                        fixture.controller().getCurrentWave().getWaveNumber()),
                () -> assertEquals(
                        fixture.controller().getCurrentWave().getEnemies(),
                        fixture.world().getEnemies()
                )
        );
    }

    @Test
    void acceptsUpgradeAlreadySelectedByViewSession() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0,
                1
        );
        Item selectedItem = fixture.controller()
                .getCurrentUpgradeSession()
                .selectOption(0);

        fixture.controller().selectUpgrade(selectedItem);

        assertAll(
                () -> assertEquals(
                        RunState.ACTIVE_WAVE,
                        fixture.controller().getRunState()
                ),
                () -> assertEquals(1,
                        fixture.statistics().getUpgradesChosen()),
                () -> assertEquals(
                        List.of(selectedItem),
                        fixture.statistics().getChosenItems()
                ),
                () -> assertEquals(0,
                        fixture.progression().getPendingLevelUps())
        );
    }

    @Test
    void multiplePendingLevelUpsCreateConsecutiveUpgradeSessions() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                10.0,
                2
        );
        UpgradeChoiceSession firstSession = fixture.controller()
                .getCurrentUpgradeSession();

        fixture.controller().selectUpgrade(firstUpgrade(fixture));

        UpgradeChoiceSession secondSession = fixture.controller()
                .getCurrentUpgradeSession();
        assertAll(
                () -> assertEquals(
                        RunState.UPGRADE_SELECTION,
                        fixture.controller().getRunState()
                ),
                () -> assertNotNull(secondSession),
                () -> assertNotSame(firstSession, secondSession),
                () -> assertEquals(3,
                        secondSession.getCurrentOptions().size()),
                () -> assertEquals(2,
                        secondSession.getRemainingRerolls()),
                () -> assertEquals(1,
                        fixture.progression().getPendingLevelUps()),
                () -> assertEquals(1,
                        fixture.statistics().getUpgradesChosen()),
                () -> assertSame(
                        fixture.completedWave(),
                        fixture.controller().getCurrentWave()
                )
        );

        fixture.controller().selectUpgrade(firstUpgrade(fixture));

        assertAll(
                () -> assertEquals(
                        RunState.ACTIVE_WAVE,
                        fixture.controller().getRunState()
                ),
                () -> assertNull(
                        fixture.controller().getCurrentUpgradeSession()),
                () -> assertEquals(0,
                        fixture.progression().getPendingLevelUps()),
                () -> assertEquals(2,
                        fixture.statistics().getUpgradesChosen()),
                () -> assertEquals(2,
                        fixture.controller().getCurrentWave().getWaveNumber())
        );
    }

    @Test
    void appliesFlatMaximumHealthUpgradeUsingEffectiveValue() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0,
                1,
                0.80
        );
        fixture.world().getPlayer().getHealth().takeDamage(30);

        Item selectedItem = firstUpgrade(fixture);
        fixture.controller().selectUpgrade(selectedItem);

        assertAll(
                () -> assertEquals(Rarity.EPIC, selectedItem.rarity()),
                () -> assertEquals(140,
                        fixture.world().getPlayer().getHealth().getMaxHealth()),
                () -> assertEquals(110,
                        fixture.world().getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void appliesPercentageMaximumHealthUpgradeUsingEffectiveValue() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.PERCENTAGE,
                0.10,
                1,
                0.80
        );
        fixture.world().getPlayer().getHealth().takeDamage(30);

        fixture.controller().selectUpgrade(firstUpgrade(fixture));

        assertAll(
                () -> assertEquals(120,
                        fixture.world().getPlayer().getHealth().getMaxHealth()),
                () -> assertEquals(90,
                        fixture.world().getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void positiveMaximumHealthBonusRoundedToZeroStillIncreasesByOne() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                0.10,
                1
        );

        fixture.controller().selectUpgrade(firstUpgrade(fixture));

        assertAll(
                () -> assertEquals(101,
                        fixture.world().getPlayer().getHealth().getMaxHealth()),
                () -> assertEquals(101,
                        fixture.world().getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void appliesFlatDamageUpgradeToRuntimeWeapon() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.DAMAGE,
                ModifierType.FLAT,
                5.0,
                1,
                0.80
        );

        fixture.controller().selectUpgrade(firstUpgrade(fixture));

        assertEquals(35, fixture.weapon().getCurrentStats().getDamage());
    }

    @Test
    void appliesPercentageDamageUpgradeToRuntimeWeapon() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.DAMAGE,
                ModifierType.PERCENTAGE,
                0.20,
                1,
                0.80
        );

        fixture.controller().selectUpgrade(firstUpgrade(fixture));

        assertEquals(35, fixture.weapon().getCurrentStats().getDamage());
    }

    @Test
    void appliesFlatCooldownUpgradeToRuntimeWeapon() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.COOLDOWN,
                ModifierType.FLAT,
                -0.10,
                1,
                0.80
        );

        fixture.controller().selectUpgrade(firstUpgrade(fixture));

        assertEquals(
                0.55,
                fixture.weapon().getCurrentStats().getCooldownSeconds(),
                TOLERANCE
        );
    }

    @Test
    void appliesPercentageCooldownUpgradeToRuntimeWeapon() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.COOLDOWN,
                ModifierType.PERCENTAGE,
                -0.20,
                1,
                0.80
        );

        fixture.controller().selectUpgrade(firstUpgrade(fixture));

        assertEquals(
                0.45,
                fixture.weapon().getCurrentStats().getCooldownSeconds(),
                TOLERANCE
        );
    }

    @Test
    void rejectsUpgradeActionsOutsideUpgradeSelectionWithoutRecordingThem() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        Item item = new Item(
                "Test upgrade",
                Rarity.COMMON,
                new StatModifier(StatType.MAX_HEALTH, ModifierType.FLAT, 10.0)
        );

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> controller.selectUpgrade(item)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        controller::rerollUpgradeChoices
                ),
                () -> assertEquals(0,
                        controller.getRunStatistics().getUpgradesChosen()),
                () -> assertEquals(0,
                        controller.getRunStatistics().getRerollsUsed()),
                () -> assertNull(controller.getCurrentUpgradeSession())
        );
    }

    @Test
    void completedWaveFivePrioritizesVictoryOverPendingUpgrade() {
        Enemy enemy = deadEnemyAt(900.0, 900.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Wave waveFive = new Wave(5, List.of(enemy));
        ExperienceProgression progression = progressionWithPendingLevelUps(1);
        RunStatistics statistics = new RunStatistics();
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = createDeterministicUpgradeController(
                world,
                progression,
                statistics,
                weapon,
                waveFive,
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0
        );

        controller.update(0.1);
        controller.update(0.1);

        assertAll(
                () -> assertEquals(RunState.VICTORY, controller.getRunState()),
                () -> assertSame(waveFive, controller.getCurrentWave()),
                () -> assertNull(controller.getCurrentUpgradeSession()),
                () -> assertEquals(1, progression.getPendingLevelUps()),
                () -> assertEquals(1, statistics.getWavesCompleted()),
                () -> assertEquals(0, statistics.getUpgradesChosen()),
                () -> assertEquals(0, statistics.getRerollsUsed())
        );
    }

    @Test
    void completingWaveFiveProducesVictoryWithoutCreatingWaveSix() {
        Enemy enemy = deadEnemyAt(900.0, 900.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Wave waveFive = new Wave(5, List.of(enemy));
        world.addProjectile(projectileAt(
                100.0,
                100.0,
                1.0,
                0.0,
                10,
                1.0
        ));
        GameController controller = new GameController(world, waveFive);

        controller.update(0.1);
        controller.update(0.1);

        assertAll(
                () -> assertEquals(RunState.VICTORY, controller.getRunState()),
                () -> assertSame(waveFive, controller.getCurrentWave()),
                () -> assertEquals(5,
                        controller.getCurrentWave().getWaveNumber()),
                () -> assertEquals(List.of(enemy), world.getEnemies()),
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(1,
                        controller.getRunStatistics().getWavesCompleted())
        );
    }

    @Test
    void lethalContactDamageProducesDefeat() {
        Player player = new Player(
                new Position(500.0, 500.0),
                10,
                MOVEMENT_SPEED
        );
        Enemy enemy = new Enemy(
                new Position(500.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(enemy)
        );
        Wave wave = new Wave(1, List.of(enemy));
        GameController controller = new GameController(world, wave);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(0, player.getHealth().getCurrentHealth()),
                () -> assertEquals(RunState.DEFEAT, controller.getRunState()),
                () -> assertSame(wave, controller.getCurrentWave()),
                () -> assertEquals(0,
                        controller.getRunStatistics().getWavesCompleted())
        );
    }

    @Test
    void updateStopsSimulationAfterDefeat() {
        Player player = new Player(
                new Position(500.0, 500.0),
                10,
                MOVEMENT_SPEED
        );
        Enemy enemy = new Enemy(
                new Position(500.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(enemy)
        );
        Wave wave = new Wave(1, List.of(enemy));
        Projectile projectile = projectileAt(
                100.0,
                100.0,
                1.0,
                0.0,
                10,
                10.0
        );
        world.addProjectile(projectile);
        Weapon weapon = createWeapon(0.75, 1, 10.0);
        GameController controller = new GameController(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                weapon,
                wave
        );

        controller.update(0.1);
        Position projectilePositionAfterDefeat = projectile.getPosition();
        Position enemyPositionAfterDefeat = enemy.getPosition();
        double weaponCooldownAfterDefeat = weapon.getCooldown();
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(RunState.DEFEAT, controller.getRunState()),
                () -> assertPosition(500.0, 500.0, player.getPosition()),
                () -> assertEquals(
                        enemyPositionAfterDefeat,
                        enemy.getPosition()
                ),
                () -> assertEquals(
                        projectilePositionAfterDefeat,
                        projectile.getPosition()
                ),
                () -> assertEquals(List.of(projectile), world.getProjectiles()),
                () -> assertEquals(
                        weaponCooldownAfterDefeat,
                        weapon.getCooldown(),
                        TOLERANCE
                ),
                () -> assertSame(wave, controller.getCurrentWave()),
                () -> assertEquals(0,
                        controller.getRunStatistics().getWavesCompleted())
        );
    }

    @Test
    void weaponCannotCreateProjectilesAfterVictory() {
        Enemy completedEnemy = deadEnemyAt(900.0, 900.0);
        GameWorld world = createWorld(500.0, 500.0, completedEnemy);
        Wave waveFive = new Wave(5, List.of(completedEnemy));
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = new GameController(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                weapon,
                waveFive
        );
        controller.update(0.1);
        Enemy livingEnemy = new Enemy(
                new Position(900.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        world.replaceEnemies(List.of(livingEnemy));

        controller.update(0.1);

        assertAll(
                () -> assertEquals(RunState.VICTORY, controller.getRunState()),
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(0.0, weapon.getCooldown(), TOLERANCE),
                () -> assertTrue(weapon.canAttack()),
                () -> assertEquals(1,
                        controller.getRunStatistics().getWavesCompleted())
        );
    }

    @Test
    void generatedSpawnPositionsAreInternalAndDeterministic() {
        List<Position> firstPositions = transitionToWaveTwoAndGetPositions();
        List<Position> secondPositions = transitionToWaveTwoAndGetPositions();

        assertAll(
                () -> assertEquals(List.of(
                        new Position(24.0, 24.0),
                        new Position(976.0, 24.0),
                        new Position(976.0, 976.0),
                        new Position(24.0, 976.0)
                ), firstPositions),
                () -> assertEquals(firstPositions, secondPositions),
                () -> assertTrue(firstPositions.stream().allMatch(position ->
                        position.x() > 0.0
                                && position.x() < WORLD_SIZE
                                && position.y() > 0.0
                                && position.y() < WORLD_SIZE
                ))
        );
    }

    @Test
    void rejectsNullWeapon() {
        GameWorld world = createWorld(500.0, 500.0);

        assertThrows(
                NullPointerException.class,
                () -> new GameController(
                        world,
                        new ExperienceProgression(),
                        new RunStatistics(),
                        null
                )
        );
    }

    @Test
    void rejectsNullWorld() {
        assertThrows(NullPointerException.class, () -> new GameController(null));
    }

    @Test
    void rejectsNullDirection() {
        GameController controller = new GameController(createWorld(500.0, 500.0));

        assertThrows(NullPointerException.class,
                () -> controller.setDirectionActive(null, true));
    }

    private static void assertInvalidDeltaDoesNotChangeContactState(double invalidDelta) {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(world);
        controller.update(0.1);
        Position playerPosition = world.getPlayer().getPosition();
        Position enemyPosition = enemy.getPosition();

        assertThrows(IllegalArgumentException.class,
                () -> controller.update(invalidDelta));
        assertAll(
                () -> assertEquals(playerPosition, world.getPlayer().getPosition()),
                () -> assertEquals(enemyPosition, enemy.getPosition()),
                () -> assertEquals(90,
                        world.getPlayer().getHealth().getCurrentHealth())
        );

        for (int update = 0; update < 4; update++) {
            controller.update(0.1);
        }
        assertEquals(90, world.getPlayer().getHealth().getCurrentHealth());

        controller.update(0.1);
        assertEquals(80, world.getPlayer().getHealth().getCurrentHealth());
    }

    private static void assertInvalidDeltaDoesNotChangeProjectileState(double invalidDelta) {
        Enemy enemy = new Enemy(new Position(486.0, 500.0), 20, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile projectile = projectileAt(486.0, 500.0, 1.0, 0.0, 25, 100.0);
        world.addProjectile(projectile);
        ExperienceProgression experienceProgression = new ExperienceProgression();
        RunStatistics runStatistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                experienceProgression,
                runStatistics
        );
        Position initialPosition = projectile.getPosition();

        assertThrows(IllegalArgumentException.class,
                () -> controller.update(invalidDelta));
        assertAll(
                () -> assertEquals(initialPosition, projectile.getPosition()),
                () -> assertEquals(20, enemy.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(projectile), world.getProjectiles()),
                () -> assertEquals(100,
                        world.getPlayer().getHealth().getCurrentHealth()),
                () -> assertEquals(0,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(0, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(0, runStatistics.getExperienceGained())
        );
    }

    private static UpgradeFixture enterUpgradeSelection(
            StatType statType,
            ModifierType modifierType,
            double baseValue,
            int pendingLevelUps
    ) {
        return enterUpgradeSelection(
                statType,
                modifierType,
                baseValue,
                pendingLevelUps,
                0.0
        );
    }

    private static UpgradeFixture enterUpgradeSelection(
            StatType statType,
            ModifierType modifierType,
            double baseValue,
            int pendingLevelUps,
            double rarityRoll
    ) {
        Enemy completedEnemy = deadEnemyAt(100.0, 100.0);
        GameWorld world = createWorld(500.0, 500.0, completedEnemy);
        Wave completedWave = new Wave(1, List.of(completedEnemy));
        ExperienceProgression progression = progressionWithPendingLevelUps(
                pendingLevelUps
        );
        RunStatistics statistics = new RunStatistics();
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = createDeterministicUpgradeController(
                world,
                progression,
                statistics,
                weapon,
                completedWave,
                statType,
                modifierType,
                baseValue,
                rarityRoll
        );

        controller.update(0.1);

        return new UpgradeFixture(
                world,
                progression,
                statistics,
                weapon,
                completedWave,
                controller
        );
    }

    private static GameController createDeterministicUpgradeController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Weapon weapon,
            Wave wave,
            StatType statType,
            ModifierType modifierType,
            double baseValue
    ) {
        return createDeterministicUpgradeController(
                world,
                progression,
                statistics,
                weapon,
                wave,
                statType,
                modifierType,
                baseValue,
                0.0
        );
    }

    private static GameController createDeterministicUpgradeController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Weapon weapon,
            Wave wave,
            StatType statType,
            ModifierType modifierType,
            double baseValue,
            double rarityRoll
    ) {
        StatModifier modifier = new StatModifier(
                statType,
                modifierType,
                baseValue
        );
        UpgradeCatalog catalog = new UpgradeCatalog(List.of(
                new UpgradeCatalog.Template("Test upgrade A", modifier),
                new UpgradeCatalog.Template("Test upgrade B", modifier),
                new UpgradeCatalog.Template("Test upgrade C", modifier)
        ));
        Random deterministicRarityRandom = new Random(42L) {
            @Override
            public double nextDouble() {
                return rarityRoll;
            }
        };

        return new GameController(
                world,
                progression,
                statistics,
                weapon,
                wave,
                catalog,
                deterministicRarityRandom
        );
    }

    private static ExperienceProgression progressionWithPendingLevelUps(
            int pendingLevelUps
    ) {
        if (pendingLevelUps <= 0) {
            throw new IllegalArgumentException(
                    "Pending level-up count must be positive"
            );
        }

        int requiredExperience = 0;
        for (int level = 1; level <= pendingLevelUps; level++) {
            requiredExperience += 100 + 25 * (level - 1);
        }

        ExperienceProgression progression = new ExperienceProgression();
        progression.addExperience(requiredExperience);
        return progression;
    }

    private static Item firstUpgrade(UpgradeFixture fixture) {
        return fixture.controller()
                .getCurrentUpgradeSession()
                .getCurrentOptions()
                .get(0);
    }

    private record UpgradeFixture(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Weapon weapon,
            Wave completedWave,
            GameController controller
    ) {
    }

    private static Projectile projectileAt(
            double x,
            double y,
            double directionX,
            double directionY,
            int damage,
            double movementSpeed
    ) {
        return new Projectile(
                new Position(x, y),
                directionX,
                directionY,
                damage,
                movementSpeed
        );
    }

    private static Weapon createWeapon(
            double cooldownSeconds,
            int damage,
            double projectileSpeed
    ) {
        return new Weapon(
                new WeaponStats(cooldownSeconds, damage, projectileSpeed),
                new NearestEnemyAttackStrategy()
        );
    }

    private static Enemy deadEnemyAt(double x, double y) {
        Enemy enemy = new Enemy(
                new Position(x, y),
                100,
                MOVEMENT_SPEED
        );
        enemy.takeDamage(100);
        return enemy;
    }

    private static void killAllEnemies(Wave wave) {
        wave.getEnemies().forEach(enemy -> enemy.takeDamage(
                enemy.getHealth().getCurrentHealth()
        ));
    }

    private static List<Position> moveInitiallyOverlappingEnemies() {
        Enemy firstEnemy = new Enemy(
                new Position(400.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        Enemy secondEnemy = new Enemy(
                new Position(400.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        GameWorld world = createWorld(500.0, 500.0, firstEnemy, secondEnemy);
        GameController controller = new GameController(world);

        controller.update(0.1);

        return world.getEnemies().stream().map(Enemy::getPosition).toList();
    }

    private static void assertEnemiesAreSeparated(List<Enemy> enemies) {
        for (int firstIndex = 0; firstIndex < enemies.size(); firstIndex++) {
            for (int secondIndex = firstIndex + 1;
                    secondIndex < enemies.size();
                    secondIndex++) {
                Position firstPosition = enemies.get(firstIndex).getPosition();
                Position secondPosition = enemies.get(secondIndex).getPosition();
                assertTrue(
                        distanceBetween(firstPosition, secondPosition)
                                >= ENEMY_MIN_SEPARATION - TOLERANCE,
                        () -> "Enemies are too close: "
                                + firstPosition + " and " + secondPosition
                );
            }
        }
    }

    private static double distanceBetween(Position first, Position second) {
        return Math.hypot(first.x() - second.x(), first.y() - second.y());
    }

    private static List<Position> transitionToWaveTwoAndGetPositions() {
        Enemy enemy = deadEnemyAt(100.0, 100.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = new GameController(
                world,
                new Wave(1, List.of(enemy))
        );

        controller.update(0.1);

        return controller.getCurrentWave().getEnemies().stream()
                .map(Enemy::getPosition)
                .toList();
    }

    private static GameController createControllerWithWeapon(
            GameWorld world,
            Weapon weapon
    ) {
        return new GameController(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                weapon
        );
    }

    private static GameWorld createWorld(
            double playerX,
            double playerY,
            Enemy... enemies
    ) {
        Player player = new Player(
                new Position(playerX, playerY),
                100,
                MOVEMENT_SPEED
        );
        return new GameWorld(WORLD_SIZE, WORLD_SIZE, player, List.of(enemies));
    }

    private static void assertMovement(
            MovementDirection direction,
            double expectedX,
            double expectedY
    ) {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = new GameController(world);
        controller.setDirectionActive(direction, true);

        controller.update(0.1);

        assertPosition(expectedX, expectedY, world.getPlayer().getPosition());
    }

    private static void assertPosition(double expectedX, double expectedY, Position actual) {
        assertAll(
                () -> assertEquals(expectedX, actual.x(), TOLERANCE),
                () -> assertEquals(expectedY, actual.y(), TOLERANCE)
        );
    }
}
