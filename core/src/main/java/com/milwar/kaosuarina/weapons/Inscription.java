package com.milwar.kaosuarina.weapons;

import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;

public interface Inscription {
    /**
     * Ejecutado tras impactar un enemigo. rawDamage = daño aplicado tras defensa.
     */
    void onHit(Player p, Enemy e, int rawDamage);

    /**
     * Multiplicador de daño (1.0 = sin cambio). Aplicado antes de calcular defensa.
     */
    default float damageMult() {
        return 1.0f;
    }

    /**
     * Pierce extra que se suma al de la bala.
     */
    default int extraPierce() {
        return 0;
    }

    /**
     * True si esta inscripción ignora la defensa del enemigo.
     */
    default boolean bypassesDefense() {
        return false;
    }

    /**
     * Nombre corto (max 4 chars) para el HUD.
     */
    String getName();
}
