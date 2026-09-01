package it.university.survivor.model;

import java.util.Objects;

import it.university.survivor.model.enemy.EnemyType;

public class Enemy {

    private static final double RANGED_PREFERRED_DISTANCE = 250.0;
    private static final double RANGED_DISTANCE_TOLERANCE = 50.0;
    private static final double RANGED_ATTACK_COOLDOWN = 1.0;
    private static final double RANGED_COOLDOWN_TOLERANCE = 1.0e-9;

    private Position position;
    private final Health health;
    private final double movementSpeed;
    private final EnemyType type;
    private double rangedAttackCooldown;

    public Enemy(
            Position position,
            int maxHealth,
            double movementSpeed
    ) {
        this(
                position,
                maxHealth,
                movementSpeed,
                EnemyType.BASIC
        );
    }

    public Enemy(
            Position position,
            int maxHealth,
            double movementSpeed,
            EnemyType type
    ) {
        this.position = Objects.requireNonNull(
                position,
                "Position must not be null"
        );

        if (!Double.isFinite(movementSpeed) || movementSpeed <= 0.0) {
            throw new IllegalArgumentException(
                    "Movement speed must be finite and greater than zero"
            );
        }

        this.type = Objects.requireNonNull(
                type,
                "Enemy type must not be null"
        );

        this.health = new Health(maxHealth);
        this.movementSpeed = movementSpeed;
        this.rangedAttackCooldown = 0.0;
    }

    void moveTo(Position newPosition) {
        this.position = Objects.requireNonNull(
                newPosition,
                "Position must not be null"
        );
    }

    public Position getPosition() {
        return position;
    }

    public Health getHealth() {
        return health;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public EnemyType getType() {
        return type;
    }

    public Position calculateDesiredDirection(Position targetPosition) {
        Objects.requireNonNull(
                targetPosition,
                "Target position must not be null"
        );

        double dx = targetPosition.x() - position.x();
        double dy = targetPosition.y() - position.y();
        double distance = Math.hypot(dx, dy);

        if (distance == 0.0) {
            return new Position(0.0, 0.0);
        }

        if (type == EnemyType.RANGED) {
            if (distance < RANGED_PREFERRED_DISTANCE
                    - RANGED_DISTANCE_TOLERANCE) {
                return new Position(
                        -dx / distance,
                        -dy / distance
                );
            }

            if (distance <= RANGED_PREFERRED_DISTANCE
                    + RANGED_DISTANCE_TOLERANCE) {
                return new Position(0.0, 0.0);
            }

            return new Position(
                    dx / distance,
                    dy / distance
            );
        }

        return new Position(
                dx / distance,
                dy / distance
        );
    }

    public boolean canRequestRangedAttack() {
        return type == EnemyType.RANGED
                && rangedAttackCooldown <= RANGED_COOLDOWN_TOLERANCE;
    }

    public boolean canRequestRangedAttack(Position targetPosition) {
        Objects.requireNonNull(
                targetPosition,
                "Target position must not be null"
        );

        if (!canRequestRangedAttack()) {
            return false;
        }

        double distance = Math.hypot(
                targetPosition.x() - position.x(),
                targetPosition.y() - position.y()
        );

        return distance >= RANGED_PREFERRED_DISTANCE
                - RANGED_DISTANCE_TOLERANCE
                && distance <= RANGED_PREFERRED_DISTANCE
                + RANGED_DISTANCE_TOLERANCE;
    }

    public void requestRangedAttack() {
        if (canRequestRangedAttack()) {
            rangedAttackCooldown = RANGED_ATTACK_COOLDOWN;
        }
    }

    public void updateRangedCooldown(double deltaTime) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0.0) {
            throw new IllegalArgumentException(
                    "Delta time must be finite and non-negative"
            );
        }

        rangedAttackCooldown = Math.max(
                0.0,
                rangedAttackCooldown - deltaTime
        );
    }

    public void takeDamage(int damage) {
        health.takeDamage(damage);
    }

    public boolean isDead() {
        return health.isDead();
    }
}
