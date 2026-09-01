package it.university.survivor.model;

import it.university.survivor.model.enemy.MiniBoss;
import it.university.survivor.model.enemy.EnemyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiniBossTest {

    @Test
    void miniBossShouldHaveCorrectPositionHealthAndSpeed() {
        MiniBoss miniBoss = new MiniBoss(
                new Position(100.0, 200.0),
                300,
                1.5
        );

        assertEquals(100.0, miniBoss.getPosition().x());
        assertEquals(200.0, miniBoss.getPosition().y());
        assertEquals(EnemyType.MINIBOSS, miniBoss.getType());
        assertEquals(300, miniBoss.getHealth().getMaxHealth());
        assertEquals(300, miniBoss.getHealth().getCurrentHealth());
        assertEquals(1.5, miniBoss.getMovementSpeed());
    }

    @Test
    void miniBossShouldBeAbleToTakeDamageAndDie() {
        MiniBoss miniBoss = new MiniBoss(
                new Position(100.0, 200.0),
                300,
                1.5
        );

        miniBoss.takeDamage(300);

        assertTrue(miniBoss.isDead());
    }
}
