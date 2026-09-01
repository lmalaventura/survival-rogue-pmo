package it.university.survivor.weapon;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class RadialAttackStrategy implements AttackStrategy {

    @Override
    public List<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats
    ) {
        Objects.requireNonNull(playerPosition, "Player position must not be null");
        Objects.requireNonNull(targets, "Targets must not be null");
        Objects.requireNonNull(stats, "Stats must not be null");

        int count = stats.getProjectileCount();
        List<ProjectileSpawnRequest> requests = new ArrayList<>(count);
        double angleStep = 2.0 * Math.PI / count;
        for (int i = 0; i < count; i++) {
            double angle = i * angleStep;
            requests.add(new ProjectileSpawnRequest(
                    playerPosition,
                    Math.cos(angle),
                    Math.sin(angle),
                    stats.getDamage(),
                    stats.getProjectileSpeed()
            ));
        }
        return List.copyOf(requests);
    }
}
