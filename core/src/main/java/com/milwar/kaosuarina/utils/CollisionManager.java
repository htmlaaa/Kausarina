package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.data.DataManager;
import com.milwar.kaosuarina.entities.Bullet;
import com.milwar.kaosuarina.entities.BulletPool;
import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.EnemyBulletPool;
import com.milwar.kaosuarina.entities.EnemyPool;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.systems.UpgradeManager;
import com.milwar.kaosuarina.weapons.Inscription;
import com.milwar.kaosuarina.weapons.Weapon;

public class CollisionManager {

    private static final float BOUNCE_SEARCH_RADIUS = 200f;

    private static final SpatialGrid grid = new SpatialGrid();

    // ── Broad phase: rebuild grid each frame before bullet checks ─────────────

    public static void rebuildGrid(EnemyPool pool) {
        grid.clear();
        com.badlogic.gdx.utils.Array<Enemy> list = pool.getEnemies();
        for (int i = 0; i < list.size; i++) grid.insert(list.get(i));
    }

    // ── Primary collision checks ──────────────────────────────────────────────

    public static int checkBulletsVsEnemies(Player player, BulletPool bulletPool, EnemyPool enemyPool) {
        int kills = 0;
        com.badlogic.gdx.utils.Array<Bullet> bullets = bulletPool.bullets;

        for (int bi = 0; bi < bullets.size; bi++) {
            Bullet bullet = bullets.get(bi);
            if (!bullet.active) continue;

            com.badlogic.gdx.utils.Array<Enemy> nearby =
                grid.getNearby(bullet.position.x, bullet.position.y,
                    Constants.BALA_RADIO + 80f);

            for (int ei = 0; ei < nearby.size; ei++) {
                Enemy enemy = nearby.get(ei);
                if (!enemy.active) continue;
                if (!overlap(bullet.position, Constants.BALA_RADIO, enemy.position, enemy.getRadio())) continue;

                kills += processHit(player, bullet, enemy, enemyPool);
                if (!bullet.active) break;
            }
        }
        return kills;
    }

    public static int checkPlayerVsEnemies(Player player, EnemyPool enemyPool) {
        com.badlogic.gdx.utils.Array<Enemy> list = enemyPool.getEnemies();
        for (int i = 0; i < list.size; i++) {
            Enemy e = list.get(i);
            if (!e.active) continue;
            if (overlap(player.position, player.getRadio(), e.position, e.getRadio())) {
                if (e.tipo == Enemy.Tipo.MALDITO) {
                    player.applyPoison(4f, Constants.MALDITO_POISON_DPS);
                }
                return contactDamageFor(e.tipo);
            }
        }
        return 0;
    }

    public static void checkEnemyBullets(EnemyBulletPool pool, Player player) {
        if (!player.isAlive()) return;
        com.badlogic.gdx.utils.Array<com.milwar.kaosuarina.entities.EnemyBullet> bullets = pool.bullets;
        for (int i = 0; i < bullets.size; i++) {
            com.milwar.kaosuarina.entities.EnemyBullet b = bullets.get(i);
            if (!b.active) continue;
            if (player.position.dst(b.position) < player.getRadio() + Constants.BALA_ENEMIGA_RADIO) {
                player.takeDamage(b.getDamage());
                b.deactivate();
            }
        }
    }

    public static int checkMalditoExplosions(EnemyPool pool) {
        int kills = 0;
        com.badlogic.gdx.utils.Array<Enemy> list = pool.getEnemies();
        for (int i = 0; i < list.size; i++) {
            Enemy e = list.get(i);
            if (e.debeExplotar) {
                e.debeExplotar = false;
                kills += triggerMalditoExplosion(pool, e.position);
            }
        }
        return kills;
    }

    // ── Area-of-effect attacks ────────────────────────────────────────────────

    public static int checkMelee(Player player, Vector2 center, float aimAngle, float arcRad,
                                 float radius, int damage, DamageType type, EnemyPool pool) {
        return checkMelee(player, center, aimAngle, arcRad, radius, damage, type, pool, null);
    }

    public static int checkMelee(Player player, Vector2 center, float aimAngle, float arcRad,
                                 float radius, int damage, DamageType type, EnemyPool pool,
                                 String passiveSkillId) {
        int kills = 0;
        com.badlogic.gdx.utils.Array<Enemy> list = pool.getEnemies();
        for (int i = 0; i < list.size; i++) {
            Enemy e = list.get(i);
            if (!e.active) continue;
            float dx = e.position.x - center.x;
            float dy = e.position.y - center.y;
            if (dx * dx + dy * dy > (radius + e.getRadio()) * (radius + e.getRadio())) continue;
            float enemyAngle = (float) Math.atan2(dy, dx);
            if (angleDiff(enemyAngle, aimAngle) <= arcRad * 0.5f) {
                boolean wasAlive = e.active;
                int dmg = calculateDamage(damage, type, e, false);
                e.takeDamage(dmg);
                applyStatusOnDamage(e, type);
                applyOnHitEffects(player, e, dmg, type);
                if (passiveSkillId != null) applyMeleeWeaponPassive(player, e, dmg, passiveSkillId);
                if (wasAlive && !e.active) {
                    kills++;
                    pool.registerKill(e.tipo);
                    e.pendingLootDrop = true;
                    ParticlePool.spawnDeath(e.position.x, e.position.y, e.tipo);
                    int overkill = dmg - e.lastHpSnapshot;
                    if (overkill > 0) player.getStats().addMana(overkill / Constants.OVERKILL_DIVISOR);
                }
            }
        }
        return kills;
    }

    private static void applyMeleeWeaponPassive(Player player, Enemy e, int dmg, String psk) {
        if (!e.active) return;
        switch (psk) {
            case "PSK_ARMOR_BREAK":
                if (MathUtils.random() < Constants.PSK_ARMOR_BREAK_REDUCTION) {
                    e.defReductionTimer = Constants.PSK_ARMOR_BREAK_DURATION;
                    e.defReductionPct = Constants.PSK_ARMOR_BREAK_REDUCTION;
                }
                break;
            case "PSK_LIFESTEAL":
                if (dmg > 0) player.heal(Math.max(1, Math.round(dmg * 0.10f)));
                break;
            case "PSK_SLOW_ON_HIT":
                e.slowTimer = Math.max(e.slowTimer, Constants.PSK_SLOW_DURATION);
                e.slowMult = Math.min(e.slowMult, Constants.PSK_SLOW_MULT);
                break;
            case "PSK_BURN_ON_HIT":
                if (MathUtils.random() < 0.30f) applyStatusOnDamage(e, DamageType.FUEGO);
                break;
            case "PSK_POISON_HIT":
                if (MathUtils.random() < 0.25f) applyStatusOnDamage(e, DamageType.VENENO);
                break;
            default:
                break;
        }
    }

    public static int checkBlast(Player player, Vector2 center, float radius,
                                 int damage, DamageType type, EnemyPool pool) {
        int kills = 0;
        com.badlogic.gdx.utils.Array<Enemy> list = pool.getEnemies();
        for (int i = 0; i < list.size; i++) {
            Enemy e = list.get(i);
            if (!e.active) continue;
            if (center.dst(e.position) <= radius + e.getRadio()) {
                boolean wasAlive = e.active;
                int dmg = calculateDamage(damage, type, e, false);
                e.takeDamage(dmg);
                applyStatusOnDamage(e, type);
                applyOnHitEffects(player, e, dmg, type);
                if (wasAlive && !e.active) {
                    kills++;
                    pool.registerKill(e.tipo);
                    e.pendingLootDrop = true;
                    ParticlePool.spawnDeath(e.position.x, e.position.y, e.tipo);
                    int overkill = dmg - e.lastHpSnapshot;
                    if (overkill > 0) player.getStats().addMana(overkill / Constants.OVERKILL_DIVISOR);
                }
            }
        }
        return kills;
    }

    public static Enemy nearestEnemy(EnemyPool pool, Vector2 origin, float maxRange) {
        return nearestActive(pool, origin, maxRange, null);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private static int processHit(Player player, Bullet bullet, Enemy enemy, EnemyPool pool) {
        int kills = 0;
        boolean wasAlive = enemy.active;
        int dmg = calculateDamage(bullet.damage, bullet.damageType, enemy, bullet.ignoresDefense);
        enemy.takeDamage(dmg);
        applyStatusOnDamage(enemy, bullet.damageType);
        applyOnHitEffects(player, enemy, dmg, bullet.damageType);

        if (enemy.active) {
            if (bullet.addFireDmg > 0) applyStatusOnDamage(enemy, DamageType.FUEGO);
            if (bullet.addPoisonDmg > 0) applyStatusOnDamage(enemy, DamageType.VENENO);
            if (bullet.addChaosDmg > 0) applyStatusOnDamage(enemy, DamageType.CAOS_PRIMORDIAL);
            // PSK_* weapon passive procs
            if (bullet.burnProcChance > 0 && MathUtils.random() < bullet.burnProcChance)
                applyStatusOnDamage(enemy, DamageType.FUEGO);
            if (bullet.poisonProcChance > 0 && MathUtils.random() < bullet.poisonProcChance)
                applyStatusOnDamage(enemy, DamageType.VENENO);
            if (bullet.slowOnHit) {
                enemy.slowTimer = Math.max(enemy.slowTimer, Constants.PSK_SLOW_DURATION);
                enemy.slowMult = Math.min(enemy.slowMult, Constants.PSK_SLOW_MULT);
            }
            if (bullet.armorBreakChance > 0 && MathUtils.random() < bullet.armorBreakChance) {
                enemy.defReductionTimer = Constants.PSK_ARMOR_BREAK_DURATION;
                enemy.defReductionPct = Constants.PSK_ARMOR_BREAK_REDUCTION;
            }
            if (bullet.chaosProc > 0 && MathUtils.random() < bullet.chaosProc)
                enemy.takeDamage(calculateDamage(bullet.damage, DamageType.CAOS_PRIMORDIAL, enemy, false));
        }
        if (bullet.lifeStealPctWeapon > 0 && dmg > 0)
            player.heal(Math.max(1, Math.round(dmg * bullet.lifeStealPctWeapon)));

        Inscription ins = null;
        if (bullet.sourceSlot >= 0) {
            Weapon srcW = player.getWeaponAtSlot(bullet.sourceSlot);
            if (srcW != null) ins = srcW.inscription;
        }
        if (ins != null) ins.onHit(player, enemy, dmg);

        if (wasAlive && !enemy.active) {
            kills++;
            pool.registerKill(enemy.tipo);
            enemy.pendingLootDrop = true;
            int overkill = dmg - enemy.lastHpSnapshot;
            if (overkill > 0) player.getStats().addMana(overkill / Constants.OVERKILL_DIVISOR);
        }

        AudioManager.playHit();
        ParticlePool.spawnImpact(bullet.position.x, bullet.position.y);
        if (wasAlive && !enemy.active) {
            AudioManager.playDeath();
            ParticlePool.spawnDeath(enemy.position.x, enemy.position.y, enemy.tipo);
        }

        boolean stillActive = bullet.onHit();

        if (!stillActive && bullet.bouncesLeft > 0) {
            Enemy target = nearestActive(pool, bullet.position, BOUNCE_SEARCH_RADIUS, enemy);
            if (target != null) {
                bullet.bounce(target.position.x, target.position.y);
                stillActive = true;
            }
        }

        if (!stillActive) bullet.active = false;
        return kills;
    }

    private static int triggerMalditoExplosion(EnemyPool pool, Vector2 center) {
        ParticlePool.spawnExplosion(center.x, center.y);
        int kills = 0;
        com.badlogic.gdx.utils.Array<Enemy> list = pool.getEnemies();
        for (int i = 0; i < list.size; i++) {
            Enemy e = list.get(i);
            if (!e.active) continue;
            if (center.dst(e.position) <= Constants.MALDITO_EXPLOSION_RADIUS + e.getRadio()) {
                boolean wasAlive = e.active;
                e.takeDamage(calculateDamage(Constants.MALDITO_EXPLOSION_DAMAGE, DamageType.VENENO, e, false));
                applyStatusOnDamage(e, DamageType.VENENO);
                if (wasAlive && !e.active) {
                    kills++;
                    pool.registerKill(e.tipo);
                    e.pendingLootDrop = true;
                }
            }
        }
        return kills;
    }

    private static void applyStatusOnDamage(Enemy enemy, DamageType type) {
        if (!enemy.active) return;
        switch (type) {
            case FUEGO:
                int fireDmg = MathUtils.random(Constants.STATUS_FIRE_DMG_MIN, Constants.STATUS_FIRE_DMG_MAX);
                enemy.statusEffect.apply(StatusEffect.Tipo.BURN, Constants.STATUS_FIRE_DURATION, fireDmg);
                break;
            case VENENO:
                enemy.statusEffect.apply(StatusEffect.Tipo.POISON,
                    Constants.STATUS_POISON_DURATION, Constants.STATUS_POISON_DAMAGE);
                break;
            case CAOS_PRIMORDIAL:
                enemy.statusEffect.apply(StatusEffect.Tipo.BURN,
                    Constants.STATUS_BURN_DURATION * 1.5f, Constants.STATUS_BURN_DAMAGE + 4);
                boolean slowImmune = enemy.enemyId != null &&
                    DataManager.getInstance().isImmune(enemy.enemyId, "EFF_SLOW");
                if (!slowImmune) {
                    enemy.slowTimer = Constants.CAOS_PRIMORDIAL_SLOW_DURATION;
                    enemy.slowMult = Constants.CAOS_PRIMORDIAL_SLOW_MULT;
                }
                break;
            default:
                break;
        }
    }

    private static void applyOnHitEffects(Player player, Enemy enemy, int dmg, DamageType type) {
        UpgradeManager um = player.getUpgradeManager();
        if (um == null) return;
        DamageType elemental = um.getElementalOnHit();
        if (elemental != null && enemy.active) applyStatusOnDamage(enemy, elemental);
        // FIX Bug 1: weaponAffixLifesteal was double-counted; lifeStealPercent already includes it
        float ls = um.getLifeStealPercent() + player.getStats().lifeStealPercent;
        if (ls > 0 && dmg > 0) player.heal(Math.max(1, Math.round(dmg * ls)));
    }

    private static int contactDamageFor(Enemy.Tipo tipo) {
        switch (tipo) {
            case BASICO:       return Constants.CONTACT_DAMAGE_BASICO;
            case RAPIDO:       return Constants.CONTACT_DAMAGE_RAPIDO;
            case TANQUE:       return Constants.CONTACT_DAMAGE_TANQUE;
            case SHOOTER:      return Constants.CONTACT_DAMAGE_SHOOTER;
            case MALDITO:      return Constants.CONTACT_DAMAGE_MALDITO;
            case ESPECTRAL:    return Constants.CONTACT_DAMAGE_ESPECTRAL;
            case GUARDIAN:     return Constants.GUARDIAN_CONTACT_DMG;
            case ARQUERO:      return Constants.ARQUERO_CONTACT_DMG;
            case DEVASTADOR:   return Constants.DEVASTADOR_CONTACT_DMG;
            case FRAGMENTADO:  return Constants.FRAGMENTADO_CONTACT_DMG;  // FIX Bug 3
            default:           return Constants.CONTACT_DAMAGE_DEFAULT;
        }
    }

    private static int calculateDamage(int raw, DamageType type, Enemy enemy, boolean ignoresDefense) {
        if (type == DamageType.FUEGO) raw = Math.round(raw * Constants.STATUS_FUEGO_DMG_MULT);

        if (enemy.tipo == Enemy.Tipo.ESPECTRAL) {
            if (type == DamageType.FISICO && !ignoresDefense) return 0;
            if (type == DamageType.FUEGO) return Math.round(raw * 1.5f);
        }
        if (enemy.tipo == Enemy.Tipo.GUARDIAN && type == DamageType.MAGICO) {
            return Math.max(1, Math.round(raw * 1.5f - enemy.resistenciaMagica));
        }
        if (ignoresDefense) return Math.max(1, raw);
        if (type == DamageType.CAOS_PRIMORDIAL) return Math.max(1, raw);

        float reduction;
        switch (type) {
            case FISICO:       reduction = enemy.defensa; break;
            case MAGICO:       reduction = enemy.resistenciaMagica; break;
            case CAOS:         reduction = enemy.defensa * 0.5f + enemy.resistenciaMagica * 0.5f; break;
            case A_DISTANCIA:  reduction = enemy.defensa * 0.5f; break;
            default:           reduction = 0f; break;
        }
        if (enemy.defReductionPct > 0 && enemy.defReductionTimer > 0)
            reduction *= (1f - enemy.defReductionPct);
        int damage = Math.max(1, Math.round(raw - reduction));

        if (enemy.enemyId != null) {
            String dmgId = damageTypeToId(type);
            if (dmgId != null) {
                int resistPct = DataManager.getInstance().getResistPct(enemy.enemyId, dmgId);
                if (resistPct != 0) damage = Math.max(1, Math.round(damage * (1f - resistPct / 100f)));
            }
        }
        return damage;
    }

    private static String damageTypeToId(DamageType type) {
        switch (type) {
            case FISICO:      return "DMG_PHYSICAL";
            case MAGICO:      return "DMG_MAGIC";
            case A_DISTANCIA: return "DMG_RANGED";
            case FUEGO:       return "DMG_FIRE";
            case VENENO:      return "DMG_POISON";
            case CAOS:        return "DMG_CHAOS";
            default:          return null;
        }
    }

    private static boolean overlap(Vector2 p1, float r1, Vector2 p2, float r2) {
        return p1.dst(p2) < (r1 + r2);
    }

    private static float angleDiff(float a, float b) {
        float d = a - b;
        while (d > MathUtils.PI) d -= MathUtils.PI2;
        while (d < -MathUtils.PI) d += MathUtils.PI2;
        return Math.abs(d);
    }

    private static Enemy nearestActive(EnemyPool pool, Vector2 origin, float range, Enemy exclude) {
        com.badlogic.gdx.utils.Array<Enemy> list = pool.getEnemies();
        Enemy nearest = null;
        float minDst = range;
        for (int i = 0; i < list.size; i++) {
            Enemy e = list.get(i);
            if (!e.active || e == exclude) continue;
            float dst = origin.dst(e.position);
            if (dst < minDst) {
                minDst = dst;
                nearest = e;
            }
        }
        return nearest;
    }
}
