package com.milwar.kaosuarina.weapons;

import com.milwar.kaosuarina.data.WeaponInstance;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.weapons.Inscription;

/**
 * Base class for all weapons. Instances are pre-allocated by WeaponPool (ADR-006).
 * Never instantiate with new Weapon() directly — only WeaponPool.init() may do so.
 */
public abstract class Weapon {

    public final WeaponType     type;
    public final WeaponCategory category;
    public final DamageType     damageType;
    public final int            baseDamage;
    /** Base cooldown in seconds. 0 for SKILL weapons (they use skillCooldownBase). */
    public final float          cooldownBase;
    /** Mutable: decremented each frame by Player.updateWeaponCooldowns(). */
    public       float          cooldownTimer;
    public final int            hpBonus;
    public final int            defBonus;
    public final int            resMagBonus;
    public final int            manaMaxBonus;
    /** Multiplied into player speed while this weapon is equipped. */
    public final float          moveSpeedMult;
    /** Null para armas de WeaponPool (stats hardcodeados). Poblado por WeaponDropper en drops. */
    public WeaponInstance rolledInstance = null;
    /** Tier ID del JSON (T1-T5). Null para armas base del pool. */
    public String tierId = null;
    /** Inscripción activa en este arma. Null si no lleva ninguna. */
    public Inscription inscription = null;

    private final Role.Tipo affinity;

    protected Weapon(WeaponType type, WeaponCategory category, DamageType damageType,
                     int baseDamage, float cooldownBase,
                     int hpBonus, int defBonus, int resMagBonus, int manaMaxBonus,
                     float moveSpeedMult, Role.Tipo affinity) {
        this.type          = type;
        this.category      = category;
        this.damageType    = damageType;
        this.baseDamage    = baseDamage;
        this.cooldownBase  = cooldownBase;
        this.cooldownTimer = 0f;
        this.hpBonus       = hpBonus;
        this.defBonus      = defBonus;
        this.resMagBonus   = resMagBonus;
        this.manaMaxBonus  = manaMaxBonus;
        this.moveSpeedMult = moveSpeedMult;
        this.affinity      = affinity;
    }

    /** Returns true if this weapon grants the affinity bonus for the given role. */
    public boolean hasAffinityFor(Role.Tipo tipo) {
        return affinity == tipo;
    }
}
