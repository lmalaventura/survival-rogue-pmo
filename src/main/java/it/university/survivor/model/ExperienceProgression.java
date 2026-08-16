package it.university.survivor.model;

public class ExperienceProgression {

    private int level;
    private int currentExperience;
    private int experienceForNextLevel;
    private int pendingLevelUps;

    public ExperienceProgression() {
        this.level = 1;
        this.currentExperience = 0;
        this.experienceForNextLevel = calculateThreshold(1);
        this.pendingLevelUps = 0;
    }
    private static int calculateThreshold(int level) {
        return 100 + 25 * (level -1);

    }

    public synchronized void addExperience(int amount) {
        if(amount < 0){
            throw new IllegalArgumentException("Experience amount must not be negative");

        }
        if (amount == 0) {
            return;
        }

        currentExperience += amount;
        while (currentExperience >= experienceForNextLevel) {
            currentExperience -= experienceForNextLevel;
            level++;
            pendingLevelUps++;
            experienceForNextLevel = calculateThreshold(level);
        }
    }

    public int getLevel(){
        return level;
    }
    public int getCurrentExperience(){
        return currentExperience;
    }
    public int getExperienceForNextLevel() {
        return experienceForNextLevel;
    }
    public double getProgress() {
        return (double) currentExperience / experienceForNextLevel;
    }
    public boolean hasPendingLevelUp(){
        return pendingLevelUps > 0;
    }
    public int getPendingLevelUps() {
        return pendingLevelUps;
    }
    public boolean consumePendingLevelUp() {
        if (pendingLevelUps > 0) {
            pendingLevelUps--;
            return true;
        }
        return false;
    }
}
