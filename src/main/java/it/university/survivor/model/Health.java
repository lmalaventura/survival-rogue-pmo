package it.university.survivor.model;

public final class Health {

    private final int maxHealth;
    private int currentHealth;

    public Health(int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Maximum health must be greater than zero");
        }

        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage must not be negative");
        }
        if (currentHealth == 0) {
            return;
        }

        currentHealth = Math.max(0, currentHealth - amount);
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing must not be negative");
        }
        if (currentHealth == 0 || currentHealth == maxHealth) {
            return;
        }

        int missingHealth = maxHealth - currentHealth;
        currentHealth += Math.min(amount, missingHealth);
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public boolean isDead() {
        return currentHealth == 0;
    }
}
