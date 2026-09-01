package it.university.survivor.view;

import it.university.survivor.model.Item;
import it.university.survivor.model.UpgradeChoiceSession;
import it.university.survivor.model.UpgradeOption;
import it.university.survivor.model.WeaponUpgradeChoice;
import it.university.survivor.weapon.WeaponType;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public final class UpgradeView extends VBox {

    private final UpgradeChoiceSession session;
    private final UpgradeSelectionHandler handler;
    private final Runnable rerollHandler;
    private final VBox optionsContainer;
    private final Button rerollButton;


    public UpgradeView(
            UpgradeChoiceSession session,
            UpgradeSelectionHandler handler,
            Runnable rerollHandler
    ) {
        this.session = Objects.requireNonNull(session, "Session must not be null");
        this.handler = Objects.requireNonNull(handler, "Handler must not be null");
        this.rerollHandler = Objects.requireNonNull(
                rerollHandler,
                "Reroll handler must not be null"
        );

        setSpacing(15);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-padding: 25px;");

        Label titleLabel = new Label("SCEGLI UN POTENZIAMENTO");
        titleLabel.setStyle(
                "-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;"
        );
        getChildren().add(titleLabel);

        optionsContainer = new VBox(10);
        optionsContainer.setAlignment(Pos.CENTER);
        getChildren().add(optionsContainer);

        rerollButton = new Button();
        rerollButton.setStyle(
                "-fx-font-size: 13px; -fx-background-color: #555555; "
                        + "-fx-text-fill: white; -fx-cursor: hand;"
        );
        rerollButton.setOnAction(event -> {
            if (session.getRemainingRerolls() > 0 && !session.isSelectionMade()) {
                rerollHandler.run();
                refreshView();
            }
        });
        getChildren().add(rerollButton);

        refreshView();
    }

    private void refreshView() {
        optionsContainer.getChildren().clear();
        List<UpgradeOption> options = session.getCurrentChoices();

        for (int index = 0; index < options.size(); index++) {
            final int optionIndex = index;
            UpgradeOption option = options.get(index);

            Button button = new Button(formatOption(option));
            button.setMinWidth(360);
            button.setMinHeight(45);
            button.setWrapText(true);
            button.setStyle(
                    "-fx-font-size: 14px; -fx-text-fill: black; -fx-cursor: hand;"
            );
            button.setOnAction(event -> {
                UpgradeOption selected = session.selectChoice(optionIndex);
                handler.onUpgradeSelected(selected);
            });
            optionsContainer.getChildren().add(button);
        }

        int remainingRerolls = session.getRemainingRerolls();
        String rerollGrammar = remainingRerolls == 1 ? "rimasto" : "rimasti";
        rerollButton.setText(
                "Reroll (" + remainingRerolls + " " + rerollGrammar + ")"
        );
        rerollButton.setDisable(
                remainingRerolls <= 0 || session.isSelectionMade()
        );
    }

    static String formatOption(UpgradeOption option) {
        Objects.requireNonNull(option, "Upgrade option must not be null");
        if (option.isItem()) {
            Item item = option.item();
            return String.format(
                    "%s [%s] (%s)",
                    item.name(),
                    item.rarity(),
                    ItemEffectFormatter.format(item)
            );
        }

        WeaponUpgradeChoice choice = option.weaponChoice();
        String status;
        if (choice.isNewWeapon()) {
            status = "NUOVA ARMA";
        } else if (choice.willEvolve()) {
            status = "EVOLUZIONE";
        } else {
            status = "LIVELLO " + choice.offeredLevel();
        }
        return choice.weaponType().name()
                + " [" + status + "] - "
                + weaponDescription(choice.weaponType());
    }

    private static String weaponDescription(WeaponType type) {
        return switch (type) {
            case AUTOMATIC -> "fuoco automatico sul bersaglio più vicino";
            case SHOTGUN -> "ventaglio di proiettili a corto raggio";
            case SNIPER -> "colpo potente sul bersaglio più lontano";
            case PULSE -> "raffica radiale attorno al giocatore";
        };
    }
}
