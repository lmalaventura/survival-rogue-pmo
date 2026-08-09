package it.university.survivor;

import it.university.survivor.controller.GameController;
import it.university.survivor.controller.MovementDirection;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Item;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.Rarity;
import it.university.survivor.model.StatModifier;
import it.university.survivor.model.StatType;
import it.university.survivor.view.GameView;
import it.university.survivor.view.UpgradeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.EnumSet;
import java.util.List;

public class App extends Application {

    private static final double ARENA_WIDTH = 800.0;
    private static final double ARENA_HEIGHT = 600.0;
    private static final int PLAYER_MAX_HEALTH = 100;
    private static final double PLAYER_MOVEMENT_SPEED = 200.0;

    private GameLoop gameLoop;

    @Override
    public void start(Stage primaryStage) {
        Player player = new Player(
                new Position(400.0, 300.0),
                PLAYER_MAX_HEALTH,
                PLAYER_MOVEMENT_SPEED
        );
        GameWorld world = new GameWorld(ARENA_WIDTH, ARENA_HEIGHT, player);
        GameView view = new GameView(ARENA_WIDTH, ARENA_HEIGHT);
        GameController controller = new GameController(world);
        gameLoop = new GameLoop(world, controller, view);

        StatModifier healthMod = new StatModifier(StatType.MAX_HEALTH, 20.0);


        List<Item> testOptions = List.of(
            new Item("Cuore di pietra", Rarity.COMMON, healthMod),
            new Item("Elisir Vitalizzante", Rarity.RARE, healthMod),
            new Item("Benedizione dei Titani", Rarity.EPIC, healthMod)
        );

        UpgradeView upgradeView = new UpgradeView(testOptions, selectedItem -> {
            System.out.println("[TEST UPGRADE] Selezionato: " + selectedItem.name() + " (+ " + selectedItem.getEffectiveValue() + " " 
                                + selectedItem.baseModifier().statType() + ")");
        });

       StackPane root = new StackPane();
       root.getChildren().addAll(view.getRoot(), upgradeView);


        Scene scene = new Scene(root, ARENA_WIDTH, ARENA_HEIGHT);
        EnumSet<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);

        scene.setOnKeyPressed(event -> {

            if(event.getCode() == KeyCode.U) {
                upgradeView.setVisible(!upgradeView.isVisible());
            }


            pressedKeys.add(event.getCode());
            updateLogicalDirections(controller, pressedKeys);
        });


        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
            updateLogicalDirections(controller, pressedKeys);
        });


        primaryStage.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                pressedKeys.clear();
                updateLogicalDirections(controller, pressedKeys);
            }
        });
        primaryStage.setOnCloseRequest(event -> gameLoop.stop());

        primaryStage.setTitle("Survivor Roguelite");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        gameLoop.start();
    }

    @Override
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void updateLogicalDirections(
            GameController controller,
            EnumSet<KeyCode> pressedKeys
    ) {
        controller.setDirectionActive(
                MovementDirection.UP,
                pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)
        );
        controller.setDirectionActive(
                MovementDirection.DOWN,
                pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)
        );
        controller.setDirectionActive(
                MovementDirection.LEFT,
                pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)
        );
        controller.setDirectionActive(
                MovementDirection.RIGHT,
                pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)
        );
    }
}
