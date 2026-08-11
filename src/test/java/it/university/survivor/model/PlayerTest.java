package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerTest {

    @Test
    void exposesItsInitialPosition() {
        Position initialPosition = new Position(2.0, 3.0);
        Player player = new Player(initialPosition, 100, 200.0);

        assertEquals(initialPosition, player.getPosition());
    }

    @Test
    void movesToANewPosition() {
        Player player = new Player(new Position(2.0, 3.0), 100, 200.0);
        Position newPosition = new Position(8.0, 5.0);

        player.moveTo(newPosition);

        assertEquals(newPosition, player.getPosition());
    }

    @Test
    void rejectsNullPositions() {
        Player player = new Player(new Position(0.0, 0.0), 100, 200.0);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new Player(null, 100, 200.0)),
                () -> assertThrows(NullPointerException.class, () -> player.moveTo(null))
        );
    }

    @Test
    void exposesItsInitialHealth() {
        Player player = new Player(new Position(2.0, 3.0), 125, 200.0);
        Health health = player.getHealth();

        assertAll(
                () -> assertEquals(125, health.getMaxHealth()),
                () -> assertEquals(125, health.getCurrentHealth()),
                () -> assertSame(health, player.getHealth())
        );
    }

    @Test
    void exposesItsMovementSpeed() {
        Player player = new Player(new Position(2.0, 3.0), 100, 175.5);

        assertEquals(175.5, player.getMovementSpeed());
    }

    @Test
    void rejectsInvalidMovementSpeeds() {
        Position position = new Position(2.0, 3.0);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Player(position, 100, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Player(position, 100, -1.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Player(position, 100, Double.NaN)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Player(position, 100, Double.POSITIVE_INFINITY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Player(position, 100, Double.NEGATIVE_INFINITY))
        );
    }
}
