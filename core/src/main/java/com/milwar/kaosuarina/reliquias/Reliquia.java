package com.milwar.kaosuarina.reliquias;

import com.milwar.kaosuarina.entities.Player;

/**
 * Interfaz base para las Reliquias — pasivas únicas por personaje siempre activas.
 * Sustituye al antiguo CoreEffect. Los callbacks de maná se añadirán en S2-10+.
 */
public interface Reliquia {

    /** Llamado cuando el jugador recibe daño (antes de aplicarlo). */
    void onDamageReceived(Player player, int damage);

    /** Llamado cuando el jugador mata un enemigo. */
    void onKill(Player player);

    /** Llamado cada frame con delta time. */
    void onUpdate(Player player, float delta);

    /** Multiplicador de cooldown de disparo (1.0 = sin efecto). */
    default float getCooldownMultiplier() {
        return 1f;
    }

    /** Reducción flat del daño recibido (0 = sin efecto). */
    default float getDamageReduction() {
        return 0f;
    }

    /** Número de rebotes que tienen las balas al dispararse (0 = sin rebote). */
    default int getBounces() {
        return 0;
    }
}
