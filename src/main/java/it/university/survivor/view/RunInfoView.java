package it.university.survivor.view;

import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.Health;
import it.university.survivor.model.Item;
import it.university.survivor.model.Player;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.weapon.Weapon;
import it.university.survivor.weapon.WeaponStats;
import it.university.survivor.weapon.WeaponType;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class RunInfoView extends VBox {

    private static final int FINAL_WAVE_NUMBER = 15;

    public RunInfoView(
            Player player,
            ExperienceProgression experienceProgression,
            Map<WeaponType, Weapon> weapons,
            RunStatistics runStatistics,
            Wave currentWave
    ) {
        Objects.requireNonNull(player, "Player must not be null");
        Objects.requireNonNull(
                experienceProgression,
                "Experience progression must not be null"
        );
        Objects.requireNonNull(weapons, "Weapons must not be null");
        Objects.requireNonNull(runStatistics, "Run statistics must not be null");
        Objects.requireNonNull(currentWave, "Current wave must not be null");

        setSpacing(12.0);
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(460.0);
        setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.88);"
                        + " -fx-padding: 16px;"
        );

        Label title = new Label("RUN INFO");
        title.setStyle(
                "-fx-text-fill: white;"
                        + " -fx-font-size: 22px;"
                        + " -fx-font-weight: bold;"
        );
        getChildren().add(title);

        Health health = player.getHealth();
        addSection(
                "PLAYER",
                "Level: " + experienceProgression.getLevel(),
                "HP: " + health.getCurrentHealth() + " / " + health.getMaxHealth(),
                "XP: " + experienceProgression.getCurrentExperience()
                        + " / " + experienceProgression.getExperienceForNextLevel()
        );

        addWeapons(weapons);

        addSection(
                "RUN",
                "Wave: " + currentWave.getWaveNumber() + " / " + FINAL_WAVE_NUMBER,
                "Enemies defeated: " + runStatistics.getEnemiesDefeated(),
                "Time: " + formatElapsedTime(runStatistics.getElapsedTime())
        );

        addUpgrades(runStatistics.getChosenItems());
    }

    private void addWeapons(Map<WeaponType, Weapon> weapons) {
        VBox section = new VBox(4.0);
        section.getChildren().add(createSectionTitle("WEAPONS"));
        if (weapons.isEmpty()) {
            section.getChildren().add(createContentLabel("Nessuna"));
        } else {
            for (WeaponType type : WeaponType.values()) {
                Weapon weapon = weapons.get(type);
                if (weapon == null) {
                    continue;
                }
                WeaponStats stats = weapon.getCurrentStats();
                String evolutionStatus = weapon.isEvolved() ? "EVOLUTA" : "BASE";
                section.getChildren().add(createContentLabel(
                        type.name()
                                + " Lv." + weapon.getLevel() + "/" + weapon.getMaxLevel()
                                + " [" + evolutionStatus + "]\n"
                                + "Damage: " + stats.getDamage()
                                + " | Cooldown: " + formatDecimal(stats.getCooldownSeconds()) + " s"
                                + " | Speed: " + formatDecimal(stats.getProjectileSpeed())
                ));
            }
        }

        ScrollPane scrollPane = new ScrollPane(section);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(150.0);
        scrollPane.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent;"
        );
        getChildren().add(scrollPane);
    }

    private void addSection(String title, String... lines) {
        VBox section = new VBox(2.0);
        section.getChildren().add(createSectionTitle(title));
        for (String line : lines) {
            section.getChildren().add(createContentLabel(line));
        }
        getChildren().add(section);
    }

    private void addUpgrades(List<Item> chosenItems) {
        VBox section = new VBox(2.0);
        section.getChildren().add(createSectionTitle("ITEMS"));
        if (chosenItems.isEmpty()) {
            section.getChildren().add(createContentLabel("Nessuno"));
        } else {
            for (Item item : chosenItems) {
                section.getChildren().add(createContentLabel(
                        item.name()
                                + " [" + item.rarity() + "] "
                                + ItemEffectFormatter.format(item)
                ));
            }
        }

        ScrollPane scrollPane = new ScrollPane(section);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(110.0);
        scrollPane.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent;"
        );
        getChildren().add(scrollPane);
    }

    private static Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: #ffd166;"
                        + " -fx-font-size: 15px;"
                        + " -fx-font-weight: bold;"
        );
        return label;
    }

    private static Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        return label;
    }

    static String formatElapsedTime(double elapsedSeconds) {
        long totalSeconds = (long) Math.floor(elapsedSeconds);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    private static String formatDecimal(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
