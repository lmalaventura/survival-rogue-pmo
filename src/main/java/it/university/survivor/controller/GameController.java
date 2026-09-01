package it.university.survivor.controller;

import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Position;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.UpgradeCatalog;
import it.university.survivor.model.UpgradeChoiceSession;
import it.university.survivor.model.UpgradeOption;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveFactory;
import it.university.survivor.model.enemy.WaveProgression;
import it.university.survivor.weapon.Weapon;
import it.university.survivor.weapon.WeaponType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class GameController {

    private static final double MAX_DELTA_SECONDS = 0.1;
    private static final double SPAWN_MARGIN = 24.0;

    private final GameWorld world;
    private final ExperienceProgression experienceProgression;
    private final RunStatistics runStatistics;
    private final EnumMap<WeaponType, Weapon> weapons;
    private final MovementEngine movementEngine;
    private final CombatEngine combatEngine;
    private final EliteEncounter eliteEncounter;
    private final UpgradeManager upgradeManager;

    private Wave currentWave;
    private RunState runState = RunState.ACTIVE_WAVE;
    private UpgradeChoiceSession currentUpgradeSession;

    public GameController(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Map<WeaponType, Weapon> initialWeapons,
            Wave initialWave
    ) {
        this(
                world,
                experienceProgression,
                runStatistics,
                initialWeapons,
                Objects.requireNonNull(initialWave, "Initial wave must not be null"),
                new UpgradeCatalog(),
                new Random()
        );
    }

    GameController(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Map<WeaponType, Weapon> initialWeapons,
            Wave initialWave,
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
        Objects.requireNonNull(initialWeapons, "Initial weapons must not be null");
        this.weapons = new EnumMap<>(WeaponType.class);
        for (Map.Entry<WeaponType, Weapon> entry : initialWeapons.entrySet()) {
            WeaponType type = Objects.requireNonNull(
                    entry.getKey(),
                    "Weapon type must not be null"
            );
            Weapon weapon = Objects.requireNonNull(
                    entry.getValue(),
                    "Weapon must not be null"
            );
            weapons.put(type, weapon);
        }
        this.currentWave = initialWave;
        movementEngine = new MovementEngine(world);
        combatEngine = new CombatEngine(
                world,
                experienceProgression,
                runStatistics,
                weapons
        );
        eliteEncounter = new EliteEncounter(world);
        upgradeManager = new UpgradeManager(
                world,
                experienceProgression,
                runStatistics,
                weapons,
                upgradeCatalog,
                upgradeRandom
        );
    }

    ExperienceProgression getExperienceProgression() {
        return experienceProgression;
    }

    RunStatistics getRunStatistics() {
        return runStatistics;
    }

    public Map<WeaponType, Weapon> getWeapons() {
        return Collections.unmodifiableMap(new EnumMap<>(weapons));
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


    public void selectUpgradeOption(UpgradeOption option) {
        UpgradeChoiceSession session = requireUpgradeSelection();
        boolean morePendingLevels = upgradeManager.applyChoice(session, option);
        if (morePendingLevels) {
            currentUpgradeSession = upgradeManager.createSession();
            return;
        }

        currentUpgradeSession = null;
        startNextWave();
    }

    public void rerollUpgradeChoices() {
        upgradeManager.reroll(requireUpgradeSelection());
    }

    public void setDirectionActive(MovementDirection direction, boolean active) {
        movementEngine.setDirectionActive(direction, active);
    }

    public void update(double deltaSeconds) {
        if (runState != RunState.ACTIVE_WAVE) {
            return;
        }
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0.0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        if (world.getPlayer().getHealth().isDead()) {
            endRun(RunState.DEFEAT);
            return;
        }
        if (deltaSeconds == 0.0 || completeFinalWaveIfNeeded()) {
            return;
        }

        double effectiveDelta = Math.min(deltaSeconds, MAX_DELTA_SECONDS);
        runStatistics.addElapsedTime(effectiveDelta);

        eliteEncounter.updateBeforeMovement(currentWave, effectiveDelta);
        combatEngine.updatePlayerInvulnerability(effectiveDelta);
        movementEngine.updatePlayer(effectiveDelta);
        movementEngine.updateEnemies(effectiveDelta, currentWave, eliteEncounter);
        combatEngine.updateRangedAttacks(effectiveDelta);
        combatEngine.updateWeapons(effectiveDelta);
        combatEngine.updateProjectiles(effectiveDelta);

        if (completeFinalWaveIfNeeded()) {
            return;
        }

        eliteEncounter.updateBossSummoning(currentWave, effectiveDelta);
        combatEngine.applyEnemyContactDamage();
        if (world.getPlayer().getHealth().isDead()) {
            endRun(RunState.DEFEAT);
            return;
        }
        advanceWaveIfCompleted();
    }

    private UpgradeChoiceSession requireUpgradeSelection() {
        if (runState != RunState.UPGRADE_SELECTION || currentUpgradeSession == null) {
            throw new IllegalStateException("No upgrade selection is active");
        }
        return currentUpgradeSession;
    }

    private void advanceWaveIfCompleted() {
        if (currentWave == null || !currentWave.isCompleted()) {
            return;
        }

        runStatistics.recordWaveCompleted();
        world.clearProjectiles();
        if (currentWave.getWaveNumber() >= WaveProgression.MAX_WAVES) {
            currentUpgradeSession = null;
            endRun(RunState.VICTORY);
            return;
        }

        if (experienceProgression.hasPendingLevelUp()) {
            currentUpgradeSession = upgradeManager.createSession();
            runState = RunState.UPGRADE_SELECTION;
        } else {
            startNextWave();
        }
    }

    private boolean completeFinalWaveIfNeeded() {
        if (currentWave == null
                || currentWave.getWaveNumber() != WaveProgression.MAX_WAVES
                || !currentWave.isCompleted()) {
            return false;
        }
        advanceWaveIfCompleted();
        return runState == RunState.VICTORY;
    }

    private void startNextWave() {
        if (currentWave == null
                || currentWave.getWaveNumber() >= WaveProgression.MAX_WAVES) {
            throw new IllegalStateException("A next wave is not available");
        }

        int nextWaveNumber = currentWave.getWaveNumber() + 1;
        WaveConfig nextConfig = WaveProgression.getConfig(nextWaveNumber);
        Wave nextWave = WaveFactory.createWave(
                nextWaveNumber,
                createSpawnPositions(nextConfig.enemyCount())
        );

        world.replaceEnemies(nextWave.getEnemies());
        currentWave = nextWave;
        eliteEncounter.reset();
        runState = RunState.ACTIVE_WAVE;
    }

    private void endRun(RunState finalState) {
        runState = finalState;
        eliteEncounter.reset();
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
                positions.add(new Position(right, top + distance - horizontalLength));
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


}
