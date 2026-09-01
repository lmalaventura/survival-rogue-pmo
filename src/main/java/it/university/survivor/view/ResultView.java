package it.university.survivor.view;

import it.university.survivor.controller.RunState;
import it.university.survivor.model.Item;
import it.university.survivor.model.RunStatistics;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

public final class ResultView extends VBox {

    public ResultView(RunState runState, RunStatistics stats, int finalLevel) {
        Objects.requireNonNull(runState, "Run state must not be null");
        Objects.requireNonNull(stats, "RunStatistics must not be null");
        if (runState != RunState.VICTORY && runState != RunState.DEFEAT) {
            throw new IllegalArgumentException("Result view requires a terminal run state");
        }

        setSpacing(10);
        setAlignment(Pos.CENTER);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setStyle("-fx-background-color: rgba(15, 15, 25, 0.92); -fx-padding: 30px;");

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
                statisticLabel("Upgrade scelti: " + stats.getUpgradesChosen()),
                statisticLabel("Reroll usati: " + stats.getRerollsUsed()),
                statisticLabel("Tempo totale: " + formatElapsedTime(stats.getElapsedTime()))
        );

        Label itemsTitle = new Label("ITEM / UPGRADE SCELTI");
        itemsTitle.setStyle(
                "-fx-text-fill: #A0A0A0; -fx-font-size: 14px; "
                        + "-fx-font-weight: bold; -fx-padding: 10 0 0 0;"
        );
        getChildren().add(itemsTitle);

        VBox itemsContainer = new VBox(4.0);
        itemsContainer.setAlignment(Pos.CENTER);
        if (stats.getChosenItems().isEmpty()) {
            Label emptyItemsLabel = new Label("Nessuno");
            emptyItemsLabel.setStyle("-fx-text-fill: #DCDCDC; -fx-font-size: 13px;");
            itemsContainer.getChildren().add(emptyItemsLabel);
        } else {
            for (Item item : stats.getChosenItems()) {
                Label itemLabel = new Label(
                        "- " + item.name()
                                + " [" + item.rarity() + "] "
                                + ItemEffectFormatter.format(item)
                );
                itemLabel.setWrapText(true);
                itemLabel.setStyle("-fx-text-fill: #DCDCDC; -fx-font-size: 13px;");
                itemsContainer.getChildren().add(itemLabel);
            }
        }

        ScrollPane itemsScrollPane = new ScrollPane(itemsContainer);
        itemsScrollPane.setFitToWidth(true);
        itemsScrollPane.setMaxWidth(560.0);
        itemsScrollPane.setMaxHeight(170.0);
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
