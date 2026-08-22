package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.Boss;
import it.university.survivor.model.enemy.EnemySpawner;
import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.MiniBoss;

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
                        new Enemy(
                                new Position(200.0, 200.0),
                                100,
                                2.0
                        )
                )
        );
    }

    @Test
    void spawnerShouldCreateBasicEnemiesWithLegacyConstructor() {
        EnemySpawner spawner = new EnemySpawner(100, 80.0);

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(0.0, 0.0),
                new Position(100.0, 100.0)
        ));

        assertEquals(2, enemies.size());
        assertEquals(EnemyType.BASIC, enemies.get(0).getType());
        assertEquals(EnemyType.BASIC, enemies.get(1).getType());
    }

    @Test
    void spawnerShouldCreateEnemiesWithConfiguredType() {
        EnemySpawner spawner = new EnemySpawner(
                60,
                1.8,
                EnemyType.FAST
        );

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(0.0, 0.0),
                new Position(100.0, 100.0)
        ));

        assertEquals(2, enemies.size());

        assertEquals(EnemyType.FAST, enemies.get(0).getType());
        assertEquals(EnemyType.FAST, enemies.get(1).getType());

        assertEquals(
                60,
                enemies.get(0).getHealth().getMaxHealth()
        );

        assertEquals(
                1.8,
                enemies.get(0).getMovementSpeed(),
                0.0001
        );
    }

    @Test
    void spawnerShouldRejectNullEnemyType() {
        assertThrows(
                NullPointerException.class,
                () -> new EnemySpawner(
                        100,
                        80.0,
                        null
                )
        );
    }

    @Test
    void spawnerShouldCreateMiniBoss() {
        EnemySpawner spawner = new EnemySpawner(
                500,
                0.7,
                EnemyType.MINIBOSS
        );

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(100.0, 100.0)
        ));

        assertEquals(1, enemies.size());
        assertTrue(enemies.get(0) instanceof MiniBoss);
        assertEquals(EnemyType.MINIBOSS, enemies.get(0).getType());
    }

    @Test
    void spawnerShouldCreateBoss() {
        EnemySpawner spawner = new EnemySpawner(
                1000,
                0.6,
                EnemyType.BOSS
        );

        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(400.0, 300.0)
        ));

        assertEquals(1, enemies.size());
        assertTrue(enemies.get(0) instanceof Boss);
        assertEquals(EnemyType.BOSS, enemies.get(0).getType());
    }
}