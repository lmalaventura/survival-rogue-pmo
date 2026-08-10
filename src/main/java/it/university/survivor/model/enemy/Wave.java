package it.university.survivor.model.enemy;

import java.util.List;
import java.util.Objects;

public final class Wave {

    private final List<Enemy> enemies;

    public Wave(List<Enemy> enemies) {
        Objects.requireNonNull(enemies, "Enemies must not be null");

        if (enemies.size() != 3) {
            throw new IllegalArgumentException("A wave must contain exactly 3 enemies");
        }

        this.enemies = List.copyOf(enemies);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public boolean isCompleted() {
        return enemies.stream().allMatch(Enemy::isDead);
    }
}