package it.university.survivor.weapon;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public class NearestEnemyAttackStrategy implements AttackStrategy {

    @Override
    public Optional<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats) {

        Objects.requireNonNull(playerPosition);
        Objects.requireNonNull(targets);
        Objects.requireNonNull(stats);

        Enemy nearestTarget = null;
        double minDistance = Double.MAX_VALUE;

        for (Enemy target : targets) {

            if (target == null || target.isDead()) {
                continue;
            }

            Position targetPosition = target.getPosition();

            if (targetPosition == null) {
                continue;
            }

            double distance = Math.hypot(
                    targetPosition.x() - playerPosition.x(),
                    targetPosition.y() - playerPosition.y()
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearestTarget = target;
            }
        }

        if (nearestTarget == null) {
            return Optional.empty();
        }

        Position targetPosition = nearestTarget.getPosition();

        double dx = targetPosition.x() - playerPosition.x();
        double dy = targetPosition.y() - playerPosition.y();

        double directionX;
        double directionY;

        if (minDistance < 1e-6) {
            directionX = 1.0;
            directionY = 0.0;
        } else {
            directionX = dx / minDistance;
            directionY = dy / minDistance;
        }

        return Optional.of(
                new ProjectileSpawnRequest(
                        playerPosition,
                        directionX,
                        directionY,
                        stats.getDamage(),
                        stats.getProjectileSpeed()
                )
        );
    }
}