package it.university.survivor.weapon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;

class WeaponEvolutionTest {

    @Test
    void shouldStartAtLevelOne() {
        Weapon weapon = WeaponFactory.createAutomatic();

        assertEquals(1, weapon.getLevel());
        assertEquals(5, weapon.getMaxLevel());
        assertFalse(weapon.isEvolved());
    }

    @Test
    void shouldEvolveExactlyAtMaximumLevel() {
        Weapon weapon = WeaponFactory.createShotgun();

        weapon.levelUp();
        weapon.levelUp();
        weapon.levelUp();

        assertEquals(4, weapon.getLevel());
        assertFalse(weapon.isEvolved());
        assertEquals(5, weapon.getCurrentStats().getProjectileCount());

        weapon.levelUp();

        assertEquals(5, weapon.getLevel());
        assertTrue(weapon.isEvolved());
        assertEquals(7, weapon.getCurrentStats().getProjectileCount());
    }

    @Test
    void shouldNotApplyEvolutionTwice() {
        Weapon weapon = WeaponFactory.createAutomatic();

        for (int i = 0; i < 20; i++) {
            weapon.levelUp();
        }

        assertEquals(5, weapon.getLevel());
        assertEquals(3, weapon.getCurrentStats().getProjectileCount());
        assertEquals(20.0, weapon.getCurrentStats().getSpreadDegrees(), 1e-9);
    }

    @Test
    void shouldNotLevelBeyondMaximum() {
        Weapon weapon = WeaponFactory.createSniper();

        for (int i = 0; i < 20; i++) {
            weapon.levelUp();
        }

        assertEquals(5, weapon.getLevel());
        assertTrue(weapon.isEvolved());
    }

    @Test
    void evolvedWeaponShouldStillUseNormalCooldown() {
        Weapon weapon = WeaponFactory.createShotgun();
        Enemy target = new Enemy(new Position(10, 0), 100, 1.0);

        for (int i = 0; i < 4; i++) {
            weapon.levelUp();
        }

        assertEquals(7, weapon.attack(new Position(0, 0), List.of(target)).size());
        assertEquals(1.20, weapon.getCooldownRemaining(), 1e-9);
    }
}