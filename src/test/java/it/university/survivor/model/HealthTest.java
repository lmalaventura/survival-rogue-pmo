package it.university.survivor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthTest {

    @Test
    void shouldStartFullAndRejectInvalidMaximum() {
        Health health = new Health(100);
        assertAll(
                () -> assertEquals(100, health.getMaxHealth()),
                () -> assertEquals(100, health.getCurrentHealth()),
                () -> assertTrue(!health.isDead()),
                () -> assertThrows(IllegalArgumentException.class, () -> new Health(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Health(-1))
        );
    }

    @Test
    void shouldIncreaseMaximumAndCurrentHealthTogether() {
        Health health = new Health(100);
        health.takeDamage(40);
        health.increaseMaxHealth(25);

        assertEquals(125, health.getMaxHealth());
        assertEquals(85, health.getCurrentHealth());
    }

    @Test
    void shouldRejectInvalidMaximumHealthIncreaseWithoutChangingState() {
        Health health = new Health(100);
        health.takeDamage(20);

        assertThrows(IllegalArgumentException.class, () -> health.increaseMaxHealth(0));
        assertThrows(IllegalArgumentException.class, () -> health.increaseMaxHealth(-1));
        assertEquals(100, health.getMaxHealth());
        assertEquals(80, health.getCurrentHealth());
    }

    @Test
    void shouldRejectMaximumHealthOverflowWithoutChangingState() {
        Health health = new Health(Integer.MAX_VALUE);
        health.takeDamage(1);

        assertThrows(IllegalArgumentException.class, () -> health.increaseMaxHealth(1));
        assertEquals(Integer.MAX_VALUE, health.getMaxHealth());
        assertEquals(Integer.MAX_VALUE - 1, health.getCurrentHealth());
    }

    @Test
    void shouldApplyAndClampDamage() {
        Health health = new Health(100);
        health.takeDamage(30);
        assertEquals(70, health.getCurrentHealth());

        health.takeDamage(1000);
        assertEquals(0, health.getCurrentHealth());
        assertTrue(health.isDead());

        health.takeDamage(10);
        assertEquals(0, health.getCurrentHealth());
    }

    @Test
    void shouldRejectNegativeDamage() {
        Health health = new Health(100);
        assertThrows(IllegalArgumentException.class, () -> health.takeDamage(-1));
        assertEquals(100, health.getCurrentHealth());
    }
}
