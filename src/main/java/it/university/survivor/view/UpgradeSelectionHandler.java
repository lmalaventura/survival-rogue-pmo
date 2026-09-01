package it.university.survivor.view;

import it.university.survivor.model.UpgradeOption;

@FunctionalInterface
public interface UpgradeSelectionHandler {
    void onUpgradeSelected(UpgradeOption option);
}
