package it.university.survivor.model;

import it.university.survivor.model.enemy.EnemySpawner;
import it.university.survivor.model.enemy.EnemyType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnemySpawnerTest {

    @Test
    void shouldSpawnConfiguredEnemiesAtRequestedPositions() {
        EnemySpawner spawner = new EnemySpawner(60, 144.0, EnemyType.FAST);
        List<Position> positions = List.of(
                new Position(100.0, 100.0),
                new Position(300.0, 100.0),
                new Position(500.0, 100.0)
        );

        List<Enemy> enemies = spawner.spawn(positions);

        assertEquals(3, enemies.size());
        assertEquals(positions.get(1), enemies.get(1).getPosition());
        assertEquals(60, enemies.get(0).getHealth().getMaxHealth());
        assertEquals(144.0, enemies.get(0).getMovementSpeed(), 1e-9);
        assertEquals(EnemyType.FAST, enemies.get(0).getType());
    }

    @Test
    void shouldSupportSpecialEnemyTypesWithoutSubclasses() {
        Enemy miniBoss = new EnemySpawner(500, 56.0, EnemyType.MINIBOSS)
                .spawn(List.of(new Position(100, 100)))
                .get(0);
        Enemy boss = new EnemySpawner(1000, 48.0, EnemyType.BOSS)
                .spawn(List.of(new Position(400, 300)))
                .get(0);

        assertEquals(EnemyType.MINIBOSS, miniBoss.getType());
        assertEquals(EnemyType.BOSS, boss.getType());
    }

    @Test
    void shouldValidateConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new EnemySpawner(0, 80.0, EnemyType.BASIC));
        assertThrows(IllegalArgumentException.class,
                () -> new EnemySpawner(100, 0.0, EnemyType.BASIC));
        assertThrows(NullPointerException.class,
                () -> new EnemySpawner(100, 80.0, null));
    }

    @Test
    void shouldValidateSpawnPositions() {
        EnemySpawner spawner = new EnemySpawner(100, 80.0, EnemyType.BASIC);
        assertThrows(NullPointerException.class, () -> spawner.spawn(null));
        assertThrows(IllegalArgumentException.class, () -> spawner.spawn(List.of()));
    }

    @Test
    void shouldReturnUnmodifiableEnemyList() {
        EnemySpawner spawner = new EnemySpawner(100, 80.0, EnemyType.BASIC);
        List<Enemy> enemies = spawner.spawn(List.of(new Position(100, 100)));

        assertThrows(
                UnsupportedOperationException.class,
                () -> enemies.add(new Enemy(new Position(0, 0), 100, 80.0))
        );
    }
}
