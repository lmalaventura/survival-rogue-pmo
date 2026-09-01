package it.university.survivor.controller;

import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Item;
import it.university.survivor.model.ModifierType;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.StatModifier;
import it.university.survivor.model.StatType;
import it.university.survivor.model.UpgradeCatalog;
import it.university.survivor.model.UpgradeChoiceSession;
import it.university.survivor.model.UpgradeOption;
import it.university.survivor.model.WeaponUpgradeChoice;
import it.university.survivor.weapon.FlatCooldownUpgrade;
import it.university.survivor.weapon.FlatDamageUpgrade;
import it.university.survivor.weapon.PercentCooldownUpgrade;
import it.university.survivor.weapon.PercentDamageUpgrade;
import it.university.survivor.weapon.Weapon;
import it.university.survivor.weapon.WeaponFactory;
import it.university.survivor.weapon.WeaponType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

final class UpgradeManager {

    private final GameWorld world;
    private final ExperienceProgression experienceProgression;
    private final RunStatistics runStatistics;
    private final Map<WeaponType, Weapon> weapons;
    private final UpgradeCatalog catalog;
    private final Random random;

    UpgradeManager(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Map<WeaponType, Weapon> weapons,
            UpgradeCatalog catalog,
            Random random
    ) {
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.experienceProgression = Objects.requireNonNull(
                experienceProgression,
                "Experience progression must not be null"
        );
        this.runStatistics = Objects.requireNonNull(
                runStatistics,
                "Run statistics must not be null"
        );
        this.weapons = Objects.requireNonNull(weapons, "Weapons must not be null");
        this.catalog = Objects.requireNonNull(catalog, "Upgrade catalog must not be null");
        this.random = Objects.requireNonNull(random, "Upgrade random must not be null");
    }

    UpgradeChoiceSession createSession() {
        return new UpgradeChoiceSession(catalog, random, availableWeaponChoices());
    }

    void reroll(UpgradeChoiceSession session) {
        Objects.requireNonNull(session, "Upgrade session must not be null").reroll();
        runStatistics.recordReroll();
    }

    boolean applyChoice(UpgradeChoiceSession session, UpgradeOption option) {
        Objects.requireNonNull(session, "Upgrade session must not be null");
        Objects.requireNonNull(option, "Upgrade option must not be null");
        if (!experienceProgression.hasPendingLevelUp()) {
            throw new IllegalStateException("No pending level-up is available");
        }

        int optionIndex = findOptionIndex(session, option);
        if (optionIndex < 0) {
            throw new IllegalArgumentException(
                    "Upgrade option does not belong to the current session"
            );
        }
        if (session.isSelectionMade() && !session.getSelectedChoice().equals(option)) {
            throw new IllegalStateException("A different upgrade has already been selected");
        }

        validate(option);
        UpgradeOption selected = session.isSelectionMade()
                ? session.getSelectedChoice()
                : session.selectChoice(optionIndex);
        apply(selected);

        if (!experienceProgression.consumePendingLevelUp()) {
            throw new IllegalStateException("Pending level-up could not be consumed");
        }
        return experienceProgression.hasPendingLevelUp();
    }

    private void validate(UpgradeOption option) {
        if (option.isItem()) {
            validateItem(option.item());
        } else {
            validateWeaponChoice(option.weaponChoice());
        }
    }

    private void apply(UpgradeOption option) {
        if (option.isItem()) {
            Item item = option.item();
            applyItem(item);
            runStatistics.recordUpgradeSelected(item);
        } else {
            applyWeaponChoice(option.weaponChoice());
            runStatistics.recordWeaponChoice();
        }
    }

    private void validateWeaponChoice(WeaponUpgradeChoice choice) {
        Weapon currentWeapon = weapons.get(choice.weaponType());
        if (currentWeapon == null) {
            if (!choice.isNewWeapon()) {
                throw new IllegalStateException("Weapon choice is no longer valid");
            }
            return;
        }

        if (choice.isNewWeapon()
                || currentWeapon.getLevel() != choice.currentLevel()
                || currentWeapon.getMaxLevel() != choice.maxLevel()
                || currentWeapon.isEvolved()) {
            throw new IllegalStateException("Weapon choice is no longer valid");
        }
    }

    private void applyWeaponChoice(WeaponUpgradeChoice choice) {
        Weapon currentWeapon = weapons.get(choice.weaponType());
        if (currentWeapon == null) {
            Weapon unlockedWeapon = WeaponFactory.create(choice.weaponType());
            applyPreviousWeaponItems(unlockedWeapon);
            weapons.put(choice.weaponType(), unlockedWeapon);
        } else {
            currentWeapon.levelUp();
        }
    }

    private void applyPreviousWeaponItems(Weapon weapon) {
        for (Item item : runStatistics.getChosenItems()) {
            StatModifier modifier = item.baseModifier();
            if (modifier.statType() == StatType.DAMAGE
                    || modifier.statType() == StatType.COOLDOWN) {
                applyWeaponModifier(weapon, modifier, item.getEffectiveValue());
            }
        }
    }

    private void validateItem(Item item) {
        StatModifier modifier = item.baseModifier();
        double effectiveValue = item.getEffectiveValue();
        if (!Double.isFinite(effectiveValue)) {
            throw new IllegalArgumentException("Effective upgrade value must be finite");
        }

        switch (modifier.statType()) {
            case MAX_HEALTH -> validateMaxHealthUpgrade(modifier, effectiveValue);
            case DAMAGE -> validateDamageUpgrade(modifier, effectiveValue);
            case COOLDOWN -> validateCooldownUpgrade(modifier, effectiveValue);
        }
    }

    private void validateMaxHealthUpgrade(StatModifier modifier, double effectiveValue) {
        int increment = calculateMaxHealthIncrement(modifier, effectiveValue);
        try {
            Math.addExact(world.getPlayer().getHealth().getMaxHealth(), increment);
            Math.addExact(world.getPlayer().getHealth().getCurrentHealth(), increment);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Maximum health upgrade is too large", exception);
        }
    }

    private void validateDamageUpgrade(StatModifier modifier, double effectiveValue) {
        requireWeapons();
        if (modifier.modifierType() == ModifierType.FLAT) {
            int bonus = roundPositiveIncrement(effectiveValue, "Damage bonus");
            for (Weapon weapon : weapons.values()) {
                try {
                    Math.addExact(weapon.getCurrentStats().getDamage(), bonus);
                } catch (ArithmeticException exception) {
                    throw new IllegalArgumentException("Damage upgrade is too large", exception);
                }
            }
            return;
        }

        requirePositiveFinite(effectiveValue, "Damage percentage");
        for (Weapon weapon : weapons.values()) {
            double upgradedDamage = weapon.getCurrentStats().getDamage() * (1.0 + effectiveValue);
            if (!Double.isFinite(upgradedDamage)
                    || Math.round(upgradedDamage) > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Damage upgrade is too large");
            }
        }
    }

    private void validateCooldownUpgrade(StatModifier modifier, double effectiveValue) {
        requireWeapons();
        double reduction = Math.abs(effectiveValue);
        requirePositiveFinite(reduction, "Cooldown reduction");
        if (modifier.modifierType() == ModifierType.PERCENTAGE && reduction >= 1.0) {
            throw new IllegalArgumentException(
                    "Cooldown percentage reduction must be less than one"
            );
        }
    }

    private void applyItem(Item item) {
        StatModifier modifier = item.baseModifier();
        double effectiveValue = item.getEffectiveValue();

        if (modifier.statType() == StatType.MAX_HEALTH) {
            world.getPlayer().getHealth().increaseMaxHealth(
                    calculateMaxHealthIncrement(modifier, effectiveValue)
            );
            return;
        }

        for (Weapon weapon : weapons.values()) {
            applyWeaponModifier(weapon, modifier, effectiveValue);
        }
    }

    private int calculateMaxHealthIncrement(StatModifier modifier, double effectiveValue) {
        double rawIncrement = modifier.modifierType() == ModifierType.FLAT
                ? effectiveValue
                : world.getPlayer().getHealth().getMaxHealth() * effectiveValue;
        return roundPositiveIncrement(rawIncrement, "Maximum health bonus");
    }

    private static void applyWeaponModifier(
            Weapon weapon,
            StatModifier modifier,
            double effectiveValue
    ) {
        switch (modifier.statType()) {
            case DAMAGE -> {
                if (modifier.modifierType() == ModifierType.FLAT) {
                    weapon.upgrade(new FlatDamageUpgrade(
                            roundPositiveIncrement(effectiveValue, "Damage bonus")
                    ));
                } else {
                    weapon.upgrade(new PercentDamageUpgrade(effectiveValue));
                }
            }
            case COOLDOWN -> {
                double reduction = Math.abs(effectiveValue);
                if (modifier.modifierType() == ModifierType.FLAT) {
                    weapon.upgrade(new FlatCooldownUpgrade(reduction));
                } else {
                    weapon.upgrade(new PercentCooldownUpgrade(reduction));
                }
            }
            case MAX_HEALTH -> throw new IllegalArgumentException(
                    "Maximum health is not a Weapon modifier"
            );
        }
    }

    private List<WeaponUpgradeChoice> availableWeaponChoices() {
        List<WeaponUpgradeChoice> choices = new ArrayList<>();
        for (WeaponType type : WeaponType.values()) {
            Weapon weapon = weapons.get(type);
            if (weapon == null) {
                Weapon preview = WeaponFactory.create(type);
                choices.add(new WeaponUpgradeChoice(type, 0, preview.getMaxLevel()));
            } else if (!weapon.isEvolved()) {
                choices.add(new WeaponUpgradeChoice(
                        type,
                        weapon.getLevel(),
                        weapon.getMaxLevel()
                ));
            }
        }
        return List.copyOf(choices);
    }

    private void requireWeapons() {
        if (weapons.isEmpty()) {
            throw new IllegalStateException("At least one Weapon is required for this upgrade");
        }
    }

    private static int findOptionIndex(UpgradeChoiceSession session, UpgradeOption option) {
        List<UpgradeOption> options = session.getCurrentChoices();
        for (int index = 0; index < options.size(); index++) {
            if (options.get(index) == option) {
                return index;
            }
        }
        return options.indexOf(option);
    }

    private static int roundPositiveIncrement(double value, String description) {
        requirePositiveFinite(value, description);
        long roundedValue = Math.max(1L, Math.round(value));
        if (roundedValue > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(description + " is too large");
        }
        return (int) roundedValue;
    }

    private static void requirePositiveFinite(double value, String description) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(description + " must be finite and positive");
        }
    }
}
