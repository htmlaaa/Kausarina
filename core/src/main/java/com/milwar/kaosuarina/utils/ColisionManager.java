package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.entities.*;
import com.milwar.kaosuarina.utils.DamageType;

public class ColisionManager {

    private static final float RADIO_REBOTE = 200f;

    public static int comprobarBalasVsEnemigos(PoolBalas poolBalas, PoolEnemigos poolEnemigos) {
        int muertes = 0;

        for (Bala bala : poolBalas.balas) {
            if (!bala.active) continue;

            for (Enemy enemy : poolEnemigos.getEnemigos()) {
                if (!enemy.active) continue;

                if (colisionan(bala.position, Constants.BALA_RADIO, enemy.position, enemy.getRadio())) {
                    boolean estabaVivo = enemy.active;
                    enemy.recibirDanio(calcularDaño(bala.damage, bala.damageType, enemy));
                    if (estabaVivo && !enemy.active) muertes++;

                    boolean sigueActiva = bala.onHit();

                    if (!sigueActiva && bala.rebotesRestantes > 0) {
                        // Intentar rebotar hacia el enemigo activo más cercano (excluyendo al golpeado)
                        Enemy objetivo = enemigoCercano(poolEnemigos, bala.position, RADIO_REBOTE, enemy);
                        if (objetivo != null) {
                            bala.rebotar(objetivo.position.x, objetivo.position.y);
                            sigueActiva = true;
                        }
                    }

                    if (!sigueActiva) break;
                }
            }
        }
        return muertes;
    }

    public static boolean comprobarJugadorVsEnemigos(Vector2 posJugador, float radioJugador,
                                                     PoolEnemigos poolEnemigos) {
        for (Enemy e : poolEnemigos.getEnemigos()) {
            if (e.active && colisionan(posJugador, radioJugador, e.position, e.getRadio()))
                return true;
        }
        return false;
    }

    public static void comprobarBalasEnemigas(PoolBalasEnemigas poolBalasEnemigas, Player player) {
        if (!player.isAlive()) return;

        for (BalaEnemiga bala : poolBalasEnemigas.balas) {
            if (!bala.active) continue;
            if (player.position.dst(bala.position) < player.getRadio() + Constants.BALA_ENEMIGA_RADIO) {
                player.recibirDanio(bala.getDamage());
                bala.deactivate();
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Aplica la reducción de defensa/resistencia según el tipo de daño. Mínimo 1. */
    private static int calcularDaño(int raw, DamageType tipo, Enemy enemy) {
        float reduccion;
        switch (tipo) {
            case FISICO:      reduccion = enemy.defensa; break;
            case MAGICO:      reduccion = enemy.resistenciaMagica; break;
            case CAOS:        reduccion = enemy.defensa * 0.5f + enemy.resistenciaMagica * 0.5f; break;
            default:          reduccion = 0f; break;  // A_DISTANCIA, FUEGO, VENENO
        }
        return Math.max(1, Math.round(raw - reduccion));
    }

    private static boolean colisionan(Vector2 p1, float r1, Vector2 p2, float r2) {
        return p1.dst(p2) < (r1 + r2);
    }

    private static Enemy enemigoCercano(PoolEnemigos pool, Vector2 origen, float radio, Enemy excluir) {
        // Iteración por índice: for-each anidado sobre el mismo Array<Enemy> corrompe
        // el iterador compartido de LibGDX y lanza NoSuchElementException.
        com.badlogic.gdx.utils.Array<Enemy> lista = pool.getEnemigos();
        Enemy masCercano = null;
        float minDst = radio;
        for (int i = 0; i < lista.size; i++) {
            Enemy e = lista.get(i);
            if (!e.active || e == excluir) continue;
            float dst = origen.dst(e.position);
            if (dst < minDst) {
                minDst = dst;
                masCercano = e;
            }
        }
        return masCercano;
    }
}
