package it.university.survivor.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import it.university.survivor.model.Position;

class ProjectileSpawnRequestTest {

    @Test
    void shouldCreateValidRequest() {
        Position origin = new Position(10.0, 20.0);

        ProjectileSpawnRequest request =
                new ProjectileSpawnRequest(
                        origin,
                        1.0,
                        0.0,
                        25,
                        8.0
                );

        assertEquals(origin, request.origin());
        assertEquals(1.0, request.directionX());
        assertEquals(0.0, request.directionY());
        assertEquals(25, request.damage());
        assertEquals(8.0, request.speed());
    }

    @Test
    void shouldRejectNullOrigin() {
        assertThrows(
                NullPointerException.class,
                () -> new ProjectileSpawnRequest(
                        null,
                        1.0,
                        0.0,
                        10,
                        5.0
                )
        );
    }

    @Test
    void shouldRejectInvalidDirectionX() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        Double.NaN,
                        0.0,
                        10,
                        5.0
                )
        );
    }

    @Test
    void shouldRejectInvalidDirectionY() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        1.0,
                        Double.POSITIVE_INFINITY,
                        10,
                        5.0
                )
        );
    }

    @Test
    void shouldRejectInvalidDamage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        1.0,
                        0.0,
                        0,
                        5.0
                )
        );
    }

    @Test
    void shouldRejectInvalidSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        1.0,
                        0.0,
                        10,
                        0.0
                )
        );
    }
}