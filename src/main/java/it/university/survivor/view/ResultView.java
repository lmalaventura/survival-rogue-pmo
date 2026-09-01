package it.university.survivor.view;

import it.university.survivor.controller.RunState;
import it.university.survivor.model.Item;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.weapon.Weapon;
import it.university.survivor.weapon.WeaponType;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Objects;

public final class ResultView extends VBox {


    public ResultView(
            RunState runState,
            RunStatistics stats,
            int finalLevel,
            Map<WeaponType, Weapon> weapons
    ) {
        Objects.requireNonNull(runState, "Run state must not be null");
        Objects.requireNonNull(stats, "RunStatistics must not be null");
        Objects.requireNonNull(weapons, "Weapons must not be null");
        if (runState != RunState.VICTORY && runState != RunState.DEFEAT) {
            throw new IllegalArgumentException("Result view requires a terminal run state");
        }

        setSpacing(7);
        setAlignment(Pos.CENTER);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setStyle("-fx-background-color: rgba(15, 15, 25, 0.92); -fx-padding: 20px;");

        Label titleLabel = new Label(
                runState == RunState.VICTORY ? "VICTORY" : "DEFEAT"
        );
        titleLabel.setStyle(titleStyle(runState));

        getChildren().addAll(
                titleLabel,
                statisticLabel("Livello finale: " + finalLevel),
                statisticLabel("Wave completate: " + stats.getWavesCompleted()),
                statisticLabel("Enemy sconfitti: " + stats.getEnemiesDefeated()),
                statisticLabel("XP totale ottenuta: " + stats.getExperienceGained()),
                statisticLabel("Item scelti: " + stats.getUpgradesChosen()),
                statisticLabel("Scelte arma: " + stats.getWeaponChoicesMade()),
                statisticLabel("Reroll usati: " + stats.getRerollsUsed()),
                statisticLabel("Tempo totale: " + formatElapsedTime(stats.getElapsedTime()))
        );

        addWeapons(weapons);
        addItems(stats);
    }

    private void addWeapons(Map<WeaponType, Weapon> weapons) {
        Label weaponsTitle = new Label("ARMI FINALI");
        weaponsTitle.setStyle(sectionTitleStyle());
        getChildren().add(weaponsTitle);

        VBox weaponsContainer = new VBox(4.0);
        weaponsContainer.setAlignment(Pos.CENTER);
        if (weapons.isEmpty()) {
            weaponsContainer.getChildren().add(contentLabel("Nessuna"));
        } else {
            for (WeaponType type : WeaponType.values()) {
                Weapon weapon = weapons.get(type);
                if (weapon == null) {
                    continue;
                }
                weaponsContainer.getChildren().add(contentLabel(
                        type.name()
                                + " Lv." + weapon.getLevel() + "/" + weapon.getMaxLevel()
                                + (weapon.isEvolved() ? " [EVOLUTA]" : "")
                ));
            }
        }
        getChildren().add(weaponsContainer);
    }

    private void addItems(RunStatistics stats) {
        Label itemsTitle = new Label("ITEM SCELTI");
        itemsTitle.setStyle(sectionTitleStyle());
        getChildren().add(itemsTitle);

        VBox itemsContainer = new VBox(4.0);
        itemsContainer.setAlignment(Pos.CENTER);
        if (stats.getChosenItems().isEmpty()) {
            itemsContainer.getChildren().add(contentLabel("Nessuno"));
        } else {
            for (Item item : stats.getChosenItems()) {
                itemsContainer.getChildren().add(contentLabel(
                        "- " + item.name()
                                + " [" + item.rarity() + "] "
                                + ItemEffectFormatter.format(item)
                ));
            }
        }

        ScrollPane itemsScrollPane = new ScrollPane(itemsContainer);
        itemsScrollPane.setFitToWidth(true);
        itemsScrollPane.setMaxWidth(560.0);
        itemsScrollPane.setMaxHeight(100.0);
        itemsScrollPane.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent;"
        );
        getChildren().add(itemsScrollPane);
    }

    private static Label statisticLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        return label;
    }

    private static Label contentLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #DCDCDC; -fx-font-size: 13px;");
        return label;
    }

    private static String sectionTitleStyle() {
        return "-fx-text-fill: #A0A0A0; -fx-font-size: 14px; "
                + "-fx-font-weight: bold; -fx-padding: 10 0 0 0;";
    }

    private static String titleStyle(RunState runState) {
        String titleColor = runState == RunState.VICTORY ? "#FFD700" : "#FF5A5A";
        return "-fx-text-fill: " + titleColor
                + "; -fx-font-size: 36px; -fx-font-weight: bold;";
    }

    private static String formatElapsedTime(double elapsedTime) {
        long totalSeconds = (long) elapsedTime;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
