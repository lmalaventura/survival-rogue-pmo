package it.university.survivor.weapon;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public class FarthestEnemyAttackStrategy implements AttackStrategy {

    @Override
    public Optional<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats) {

        Objects.requireNonNull(playerPosition);
        Objects.requireNonNull(targets);
        Objects.requireNonNull(stats);

        Enemy farthestTarget = null;
        double maxDistance = -1.0;

        for (Enemy target : targets) {

            if (target == null
                    || target.isDead()
                    || target.getPosition() == null) {
                continue;
            }

            Position targetPosition = target.getPosition();

            double distance = Math.hypot(
                    targetPosition.x() - playerPosition.x(),
                    targetPosition.y() - playerPosition.y()
            );

            if (distance > maxDistance) {
                maxDistance = distance;
                farthestTarget = target;
            }
        }

        if (farthestTarget == null) {
            return Optional.empty();
        }

        Position targetPosition = farthestTarget.getPosition();

        double dx = targetPosition.x() - playerPosition.x();
        double dy = targetPosition.y() - playerPosition.y();

        if (maxDistance < 1e-6) {
            dx = 1.0;
            dy = 0.0;
            maxDistance = 1.0;
        }

        return Optional.of(
                new ProjectileSpawnRequest(
                        playerPosition,
                        dx / maxDistance,
                        dy / maxDistance,
                        stats.getDamage(),
                        stats.getProjectileSpeed()
                )
        );
    }
}