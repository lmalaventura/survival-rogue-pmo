package it.university.survivor;

import it.university.survivor.controller.GameController;
import it.university.survivor.model.GameWorld;
import it.university.survivor.view.GameView;
import javafx.animation.AnimationTimer;

import java.util.Objects;

public final class GameLoop {

    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final GameWorld world;
    private final GameController controller;
    private final GameView view;
    private final AnimationTimer timer;

    private long previousTimestamp;

    public GameLoop(GameWorld world, GameController controller, GameView view) {
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");
        this.view = Objects.requireNonNull(view, "View must not be null");
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleFrame(now);
            }
        };
    }

    public void start() {
        previousTimestamp = 0L;
        view.render(world);
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

        controller.update(deltaSeconds);
        view.render(world);
    }
}
