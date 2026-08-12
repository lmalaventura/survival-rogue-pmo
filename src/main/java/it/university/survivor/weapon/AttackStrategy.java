package it.university.survivor.weapon;

import java.util.Collection;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public interface AttackStrategy {

    Optional<ProjectileSpawnRequest> attack(
        Position playerPosition,
        Collection<? extends Enemy> targets,
        WeaponStats stats
    );
}