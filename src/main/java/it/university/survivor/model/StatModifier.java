package it.university.survivor.model;

import java.util.Objects;


public record StatModifier(StatType statType, ModifierType modifierType, double baseValue) {


    public StatModifier {
        Objects.requireNonNull(statType, "statType must not be null");
        Objects.requireNonNull(modifierType, "modifierType must not be null");  
      
        if(Double.isNaN(baseValue) || Double.isInfinite(baseValue)) {
            throw new IllegalArgumentException("baseValue must be a valid number");
        }
    }
}


