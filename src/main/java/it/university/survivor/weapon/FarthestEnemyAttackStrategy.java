package it.university.survivor.weapon;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class FarthestEnemyAttackStrategy implements AttackStrategy {

    @Override
    public List<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats
    ) {
        Objects.requireNonNull(playerPosition, "Player position must not be null");
        Objects.requireNonNull(targets, "Targets must not be null");
        Objects.requireNonNull(stats, "Stats must not be null");

        Enemy farthestTarget = null;
        double maxDistance = -1.0;
        for (Enemy target : targets) {
            if (target == null || target.isDead()) {
                continue;
            }

            double distance = distance(playerPosition, target.getPosition());
            if (distance > maxDistance) {
                maxDistance = distance;
                farthestTarget = target;
            }
        }

        if (farthestTarget == null) {
            return List.of();
        }
        return List.of(createRequest(playerPosition, farthestTarget.getPosition(), stats));
    }

    private static ProjectileSpawnRequest createRequest(
            Position origin,
            Position target,
            WeaponStats stats
    ) {
        double deltaX = target.x() - origin.x();
        double deltaY = target.y() - origin.y();
        double distance = Math.hypot(deltaX, deltaY);
        if (distance < 1e-6) {
            deltaX = 1.0;
            deltaY = 0.0;
            distance = 1.0;
        }

        return new ProjectileSpawnRequest(
                origin,
                deltaX / distance,
                deltaY / distance,
                stats.getDamage(),
                stats.getProjectileSpeed()
        );
    }

    private static double distance(Position first, Position second) {
        return Math.hypot(second.x() - first.x(), second.y() - first.y());
    }
}
