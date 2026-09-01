package it.university.survivor.model;

import java.util.Objects;

public final class Projectile {

    private Position position;
    private final double directionX;
    private final double directionY;
    private final int damage;
    private final double movementSpeed;
    private final ProjectileOwner owner;

    public Projectile(
            Position position,
            double directionX,
            double directionY,
            int damage,
            double movementSpeed
    ) {
        this(
                position,
                directionX,
                directionY,
                damage,
                movementSpeed,
                ProjectileOwner.PLAYER
        );
    }

    public Projectile(
            Position position,
            double directionX,
            double directionY,
            int damage,
            double movementSpeed,
            ProjectileOwner owner
    ) {
        Position validatedPosition = Objects.requireNonNull(
                position,
                "Position must not be null"
        );
        ProjectileOwner validatedOwner = Objects.requireNonNull(
                owner,
                "Owner must not be null"
        );
        if (!Double.isFinite(validatedPosition.x())
                || !Double.isFinite(validatedPosition.y())) {
            throw new IllegalArgumentException("Position coordinates must be finite");
        }
        if (!Double.isFinite(directionX) || !Double.isFinite(directionY)) {
            throw new IllegalArgumentException("Direction must be finite");
        }

        double directionScale = Math.max(Math.abs(directionX), Math.abs(directionY));
        if (directionScale == 0.0) {
            throw new IllegalArgumentException("Direction must not be the zero vector");
        }
        if (damage <= 0) {
            throw new IllegalArgumentException("Damage must be greater than zero");
        }
        if (!Double.isFinite(movementSpeed) || movementSpeed <= 0.0) {
            throw new IllegalArgumentException(
                    "Movement speed must be finite and greater than zero"
            );
        }

        double scaledDirectionX = directionX / directionScale;
        double scaledDirectionY = directionY / directionScale;
        double directionMagnitude = Math.hypot(scaledDirectionX, scaledDirectionY);

        this.position = validatedPosition;
        this.directionX = scaledDirectionX / directionMagnitude;
        this.directionY = scaledDirectionY / directionMagnitude;
        this.damage = damage;
        this.movementSpeed = movementSpeed;
        this.owner = validatedOwner;
    }

    public Position getPosition() {
        return position;
    }

    public double getDirectionX() {
        return directionX;
    }

    public double getDirectionY() {
        return directionY;
    }

    public int getDamage() {
        return damage;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public ProjectileOwner getOwner() {
        return owner;
    }

    void moveTo(Position newPosition) {
        position = Objects.requireNonNull(newPosition, "Position must not be null");
    }
}
