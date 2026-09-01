package it.university.survivor.weapon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public class SpreadAttackStrategy implements AttackStrategy {

    private final boolean requireTarget;

    public SpreadAttackStrategy() {
        this(true);
    }

    public SpreadAttackStrategy(boolean requireTarget) {
        this.requireTarget = requireTarget;
    }

    @Override
    public Optional<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets,
            WeaponStats stats) {

        List<ProjectileSpawnRequest> requests =
                attackMultiple(playerPosition, targets, stats);

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

        Position aim = findNearestLivingTarget(
                playerPosition,
                targets
        )
                .map(Enemy::getPosition)
                .orElse(null);

        if (requireTarget && aim == null) {
            return List.of();
        }

        double baseAngle = aim == null
                ? 0.0
                : Math.atan2(
                        aim.y() - playerPosition.y(),
                        aim.x() - playerPosition.x()
                );

        int count = stats.getProjectileCount();

        double spreadRadians =
                Math.toRadians(stats.getSpreadDegrees());

        double step =
                count == 1
                        ? 0.0
                        : spreadRadians / (count - 1);

        double start =
                baseAngle - spreadRadians / 2.0;

        List<ProjectileSpawnRequest> requests =
                new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            double angle =
                    count == 1
                            ? baseAngle
                            : start + i * step;

            requests.add(
                    createRequest(
                            playerPosition,
                            angle,
                            stats
                    )
            );
        }

        return requests;
    }

    private static Optional<Enemy> findNearestLivingTarget(
            Position playerPosition,
            Collection<? extends Enemy> targets) {

        Enemy nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Enemy target : targets) {

            if (target == null
                    || target.isDead()
                    || target.getPosition() == null) {
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

        return Optional.ofNullable(nearest);
    }

    private static ProjectileSpawnRequest createRequest(
            Position origin,
            double angle,
            WeaponStats stats) {

        return new ProjectileSpawnRequest(
                origin,
                Math.cos(angle),
                Math.sin(angle),
                stats.getDamage(),
                stats.getProjectileSpeed()
        );
    }
}