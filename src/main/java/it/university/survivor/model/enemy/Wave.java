package it.university.survivor.model.enemy;

import it.university.survivor.model.Enemy;

import java.util.List;
import java.util.Objects;

public final class Wave {

    private final int waveNumber;
    private final List<Enemy> enemies;

    public Wave(int waveNumber, List<Enemy> enemies) {
        if (waveNumber <= 0) {
            throw new IllegalArgumentException(
                    "Wave number must be greater than zero"
            );
        }

        Objects.requireNonNull(enemies, "Enemies must not be null");

        if (enemies.isEmpty()) {
            throw new IllegalArgumentException(
                    "A wave must contain at least one enemy"
            );
        }

        this.waveNumber = waveNumber;
        this.enemies = List.copyOf(enemies);
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public boolean isCompleted() {
        return enemies.stream().allMatch(Enemy::isDead);
    }
}