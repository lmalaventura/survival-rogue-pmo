package it.university.survivor.weapon;
public class PercentDamageUpgrade implements WeaponUpgrade{
    private final double percentage;

    public PercentDamageUpgrade(double percentage) {
        if (!Double.isFinite(percentage) || percentage <= 0.0) {
            throw new IllegalArgumentException(
                    "La percentuale deve essere finita e positiva."
            );
        }

        this.percentage = percentage;
    }

    @Override
    public WeaponStats apply(WeaponStats stats) {
        double newDamage = stats.getDamage() * (1.0 + percentage);

        return new WeaponStats(
                stats.getCooldownSeconds(),
                Math.round(newDamage),
                stats.getProjectileSpeed()
        );
    }
}