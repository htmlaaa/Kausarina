package com.milwar.kaosuarina.utils;

public enum DamageType {
    FISICO,
    MAGICO,
    A_DISTANCIA,
    FUEGO,
    VENENO,
    /**
     * Ignora el 50% de defensa y resistencia mágica del objetivo.
     */
    CAOS,
    /**
     * Variante primordial del caos. Igual que CAOS: ignora el 50% de defensa y resMag.
     * Usado por TOMO_CAOS (WeaponSkill).
     */
    CAOS_PRIMORDIAL
}
