package it.university.survivor.view;

import it.university.survivor.model.Item;

@FunctionalInterface
public interface UpgradeSelectionHandler {
    void onUpgradeSelected(Item item);

}
