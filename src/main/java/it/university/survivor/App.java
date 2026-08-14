package it.university.survivor;

import it.university.survivor.controller.GameController;
import it.university.survivor.controller.MovementDirection;
import it.university.survivor.model.Enemy;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.enemy.EnemySpawner;
import it.university.survivor.view.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.EnumSet;
import java.util.List;

public class App extends Application {

    private static final double ARENA_WIDTH = 800.0;
    private static final double ARENA_HEIGHT = 600.0;
    private static final int PLAYER_MAX_HEALTH = 100;
    private static final double PLAYER_MOVEMENT_SPEED = 200.0;
    private static final int ENEMY_MAX_HEALTH = 100;
    private static final double ENEMY_MOVEMENT_SPEED = 80.0;

    private GameLoop gameLoop;

    @Override
    public void start(Stage primaryStage) {
        Player player = new Player(
                new Position(400.0, 300.0),
                PLAYER_MAX_HEALTH,
                PLAYER_MOVEMENT_SPEED
        );
        EnemySpawner spawner = new EnemySpawner(ENEMY_MAX_HEALTH, ENEMY_MOVEMENT_SPEED);
        List<Enemy> enemies = spawner.spawn(List.of(
                new Position(24.0, 150.0),
                new Position(400.0, 24.0),
                new Position(776.0, 450.0)
        ));
        GameWorld world = new GameWorld(ARENA_WIDTH, ARENA_HEIGHT, player, enemies);
        ExperienceProgression progression = new ExperienceProgression();
        RunStatistics statistics = new RunStatistics();
        GameController controller = new GameController(world, progression, statistics);
        GameView view = new GameView(ARENA_WIDTH, ARENA_HEIGHT, progression);
        gameLoop = new GameLoop(world, controller, view);

        Scene scene = new Scene(view.getRoot(), ARENA_WIDTH, ARENA_HEIGHT);
        EnumSet<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);

        scene.setOnKeyPressed(event -> {
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
