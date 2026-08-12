package it.university.survivor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class UpgradeChoiceSession {

    private final UpgradeCatalog catalog;
    private final Random random;
    private List<Item> currentOptions;
    private int remainingRerolls;
    private Item selectedItem;

    public UpgradeChoiceSession(UpgradeCatalog catalog, Random random) {
        this.catalog = Objects.requireNonNull(catalog, "Catalog must not be null");
        this.random = Objects.requireNonNull(random, "Random must not be null");
        this.remainingRerolls = 2;
        this.selectedItem = null;
        this.currentOptions = generateOptions();
    }

    public List<Item> getCurrentOptions() {
        return List.copyOf(currentOptions);
    }

    public int getRemainingRerolls() {
        return remainingRerolls;
    }
    public boolean isSelectionMade() {
        return selectedItem != null;
    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    public void reroll() {
        if (isSelectionMade()) {
            throw new IllegalStateException("Cannot reroll after a selection has been made");
        }
        if (remainingRerolls <= 0) {
            throw new IllegalStateException("No rerolls remaining");
        }
        remainingRerolls--;
        currentOptions = generateOptions();
    }
    public Item selectOption(int index) {
        if (isSelectionMade()) {
            throw new IllegalStateException("A selection has already been made");
        }
        if (index < 0 || index >= currentOptions.size()) {
            throw new IndexOutOfBoundsException("Invalid option index: " + index);

        }
        this.selectedItem = currentOptions.get(index);
        return this.selectedItem;
    }

    private List<Item> generateOptions() {
        List<UpgradeCatalog.Template> templates = new ArrayList<>(catalog.getTemplates());
        Collections.shuffle(templates, random);

        List<Item> options = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UpgradeCatalog.Template template = templates.get(i);
            Rarity drawnRarity = drawRarity();
            options.add(new Item(template.name(), drawnRarity, template.modifier()));
        }
        return Collections.unmodifiableList(options);
    }
    private Rarity drawRarity() {
        double roll = random.nextDouble();
        if (roll < 0.50) {
            return Rarity.COMMON;
        } else if (roll < 0.75) {
            return Rarity.RARE;
        } else if (roll < 0.98) {
            return Rarity.LEGENDARY;
        } else {
            return Rarity.ULTRA;
        }
    }

}
