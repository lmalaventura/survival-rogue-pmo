package it.university.survivor.controller;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.Projectile;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveConfig;
import it.university.survivor.model.enemy.WaveFactory;
import it.university.survivor.model.enemy.WaveProgression;
import it.university.survivor.weapon.ProjectileSpawnRequest;
import it.university.survivor.weapon.Weapon;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GameController {

    private static final double MAX_DELTA_SECONDS = 0.1;
    private static final double ENEMY_CONTACT_DISTANCE = 14.0;
    private static final double ENEMY_MIN_SEPARATION = 13.0;
    private static final double PLAYER_MOVEMENT_SUBSTEP_DISTANCE = 1.0;
    private static final double PROJECTILE_ENEMY_COLLISION_DISTANCE = 9.0;
    private static final double CONTACT_DISTANCE_TOLERANCE = 1.0e-9;
    private static final int ENEMY_CONTACT_DAMAGE = 10;
    private static final int ENEMY_EXPERIENCE_REWARD = 25;
    private static final double PLAYER_HIT_INVULNERABILITY_SECONDS = 0.5;
    private static final int MAX_WAVES = 5;
    private static final double SPAWN_MARGIN = 24.0;

    private final GameWorld world;
    private final ExperienceProgression experienceProgression;
    private final RunStatistics runStatistics;
    private final Optional<Weapon> weapon;
    private Wave currentWave;
    private RunState runState;
    private final EnumSet<MovementDirection> activeDirections =
            EnumSet.noneOf(MovementDirection.class);
    private double playerHitInvulnerabilityRemaining = 0.0;

    public GameController(GameWorld world) {
        this(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                Optional.empty(),
                null
        );
    }

    public GameController(GameWorld world, Wave initialWave) {
        this(
                world,
                new ExperienceProgression(),
                new RunStatistics(),
                Optional.empty(),
                Objects.requireNonNull(initialWave, "Initial wave must not be null")
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
                null
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
                null
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
                Objects.requireNonNull(initialWave, "Initial wave must not be null")
        );
    }

    private GameController(
            GameWorld world,
            ExperienceProgression experienceProgression,
            RunStatistics runStatistics,
            Optional<Weapon> weapon,
            Wave currentWave
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
        this.currentWave = currentWave;
        this.runState = RunState.ACTIVE_WAVE;
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
            return;
        }
        if (deltaSeconds == 0.0) {
            return;
        }

        double effectiveDelta = Math.min(deltaSeconds, MAX_DELTA_SECONDS);
        updatePlayerHitInvulnerability(effectiveDelta);
        updatePlayerMovement(effectiveDelta);
        updateEnemyMovement(effectiveDelta);
        updateWeapon(effectiveDelta);
        updateProjectileMovementAndCollisions(effectiveDelta);
        applyEnemyContactDamage();

        if (world.getPlayer().getHealth().isDead()) {
            runState = RunState.DEFEAT;
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
                    ENEMY_CONTACT_DISTANCE,
                    currentDistance
            );

            if (candidateDistance < minimumAllowedDistance) {
                return false;
            }
        }

        return true;
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
            double maximumMovement = distanceToPlayer - ENEMY_CONTACT_DISTANCE;
            if (isWithinContactDistance(distanceToPlayer)) {
                continue;
            }

            Position direction = enemy.calculateDesiredDirection(playerPosition);
            double desiredMovement = enemy.getMovementSpeed() * effectiveDelta;
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
                    ENEMY_MIN_SEPARATION,
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

            if (isWithinContactDistance(distance)) {
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
        if (currentWave.getWaveNumber() >= MAX_WAVES) {
            world.clearProjectiles();
            runState = RunState.VICTORY;
            return;
        }

        int nextWaveNumber = currentWave.getWaveNumber() + 1;
        WaveConfig nextConfig = WaveProgression.getConfig(nextWaveNumber);
        List<Position> spawnPositions = createSpawnPositions(nextConfig.enemyCount());
        Wave nextWave = WaveFactory.createWave(nextWaveNumber, spawnPositions);

        world.clearProjectiles();
        world.replaceEnemies(nextWave.getEnemies());
        currentWave = nextWave;
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
                    experienceProgression.addExperience(ENEMY_EXPERIENCE_REWARD);
                    runStatistics.recordEnemyDefeated();
                    runStatistics.recordExperienceGained(ENEMY_EXPERIENCE_REWARD);
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
            if (distance <= PROJECTILE_ENEMY_COLLISION_DISTANCE
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

    private static boolean isWithinContactDistance(double distance) {
        return distance <= ENEMY_CONTACT_DISTANCE + CONTACT_DISTANCE_TOLERANCE;
    }
}
