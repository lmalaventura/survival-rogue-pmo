package it.university.survivor.model;

import java.util.Objects;

public final class Player {

    private Position position;
    private final Health health;

    public Player(Position position, int maxHealth) {
        this.position = Objects.requireNonNull(position, "Position must not be null");
        this.health = new Health(maxHealth);
    }

    public Position getPosition() {
        return position;
    }

    public Health getHealth() {
        return health;
    }

    void moveTo(Position newPosition) {
        position = Objects.requireNonNull(newPosition, "Position must not be null");
    }
}
