package it.university.survivor;

import it.university.survivor.controller.GameController;
import it.university.survivor.controller.MovementDirection;
import it.university.survivor.controller.RunState;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.UpgradeChoiceSession;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveFactory;
import it.university.survivor.view.GameView;
import it.university.survivor.view.ResultView;
import it.university.survivor.view.RunInfoView;
import it.university.survivor.view.UpgradeView;
import it.university.survivor.weapon.NearestEnemyAttackStrategy;
import it.university.survivor.weapon.Weapon;
import it.university.survivor.weapon.WeaponStats;
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
    private static final double WEAPON_COOLDOWN_SECONDS = 0.75;
    private static final int WEAPON_DAMAGE = 25;
    private static final double PROJECTILE_SPEED = 300.0;

    private GameLoop gameLoop;
    private UpgradeChoiceSession displayedUpgradeSession;
    private UpgradeView displayedUpgradeView;
    private RunInfoView displayedRunInfoView;
    private RunState displayedResultState;
    private ResultView displayedResultView;

    @Override
    public void start(Stage primaryStage) {
        Player player = new Player(
                new Position(400.0, 300.0),
                PLAYER_MAX_HEALTH,
                PLAYER_MOVEMENT_SPEED
        );
        Wave initialWave = WaveFactory.createWave(1, List.of(
                new Position(24.0, 150.0),
                new Position(400.0, 24.0),
                new Position(776.0, 450.0)
        ));
        GameWorld world = new GameWorld(
                ARENA_WIDTH,
                ARENA_HEIGHT,
                player,
                initialWave.getEnemies()
        );
        ExperienceProgression progression = new ExperienceProgression();
        RunStatistics statistics = new RunStatistics();
        Weapon weapon = new Weapon(
                new WeaponStats(
                        WEAPON_COOLDOWN_SECONDS,
                        WEAPON_DAMAGE,
                        PROJECTILE_SPEED
                ),
                new NearestEnemyAttackStrategy()
        );
        GameController controller = new GameController(
                world,
                progression,
                statistics,
                weapon,
                initialWave
        );
        GameView view = new GameView(ARENA_WIDTH, ARENA_HEIGHT, progression);
        StackPane sceneRoot = new StackPane(view.getRoot());
        gameLoop = new GameLoop(
                world,
                controller,
                view,
                () -> synchronizeOverlays(
                        sceneRoot,
                        controller,
                        progression,
                        statistics
                ),
                () -> displayedRunInfoView != null
        );

        Scene scene = new Scene(sceneRoot, ARENA_WIDTH, ARENA_HEIGHT);
        EnumSet<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);

        scene.setOnKeyPressed(event -> {
            boolean newlyPressed = pressedKeys.add(event.getCode());
            if (newlyPressed && event.getCode() == KeyCode.I) {
                toggleRunInfoOverlay(
                        sceneRoot,
                        world,
                        controller,
                        progression,
                        statistics,
                        weapon
                );
            }
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

    private void synchronizeOverlays(
            StackPane sceneRoot,
            GameController controller,
            ExperienceProgression progression,
            RunStatistics statistics
    ) {
        synchronizeUpgradeOverlay(sceneRoot, controller);
        synchronizeResultOverlay(sceneRoot, controller, progression, statistics);

        if (controller.getRunState() != RunState.ACTIVE_WAVE) {
            removeRunInfoOverlay(sceneRoot);
        }
    }

    private void synchronizeUpgradeOverlay(
            StackPane sceneRoot,
            GameController controller
    ) {
        UpgradeChoiceSession currentSession = controller.getCurrentUpgradeSession();
        if (controller.getRunState() != RunState.UPGRADE_SELECTION
                || currentSession == null) {
            removeUpgradeOverlay(sceneRoot);
            return;
        }

        if (currentSession == displayedUpgradeSession) {
            return;
        }

        removeUpgradeOverlay(sceneRoot);
        displayedUpgradeSession = currentSession;
        displayedUpgradeView = new UpgradeView(
                currentSession,
                item -> {
                    controller.selectUpgrade(item);
                    synchronizeUpgradeOverlay(sceneRoot, controller);
                },
                controller::rerollUpgradeChoices
        );
        sceneRoot.getChildren().add(displayedUpgradeView);
    }

    private void removeUpgradeOverlay(StackPane sceneRoot) {
        if (displayedUpgradeView != null) {
            sceneRoot.getChildren().remove(displayedUpgradeView);
        }
        displayedUpgradeView = null;
        displayedUpgradeSession = null;
    }

    private void toggleRunInfoOverlay(
            StackPane sceneRoot,
            GameWorld world,
            GameController controller,
            ExperienceProgression progression,
            RunStatistics statistics,
            Weapon weapon
    ) {
        if (controller.getRunState() != RunState.ACTIVE_WAVE) {
            return;
        }
        if (displayedRunInfoView != null) {
            removeRunInfoOverlay(sceneRoot);
            return;
        }

        displayedRunInfoView = new RunInfoView(
                world.getPlayer(),
                progression,
                weapon.getCurrentStats(),
                statistics,
                controller.getCurrentWave()
        );
        sceneRoot.getChildren().add(displayedRunInfoView);
    }

    private void removeRunInfoOverlay(StackPane sceneRoot) {
        if (displayedRunInfoView != null) {
            sceneRoot.getChildren().remove(displayedRunInfoView);
        }
        displayedRunInfoView = null;
    }

    private void synchronizeResultOverlay(
            StackPane sceneRoot,
            GameController controller,
            ExperienceProgression progression,
            RunStatistics statistics
    ) {
        RunState currentState = controller.getRunState();
        if (currentState != RunState.VICTORY
                && currentState != RunState.DEFEAT) {
            removeResultOverlay(sceneRoot);
            return;
        }
        if (displayedResultView != null && displayedResultState == currentState) {
            return;
        }

        removeResultOverlay(sceneRoot);
        displayedResultState = currentState;
        displayedResultView = new ResultView(
                currentState,
                statistics,
                progression.getLevel()
        );
        sceneRoot.getChildren().add(displayedResultView);
    }

    private void removeResultOverlay(StackPane sceneRoot) {
        if (displayedResultView != null) {
            sceneRoot.getChildren().remove(displayedResultView);
        }
        displayedResultView = null;
        displayedResultState = null;
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
