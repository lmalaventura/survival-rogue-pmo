package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.Projectile;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public final class GameController {

    private static final double MAX_DELTA_SECONDS = 0.1;
    private static final double ENEMY_CONTACT_DISTANCE = 14.0;
    private static final double PROJECTILE_ENEMY_COLLISION_DISTANCE = 9.0;
    private static final double CONTACT_DISTANCE_TOLERANCE = 1.0e-9;
    private static final int ENEMY_CONTACT_DAMAGE = 10;
    private static final double PLAYER_HIT_INVULNERABILITY_SECONDS = 0.5;

    private final GameWorld world;
    private final EnumSet<MovementDirection> activeDirections =
            EnumSet.noneOf(MovementDirection.class);
    private double playerHitInvulnerabilityRemaining = 0.0;

    public GameController(GameWorld world) {
        this.world = Objects.requireNonNull(world, "World must not be null");
    }

    public void setDirectionActive(MovementDirection direction, boolean active) {
        Objects.requireNonNull(direction, "Direction must not be null");

        if (active) {
            activeDirections.add(direction);
        } else {
            activeDirections.remove(direction);
        }
    }

    public void update(double deltaSeconds) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0.0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        if (deltaSeconds == 0.0) {
            return;
        }

        double effectiveDelta = Math.min(deltaSeconds, MAX_DELTA_SECONDS);
        updatePlayerHitInvulnerability(effectiveDelta);
        updatePlayerMovement(effectiveDelta);
        updateEnemyMovement(effectiveDelta);
        updateProjectileMovementAndCollisions(effectiveDelta);
        applyEnemyContactDamage();
    }

    private void updatePlayerHitInvulnerability(double effectiveDelta) {
        double comparisonTolerance = Math.ulp(PLAYER_HIT_INVULNERABILITY_SECONDS);
        if (playerHitInvulnerabilityRemaining <= effectiveDelta + comparisonTolerance) {
            playerHitInvulnerabilityRemaining = 0.0;
        } else {
            playerHitInvulnerabilityRemaining -= effectiveDelta;
        }
    }

    private void updatePlayerMovement(double effectiveDelta) {
        double directionX = (activeDirections.contains(MovementDirection.RIGHT) ? 1.0 : 0.0)
                - (activeDirections.contains(MovementDirection.LEFT) ? 1.0 : 0.0);
        double directionY = (activeDirections.contains(MovementDirection.DOWN) ? 1.0 : 0.0)
                - (activeDirections.contains(MovementDirection.UP) ? 1.0 : 0.0);

        if (directionX == 0.0 && directionY == 0.0) {
            return;
        }

        double magnitude = Math.hypot(directionX, directionY);
        double normalizedX = directionX / magnitude;
        double normalizedY = directionY / magnitude;

        Player player = world.getPlayer();
        double distance = player.getMovementSpeed() * effectiveDelta;
        world.movePlayerBy(normalizedX * distance, normalizedY * distance);
    }

    private void updateEnemyMovement(double effectiveDelta) {
        Position playerPosition = world.getPlayer().getPosition();

        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double deltaToPlayerX = playerPosition.x() - enemyPosition.x();
            double deltaToPlayerY = playerPosition.y() - enemyPosition.y();
            double distanceToPlayer = Math.hypot(deltaToPlayerX, deltaToPlayerY);
            double maximumMovement = distanceToPlayer - ENEMY_CONTACT_DISTANCE;
            if (isWithinContactDistance(distanceToPlayer)) {
                continue;
            }

            Position direction = enemy.calculateDesiredDirection(playerPosition);
            double desiredMovement = enemy.getMovementSpeed() * effectiveDelta;
            double actualMovement = Math.min(desiredMovement, maximumMovement);
            world.moveEnemyBy(
                    enemy,
                    direction.x() * actualMovement,
                    direction.y() * actualMovement
            );
        }
    }

    private void applyEnemyContactDamage() {
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

            Position enemyPosition = enemy.getPosition();
            double deltaX = enemyPosition.x() - playerPosition.x();
            double deltaY = enemyPosition.y() - playerPosition.y();
            double distance = Math.hypot(deltaX, deltaY);

            if (isWithinContactDistance(distance)) {
                enemiesInContact++;
            }
        }

        if (enemiesInContact == 0) {
            return;
        }

        int damage = ENEMY_CONTACT_DAMAGE * enemiesInContact;
        player.getHealth().takeDamage(damage);
        playerHitInvulnerabilityRemaining = PLAYER_HIT_INVULNERABILITY_SECONDS;
    }

    private void updateProjectileMovementAndCollisions(double effectiveDelta) {
        for (Projectile projectile : List.copyOf(world.getProjectiles())) {
            double distance = projectile.getMovementSpeed() * effectiveDelta;
            world.moveProjectileBy(
                    projectile,
                    projectile.getDirectionX() * distance,
                    projectile.getDirectionY() * distance
            );

            if (isOutsideWorld(projectile.getPosition())) {
                world.removeProjectile(projectile);
                continue;
            }

            Enemy hitEnemy = findFirstCollidingEnemy(projectile);
            if (hitEnemy != null) {
                hitEnemy.takeDamage(projectile.getDamage());
                world.removeProjectile(projectile);
            }
        }
    }

    private Enemy findFirstCollidingEnemy(Projectile projectile) {
        Position projectilePosition = projectile.getPosition();
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double deltaX = enemyPosition.x() - projectilePosition.x();
            double deltaY = enemyPosition.y() - projectilePosition.y();
            double distance = Math.hypot(deltaX, deltaY);
            if (distance <= PROJECTILE_ENEMY_COLLISION_DISTANCE
                    + CONTACT_DISTANCE_TOLERANCE) {
                return enemy;
            }
        }

        return null;
    }

    private boolean isOutsideWorld(Position position) {
        return position.x() < 0.0 || position.x() > world.getWidth()
                || position.y() < 0.0 || position.y() > world.getHeight();
    }

    private static boolean isWithinContactDistance(double distance) {
        return distance <= ENEMY_CONTACT_DISTANCE + CONTACT_DISTANCE_TOLERANCE;
    }
}
