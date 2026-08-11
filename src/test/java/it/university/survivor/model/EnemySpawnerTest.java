package it.university.survivor.model;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.enemy.EnemySpawner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemySpawnerTest {

    @Test
    void spawnerShouldCreateThreeEnemies() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(100.0, 100.0),
                new Position(300.0, 100.0),
                new Position(500.0, 100.0)
        ));

        assertEquals(3, enemies.size());
    }

    @Test
    void spawnedEnemiesShouldHaveCorrectPosition() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(100.0, 100.0),
                new Position(300.0, 100.0),
                new Position(500.0, 100.0)
        ));

        assertEquals(100.0, enemies.get(0).getPosition().x());
        assertEquals(300.0, enemies.get(1).getPosition().x());
        assertEquals(500.0, enemies.get(2).getPosition().x());
    }

    @Test
    void spawnerShouldRejectWrongNumberOfPositions() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> spawner.spawn(List.of(
                        new Position(100.0, 100.0),
                        new Position(300.0, 100.0)
                ))
        );
    }
}