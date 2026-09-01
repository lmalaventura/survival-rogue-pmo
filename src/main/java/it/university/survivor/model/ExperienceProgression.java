package it.university.survivor.model;

public final class ExperienceProgression {

    private static final int INITIAL_THRESHOLD = 100;
    private static final int THRESHOLD_INCREMENT = 25;

    private int level = 1;
    private int currentExperience;
    private int experienceForNextLevel = calculateThreshold(level);
    private int pendingLevelUps;

    public synchronized void addExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Experience amount must not be negative");
        }
        currentExperience += amount;

        while (currentExperience >= experienceForNextLevel) {
            currentExperience -= experienceForNextLevel;
            level++;
            pendingLevelUps++;
            experienceForNextLevel = calculateThreshold(level);
        }
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentExperience() {
        return currentExperience;
    }

    public int getExperienceForNextLevel() {
        return experienceForNextLevel;
    }

    public double getProgress() {
        return (double) currentExperience / experienceForNextLevel;
    }

    public boolean hasPendingLevelUp() {
        return pendingLevelUps > 0;
    }

    public int getPendingLevelUps() {
        return pendingLevelUps;
    }

    public boolean consumePendingLevelUp() {
        if (!hasPendingLevelUp()) {
            return false;
        }
        pendingLevelUps--;
        return true;
    }

    private static int calculateThreshold(int level) {
        return INITIAL_THRESHOLD + THRESHOLD_INCREMENT * (level - 1);
    }
}
