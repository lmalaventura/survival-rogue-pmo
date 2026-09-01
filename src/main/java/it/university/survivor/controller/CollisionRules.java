package it.university.survivor.controller;

import it.university.survivor.model.Enemy;

final class CollisionRules {

    static final double PLAYER_RADIUS = 8.0;
    static final double PROJECTILE_RADIUS = 3.0;
    static final double ENEMY_GAP = 1.0;
    static final double TOLERANCE = 1.0e-9;

    private CollisionRules() {
    }

    static double playerEnemyDistance(Enemy enemy) {
        return PLAYER_RADIUS + enemy.getType().collisionRadius();
    }

    static double enemySeparationDistance(Enemy firstEnemy, Enemy secondEnemy) {
        return firstEnemy.getType().collisionRadius()
                + secondEnemy.getType().collisionRadius()
                + ENEMY_GAP;
    }

    static double projectileEnemyDistance(Enemy enemy) {
        return PROJECTILE_RADIUS + enemy.getType().collisionRadius();
    }

    static boolean isWithinPlayerContact(double distance, Enemy enemy) {
        return distance <= playerEnemyDistance(enemy) + TOLERANCE;
    }
}
