package it.university.survivor.model;

@FunctionalInterface
public interface UpgradeSelectionHandler {
    void onUpgradeSelected(Item selectedItem);

}
