package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Item;
import it.university.survivor.model.ModifierType;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.Projectile;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.StatModifier;
import it.university.survivor.model.UpgradeCatalog;
import it.university.survivor.model.UpgradeChoiceSession;
import it.university.survivor.model.enemy.EnemySpawner;
import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveFactory;
import it.university.survivor.model.enemy.WaveProgression;
import it.university.survivor.weapon.FlatCooldownUpgrade;
import it.university.survivor.weapon.FlatDamageUpgrade;
import it.university.survivor.weapon.PercentCooldownUpgrade;
import it.university.survivor.weapon.PercentDamageUpgrade;
import it.university.survivor.weapon.ProjectileSpawnRequest;
import it.university.survivor.weapon.Weapon;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public final class GameController {

    private static final double MAX_DELTA_SECONDS = 0.1;
    private static final double PLAYER_COLLISION_RADIUS = 8.0;
    private static final double PROJECTILE_COLLISION_RADIUS = 3.0;
    private static final double ENEMY_SEPARATION_GAP = 1.0;
    private static final double PLAYER_MOVEMENT_SUBSTEP_DISTANCE = 1.0;
    private static final double CONTACT_DISTANCE_TOLERANCE = 1.0e-9;
    private static final int ENEMY_CONTACT_DAMAGE = 10;
    private static final double PLAYER_HIT_INVULNERABILITY_SECONDS = 0.5;
    private static final double SPAWN_MARGIN = 24.0;
    private static final double MINIBOSS_CHARGE_INTERVAL_SECONDS = 3.0;
    private static final double MINIBOSS_CHARGE_DURATION_SECONDS = 0.6;
    private static final double MINIBOSS_CHARGE_SPEED_MULTIPLIER = 2.5;
    private static final double MINIBOSS_ENRAGE_HEALTH_RATIO = 0.40;
    private static final double MINIBOSS_ENRAGE_SPEED_MULTIPLIER = 1.8;
    private static final double BOSS_SUMMON_INTERVAL_SECONDS = 4.0;
    private static final int BOSS_MINIONS_PER_SUMMON = 2;
    private static final int MAX_BOSS_MINIONS_ALIVE = 6;
    private static final int BOSS_SUMMON_DIRECTION_COUNT = 16;
    private static final int BOSS_SUMMON_RING_COUNT = 4;
    private static final double BOSS_SUMMON_EXTRA_DISTANCE = 4.0;

    private final GameWorld world;
    private final ExperienceProgression experienceProgression;
    private final RunStatistics runStatistics;
    private final Optional<Weapon> weapon;
    private final UpgradeCatalog upgradeCatalog;
    private final Random upgradeRandom;
    private Wave currentWave;
    private RunState runState;
    private UpgradeChoiceSession currentUpgradeSession;
    private final EnumSet<MovementDirection> activeDirections =
            EnumSet.noneOf(MovementDirection.class);
    private double playerHitInvulnerabilityRemaining = 0.0;
    private double miniBossChargeIntervalElapsed = 0.0;
    private double miniBossChargeRemaining = 0.0;
    private Enemy enragedMiniBoss;
    private double bossSummonIntervalElapsed = 0.0;
    private int bossMinionTypeSequence = 0;
    private int bossSummonPositionSequence = 0;
    private final List<Enemy> bossSummonedMinions = new ArrayList<>();

    public GameController(GameWorld world) {
        this(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                Optional.empty(),
                null,
                new UpgradeCatalog(),
                new Random()
        );
    }

    public GameController(GameWorld world, Wave initialWave) {
        this(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                Optional.empty(),
                Objects.requireNonNull(initialWave, "Initial wave must not be null"),
                new UpgradeCatalog(),
                new Random()
        );
    }

    public GameController(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics
    ) {
        this(
                world,
                experienceProgression,
                runStatistics,
                Optional.empty(),
                null,
                new UpgradeCatalog(),
                new Random()
        );
    }

    public GameController(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Weapon weapon
    ) {
        this(
                world,
                experienceProgression,
                runStatistics,
                Optional.of(
                        Objects.requireNonNull(weapon, "Weapon must not be null")
                ),
                null,
                new UpgradeCatalog(),
                new Random()
        );
    }

    public GameController(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Weapon weapon,
            Wave initialWave
    ) {
        this(
                world,
                experienceProgression,
                runStatistics,
                Optional.of(
                        Objects.requireNonNull(weapon, "Weapon must not be null")
                ),
                Objects.requireNonNull(initialWave, "Initial wave must not be null"),
                new UpgradeCatalog(),
                new Random()
        );
    }

    GameController(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Weapon weapon,
            Wave initialWave,
            UpgradeCatalog upgradeCatalog,
            Random upgradeRandom
    ) {
        this(
                world,
                experienceProgression,
                runStatistics,
                Optional.of(Objects.requireNonNull(weapon, "Weapon must not be null")),
                Objects.requireNonNull(initialWave, "Initial wave must not be null"),
                upgradeCatalog,
                upgradeRandom
        );
    }

    private GameController(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Optional<Weapon> weapon,
            Wave currentWave,
            UpgradeCatalog upgradeCatalog,
            Random upgradeRandom
    ) {
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.experienceProgression = Objects.requireNonNull(
                experienceProgression,
                "Experience progression must not be null"
        );
        this.runStatistics = Objects.requireNonNull(
                runStatistics,
                "Run statistics must not be null"
        );
        this.weapon = Objects.requireNonNull(weapon, "Weapon must not be null");
        this.upgradeCatalog = Objects.requireNonNull(
                upgradeCatalog,
                "Upgrade catalog must not be null"
        );
        this.upgradeRandom = Objects.requireNonNull(
                upgradeRandom,
                "Upgrade random must not be null"
        );
        this.currentWave = currentWave;
        this.runState = RunState.ACTIVE_WAVE;
        this.currentUpgradeSession = null;
    }

    public ExperienceProgression getExperienceProgression() {
        return experienceProgression;
    }

    public RunStatistics getRunStatistics() {
        return runStatistics;
    }

    public Wave getCurrentWave() {
        return currentWave;
    }

    public RunState getRunState() {
        return runState;
    }

    public UpgradeChoiceSession getCurrentUpgradeSession() {
        return currentUpgradeSession;
    }

    public void selectUpgrade(Item item) {
        Objects.requireNonNull(item, "Item must not be null");
        UpgradeChoiceSession session = requireUpgradeSelection();
        if (!experienceProgression.hasPendingLevelUp()) {
            throw new IllegalStateException("No pending level-up is available");
        }

        int optionIndex = findUpgradeOptionIndex(session, item);
        if (optionIndex < 0) {
            throw new IllegalArgumentException("Item does not belong to the current session");
        }

        if (session.isSelectionMade() && !session.getSelectedItem().equals(item)) {
            throw new IllegalStateException("A different upgrade has already been selected");
        }

        validateUpgradeApplication(item);
        Item selectedItem = session.isSelectionMade()
                ? session.getSelectedItem()
                : session.selectOption(optionIndex);
        applyUpgrade(selectedItem);
        runStatistics.recordUpgradeSelected(selectedItem);

        if (!experienceProgression.consumePendingLevelUp()) {
            throw new IllegalStateException("Pending level-up could not be consumed");
        }

        if (experienceProgression.hasPendingLevelUp()) {
            currentUpgradeSession = createUpgradeChoiceSession();
            return;
        }

        currentUpgradeSession = null;
        startNextWave();
    }

    public void rerollUpgradeChoices() {
        UpgradeChoiceSession session = requireUpgradeSelection();
        session.reroll();
        runStatistics.recordReroll();
    }

    private UpgradeChoiceSession requireUpgradeSelection() {
        if (runState != RunState.UPGRADE_SELECTION || currentUpgradeSession == null) {
            throw new IllegalStateException("No upgrade selection is active");
        }
        return currentUpgradeSession;
    }

    private static int findUpgradeOptionIndex(
            UpgradeChoiceSession session,
            Item item
    ) {
        List<Item> options = session.getCurrentOptions();
        for (int index = 0; index < options.size(); index++) {
            if (options.get(index) == item) {
                return index;
            }
        }
        return options.indexOf(item);
    }

    private void validateUpgradeApplication(Item item) {
        StatModifier modifier = item.baseModifier();
        double effectiveValue = item.getEffectiveValue();
        if (!Double.isFinite(effectiveValue)) {
            throw new IllegalArgumentException("Effective upgrade value must be finite");
        }

        switch (modifier.statType()) {
            case MAX_HEALTH -> validateMaxHealthUpgrade(modifier, effectiveValue);
            case DAMAGE -> validateDamageUpgrade(modifier, effectiveValue);
            case COOLDOWN -> validateCooldownUpgrade(modifier, effectiveValue);
        }
    }

    private void validateMaxHealthUpgrade(
            StatModifier modifier,
            double effectiveValue
    ) {
        int increment = calculateMaxHealthIncrement(modifier, effectiveValue);
        int currentMaximum = world.getPlayer().getHealth().getMaxHealth();
        int currentHealth = world.getPlayer().getHealth().getCurrentHealth();
        try {
            Math.addExact(currentMaximum, increment);
            Math.addExact(currentHealth, increment);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Maximum health upgrade is too large", exception);
        }
    }

    private void validateDamageUpgrade(
            StatModifier modifier,
            double effectiveValue
    ) {
        Weapon activeWeapon = requireWeaponForUpgrade();
        if (modifier.modifierType() == ModifierType.FLAT) {
            int bonus = roundPositiveIncrement(effectiveValue, "Damage bonus");
            try {
                Math.addExact(activeWeapon.getCurrentStats().getDamage(), bonus);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("Damage upgrade is too large", exception);
            }
            return;
        }

        requirePositiveFinite(effectiveValue, "Damage percentage");
        double upgradedDamage = activeWeapon.getCurrentStats().getDamage()
                * (1.0 + effectiveValue);
        if (!Double.isFinite(upgradedDamage)
                || Math.round(upgradedDamage) > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Damage upgrade is too large");
        }
    }

    private void validateCooldownUpgrade(
            StatModifier modifier,
            double effectiveValue
    ) {
        requireWeaponForUpgrade();
        double reduction = Math.abs(effectiveValue);
        requirePositiveFinite(reduction, "Cooldown reduction");
        if (modifier.modifierType() == ModifierType.PERCENTAGE && reduction >= 1.0) {
            throw new IllegalArgumentException(
                    "Cooldown percentage reduction must be less than one"
            );
        }
    }

    private void applyUpgrade(Item item) {
        StatModifier modifier = item.baseModifier();
        double effectiveValue = item.getEffectiveValue();

        switch (modifier.statType()) {
            case MAX_HEALTH -> world.getPlayer().getHealth().increaseMaxHealth(
                    calculateMaxHealthIncrement(modifier, effectiveValue)
            );
            case DAMAGE -> applyDamageUpgrade(modifier, effectiveValue);
            case COOLDOWN -> applyCooldownUpgrade(modifier, effectiveValue);
        }
    }

    private int calculateMaxHealthIncrement(
            StatModifier modifier,
            double effectiveValue
    ) {
        double rawIncrement = modifier.modifierType() == ModifierType.FLAT
                ? effectiveValue
                : world.getPlayer().getHealth().getMaxHealth() * effectiveValue;
        return roundPositiveIncrement(rawIncrement, "Maximum health bonus");
    }

    private void applyDamageUpgrade(
            StatModifier modifier,
            double effectiveValue
    ) {
        Weapon activeWeapon = requireWeaponForUpgrade();
        if (modifier.modifierType() == ModifierType.FLAT) {
            activeWeapon.upgrade(new FlatDamageUpgrade(
                    roundPositiveIncrement(effectiveValue, "Damage bonus")
            ));
        } else {
            activeWeapon.upgrade(new PercentDamageUpgrade(effectiveValue));
        }
    }

    private void applyCooldownUpgrade(
            StatModifier modifier,
            double effectiveValue
    ) {
        Weapon activeWeapon = requireWeaponForUpgrade();
        double reduction = Math.abs(effectiveValue);
        if (modifier.modifierType() == ModifierType.FLAT) {
            activeWeapon.upgrade(new FlatCooldownUpgrade(reduction));
        } else {
            activeWeapon.upgrade(new PercentCooldownUpgrade(reduction));
        }
    }

    private Weapon requireWeaponForUpgrade() {
        return weapon.orElseThrow(() -> new IllegalStateException(
                "A Weapon is required for this upgrade"
        ));
    }

    private static int roundPositiveIncrement(double value, String description) {
        requirePositiveFinite(value, description);
        long roundedValue = Math.max(1L, Math.round(value));
        if (roundedValue > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(description + " is too large");
        }
        return (int) roundedValue;
    }

    private static void requirePositiveFinite(double value, String description) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(description + " must be finite and positive");
        }
    }

    public void setDirectionActive(MovementDirection direction, boolean active) {
        Objects.requireNonNull(direction, "Direction must not be null");

        if (active) {
            activeDirections.add(direction);
        } else {
            activeDirections.remove(direction);
        }
    }

    public void update(double deltaSeconds) {
        if (runState != RunState.ACTIVE_WAVE) {
            return;
        }
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0.0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        if (world.getPlayer().getHealth().isDead()) {
            runState = RunState.DEFEAT;
            resetEliteEncounterState();
            return;
        }
        if (deltaSeconds == 0.0) {
            return;
        }
        if (completeFinalWaveIfNeeded()) {
            return;
        }

        double effectiveDelta = Math.min(deltaSeconds, MAX_DELTA_SECONDS);
        runStatistics.addElapsedTime(effectiveDelta);
        updateEliteEncounterState(effectiveDelta);
        updatePlayerHitInvulnerability(effectiveDelta);
        updatePlayerMovement(effectiveDelta);
        updateEnemyMovement(effectiveDelta);
        updateWeapon(effectiveDelta);
        updateProjectileMovementAndCollisions(effectiveDelta);
        if (completeFinalWaveIfNeeded()) {
            return;
        }
        updateWaveFifteenSummoning(effectiveDelta);
        applyEnemyContactDamage();

        if (world.getPlayer().getHealth().isDead()) {
            runState = RunState.DEFEAT;
            resetEliteEncounterState();
            return;
        }
        advanceWaveIfCompleted();
    }

    private void updatePlayerHitInvulnerability(double effectiveDelta) {
        double comparisonTolerance = Math.ulp(PLAYER_HIT_INVULNERABILITY_SECONDS);
        if (playerHitInvulnerabilityRemaining <= effectiveDelta + comparisonTolerance) {
            playerHitInvulnerabilityRemaining = 0.0;
        } else {
            playerHitInvulnerabilityRemaining -= effectiveDelta;
        }
    }

    private void updatePlayerMovement(double effectiveDelta) {
        double directionX = (activeDirections.contains(MovementDirection.RIGHT) ? 1.0 : 0.0)
                - (activeDirections.contains(MovementDirection.LEFT) ? 1.0 : 0.0);
        double directionY = (activeDirections.contains(MovementDirection.DOWN) ? 1.0 : 0.0)
                - (activeDirections.contains(MovementDirection.UP) ? 1.0 : 0.0);

        if (directionX == 0.0 && directionY == 0.0) {
            return;
        }

        double magnitude = Math.hypot(directionX, directionY);
        double normalizedX = directionX / magnitude;
        double normalizedY = directionY / magnitude;

        Player player = world.getPlayer();
        double distance = player.getMovementSpeed() * effectiveDelta;
        if (distance == 0.0) {
            return;
        }

        int substepCount = Math.max(
                1,
                (int) Math.ceil(distance / PLAYER_MOVEMENT_SUBSTEP_DISTANCE)
        );
        double substepX = normalizedX * distance / substepCount;
        double substepY = normalizedY * distance / substepCount;

        for (int substep = 0; substep < substepCount; substep++) {
            Position currentPosition = player.getPosition();
            Position candidatePosition = createMovementCandidate(
                    currentPosition,
                    substepX,
                    substepY
            );

            if (candidatePosition.equals(currentPosition)
                    || !preservesPlayerEnemyDistance(
                            currentPosition,
                            candidatePosition
                    )) {
                break;
            }

            world.movePlayerBy(substepX, substepY);
        }
    }

    private boolean preservesPlayerEnemyDistance(
            Position currentPosition,
            Position candidatePosition
    ) {
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double currentDistance = Math.hypot(
                    currentPosition.x() - enemyPosition.x(),
                    currentPosition.y() - enemyPosition.y()
            );
            double candidateDistance = Math.hypot(
                    candidatePosition.x() - enemyPosition.x(),
                    candidatePosition.y() - enemyPosition.y()
            );
            double minimumAllowedDistance = Math.min(
                    playerEnemyContactDistance(enemy),
                    currentDistance
            );

            if (candidateDistance < minimumAllowedDistance) {
                return false;
            }
        }

        return true;
    }

    private void updateEliteEncounterState(double effectiveDelta) {
        updateWaveFiveCharge(effectiveDelta);
        updateWaveTenEnrage();
    }

    private void updateWaveFiveCharge(double effectiveDelta) {
        if (!isCurrentWave(5)) {
            return;
        }

        Enemy miniBoss = findLivingCurrentWaveEnemy(EnemyType.MINIBOSS);
        if (miniBoss == null) {
            miniBossChargeIntervalElapsed = 0.0;
            miniBossChargeRemaining = 0.0;
            return;
        }

        if (miniBossChargeRemaining > 0.0) {
            if (miniBossChargeRemaining
                    <= effectiveDelta + CONTACT_DISTANCE_TOLERANCE) {
                miniBossChargeRemaining = 0.0;
                miniBossChargeIntervalElapsed = 0.0;
            } else {
                miniBossChargeRemaining -= effectiveDelta;
            }
            return;
        }

        miniBossChargeIntervalElapsed += effectiveDelta;
        if (miniBossChargeIntervalElapsed + CONTACT_DISTANCE_TOLERANCE
                >= MINIBOSS_CHARGE_INTERVAL_SECONDS) {
            miniBossChargeIntervalElapsed = 0.0;
            miniBossChargeRemaining = MINIBOSS_CHARGE_DURATION_SECONDS;
        }
    }

    private void updateWaveTenEnrage() {
        if (!isCurrentWave(10) || enragedMiniBoss != null) {
            return;
        }

        Enemy miniBoss = findLivingCurrentWaveEnemy(EnemyType.MINIBOSS);
        if (miniBoss == null) {
            return;
        }

        double healthRatio = (double) miniBoss.getHealth().getCurrentHealth()
                / miniBoss.getHealth().getMaxHealth();
        if (healthRatio <= MINIBOSS_ENRAGE_HEALTH_RATIO) {
            enragedMiniBoss = miniBoss;
        }
    }

    private void updateWaveFifteenSummoning(double effectiveDelta) {
        if (!isCurrentWave(WaveProgression.MAX_WAVES)) {
            return;
        }

        Enemy boss = findLivingCurrentWaveEnemy(EnemyType.BOSS);
        if (boss == null) {
            return;
        }

        bossSummonIntervalElapsed = Math.min(
                BOSS_SUMMON_INTERVAL_SECONDS,
                bossSummonIntervalElapsed + effectiveDelta
        );
        if (bossSummonIntervalElapsed + CONTACT_DISTANCE_TOLERANCE
                < BOSS_SUMMON_INTERVAL_SECONDS) {
            return;
        }

        if (summonBossMinions(boss)) {
            bossSummonIntervalElapsed = 0.0;
        }
    }

    private boolean summonBossMinions(Enemy boss) {
        int aliveMinions = (int) bossSummonedMinions.stream()
                .filter(enemy -> !enemy.isDead())
                .count();
        int availableSlots = MAX_BOSS_MINIONS_ALIVE - aliveMinions;
        if (availableSlots < BOSS_MINIONS_PER_SUMMON) {
            return false;
        }

        WaveConfig config = WaveProgression.getConfig(
                currentWave.getWaveNumber()
        );
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
            double movementSpeed = config.enemySpeed()
                    * minionType.speedMultiplier();
            Enemy minion = new EnemySpawner(
                    maxHealth,
                    movementSpeed,
                    minionType
            ).spawn(List.of(spawnPosition)).get(0);
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
        int candidateCount = BOSS_SUMMON_DIRECTION_COUNT
                * BOSS_SUMMON_RING_COUNT;
        double minimumBossDistance = collisionRadius(boss)
                + minionType.collisionRadius()
                + ENEMY_SEPARATION_GAP
                + BOSS_SUMMON_EXTRA_DISTANCE;
        double ringSpacing = 2.0 * minionType.collisionRadius()
                + ENEMY_SEPARATION_GAP
                + BOSS_SUMMON_EXTRA_DISTANCE;

        for (int offset = 0; offset < candidateCount; offset++) {
            int candidateIndex = (bossSummonPositionSequence + offset)
                    % candidateCount;
            int ringIndex = candidateIndex / BOSS_SUMMON_DIRECTION_COUNT;
            int directionIndex = candidateIndex
                    % BOSS_SUMMON_DIRECTION_COUNT;
            double angle = 2.0 * Math.PI * directionIndex
                    / BOSS_SUMMON_DIRECTION_COUNT;
            double distance = minimumBossDistance + ringIndex * ringSpacing;
            Position bossPosition = boss.getPosition();
            Position candidate = new Position(
                    bossPosition.x() + Math.cos(angle) * distance,
                    bossPosition.y() + Math.sin(angle) * distance
            );

            if (isValidBossMinionSpawnPosition(
                    candidate,
                    minionType,
                    pendingMinions
            )) {
                bossSummonPositionSequence = (candidateIndex + 1)
                        % candidateCount;
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
        double playerDistance = Math.hypot(
                candidate.x() - playerPosition.x(),
                candidate.y() - playerPosition.y()
        );
        if (playerDistance + CONTACT_DISTANCE_TOLERANCE
                < PLAYER_COLLISION_RADIUS + minionType.collisionRadius()) {
            return false;
        }

        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double enemyDistance = Math.hypot(
                    candidate.x() - enemyPosition.x(),
                    candidate.y() - enemyPosition.y()
            );
            double minimumDistance = minionType.collisionRadius()
                    + collisionRadius(enemy)
                    + ENEMY_SEPARATION_GAP;
            if (enemyDistance + CONTACT_DISTANCE_TOLERANCE < minimumDistance) {
                return false;
            }
        }

        for (Enemy pendingMinion : pendingMinions) {
            Position pendingPosition = pendingMinion.getPosition();
            double pendingDistance = Math.hypot(
                    candidate.x() - pendingPosition.x(),
                    candidate.y() - pendingPosition.y()
            );
            double minimumDistance = minionType.collisionRadius()
                    + collisionRadius(pendingMinion)
                    + ENEMY_SEPARATION_GAP;
            if (pendingDistance + CONTACT_DISTANCE_TOLERANCE < minimumDistance) {
                return false;
            }
        }

        return true;
    }

    private Enemy findLivingCurrentWaveEnemy(EnemyType type) {
        if (currentWave == null) {
            return null;
        }

        return currentWave.getEnemies().stream()
                .filter(enemy -> enemy.getType() == type)
                .filter(enemy -> !enemy.isDead())
                .findFirst()
                .orElse(null);
    }

    private boolean isCurrentWave(int waveNumber) {
        return currentWave != null
                && currentWave.getWaveNumber() == waveNumber;
    }

    private double enemyMovementSpeedMultiplier(Enemy enemy) {
        if (isCurrentWave(5)
                && miniBossChargeRemaining > 0.0
                && enemy.getType() == EnemyType.MINIBOSS
                && currentWave.getEnemies().stream()
                        .anyMatch(waveEnemy -> waveEnemy == enemy)) {
            return MINIBOSS_CHARGE_SPEED_MULTIPLIER;
        }

        if (enemy == enragedMiniBoss) {
            return MINIBOSS_ENRAGE_SPEED_MULTIPLIER;
        }

        return 1.0;
    }

    private void updateEnemyMovement(double effectiveDelta) {
        Position playerPosition = world.getPlayer().getPosition();

        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double deltaToPlayerX = playerPosition.x() - enemyPosition.x();
            double deltaToPlayerY = playerPosition.y() - enemyPosition.y();
            double distanceToPlayer = Math.hypot(deltaToPlayerX, deltaToPlayerY);
            double contactDistance = playerEnemyContactDistance(enemy);
            double maximumMovement = distanceToPlayer - contactDistance;
            if (isWithinContactDistance(distanceToPlayer, enemy)) {
                continue;
            }

            Position direction = enemy.calculateDesiredDirection(playerPosition);
            double desiredMovement = enemy.getMovementSpeed()
                    * enemyMovementSpeedMultiplier(enemy)
                    * effectiveDelta;
            double actualMovement = Math.min(desiredMovement, maximumMovement);
            double movementX = direction.x() * actualMovement;
            double movementY = direction.y() * actualMovement;
            Position candidatePosition = createMovementCandidate(
                    enemyPosition,
                    movementX,
                    movementY
            );

            if (preservesEnemySeparation(enemy, enemyPosition, candidatePosition)) {
                world.moveEnemyBy(enemy, movementX, movementY);
            }
        }
    }

    private Position createMovementCandidate(
            Position currentPosition,
            double movementX,
            double movementY
    ) {
        double candidateX = Math.max(
                0.0,
                Math.min(world.getWidth(), currentPosition.x() + movementX)
        );
        double candidateY = Math.max(
                0.0,
                Math.min(world.getHeight(), currentPosition.y() + movementY)
        );
        return new Position(candidateX, candidateY);
    }

    private boolean preservesEnemySeparation(
            Enemy movingEnemy,
            Position currentPosition,
            Position candidatePosition
    ) {
        for (Enemy otherEnemy : world.getEnemies()) {
            if (otherEnemy == movingEnemy || otherEnemy.isDead()) {
                continue;
            }

            Position otherPosition = otherEnemy.getPosition();
            double currentDistance = Math.hypot(
                    currentPosition.x() - otherPosition.x(),
                    currentPosition.y() - otherPosition.y()
            );
            double candidateDistance = Math.hypot(
                    candidatePosition.x() - otherPosition.x(),
                    candidatePosition.y() - otherPosition.y()
            );
            double minimumAllowedDistance = Math.min(
                    enemySeparationDistance(movingEnemy, otherEnemy),
                    currentDistance
            );

            if (candidateDistance < minimumAllowedDistance) {
                return false;
            }
        }

        return true;
    }

    private void applyEnemyContactDamage() {
        Player player = world.getPlayer();
        if (player.getHealth().isDead() || playerHitInvulnerabilityRemaining > 0.0) {
            return;
        }

        Position playerPosition = player.getPosition();
        int enemiesInContact = 0;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double deltaX = enemyPosition.x() - playerPosition.x();
            double deltaY = enemyPosition.y() - playerPosition.y();
            double distance = Math.hypot(deltaX, deltaY);

            if (isWithinContactDistance(distance, enemy)) {
                enemiesInContact++;
            }
        }

        if (enemiesInContact == 0) {
            return;
        }

        int damage = ENEMY_CONTACT_DAMAGE * enemiesInContact;
        player.getHealth().takeDamage(damage);
        playerHitInvulnerabilityRemaining = PLAYER_HIT_INVULNERABILITY_SECONDS;
    }

    private void updateWeapon(double effectiveDelta) {
        if (weapon.isEmpty()) {
            return;
        }

        Weapon activeWeapon = weapon.get();
        activeWeapon.update(effectiveDelta);
        activeWeapon.attack(
                world.getPlayer().getPosition(),
                world.getEnemies()
        ).ifPresent(request -> world.addProjectile(createProjectile(request)));
    }

    private static Projectile createProjectile(ProjectileSpawnRequest request) {
        return new Projectile(
                request.origin(),
                request.directionX(),
                request.directionY(),
                request.damage(),
                request.speed()
        );
    }

    private void advanceWaveIfCompleted() {
        if (currentWave == null || !currentWave.isCompleted()) {
            return;
        }

        runStatistics.recordWaveCompleted();
        world.clearProjectiles();
        if (currentWave.getWaveNumber() >= WaveProgression.MAX_WAVES) {
            currentUpgradeSession = null;
            runState = RunState.VICTORY;
            resetEliteEncounterState();
            return;
        }

        if (experienceProgression.hasPendingLevelUp()) {
            currentUpgradeSession = createUpgradeChoiceSession();
            runState = RunState.UPGRADE_SELECTION;
            return;
        }

        startNextWave();
    }

    private boolean completeFinalWaveIfNeeded() {
        if (!isCurrentWave(WaveProgression.MAX_WAVES)
                || !currentWave.isCompleted()) {
            return false;
        }

        advanceWaveIfCompleted();
        return runState == RunState.VICTORY;
    }

    private UpgradeChoiceSession createUpgradeChoiceSession() {
        return new UpgradeChoiceSession(upgradeCatalog, upgradeRandom);
    }

    private void startNextWave() {
        if (currentWave == null
                || currentWave.getWaveNumber() >= WaveProgression.MAX_WAVES) {
            throw new IllegalStateException("A next wave is not available");
        }

        int nextWaveNumber = currentWave.getWaveNumber() + 1;
        WaveConfig nextConfig = WaveProgression.getConfig(nextWaveNumber);
        List<Position> spawnPositions = createSpawnPositions(nextConfig.enemyCount());
        Wave nextWave = WaveFactory.createWave(nextWaveNumber, spawnPositions);

        world.replaceEnemies(nextWave.getEnemies());
        currentWave = nextWave;
        resetEliteEncounterState();
        runState = RunState.ACTIVE_WAVE;
    }

    private void resetEliteEncounterState() {
        miniBossChargeIntervalElapsed = 0.0;
        miniBossChargeRemaining = 0.0;
        enragedMiniBoss = null;
        bossSummonIntervalElapsed = 0.0;
        bossMinionTypeSequence = 0;
        bossSummonPositionSequence = 0;
        bossSummonedMinions.clear();
    }

    private List<Position> createSpawnPositions(int enemyCount) {
        double margin = Math.min(
                SPAWN_MARGIN,
                Math.min(world.getWidth(), world.getHeight()) / 4.0
        );
        double left = margin;
        double right = world.getWidth() - margin;
        double top = margin;
        double bottom = world.getHeight() - margin;
        double horizontalLength = right - left;
        double verticalLength = bottom - top;
        double perimeter = 2.0 * (horizontalLength + verticalLength);

        List<Position> positions = new ArrayList<>(enemyCount);
        for (int index = 0; index < enemyCount; index++) {
            double distance = perimeter * index / enemyCount;

            if (distance < horizontalLength) {
                positions.add(new Position(left + distance, top));
            } else if (distance < horizontalLength + verticalLength) {
                positions.add(new Position(
                        right,
                        top + distance - horizontalLength
                ));
            } else if (distance < 2.0 * horizontalLength + verticalLength) {
                positions.add(new Position(
                        right - distance + horizontalLength + verticalLength,
                        bottom
                ));
            } else {
                positions.add(new Position(
                        left,
                        bottom - distance + 2.0 * horizontalLength + verticalLength
                ));
            }
        }

        return positions;
    }

    private void updateProjectileMovementAndCollisions(double effectiveDelta) {
        for (Projectile projectile : List.copyOf(world.getProjectiles())) {
            double distance = projectile.getMovementSpeed() * effectiveDelta;
            world.moveProjectileBy(
                    projectile,
                    projectile.getDirectionX() * distance,
                    projectile.getDirectionY() * distance
            );

            if (isOutsideWorld(projectile.getPosition())) {
                world.removeProjectile(projectile);
                continue;
            }

            Enemy hitEnemy = findFirstCollidingEnemy(projectile);
            if (hitEnemy != null) {
                boolean wasAlive = !hitEnemy.isDead();
                hitEnemy.takeDamage(projectile.getDamage());
                if (wasAlive && hitEnemy.isDead()) {
                    int experienceReward = hitEnemy.getType().experienceReward();
                    experienceProgression.addExperience(experienceReward);
                    runStatistics.recordEnemyDefeated();
                    runStatistics.recordExperienceGained(experienceReward);
                }
                world.removeProjectile(projectile);
            }
        }
    }

    private Enemy findFirstCollidingEnemy(Projectile projectile) {
        Position projectilePosition = projectile.getPosition();
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            Position enemyPosition = enemy.getPosition();
            double deltaX = enemyPosition.x() - projectilePosition.x();
            double deltaY = enemyPosition.y() - projectilePosition.y();
            double distance = Math.hypot(deltaX, deltaY);
            if (distance <= projectileEnemyCollisionDistance(enemy)
                    + CONTACT_DISTANCE_TOLERANCE) {
                return enemy;
            }
        }

        return null;
    }

    private boolean isOutsideWorld(Position position) {
        return position.x() < 0.0 || position.x() > world.getWidth()
                || position.y() < 0.0 || position.y() > world.getHeight();
    }

    private static double playerEnemyContactDistance(Enemy enemy) {
        return PLAYER_COLLISION_RADIUS + collisionRadius(enemy);
    }

    private static double enemySeparationDistance(Enemy firstEnemy, Enemy secondEnemy) {
        return collisionRadius(firstEnemy)
                + collisionRadius(secondEnemy)
                + ENEMY_SEPARATION_GAP;
    }

    private static double projectileEnemyCollisionDistance(Enemy enemy) {
        return PROJECTILE_COLLISION_RADIUS + collisionRadius(enemy);
    }

    private static double collisionRadius(Enemy enemy) {
        EnemyType enemyType = enemy.getType();
        return enemyType.collisionRadius();
    }

    private static boolean isWithinContactDistance(double distance, Enemy enemy) {
        return distance <= playerEnemyContactDistance(enemy)
                + CONTACT_DISTANCE_TOLERANCE;
    }
}
