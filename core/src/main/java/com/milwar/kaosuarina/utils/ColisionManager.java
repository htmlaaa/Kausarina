package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.entities.*;

public class ColisionManager {

    public static int comprobarBalasVsEnemigos(PoolBalas poolBalas, PoolEnemigos poolEnemigos) {
        int muertes = 0;

        for (Bala bala : poolBalas.balas) {
            if (!bala.active) continue;

            for (Enemy enemy : poolEnemigos.getEnemigos()) {
                if (!enemy.active) continue;

                if (colisionan(bala.position, Constants.BALA_RADIO, enemy.position, enemy.getRadio())) {
                    boolean estabaVivo = enemy.active;
                    enemy.recibirDanio(bala.damage);
                    if (estabaVivo && !enemy.active) muertes++;

                    // Notificar a la bala; si deja de estar activa, pasar al siguiente enemigo
                    boolean sigueActiva = bala.onHit();
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

    private static boolean colisionan(Vector2 p1, float r1, Vector2 p2, float r2) {
        return p1.dst(p2) < (r1 + r2);
    }
}
