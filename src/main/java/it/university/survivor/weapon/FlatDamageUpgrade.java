package it.university.survivor.weapon;

public class FlatDamageUpgrade implements WeaponUpgrade{
     private final int bonusDamage;

    public FlatDamageUpgrade(int bonusDamage) {
        if (bonusDamage <= 0) {
            throw new IllegalArgumentException("Il bonus deve essere positivo.");
        }

        this.bonusDamage = bonusDamage;
    }

    @Override
    public WeaponStats apply(WeaponStats stats) {
        return new WeaponStats(
                stats.getCooldownSeconds(),
                stats.getDamage() + bonusDamage,
                stats.getProjectileSpeed()
        );
    }
}