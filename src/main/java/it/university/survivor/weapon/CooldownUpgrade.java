package it.university.survivor.weapon;
public class CooldownUpgrade implements WeaponUpgrade {

    private final double reduction;

    public CooldownUpgrade(double reduction) {
        this.reduction = reduction;
    }

    @Override
    public WeaponStats applyFlat(WeaponStats stats) {
        return new WeaponStats(
            stats.getCooldownSeconds() - this.reduction,
            stats.getDamage(),
            stats.getProjectileSpeed()
        );
    }
    @Override
    public WeaponStats applyPerc(WeaponStats stats){

        return new WeaponStats(
            stats.getCooldownSeconds()*(1-this.reduction), 
            stats.getDamage(),
            stats.getProjectileSpeed()) ;               
    }
}