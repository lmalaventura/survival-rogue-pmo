package it.university.survivor.weapon;
public class DamageUpgrade implements WeaponUpgrade {

    private final int bonusDamage;

    public DamageUpgrade(int bonusDamage) {
        if (bonusDamage <= 0) {
            throw new IllegalArgumentException("Il bonus deve essere positivo.");
        }

        this.bonusDamage = bonusDamage;
    }

    @Override
    public WeaponStats applyFlat(WeaponStats stats) {
        return new WeaponStats(
            stats.getCooldownSeconds(),
            stats.getDamage() + this.bonusDamage,
            stats.getProjectileSpeed()
        );
    }
    @Override
    public WeaponStats applyPerc(WeaponStats stats){
        return new WeaponStats(
            stats.getCooldownSeconds(), 
            stats.getDamage()*(1+this.bonusDamage),
            stats.getProjectileSpeed()) ;               
    }
}