package it.university.survivor.model.enemy;

import it.university.survivor.model.Enemy;
import java.util.List;
import java.util.Objects;

public final class Wave {

    private final List<Enemy> enemies;

    public Wave(List<Enemy> enemies) {
        Objects.requireNonNull(enemies, "Enemies must not be null");

        if (enemies.isEmpty()) {
            throw new IllegalArgumentException("A wave must contain at least one enemy");
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