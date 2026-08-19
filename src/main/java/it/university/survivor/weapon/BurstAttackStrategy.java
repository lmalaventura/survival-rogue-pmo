package it.university.survivor.weapon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public class BurstAttackStrategy implements AttackStrategy {

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

        List<Enemy> living = new ArrayList<>();

        for (Enemy target : targets) {

            if (target != null
                    && !target.isDead()
                    && target.getPosition() != null) {

                living.add(target);
            }
        }

        living.sort(
                Comparator.comparingDouble(
                        e -> distance(
                                playerPosition,
                                e.getPosition()
                        )
                )
        );

        int requestCount =
                Math.min(
                        stats.getProjectileCount(),
                        living.size()
                );

        List<ProjectileSpawnRequest> requests =
                new ArrayList<>(requestCount);

        for (int i = 0; i < requestCount; i++) {

            Position target =
                    living.get(i).getPosition();

            double dx =
                    target.x() - playerPosition.x();

            double dy =
                    target.y() - playerPosition.y();

            double distance =
                    Math.hypot(dx, dy);

            if (distance < 1e-6) {
                dx = 1.0;
                dy = 0.0;
                distance = 1.0;
            }

            requests.add(
                    new ProjectileSpawnRequest(
                            playerPosition,
                            dx / distance,
                            dy / distance,
                            stats.getDamage(),
                            stats.getProjectileSpeed()
                    )
            );
        }

        return requests;
    }

    private static double distance(
            Position a,
            Position b) {

        return Math.hypot(
                b.x() - a.x(),
                b.y() - a.y()
        );
    }
}