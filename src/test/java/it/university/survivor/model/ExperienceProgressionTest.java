package it.university.survivor.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class ExperienceProgressionTest {
    
    @Test
    void initialStateIsCorrect() {
        ExperienceProgression exp = new ExperienceProgression();
        assertEquals(1, exp.getLevel());
        assertEquals(0, exp.getCurrentExperience());
        assertEquals(100, exp.getExperienceForNextLevel());
        assertEquals(0.0, exp.getProgress(), 0.0001);
        assertFalse(exp.hasPendingLevelUp());
        assertEquals(0, exp.getPendingLevelUps());
    }

    @Test
    void addExperienceBelowThresHold() {
        ExperienceProgression exp = new ExperienceProgression();
        exp.addExperience(50);

        assertEquals(1, exp.getLevel());
        assertEquals(50, exp.getCurrentExperience());
        assertEquals(100, exp.getExperienceForNextLevel());
        assertEquals(0.5, exp.getProgress());
        assertFalse(exp.hasPendingLevelUp());
    }
    @Test
    void exactThresholdReached() {
        ExperienceProgression exp = new ExperienceProgression();
        exp.addExperience(100);

        assertEquals(2, exp.getLevel());
        assertEquals(0, exp.getCurrentExperience());
        assertEquals(125, exp.getExperienceForNextLevel());
        assertTrue(exp.hasPendingLevelUp());
        assertEquals(1, exp.getPendingLevelUps());
    }

    @Test
    void excessExperiencePreservedAndMultipleLevelUps() {
        ExperienceProgression exp = new ExperienceProgression();
        exp.addExperience(240);

        assertEquals(3, exp.getLevel());
        assertEquals(15, exp.getCurrentExperience());
        assertEquals(150, exp.getExperienceForNextLevel());
        assertEquals(2, exp.getPendingLevelUps());
    }

    @Test
    void rejectsNegativeExperienceAndHandlesZero() {
        ExperienceProgression exp = new ExperienceProgression();
        assertThrows(IllegalArgumentException.class, () -> exp.addExperience(-10));

        exp.addExperience(0);
        assertEquals(0, exp.getCurrentExperience());
    }
    @Test
    void consumesPendingLevelUps() {
        ExperienceProgression exp = new ExperienceProgression();
        exp.addExperience(225);

        assertEquals(2, exp.getPendingLevelUps());

        assertTrue(exp.consumePendingLevelUp());
        assertEquals(1, exp.getPendingLevelUps());

        assertTrue(exp.consumePendingLevelUp());
        assertEquals(0, exp.getPendingLevelUps());

        assertFalse(exp.consumePendingLevelUp());
     }
}
