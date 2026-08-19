package it.university.survivor.weapon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import it.university.survivor.model.Enemy;
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
    void shouldRejectZeroDirection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        0.0,
                        0.0,
                        10,
                        5.0
                )
        );
    }

    @Test
    void shouldNormalizeDirection() {
        ProjectileSpawnRequest request =
                new ProjectileSpawnRequest(
                        new Position(0, 0),
                        3.0,
                        4.0,
                        10,
                        5.0
                );

        assertEquals(0.6, request.directionX(), 1e-9);
        assertEquals(0.8, request.directionY(), 1e-9);
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

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        1.0,
                        0.0,
                        -1,
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

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        1.0,
                        0.0,
                        10,
                        -1.0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        1.0,
                        0.0,
                        10,
                        Double.NaN
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectileSpawnRequest(
                        new Position(0, 0),
                        1.0,
                        0.0,
                        10,
                        Double.POSITIVE_INFINITY
                )
        );
    }
    @Test
void shouldCreateMultipleProjectiles() {

    AttackStrategy strategy =
            new SpreadAttackStrategy();

    Enemy enemy =
            new Enemy(
                    new Position(10, 0),
                    100,
                    1.0
            );

    WeaponStats stats =
            new WeaponStats(
                    1.0,
                    10,
                    5.0,
                    3,
                    30.0
            );

    List<ProjectileSpawnRequest> requests =
            strategy.attackMultiple(
                    new Position(0, 0),
                    List.of(enemy),
                    stats
            );

    assertEquals(3, requests.size());
}
@Test
void shouldFireInAllDirections() {

    AttackStrategy strategy =
            new RadialAttackStrategy();

    WeaponStats stats =
            new WeaponStats(
                    1.0,
                    10,
                    5.0,
                    4,
                    360.0
            );

    List<ProjectileSpawnRequest> requests =
            strategy.attackMultiple(
                    new Position(0, 0),
                    List.of(),
                    stats
            );

    assertEquals(4, requests.size());
}
void shouldFireFarthestEnemy(){
     Enemy near =
        new Enemy(
                new Position(3, 0),
                100,
                1.0
        );

Enemy far =
        new Enemy(
                new Position(10, 0),
                100,
                1.0
        );   
        Optional<ProjectileSpawnRequest> result =
        AttackStrategy.attack(
                new Position(0, 0),
                List.of(near, far),
                stats
        );
}
}
