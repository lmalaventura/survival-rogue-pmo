package it.university.survivor.weapon;
public class CooldownUpgrade implements WeaponUpgrade {

    private final double reduction;

    public CooldownUpgrade(double reduction) {
        this.reduction = reduction;
    }

    @Override
    public WeaponStats apply(WeaponStats stats) {
        double newCooldown =
                Math.max(0.05, stats.getCooldownSeconds() - reduction);

        return new WeaponStats(
            newCooldown,
            stats.getDamage(),
            stats.getProjectileSpeed()
        );
    }
}