package it.university.survivor.controller;

import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import org.junit.jupiter.api.Test;

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
    void rejectsNullWorld() {
        assertThrows(NullPointerException.class, () -> new GameController(null));
    }

    @Test
    void rejectsNullDirection() {
        GameController controller = new GameController(createWorld(500.0, 500.0));

        assertThrows(NullPointerException.class,
                () -> controller.setDirectionActive(null, true));
    }

    private static GameWorld createWorld(double playerX, double playerY) {
        Player player = new Player(
                new Position(playerX, playerY),
                100,
                MOVEMENT_SPEED
        );
        return new GameWorld(WORLD_SIZE, WORLD_SIZE, player);
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
