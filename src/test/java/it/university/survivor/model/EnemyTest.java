package it.university.survivor.model;

import it.university.survivor.model.enemy.EnemyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnemyTest {

    @Test
    void shouldExposeConfiguredStateAndDefaultToBasic() {
        Enemy basic = new Enemy(new Position(0, 0), 100, 2.0);
        Enemy fast = new Enemy(new Position(1, 2), 60, 3.6, EnemyType.FAST);

        assertEquals(EnemyType.BASIC, basic.getType());
        assertEquals(EnemyType.FAST, fast.getType());
        assertEquals(new Position(1, 2), fast.getPosition());
        assertEquals(60, fast.getHealth().getMaxHealth());
        assertEquals(3.6, fast.getMovementSpeed(), 1e-9);
    }

    @Test
    void shouldValidateConstruction() {
        assertThrows(NullPointerException.class, () -> new Enemy(null, 100, 2.0));
        assertThrows(NullPointerException.class,
                () -> new Enemy(new Position(0, 0), 100, 2.0, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Enemy(new Position(0, 0), 100, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new Enemy(new Position(0, 0), 100, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new Enemy(new Position(0, 0), 100, Double.POSITIVE_INFINITY));
    }

    @Test
    void shouldMoveOnlyToValidPosition() {
        Enemy enemy = basicEnemyAt(0, 0);
        Position next = new Position(5, 7);
        enemy.moveTo(next);

        assertEquals(next, enemy.getPosition());
        assertThrows(NullPointerException.class, () -> enemy.moveTo(null));
    }

    @Test
    void shouldDelegateDamageAndDeathToHealth() {
        Enemy enemy = basicEnemyAt(0, 0);
        enemy.takeDamage(30);
        assertEquals(70, enemy.getHealth().getCurrentHealth());
        assertFalse(enemy.isDead());

        enemy.takeDamage(70);
        assertTrue(enemy.isDead());
    }

    @Test
    void basicEnemyShouldCalculateNormalizedDirection() {
        Enemy enemy = basicEnemyAt(0, 0);
        Position direction = enemy.calculateDesiredDirection(new Position(3, 4));

        assertEquals(0.6, direction.x(), 1e-9);
        assertEquals(0.8, direction.y(), 1e-9);
    }

    @Test
    void shouldReturnZeroDirectionForOverlappingTarget() {
        Enemy enemy = basicEnemyAt(0, 0);
        assertEquals(new Position(0, 0), enemy.calculateDesiredDirection(new Position(0, 0)));
        assertThrows(NullPointerException.class, () -> enemy.calculateDesiredDirection(null));
    }

    @Test
    void rangedEnemyShouldMaintainPreferredDistanceBand() {
        Enemy enemy = rangedEnemyAt(0, 0);

        Position towardFarTarget = enemy.calculateDesiredDirection(new Position(400, 0));
        Position holdPosition = enemy.calculateDesiredDirection(new Position(250, 0));
        Position awayFromNearTarget = enemy.calculateDesiredDirection(new Position(100, 0));

        assertEquals(new Position(1, 0), towardFarTarget);
        assertEquals(new Position(0, 0), holdPosition);
        assertEquals(-1.0, awayFromNearTarget.x(), 1e-9);
        assertEquals(0.0, awayFromNearTarget.y(), 1e-9);
    }

    @Test
    void rangedAttackShouldRequireRangeAndReadyCooldown() {
        Enemy enemy = rangedEnemyAt(0, 0);

        assertFalse(enemy.canRequestRangedAttack(new Position(199, 0)));
        assertTrue(enemy.canRequestRangedAttack(new Position(200, 0)));
        assertTrue(enemy.canRequestRangedAttack(new Position(300, 0)));
        assertFalse(enemy.canRequestRangedAttack(new Position(301, 0)));

        enemy.requestRangedAttack();
        assertFalse(enemy.canRequestRangedAttack(new Position(250, 0)));
        enemy.updateRangedCooldown(1.0);
        assertTrue(enemy.canRequestRangedAttack(new Position(250, 0)));
    }

    @Test
    void rangedCooldownShouldAccumulateAcrossUpdates() {
        Enemy enemy = rangedEnemyAt(0, 0);
        enemy.requestRangedAttack();

        for (int update = 0; update < 9; update++) {
            enemy.updateRangedCooldown(0.1);
        }
        assertFalse(enemy.canRequestRangedAttack());

        enemy.updateRangedCooldown(0.1);
        assertTrue(enemy.canRequestRangedAttack());
    }

    @Test
    void nonRangedEnemyShouldNeverRequestRangedAttack() {
        Enemy enemy = basicEnemyAt(0, 0);
        assertFalse(enemy.canRequestRangedAttack());
        assertFalse(enemy.canRequestRangedAttack(new Position(250, 0)));
    }

    @Test
    void rangedApiShouldValidateArguments() {
        Enemy enemy = rangedEnemyAt(0, 0);
        assertThrows(NullPointerException.class, () -> enemy.canRequestRangedAttack(null));
        assertThrows(IllegalArgumentException.class, () -> enemy.updateRangedCooldown(-0.1));
        assertThrows(IllegalArgumentException.class,
                () -> enemy.updateRangedCooldown(Double.NaN));
    }

    private static Enemy basicEnemyAt(double x, double y) {
        return new Enemy(new Position(x, y), 100, 2.0);
    }

    private static Enemy rangedEnemyAt(double x, double y) {
        return new Enemy(new Position(x, y), 80, 0.8, EnemyType.RANGED);
    }
}
