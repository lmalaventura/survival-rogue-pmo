package it.university.survivor.model;

import java.util.Objects;


public record StatModifier(StatType statType, double baseValue) {
    public StatModifier {
        Objects.requireNonNull(statType, "StatType must not be null");
        if (!Double.isFinite(baseValue)) {
            throw new IllegalArgumentException("Base value must be finite");
        }
    }
}


