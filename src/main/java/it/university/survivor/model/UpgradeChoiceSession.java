package it.university.survivor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class UpgradeChoiceSession {

    private static final int OPTION_COUNT = 3;
    private static final int MIXED_ITEM_COUNT = 2;
    private static final int INITIAL_REROLLS = 2;

    private final UpgradeCatalog catalog;
    private final Random random;
    private final List<WeaponUpgradeChoice> weaponChoices;
    private List<UpgradeOption> currentChoices;
    private int remainingRerolls = INITIAL_REROLLS;
    private UpgradeOption selectedChoice;

    public UpgradeChoiceSession(UpgradeCatalog catalog, Random random) {
        this(catalog, random, List.of());
    }

    public UpgradeChoiceSession(
            UpgradeCatalog catalog,
            Random random,
            List<WeaponUpgradeChoice> weaponChoices
    ) {
        this.catalog = Objects.requireNonNull(catalog, "Catalog must not be null");
        this.random = Objects.requireNonNull(random, "Random must not be null");
        Objects.requireNonNull(weaponChoices, "Weapon choices must not be null");
        if (weaponChoices.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Weapon choices must not contain null values");
        }
        this.weaponChoices = List.copyOf(weaponChoices);
        currentChoices = generateChoices();
    }

    public List<UpgradeOption> getCurrentChoices() {
        return List.copyOf(currentChoices);
    }

    public int getRemainingRerolls() {
        return remainingRerolls;
    }

    public boolean isSelectionMade() {
        return selectedChoice != null;
    }

    public UpgradeOption getSelectedChoice() {
        return selectedChoice;
    }

    public void reroll() {
        if (isSelectionMade()) {
            throw new IllegalStateException("Cannot reroll after a selection has been made");
        }
        if (remainingRerolls == 0) {
            throw new IllegalStateException("No rerolls remaining");
        }
        remainingRerolls--;
        currentChoices = generateChoices();
    }

    public UpgradeOption selectChoice(int index) {
        if (isSelectionMade()) {
            throw new IllegalStateException("A selection has already been made");
        }
        if (index < 0 || index >= currentChoices.size()) {
            throw new IndexOutOfBoundsException("Invalid option index: " + index);
        }
        selectedChoice = currentChoices.get(index);
        return selectedChoice;
    }

    private List<UpgradeOption> generateChoices() {
        List<UpgradeCatalog.Template> templates = new ArrayList<>(catalog.getTemplates());
        Collections.shuffle(templates, random);

        int itemCount = weaponChoices.isEmpty() ? OPTION_COUNT : MIXED_ITEM_COUNT;
        List<UpgradeOption> choices = new ArrayList<>(OPTION_COUNT);
        for (int index = 0; index < itemCount; index++) {
            UpgradeCatalog.Template template = templates.get(index);
            choices.add(UpgradeOption.forItem(new Item(
                    template.name(),
                    drawRarity(),
                    template.modifier()
            )));
        }

        if (!weaponChoices.isEmpty()) {
            choices.add(UpgradeOption.forWeapon(
                    weaponChoices.get(random.nextInt(weaponChoices.size()))
            ));
            Collections.shuffle(choices, random);
        }

        return List.copyOf(choices);
    }

    private Rarity drawRarity() {
        double roll = random.nextDouble();
        if (roll < 0.50) {
            return Rarity.COMMON;
        }
        if (roll < 0.75) {
            return Rarity.RARE;
        }
        if (roll < 0.90) {
            return Rarity.EPIC;
        }
        if (roll < 0.98) {
            return Rarity.LEGENDARY;
        }
        return Rarity.ULTRA;
    }
}
