package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthTest {

    @Test
    void initializesAtFullHealthAndAlive() {
        Health health = new Health(100);

        assertAll(
                () -> assertEquals(100, health.getMaxHealth()),
                () -> assertEquals(100, health.getCurrentHealth()),
                () -> assertTrue(health.isAlive()),
                () -> assertFalse(health.isDead())
        );
    }

    @Test
    void rejectsNonPositiveMaximumHealth() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Health(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Health(-1))
        );
    }

    @Test
    void increasesMaximumAndCurrentHealthByTheSameAmount() {
        Health health = new Health(100);
        health.takeDamage(40);

        health.increaseMaxHealth(25);

        assertAll(
                () -> assertEquals(125, health.getMaxHealth()),
                () -> assertEquals(85, health.getCurrentHealth()),
                () -> assertTrue(health.isAlive())
        );
    }

    @Test
    void keepsFullHealthAtTheNewMaximum() {
        Health health = new Health(100);

        health.increaseMaxHealth(30);

        assertAll(
                () -> assertEquals(130, health.getMaxHealth()),
                () -> assertEquals(130, health.getCurrentHealth())
        );
    }

    @Test
    void rejectsNonPositiveMaximumHealthIncreaseWithoutChangingState() {
        Health health = new Health(100);
        health.takeDamage(20);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> health.increaseMaxHealth(0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> health.increaseMaxHealth(-1)),
                () -> assertEquals(100, health.getMaxHealth()),
                () -> assertEquals(80, health.getCurrentHealth())
        );
    }

    @Test
    void rejectsOverflowWithoutChangingState() {
        Health health = new Health(Integer.MAX_VALUE);
        health.takeDamage(1);

        assertThrows(IllegalArgumentException.class, () -> health.increaseMaxHealth(1));

        assertAll(
                () -> assertEquals(Integer.MAX_VALUE, health.getMaxHealth()),
                () -> assertEquals(Integer.MAX_VALUE - 1, health.getCurrentHealth())
        );
    }

    @Test
    void appliesPositiveDamageAndAcceptsZero() {
        Health health = new Health(100);

        health.takeDamage(30);
        assertAll(
                () -> assertEquals(70, health.getCurrentHealth()),
                () -> assertTrue(health.isAlive()),
                () -> assertFalse(health.isDead())
        );

        health.takeDamage(0);
        assertEquals(70, health.getCurrentHealth());
    }

    @Test
    void clampsDamageAtZeroAndKeepsDeadHealthAtZero() {
        Health exactDamage = new Health(100);
        Health excessiveDamage = new Health(100);

        exactDamage.takeDamage(100);
        assertEquals(0, exactDamage.getCurrentHealth());
        exactDamage.takeDamage(10);

        excessiveDamage.takeDamage(60);
        excessiveDamage.takeDamage(100);

        assertAll(
                () -> assertEquals(0, exactDamage.getCurrentHealth()),
                () -> assertEquals(0, excessiveDamage.getCurrentHealth()),
                () -> assertTrue(exactDamage.isDead()),
                () -> assertFalse(exactDamage.isAlive())
        );
    }

    @Test
    void rejectsNegativeDamageWithoutChangingStateEvenWhenDead() {
        Health aliveHealth = new Health(100);
        aliveHealth.takeDamage(20);

        Health deadHealth = new Health(100);
        deadHealth.takeDamage(100);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> aliveHealth.takeDamage(-1)),
                () -> assertEquals(80, aliveHealth.getCurrentHealth()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> deadHealth.takeDamage(-1)),
                () -> assertEquals(0, deadHealth.getCurrentHealth())
        );
    }

    @Test
    void appliesPositiveHealingAndAcceptsZero() {
        Health health = new Health(100);
        health.takeDamage(50);

        health.heal(20);
        assertEquals(70, health.getCurrentHealth());

        health.heal(0);
        assertEquals(70, health.getCurrentHealth());
    }

    @Test
    void capsHealingAtMaximumWithoutOverflow() {
        Health health = new Health(100);

        health.takeDamage(40);
        health.heal(40);
        assertEquals(100, health.getCurrentHealth());

        health.heal(10);
        assertEquals(100, health.getCurrentHealth());

        health.takeDamage(40);
        health.heal(50);
        assertEquals(100, health.getCurrentHealth());

        health.takeDamage(40);
        health.heal(Integer.MAX_VALUE);
        assertEquals(100, health.getCurrentHealth());
    }

    @Test
    void rejectsNegativeHealingWithoutChangingStateEvenWhenDead() {
        Health aliveHealth = new Health(100);
        aliveHealth.takeDamage(20);

        Health deadHealth = new Health(100);
        deadHealth.takeDamage(100);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> aliveHealth.heal(-1)),
                () -> assertEquals(80, aliveHealth.getCurrentHealth()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> deadHealth.heal(-1)),
                () -> assertEquals(0, deadHealth.getCurrentHealth())
        );
    }

    @Test
    void doesNotHealDeadHealth() {
        Health health = new Health(100);
        health.takeDamage(100);

        health.heal(50);

        assertAll(
                () -> assertEquals(0, health.getCurrentHealth()),
                () -> assertFalse(health.isAlive()),
                () -> assertTrue(health.isDead())
        );
    }
}
