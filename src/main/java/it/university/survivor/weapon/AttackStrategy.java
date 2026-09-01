package it.university.survivor.weapon;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public interface AttackStrategy {

    Optional<ProjectileSpawnRequest> attack(
        Position playerPosition,
        Collection<? extends Enemy> targets,
        WeaponStats stats
    );
    default List<ProjectileSpawnRequest> attackMultiple(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats
    ) {
        return attack(playerPosition, targets, stats)
                .map(List::of)
                .orElseGet(List::of);
    }
}
