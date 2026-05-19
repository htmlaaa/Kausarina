package com.milwar.kaosuarina.data;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.data.model.TierData;
import com.milwar.kaosuarina.data.model.WeaponAffixData;
import com.milwar.kaosuarina.data.model.WeaponData;

import java.util.ArrayList;
import java.util.List;

/**
 * Rolls a WeaponInstance given a weapon_id and tier_id.
 *
 * Per-hit damage formula (from README):
 *   base = random(effectiveDmgMin, effectiveDmgMax)   // tier.dmgMult already applied
 *   dmg  = base + sum(affix dmg_flat)
 *   dmg *= (1 + sum(affix dmg_pct) / 100)
 *   // player stat scaling, crit, enemy resist handled externally in combat
 */
public class WeaponInstanceFactory {

    private final DataManager dm;

    public WeaponInstanceFactory() {
        this.dm = DataManager.getInstance();
    }

    public WeaponInstance roll(String weaponId, String tierId) {
        WeaponData weapon = dm.getWeapon(weaponId);
        TierData   tier   = dm.getTier(tierId);
        if (weapon == null) throw new IllegalArgumentException("Unknown weapon: " + weaponId);
        if (tier   == null) throw new IllegalArgumentException("Unknown tier: "   + tierId);

        List<WeaponAffixData> eligible = eligibleAffixes(tier);
        List<RolledAffix>     affixes  = rollAffixes(eligible, tier.affixSlots);

        float dmgMin  = weapon.baseDmgMin  * tier.dmgMult;
        float dmgMax  = weapon.baseDmgMax  * tier.dmgMult;
        float speed   = weapon.baseAttackSpeed * tier.speedMult;
        float crit    = weapon.baseCritChance  * tier.critMult;
        float critDmg = weapon.baseCritDmg     * tier.critMult;

        return new WeaponInstance(weaponId, tierId, dmgMin, dmgMax, speed, crit, critDmg, affixes);
    }

    /**
     * Computes the flat damage roll for a single hit (affix bonuses applied).
     * Player stat scaling, crit, and enemy resist must be applied externally.
     */
    public float rollHitDamage(WeaponInstance inst) {
        float dmg = MathUtils.random(inst.effectiveDmgMin, inst.effectiveDmgMax);
        dmg += inst.getSumStat("dmg_flat");
        dmg *= (1f + inst.getSumStat("dmg_pct") / 100f);
        return dmg;
    }

    // -------------------------------------------------------------------------

    private List<WeaponAffixData> eligibleAffixes(TierData tier) {
        List<WeaponAffixData> result = new ArrayList<>();
        for (WeaponAffixData a : dm.getAffixList()) {
            TierData minTier = dm.getTier(a.minTier);
            if (minTier != null && minTier.order <= tier.order) result.add(a);
        }
        return result;
    }

    private List<RolledAffix> rollAffixes(List<WeaponAffixData> eligible, int slots) {
        List<RolledAffix>     result = new ArrayList<>();
        List<WeaponAffixData> pool   = new ArrayList<>(eligible);
        for (int i = 0; i < slots && !pool.isEmpty(); i++) {
            WeaponAffixData picked = weightedPick(pool);
            pool.remove(picked);
            float value = (picked.valueMin == picked.valueMax)
                ? picked.valueMin
                : MathUtils.random((float) picked.valueMin, (float) picked.valueMax);
            result.add(new RolledAffix(picked.affixId, picked.stat, value));
        }
        return result;
    }

    private WeaponAffixData weightedPick(List<WeaponAffixData> pool) {
        int total = 0;
        for (WeaponAffixData a : pool) total += a.weight;
        int roll = MathUtils.random(1, total);
        int cumulative = 0;
        for (WeaponAffixData a : pool) {
            cumulative += a.weight;
            if (roll <= cumulative) return a;
        }
        return pool.get(pool.size() - 1);
    }
}
