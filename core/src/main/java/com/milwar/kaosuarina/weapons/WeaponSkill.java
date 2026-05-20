package com.milwar.kaosuarina.weapons;

import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.entities.Bala;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.entities.PoolBalas;
import com.milwar.kaosuarina.entities.PoolEnemigos;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.data.WeaponInstanceFactory;
import com.milwar.kaosuarina.systems.UpgradeManager;
import com.milwar.kaosuarina.utils.ColisionManager;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.DamageType;

/**
 * Player-activated weapon (Q/E). Mana check and cooldown guard are handled by
 * GameScreen before calling activate() — this method just executes the effect.
 */
public class WeaponSkill extends Weapon {

    public final int   manaCost;
    public final float skillCooldownBase;
    /** Mutable: decremented each frame by GameScreen. */
    public       float skillCooldownTimer;

    public WeaponSkill(WeaponType type, DamageType damageType,
                       int baseDamage,
                       int manaCost, float skillCooldownBase,
                       int hpBonus, int defBonus, int resMagBonus, int manaMaxBonus,
                       float moveSpeedMult, Role.Tipo affinity) {
        super(type, WeaponCategory.SKILL, damageType, baseDamage, 0f,
              hpBonus, defBonus, resMagBonus, manaMaxBonus, moveSpeedMult, affinity);
        this.manaCost           = manaCost;
        this.skillCooldownBase  = skillCooldownBase;
        this.skillCooldownTimer = 0f;
    }

    /**
     * Executes the skill effect at targetPos (cursor world position).
     * Caller must verify mana and cooldown before calling this.
     */
    public void activate(Player p, PoolBalas pool, PoolEnemigos poolEnemigos, Vector2 targetPos) {
        int dmg = calcDamage(p);
        if (inscription != null) dmg = Math.max(1, Math.round(dmg * inscription.damageMult()));
        int extraP = inscription != null ? inscription.extraPierce() : 0;
        boolean bypassDef = inscription != null && inscription.bypassesDefense();

        switch (type) {
            case MARTILLO_JUICIO:
                ColisionManager.comprobarBlast(p, targetPos, 200f, dmg, DamageType.FISICO, poolEnemigos);
                break;
            case TOMO_CAOS:
                ColisionManager.comprobarBlast(p, targetPos, 250f, dmg, DamageType.CAOS_PRIMORDIAL, poolEnemigos);
                break;
            case RIFLE_PRECISION: {
                float dx = targetPos.x - p.position.x;
                float dy = targetPos.y - p.position.y;
                Bala b = pool.spawnReturning(p.position.x, p.position.y,
                    dx, dy, dmg, 999 + extraP, 0, damageType, 2000f, 2200f, type);
                if (b != null && bypassDef) b.ignoresDefense = true;
                break;
            }
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
            return Math.max(1, Math.round(base * mult));
        }
        return Math.max(1, Math.round(baseDamage * mult));
    }
}
