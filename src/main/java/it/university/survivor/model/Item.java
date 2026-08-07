package it.university.survivor.model;

import java.util.Objects;

public record Item(String name, Rarity rarity, StatModifier baseModifier) {
    public Item {
        Objects.requireNonNull(name,"Name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");

        }
        Objects.requireNonNull(rarity, "Rarity must not be null");
        Objects.requireNonNull(baseModifier, "Base modifier must not be null");
    }

    public double getEffectiveValue() {
        return baseModifier.baseValue() * rarity.getMultiplier();
    }
}
