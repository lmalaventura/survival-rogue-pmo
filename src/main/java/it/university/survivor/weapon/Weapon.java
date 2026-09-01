package it.university.survivor.weapon;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class Weapon {

    public static final int DEFAULT_MAX_LEVEL = 5;

    private WeaponStats stats;
    private AttackStrategy attackStrategy;
    private final AttackStrategy evolvedAttackStrategy;
    private final WeaponUpgrade evolutionUpgrade;
    private final int maxLevel;
    private int level = 1;
    private double cooldownRemaining;

    public Weapon(WeaponStats stats, AttackStrategy attackStrategy) {
        this(stats, attackStrategy, attackStrategy, DEFAULT_MAX_LEVEL, null);
    }

    public Weapon(
            WeaponStats stats,
            AttackStrategy attackStrategy,
            AttackStrategy evolvedAttackStrategy,
            int maxLevel
    ) {
        this(stats, attackStrategy, evolvedAttackStrategy, maxLevel, null);
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
            throw new IllegalArgumentException("Maximum level must be at least two");
        }
        this.maxLevel = maxLevel;
        this.evolutionUpgrade = evolutionUpgrade;
    }

    public void update(double deltaSeconds) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0.0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        cooldownRemaining = Math.max(0.0, cooldownRemaining - deltaSeconds);
    }

    public boolean canAttack() {
        return cooldownRemaining == 0.0;
    }

    public List<ProjectileSpawnRequest> attack(
            Position playerPosition,
            Collection<? extends Enemy> targets
    ) {
        Objects.requireNonNull(playerPosition, "Player position must not be null");
        Objects.requireNonNull(targets, "Targets must not be null");
        if (!canAttack()) {
            return List.of();
        }

        List<ProjectileSpawnRequest> requests = List.copyOf(
                attackStrategy.attack(playerPosition, targets, stats)
        );
        if (!requests.isEmpty()) {
            cooldownRemaining = stats.getCooldownSeconds();
        }
        return requests;
    }

    public double getCooldownRemaining() {
        return cooldownRemaining;
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
        return level == maxLevel;
    }

    public void levelUp() {
        if (isEvolved()) {
            return;
        }
        level++;
        if (isEvolved()) {
            attackStrategy = evolvedAttackStrategy;
            if (evolutionUpgrade != null) {
                upgrade(evolutionUpgrade);
            }
        }
    }
}
