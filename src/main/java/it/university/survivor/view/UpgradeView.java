package it.university.survivor.view;


import it.university.survivor.model.Item;
import it.university.survivor.model.ModifierType;
import it.university.survivor.model.UpgradeChoiceSession;
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

    public UpgradeView(UpgradeChoiceSession session, UpgradeSelectionHandler handler) {
        this(session, handler, () -> session.reroll());
    }

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
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        getChildren().add(titleLabel);

        optionsContainer = new VBox(10);
        optionsContainer.setAlignment(Pos.CENTER);
        getChildren().add(optionsContainer);

        rerollButton = new Button();
        rerollButton.setStyle("-fx-font-size: 13px; -fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");
        rerollButton.setOnAction(e -> {
            if(session.getRemainingRerolls() > 0 &&!session.isSelectionMade()) {
                rerollHandler.run();
                refreshView();
            }
        });

        getChildren().add(rerollButton);

        refreshView();

        }
        private void refreshView() {
            optionsContainer.getChildren().clear();
            List<Item> options = session.getCurrentOptions();

            for(int i = 0; i < options.size(); i++) {
                final int index = i;
                Item item = options.get(i);

                String valueStr = item.baseModifier().modifierType() == ModifierType.PERCENTAGE
                ? String.format("%+.0f%%", item.getEffectiveValue() * 100)
                : String.format("%+.0f", item.getEffectiveValue());

                String label = String.format("%s [%s] (%s %s)",
                    item.name(),
                    item.rarity(),
                    valueStr,
                    item.baseModifier().statType());


                Button button = new Button(label);
                button.setMinWidth(320);
                button.setMinHeight(45);
                button.setStyle("-fx-font-size: 14px; -fx-text-fill: black; -fx-cursor: hand;");

                button.setOnAction(event -> {
                    Item selected = session.selectOption(index);
                    handler.onUpgradeSelected(selected);
                });

                optionsContainer.getChildren().add(button);
            }

            rerollButton.setText("Reroll (" + session.getRemainingRerolls() + " rimasto)");
            rerollButton.setDisable(session.getRemainingRerolls() <= 0 || session.isSelectionMade());
        }

    }


