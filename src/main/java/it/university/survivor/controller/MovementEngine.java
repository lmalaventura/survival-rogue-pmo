package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.enemy.Wave;

import java.util.EnumSet;
import java.util.Objects;

final class MovementEngine {

    private static final double PLAYER_SUBSTEP_DISTANCE = 1.0;

    private final GameWorld world;
    private final EnumSet<MovementDirection> activeDirections =
            EnumSet.noneOf(MovementDirection.class);

    MovementEngine(GameWorld world) {
        this.world = Objects.requireNonNull(world, "World must not be null");
    }

    void setDirectionActive(MovementDirection direction, boolean active) {
        Objects.requireNonNull(direction, "Direction must not be null");
        if (active) {
            activeDirections.add(direction);
        } else {
            activeDirections.remove(direction);
        }
    }

    void updatePlayer(double deltaSeconds) {
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
        double distance = player.getMovementSpeed() * deltaSeconds;
        if (distance == 0.0) {
            return;
        }

        int substepCount = Math.max(1, (int) Math.ceil(distance / PLAYER_SUBSTEP_DISTANCE));
        double substepX = normalizedX * distance / substepCount;
        double substepY = normalizedY * distance / substepCount;

        for (int substep = 0; substep < substepCount; substep++) {
            Position currentPosition = player.getPosition();
            Position candidatePosition = movementCandidate(currentPosition, substepX, substepY);

            if (candidatePosition.equals(currentPosition)
                    || !preservesPlayerEnemyDistance(currentPosition, candidatePosition)) {
                break;
            }
            world.movePlayerBy(substepX, substepY);
        }
    }

    void updateEnemies(double deltaSeconds, Wave currentWave, EliteEncounter encounter) {
        Objects.requireNonNull(encounter, "Elite encounter must not be null");
        Position playerPosition = world.getPlayer().getPosition();

        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double deltaToPlayerX = playerPosition.x() - enemyPosition.x();
            double deltaToPlayerY = playerPosition.y() - enemyPosition.y();
            double distanceToPlayer = Math.hypot(deltaToPlayerX, deltaToPlayerY);
            double contactDistance = CollisionRules.playerEnemyDistance(enemy);
            double maximumMovement = distanceToPlayer - contactDistance;

            Position direction = enemy.calculateDesiredDirection(playerPosition);
            double directionTowardPlayer = direction.x() * deltaToPlayerX
                    + direction.y() * deltaToPlayerY;
            if (directionTowardPlayer > 0.0
                    && CollisionRules.isWithinPlayerContact(distanceToPlayer, enemy)) {
                continue;
            }

            double desiredMovement = enemy.getMovementSpeed()
                    * encounter.movementSpeedMultiplier(currentWave, enemy)
                    * deltaSeconds;
            double actualMovement = directionTowardPlayer > 0.0
                    ? Math.min(desiredMovement, Math.max(0.0, maximumMovement))
                    : desiredMovement;
            double movementX = direction.x() * actualMovement;
            double movementY = direction.y() * actualMovement;
            Position candidatePosition = movementCandidate(enemyPosition, movementX, movementY);

            if (preservesEnemySeparation(enemy, enemyPosition, candidatePosition)) {
                world.moveEnemyBy(enemy, movementX, movementY);
            }
        }
    }

    private boolean preservesPlayerEnemyDistance(
            Position currentPosition,
            Position candidatePosition
    ) {
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double currentDistance = distance(currentPosition, enemyPosition);
            double candidateDistance = distance(candidatePosition, enemyPosition);
            double minimumAllowedDistance = Math.min(
                    CollisionRules.playerEnemyDistance(enemy),
                    currentDistance
            );

            if (candidateDistance < minimumAllowedDistance) {
                return false;
            }
        }
        return true;
    }

    private boolean preservesEnemySeparation(
            Enemy movingEnemy,
            Position currentPosition,
            Position candidatePosition
    ) {
        for (Enemy otherEnemy : world.getEnemies()) {
            if (otherEnemy == movingEnemy || otherEnemy.isDead()) {
                continue;
            }

            Position otherPosition = otherEnemy.getPosition();
            double currentDistance = distance(currentPosition, otherPosition);
            double candidateDistance = distance(candidatePosition, otherPosition);
            double minimumAllowedDistance = Math.min(
                    CollisionRules.enemySeparationDistance(movingEnemy, otherEnemy),
                    currentDistance
            );

            if (candidateDistance < minimumAllowedDistance) {
                return false;
            }
        }
        return true;
    }

    private Position movementCandidate(
            Position currentPosition,
            double movementX,
            double movementY
    ) {
        double candidateX = Math.max(
                0.0,
                Math.min(world.getWidth(), currentPosition.x() + movementX)
        );
        double candidateY = Math.max(
                0.0,
                Math.min(world.getHeight(), currentPosition.y() + movementY)
        );
        return new Position(candidateX, candidateY);
    }

    private static double distance(Position first, Position second) {
        return Math.hypot(first.x() - second.x(), first.y() - second.y());
    }
}
