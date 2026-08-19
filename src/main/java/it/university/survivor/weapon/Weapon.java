package it.university.survivor.weapon;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public class Weapon {

    private WeaponStats stats;
    private AttackStrategy attackStrategy;
    private final AttackStrategy evolvedAttackStrategy;
    private final int maxLevel;

    private int level;
    private double currentCooldown;
    public static final int DEFAULT_MAX_LEVEL = 5;
public Weapon(
        WeaponStats stats,
        AttackStrategy attackStrategy) {

    this.stats=stats;
    this.attackStrategy = attackStrategy;
    this.evolvedAttackStrategy = attackStrategy;
    this.maxLevel = DEFAULT_MAX_LEVEL;
}
    public Weapon(
        WeaponStats stats,
        AttackStrategy attackStrategy,
        AttackStrategy evolvedAttackStrategy) {

    this.stats=stats;
    this.attackStrategy = attackStrategy;
    this.evolvedAttackStrategy = evolvedAttackStrategy;
    this.maxLevel = DEFAULT_MAX_LEVEL;
}
public Weapon(
        WeaponStats stats,
        AttackStrategy attackStrategy,
        AttackStrategy evolvedAttackStrategy,
        int maxLevel) {

    this.stats = Objects.requireNonNull(stats);
    this.attackStrategy = Objects.requireNonNull(attackStrategy);
    this.evolvedAttackStrategy =
            Objects.requireNonNull(evolvedAttackStrategy);

    if (maxLevel < 2) {
        throw new IllegalArgumentException(
                "Il livello massimo deve essere almeno 2."
        );
    }

    this.maxLevel = maxLevel;
    this.level = 1;
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

    List<ProjectileSpawnRequest> requests =
            attackAll(playerPosition, targets);

    return requests.isEmpty()
            ? Optional.empty()
            : Optional.of(requests.get(0));
}
public List<ProjectileSpawnRequest> attackAll(
        Position playerPosition,
        Collection<? extends Enemy> targets) {

    Objects.requireNonNull(playerPosition);
    Objects.requireNonNull(targets);

    if (!canAttack()) {
        return List.of();
    }

    List<ProjectileSpawnRequest> requests =
            attackStrategy.attackMultiple(
                    playerPosition,
                    targets,
                    stats
            );

    if (!requests.isEmpty()) {
        currentCooldown =
                stats.getCooldownSeconds();
    }

    return List.copyOf(requests);
}

    public double getCooldown() {
        return currentCooldown;
    }

    public WeaponStats getCurrentStats() {
        return stats;
    }

    public void upgrade(WeaponUpgrade upgrade) {

    Objects.requireNonNull(upgrade);

    stats = Objects.requireNonNull(
            upgrade.apply(stats)
    );
}
    public int getLevel() {
    return level;
    }

    public int getMaxLevel() {
    return maxLevel;
    }

    public boolean isEvolved() {
    return level >= maxLevel;
    }
    public void levelUp() {

    if (level >= maxLevel) {
        return;
    }

    level++;

    if (level == maxLevel) {
        attackStrategy = evolvedAttackStrategy;
    }
}
}