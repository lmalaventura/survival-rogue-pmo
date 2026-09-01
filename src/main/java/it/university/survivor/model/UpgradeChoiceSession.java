package it.university.survivor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class UpgradeChoiceSession {

    private static final int OPTION_COUNT = 3;
    private static final int MIXED_ITEM_COUNT = 2;

    private final UpgradeCatalog catalog;
    private final Random random;
    private final List<WeaponUpgradeChoice> weaponChoices;
    private List<UpgradeOption> currentChoices;
    private int remainingRerolls;
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
        this.remainingRerolls = 2;
        this.selectedChoice = null;
        this.currentChoices = generateChoices();
    }

    public List<UpgradeOption> getCurrentChoices() {
        return List.copyOf(currentChoices);
    }

    /**
     * Legacy item-only accessor kept for existing item-focused callers/tests.
     * Mixed runtime sessions should use {@link #getCurrentChoices()}.
     */
    public List<Item> getCurrentOptions() {
        return currentChoices.stream()
                .filter(UpgradeOption::isItem)
                .map(UpgradeOption::item)
                .toList();
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

    public Item getSelectedItem() {
        if (selectedChoice == null) {
            return null;
        }
        if (!selectedChoice.isItem()) {
            throw new IllegalStateException("The selected choice is not an Item");
        }
        return selectedChoice.item();
    }

    public void reroll() {
        if (isSelectionMade()) {
            throw new IllegalStateException("Cannot reroll after a selection has been made");
        }
        if (remainingRerolls <= 0) {
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

    public Item selectOption(int index) {
        UpgradeOption selected = selectChoice(index);
        if (!selected.isItem()) {
            selectedChoice = null;
            throw new IllegalStateException("The selected option is not an Item");
        }
        return selected.item();
    }

    private List<UpgradeOption> generateChoices() {
        List<UpgradeCatalog.Template> templates = new ArrayList<>(catalog.getTemplates());
        Collections.shuffle(templates, random);

        int itemCount = weaponChoices.isEmpty() ? OPTION_COUNT : MIXED_ITEM_COUNT;
        List<UpgradeOption> choices = new ArrayList<>(OPTION_COUNT);
        for (int index = 0; index < itemCount; index++) {
            UpgradeCatalog.Template template = templates.get(index);
            Rarity drawnRarity = drawRarity();
            choices.add(UpgradeOption.forItem(new Item(
                    template.name(),
                    drawnRarity,
                    template.modifier()
            )));
        }

        if (!weaponChoices.isEmpty()) {
            WeaponUpgradeChoice weaponChoice = weaponChoices.get(
                    random.nextInt(weaponChoices.size())
            );
            choices.add(UpgradeOption.forWeapon(weaponChoice));
            Collections.shuffle(choices, random);
        }

        return List.copyOf(choices);
    }

    private Rarity drawRarity() {
        double roll = random.nextDouble();
        if (roll < 0.50) {
            return Rarity.COMMON;
        } else if (roll < 0.75) {
            return Rarity.RARE;
        } else if (roll < 0.90) {
            return Rarity.EPIC;
        } else if (roll < 0.98) {
            return Rarity.LEGENDARY;
        } else {
            return Rarity.ULTRA;
        }
    }
}
