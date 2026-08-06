public record ProjectileSpawnRequest(
    Position origin, 
    Position targetPosition, 
    double damage, 
    double speed
) {}