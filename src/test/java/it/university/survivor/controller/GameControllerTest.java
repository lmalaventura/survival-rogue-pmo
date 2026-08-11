package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameControllerTest {

    private static final double WORLD_SIZE = 1_000.0;
    private static final double MOVEMENT_SPEED = 100.0;
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
                () -> assertPosition(510.0, 500.0,
                        world.getPlayer().getPosition()),
                () -> assertEquals(0,
                        world.getPlayer().getHealth().getCurrentHealth())
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
