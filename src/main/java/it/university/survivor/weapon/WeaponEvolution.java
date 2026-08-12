package it.university.survivor.weapon;
public interface WeaponEvolution {

    boolean canEvolve(Weapon weapon);

    Weapon evolve(Weapon weapon);
}