package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EnemyTest {

    @Test
    void enemyShouldCalculateDirectionTowardsTarget() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        Position direction = enemy.calculateDesiredDirection(
                new Position(3.0, 4.0)
        );

        assertEquals(0.6, direction.x(), 0.0001);
        assertEquals(0.8, direction.y(), 0.0001);
    }

    @Test
    void enemyShouldTakeDamage() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        enemy.takeDamage(30);

        assertEquals(70, enemy.getHealth().getCurrentHealth());
        assertFalse(enemy.isDead());
    }

    @Test
    void enemyShouldDieWhenHealthReachesZero() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        enemy.takeDamage(100);

        assertTrue(enemy.isDead());
        assertEquals(0, enemy.getHealth().getCurrentHealth());
    }

     @Test
    void enemyShouldRejectNullPosition() {
        assertThrows(NullPointerException.class, () ->
            new Enemy(null, 100, 2.0)
        );
    }

    @Test
    void enemyShouldRejectZeroSpeed() {
    assertThrows(IllegalArgumentException.class, () ->
        new Enemy(new Position(0.0, 0.0), 100, 0.0)
        );
    }

    @Test
    void enemyShouldRejectNegativeSpeed() {
    assertThrows(IllegalArgumentException.class, () ->
        new Enemy(new Position(0.0, 0.0), 100, -2.0)
         );
    }

    @Test
    void enemyShouldRejectNonFiniteSpeed() {
    assertThrows(IllegalArgumentException.class, () ->
        new Enemy(new Position(0.0, 0.0), 100, Double.NaN)
        );

    assertThrows(IllegalArgumentException.class, () ->
        new Enemy(new Position(0.0, 0.0), 100, Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void enemyShouldRejectNullTarget() {
    Enemy enemy = new Enemy(
        new Position(0.0, 0.0),
        100,
        2.0
        );

    assertThrows(NullPointerException.class, () ->
        enemy.calculateDesiredDirection(null)
        );
    }

    @Test
    void enemyShouldReturnZeroDirectionForSamePosition() {
    Enemy enemy = new Enemy(
        new Position(0.0, 0.0),
        100,
        2.0
        );

    Position direction = enemy.calculateDesiredDirection(
        new Position(0.0, 0.0)
        );

    assertEquals(0.0, direction.x(), 0.0001);
    assertEquals(0.0, direction.y(), 0.0001);
    }

    @Test
    void enemyShouldExposeMovementSpeed() {
    Enemy enemy = new Enemy(
        new Position(0.0, 0.0),
        100,
        2.0
        );

    assertEquals(2.0, enemy.getMovementSpeed(), 0.0001);
    }

    @Test
    void enemyShouldMoveToNewPosition() {
    Enemy enemy = new Enemy(
        new Position(0.0, 0.0),
        100,
        2.0
        );

    Position newPosition = new Position(5.0, 7.0);

    enemy.moveTo(newPosition);

    assertEquals(newPosition, enemy.getPosition());
    }

    @Test
    void enemyShouldRejectNullPositionInMoveTo() {
    Enemy enemy = new Enemy(
        new Position(0.0, 0.0),
        100,
        2.0
        );

    assertThrows(NullPointerException.class, () ->
        enemy.moveTo(null)
        );
    }
}