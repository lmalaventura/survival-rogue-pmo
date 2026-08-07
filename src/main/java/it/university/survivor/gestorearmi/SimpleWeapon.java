package it.university.survivor.gestorearmi;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import it.university.survivor.model.Position;

public class SimpleWeapon {
    
    private final double cooldownSeconds;
    private final int damage;
    private final double projectileSpeed;
    
    private double currentCooldown;

    public SimpleWeapon(double cooldownSeconds, int damage, double projectileSpeed) {
        if (!Double.isFinite(cooldownSeconds) || cooldownSeconds <= 0) {
            throw new IllegalArgumentException("Il cooldown deve essere finito e maggiore di zero.");
        }
        if (damage <= 0) {
            throw new IllegalArgumentException("Il danno deve essere maggiore di zero.");
        }
        if (!Double.isFinite(projectileSpeed) || projectileSpeed <= 0) {
            throw new IllegalArgumentException("La velocità del proiettile deve essere finita e maggiore di zero.");
        }
        
        this.cooldownSeconds = cooldownSeconds;
        this.damage = damage;
        this.projectileSpeed = projectileSpeed;
        this.currentCooldown = 0.0;
    }

    public Optional<ProjectileSpawnRequest> update(double deltaTimeSeconds, Position playerPosition, Collection<? extends Targetable> targets) {
        // 1. Validazione input ad ogni frame
        if (!Double.isFinite(deltaTimeSeconds) || deltaTimeSeconds < 0) {
            throw new IllegalArgumentException("Il delta time deve essere finito e >= 0.");
        }
        Objects.requireNonNull(playerPosition, "La posizione del giocatore non può essere null.");
        Objects.requireNonNull(targets, "La lista dei bersagli non può essere null.");

        // 2. Aggiornamento timer
        if (currentCooldown > 0) {
            currentCooldown -= deltaTimeSeconds;
        }

        // 3. Ritorno anticipato se in cooldown
        if (currentCooldown > 0) {
            return Optional.empty();
        }

        // 4. Selezione bersaglio più vicino
        Targetable nearestTarget = null;
        double minDistance = Double.MAX_VALUE;

        for (Targetable target : targets) {
            // Ignoriamo target nulli passati per errore
            if (target == null || target.getPosition() == null) {
                continue;
            }
            
            double dist = getDistance(playerPosition, target.getPosition());
            if (dist < minDistance) {
                minDistance = dist;
                nearestTarget = target;
            }
        }

        // 5. Generazione richiesta se abbiamo un bersaglio valido
        if (nearestTarget != null) {
            currentCooldown = cooldownSeconds;
            
            // Calcolo direzione normalizzata
            double dx = nearestTarget.getPosition().x() - playerPosition.x();
            double dy = nearestTarget.getPosition().y() - playerPosition.y();
            
            double dirX = 0.0;
            double dirY = 0.0;
            
            // Gestione del caso limite: target sovrapposto al player (distanza 0)
            if (minDistance < 1e-6) {
                dirX = 1.0; // Direzione arbitraria di default (es. a destra)
            } else {
                dirX = dx / minDistance;
                dirY = dy / minDistance;
            }
            
            return Optional.of(new ProjectileSpawnRequest(
                playerPosition, dirX, dirY, damage, projectileSpeed
            ));
        }

        return Optional.empty();
    }

    private double getDistance(Position p1, Position p2) {
        return Math.hypot(p1.x() - p2.x(), p1.y() - p2.y());
    }
}