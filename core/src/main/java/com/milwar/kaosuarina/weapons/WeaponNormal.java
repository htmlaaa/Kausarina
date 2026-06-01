package com.milwar.kaosuarina.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.entities.Bala;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.entities.PoolBalas;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.data.WeaponInstanceFactory;
import com.milwar.kaosuarina.systems.UpgradeManager;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.DamageType;

/**
 * Auto-firing weapon. GameScreen drives the fire loop — it checks cooldownTimer
 * and calls shoot() when it reaches 0.
 */
public class WeaponNormal extends Weapon {

    public WeaponNormal(WeaponType type, DamageType damageType,
                        int baseDamage, float cooldownBase,
                        int hpBonus, int defBonus, int resMagBonus, int manaMaxBonus,
                        float moveSpeedMult, Role.Tipo affinity) {
        super(type, WeaponCategory.NORMAL, damageType, baseDamage, cooldownBase,
              hpBonus, defBonus, resMagBonus, manaMaxBonus, moveSpeedMult, affinity);
    }

    /**
     * Fires this weapon toward aimAngle. Damage includes upgrade and affinity multipliers.
     *
     * @param p        the player firing (provides position, role, upgrade multipliers)
     * @param pool     bullet pool to spawn from
     * @param aimAngle angle toward the cursor in radians
     */
    public void shoot(Player p, PoolBalas pool, float aimAngle) {
        int dmg = calcDamage(p);
        if (inscription != null) dmg = Math.max(1, Math.round(dmg * inscription.damageMult()));
        int extraP = inscription != null ? inscription.extraPierce() : 0;
        boolean bypassDef = inscription != null && inscription.bypassesDefense();

        float secFire   = rolledInstance != null ? rolledInstance.getSumStat("add_fire_dmg")   : 0f;
        float secPoison = rolledInstance != null ? rolledInstance.getSumStat("add_poison_dmg") : 0f;
        float secChaos  = rolledInstance != null ? rolledInstance.getSumStat("add_chaos_dmg")  : 0f;

        switch (type) {
            case ESPADA_VERDUGO:
                for (int i = -1; i <= 1; i++) {
                    float a = aimAngle + i * MathUtils.degreesToRadians * 30f;
                    Bala b = pool.spawnReturning(p.position.x, p.position.y,
                        MathUtils.cos(a), MathUtils.sin(a),
                        dmg, extraP, 0, damageType, 800f, 250f, type);
                    if (b != null) {
                        if (bypassDef) b.ignoresDefense = true;
                        b.addFireDmg = secFire; b.addPoisonDmg = secPoison; b.addChaosDmg = secChaos;
                    }
                }
                break;
            case BACULO_ARCANO: {
                Bala b = pool.spawnReturning(p.position.x, p.position.y,
                    MathUtils.cos(aimAngle), MathUtils.sin(aimAngle),
                    dmg, extraP, 0, damageType, 500f, 1500f, type);
                if (b != null) {
                    if (bypassDef) b.ignoresDefense = true;
                    b.addFireDmg = secFire; b.addPoisonDmg = secPoison; b.addChaosDmg = secChaos;
                }
                break;
            }
            case PISTOLAS_GEMELAS:
                for (int i = -1; i <= 1; i += 2) {
                    float a = aimAngle + i * MathUtils.degreesToRadians * 3f;
                    Bala b = pool.spawnReturning(p.position.x, p.position.y,
                        MathUtils.cos(a), MathUtils.sin(a),
                        dmg, extraP, 0, damageType, 900f, 1500f, type);
                    if (b != null) {
                        if (bypassDef) b.ignoresDefense = true;
                        b.addFireDmg = secFire; b.addPoisonDmg = secPoison; b.addChaosDmg = secChaos;
                    }
                }
                break;
            default:
                break;
        }
    }

    private int calcDamage(Player p) {
        UpgradeManager um = p.getUpgradeManager();
        float mult = (um != null) ? um.getMultiplicadorDanio() : 1f;
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
