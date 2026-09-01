package it.university.survivor.weapon;
public final class WeaponFactory {

    private WeaponFactory() {
    }

    public static Weapon create(WeaponType type) {
        return switch (type) {
            case AUTOMATIC -> createAutomatic();
            case SHOTGUN -> createShotgun();
            case SNIPER -> createSniper();
            case PULSE -> createPulse();
        };
    }

    public static Weapon createAutomatic() {
        return new Weapon(
                new WeaponStats(0.75, 25, 300.0),
                new NearestEnemyAttackStrategy(),
                new SpreadAttackStrategy(),
                5,
                new PatternEvolutionUpgrade(3, 20.0)
        );
    }

    public static Weapon createShotgun() {
        return new Weapon(
                new WeaponStats(1.20, 14, 260.0, 5, 55.0),
                new SpreadAttackStrategy(),
                new SpreadAttackStrategy(),
                5,
                new PatternEvolutionUpgrade(7, 75.0)
        );
    }

    public static Weapon createSniper() {
        return new Weapon(
                new WeaponStats(1.80, 80, 650.0, 3, 0.0),
                new FarthestEnemyAttackStrategy(),
                new BurstAttackStrategy(),
                5
        );
    }

    public static Weapon createPulse() {
        return new Weapon(
                new WeaponStats(1.50, 12, 220.0, 8, 360.0),
                new RadialAttackStrategy(),
                new RadialAttackStrategy(),
                5,
                new PatternEvolutionUpgrade(12, 360.0)
        );
    }
}
