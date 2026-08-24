package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemyType;

class EnemyTypeTest {

    @Test
    void shouldHaveDifferentStatsForEnemyTypes() {
        assertEquals(100, EnemyType.BASIC.maxHealth());
        assertEquals(1.0, EnemyType.BASIC.movementSpeed());

        assertEquals(60, EnemyType.FAST.maxHealth());
        assertEquals(1.8, EnemyType.FAST.movementSpeed());

        assertEquals(250, EnemyType.TANK.maxHealth());
        assertEquals(0.5, EnemyType.TANK.movementSpeed());
    }

    @Test
    void fastEnemyShouldBeFasterThanBasic() {
        assertTrue(
                EnemyType.FAST.movementSpeed() > EnemyType.BASIC.movementSpeed()
        );
    }

    @Test
    void tankEnemyShouldHaveMoreHealthThanBasic() {
        assertTrue(
                EnemyType.TANK.maxHealth() > EnemyType.BASIC.maxHealth()
        );
    }

    @Test
    void shouldHaveAllRequiredEnemyTypes() {
        assertTrue(contains(EnemyType.BASIC));
        assertTrue(contains(EnemyType.FAST));
        assertTrue(contains(EnemyType.TANK));
        assertTrue(contains(EnemyType.RANGED));
        assertTrue(contains(EnemyType.MINIBOSS));
        assertTrue(contains(EnemyType.BOSS));
    }

    private boolean contains(EnemyType type) {
        for (EnemyType enemyType : EnemyType.values()) {
            if (enemyType == type) {
                return true;
            }
        }

        return false;
    }
}