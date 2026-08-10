package it.university.survivor.model.enemy;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EnemySpawner {

    private final int maxHealth;
    private final double movementSpeed;

    public EnemySpawner(int maxHealth, double movementSpeed) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be greater than zero");
        }

        if (!Double.isFinite(movementSpeed) || movementSpeed <= 0.0) {
            throw new IllegalArgumentException(
                    "Movement speed must be finite and greater than zero"
            );
        }

        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
    }

    public List<Enemy> spawn(List<Position> positions) {
        Objects.requireNonNull(positions, "Positions must not be null");

        if (positions.size() != 3) {
            throw new IllegalArgumentException(
                    "A wave must spawn exactly 3 enemies"
            );
        }

        List<Enemy> enemies = new ArrayList<>();

        for (Position position : positions) {
            enemies.add(new Enemy(position, maxHealth, movementSpeed));
        }

        return List.copyOf(enemies);
    }
}