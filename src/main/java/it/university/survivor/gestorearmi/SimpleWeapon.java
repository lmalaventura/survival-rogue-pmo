import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public class SimpleWeapon {
    
    private final double cooldownSeconds;
    private final double damage;
    private final double projectileSpeed;
    
    private double currentCooldown;

    public SimpleWeapon(double cooldownSeconds, double damage, double projectileSpeed) {
        this.cooldownSeconds = cooldownSeconds;
        this.damage = damage;
        this.projectileSpeed = projectileSpeed;
        this.currentCooldown = 0.0; // appena creata è pronta a sparare
    }

    
    public Optional<ProjectileSpawnRequest> update(double deltaTimeSeconds, Position playerPosition, Collection<? extends Targetable> targets) {
        // 1. Timer update
        if (currentCooldown > 0) {
            currentCooldown -= deltaTimeSeconds;
        }

        // 2. Verifica se l'arma è pronta a sparare
        if (currentCooldown > 0) {
            return Optional.empty();
        }

        // 3. Trova il bersaglio più vicino (Logica di Targeting)
        Optional<? extends Targetable> nearestTarget = findNearestTarget(playerPosition, targets);

        // 4. Se c'è un bersaglio, genera il proiettile e resetta il cooldown
        if (nearestTarget.isPresent()) {
            currentCooldown = cooldownSeconds;
            
            ProjectileSpawnRequest request = new ProjectileSpawnRequest(
                playerPosition,
                nearestTarget.get().getPosition(),
                damage,
                projectileSpeed
            );
            
            return Optional.of(request);
        }

        // Nessun bersaglio valido, non spara e non resetta il cooldown
        return Optional.empty();
    }

    private Optional<? extends Targetable> findNearestTarget(Position origin, Collection<? extends Targetable> targets) {
        if (targets == null || targets.isEmpty()) {
            return Optional.empty();
        }

        return targets.stream()
            .min(Comparator.comparingDouble(t -> origin.distanceTo(t.getPosition())));
    }
}