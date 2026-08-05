package it.university.survivor.model;

import java.util.Objects;

public final class GameWorld {

    private final double width;
    private final double height;
    private final Player player;

    public GameWorld(double width, double height, Player player) {
        if (!Double.isFinite(width) || width <= 0.0) {
            throw new IllegalArgumentException("Width must be finite and greater than zero");
        }
        if (!Double.isFinite(height) || height <= 0.0) {
            throw new IllegalArgumentException("Height must be finite and greater than zero");
        }

        Player validatedPlayer = Objects.requireNonNull(player, "Player must not be null");
        Position initialPosition = validatedPlayer.getPosition();
        if (initialPosition.x() < 0.0 || initialPosition.x() > width
                || initialPosition.y() < 0.0 || initialPosition.y() > height) {
            throw new IllegalArgumentException("Player position must be within world bounds");
        }

        this.width = width;
        this.height = height;
        this.player = validatedPlayer;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Player getPlayer() {
        return player;
    }

    public void movePlayerBy(double deltaX, double deltaY) {
        if (!Double.isFinite(deltaX) || !Double.isFinite(deltaY)) {
            throw new IllegalArgumentException("Movement deltas must be finite");
        }

        Position currentPosition = player.getPosition();
        double newX = Math.max(0.0, Math.min(width, currentPosition.x() + deltaX));
        double newY = Math.max(0.0, Math.min(height, currentPosition.y() + deltaY));

        player.moveTo(new Position(newX, newY));
    }
}
