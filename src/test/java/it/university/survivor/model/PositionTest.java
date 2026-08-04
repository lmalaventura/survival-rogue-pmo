package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {

    @Test
    void storesCoordinates() {
        Position position = new Position(12.5, -3.25);

        assertAll(
                () -> assertEquals(12.5, position.x()),
                () -> assertEquals(-3.25, position.y())
        );
    }

    @Test
    void positionsWithTheSameCoordinatesAreEqual() {
        Position first = new Position(4.0, 7.5);
        Position second = new Position(4.0, 7.5);

        assertEquals(first, second);
    }

    @Test
    void rejectsNonFiniteCoordinates() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Position(Double.NaN, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Position(Double.POSITIVE_INFINITY, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Position(Double.NEGATIVE_INFINITY, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Position(0.0, Double.NaN)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Position(0.0, Double.POSITIVE_INFINITY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Position(0.0, Double.NEGATIVE_INFINITY))
        );
    }
}
