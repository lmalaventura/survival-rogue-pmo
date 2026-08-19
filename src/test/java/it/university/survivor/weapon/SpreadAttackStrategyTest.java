package it.university.survivor.weapon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.Position;



public class SpreadAttackStrategyTest{
    
@Test
void shouldCreateCorrectNumberOfProjectiles() {
    AttackStrategy strategy = new SpreadAttackStrategy();

    Enemy enemy =
            new Enemy(new Position(10, 0), 100, 1.0);

    WeaponStats stats =
            new WeaponStats(1.0, 10, 5.0, 3, 30.0);

    List<ProjectileSpawnRequest> requests =
            strategy.attackMultiple(
                    new Position(0, 0),
                    List.of(enemy),
                    stats
            );

    assertEquals(3, requests.size());
}
@Test
void shouldNotAttackWithoutTarget() {
    AttackStrategy strategy = new SpreadAttackStrategy(true);

    WeaponStats stats =
            new WeaponStats(1.0, 10, 5.0, 3, 30.0);

    List<ProjectileSpawnRequest> requests =
            strategy.attackMultiple(
                    new Position(0, 0),
                    List.of(),
                    stats
            );

    assertTrue(requests.isEmpty());
}

}