package it.university.survivor.weapon;

public class PatternEvolutionUpgrade
        implements WeaponUpgrade {

    private final int projectileCount;
    private final double spreadDegrees;

    public PatternEvolutionUpgrade(
            int projectileCount,
            double spreadDegrees) {

        if (projectileCount <= 0) {
            throw new IllegalArgumentException(
                    "Il numero di proiettili deve essere positivo."
            );
        }

        if (!Double.isFinite(spreadDegrees)
                || spreadDegrees < 0.0
                || spreadDegrees > 360.0) {

            throw new IllegalArgumentException(
                    "Lo spread non è valido."
            );
        }

        this.projectileCount = projectileCount;
        this.spreadDegrees = spreadDegrees;
    }

    @Override
    public WeaponStats apply(
            WeaponStats currentStats) {

        return currentStats.withProjectilePattern(
                projectileCount,
                spreadDegrees
        );
    }
}