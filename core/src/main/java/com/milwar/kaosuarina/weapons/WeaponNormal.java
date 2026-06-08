package com.milwar.kaosuarina.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.entities.Bullet;
import com.milwar.kaosuarina.entities.BulletPool;
import com.milwar.kaosuarina.entities.EnemyPool;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.data.WeaponInstanceFactory;
import com.milwar.kaosuarina.systems.UpgradeManager;
import com.milwar.kaosuarina.utils.CollisionManager;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.DamageType;

/**
 * Auto-firing weapon. GameScreen drives the fire loop — it checks cooldownTimer
 * and calls shoot() when it reaches 0.
 */
public class WeaponNormal extends Weapon {

    /** JSON wtype_id (WT_SWORD, WT_DAGGER, WT_GREATSWORD, WT_BOW, WT_CROSSBOW,
     *  WT_PISTOL, WT_STAFF, WT_WAND, WT_GRIMOIRE). Empty string = legacy fallback. */
    public String wtypeId = "";
    /** JSON passive_skill_id (PSK_BURN_ON_HIT, PSK_PIERCE, PSK_LIFESTEAL, …). */
    public String passiveSkillId = "";

    public WeaponNormal(WeaponType type, DamageType damageType,
                        int baseDamage, float cooldownBase,
                        int hpBonus, int defBonus, int resMagBonus, int manaMaxBonus,
                        float moveSpeedMult, Role.Tipo affinity) {
        super(type, WeaponCategory.NORMAL, damageType, baseDamage, cooldownBase,
            hpBonus, defBonus, resMagBonus, manaMaxBonus, moveSpeedMult, affinity);
    }

    /**
     * Fires this weapon toward aimAngle. Pattern is selected by wtypeId from JSON.
     *
     * @param p         the player firing
     * @param pool      bullet pool to spawn from
     * @param enemyPool enemy pool (needed for melee/blast arc checks)
     * @param aimAngle  angle toward cursor in radians
     * @param slotIndex weapon slot index (for inscription lookup on hit)
     */
    public void shoot(Player p, BulletPool pool, EnemyPool enemyPool, float aimAngle, int slotIndex) {
        int dmg = calcDamage(p);
        if (inscription != null) dmg = Math.max(1, Math.round(dmg * inscription.damageMult()));
        int extraP = inscription != null ? inscription.extraPierce() : 0;
        boolean bypassDef = inscription != null && inscription.bypassesDefense();

        float secFire   = rolledInstance != null ? rolledInstance.getSumStat("add_fire_dmg")   : 0f;
        float secPoison = rolledInstance != null ? rolledInstance.getSumStat("add_poison_dmg") : 0f;
        float secChaos  = rolledInstance != null ? rolledInstance.getSumStat("add_chaos_dmg")  : 0f;

        switch (wtypeId) {
            // ── Melee ────────────────────────────────────────────────────────
            case "WT_DAGGER":
                CollisionManager.checkMelee(p, p.position, aimAngle,
                    80f * MathUtils.degreesToRadians, 130f, dmg, damageType, enemyPool, passiveSkillId);
                break;
            case "WT_SWORD":
                CollisionManager.checkMelee(p, p.position, aimAngle,
                    120f * MathUtils.degreesToRadians, 160f, dmg, damageType, enemyPool, passiveSkillId);
                break;
            case "WT_GREATSWORD":
                CollisionManager.checkMelee(p, p.position, aimAngle,
                    180f * MathUtils.degreesToRadians, 220f, dmg, damageType, enemyPool, passiveSkillId);
                break;
            // ── Ranged ───────────────────────────────────────────────────────
            case "WT_BOW":
                applyPassive(spawnBullet(p, pool, aimAngle, slotIndex, dmg, extraP, bypassDef,
                    secFire, secPoison, secChaos, 600f, 1400f));
                break;
            case "WT_CROSSBOW":
                // Always pierces 1 extra enemy
                applyPassive(spawnBullet(p, pool, aimAngle, slotIndex, dmg, extraP + 1, bypassDef,
                    secFire, secPoison, secChaos, 900f, 2000f));
                break;
            case "WT_PISTOL": {
                int shots = ("PSK_DOUBLE_TAP".equals(passiveSkillId) && MathUtils.random() < 0.20f) ? 2 : 1;
                for (int burst = 0; burst < shots; burst++) {
                    for (int i = -1; i <= 1; i++) {
                        float a = aimAngle + i * 15f * MathUtils.degreesToRadians;
                        applyPassive(spawnBullet(p, pool, a, slotIndex, dmg, extraP, bypassDef,
                            secFire, secPoison, secChaos, 850f, 800f));
                    }
                }
                break;
            }
            // ── Magic ────────────────────────────────────────────────────────
            case "WT_STAFF":
                applyPassive(spawnBullet(p, pool, aimAngle, slotIndex, dmg, extraP, bypassDef,
                    secFire, secPoison, secChaos, 400f, 1800f));
                break;
            case "WT_WAND": {
                for (int i = -1; i <= 1; i += 2) {
                    float a = aimAngle + i * 8f * MathUtils.degreesToRadians;
                    applyPassive(spawnBullet(p, pool, a, slotIndex, dmg, extraP, bypassDef,
                        secFire, secPoison, secChaos, 550f, 1600f));
                }
                break;
            }
            case "WT_GRIMOIRE":
                CollisionManager.checkBlast(p, p.position, 200f, dmg, damageType, enemyPool);
                break;
            // ── Legacy fallback (weapons without wtype_id) ───────────────────
            default:
                shootLegacy(p, pool, aimAngle, slotIndex, dmg, extraP, bypassDef,
                    secFire, secPoison, secChaos);
                break;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Bullet spawnBullet(Player p, BulletPool pool, float angle, int slotIndex,
                                int dmg, int extraPierce, boolean bypassDef,
                                float secFire, float secPoison, float secChaos,
                                float speed, float range) {
        Bullet b = pool.spawnReturning(p.position.x, p.position.y,
            MathUtils.cos(angle), MathUtils.sin(angle),
            dmg, extraPierce, 0, damageType, speed, range, type);
        if (b != null) {
            b.sourceSlot = slotIndex;
            if (bypassDef) b.ignoresDefense = true;
            b.addFireDmg  = secFire;
            b.addPoisonDmg = secPoison;
            b.addChaosDmg  = secChaos;
        }
        return b;
    }

    private void applyPassive(Bullet b) {
        if (b == null || passiveSkillId.isEmpty()) return;
        switch (passiveSkillId) {
            case "PSK_BURN_ON_HIT":  b.burnProcChance     = 0.30f; break;
            case "PSK_POISON_HIT":   b.poisonProcChance   = 0.25f; break;
            case "PSK_PIERCE":       b.ignoresDefense      = true;  break;
            case "PSK_LIFESTEAL":    b.lifeStealPctWeapon = 0.10f; break;
            case "PSK_SLOW_ON_HIT":  b.slowOnHit           = true;  break;
            case "PSK_ARMOR_BREAK":  b.armorBreakChance   = 0.15f; break;
            case "PSK_CHAOS_PROC":   b.chaosProc           = 0.15f; break;
            default: break;
        }
    }

    private void shootLegacy(Player p, BulletPool pool, float aimAngle, int slotIndex,
                              int dmg, int extraP, boolean bypassDef,
                              float secFire, float secPoison, float secChaos) {
        switch (type) {
            case ESPADA_VERDUGO:
                for (int i = -1; i <= 1; i++) {
                    float a = aimAngle + i * MathUtils.degreesToRadians * 30f;
                    applyPassive(spawnBullet(p, pool, a, slotIndex, dmg, extraP, bypassDef,
                        secFire, secPoison, secChaos, 800f, 250f));
                }
                break;
            case BACULO_ARCANO:
                applyPassive(spawnBullet(p, pool, aimAngle, slotIndex, dmg, extraP, bypassDef,
                    secFire, secPoison, secChaos, 500f, 1500f));
                break;
            case PISTOLAS_GEMELAS:
                for (int i = -1; i <= 1; i += 2) {
                    float a = aimAngle + i * MathUtils.degreesToRadians * 3f;
                    applyPassive(spawnBullet(p, pool, a, slotIndex, dmg, extraP, bypassDef,
                        secFire, secPoison, secChaos, 900f, 1500f));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public Weapon clonar() {
        WeaponNormal w = new WeaponNormal(type, damageType, baseDamage, cooldownBase,
            hpBonus, defBonus, resMagBonus, manaMaxBonus, moveSpeedMult, affinity);
        w.rolledInstance  = rolledInstance;
        w.tierId          = tierId;
        w.inscription     = inscription;
        w.wtypeId         = wtypeId;
        w.passiveSkillId  = passiveSkillId;
        return w;
    }

    private int calcDamage(Player p) {
        UpgradeManager um = p.getUpgradeManager();
        float mult = (um != null) ? um.getDamageMultiplier() : 1f;
        if (hasAffinityFor(p.getRole().tipo)) mult *= Constants.WEAPON_AFFINITY_DMG_MULT;
        if (rolledInstance != null) {
            float base = WeaponInstanceFactory.getInstance().rollHitDamage(rolledInstance);
            int dmg = Math.max(1, Math.round(base * mult));
            float critChance = rolledInstance.critChance + rolledInstance.getSumStat("crit_chance");
            if (critChance > 0 && MathUtils.random() * 100f < critChance) {
                float critMult = 1f + (rolledInstance.critDmg + rolledInstance.getSumStat("crit_dmg")) / 100f;
                dmg = Math.max(1, Math.round(dmg * critMult));
            }
            return dmg;
        }
        return Math.max(1, Math.round(baseDamage * mult));
    }
}
