package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.EnemyType;

class EnemyTypeTest {

    @Test
    void shouldExposeDemoHealthAndSpeedStats() {
        assertAll(
                () -> assertStats(EnemyType.BASIC, 100, 1.0),
                () -> assertStats(EnemyType.FAST, 60, 1.8),
                () -> assertStats(EnemyType.TANK, 250, 0.5),
                () -> assertStats(EnemyType.RANGED, 80, 0.8),
                () -> assertStats(EnemyType.MINIBOSS, 500, 0.7),
                () -> assertStats(EnemyType.BOSS, 1000, 0.6)
        );
    }

    @Test
    void shouldExposeCollisionRadiiConsistentWithEnemySize() {
        assertAll(
                () -> assertEquals(6.0, EnemyType.BASIC.collisionRadius()),
                () -> assertEquals(5.0, EnemyType.FAST.collisionRadius()),
                () -> assertEquals(8.0, EnemyType.TANK.collisionRadius()),
                () -> assertEquals(6.0, EnemyType.RANGED.collisionRadius()),
                () -> assertEquals(12.0, EnemyType.MINIBOSS.collisionRadius()),
                () -> assertEquals(18.0, EnemyType.BOSS.collisionRadius())
        );

        assertTrue(
                EnemyType.FAST.collisionRadius()
                        < EnemyType.BASIC.collisionRadius()
        );
        assertTrue(
                EnemyType.BASIC.collisionRadius()
                        < EnemyType.TANK.collisionRadius()
        );
        assertTrue(
                EnemyType.TANK.collisionRadius()
                        < EnemyType.MINIBOSS.collisionRadius()
        );
        assertTrue(
                EnemyType.MINIBOSS.collisionRadius()
                        < EnemyType.BOSS.collisionRadius()
        );
    }

    @Test
    void shouldExposeExperienceRewardForEveryEnemyType() {
        assertAll(
                () -> assertEquals(10, EnemyType.BASIC.experienceReward()),
                () -> assertEquals(12, EnemyType.FAST.experienceReward()),
                () -> assertEquals(18, EnemyType.TANK.experienceReward()),
                () -> assertEquals(15, EnemyType.RANGED.experienceReward()),
                () -> assertEquals(75, EnemyType.MINIBOSS.experienceReward()),
                () -> assertEquals(250, EnemyType.BOSS.experienceReward())
        );
    }

    @Test
    void bossTypesShouldHaveDistinctHealthInvariants() {
        assertTrue(
                EnemyType.MINIBOSS.maxHealth()
                        > EnemyType.BASIC.maxHealth()
        );
        assertTrue(
                EnemyType.BOSS.maxHealth()
                        > EnemyType.MINIBOSS.maxHealth()
        );
    }

    @Test
    void shouldHaveAllRequiredEnemyTypes() {
        assertEquals(
                Set.of(
                        EnemyType.BASIC,
                        EnemyType.FAST,
                        EnemyType.TANK,
                        EnemyType.RANGED,
                        EnemyType.MINIBOSS,
                        EnemyType.BOSS
                ),
                Set.of(EnemyType.values())
        );
    }

    private void assertStats(
            EnemyType type,
            int maxHealth,
            double speedMultiplier
    ) {
        assertEquals(maxHealth, type.maxHealth());
        assertEquals(speedMultiplier, type.speedMultiplier());
    }
}
