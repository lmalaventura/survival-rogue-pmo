package it.university.survivor.weapon;

public class FlatCooldownUpgrade implements WeaponUpgrade {

    private static final double MIN_COOLDOWN_SECONDS = 0.05;

    private final double reduction;

    public FlatCooldownUpgrade(double reduction) {
        if (!Double.isFinite(reduction) || reduction <= 0.0) {
            throw new IllegalArgumentException(
                    "La reduction deve essere finita e positiva."
            );
        }

        this.reduction = reduction;
    }

    @Override
    public WeaponStats apply(WeaponStats stats) {
        return new WeaponStats(
                Math.max(
                        MIN_COOLDOWN_SECONDS,
                        stats.getCooldownSeconds() - reduction
                ),
                stats.getDamage(),
                stats.getProjectileSpeed()
        );
    }
}

