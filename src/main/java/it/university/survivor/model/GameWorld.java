package it.university.survivor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GameWorld {

    private final double width;
    private final double height;
    private final Player player;
    private final List<Enemy> enemies;
    private final List<Projectile> projectiles = new ArrayList<>();

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

    public List<Projectile> getProjectiles() {
        return Collections.unmodifiableList(projectiles);
    }

    public void addProjectile(Projectile projectile) {
        Projectile validatedProjectile = Objects.requireNonNull(
                projectile,
                "Projectile must not be null"
        );
        if (indexOfProjectile(validatedProjectile) >= 0) {
            return;
        }
        if (!isWithinBounds(validatedProjectile.getPosition(), width, height)) {
            throw new IllegalArgumentException(
                    "Projectile position must be within world bounds"
            );
        }

        projectiles.add(validatedProjectile);
    }

    public void removeProjectile(Projectile projectile) {
        Projectile validatedProjectile = Objects.requireNonNull(
                projectile,
                "Projectile must not be null"
        );
        int projectileIndex = indexOfProjectile(validatedProjectile);
        if (projectileIndex < 0) {
            throw new IllegalArgumentException("Projectile must belong to this world");
        }

        projectiles.remove(projectileIndex);
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

    public void moveProjectileBy(Projectile projectile, double deltaX, double deltaY) {
        Projectile validatedProjectile = Objects.requireNonNull(
                projectile,
                "Projectile must not be null"
        );
        if (indexOfProjectile(validatedProjectile) < 0) {
            throw new IllegalArgumentException("Projectile must belong to this world");
        }
        if (!Double.isFinite(deltaX) || !Double.isFinite(deltaY)) {
            throw new IllegalArgumentException("Movement deltas must be finite");
        }

        Position currentPosition = validatedProjectile.getPosition();
        validatedProjectile.moveTo(new Position(
                currentPosition.x() + deltaX,
                currentPosition.y() + deltaY
        ));
    }

    private Position clampMovement(Position currentPosition, double deltaX, double deltaY) {
        double newX = Math.max(0.0, Math.min(width, currentPosition.x() + deltaX));
        double newY = Math.max(0.0, Math.min(height, currentPosition.y() + deltaY));

        return new Position(newX, newY);
    }

    private int indexOfProjectile(Projectile projectile) {
        for (int index = 0; index < projectiles.size(); index++) {
            if (projectiles.get(index) == projectile) {
                return index;
            }
        }

        return -1;
    }

    private static boolean isWithinBounds(Position position, double width, double height) {
        return position.x() >= 0.0 && position.x() <= width
                && position.y() >= 0.0 && position.y() <= height;
    }
}
