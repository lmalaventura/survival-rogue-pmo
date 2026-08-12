package it.university.survivor.weapon;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public class Weapon {

    private WeaponStats stats;
    private final AttackStrategy attackStrategy;

    private double currentCooldown;

    public Weapon(
            WeaponStats stats,
            AttackStrategy attackStrategy) {

        this.stats = Objects.requireNonNull(stats);
        this.attackStrategy = Objects.requireNonNull(attackStrategy);
        this.currentCooldown = 0.0;
    }

    public void update(double deltaTimeSeconds) {

        if (!Double.isFinite(deltaTimeSeconds) || deltaTimeSeconds < 0) {
            throw new IllegalArgumentException(
                    "Delta time must be finite and >= 0"
            );
        }

        currentCooldown = Math.max(
                0.0,
                currentCooldown - deltaTimeSeconds
        );
    }

    public boolean canAttack() {
        return currentCooldown <= 0.0;
    }

    public Optional<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets) {

        Objects.requireNonNull(playerPosition);
        Objects.requireNonNull(targets);

        if (!canAttack()) {
            return Optional.empty();
        }

        Optional<ProjectileSpawnRequest> request =
                attackStrategy.attack(
                        playerPosition,
                        targets,
                        stats
                );

        if (request.isPresent()) {
            currentCooldown = stats.getCooldownSeconds();
        }

        return request;
    }

    public double getCooldown() {
        return currentCooldown;
    }

    public WeaponStats getCurrentStats() {
        return stats;
    }

    public void upgrade(WeaponUpgrade upgrade) {
        Objects.requireNonNull(upgrade);
        stats = upgrade.apply(stats);
    }
}