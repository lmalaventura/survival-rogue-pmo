package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameWorldTest {

    @Test
    void exposesDimensionsAndPlayer() {
        Player player = new Player(new Position(10.0, 20.0));
        GameWorld world = new GameWorld(800.0, 600.0, player);

        assertAll(
                () -> assertEquals(800.0, world.getWidth()),
                () -> assertEquals(600.0, world.getHeight()),
                () -> assertSame(player, world.getPlayer())
        );
    }

    @Test
    void rejectsInvalidWidth() {
        Player player = new Player(new Position(0.0, 0.0));

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
        Player player = new Player(new Position(0.0, 0.0));

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
}
