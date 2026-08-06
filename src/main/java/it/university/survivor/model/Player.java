package it.university.survivor.model;

import java.util.Objects;

public final class Player {

    private Position position;
    private final Health health;
    private final double movementSpeed;

    public Player(Position position, int maxHealth, double movementSpeed) {
        this.position = Objects.requireNonNull(position, "Position must not be null");
        if (!Double.isFinite(movementSpeed) || movementSpeed <= 0.0) {
            throw new IllegalArgumentException("Movement speed must be finite and greater than zero");
        }

        this.health = new Health(maxHealth);
        this.movementSpeed = movementSpeed;
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

    void moveTo(Position newPosition) {
        position = Objects.requireNonNull(newPosition, "Position must not be null");
    }
}
