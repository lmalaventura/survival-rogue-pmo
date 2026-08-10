package it.university.survivor.model;

import it.university.survivor.model.enemy.Enemy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}