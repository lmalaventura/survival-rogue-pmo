package it.university.survivor.model;

import it.university.survivor.model.enemy.Enemy;
import it.university.survivor.model.enemy.Wave;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WaveTest {

    private Enemy createEnemy() {
        return new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );
    }

    @Test
    void waveShouldContainExactlyThreeEnemies() {
        Wave wave = new Wave(List.of(
                createEnemy(),
                createEnemy(),
                createEnemy()
        ));

        assertEquals(3, wave.getEnemies().size());
    }

    @Test
    void waveShouldBeCompletedWhenAllEnemiesAreDead() {
        Enemy enemy1 = createEnemy();
        Enemy enemy2 = createEnemy();
        Enemy enemy3 = createEnemy();

        Wave wave = new Wave(List.of(enemy1, enemy2, enemy3));

        assertFalse(wave.isCompleted());

        enemy1.takeDamage(100);
        enemy2.takeDamage(100);
        enemy3.takeDamage(100);

        assertTrue(wave.isCompleted());
    }

    @Test
    void waveShouldNotBeCompletedIfAtLeastOneEnemyIsAlive() {
        Enemy enemy1 = createEnemy();
        Enemy enemy2 = createEnemy();
        Enemy enemy3 = createEnemy();

        Wave wave = new Wave(List.of(enemy1, enemy2, enemy3));

        enemy1.takeDamage(100);
        enemy2.takeDamage(100);

        assertFalse(wave.isCompleted());
    }
}