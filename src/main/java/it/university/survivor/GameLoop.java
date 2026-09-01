package it.university.survivor;

import it.university.survivor.controller.GameController;
import it.university.survivor.model.GameWorld;
import it.university.survivor.view.GameView;
import javafx.animation.AnimationTimer;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class GameLoop {

    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final GameWorld world;
    private final GameController controller;
    private final GameView view;
    private final Runnable uiSynchronizer;
    private final BooleanSupplier simulationPaused;
    private final AnimationTimer timer;

    private long previousTimestamp;

    public GameLoop(GameWorld world, GameController controller, GameView view) {
        this(world, controller, view, () -> {
        }, () -> false);
    }

    public GameLoop(
            GameWorld world,
            GameController controller,
            GameView view,
            Runnable uiSynchronizer
    ) {
        this(world, controller, view, uiSynchronizer, () -> false);
    }

    public GameLoop(
            GameWorld world,
            GameController controller,
            GameView view,
            Runnable uiSynchronizer,
            BooleanSupplier simulationPaused
    ) {
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");
        this.view = Objects.requireNonNull(view, "View must not be null");
        this.uiSynchronizer = Objects.requireNonNull(
                uiSynchronizer,
                "UI synchronizer must not be null"
        );
        this.simulationPaused = Objects.requireNonNull(
                simulationPaused,
                "Simulation pause supplier must not be null"
        );
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleFrame(now);
            }
        };
    }

    public void start() {
        previousTimestamp = 0L;
        renderCurrentState();
        timer.start();
    }

    public void stop() {
        timer.stop();
        previousTimestamp = 0L;
    }

    private void handleFrame(long now) {
        if (previousTimestamp == 0L) {
            previousTimestamp = now;
            return;
        }
        if (now <= previousTimestamp) {
            previousTimestamp = now;
            return;
        }

        double deltaSeconds = (now - previousTimestamp) / NANOS_PER_SECOND;
        previousTimestamp = now;

        advanceSimulation(
                controller,
                deltaSeconds,
                simulationPaused.getAsBoolean()
        );
        renderCurrentState();
    }

    static void advanceSimulation(
            GameController controller,
            double deltaSeconds,
            boolean simulationPaused
    ) {
        Objects.requireNonNull(controller, "Controller must not be null");
        if (!simulationPaused) {
            controller.update(deltaSeconds);
        }
    }

    private void renderCurrentState() {
        view.render(
                world,
                controller.getCurrentWave(),
                controller.getRunState()
        );
        uiSynchronizer.run();
    }
}
