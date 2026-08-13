package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.Wave;

class WaveTest {

    private Enemy createEnemy() {
        return new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );
    }

    @Test
    void waveShouldRejectNullEnemyList() {
        assertThrows(NullPointerException.class, () -> new Wave(null));
    }

    @Test
    void waveShouldRejectEmptyEnemyList() {
        assertThrows(IllegalArgumentException.class, () -> new Wave(List.of()));
    }

    @Test
    void waveShouldAcceptOneEnemy() {
        Wave wave = new Wave(List.of(createEnemy()));

        assertEquals(1, wave.getEnemies().size());
    }

    @Test
    void waveShouldAcceptMoreThanThreeEnemies() {
        Wave wave = new Wave(List.of(
            createEnemy(),
            createEnemy(),
            createEnemy(),
            createEnemy()
        ));

        assertEquals(4, wave.getEnemies().size());
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