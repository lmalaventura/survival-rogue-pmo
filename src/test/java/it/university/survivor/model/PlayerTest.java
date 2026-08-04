package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerTest {

    @Test
    void exposesItsInitialPosition() {
        Position initialPosition = new Position(2.0, 3.0);
        Player player = new Player(initialPosition);

        assertEquals(initialPosition, player.getPosition());
    }

    @Test
    void movesToANewPosition() {
        Player player = new Player(new Position(2.0, 3.0));
        Position newPosition = new Position(8.0, 5.0);

        player.moveTo(newPosition);

        assertEquals(newPosition, player.getPosition());
    }

    @Test
    void rejectsNullPositions() {
        Player player = new Player(new Position(0.0, 0.0));

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new Player(null)),
                () -> assertThrows(NullPointerException.class, () -> player.moveTo(null))
        );
    }
}
