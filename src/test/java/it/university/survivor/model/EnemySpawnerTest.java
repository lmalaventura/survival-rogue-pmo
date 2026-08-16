package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemySpawner;

class EnemySpawnerTest {

    @Test
    void spawnerShouldCreateOneEnemy() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(100.0, 100.0)
        ));

        assertEquals(1, enemies.size());
    }

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
    void spawnerShouldCreateMoreThanThreeEnemies() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(100.0, 100.0),
                new Position(300.0, 100.0),
                new Position(500.0, 100.0),
                new Position(700.0, 100.0)
        ));

        assertEquals(4, enemies.size());
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
    void spawnedEnemiesShouldHaveConfiguredHealthAndSpeed() {
        EnemySpawner spawner = new EnemySpawner(120, 84.0);

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(100.0, 100.0)
        ));

        assertEquals(120, enemies.get(0).getHealth().getMaxHealth());
        assertEquals(120, enemies.get(0).getHealth().getCurrentHealth());
        assertEquals(84.0, enemies.get(0).getMovementSpeed());
    }

    @Test
    void spawnerShouldRejectNullPositionsList() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        assertThrows(
                NullPointerException.class,
                () -> spawner.spawn(null)
        );
    }

    @Test
    void spawnerShouldRejectEmptyPositionsList() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> spawner.spawn(List.of())
        );
    }

    @Test
    void spawnerShouldRejectNullPositionElement() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        assertThrows(
                NullPointerException.class,
                () -> spawner.spawn(List.of(
                        new Position(100.0, 100.0),
                        null
                ))
        );
    }

    @Test
    void spawnedEnemiesListShouldBeUnmodifiable() {
        EnemySpawner spawner = new EnemySpawner(100, 2.0);

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(100.0, 100.0)
        ));

        assertThrows(
                UnsupportedOperationException.class,
                () -> enemies.add(
                        new Enemy(new Position(200.0, 200.0), 100, 2.0)
                )
        );
    }
}