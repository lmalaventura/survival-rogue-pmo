package it.university.survivor.weapon;
public class PercentCooldownUpgrade implements WeaponUpgrade {

    private final double percentage;

    public PercentCooldownUpgrade(double percentage) {
        if (!Double.isFinite(percentage) || percentage <= 0.0) {
            throw new IllegalArgumentException(
                    "La percentuale deve essere finita e positiva."
            );
        }

        if (percentage >= 1.0) {
            throw new IllegalArgumentException(
                    "La percentuale deve essere inferiore a 1."
            );
        }

        this.percentage = percentage;
    }

    @Override
    public WeaponStats apply(WeaponStats stats) {
        return new WeaponStats(
                stats.getCooldownSeconds() * (1.0 - percentage),
                stats.getDamage(),
                stats.getProjectileSpeed()
        );
    }
}