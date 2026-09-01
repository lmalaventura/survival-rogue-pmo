package it.university.survivor.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PatternEvolutionUpgradeTest {

    @Test
    void shouldChangeProjectilePattern() {
        WeaponStats original = new WeaponStats(1.0, 10, 5.0, 1, 0.0);
        WeaponStats upgraded = new PatternEvolutionUpgrade(5, 45.0).apply(original);

        assertEquals(5, upgraded.getProjectileCount());
        assertEquals(45.0, upgraded.getSpreadDegrees(), 1e-9);
        assertEquals(10, upgraded.getDamage());
        assertEquals(1.0, upgraded.getCooldownSeconds(), 1e-9);
    }

    @Test
    void shouldRejectInvalidPattern() {
        assertThrows(IllegalArgumentException.class, () -> new PatternEvolutionUpgrade(0, 20.0));
        assertThrows(IllegalArgumentException.class, () -> new PatternEvolutionUpgrade(3, -1.0));
        assertThrows(IllegalArgumentException.class, () -> new PatternEvolutionUpgrade(3, 361.0));
    }
}