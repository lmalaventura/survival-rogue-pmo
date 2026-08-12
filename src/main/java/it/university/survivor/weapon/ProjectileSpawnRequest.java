package it.university.survivor.weapon;

import java.util.Objects;

import it.university.survivor.model.Position;

public record ProjectileSpawnRequest(
    Position origin, 
    double directionX, 
    double directionY, 
    int damage, 
    double speed
) {    public ProjectileSpawnRequest {
        Objects.requireNonNull(origin, "Origin must not be null");

        if (!Double.isFinite(directionX) || !Double.isFinite(directionY)) {
            throw new IllegalArgumentException("Direction must be finite");
        }

        if (damage <= 0) {
            throw new IllegalArgumentException("Damage must be greater than zero");
        }

        if (!Double.isFinite(speed) || speed <= 0.0) {
            throw new IllegalArgumentException("Speed must be finite and greater than zero");
        }
    }
}