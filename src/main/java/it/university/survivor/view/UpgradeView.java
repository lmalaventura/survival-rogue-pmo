package it.university.survivor.view;


import it.university.survivor.model.Item;
import it.university.survivor.model.UpgradeSelectionHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;



import java.util.List;
import java.util.Objects;


public final class UpgradeView extends VBox {

    public UpgradeView(List<Item> options, UpgradeSelectionHandler handler) {
        Objects.requireNonNull(options, "Options list must not be null");
        Objects.requireNonNull(handler, "Handler must not be null");


        if(options.size() !=3) {
            throw new IllegalArgumentException("UpgradeView requires exactly 3 options");

        }

        setSpacing(15);
        setAlignment(Pos.CENTER);

        setStyle("-fx-background-color: rgba(0, 0, 0, 0.75); -fx-padding: 20px;");

        setVisible(false);

        Label titleLabel = new Label("SCEGLI UN POTENZIAMENTO");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        getChildren().add(titleLabel);


        for(Item item : options) {
            String label = String.format("%s [%s] (+%.0f %s)",
                item.name(),
                item.rarity(),
                item.getEffectiveValue(),
                item.baseModifier().statType());

            Button button = new Button(label);
            button.setMinWidth(280);
            button.setMinHeight(45);
            button.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");

            button.setOnAction(event -> handler.onUpgradeSelected(item));

            getChildren().add(button);
            
        }
    }

}
