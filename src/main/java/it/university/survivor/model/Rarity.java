package it.university.survivor.model;

public enum Rarity {
    COMMON(1.0),
    RARE(1.5),
    EPIC(2.0),
    LEGENDARY(2.5),
    ULTRA(3.0);

    private final double multiplier;

    Rarity(double multiplier){
        this.multiplier = multiplier;
    }

    public double getMultiplier(){
        return multiplier;
    }
}
