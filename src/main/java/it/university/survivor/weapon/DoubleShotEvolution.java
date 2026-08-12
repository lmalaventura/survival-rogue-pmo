package it.university.survivor.weapon;
public class DoubleShotEvolution implements WeaponEvolution {

    @Override
    public boolean canEvolve(Weapon weapon) {
        return weapon.getCurrentStats().getDamage() >= 20;
    }

    @Override
    public Weapon evolve(Weapon weapon) {
        WeaponStats stats = weapon.getCurrentStats();

        WeaponStats evolvedStats = new WeaponStats(
            stats.getCooldownSeconds(),
            stats.getDamage() * 2,
            stats.getProjectileSpeed()
        );

        return new Weapon(
            evolvedStats,
            new NearestEnemyAttackStrategy()
        );
    }
}