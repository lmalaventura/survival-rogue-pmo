package it.university.survivor.model;

import java.util.Objects;

public record UpgradeOption(Item item, WeaponUpgradeChoice weaponChoice) {

    public UpgradeOption {
        if ((item == null) == (weaponChoice == null)) {
            throw new IllegalArgumentException(
                    "An upgrade option must contain exactly one choice type"
            );
        }
    }

    public static UpgradeOption forItem(Item item) {
        return new UpgradeOption(
                Objects.requireNonNull(item, "Item must not be null"),
                null
        );
    }

    public static UpgradeOption forWeapon(WeaponUpgradeChoice weaponChoice) {
        return new UpgradeOption(
                null,
                Objects.requireNonNull(
                        weaponChoice,
                        "Weapon choice must not be null"
                )
        );
    }

    public boolean isItem() {
        return item != null;
    }

    public boolean isWeapon() {
        return weaponChoice != null;
    }
}
