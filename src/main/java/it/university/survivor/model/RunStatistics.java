package it.university.survivor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RunStatistics {

    private int enemiesDefeated;
    private int wavesCompleted;
    private int experienceGained;
    private int upgradesChosen;
    private int weaponChoicesMade;
    private int rerollsUsed;
    private double elapsedTime;
    private final List<Item> chosenItems = new ArrayList<>();

    public void recordEnemyDefeated() {
        enemiesDefeated++;
    }

    public void recordWaveCompleted() {
        wavesCompleted++;
    }

    public void recordExperienceGained(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Experience gained cannot be negative");
        }
        experienceGained += amount;
    }

    public void recordUpgradeSelected(Item item) {
        chosenItems.add(Objects.requireNonNull(item, "Item must not be null"));
        upgradesChosen++;
    }

    public void recordWeaponChoice() {
        weaponChoicesMade++;
    }

    public void recordReroll() {
        rerollsUsed++;
    }

    public void addElapsedTime(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0.0) {
            throw new IllegalArgumentException(
                    "Elapsed seconds must be a non-negative finite number"
            );
        }
        elapsedTime += seconds;
    }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public int getWavesCompleted() {
        return wavesCompleted;
    }

    public int getExperienceGained() {
        return experienceGained;
    }

    public int getUpgradesChosen() {
        return upgradesChosen;
    }

    public int getWeaponChoicesMade() {
        return weaponChoicesMade;
    }

    public int getRerollsUsed() {
        return rerollsUsed;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public List<Item> getChosenItems() {
        return List.copyOf(chosenItems);
    }
}
