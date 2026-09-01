package it.university.survivor.model;

import it.university.survivor.model.enemy.Boss;
import it.university.survivor.model.enemy.EnemyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossTest {

    @Test
    void bossShouldHaveCorrectPositionHealthAndSpeed() {
        Boss boss = new Boss(
                new Position(400.0, 300.0),
                1000,
                1.0
        );

        assertEquals(400.0, boss.getPosition().x());
        assertEquals(300.0, boss.getPosition().y());
        assertEquals(EnemyType.BOSS, boss.getType());
        assertEquals(1000, boss.getHealth().getMaxHealth());
        assertEquals(1000, boss.getHealth().getCurrentHealth());
        assertEquals(1.0, boss.getMovementSpeed());
    }

    @Test
    void bossShouldBeAbleToTakeDamageAndDie() {
        Boss boss = new Boss(
                new Position(400.0, 300.0),
                1000,
                1.0
        );

        boss.takeDamage(1200);

        assertTrue(boss.isDead());
        assertEquals(0, boss.getHealth().getCurrentHealth());
        assertEquals(1000, boss.getHealth().getMaxHealth());
    }
}
