package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Item;
import it.university.survivor.model.ModifierType;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.Projectile;
import it.university.survivor.model.ProjectileOwner;
import it.university.survivor.model.Rarity;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.StatModifier;
import it.university.survivor.model.StatType;
import it.university.survivor.model.UpgradeCatalog;
import it.university.survivor.model.UpgradeChoiceSession;
import it.university.survivor.model.UpgradeOption;
import it.university.survivor.model.WeaponUpgradeChoice;
import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveProgression;
import it.university.survivor.weapon.NearestEnemyAttackStrategy;
import it.university.survivor.weapon.Weapon;
import it.university.survivor.weapon.WeaponFactory;
import it.university.survivor.weapon.WeaponType;
import it.university.survivor.weapon.WeaponStats;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    private static final double PLAYER_COLLISION_RADIUS = 8.0;
    private static final double PROJECTILE_COLLISION_RADIUS = 3.0;
    private static final double ENEMY_SEPARATION_GAP = 1.0;
    private static final double TOLERANCE = 1.0e-9;

    @Test
    void doesNotMoveWithoutActiveDirections() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = createController(world);

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
        GameController controller = createController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.05);
        controller.update(0.05);

        assertPosition(510.0, 500.0, world.getPlayer().getPosition());
    }

    @Test
    void stopsAfterDirectionIsDeactivated() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = createController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);
        controller.update(0.05);

        controller.setDirectionActive(MovementDirection.RIGHT, false);
        controller.update(0.05);

        assertPosition(505.0, 500.0, world.getPlayer().getPosition());
    }

    @Test
    void oppositeDirectionsCancelEachOther() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = createController(world);

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
        GameController controller = createController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);
        controller.setDirectionActive(MovementDirection.DOWN, true);

        controller.update(0.1);

        double component = 10.0 / Math.sqrt(2.0);
        assertPosition(500.0 + component, 500.0 + component,
                world.getPlayer().getPosition());
    }

    @Test
    void capsLargeDeltaAtPointOneSeconds() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = createController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(5.0);

        assertPosition(510.0, 500.0, world.getPlayer().getPosition());
    }

    @Test
    void elapsedTimeAdvancesByCappedDeltaDuringActiveWave() {
        GameWorld world = createWorld(500.0, 500.0);
        RunStatistics statistics = new RunStatistics();
        GameController controller = createController(
                world,
                new ExperienceProgression(),
                statistics
        );

        controller.update(0.04);
        controller.update(5.0);

        assertEquals(0.14, statistics.getElapsedTime(), TOLERANCE);
    }

    @Test
    void zeroAndInvalidDeltasDoNotChangeElapsedTime() {
        GameWorld world = createWorld(500.0, 500.0);
        RunStatistics statistics = new RunStatistics();
        GameController controller = createController(
                world,
                new ExperienceProgression(),
                statistics
        );
        controller.update(0.04);

        controller.update(0.0);
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

        assertEquals(0.04, statistics.getElapsedTime(), TOLERANCE);
    }

    @Test
    void delegatesBoundaryClampingToGameWorld() {
        Player player = new Player(new Position(95.0, 50.0), 100, MOVEMENT_SPEED);
        GameWorld world = new GameWorld(100.0, 100.0, player);
        GameController controller = createController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        assertEquals(new Position(100.0, 50.0), player.getPosition());
    }

    @Test
    void playerStopsBeforeLivingEnemy() {
        Enemy enemy = new Enemy(new Position(520.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);
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
        GameController controller = createController(world);
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
        GameController controller = createController(world);
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
        GameController controller = createController(world);
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
        GameController controller = createController(world);
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
        GameController controller = createController(world);
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
        GameController controller = createController(world);

        controller.update(0.1);

        assertPosition(410.0, 500.0, enemy.getPosition());
    }

    @Test
    void rangedEnemyApproachesPlayerWhenTooFarAway() {
        Enemy rangedEnemy = new Enemy(
                new Position(100.0, 500.0),
                80,
                MOVEMENT_SPEED,
                EnemyType.RANGED
        );
        GameWorld world = createWorld(500.0, 500.0, rangedEnemy);
        GameController controller = createController(world);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(110.0, 500.0, rangedEnemy.getPosition()),
                () -> assertTrue(world.getProjectiles().isEmpty())
        );
    }

    @Test
    void rangedEnemyMovesAwayEvenWhenInitiallyInsideContactDistance() {
        Enemy rangedEnemy = new Enemy(
                new Position(490.0, 500.0),
                80,
                MOVEMENT_SPEED,
                EnemyType.RANGED
        );
        GameWorld world = createWorld(500.0, 500.0, rangedEnemy);
        GameController controller = createController(world);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(480.0, 500.0, rangedEnemy.getPosition()),
                () -> assertEquals(
                        100,
                        world.getPlayer().getHealth().getCurrentHealth()
                ),
                () -> assertTrue(world.getProjectiles().isEmpty())
        );
    }

    @Test
    void rangedEnemyMaintainsItsPreferredDistance() {
        Enemy rangedEnemy = new Enemy(
                new Position(250.0, 500.0),
                80,
                MOVEMENT_SPEED,
                EnemyType.RANGED
        );
        GameWorld world = createWorld(500.0, 500.0, rangedEnemy);
        GameController controller = createController(world);

        controller.update(0.1);

        assertPosition(250.0, 500.0, rangedEnemy.getPosition());
    }

    @Test
    void enemyFollowsPlayersUpdatedPositionInTheSameUpdate() {
        Enemy enemy = new Enemy(new Position(500.0, 400.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);
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
        GameController controller = createController(world);

        controller.update(0.05);
        controller.update(0.05);

        assertPosition(410.0, 500.0, enemy.getPosition());
    }

    @Test
    void updatesMultipleEnemiesInTheSameFrame() {
        Enemy leftEnemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy rightEnemy = new Enemy(new Position(600.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, leftEnemy, rightEnemy);
        GameController controller = createController(world);

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
        GameController controller = createController(world);

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
    void mixedEnemyTypesUseTheSumOfTheirRadiiForSeparation() {
        Enemy basicEnemy = new Enemy(
                new Position(449.0, 500.0),
                100,
                MOVEMENT_SPEED,
                EnemyType.BASIC
        );
        Enemy boss = new Enemy(
                new Position(474.0, 500.0),
                100,
                MOVEMENT_SPEED,
                EnemyType.BOSS
        );
        GameWorld world = createWorld(500.0, 500.0, basicEnemy, boss);
        GameController controller = createController(world);

        controller.update(0.1);

        double expectedSeparation = EnemyType.BASIC.collisionRadius()
                + EnemyType.BOSS.collisionRadius()
                + ENEMY_SEPARATION_GAP;
        assertAll(
                () -> assertPosition(449.0, 500.0, basicEnemy.getPosition()),
                () -> assertPosition(474.0, 500.0, boss.getPosition()),
                () -> assertEquals(
                        expectedSeparation,
                        distanceBetween(basicEnemy.getPosition(), boss.getPosition()),
                        TOLERANCE
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
        GameController controller = createController(world);

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
    void doesNotMoveDeadEnemy() {
        Enemy enemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        enemy.takeDamage(100);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);

        controller.update(0.1);

        assertEquals(new Position(400.0, 500.0), enemy.getPosition());
    }

    @Test
    void capsLargeDeltaForEnemyMovement() {
        Enemy enemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);

        controller.update(5.0);

        assertPosition(410.0, 500.0, enemy.getPosition());
    }

    @Test
    void keepsEnemyAtContactDistanceNearWorldBoundary() {
        Player player = new Player(new Position(100.0, 50.0), 100, MOVEMENT_SPEED);
        Enemy enemy = new Enemy(new Position(80.0, 50.0), 100, MOVEMENT_SPEED);
        GameWorld world = new GameWorld(100.0, 100.0, player, List.of(enemy));
        GameController controller = createController(world);

        controller.update(0.1);

        assertEquals(new Position(86.0, 50.0), enemy.getPosition());
    }

    @Test
    void damagesPlayerOnceWhenLivingEnemyIsInContact() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);

        controller.update(0.1);

        assertEquals(90, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void diagonalMovementStopsAtContactDistanceAndDealsDamage() {
        Enemy enemy = new Enemy(new Position(490.0, 490.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);

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
        GameController controller = createController(world);

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
    void blocksContactDamageDuringInvulnerability() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);

        controller.update(0.1);
        for (int update = 0; update < 4; update++) {
            controller.update(0.1);
        }

        assertEquals(90, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void appliesContactDamageAgainWhenInvulnerabilityExpires() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);

        controller.update(0.1);
        for (int update = 0; update < 5; update++) {
            controller.update(0.1);
        }

        assertEquals(80, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void deadEnemyInContactDoesNotDamagePlayer() {
        Enemy deadEnemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        deadEnemy.takeDamage(100);
        Enemy livingEnemy = new Enemy(new Position(400.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, deadEnemy, livingEnemy);
        GameController controller = createController(world);

        controller.update(0.1);

        assertEquals(100, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void contactDamageIsClampedAtZeroHealth() {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        world.getPlayer().getHealth().takeDamage(95);
        GameController controller = createController(world);

        controller.update(0.1);

        assertEquals(0, world.getPlayer().getHealth().getCurrentHealth());
    }

    @Test
    void limitsMovementAndAppliesDamageWhenEnemyReachesContactDistance() {
        Enemy enemy = new Enemy(new Position(480.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);

        controller.update(0.1);

        assertAll(
                () -> assertPosition(486.0, 500.0, enemy.getPosition()),
                () -> assertEquals(90, world.getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void tankStopsAndDealsContactDamageAtItsLargerRadius() {
        Enemy tank = new Enemy(
                new Position(480.0, 500.0),
                100,
                MOVEMENT_SPEED,
                EnemyType.TANK
        );
        GameWorld world = createWorld(500.0, 500.0, tank);
        GameController controller = createController(world);

        controller.update(0.1);

        double expectedContactDistance = PLAYER_COLLISION_RADIUS
                + EnemyType.TANK.collisionRadius();
        assertAll(
                () -> assertPosition(
                        500.0 - expectedContactDistance,
                        500.0,
                        tank.getPosition()
                ),
                () -> assertEquals(
                        expectedContactDistance,
                        distanceBetween(
                                tank.getPosition(),
                                world.getPlayer().getPosition()
                        ),
                        TOLERANCE
                ),
                () -> assertEquals(90,
                        world.getPlayer().getHealth().getCurrentHealth())
        );
    }

    @Test
    void playerStopsBeforeBossUsingBossCollisionRadius() {
        Player player = new Player(
                new Position(500.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        Enemy boss = new Enemy(
                new Position(530.0, 500.0),
                100,
                MOVEMENT_SPEED,
                EnemyType.BOSS
        );
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(boss)
        );
        GameController controller = createController(world);
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        controller.update(0.1);

        double expectedContactDistance = PLAYER_COLLISION_RADIUS
                + EnemyType.BOSS.collisionRadius();
        assertAll(
                () -> assertPosition(504.0, 500.0, player.getPosition()),
                () -> assertEquals(
                        expectedContactDistance,
                        distanceBetween(player.getPosition(), boss.getPosition()),
                        TOLERANCE
                )
        );
    }

    @Test
    void movesProjectileHorizontally() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(100.0, 100.0, 1.0, 0.0, 10, 100.0);
        world.addProjectile(projectile);
        GameController controller = createController(world);

        controller.update(0.1);

        assertEquals(new Position(110.0, 100.0), projectile.getPosition());
    }

    @Test
    void removesProjectileAfterItLeavesArena() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(995.0, 500.0, 1.0, 0.0, 10, 100.0);
        world.addProjectile(projectile);
        GameController controller = createController(world);

        controller.update(0.1);

        assertEquals(List.of(), world.getProjectiles());
    }

    @Test
    void playerProjectileNeverDamagesPlayer() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(
                490.0,
                500.0,
                1.0,
                0.0,
                8,
                10.0,
                ProjectileOwner.PLAYER
        );
        world.addProjectile(projectile);
        GameController controller = createController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(
                        100,
                        world.getPlayer().getHealth().getCurrentHealth()
                ),
                () -> assertEquals(List.of(projectile), world.getProjectiles())
        );
    }

    @Test
    void enemyProjectileNeverDamagesEnemy() {
        Enemy enemy = new Enemy(
                new Position(500.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        GameWorld world = createWorld(100.0, 100.0, enemy);
        Projectile projectile = projectileAt(
                490.0,
                500.0,
                1.0,
                0.0,
                8,
                10.0,
                ProjectileOwner.ENEMY
        );
        world.addProjectile(projectile);
        GameController controller = createController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(100, enemy.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(projectile), world.getProjectiles())
        );
    }

    @Test
    void enemyProjectileDamagesPlayerAndIsRemoved() {
        GameWorld world = createWorld(500.0, 500.0);
        Projectile projectile = projectileAt(
                490.0,
                500.0,
                1.0,
                0.0,
                8,
                10.0,
                ProjectileOwner.ENEMY
        );
        world.addProjectile(projectile);
        GameController controller = createController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(
                        92,
                        world.getPlayer().getHealth().getCurrentHealth()
                ),
                () -> assertTrue(world.getProjectiles().isEmpty())
        );
    }

    @Test
    void enemyProjectileUsesExistingPlayerInvulnerabilityWindow() {
        Enemy contactingEnemy = new Enemy(
                new Position(486.0, 500.0),
                100,
                MOVEMENT_SPEED
        );
        GameWorld world = createWorld(500.0, 500.0, contactingEnemy);
        GameController controller = createController(world);
        controller.update(0.1);
        contactingEnemy.takeDamage(100);

        Projectile projectile = projectileAt(
                490.0,
                500.0,
                1.0,
                0.0,
                8,
                10.0,
                ProjectileOwner.ENEMY
        );
        world.addProjectile(projectile);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(
                        90,
                        world.getPlayer().getHealth().getCurrentHealth()
                ),
                () -> assertTrue(world.getProjectiles().isEmpty())
        );
    }

    @Test
    void rangedEnemyCreatesConfiguredHostileProjectileInAttackRange() {
        Enemy rangedEnemy = new Enemy(
                new Position(250.0, 500.0),
                80,
                MOVEMENT_SPEED,
                EnemyType.RANGED
        );
        GameWorld world = createWorld(500.0, 500.0, rangedEnemy);
        GameController controller = createController(world);

        controller.update(0.1);

        Projectile projectile = world.getProjectiles().get(0);
        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertEquals(ProjectileOwner.ENEMY, projectile.getOwner()),
                () -> assertEquals(8, projectile.getDamage()),
                () -> assertEquals(220.0, projectile.getMovementSpeed(), TOLERANCE),
                () -> assertEquals(1.0, projectile.getDirectionX(), TOLERANCE),
                () -> assertEquals(0.0, projectile.getDirectionY(), TOLERANCE),
                () -> assertFalse(rangedEnemy.canRequestRangedAttack())
        );
    }

    @Test
    void rangedCooldownPreventsConsecutiveProjectiles() {
        Enemy rangedEnemy = new Enemy(
                new Position(250.0, 500.0),
                80,
                MOVEMENT_SPEED,
                EnemyType.RANGED
        );
        GameWorld world = createWorld(500.0, 500.0, rangedEnemy);
        GameController controller = createController(world);

        controller.update(0.1);
        Projectile firstProjectile = world.getProjectiles().get(0);
        controller.update(0.1);

        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertSame(firstProjectile, world.getProjectiles().get(0))
        );
    }

    @Test
    void rangedEnemyCanFireAgainAfterOneSecond() {
        Enemy rangedEnemy = new Enemy(
                new Position(650.0, 500.0),
                80,
                MOVEMENT_SPEED,
                EnemyType.RANGED
        );
        GameWorld world = createWorld(900.0, 500.0, rangedEnemy);
        GameController controller = createController(world);

        controller.update(0.1);
        Projectile firstProjectile = world.getProjectiles().get(0);
        for (int update = 0; update < 9; update++) {
            controller.update(0.1);
        }

        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertSame(firstProjectile, world.getProjectiles().get(0))
        );

        controller.update(0.1);

        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertNotSame(firstProjectile, world.getProjectiles().get(0)),
                () -> assertEquals(
                        ProjectileOwner.ENEMY,
                        world.getProjectiles().get(0).getOwner()
                )
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
        GameController controller = createController(
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
    void projectileCollisionUsesBossRadius() {
        Enemy boss = new Enemy(
                new Position(526.0, 500.0),
                100,
                MOVEMENT_SPEED,
                EnemyType.BOSS
        );
        GameWorld world = createWorld(500.0, 500.0, boss);
        Projectile projectile = projectileAt(
                504.0,
                500.0,
                1.0,
                0.0,
                25,
                10.0
        );
        world.addProjectile(projectile);
        GameController controller = createController(world);

        controller.update(0.1);

        double expectedCollisionDistance = PROJECTILE_COLLISION_RADIUS
                + EnemyType.BOSS.collisionRadius();
        assertAll(
                () -> assertEquals(21.0, expectedCollisionDistance, TOLERANCE),
                () -> assertEquals(75, boss.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(), world.getProjectiles())
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
        GameController controller = createController(
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
                () -> assertEquals(10,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(1, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(10, runStatistics.getExperienceGained())
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
        GameController controller = createController(
                world,
                experienceProgression,
                runStatistics
        );

        controller.update(0.1);

        assertAll(
                () -> assertTrue(enemy.isDead()),
                () -> assertEquals(List.of(laterProjectile), world.getProjectiles()),
                () -> assertEquals(10,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(1, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(10, runStatistics.getExperienceGained())
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
        experienceProgression.addExperience(90);
        RunStatistics runStatistics = new RunStatistics();
        GameController controller = createController(
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
                () -> assertEquals(10, runStatistics.getExperienceGained())
        );
    }

    @Test
    void projectileHitsOnlyFirstLivingEnemyInWorldOrder() {
        Enemy firstEnemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        Enemy secondEnemy = new Enemy(new Position(486.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, firstEnemy, secondEnemy);
        Projectile projectile = projectileAt(476.0, 500.0, 1.0, 0.0, 30, 100.0);
        world.addProjectile(projectile);
        GameController controller = createController(world);

        controller.update(0.1);

        assertAll(
                () -> assertEquals(70, firstEnemy.getHealth().getCurrentHealth()),
                () -> assertEquals(100, secondEnemy.getHealth().getCurrentHealth()),
                () -> assertEquals(List.of(), world.getProjectiles())
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
                () -> assertEquals(0.0, weapon.getCooldownRemaining(), TOLERANCE),
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
                () -> assertEquals(0.15, weapon.getCooldownRemaining(), TOLERANCE)
        );

        controller.update(0.1);
        assertAll(
                () -> assertEquals(1, world.getProjectiles().size()),
                () -> assertEquals(0.05, weapon.getCooldownRemaining(), TOLERANCE)
        );

        controller.update(0.1);
        assertAll(
                () -> assertEquals(2, world.getProjectiles().size()),
                () -> assertEquals(0.25, weapon.getCooldownRemaining(), TOLERANCE)
        );
    }

    @Test
    void weaponProjectileKillUpdatesExperienceAndStatistics() {
        Enemy enemy = new Enemy(new Position(538.0, 500.0), 25, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        ExperienceProgression experienceProgression = new ExperienceProgression();
        RunStatistics runStatistics = new RunStatistics();
        Weapon weapon = createWeapon(0.75, 25, 200.0);
        GameController controller = createController(
                world,
                experienceProgression,
                runStatistics,
                weapon
        );

        controller.update(0.1);

        assertAll(
                () -> assertTrue(enemy.isDead()),
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(10,
                        experienceProgression.getCurrentExperience()),
                () -> assertEquals(1, runStatistics.getEnemiesDefeated()),
                () -> assertEquals(10, runStatistics.getExperienceGained())
        );
    }

    @Test
    void enemyTypesAwardTheirConfiguredExperience() {
        assertAll(
                () -> assertEnemyKillReward(EnemyType.BASIC, 10, 1, 10),
                () -> assertEnemyKillReward(EnemyType.FAST, 12, 1, 12),
                () -> assertEnemyKillReward(EnemyType.TANK, 18, 1, 18),
                () -> assertEnemyKillReward(EnemyType.RANGED, 15, 1, 15),
                () -> assertEnemyKillReward(EnemyType.MINIBOSS, 75, 1, 75),
                () -> assertEnemyKillReward(EnemyType.BOSS, 250, 3, 25)
        );
    }

    @Test
    void waveFiveMiniBossMovesNormallyBeforeChargeThenMovesFaster() {
        Enemy miniBoss = new Enemy(
                new Position(100.0, 500.0),
                100,
                10.0,
                EnemyType.MINIBOSS
        );
        GameWorld world = createWorld(900.0, 500.0, miniBoss);
        GameController controller = createController(
                world,
                new Wave(5, List.of(miniBoss))
        );

        advanceUpdates(controller, 28);
        double beforeNormalUpdate = miniBoss.getPosition().x();
        controller.update(0.1);
        double afterNormalUpdate = miniBoss.getPosition().x();
        controller.update(0.1);
        double afterChargeStarts = miniBoss.getPosition().x();

        assertAll(
                () -> assertEquals(
                        1.0,
                        afterNormalUpdate - beforeNormalUpdate,
                        TOLERANCE
                ),
                () -> assertEquals(
                        2.5,
                        afterChargeStarts - afterNormalUpdate,
                        TOLERANCE
                )
        );
    }

    @Test
    void waveFiveMiniBossChargeEndsAndCanActivateAgain() {
        Enemy miniBoss = new Enemy(
                new Position(100.0, 500.0),
                100,
                10.0,
                EnemyType.MINIBOSS
        );
        GameWorld world = createWorld(900.0, 500.0, miniBoss);
        GameController controller = createController(
                world,
                new Wave(5, List.of(miniBoss))
        );

        advanceUpdates(controller, 30);
        advanceUpdates(controller, 5);
        double beforeChargeEnds = miniBoss.getPosition().x();
        controller.update(0.1);
        double afterChargeEnds = miniBoss.getPosition().x();

        advanceUpdates(controller, 29);
        double beforeSecondCharge = miniBoss.getPosition().x();
        controller.update(0.1);
        double afterSecondCharge = miniBoss.getPosition().x();

        assertAll(
                () -> assertEquals(
                        1.0,
                        afterChargeEnds - beforeChargeEnds,
                        TOLERANCE
                ),
                () -> assertEquals(
                        2.5,
                        afterSecondCharge - beforeSecondCharge,
                        TOLERANCE
                )
        );
    }

    @Test
    void waveTenMiniBossEnragesAtFortyPercentAndStaysEnraged() {
        Enemy miniBoss = new Enemy(
                new Position(100.0, 500.0),
                100,
                10.0,
                EnemyType.MINIBOSS
        );
        GameWorld world = createWorld(900.0, 500.0, miniBoss);
        GameController controller = createController(
                world,
                new Wave(10, List.of(miniBoss))
        );

        miniBoss.takeDamage(59);
        controller.update(0.1);
        double afterNormalMovement = miniBoss.getPosition().x();

        miniBoss.takeDamage(1);
        controller.update(0.1);
        double afterEnragedMovement = miniBoss.getPosition().x();

        controller.update(0.1);
        double afterFollowingUpdate = miniBoss.getPosition().x();

        assertAll(
                () -> assertEquals(101.0, afterNormalMovement, TOLERANCE),
                () -> assertEquals(
                        1.8,
                        afterEnragedMovement - afterNormalMovement,
                        TOLERANCE
                ),
                () -> assertEquals(
                        1.8,
                        afterFollowingUpdate - afterEnragedMovement,
                        TOLERANCE
                )
        );
    }

    @Test
    void waveTenEnrageDoesNotAffectOtherEnemyTypes() {
        Enemy basicEnemy = new Enemy(
                new Position(100.0, 500.0),
                100,
                10.0,
                EnemyType.BASIC
        );
        basicEnemy.takeDamage(60);
        GameWorld world = createWorld(900.0, 500.0, basicEnemy);
        GameController controller = createController(
                world,
                new Wave(10, List.of(basicEnemy))
        );

        controller.update(0.1);

        assertPosition(101.0, 500.0, basicEnemy.getPosition());
    }

    @Test
    void waveFifteenBossSummonsTwoAlternatingMinionsAtInterval() {
        BossEncounterFixture fixture = createBossEncounterFixture(15);

        advanceUpdates(fixture.controller(), 39);
        assertEquals(1, fixture.world().getEnemies().size());

        fixture.controller().update(0.1);

        assertAll(
                () -> assertEquals(3, fixture.world().getEnemies().size()),
                () -> assertEquals(
                        EnemyType.BASIC,
                        fixture.world().getEnemies().get(1).getType()
                ),
                () -> assertEquals(
                        EnemyType.FAST,
                        fixture.world().getEnemies().get(2).getType()
                ),
                () -> assertTrue(fixture.world().getEnemies().stream()
                        .allMatch(enemy -> isWithinWorld(
                                enemy.getPosition(),
                                fixture.world()
                        )))
        );
    }

    @Test
    void bossSummonedMinionsStayCappedAndCanBeReplenished() {
        BossEncounterFixture fixture = createBossEncounterFixture(15);

        advanceUpdates(fixture.controller(), 120);
        List<Enemy> firstSixMinions = fixture.world().getEnemies().stream()
                .filter(enemy -> enemy != fixture.boss())
                .toList();

        advanceUpdates(fixture.controller(), 40);
        int countWhileCapped = fixture.world().getEnemies().size();

        firstSixMinions.stream().limit(2).forEach(enemy -> enemy.takeDamage(
                enemy.getHealth().getCurrentHealth()
        ));
        fixture.controller().update(0.1);

        long livingSummonedMinions = fixture.world().getEnemies().stream()
                .filter(enemy -> enemy != fixture.boss())
                .filter(enemy -> !enemy.isDead())
                .count();
        assertAll(
                () -> assertEquals(6, firstSixMinions.size()),
                () -> assertEquals(7, countWhileCapped),
                () -> assertEquals(9, fixture.world().getEnemies().size()),
                () -> assertEquals(6, livingSummonedMinions)
        );
    }

    @Test
    void bossDoesNotSummonWhenDeadOrOutsideWaveFifteen() {
        BossEncounterFixture waveFourteen = createBossEncounterFixture(14);
        advanceUpdates(waveFourteen.controller(), 40);

        BossEncounterFixture deadBossWave = createBossEncounterFixture(15);
        deadBossWave.boss().takeDamage(
                deadBossWave.boss().getHealth().getCurrentHealth()
        );
        advanceUpdates(deadBossWave.controller(), 40);

        assertAll(
                () -> assertEquals(1, waveFourteen.world().getEnemies().size()),
                () -> assertEquals(1, deadBossWave.world().getEnemies().size()),
                () -> assertEquals(
                        RunState.VICTORY,
                        deadBossWave.controller().getRunState()
                )
        );
    }

    @Test
    void summonedMinionsDoNotBlockVictoryOrCreateWaveSixteen() {
        BossEncounterFixture fixture = createBossEncounterFixture(15);
        advanceUpdates(fixture.controller(), 40);
        fixture.boss().takeDamage(fixture.boss().getHealth().getCurrentHealth());

        fixture.controller().update(0.1);
        Enemy livingMinion = fixture.world().getEnemies().stream()
                .filter(enemy -> enemy != fixture.boss())
                .filter(enemy -> !enemy.isDead())
                .findFirst()
                .orElseThrow();
        Position positionAtVictory = livingMinion.getPosition();
        fixture.controller().update(0.1);

        assertAll(
                () -> assertEquals(
                        RunState.VICTORY,
                        fixture.controller().getRunState()
                ),
                () -> assertSame(
                        fixture.wave(),
                        fixture.controller().getCurrentWave()
                ),
                () -> assertEquals(
                        WaveProgression.MAX_WAVES,
                        fixture.controller().getCurrentWave().getWaveNumber()
                ),
                () -> assertEquals(1, fixture.wave().getEnemies().size()),
                () -> assertEquals(3, fixture.world().getEnemies().size()),
                () -> assertEquals(positionAtVictory, livingMinion.getPosition())
        );
    }

    @Test
    void incompleteWaveDoesNotTransitionOrRecordCompletion() {
        Enemy enemy = new Enemy(new Position(100.0, 100.0), 100, 1.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Wave wave = new Wave(1, List.of(enemy));
        GameController controller = createController(world, wave);

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
        GameController controller = createController(world, waveOne);

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
        GameController controller = createController(
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
    void successiveTransitionsFollowWaveProgressionThroughWaveFifteen() {
        Enemy enemy = deadEnemyAt(100.0, 100.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(
                world,
                new Wave(1, List.of(enemy))
        );

        for (int expectedWaveNumber = 2;
                expectedWaveNumber <= WaveProgression.MAX_WAVES;
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

            if (expectedWaveNumber < WaveProgression.MAX_WAVES) {
                killAllEnemies(controller.getCurrentWave());
            }
        }
    }

    @Test
    void waveTransitionClearsProjectilesFromPreviousWave() {
        Enemy enemy = deadEnemyAt(900.0, 900.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Projectile playerProjectile = projectileAt(
                100.0,
                100.0,
                1.0,
                0.0,
                10,
                1.0
        );
        Projectile enemyProjectile = projectileAt(
                900.0,
                100.0,
                -1.0,
                0.0,
                8,
                1.0,
                ProjectileOwner.ENEMY
        );
        world.addProjectile(playerProjectile);
        world.addProjectile(enemyProjectile);
        GameController controller = createController(
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
                () -> assertEquals(3, session.getCurrentChoices().size())
        );
    }

    @Test
    void elapsedTimeDoesNotAdvanceDuringUpgradeSelection() {
        UpgradeFixture fixture = enterUpgradeSelection(
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0,
                1
        );
        double elapsedWhenSelectionOpened = fixture.statistics().getElapsedTime();

        fixture.controller().update(0.1);
        fixture.controller().update(5.0);

        assertAll(
                () -> assertEquals(
                        RunState.UPGRADE_SELECTION,
                        fixture.controller().getRunState()
                ),
                () -> assertEquals(
                        elapsedWhenSelectionOpened,
                        fixture.statistics().getElapsedTime(),
                        TOLERANCE
                )
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
        double weaponCooldown = fixture.weapon().getCooldownRemaining();

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
                        fixture.weapon().getCooldownRemaining(),
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

        fixture.controller().selectUpgradeOption(findItemOption(
                fixture.controller().getCurrentUpgradeSession(),
                selectedItem
        ));

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
        UpgradeOption selectedOption = fixture.controller()
                .getCurrentUpgradeSession()
                .selectChoice(0);
        Item selectedItem = selectedOption.item();

        fixture.controller().selectUpgradeOption(selectedOption);

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

        fixture.controller().selectUpgradeOption(firstItemOption(fixture));

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
                        secondSession.getCurrentChoices().size()),
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

        fixture.controller().selectUpgradeOption(firstItemOption(fixture));

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
        fixture.controller().selectUpgradeOption(findItemOption(
                fixture.controller().getCurrentUpgradeSession(),
                selectedItem
        ));

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

        fixture.controller().selectUpgradeOption(firstItemOption(fixture));

        assertAll(
                () -> assertEquals(120,
                        fixture.world().getPlayer().getHealth().getMaxHealth()),
                () -> assertEquals(90,
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

        fixture.controller().selectUpgradeOption(firstItemOption(fixture));

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

        fixture.controller().selectUpgradeOption(firstItemOption(fixture));

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

        fixture.controller().selectUpgradeOption(firstItemOption(fixture));

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

        fixture.controller().selectUpgradeOption(firstItemOption(fixture));

        assertEquals(
                0.45,
                fixture.weapon().getCurrentStats().getCooldownSeconds(),
                TOLERANCE
        );
    }

    @Test
    void rejectsUpgradeActionsOutsideUpgradeSelectionWithoutRecordingThem() {
        GameWorld world = createWorld(500.0, 500.0);
        GameController controller = createController(world);
        Item item = new Item(
                "Test upgrade",
                Rarity.COMMON,
                new StatModifier(StatType.MAX_HEALTH, ModifierType.FLAT, 10.0)
        );

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> controller.selectUpgradeOption(UpgradeOption.forItem(item))
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
    void pendingUpgradeAfterWaveFiveResumesAtWaveSix() {
        assertMilestoneWaveUpgradeTransition(5, 6);
    }

    @Test
    void completedWaveFifteenPrioritizesVictoryOverPendingUpgrade() {
        Enemy enemy = deadEnemyAt(900.0, 900.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Wave waveFifteen = new Wave(15, List.of(enemy));
        ExperienceProgression progression = progressionWithPendingLevelUps(1);
        RunStatistics statistics = new RunStatistics();
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = createDeterministicUpgradeController(
                world,
                progression,
                statistics,
                weapon,
                waveFifteen,
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0
        );

        controller.update(0.1);
        controller.update(0.1);

        assertAll(
                () -> assertEquals(RunState.VICTORY, controller.getRunState()),
                () -> assertSame(waveFifteen, controller.getCurrentWave()),
                () -> assertNull(controller.getCurrentUpgradeSession()),
                () -> assertEquals(1, progression.getPendingLevelUps()),
                () -> assertEquals(1, statistics.getWavesCompleted()),
                () -> assertEquals(0, statistics.getUpgradesChosen()),
                () -> assertEquals(0, statistics.getRerollsUsed())
        );
    }

    @Test
    void completingWaveFifteenProducesVictoryWithoutCreatingWaveSixteen() {
        Enemy enemy = deadEnemyAt(900.0, 900.0);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        Wave waveFifteen = new Wave(15, List.of(enemy));
        world.addProjectile(projectileAt(
                100.0,
                100.0,
                1.0,
                0.0,
                10,
                1.0
        ));
        GameController controller = createController(world, waveFifteen);

        controller.update(0.1);
        controller.update(0.1);

        assertAll(
                () -> assertEquals(RunState.VICTORY, controller.getRunState()),
                () -> assertSame(waveFifteen, controller.getCurrentWave()),
                () -> assertEquals(15,
                        controller.getCurrentWave().getWaveNumber()),
                () -> assertEquals(List.of(enemy), world.getEnemies()),
                () -> assertEquals(List.of(), world.getProjectiles()),
                () -> assertEquals(1,
                        controller.getRunStatistics().getWavesCompleted())
        );
    }

    @Test
    void elapsedTimeCountsActiveFrameEndingInVictoryAndThenStops() {
        Enemy enemy = new Enemy(
                new Position(900.0, 900.0),
                10,
                1.0
        );
        GameWorld world = createWorld(100.0, 100.0, enemy);
        Wave waveFifteen = new Wave(15, List.of(enemy));
        world.addProjectile(projectileAt(
                900.0,
                900.0,
                1.0,
                0.0,
                10,
                1.0
        ));
        RunStatistics statistics = new RunStatistics();
        GameController controller = createController(
                world,
                new ExperienceProgression(),
                statistics,
                createWeapon(0.75, 25, 300.0),
                waveFifteen
        );

        controller.update(0.05);
        double elapsedAtVictory = statistics.getElapsedTime();
        controller.update(5.0);

        assertAll(
                () -> assertEquals(RunState.VICTORY, controller.getRunState()),
                () -> assertEquals(0.05, elapsedAtVictory, TOLERANCE),
                () -> assertEquals(
                        elapsedAtVictory,
                        statistics.getElapsedTime(),
                        TOLERANCE
                )
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
        GameController controller = createController(world, wave);

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
    void elapsedTimeCountsActiveFrameEndingInDefeatAndThenStops() {
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
        RunStatistics statistics = new RunStatistics();
        GameController controller = createController(
                world,
                new ExperienceProgression(),
                statistics,
                createWeapon(0.75, 25, 300.0),
                new Wave(1, List.of(enemy))
        );

        controller.update(0.05);
        double elapsedAtDefeat = statistics.getElapsedTime();
        controller.update(5.0);

        assertAll(
                () -> assertEquals(RunState.DEFEAT, controller.getRunState()),
                () -> assertEquals(0.05, elapsedAtDefeat, TOLERANCE),
                () -> assertEquals(
                        elapsedAtDefeat,
                        statistics.getElapsedTime(),
                        TOLERANCE
                )
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
        GameController controller = createController(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                weapon,
                wave
        );

        controller.update(0.1);
        Position projectilePositionAfterDefeat = projectile.getPosition();
        Position enemyPositionAfterDefeat = enemy.getPosition();
        double weaponCooldownAfterDefeat = weapon.getCooldownRemaining();
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
                        weapon.getCooldownRemaining(),
                        TOLERANCE
                ),
                () -> assertSame(wave, controller.getCurrentWave()),
                () -> assertEquals(0,
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
                () -> createController(
                        world,
                        new ExperienceProgression(),
                        new RunStatistics(),
                        null
                )
        );
    }

    @Test
    void rejectsNullWorld() {
        assertThrows(NullPointerException.class, () -> createController(null));
    }

    @Test
    void rejectsNullDirection() {
        GameController controller = createController(createWorld(500.0, 500.0));

        assertThrows(NullPointerException.class,
                () -> controller.setDirectionActive(null, true));
    }

    private static void assertInvalidDeltaDoesNotChangeContactState(double invalidDelta) {
        Enemy enemy = new Enemy(new Position(500.0, 500.0), 100, MOVEMENT_SPEED);
        GameWorld world = createWorld(500.0, 500.0, enemy);
        GameController controller = createController(world);
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
        GameController controller = createController(
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

    @Test
    void runtimeLoadoutFiresAllOwnedWeapons() {
        Enemy target = new Enemy(
                new Position(700.0, 500.0),
                10_000,
                MOVEMENT_SPEED
        );
        GameWorld world = createWorld(500.0, 500.0, target);
        Wave wave = new Wave(1, List.of(target));
        Map<WeaponType, Weapon> loadout = new EnumMap<>(WeaponType.class);
        loadout.put(WeaponType.AUTOMATIC, WeaponFactory.createAutomatic());
        loadout.put(WeaponType.PULSE, WeaponFactory.createPulse());
        GameController controller = createController(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                loadout,
                wave
        );

        controller.update(0.01);

        assertEquals(9, world.getProjectiles().size());
    }

    @Test
    void mixedLevelUpCanUnlockWeaponAndNewWeaponInheritsPreviousDamageItems() {
        Enemy completedEnemy = deadEnemyAt(100.0, 100.0);
        GameWorld world = createWorld(500.0, 500.0, completedEnemy);
        Wave completedWave = new Wave(1, List.of(completedEnemy));
        ExperienceProgression progression = progressionWithPendingLevelUps(2);
        RunStatistics statistics = new RunStatistics();
        Map<WeaponType, Weapon> loadout = new EnumMap<>(WeaponType.class);
        Weapon automatic = WeaponFactory.createAutomatic();
        while (!automatic.isEvolved()) {
            automatic.levelUp();
        }
        loadout.put(WeaponType.AUTOMATIC, automatic);
        GameController controller = createDeterministicMixedUpgradeController(
                world,
                progression,
                statistics,
                loadout,
                completedWave,
                StatType.DAMAGE,
                ModifierType.FLAT,
                5.0
        );

        controller.update(0.1);
        UpgradeOption itemChoice = controller.getCurrentUpgradeSession()
                .getCurrentChoices()
                .stream()
                .filter(UpgradeOption::isItem)
                .findFirst()
                .orElseThrow();
        controller.selectUpgradeOption(itemChoice);

        UpgradeOption weaponOption = controller.getCurrentUpgradeSession()
                .getCurrentChoices()
                .stream()
                .filter(UpgradeOption::isWeapon)
                .findFirst()
                .orElseThrow();
        WeaponUpgradeChoice offeredWeapon = weaponOption.weaponChoice();
        int baseDamage = WeaponFactory.create(offeredWeapon.weaponType())
                .getCurrentStats()
                .getDamage();
        controller.selectUpgradeOption(weaponOption);

        Weapon resultingWeapon = controller.getWeapons().get(offeredWeapon.weaponType());
        assertAll(
                () -> assertNotNull(resultingWeapon),
                () -> assertEquals(baseDamage + 5, resultingWeapon.getCurrentStats().getDamage()),
                () -> assertEquals(1, statistics.getUpgradesChosen()),
                () -> assertEquals(1, statistics.getWeaponChoicesMade()),
                () -> assertEquals(RunState.ACTIVE_WAVE, controller.getRunState())
        );
    }

    @Test
    void choosingOwnedWeaponLevelsItAndCanTriggerEvolution() {
        Enemy completedEnemy = deadEnemyAt(100.0, 100.0);
        GameWorld world = createWorld(500.0, 500.0, completedEnemy);
        Wave completedWave = new Wave(1, List.of(completedEnemy));
        ExperienceProgression progression = progressionWithPendingLevelUps(1);
        RunStatistics statistics = new RunStatistics();
        Map<WeaponType, Weapon> loadout = new EnumMap<>(WeaponType.class);
        for (WeaponType type : WeaponType.values()) {
            Weapon weapon = WeaponFactory.create(type);
            while (!weapon.isEvolved()) {
                weapon.levelUp();
            }
            loadout.put(type, weapon);
        }
        Weapon automatic = WeaponFactory.createAutomatic();
        automatic.levelUp();
        automatic.levelUp();
        automatic.levelUp();
        loadout.put(WeaponType.AUTOMATIC, automatic);
        GameController controller = createDeterministicMixedUpgradeController(
                world,
                progression,
                statistics,
                loadout,
                completedWave,
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0
        );

        controller.update(0.1);
        UpgradeOption weaponOption = controller.getCurrentUpgradeSession()
                .getCurrentChoices()
                .stream()
                .filter(UpgradeOption::isWeapon)
                .findFirst()
                .orElseThrow();
        assertEquals(WeaponType.AUTOMATIC, weaponOption.weaponChoice().weaponType());
        assertTrue(weaponOption.weaponChoice().willEvolve());

        controller.selectUpgradeOption(weaponOption);

        assertAll(
                () -> assertEquals(5, automatic.getLevel()),
                () -> assertTrue(automatic.isEvolved()),
                () -> assertEquals(1, statistics.getWeaponChoicesMade())
        );
    }

    @Test
    void damageItemAppliesToEveryOwnedWeapon() {
        Enemy completedEnemy = deadEnemyAt(100.0, 100.0);
        GameWorld world = createWorld(500.0, 500.0, completedEnemy);
        Wave completedWave = new Wave(1, List.of(completedEnemy));
        ExperienceProgression progression = progressionWithPendingLevelUps(1);
        RunStatistics statistics = new RunStatistics();
        Map<WeaponType, Weapon> loadout = new EnumMap<>(WeaponType.class);
        Weapon automatic = WeaponFactory.createAutomatic();
        Weapon shotgun = WeaponFactory.createShotgun();
        loadout.put(WeaponType.AUTOMATIC, automatic);
        loadout.put(WeaponType.SHOTGUN, shotgun);
        GameController controller = createDeterministicMixedUpgradeController(
                world,
                progression,
                statistics,
                loadout,
                completedWave,
                StatType.DAMAGE,
                ModifierType.FLAT,
                5.0
        );

        controller.update(0.1);
        UpgradeOption itemChoice = controller.getCurrentUpgradeSession()
                .getCurrentChoices()
                .stream()
                .filter(UpgradeOption::isItem)
                .findFirst()
                .orElseThrow();
        controller.selectUpgradeOption(itemChoice);

        assertAll(
                () -> assertEquals(30, automatic.getCurrentStats().getDamage()),
                () -> assertEquals(19, shotgun.getCurrentStats().getDamage())
        );
    }

    private static void assertMilestoneWaveUpgradeTransition(
            int completedWaveNumber,
            int expectedNextWaveNumber
    ) {
        Enemy completedEnemy = deadEnemyAt(100.0, 100.0);
        GameWorld world = createWorld(500.0, 500.0, completedEnemy);
        Wave completedWave = new Wave(completedWaveNumber, List.of(completedEnemy));
        ExperienceProgression progression = progressionWithPendingLevelUps(1);
        RunStatistics statistics = new RunStatistics();
        Weapon weapon = createWeapon(0.75, 25, 300.0);
        GameController controller = createDeterministicUpgradeController(
                world,
                progression,
                statistics,
                weapon,
                completedWave,
                StatType.MAX_HEALTH,
                ModifierType.FLAT,
                20.0
        );

        controller.update(0.1);

        assertAll(
                () -> assertEquals(
                        RunState.UPGRADE_SELECTION,
                        controller.getRunState()
                ),
                () -> assertSame(completedWave, controller.getCurrentWave()),
                () -> assertNotNull(controller.getCurrentUpgradeSession()),
                () -> assertEquals(1, progression.getPendingLevelUps())
        );

        controller.selectUpgradeOption(
                controller.getCurrentUpgradeSession().getCurrentChoices().get(0)
        );

        assertAll(
                () -> assertEquals(RunState.ACTIVE_WAVE, controller.getRunState()),
                () -> assertEquals(
                        expectedNextWaveNumber,
                        controller.getCurrentWave().getWaveNumber()
                ),
                () -> assertEquals(
                        controller.getCurrentWave().getEnemies(),
                        world.getEnemies()
                ),
                () -> assertNull(controller.getCurrentUpgradeSession()),
                () -> assertFalse(progression.hasPendingLevelUp()),
                () -> assertEquals(1, statistics.getUpgradesChosen()),
                () -> assertEquals(1, statistics.getWavesCompleted())
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

        return createController(
                world,
                progression,
                statistics,
                weapon,
                wave,
                catalog,
                deterministicRarityRandom
        );
    }

    private static GameController createDeterministicMixedUpgradeController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Map<WeaponType, Weapon> weapons,
            Wave wave,
            StatType statType,
            ModifierType modifierType,
            double baseValue
    ) {
        StatModifier modifier = new StatModifier(statType, modifierType, baseValue);
        UpgradeCatalog catalog = new UpgradeCatalog(List.of(
                new UpgradeCatalog.Template("Test upgrade A", modifier),
                new UpgradeCatalog.Template("Test upgrade B", modifier),
                new UpgradeCatalog.Template("Test upgrade C", modifier)
        ));
        Random deterministicRandom = new Random(42L) {
            @Override
            public double nextDouble() {
                return 0.0;
            }
        };
        return createController(
                world,
                progression,
                statistics,
                weapons,
                wave,
                catalog,
                deterministicRandom
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

    private static UpgradeOption firstItemOption(UpgradeFixture fixture) {
        return fixture.controller()
                .getCurrentUpgradeSession()
                .getCurrentChoices()
                .stream()
                .filter(UpgradeOption::isItem)
                .findFirst()
                .orElseThrow();
    }

    private static Item firstUpgrade(UpgradeFixture fixture) {
        return firstItemOption(fixture).item();
    }

    private static UpgradeOption findItemOption(
            UpgradeChoiceSession session,
            Item item
    ) {
        return session.getCurrentChoices().stream()
                .filter(UpgradeOption::isItem)
                .filter(option -> option.item().equals(item))
                .findFirst()
                .orElseThrow();
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

    private static void assertEnemyKillReward(
            EnemyType enemyType,
            int expectedReward,
            int expectedLevel,
            int expectedCurrentExperience
    ) {
        double enemyX = enemyType == EnemyType.RANGED ? 250.0 : 486.0;
        double projectileX = enemyType == EnemyType.RANGED ? 240.0 : 476.0;
        Enemy enemy = new Enemy(
                new Position(enemyX, 500.0),
                20,
                MOVEMENT_SPEED,
                enemyType
        );
        GameWorld world = createWorld(500.0, 500.0, enemy);
        world.addProjectile(projectileAt(
                projectileX,
                500.0,
                1.0,
                0.0,
                25,
                100.0
        ));
        ExperienceProgression progression = new ExperienceProgression();
        RunStatistics statistics = new RunStatistics();
        GameController controller = createController(
                world,
                progression,
                statistics
        );

        controller.update(0.1);

        assertAll(
                () -> assertTrue(enemy.isDead()),
                () -> assertEquals(expectedLevel, progression.getLevel()),
                () -> assertEquals(
                        expectedCurrentExperience,
                        progression.getCurrentExperience()
                ),
                () -> assertEquals(1, statistics.getEnemiesDefeated()),
                () -> assertEquals(
                        expectedReward,
                        statistics.getExperienceGained()
                )
        );
    }

    private static void advanceUpdates(GameController controller, int updateCount) {
        for (int update = 0; update < updateCount; update++) {
            controller.update(0.1);
        }
    }

    private static BossEncounterFixture createBossEncounterFixture(int waveNumber) {
        Player player = new Player(
                new Position(900.0, 900.0),
                100_000,
                MOVEMENT_SPEED
        );
        Enemy boss = new Enemy(
                new Position(100.0, 100.0),
                EnemyType.BOSS.maxHealth(),
                1.0,
                EnemyType.BOSS
        );
        GameWorld world = new GameWorld(
                WORLD_SIZE,
                WORLD_SIZE,
                player,
                List.of(boss)
        );
        Wave wave = new Wave(waveNumber, List.of(boss));
        GameController controller = createController(world, wave);
        return new BossEncounterFixture(world, boss, wave, controller);
    }

    private static boolean isWithinWorld(Position position, GameWorld world) {
        return position.x() >= 0.0 && position.x() <= world.getWidth()
                && position.y() >= 0.0 && position.y() <= world.getHeight();
    }

    private record BossEncounterFixture(
            GameWorld world,
            Enemy boss,
            Wave wave,
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

    private static Projectile projectileAt(
            double x,
            double y,
            double directionX,
            double directionY,
            int damage,
            double movementSpeed,
            ProjectileOwner owner
    ) {
        return new Projectile(
                new Position(x, y),
                directionX,
                directionY,
                damage,
                movementSpeed,
                owner
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
        GameController controller = createController(world);

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
        GameController controller = createController(
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
        return createController(
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
        GameController controller = createController(world);
        controller.setDirectionActive(direction, true);

        controller.update(0.1);

        assertPosition(expectedX, expectedY, world.getPlayer().getPosition());
    }


    private static GameController createController(GameWorld world) {
        return new GameController(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                Map.of(),
                null,
                new UpgradeCatalog(),
                new Random()
        );
    }

    private static GameController createController(GameWorld world, Wave wave) {
        return new GameController(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                Map.of(),
                wave,
                new UpgradeCatalog(),
                new Random()
        );
    }

    private static GameController createController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics
    ) {
        return new GameController(
                world,
                progression,
                statistics,
                Map.of(),
                null,
                new UpgradeCatalog(),
                new Random()
        );
    }

    private static GameController createController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Weapon weapon
    ) {
        return new GameController(
                world,
                progression,
                statistics,
                Map.of(WeaponType.AUTOMATIC, weapon),
                null,
                new UpgradeCatalog(),
                new Random()
        );
    }

    private static GameController createController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Weapon weapon,
            Wave wave
    ) {
        return new GameController(
                world,
                progression,
                statistics,
                Map.of(WeaponType.AUTOMATIC, weapon),
                wave,
                new UpgradeCatalog(),
                new Random()
        );
    }

    private static GameController createController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Map<WeaponType, Weapon> weapons,
            Wave wave
    ) {
        return new GameController(
                world,
                progression,
                statistics,
                weapons,
                wave,
                new UpgradeCatalog(),
                new Random()
        );
    }

    private static GameController createController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Weapon weapon,
            Wave wave,
            UpgradeCatalog catalog,
            Random random
    ) {
        return new GameController(
                world,
                progression,
                statistics,
                Map.of(WeaponType.AUTOMATIC, weapon),
                wave,
                catalog,
                random
        );
    }

    private static GameController createController(
            GameWorld world,
            ExperienceProgression progression,
            RunStatistics statistics,
            Map<WeaponType, Weapon> weapons,
            Wave wave,
            UpgradeCatalog catalog,
            Random random
    ) {
        return new GameController(
                world,
                progression,
                statistics,
                weapons,
                wave,
                catalog,
                random
        );
    }

    private static void assertPosition(double expectedX, double expectedY, Position actual) {
        assertAll(
                () -> assertEquals(expectedX, actual.x(), TOLERANCE),
                () -> assertEquals(expectedY, actual.y(), TOLERANCE)
        );
    }
}
