package it.university.survivor.model;

import java.util.List;
import java.util.Objects;

public final class GameWorld {

    private final double width;
    private final double height;
    private final Player player;
    private final List<Enemy> enemies;

    public GameWorld(double width, double height, Player player) {
        this(width, height, player, List.of());
    }

    public GameWorld(double width, double height, Player player, List<Enemy> enemies) {
        if (!Double.isFinite(width) || width <= 0.0) {
            throw new IllegalArgumentException("Width must be finite and greater than zero");
        }
        if (!Double.isFinite(height) || height <= 0.0) {
            throw new IllegalArgumentException("Height must be finite and greater than zero");
        }

        Player validatedPlayer = Objects.requireNonNull(player, "Player must not be null");
        Position initialPosition = validatedPlayer.getPosition();
        if (!isWithinBounds(initialPosition, width, height)) {
            throw new IllegalArgumentException("Player position must be within world bounds");
        }

        List<Enemy> validatedEnemies = List.copyOf(
                Objects.requireNonNull(enemies, "Enemies must not be null")
        );
        for (Enemy enemy : validatedEnemies) {
            if (!isWithinBounds(enemy.getPosition(), width, height)) {
                throw new IllegalArgumentException("Enemy position must be within world bounds");
            }
        }

        this.width = width;
        this.height = height;
        this.player = validatedPlayer;
        this.enemies = validatedEnemies;
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

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void movePlayerBy(double deltaX, double deltaY) {
        if (!Double.isFinite(deltaX) || !Double.isFinite(deltaY)) {
            throw new IllegalArgumentException("Movement deltas must be finite");
        }

        player.moveTo(clampMovement(player.getPosition(), deltaX, deltaY));
    }

    public void moveEnemyBy(Enemy enemy, double deltaX, double deltaY) {
        Enemy validatedEnemy = Objects.requireNonNull(enemy, "Enemy must not be null");
        boolean belongsToWorld = enemies.stream()
                .anyMatch(worldEnemy -> worldEnemy == validatedEnemy);
        if (!belongsToWorld) {
            throw new IllegalArgumentException("Enemy must belong to this world");
        }
        if (!Double.isFinite(deltaX) || !Double.isFinite(deltaY)) {
            throw new IllegalArgumentException("Movement deltas must be finite");
        }

        validatedEnemy.moveTo(clampMovement(validatedEnemy.getPosition(), deltaX, deltaY));
    }

    private Position clampMovement(Position currentPosition, double deltaX, double deltaY) {
        double newX = Math.max(0.0, Math.min(width, currentPosition.x() + deltaX));
        double newY = Math.max(0.0, Math.min(height, currentPosition.y() + deltaY));

        return new Position(newX, newY);
    }

    private static boolean isWithinBounds(Position position, double width, double height) {
        return position.x() >= 0.0 && position.x() <= width
                && position.y() >= 0.0 && position.y() <= height;
    }
}
