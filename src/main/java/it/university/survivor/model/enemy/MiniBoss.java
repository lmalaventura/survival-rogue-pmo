package it.university.survivor.model.enemy;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public final class MiniBoss extends Enemy {

    public MiniBoss(
            Position position,
            int maxHealth,
            double movementSpeed
    ) {
        super(
                position,
                maxHealth,
                movementSpeed,
                EnemyType.MINIBOSS
        );
    }
}