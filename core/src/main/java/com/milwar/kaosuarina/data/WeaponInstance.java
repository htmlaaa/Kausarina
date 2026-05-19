package com.milwar.kaosuarina.data;

import java.util.Collections;
import java.util.List;

public class WeaponInstance {
    public final String weaponId;
    public final String tierId;
    /** Base damage range after tier dmg_mult applied. Affix flat/pct bonuses are added per-hit. */
    public final float effectiveDmgMin;
    public final float effectiveDmgMax;
    public final float attackSpeed;
    public final float critChance;
    public final float critDmg;

    private final List<RolledAffix> affixes;

    WeaponInstance(String weaponId, String tierId,
                   float effectiveDmgMin, float effectiveDmgMax,
                   float attackSpeed, float critChance, float critDmg,
                   List<RolledAffix> affixes) {
        this.weaponId = weaponId;
        this.tierId = tierId;
        this.effectiveDmgMin = effectiveDmgMin;
        this.effectiveDmgMax = effectiveDmgMax;
        this.attackSpeed = attackSpeed;
        this.critChance = critChance;
        this.critDmg = critDmg;
        this.affixes = affixes;
    }

    public List<RolledAffix> getAffixes() {
        return Collections.unmodifiableList(affixes);
    }

    /** Sum of all rolled values for a given stat key (e.g. "dmg_flat", "dmg_pct", "atk_speed_pct"). */
    public float getSumStat(String stat) {
        float sum = 0;
        for (RolledAffix a : affixes) {
            if (stat.equals(a.stat)) sum += a.value;
        }
        return sum;
    }
}
