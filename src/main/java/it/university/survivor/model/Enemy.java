package it.university.survivor.model;

import it.university.survivor.model.Health;
import it.university.survivor.model.Position;

import java.util.Objects;

public class Enemy {

    private Position position;
    private final Health health;
    private final double movementSpeed;

    public Enemy(Position position, int maxHealth, double movementSpeed) {
        this.position = Objects.requireNonNull(position, "Position must not be null");

        if (!Double.isFinite(movementSpeed) || movementSpeed <= 0.0) {
            throw new IllegalArgumentException(
                    "Movement speed must be finite and greater than zero"
            );
        }

        this.health = new Health(maxHealth);
        this.movementSpeed = movementSpeed;
        }
        void moveTo(Position newPosition) {
            this.position = Objects.requireNonNull(newPosition, "Position must not be null");

    }

    public Position getPosition() {
        return position;
    }

    public Health getHealth() {
        return health;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public Position calculateDesiredDirection(Position targetPosition) {
        Objects.requireNonNull(targetPosition, "Target position must not be null");

        double dx = targetPosition.x() - position.x();
        double dy = targetPosition.y() - position.y();

        double magnitude = Math.hypot(dx, dy);

        if (magnitude == 0.0) {
            return new Position(0.0, 0.0);
        }

        return new Position(
                dx / magnitude,
                dy / magnitude
        );
    }

    public void takeDamage(int damage) {
        health.takeDamage(damage);
    }

    public boolean isDead() {
        return health.isDead();
    }
}