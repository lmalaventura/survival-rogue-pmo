package it.university.survivor.weapon;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.university.survivor.model.Position;

class SimpleWeaponTest {

    private SimpleWeapon weapon;
    private Position playerPos;

    // Record dummy per simulare i nemici nei test
    private record DummyEnemy(Position getPosition) implements Targetable {}

    @BeforeEach
    void setUp() {
        // Cooldown: 1.0s, Danno: 10, Velocità: 5.0
        weapon = new SimpleWeapon(1.0, 10, 5.0);
        playerPos = new Position(0, 0); // Per i test assumiamo il giocatore nell'origine
    }

    @Test
    void shouldThrowOnInvalidConstructorArguments() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleWeapon(0, 10, 5.0));
        assertThrows(IllegalArgumentException.class, () -> new SimpleWeapon(-1.0, 10, 5.0));
        assertThrows(IllegalArgumentException.class, () -> new SimpleWeapon(Double.NaN, 10, 5.0));
        assertThrows(IllegalArgumentException.class, () -> new SimpleWeapon(1.0, 0, 5.0));
        assertThrows(IllegalArgumentException.class, () -> new SimpleWeapon(1.0, -5, 5.0));
        assertThrows(IllegalArgumentException.class, () -> new SimpleWeapon(1.0, 10, -2.0));
    }

    @Test
    void shouldThrowOnInvalidUpdateArguments() {
        List<Targetable> targets = Collections.emptyList();
        
        assertThrows(IllegalArgumentException.class, () -> weapon.update(-0.1, playerPos, targets));
        assertThrows(NullPointerException.class, () -> weapon.update(0.1, null, targets));
        assertThrows(NullPointerException.class, () -> weapon.update(0.1, playerPos, null));
    }

    @Test
    void shouldTargetNearestEnemyAndNormalizeDirection() {
        Targetable near = new DummyEnemy(new Position(3, 4));   // Distanza 5
        Targetable far = new DummyEnemy(new Position(6, 8));    // Distanza 10
        
        List<Targetable> targets = List.of(far, near);

        Optional<ProjectileSpawnRequest> req = weapon.update(0.1, playerPos, targets);

        assertTrue(req.isPresent());
        // Normalizzazione del vettore (3, 4) con lunghezza 5 -> dirX = 3/5 = 0.6, dirY = 4/5 = 0.8
        assertEquals(0.6, req.get().directionX(), 0.0001);
        assertEquals(0.8, req.get().directionY(), 0.0001);
        assertEquals(10, req.get().damage());
        assertEquals(5.0, req.get().speed());
    }

    @Test
    void shouldHandleOverlappingTargetWithoutNaN() {
        // Target esattamente sulle stesse coordinate del player
        Targetable overlapping = new DummyEnemy(new Position(0, 0));
        
        Optional<ProjectileSpawnRequest> req = weapon.update(0.1, playerPos, List.of(overlapping));

        assertTrue(req.isPresent());
        // Deve essere stata assegnata una direzione di fallback per evitare la divisione per zero
        assertFalse(Double.isNaN(req.get().directionX()));
        assertFalse(Double.isNaN(req.get().directionY()));
        assertEquals(1.0, req.get().directionX(), 0.0001); // Assumiamo il fallback a destra (1, 0)
        assertEquals(0.0, req.get().directionY(), 0.0001);
    }

    @Test
    void shouldIgnoreNullTargetsInList() {
        Targetable valid = new DummyEnemy(new Position(0, 10));
        // Lista con elementi nulli
        List<Targetable> targets = Arrays.asList(null, valid, new DummyEnemy(null));

        Optional<ProjectileSpawnRequest> req = weapon.update(0.1, playerPos, targets);

        assertTrue(req.isPresent());
        assertEquals(0.0, req.get().directionX(), 0.0001);
        assertEquals(1.0, req.get().directionY(), 0.0001);
    }

    @Test
    void shouldRespectCooldown() {
        List<Targetable> targets = List.of(new DummyEnemy(new Position(5, 5)));
        
        // Sparo 1 (Successo)
        assertTrue(weapon.update(0.1, playerPos, targets).isPresent());
        
        // Sparo 2 immediato, delta 0.5s (Fallimento, in cooldown)
        assertFalse(weapon.update(0.5, playerPos, targets).isPresent());
        
        // Sparo 3, delta 0.5s (Successo, cooldown esaurito 0.5 + 0.5 = 1.0)
        assertTrue(weapon.update(0.5, playerPos, targets).isPresent());
    }

    @Test
    void shouldNotSufferCooldownIfNoTargets() {
        // Passa 0.1s ma nessun target
        assertFalse(weapon.update(0.1, playerPos, Collections.emptyList()).isPresent());
        
        // Al frame dopo compare un target, deve sparare (il cooldown non è partito a vuoto)
        List<Targetable> targets = List.of(new DummyEnemy(new Position(5, 5)));
        assertTrue(weapon.update(0.1, playerPos, targets).isPresent());
    }
}