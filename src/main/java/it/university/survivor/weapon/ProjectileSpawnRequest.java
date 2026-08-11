package it.university.survivor.weapon;

import it.university.survivor.model.Position;

public record ProjectileSpawnRequest(
    Position origin, 
    double directionX, 
    double directionY, 
    int damage, 
    double speed
) {}