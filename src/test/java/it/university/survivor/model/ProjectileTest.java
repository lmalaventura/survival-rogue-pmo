package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectileTest {

    private static final double TOLERANCE = 1.0e-12;

    @Test
    void exposesItsRuntimeProperties() {
        Position initialPosition = new Position(10.0, 20.0);
        Projectile projectile = new Projectile(
                initialPosition,
                1.0,
                0.0,
                25,
                300.0
        );

        assertAll(
                () -> assertSame(initialPosition, projectile.getPosition()),
                () -> assertEquals(1.0, projectile.getDirectionX(), TOLERANCE),
                () -> assertEquals(0.0, projectile.getDirectionY(), TOLERANCE),
                () -> assertEquals(25, projectile.getDamage()),
                () -> assertEquals(300.0, projectile.getMovementSpeed())
        );
    }

    @Test
    void normalizesDirection() {
        Projectile projectile = new Projectile(
                new Position(0.0, 0.0),
                3.0,
                4.0,
                10,
                100.0
        );

        assertAll(
                () -> assertEquals(0.6, projectile.getDirectionX(), TOLERANCE),
                () -> assertEquals(0.8, projectile.getDirectionY(), TOLERANCE),
                () -> assertEquals(
                        1.0,
                        Math.hypot(projectile.getDirectionX(), projectile.getDirectionY()),
                        TOLERANCE
                )
        );
    }

    @Test
    void normalizesExtremeFiniteDirections() {
        Projectile largeDirection = new Projectile(
                new Position(0.0, 0.0),
                Double.MAX_VALUE,
                Double.MAX_VALUE,
                10,
                100.0
        );
        Projectile smallDirection = new Projectile(
                new Position(0.0, 0.0),
                Double.MIN_VALUE,
                0.0,
                10,
                100.0
        );

        assertAll(
                () -> assertEquals(
                        1.0,
                        Math.hypot(
                                largeDirection.getDirectionX(),
                                largeDirection.getDirectionY()
                        ),
                        TOLERANCE
                ),
                () -> assertEquals(1.0, smallDirection.getDirectionX(), TOLERANCE),
                () -> assertEquals(0.0, smallDirection.getDirectionY(), TOLERANCE)
        );
    }

    @Test
    void rejectsNullPosition() {
        assertThrows(
                NullPointerException.class,
                () -> new Projectile(null, 1.0, 0.0, 10, 100.0)
        );
    }

    @Test
    void rejectsZeroDirection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Projectile(
                        new Position(0.0, 0.0),
                        0.0,
                        -0.0,
                        10,
                        100.0
                )
        );
    }

    @Test
    void rejectsNonFiniteDirectionComponents() {
        Position position = new Position(0.0, 0.0);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(position, Double.NaN, 0.0, 10, 100.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(
                                position,
                                Double.POSITIVE_INFINITY,
                                0.0,
                                10,
                                100.0
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(
                                position,
                                Double.NEGATIVE_INFINITY,
                                0.0,
                                10,
                                100.0
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(position, 0.0, Double.NaN, 10, 100.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(
                                position,
                                0.0,
                                Double.POSITIVE_INFINITY,
                                10,
                                100.0
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(
                                position,
                                0.0,
                                Double.NEGATIVE_INFINITY,
                                10,
                                100.0
                        ))
        );
    }

    @Test
    void rejectsNonPositiveDamage() {
        Position position = new Position(0.0, 0.0);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(position, 1.0, 0.0, 0, 100.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(position, 1.0, 0.0, -1, 100.0))
        );
    }

    @Test
    void rejectsInvalidMovementSpeed() {
        Position position = new Position(0.0, 0.0);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(position, 1.0, 0.0, 10, 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(position, 1.0, 0.0, 10, -1.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(position, 1.0, 0.0, 10, Double.NaN)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(
                                position,
                                1.0,
                                0.0,
                                10,
                                Double.POSITIVE_INFINITY
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Projectile(
                                position,
                                1.0,
                                0.0,
                                10,
                                Double.NEGATIVE_INFINITY
                        ))
        );
    }

    @Test
    void movesToANewPositionWithinTheModelPackage() {
        Projectile projectile = new Projectile(
                new Position(1.0, 2.0),
                1.0,
                0.0,
                10,
                100.0
        );
        Position newPosition = new Position(5.0, 7.0);

        projectile.moveTo(newPosition);

        assertSame(newPosition, projectile.getPosition());
    }

    @Test
    void rejectsNullPositionWhenMoving() {
        Projectile projectile = new Projectile(
                new Position(1.0, 2.0),
                1.0,
                0.0,
                10,
                100.0
        );

        assertThrows(NullPointerException.class, () -> projectile.moveTo(null));
    }
}
