package it.university.survivor.model;

public final class Health {

    private int maxHealth;
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

    public void increaseMaxHealth(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Maximum health increase must be greater than zero");
        }

        final int increasedMaxHealth;
        final int increasedCurrentHealth;
        try {
            increasedMaxHealth = Math.addExact(maxHealth, amount);
            increasedCurrentHealth = Math.addExact(currentHealth, amount);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Maximum health increase is too large", exception);
        }

        maxHealth = increasedMaxHealth;
        currentHealth = increasedCurrentHealth;
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

    public boolean isDead() {
        return currentHealth == 0;
    }
}
