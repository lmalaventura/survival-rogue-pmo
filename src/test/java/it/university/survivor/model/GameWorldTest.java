package it.university.survivor.model;

import org.junit.jupiter.api.Test;

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
}
