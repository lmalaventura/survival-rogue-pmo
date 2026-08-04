package it.university.survivor.model;

import java.util.Objects;

public final class Player {

    private Position position;

    public Player(Position position) {
        this.position = Objects.requireNonNull(position, "Position must not be null");
    }

    public Position getPosition() {
        return position;
    }

    public void moveTo(Position newPosition) {
        position = Objects.requireNonNull(newPosition, "Position must not be null");
    }
}
