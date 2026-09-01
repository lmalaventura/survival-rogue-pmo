package it.university.survivor.weapon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

class WeaponArsenalTest {

    private static final Position PLAYER = new Position(0, 0);

    @Test
    void shouldProvideFourDistinctWeaponConfigurations() {
        Weapon automatic = WeaponFactory.createAutomatic();
        Weapon shotgun = WeaponFactory.createShotgun();
        Weapon sniper = WeaponFactory.createSniper();
        Weapon pulse = WeaponFactory.createPulse();

        assertEquals(1, automatic.getCurrentStats().getProjectileCount());
        assertEquals(5, shotgun.getCurrentStats().getProjectileCount());
        assertEquals(3, sniper.getCurrentStats().getProjectileCount());
        assertEquals(8, pulse.getCurrentStats().getProjectileCount());

        assertTrue(sniper.getCurrentStats().getDamage() > automatic.getCurrentStats().getDamage());
        assertTrue(shotgun.getCurrentStats().getSpreadDegrees() > 0.0);
        assertTrue(pulse.getCurrentStats().getProjectileCount() > automatic.getCurrentStats().getProjectileCount());
    }

    @Test
    void automaticShouldStillTargetNearestEnemy() {
        Weapon weapon = WeaponFactory.createAutomatic();
        Enemy far = new Enemy(new Position(10, 0), 100, 1.0);
        Enemy near = new Enemy(new Position(0, 3), 100, 1.0);

        ProjectileSpawnRequest request = weapon.attackAll(PLAYER, List.of(far, near)).get(0);

        assertEquals(0.0, request.directionX(), 1e-9);
        assertEquals(1.0, request.directionY(), 1e-9);
    }

    @Test
    void shotgunShouldProduceMultipleSpreadProjectiles() {
        Weapon weapon = WeaponFactory.createShotgun();
        Enemy target = new Enemy(new Position(10, 0), 100, 1.0);

        List<ProjectileSpawnRequest> requests = weapon.attackAll(PLAYER, List.of(target));

        assertEquals(5, requests.size());
        assertTrue(requests.get(0).directionY() < 0.0);
        assertEquals(0.0, requests.get(2).directionY(), 1e-9);
        assertTrue(requests.get(4).directionY() > 0.0);
    }

    @Test
    void sniperShouldPreferFarthestEnemyBeforeEvolution() {
        Weapon weapon = WeaponFactory.createSniper();
        Enemy near = new Enemy(new Position(3, 0), 100, 1.0);
        Enemy far = new Enemy(new Position(0, 10), 100, 1.0);

        ProjectileSpawnRequest request = weapon.attackAll(PLAYER, List.of(near, far)).get(0);

        assertEquals(0.0, request.directionX(), 1e-9);
        assertEquals(1.0, request.directionY(), 1e-9);
    }

    @Test
    void pulseShouldFireAroundThePlayerWithoutTargets() {
        Weapon weapon = WeaponFactory.createPulse();

        List<ProjectileSpawnRequest> requests = weapon.attackAll(PLAYER, List.of());

        assertEquals(8, requests.size());
        assertEquals(1.0, requests.get(0).directionX(), 1e-9);
        assertEquals(0.0, requests.get(0).directionY(), 1e-9);
        assertEquals(0.0, requests.get(2).directionX(), 1e-9);
        assertEquals(1.0, requests.get(2).directionY(), 1e-9);
    }

    @Test
    void sniperEvolutionShouldChangeFromSingleTargetToBurst() {
        Weapon weapon = WeaponFactory.createSniper();
        Enemy first = new Enemy(new Position(3, 0), 100, 1.0);
        Enemy second = new Enemy(new Position(0, 10), 100, 1.0);
        Enemy third = new Enemy(new Position(-10, 0), 100, 1.0);

        assertEquals(1, weapon.attackAll(PLAYER, List.of(first, second, third)).size());

        for (int i = 0; i < 4; i++) {
            weapon.levelUp();
        }

        assertEquals(5, weapon.getLevel());
        assertTrue(weapon.isEvolved());
        weapon.update(weapon.getCooldown());
        assertEquals(3, weapon.attackAll(PLAYER, List.of(first, second, third)).size());
    }

    @Test
    void automaticEvolutionShouldIncreaseProjectilePattern() {
        Weapon weapon = WeaponFactory.createAutomatic();
        Enemy target = new Enemy(new Position(10, 0), 100, 1.0);

        assertEquals(1, weapon.attackAll(PLAYER, List.of(target)).size());

        for (int i = 0; i < 4; i++) {
            weapon.levelUp();
        }

        assertEquals(3, weapon.getCurrentStats().getProjectileCount());
        assertEquals(20.0, weapon.getCurrentStats().getSpreadDegrees(), 1e-9);
        weapon.update(weapon.getCooldown());
        assertEquals(3, weapon.attackAll(PLAYER, List.of(target)).size());
    }

    @Test
    void shotgunEvolutionShouldChangePatternWithoutChangingDamage() {
        Weapon weapon = WeaponFactory.createShotgun();

        for (int i = 0; i < 4; i++) {
            weapon.levelUp();
        }

        assertEquals(7, weapon.getCurrentStats().getProjectileCount());
        assertEquals(75.0, weapon.getCurrentStats().getSpreadDegrees(), 1e-9);
        assertEquals(14, weapon.getCurrentStats().getDamage());
    }

    @Test
    void pulseEvolutionShouldIncreaseRadialDensity() {
        Weapon weapon = WeaponFactory.createPulse();

        for (int i = 0; i < 4; i++) {
            weapon.levelUp();
        }

        assertEquals(12, weapon.getCurrentStats().getProjectileCount());
        assertEquals(12, weapon.attackAll(PLAYER, List.of()).size());
    }
}