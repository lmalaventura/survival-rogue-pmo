package it.university.survivor.view;

import it.university.survivor.model.Item;
import it.university.survivor.model.RunStatistics;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Objects;

public final class ResultView extends VBox {

    public ResultView(RunStatistics stats, int finalLevel) {
        Objects.requireNonNull(stats, "RunStatistics must not be null");

        setSpacing(12);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: rgba(15, 15, 25, 0.90); -fx-padding: 30px;");


        Label titleLabel = new Label("REPORT FINALE RUN");
        titleLabel.setStyle("-fx-test-fill: #FFD700; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label levelLabel = new Label("Livello Finale: " + finalLevel);
        levelLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label wavesLabel = new Label("Ondate Completate: " + stats.getWavesCompleted());
        wavesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label enemiesLabel = new Label("Nemici Sconfitti: " + stats.getEnemiesDefeated());
        enemiesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label xpLabel = new Label("Esperienza Totale: " + stats.getExperienceGained() + " XP");
        xpLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label upgradesLabel = new Label("Upgrade Scelti: " + stats.getUpgradesChosen());
        upgradesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label rerollsLabel = new Label("Reroll Utilizzati: " + stats.getRerollsUsed());
        rerollsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        int minutes = (int) stats.getElapsedTime() / 60;
        int seconds = (int) stats.getElapsedTime() % 60;
        Label timeLabel = new Label(String.format("Tempo di Gioco: %02d:%02d", minutes, seconds));
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        getChildren().addAll(
                titleLabel,
                levelLabel,
                wavesLabel,
                enemiesLabel,
                xpLabel,
                upgradesLabel,
                rerollsLabel,
                timeLabel
        );

        if (!stats.getChosenItems().isEmpty()) {
            Label itemsTitle = new Label("Potenziamenti Acquisiti:");
            itemsTitle.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
            getChildren().add(itemsTitle);

            for (Item item : stats.getChosenItems()) {
                Label itemLabel = new Label("• " + item.name() + " (" + item.rarity() + ")");
                itemLabel.setStyle("-fx-text-fill: #DCDCDC; -fx-font-size: 13px;");
                getChildren().add(itemLabel);
            }
        }
    }
}
    
