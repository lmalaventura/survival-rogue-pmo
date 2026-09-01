package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Position;
import it.university.survivor.model.enemy.EnemySpawner;
import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveProgression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class EliteEncounter {

    private static final double CHARGE_INTERVAL_SECONDS = 3.0;
    private static final double CHARGE_DURATION_SECONDS = 0.6;
    private static final double CHARGE_SPEED_MULTIPLIER = 2.5;
    private static final double ENRAGE_HEALTH_RATIO = 0.40;
    private static final double ENRAGE_SPEED_MULTIPLIER = 1.8;
    private static final double BOSS_SUMMON_INTERVAL_SECONDS = 4.0;
    private static final int BOSS_MINIONS_PER_SUMMON = 2;
    private static final int MAX_BOSS_MINIONS_ALIVE = 6;
    private static final int BOSS_SUMMON_DIRECTION_COUNT = 16;
    private static final int BOSS_SUMMON_RING_COUNT = 4;
    private static final double BOSS_SUMMON_EXTRA_DISTANCE = 4.0;

    private final GameWorld world;
    private double chargeIntervalElapsed;
    private double chargeRemaining;
    private Enemy enragedMiniBoss;
    private double bossSummonIntervalElapsed;
    private int bossMinionTypeSequence;
    private int bossSummonPositionSequence;
    private final List<Enemy> bossSummonedMinions = new ArrayList<>();

    EliteEncounter(GameWorld world) {
        this.world = Objects.requireNonNull(world, "World must not be null");
    }

    void updateBeforeMovement(Wave currentWave, double deltaSeconds) {
        updateWaveFiveCharge(currentWave, deltaSeconds);
        updateWaveTenEnrage(currentWave);
    }

    double movementSpeedMultiplier(Wave currentWave, Enemy enemy) {
        if (isWave(currentWave, 5)
                && chargeRemaining > 0.0
                && enemy.getType() == EnemyType.MINIBOSS
                && belongsToWave(currentWave, enemy)) {
            return CHARGE_SPEED_MULTIPLIER;
        }
        if (enemy == enragedMiniBoss) {
            return ENRAGE_SPEED_MULTIPLIER;
        }
        return 1.0;
    }

    void updateBossSummoning(Wave currentWave, double deltaSeconds) {
        if (!isWave(currentWave, WaveProgression.MAX_WAVES)) {
            return;
        }

        Enemy boss = findLivingWaveEnemy(currentWave, EnemyType.BOSS);
        if (boss == null) {
            return;
        }

        bossSummonIntervalElapsed = Math.min(
                BOSS_SUMMON_INTERVAL_SECONDS,
                bossSummonIntervalElapsed + deltaSeconds
        );
        if (bossSummonIntervalElapsed + CollisionRules.TOLERANCE
                < BOSS_SUMMON_INTERVAL_SECONDS) {
            return;
        }

        if (summonBossMinions(currentWave, boss)) {
            bossSummonIntervalElapsed = 0.0;
        }
    }

    void reset() {
        chargeIntervalElapsed = 0.0;
        chargeRemaining = 0.0;
        enragedMiniBoss = null;
        bossSummonIntervalElapsed = 0.0;
        bossMinionTypeSequence = 0;
        bossSummonPositionSequence = 0;
        bossSummonedMinions.clear();
    }

    private void updateWaveFiveCharge(Wave currentWave, double deltaSeconds) {
        if (!isWave(currentWave, 5)) {
            return;
        }

        Enemy miniBoss = findLivingWaveEnemy(currentWave, EnemyType.MINIBOSS);
        if (miniBoss == null) {
            chargeIntervalElapsed = 0.0;
            chargeRemaining = 0.0;
            return;
        }

        if (chargeRemaining > 0.0) {
            if (chargeRemaining <= deltaSeconds + CollisionRules.TOLERANCE) {
                chargeRemaining = 0.0;
                chargeIntervalElapsed = 0.0;
            } else {
                chargeRemaining -= deltaSeconds;
            }
            return;
        }

        chargeIntervalElapsed += deltaSeconds;
        if (chargeIntervalElapsed + CollisionRules.TOLERANCE >= CHARGE_INTERVAL_SECONDS) {
            chargeIntervalElapsed = 0.0;
            chargeRemaining = CHARGE_DURATION_SECONDS;
        }
    }

    private void updateWaveTenEnrage(Wave currentWave) {
        if (!isWave(currentWave, 10) || enragedMiniBoss != null) {
            return;
        }

        Enemy miniBoss = findLivingWaveEnemy(currentWave, EnemyType.MINIBOSS);
        if (miniBoss == null) {
            return;
        }

        double healthRatio = (double) miniBoss.getHealth().getCurrentHealth()
                / miniBoss.getHealth().getMaxHealth();
        if (healthRatio <= ENRAGE_HEALTH_RATIO) {
            enragedMiniBoss = miniBoss;
        }
    }

    private boolean summonBossMinions(Wave currentWave, Enemy boss) {
        long aliveMinions = bossSummonedMinions.stream()
                .filter(enemy -> !enemy.isDead())
                .count();
        if (MAX_BOSS_MINIONS_ALIVE - aliveMinions < BOSS_MINIONS_PER_SUMMON) {
            return false;
        }

        WaveConfig config = WaveProgression.getConfig(currentWave.getWaveNumber());
        int initialPositionSequence = bossSummonPositionSequence;
        int initialTypeSequence = bossMinionTypeSequence;
        List<Enemy> pendingMinions = new ArrayList<>(BOSS_MINIONS_PER_SUMMON);

        for (int index = 0; index < BOSS_MINIONS_PER_SUMMON; index++) {
            EnemyType minionType = bossMinionTypeSequence % 2 == 0
                    ? EnemyType.BASIC
                    : EnemyType.FAST;
            Position spawnPosition = findBossMinionSpawnPosition(
                    boss,
                    minionType,
                    pendingMinions
            );
            if (spawnPosition == null) {
                bossSummonPositionSequence = initialPositionSequence;
                bossMinionTypeSequence = initialTypeSequence;
                return false;
            }

            int maxHealth = minionType == EnemyType.BASIC
                    ? config.enemyHealth()
                    : minionType.maxHealth();
            double movementSpeed = config.enemySpeed() * minionType.speedMultiplier();
            Enemy minion = new EnemySpawner(maxHealth, movementSpeed, minionType)
                    .spawn(List.of(spawnPosition))
                    .get(0);
            pendingMinions.add(minion);
            bossMinionTypeSequence++;
        }

        pendingMinions.forEach(world::addEnemy);
        bossSummonedMinions.addAll(pendingMinions);
        return true;
    }

    private Position findBossMinionSpawnPosition(
            Enemy boss,
            EnemyType minionType,
            List<Enemy> pendingMinions
    ) {
        int candidateCount = BOSS_SUMMON_DIRECTION_COUNT * BOSS_SUMMON_RING_COUNT;
        double minimumBossDistance = boss.getType().collisionRadius()
                + minionType.collisionRadius()
                + CollisionRules.ENEMY_GAP
                + BOSS_SUMMON_EXTRA_DISTANCE;
        double ringSpacing = 2.0 * minionType.collisionRadius()
                + CollisionRules.ENEMY_GAP
                + BOSS_SUMMON_EXTRA_DISTANCE;

        for (int offset = 0; offset < candidateCount; offset++) {
            int candidateIndex = (bossSummonPositionSequence + offset) % candidateCount;
            int ringIndex = candidateIndex / BOSS_SUMMON_DIRECTION_COUNT;
            int directionIndex = candidateIndex % BOSS_SUMMON_DIRECTION_COUNT;
            double angle = 2.0 * Math.PI * directionIndex / BOSS_SUMMON_DIRECTION_COUNT;
            double distance = minimumBossDistance + ringIndex * ringSpacing;
            Position bossPosition = boss.getPosition();
            Position candidate = new Position(
                    bossPosition.x() + Math.cos(angle) * distance,
                    bossPosition.y() + Math.sin(angle) * distance
            );

            if (isValidBossMinionSpawnPosition(candidate, minionType, pendingMinions)) {
                bossSummonPositionSequence = (candidateIndex + 1) % candidateCount;
                return candidate;
            }
        }
        return null;
    }

    private boolean isValidBossMinionSpawnPosition(
            Position candidate,
            EnemyType minionType,
            List<Enemy> pendingMinions
    ) {
        if (candidate.x() < 0.0 || candidate.x() > world.getWidth()
                || candidate.y() < 0.0 || candidate.y() > world.getHeight()) {
            return false;
        }

        Position playerPosition = world.getPlayer().getPosition();
        double playerDistance = distance(candidate, playerPosition);
        if (playerDistance + CollisionRules.TOLERANCE
                < CollisionRules.PLAYER_RADIUS + minionType.collisionRadius()) {
            return false;
        }

        for (Enemy enemy : world.getEnemies()) {
            if (!enemy.isDead()
                    && distance(candidate, enemy.getPosition()) + CollisionRules.TOLERANCE
                    < minionType.collisionRadius()
                    + enemy.getType().collisionRadius()
                    + CollisionRules.ENEMY_GAP) {
                return false;
            }
        }

        for (Enemy pendingMinion : pendingMinions) {
            if (distance(candidate, pendingMinion.getPosition()) + CollisionRules.TOLERANCE
                    < minionType.collisionRadius()
                    + pendingMinion.getType().collisionRadius()
                    + CollisionRules.ENEMY_GAP) {
                return false;
            }
        }
        return true;
    }

    private static Enemy findLivingWaveEnemy(Wave wave, EnemyType type) {
        if (wave == null) {
            return null;
        }
        return wave.getEnemies().stream()
                .filter(enemy -> enemy.getType() == type)
                .filter(enemy -> !enemy.isDead())
                .findFirst()
                .orElse(null);
    }

    private static boolean belongsToWave(Wave wave, Enemy enemy) {
        return wave != null && wave.getEnemies().stream().anyMatch(candidate -> candidate == enemy);
    }

    private static boolean isWave(Wave wave, int waveNumber) {
        return wave != null && wave.getWaveNumber() == waveNumber;
    }

    private static double distance(Position first, Position second) {
        return Math.hypot(first.x() - second.x(), first.y() - second.y());
    }
}
