package it.university.survivor.weapon;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class SpreadAttackStrategy implements AttackStrategy {

    @Override
    public List<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats
    ) {
        Objects.requireNonNull(playerPosition, "Player position must not be null");
        Objects.requireNonNull(targets, "Targets must not be null");
        Objects.requireNonNull(stats, "Stats must not be null");

        Enemy nearestTarget = findNearestLivingTarget(playerPosition, targets);
        if (nearestTarget == null) {
            return List.of();
        }

        Position aim = nearestTarget.getPosition();
        double baseAngle = Math.atan2(
                aim.y() - playerPosition.y(),
                aim.x() - playerPosition.x()
        );
        int count = stats.getProjectileCount();
        double spreadRadians = Math.toRadians(stats.getSpreadDegrees());
        double step = count == 1 ? 0.0 : spreadRadians / (count - 1);
        double start = baseAngle - spreadRadians / 2.0;

        List<ProjectileSpawnRequest> requests = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = count == 1 ? baseAngle : start + i * step;
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

    private static Enemy findNearestLivingTarget(
            Position playerPosition,
            Collection<? extends Enemy> targets
    ) {
        Enemy nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Enemy target : targets) {
            if (target == null || target.isDead()) {
                continue;
            }

            Position position = target.getPosition();
            double distance = Math.hypot(
                    position.x() - playerPosition.x(),
                    position.y() - playerPosition.y()
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearest = target;
            }
        }
        return nearest;
    }
}
