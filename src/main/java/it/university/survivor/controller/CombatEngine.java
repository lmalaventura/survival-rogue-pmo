package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.Projectile;
import it.university.survivor.model.ProjectileOwner;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.weapon.ProjectileSpawnRequest;
import it.university.survivor.weapon.Weapon;
import it.university.survivor.weapon.WeaponType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

final class CombatEngine {

    private static final int ENEMY_CONTACT_DAMAGE = 10;
    private static final int RANGED_PROJECTILE_DAMAGE = 8;
    private static final double RANGED_PROJECTILE_SPEED = 220.0;
    private static final double PLAYER_HIT_INVULNERABILITY_SECONDS = 0.5;

    private final GameWorld world;
    private final ExperienceProgression experienceProgression;
    private final RunStatistics runStatistics;
    private final Map<WeaponType, Weapon> weapons;
    private double playerHitInvulnerabilityRemaining;

    CombatEngine(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Map<WeaponType, Weapon> weapons
    ) {
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.experienceProgression = Objects.requireNonNull(
                experienceProgression,
                "Experience progression must not be null"
        );
        this.runStatistics = Objects.requireNonNull(
                runStatistics,
                "Run statistics must not be null"
        );
        this.weapons = Objects.requireNonNull(weapons, "Weapons must not be null");
    }

    void updatePlayerInvulnerability(double deltaSeconds) {
        double comparisonTolerance = Math.ulp(PLAYER_HIT_INVULNERABILITY_SECONDS);
        if (playerHitInvulnerabilityRemaining <= deltaSeconds + comparisonTolerance) {
            playerHitInvulnerabilityRemaining = 0.0;
        } else {
            playerHitInvulnerabilityRemaining -= deltaSeconds;
        }
    }

    void updateRangedAttacks(double deltaSeconds) {
        Position playerPosition = world.getPlayer().getPosition();

        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead() || enemy.getType() != EnemyType.RANGED) {
                continue;
            }

            enemy.updateRangedCooldown(deltaSeconds);
            if (!enemy.canRequestRangedAttack(playerPosition)) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double deltaX = playerPosition.x() - enemyPosition.x();
            double deltaY = playerPosition.y() - enemyPosition.y();
            double distance = Math.hypot(deltaX, deltaY);
            world.addProjectile(new Projectile(
                    enemyPosition,
                    deltaX / distance,
                    deltaY / distance,
                    RANGED_PROJECTILE_DAMAGE,
                    RANGED_PROJECTILE_SPEED,
                    ProjectileOwner.ENEMY
            ));
            enemy.requestRangedAttack();
        }
    }

    void updateWeapons(double deltaSeconds) {
        Position playerPosition = world.getPlayer().getPosition();
        for (Weapon weapon : weapons.values()) {
            weapon.update(deltaSeconds);
            for (ProjectileSpawnRequest request : weapon.attack(
                    playerPosition,
                    world.getEnemies()
            )) {
                world.addProjectile(createProjectile(request));
            }
        }
    }

    void updateProjectiles(double deltaSeconds) {
        for (Projectile projectile : List.copyOf(world.getProjectiles())) {
            double distance = projectile.getMovementSpeed() * deltaSeconds;
            world.moveProjectileBy(
                    projectile,
                    projectile.getDirectionX() * distance,
                    projectile.getDirectionY() * distance
            );

            if (isOutsideWorld(projectile.getPosition())) {
                world.removeProjectile(projectile);
                continue;
            }

            if (projectile.getOwner() == ProjectileOwner.PLAYER) {
                applyPlayerProjectileCollision(projectile);
            } else if (isCollidingWithPlayer(projectile)) {
                damagePlayerIfVulnerable(projectile.getDamage());
                world.removeProjectile(projectile);
            }
        }
    }

    void applyEnemyContactDamage() {
        Player player = world.getPlayer();
        if (player.getHealth().isDead() || playerHitInvulnerabilityRemaining > 0.0) {
            return;
        }

        Position playerPosition = player.getPosition();
        int enemiesInContact = 0;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }
            if (CollisionRules.isWithinPlayerContact(
                    distance(enemy.getPosition(), playerPosition),
                    enemy
            )) {
                enemiesInContact++;
            }
        }

        if (enemiesInContact > 0) {
            damagePlayerIfVulnerable(ENEMY_CONTACT_DAMAGE * enemiesInContact);
        }
    }

    private void damagePlayerIfVulnerable(int damage) {
        Player player = world.getPlayer();
        if (player.getHealth().isDead() || playerHitInvulnerabilityRemaining > 0.0) {
            return;
        }
        player.getHealth().takeDamage(damage);
        playerHitInvulnerabilityRemaining = PLAYER_HIT_INVULNERABILITY_SECONDS;
    }

    private void applyPlayerProjectileCollision(Projectile projectile) {
        Enemy hitEnemy = findFirstCollidingEnemy(projectile);
        if (hitEnemy == null) {
            return;
        }

        boolean wasAlive = !hitEnemy.isDead();
        hitEnemy.takeDamage(projectile.getDamage());
        if (wasAlive && hitEnemy.isDead()) {
            int experienceReward = hitEnemy.getType().experienceReward();
            experienceProgression.addExperience(experienceReward);
            runStatistics.recordEnemyDefeated();
            runStatistics.recordExperienceGained(experienceReward);
        }
        world.removeProjectile(projectile);
    }

    private boolean isCollidingWithPlayer(Projectile projectile) {
        double distance = distance(projectile.getPosition(), world.getPlayer().getPosition());
        return distance <= CollisionRules.PLAYER_RADIUS
                + CollisionRules.PROJECTILE_RADIUS
                + CollisionRules.TOLERANCE;
    }

    private Enemy findFirstCollidingEnemy(Projectile projectile) {
        for (Enemy enemy : world.getEnemies()) {
            if (!enemy.isDead()
                    && distance(projectile.getPosition(), enemy.getPosition())
                    <= CollisionRules.projectileEnemyDistance(enemy)
                    + CollisionRules.TOLERANCE) {
                return enemy;
            }
        }
        return null;
    }

    private boolean isOutsideWorld(Position position) {
        return position.x() < 0.0 || position.x() > world.getWidth()
                || position.y() < 0.0 || position.y() > world.getHeight();
    }

    private static Projectile createProjectile(ProjectileSpawnRequest request) {
        return new Projectile(
                request.origin(),
                request.directionX(),
                request.directionY(),
                request.damage(),
                request.speed()
        );
    }

    private static double distance(Position first, Position second) {
        return Math.hypot(first.x() - second.x(), first.y() - second.y());
    }
}
