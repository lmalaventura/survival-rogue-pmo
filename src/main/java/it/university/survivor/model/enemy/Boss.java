package it.university.survivor.model.enemy;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public final class Boss extends Enemy {

    public Boss(Position position, int maxHealth, double movementSpeed) {
        super(position, maxHealth, movementSpeed);
    }
}