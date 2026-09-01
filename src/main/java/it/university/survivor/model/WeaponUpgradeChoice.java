package it.university.survivor.model;

import it.university.survivor.weapon.WeaponType;

import java.util.Objects;

public record WeaponUpgradeChoice(
        WeaponType weaponType,
        int currentLevel,
        int maxLevel
) {
    public WeaponUpgradeChoice {
        Objects.requireNonNull(weaponType, "Weapon type must not be null");
        if (currentLevel < 0) {
            throw new IllegalArgumentException("Current weapon level cannot be negative");
        }
        if (maxLevel < 2) {
            throw new IllegalArgumentException("Maximum weapon level must be at least two");
        }
        if (currentLevel > maxLevel) {
            throw new IllegalArgumentException("Current weapon level cannot exceed maximum level");
        }
    }

    public boolean isNewWeapon() {
        return currentLevel == 0;
    }

    public int offeredLevel() {
        return isNewWeapon() ? 1 : Math.min(maxLevel, currentLevel + 1);
    }

    public boolean willEvolve() {
        return currentLevel == maxLevel - 1;
    }
}
