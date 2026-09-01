package it.university.survivor.weapon;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

public class Weapon {

    public static final int DEFAULT_MAX_LEVEL = 5;

    private WeaponStats stats;
    private AttackStrategy attackStrategy;
    private final AttackStrategy evolvedAttackStrategy;
    private final WeaponUpgrade evolutionUpgrade;
    private final int maxLevel;

    private int level;
    private double currentCooldown;

    public Weapon(
            WeaponStats stats,
            AttackStrategy attackStrategy
    ) {
        this(
                stats,
                attackStrategy,
                attackStrategy,
                DEFAULT_MAX_LEVEL,
                null
        );
    }

    public Weapon(
            WeaponStats stats,
            AttackStrategy attackStrategy,
            AttackStrategy evolvedAttackStrategy
    ) {
        this(
                stats,
                attackStrategy,
                evolvedAttackStrategy,
                DEFAULT_MAX_LEVEL,
                null
        );
    }

    public Weapon(
            WeaponStats stats,
            AttackStrategy attackStrategy,
            AttackStrategy evolvedAttackStrategy,
            int maxLevel
    ) {
        this(
                stats,
                attackStrategy,
                evolvedAttackStrategy,
                maxLevel,
                null
        );
    }

    public Weapon(
            WeaponStats stats,
            AttackStrategy attackStrategy,
            AttackStrategy evolvedAttackStrategy,
            int maxLevel,
            WeaponUpgrade evolutionUpgrade
    ) {
        this.stats = Objects.requireNonNull(stats, "Stats must not be null");
        this.attackStrategy = Objects.requireNonNull(
                attackStrategy,
                "Attack strategy must not be null"
        );
        this.evolvedAttackStrategy = Objects.requireNonNull(
                evolvedAttackStrategy,
                "Evolved attack strategy must not be null"
        );

        if (maxLevel < 2) {
            throw new IllegalArgumentException(
                    "Maximum level must be at least two"
            );
        }

        this.maxLevel = maxLevel;
        this.evolutionUpgrade = evolutionUpgrade;
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
            Collection<? extends Enemy> targets
    ) {
        List<ProjectileSpawnRequest> requests = attackAll(
                playerPosition,
                targets
        );

        return requests.isEmpty()
                ? Optional.empty()
                : Optional.of(requests.get(0));
    }

    public List<ProjectileSpawnRequest> attackAll(
            Position playerPosition,
            Collection<? extends Enemy> targets
    ) {
        Objects.requireNonNull(playerPosition, "Player position must not be null");
        Objects.requireNonNull(targets, "Targets must not be null");

        if (!canAttack()) {
            return List.of();
        }

        List<ProjectileSpawnRequest> requests = attackStrategy.attackMultiple(
                playerPosition,
                targets,
                stats
        );

        if (!requests.isEmpty()) {
            currentCooldown = stats.getCooldownSeconds();
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
        Objects.requireNonNull(upgrade, "Upgrade must not be null");
        stats = Objects.requireNonNull(
                upgrade.apply(stats),
                "Upgrade result must not be null"
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
            if (evolutionUpgrade != null) {
                upgrade(evolutionUpgrade);
            }
        }
    }
}