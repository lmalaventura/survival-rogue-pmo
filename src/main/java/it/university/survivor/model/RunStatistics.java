package it.university.survivor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class RunStatistics {

    private int enemiesDefeated;
    private int wavesCompleted;
    private int experienceGained;
    private int upgradesChosen;
    private int weaponChoicesMade;
    private int rerollsUsed;
    private double elapsedTime;
    private final List<Item> chosenItems;

    public RunStatistics() {
        this.enemiesDefeated = 0;
        this.wavesCompleted = 0;
        this.experienceGained = 0;
        this.upgradesChosen = 0;
        this.weaponChoicesMade = 0;
        this.rerollsUsed = 0;
        this.elapsedTime = 0.0;
        this.chosenItems = new ArrayList<>();
    }
    public void recordEnemyDefeated() {
        this.enemiesDefeated++;
    }

    public void recordWaveCompleted() {
        this.wavesCompleted++;
    }

    public void recordExperienceGained(int amount) {
        if(amount < 0) {
            throw new IllegalArgumentException(("Experience gained cannot be negative"));
        }
        this.experienceGained += amount;
    }

    public void recordUpgradeSelected(Item item) {
        Objects.requireNonNull(item, "Item must not be null");
        this.chosenItems.add(item);
        this.upgradesChosen++;
    }

    public void recordWeaponChoice() {
        this.weaponChoicesMade++;
    }

    public void recordReroll() {
        this.rerollsUsed++;
    }
    public void addElapsedTime(double seconds) {
        if(seconds < 0 || Double.isNaN(seconds)|| Double.isInfinite(seconds)) {
            throw new IllegalArgumentException("Elapsed seconds must be a non-negative finite number");

        }
        this.elapsedTime += seconds;
    }
    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public int getWavesCompleted(){
        return wavesCompleted;
    }
    public int getExperienceGained(){
        return experienceGained;
    }
    public int getUpgradesChosen(){
        return upgradesChosen;
    }
    public int getWeaponChoicesMade() {
        return weaponChoicesMade;
    }

    public int getRerollsUsed(){
        return rerollsUsed;
    }
    public double getElapsedTime(){
        return elapsedTime;
    }
    public List<Item> getChosenItems(){
        return Collections.unmodifiableList(chosenItems);
    }


}
