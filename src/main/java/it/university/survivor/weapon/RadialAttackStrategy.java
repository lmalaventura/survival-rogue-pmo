package it.university.survivor.weapon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public class RadialAttackStrategy implements AttackStrategy {

    @Override
    public Optional<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats) {

        List<ProjectileSpawnRequest> requests =
                attackMultiple(
                        playerPosition,
                        targets,
                        stats
                );

        return requests.isEmpty()
                ? Optional.empty()
                : Optional.of(requests.get(0));
    }

    @Override
    public List<ProjectileSpawnRequest> attackMultiple(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats) {

        Objects.requireNonNull(playerPosition);
        Objects.requireNonNull(targets);
        Objects.requireNonNull(stats);

        int count = stats.getProjectileCount();

        if (count <= 0) {
            return List.of();
        }

        List<ProjectileSpawnRequest> requests =
                new ArrayList<>(count);

        double angleStep =
                2.0 * Math.PI / count;

        for (int i = 0; i < count; i++) {

            double angle = i * angleStep;

            requests.add(
                    new ProjectileSpawnRequest(
                            playerPosition,
                            Math.cos(angle),
                            Math.sin(angle),
                            stats.getDamage(),
                            stats.getProjectileSpeed()
                    )
            );
        }

        return requests;
    }
}