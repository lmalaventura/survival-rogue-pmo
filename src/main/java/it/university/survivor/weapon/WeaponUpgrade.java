package it.university.survivor.weapon;
public interface WeaponUpgrade {

    WeaponStats applyFlat(WeaponStats currentStats);
    WeaponStats applyPerc(WeaponStats currentStats);
}