package it.university.survivor.controller;

import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;

import java.util.EnumSet;
import java.util.Objects;

public final class GameController {

    private static final double MAX_DELTA_SECONDS = 0.1;

    private final GameWorld world;
    private final EnumSet<MovementDirection> activeDirections =
            EnumSet.noneOf(MovementDirection.class);

    public GameController(GameWorld world) {
        this.world = Objects.requireNonNull(world, "World must not be null");
    }

    public void setDirectionActive(MovementDirection direction, boolean active) {
        Objects.requireNonNull(direction, "Direction must not be null");

        if (active) {
            activeDirections.add(direction);
        } else {
            activeDirections.remove(direction);
        }
    }

    public void update(double deltaSeconds) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0.0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        if (deltaSeconds == 0.0) {
            return;
        }

        double directionX = (activeDirections.contains(MovementDirection.RIGHT) ? 1.0 : 0.0)
                - (activeDirections.contains(MovementDirection.LEFT) ? 1.0 : 0.0);
        double directionY = (activeDirections.contains(MovementDirection.DOWN) ? 1.0 : 0.0)
                - (activeDirections.contains(MovementDirection.UP) ? 1.0 : 0.0);

        if (directionX == 0.0 && directionY == 0.0) {
            return;
        }

        double magnitude = Math.hypot(directionX, directionY);
        double normalizedX = directionX / magnitude;
        double normalizedY = directionY / magnitude;
        double effectiveDelta = Math.min(deltaSeconds, MAX_DELTA_SECONDS);

        Player player = world.getPlayer();
        double distance = player.getMovementSpeed() * effectiveDelta;
        world.movePlayerBy(normalizedX * distance, normalizedY * distance);
    }
}
