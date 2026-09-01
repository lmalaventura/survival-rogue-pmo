package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemyType;

class EnemyTest {

    @Test
    void enemyShouldCalculateDirectionTowardsTarget() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        Position direction = enemy.calculateDesiredDirection(
                new Position(3.0, 4.0)
        );

        assertEquals(0.6, direction.x(), 0.0001);
        assertEquals(0.8, direction.y(), 0.0001);
    }

    @Test
    void enemyShouldTakeDamage() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        enemy.takeDamage(30);

        assertEquals(70, enemy.getHealth().getCurrentHealth());
        assertFalse(enemy.isDead());
    }

    @Test
    void enemyShouldDieWhenHealthReachesZero() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        enemy.takeDamage(100);

        assertTrue(enemy.isDead());
        assertEquals(0, enemy.getHealth().getCurrentHealth());
    }

    @Test
    void enemyShouldRejectNullPosition() {
        assertThrows(
                NullPointerException.class,
                () -> new Enemy(null, 100, 2.0)
        );
    }

    @Test
    void enemyShouldRejectZeroSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Enemy(
                        new Position(0.0, 0.0),
                        100,
                        0.0
                )
        );
    }

    @Test
    void enemyShouldRejectNegativeSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Enemy(
                        new Position(0.0, 0.0),
                        100,
                        -2.0
                )
        );
    }

    @Test
    void enemyShouldRejectNonFiniteSpeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Enemy(
                        new Position(0.0, 0.0),
                        100,
                        Double.NaN
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Enemy(
                        new Position(0.0, 0.0),
                        100,
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void enemyShouldRejectNullTarget() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        assertThrows(
                NullPointerException.class,
                () -> enemy.calculateDesiredDirection(null)
        );
    }

    @Test
    void enemyShouldReturnZeroDirectionForSamePosition() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        Position direction = enemy.calculateDesiredDirection(
                new Position(0.0, 0.0)
        );

        assertEquals(0.0, direction.x(), 0.0001);
        assertEquals(0.0, direction.y(), 0.0001);
    }

    @Test
    void enemyShouldExposeMovementSpeed() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        assertEquals(2.0, enemy.getMovementSpeed(), 0.0001);
    }

    @Test
    void enemyShouldMoveToNewPosition() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        Position newPosition = new Position(5.0, 7.0);

        enemy.moveTo(newPosition);

        assertEquals(newPosition, enemy.getPosition());
    }

    @Test
    void enemyShouldRejectNullPositionInMoveTo() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        assertThrows(
                NullPointerException.class,
                () -> enemy.moveTo(null)
        );
    }

    @Test
    void legacyConstructorShouldCreateBasicEnemy() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                2.0
        );

        assertEquals(EnemyType.BASIC, enemy.getType());
    }

    @Test
    void enemyShouldExposeConfiguredType() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                60,
                1.8,
                EnemyType.FAST
        );

        assertEquals(EnemyType.FAST, enemy.getType());
        assertEquals(60, enemy.getHealth().getMaxHealth());
        assertEquals(1.8, enemy.getMovementSpeed(), 0.0001);
    }

    @Test
    void enemyShouldRejectNullType() {
        assertThrows(
                NullPointerException.class,
                () -> new Enemy(
                        new Position(0.0, 0.0),
                        100,
                        2.0,
                        null
                )
        );
    }

    @Test
    void rangedEnemyShouldHaveRangedType() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        assertEquals(EnemyType.RANGED, enemy.getType());
    }

    @Test
    void rangedEnemyShouldPreferToStayAtDistanceFromPlayer() {
        Enemy enemy = new Enemy(
                new Position(100.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        Position direction = enemy.calculateDesiredDirection(
                new Position(0.0, 0.0)
        );

        assertEquals(1.0, direction.x(), 0.0001);
        assertEquals(0.0, direction.y(), 0.0001);
    }

    @Test
    void rangedEnemyShouldNotMoveTowardsPlayerWhenAlreadyAtPreferredDistance() {
        Enemy enemy = new Enemy(
                new Position(300.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        Position direction = enemy.calculateDesiredDirection(
                new Position(0.0, 0.0)
        );

        assertEquals(0.0, direction.x(), 0.0001);
        assertEquals(0.0, direction.y(), 0.0001);
    }

    @Test
    void rangedEnemyShouldMoveAwayWhenTooCloseToPlayer() {
        Enemy enemy = new Enemy(
                new Position(50.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        Position direction = enemy.calculateDesiredDirection(
                new Position(0.0, 0.0)
        );

        assertEquals(1.0, direction.x(), 0.0001);
        assertEquals(0.0, direction.y(), 0.0001);
    }

    @Test
    void rangedEnemyShouldExposeAttackCooldown() {
        Enemy enemy = new Enemy(
                new Position(300.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        assertTrue(enemy.canRequestRangedAttack());
    }

    @Test
    void rangedEnemyShouldRespectAttackCooldown() {
        Enemy enemy = new Enemy(
                new Position(300.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        assertTrue(enemy.canRequestRangedAttack());

        enemy.requestRangedAttack();

        assertFalse(enemy.canRequestRangedAttack());

        enemy.updateRangedCooldown(1.0);

        assertTrue(enemy.canRequestRangedAttack());
    }

    @Test
    void rangedEnemyShouldRequestAttackOnlyInsidePreferredDistanceBand() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        assertFalse(enemy.canRequestRangedAttack(
                new Position(301.0, 0.0)
        ));
        assertTrue(enemy.canRequestRangedAttack(
                new Position(250.0, 0.0)
        ));
        assertFalse(enemy.canRequestRangedAttack(
                new Position(199.0, 0.0)
        ));
    }

    @Test
    void rangedAttackDistanceBandShouldIncludeItsBoundaries() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        assertTrue(enemy.canRequestRangedAttack(
                new Position(200.0, 0.0)
        ));
        assertTrue(enemy.canRequestRangedAttack(
                new Position(300.0, 0.0)
        ));
    }

    @Test
    void rangedEnemyShouldBecomeReadyAfterTenCooldownUpdates() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );
        Position target = new Position(250.0, 0.0);

        enemy.requestRangedAttack();

        for (int update = 0; update < 9; update++) {
            enemy.updateRangedCooldown(0.1);
        }

        assertFalse(enemy.canRequestRangedAttack(target));

        enemy.updateRangedCooldown(0.1);

        assertTrue(enemy.canRequestRangedAttack(target));
    }

    @Test
    void rangedEnemyShouldRejectNullAttackTarget() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        assertThrows(
                NullPointerException.class,
                () -> enemy.canRequestRangedAttack(null)
        );
    }

    @Test
    void nonRangedEnemyShouldNeverRequestRangedAttack() {
        Enemy enemy = new Enemy(
                new Position(0.0, 0.0),
                100,
                1.0,
                EnemyType.BASIC
        );
        Position target = new Position(250.0, 0.0);

        assertFalse(enemy.canRequestRangedAttack());
        assertFalse(enemy.canRequestRangedAttack(target));

        enemy.requestRangedAttack();
        enemy.updateRangedCooldown(1.0);

        assertFalse(enemy.canRequestRangedAttack());
        assertFalse(enemy.canRequestRangedAttack(target));
    }

    @Test
    void rangedEnemyShouldRejectNegativeCooldownUpdate() {
        Enemy enemy = new Enemy(
                new Position(300.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> enemy.updateRangedCooldown(-0.1)
        );
    }

    @Test
    void rangedEnemyShouldRejectNonFiniteCooldownUpdate() {
        Enemy enemy = new Enemy(
                new Position(300.0, 0.0),
                80,
                0.8,
                EnemyType.RANGED
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> enemy.updateRangedCooldown(Double.NaN)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> enemy.updateRangedCooldown(
                        Double.POSITIVE_INFINITY
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> enemy.updateRangedCooldown(
                        Double.NEGATIVE_INFINITY
                )
        );
    }
}
