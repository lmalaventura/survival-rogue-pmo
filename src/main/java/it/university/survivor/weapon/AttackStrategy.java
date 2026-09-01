package it.university.survivor.weapon;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface AttackStrategy {

    List<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats
    );
}
