package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.systems.UpgradeManager;
import com.milwar.kaosuarina.entities.*;

public class ColisionManager {

    private static final float RADIO_REBOTE = 200f;

    public static int comprobarBalasVsEnemigos(Player player, PoolBalas poolBalas, PoolEnemigos poolEnemigos) {
        int muertes = 0;

        for (Bala bala : poolBalas.balas) {
            if (!bala.active) continue;

            for (Enemy enemy : poolEnemigos.getEnemigos()) {
                if (!enemy.active) continue;

                if (colisionan(bala.position, Constants.BALA_RADIO, enemy.position, enemy.getRadio())) {
                    boolean estabaVivo = enemy.active;
                    int dmg = calcularDaño(bala.damage, bala.damageType, enemy);
                    enemy.recibirDanio(dmg);
                    aplicarStatusPorDanio(enemy, bala.damageType);
                    aplicarEfectosOnHit(player, enemy, dmg, bala.damageType);
                    if (estabaVivo && !enemy.active) {
                        muertes++;
                        poolEnemigos.registrarKill(enemy.tipo);
                    }

                    boolean sigueActiva = bala.onHit();

                    if (!sigueActiva && bala.rebotesRestantes > 0) {
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

    /**
     * Contacto jugador-enemigo. Aplica veneno al jugador si el agresor es MALDITO.
     */
    public static int comprobarJugadorVsEnemigos(Player player, PoolEnemigos poolEnemigos) {
        for (Enemy e : poolEnemigos.getEnemigos()) {
            if (!e.active) continue;
            if (colisionan(player.position, player.getRadio(), e.position, e.getRadio())) {
                if (e.tipo == Enemy.Tipo.MALDITO) {
                    player.aplicarVeneno(4f, Constants.MALDITO_POISON_DPS);
                }
                return contactDamagePor(e.tipo);
            }
        }
        return 0;
    }

    /**
     * Procesa las explosiones pendientes de MALDITO muertos (por ataque O por DoT).
     * Llamar una vez por frame en procesarColisiones().
     */
    public static int comprobarExplosionesMALDITO(PoolEnemigos pool) {
        int kills = 0;
        com.badlogic.gdx.utils.Array<Enemy> lista = pool.getEnemigos();
        for (int i = 0; i < lista.size; i++) {
            Enemy e = lista.get(i);
            if (e.debeExplotar) {
                e.debeExplotar = false;
                kills += explocionMaldito(pool, e.position);
            }
        }
        return kills;
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

    // ── Ataques en área ───────────────────────────────────────────────────────

    /**
     * Arco de espada (Caballero). Devuelve kills.
     */
    public static int comprobarMelee(Player player, Vector2 center, float aimAngle, float arcRad,
                                     float radius, int damage, DamageType tipo,
                                     PoolEnemigos pool) {
        int kills = 0;
        com.badlogic.gdx.utils.Array<Enemy> lista = pool.getEnemigos();
        for (int i = 0; i < lista.size; i++) {
            Enemy e = lista.get(i);
            if (!e.active) continue;
            float dx = e.position.x - center.x;
            float dy = e.position.y - center.y;
            if (dx * dx + dy * dy > (radius + e.getRadio()) * (radius + e.getRadio())) continue;
            float enemyAngle = (float) Math.atan2(dy, dx);
            float diff = anguloAbsDiff(enemyAngle, aimAngle);
            if (diff <= arcRad * 0.5f) {
                boolean wasAlive = e.active;
                int dmgDealt = calcularDaño(damage, tipo, e);
                e.recibirDanio(dmgDealt);
                aplicarStatusPorDanio(e, tipo);
                aplicarEfectosOnHit(player, e, dmgDealt, tipo);
                if (wasAlive && !e.active) {
                    kills++;
                    pool.registrarKill(e.tipo);
                }
            }
        }
        return kills;
    }

    /**
     * Explosión circular (Mago pesado). Devuelve kills.
     */
    public static int comprobarBlast(Player player, Vector2 center, float radius,
                                     int damage, DamageType tipo, PoolEnemigos pool) {
        int kills = 0;
        com.badlogic.gdx.utils.Array<Enemy> lista = pool.getEnemigos();
        for (int i = 0; i < lista.size; i++) {
            Enemy e = lista.get(i);
            if (!e.active) continue;
            if (center.dst(e.position) <= radius + e.getRadio()) {
                boolean wasAlive = e.active;
                int dmgDealt = calcularDaño(damage, tipo, e);
                e.recibirDanio(dmgDealt);
                aplicarStatusPorDanio(e, tipo);
                aplicarEfectosOnHit(player, e, dmgDealt, tipo);
                if (wasAlive && !e.active) {
                    kills++;
                    pool.registrarKill(e.tipo);
                }
            }
        }
        return kills;
    }

    /**
     * Devuelve el enemigo activo más cercano dentro de maxRange, o null.
     */
    public static Enemy nearestEnemy(PoolEnemigos pool, Vector2 origin, float maxRange) {
        return enemigoCercano(pool, origin, maxRange, null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Explosión del MALDITO al morir. Sin encadenamiento recursivo.
     */
    private static int explocionMaldito(PoolEnemigos pool, Vector2 center) {
        int kills = 0;
        com.badlogic.gdx.utils.Array<Enemy> lista = pool.getEnemigos();
        for (int i = 0; i < lista.size; i++) {
            Enemy e = lista.get(i);
            if (!e.active) continue;
            if (center.dst(e.position) <= Constants.MALDITO_EXPLOSION_RADIUS + e.getRadio()) {
                boolean wasAlive = e.active;
                e.recibirDanio(calcularDaño(Constants.MALDITO_EXPLOSION_DAMAGE, DamageType.VENENO, e));
                aplicarStatusPorDanio(e, DamageType.VENENO);
                if (wasAlive && !e.active) {
                    kills++;
                    pool.registrarKill(e.tipo);
                }
                // Sin encadenamiento: MALDITO muerto aquí no dispara otra explosión esta pasada
            }
        }
        return kills;
    }

    /**
     * Aplica efecto de estado según el tipo de daño (FUEGO→BURN, VENENO→POISON).
     */
    private static void aplicarStatusPorDanio(Enemy enemy, DamageType tipo) {
        if (!enemy.active) return;
        switch (tipo) {
            case FUEGO:
                enemy.statusEffect.apply(StatusEffect.Tipo.BURN,
                    Constants.STATUS_BURN_DURATION, Constants.STATUS_BURN_DAMAGE);
                break;
            case VENENO:
                enemy.statusEffect.apply(StatusEffect.Tipo.POISON,
                    Constants.STATUS_POISON_DURATION, Constants.STATUS_POISON_DAMAGE);
                break;
            default:
                break;
        }
    }

    private static void aplicarEfectosOnHit(Player player, Enemy enemy, int dmg, DamageType tipo) {
        UpgradeManager um = player.getUpgradeManager();
        if (um == null) return;
        DamageType elemental = um.getElementalOnHit();
        if (elemental != null && enemy.active) aplicarStatusPorDanio(enemy, elemental);
        float ls = um.getLifeStealPercent();
        if (ls > 0 && dmg > 0) player.curar(Math.max(1, Math.round(dmg * ls)));
    }

    private static int contactDamagePor(Enemy.Tipo tipo) {
        switch (tipo) {
            case BASICO:
                return Constants.CONTACT_DAMAGE_BASICO;
            case RAPIDO:
                return Constants.CONTACT_DAMAGE_RAPIDO;
            case TANQUE:
                return Constants.CONTACT_DAMAGE_TANQUE;
            case SHOOTER:
                return Constants.CONTACT_DAMAGE_SHOOTER;
            case MALDITO:
                return Constants.CONTACT_DAMAGE_MALDITO;
            case ESPECTRAL:
                return Constants.CONTACT_DAMAGE_ESPECTRAL;
            default:
                return Constants.CONTACT_DAMAGE_DEFAULT;
        }
    }

    /**
     * Aplica defensas/resistencias según tipo de daño. ESPECTRAL es inmune a FISICO.
     */
    private static int calcularDaño(int raw, DamageType tipo, Enemy enemy) {
        if (enemy.tipo == Enemy.Tipo.ESPECTRAL) {
            if (tipo == DamageType.FISICO) return 0;
            if (tipo == DamageType.FUEGO) return Math.round(raw * 1.5f);
        }
        float reduccion;
        switch (tipo) {
            case FISICO:
                reduccion = enemy.defensa;
                break;
            case MAGICO:
                reduccion = enemy.resistenciaMagica;
                break;
            case CAOS:
                reduccion = enemy.defensa * 0.5f + enemy.resistenciaMagica * 0.5f;
                break;
            case A_DISTANCIA:
                reduccion = enemy.defensa * 0.5f;
                break;
            default:
                reduccion = 0f;
                break;
        }
        return Math.max(1, Math.round(raw - reduccion));
    }

    private static boolean colisionan(Vector2 p1, float r1, Vector2 p2, float r2) {
        return p1.dst(p2) < (r1 + r2);
    }

    private static float anguloAbsDiff(float a, float b) {
        float d = a - b;
        while (d > MathUtils.PI) d -= MathUtils.PI2;
        while (d < -MathUtils.PI) d += MathUtils.PI2;
        return Math.abs(d);
    }

    private static Enemy enemigoCercano(PoolEnemigos pool, Vector2 origen, float radio, Enemy excluir) {
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
