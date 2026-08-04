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

        this.width = width;
        this.height = height;
        this.player = Objects.requireNonNull(player, "Player must not be null");
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
}
