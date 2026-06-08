package com.milwar.kaosuarina.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.data.DataManager;
import com.milwar.kaosuarina.data.WeaponInstanceFactory;
import com.milwar.kaosuarina.data.model.WeaponData;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.weapons.WeaponPool;

import java.util.HashMap;
import java.util.Map;

/**
 * Genera armas tiered+afijos desde el catálogo JSON con sesgo por rol.
 * Cada llamada crea una nueva WeaponNormal — nunca modifica WeaponPool.
 */
public class WeaponDropper {

    private static final String[] MELEE_IDS = {
        "W_SHORTSWORD", "W_FLAMEBLADE", "W_HEAVYBLADE", "W_VAMP_DAGGER",
        "W_GREATAXE", "W_RAPIER", "W_WARSCYTHE", "W_FLAIL"
    };
    private static final String[] RANGED_IDS = {
        "W_HUNTBOW", "W_POISON_BOW", "W_BALLISTA", "W_DUAL_PISTOLS",
        "W_SNIPER", "W_SHOTGUN", "W_EXPLOSIVE", "W_BOLAS"
    };
    private static final String[] MAGIC_IDS = {
        "W_APP_STAFF", "W_FIRE_STAFF", "W_CHAOS_WAND", "W_PLAGUE_GRIM",
        "W_CHAINSTAFF", "W_BLOODTOME", "W_VOIDSTAFF", "W_FROSTORB"
    };

    private static final Map<String, WeaponType> TYPE_MAP = new HashMap<>();
    private static final Map<String, DamageType> DMG_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("W_SHORTSWORD", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_SHORTSWORD", DamageType.FISICO);
        TYPE_MAP.put("W_FLAMEBLADE", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_FLAMEBLADE", DamageType.FUEGO);
        TYPE_MAP.put("W_HEAVYBLADE", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_HEAVYBLADE", DamageType.FISICO);
        TYPE_MAP.put("W_VAMP_DAGGER", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_VAMP_DAGGER", DamageType.FISICO);
        TYPE_MAP.put("W_GREATAXE", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_GREATAXE", DamageType.FISICO);
        TYPE_MAP.put("W_RAPIER", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_RAPIER", DamageType.FISICO);
        TYPE_MAP.put("W_WARSCYTHE", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_WARSCYTHE", DamageType.FISICO);
        TYPE_MAP.put("W_FLAIL", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_FLAIL", DamageType.FISICO);
        TYPE_MAP.put("W_HUNTBOW", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_HUNTBOW", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_POISON_BOW", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_POISON_BOW", DamageType.VENENO);
        TYPE_MAP.put("W_BALLISTA", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_BALLISTA", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_DUAL_PISTOLS", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_DUAL_PISTOLS", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_SNIPER", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_SNIPER", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_SHOTGUN", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_SHOTGUN", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_EXPLOSIVE", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_EXPLOSIVE", DamageType.FUEGO);
        TYPE_MAP.put("W_BOLAS", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_BOLAS", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_APP_STAFF", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_APP_STAFF", DamageType.MAGICO);
        TYPE_MAP.put("W_FIRE_STAFF", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_FIRE_STAFF", DamageType.FUEGO);
        TYPE_MAP.put("W_CHAOS_WAND", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_CHAOS_WAND", DamageType.CAOS);
        TYPE_MAP.put("W_PLAGUE_GRIM", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_PLAGUE_GRIM", DamageType.VENENO);
        TYPE_MAP.put("W_CHAINSTAFF", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_CHAINSTAFF", DamageType.MAGICO);
        TYPE_MAP.put("W_BLOODTOME", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_BLOODTOME", DamageType.CAOS_PRIMORDIAL);
        TYPE_MAP.put("W_VOIDSTAFF", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_VOIDSTAFF", DamageType.CAOS);
        TYPE_MAP.put("W_FROSTORB", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_FROSTORB", DamageType.MAGICO);
        // ── Evolved weapons (CNT-05) ──────────────────────────────────────────
        TYPE_MAP.put("W_LONGSWORD", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_LONGSWORD", DamageType.FISICO);
        TYPE_MAP.put("W_INFERNO_BLADE", WeaponType.ESPADA_VERDUGO);
        DMG_MAP.put("W_INFERNO_BLADE", DamageType.FUEGO);
        TYPE_MAP.put("W_CUATRO_PISTOLAS", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_CUATRO_PISTOLAS", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_WARBOW", WeaponType.PISTOLAS_GEMELAS);
        DMG_MAP.put("W_WARBOW", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_ARCANE_CANON", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_ARCANE_CANON", DamageType.MAGICO);
        TYPE_MAP.put("W_VOID_CANNON", WeaponType.BACULO_ARCANO);
        DMG_MAP.put("W_VOID_CANNON", DamageType.CAOS_PRIMORDIAL);
    }

    /**
     * Genera con sesgo de rol: 60% arma propia, 25% secundaria, 15% terciaria.
     * 20% de probabilidad de generar un WeaponSkill del rol correspondiente.
     */
    public static Weapon generate(int depth, Role.Tipo roleHint) {
        // 20% chance: skill weapon for the matching role
        if (roleHint != null && MathUtils.random() < 0.20f) {
            WeaponType skillType;
            switch (roleHint) {
                case CABALLERO: skillType = WeaponType.MARTILLO_JUICIO; break;
                case MAGO:      skillType = WeaponType.TOMO_CAOS;       break;
                default:        skillType = WeaponType.RIFLE_PRECISION; break;
            }
            Weapon sk = WeaponPool.get(skillType);
            if (sk != null) return sk;
        }

        String tierId = depthToTier(depth);
        String weaponId = pickWeaponId(roleHint);

        WeaponData wd = DataManager.getInstance().getWeapon(weaponId);
        if (wd == null) {
            weaponId = "W_SHORTSWORD";
            wd = DataManager.getInstance().getWeapon(weaponId);
        }

        WeaponType wt = TYPE_MAP.get(weaponId);
        DamageType dt = DMG_MAP.get(weaponId);
        Role.Tipo affinity = typeToAffinity(wt);

        int baseDmg = Math.round((wd.baseDmgMin + wd.baseDmgMax) / 2f);
        float cd = (wd.baseAttackSpeed > 0) ? 1f / wd.baseAttackSpeed : 0.5f;

        WeaponNormal weapon = new WeaponNormal(wt, dt, baseDmg, cd, 0, 0, 0, 0, 1.0f, affinity);
        weapon.wtypeId      = wd.wtypeId      != null ? wd.wtypeId      : "";
        weapon.passiveSkillId = wd.passiveSkillId != null ? wd.passiveSkillId : "";
        weapon.rolledInstance = WeaponInstanceFactory.getInstance().roll(weaponId, tierId);
        weapon.tierId = tierId;
        if (MathUtils.random() < 0.30f) weapon.inscription = InscriptionPool.getRandom();
        return weapon;
    }

    /**
     * Genera sin sesgo de rol (para cofres sin contexto de personaje).
     */
    public static Weapon generate(int depth) {
        return generate(depth, null);
    }

    /**
     * Genera un arma específica por weapon_id a la profundidad dada (CNT-05 evolution).
     * Returns null if the weapon_id is not in TYPE_MAP.
     */
    public static Weapon generateById(String weaponId, int depth) {
        WeaponType wt = TYPE_MAP.get(weaponId);
        DamageType dt = DMG_MAP.get(weaponId);
        if (wt == null || dt == null) return null;

        String tierId = depthToTier(depth);
        WeaponData wd = DataManager.getInstance().getWeapon(weaponId);
        if (wd == null) return null;

        int baseDmg = Math.round((wd.baseDmgMin + wd.baseDmgMax) / 2f);
        float cd = (wd.baseAttackSpeed > 0) ? 1f / wd.baseAttackSpeed : 0.5f;

        Role.Tipo affinity = typeToAffinity(wt);
        WeaponNormal weapon = new WeaponNormal(wt, dt, baseDmg, cd, 0, 0, 0, 0, 1.0f, affinity);
        weapon.wtypeId        = wd.wtypeId        != null ? wd.wtypeId        : "";
        weapon.passiveSkillId = wd.passiveSkillId != null ? wd.passiveSkillId : "";
        weapon.rolledInstance = WeaponInstanceFactory.getInstance().roll(weaponId, tierId);
        weapon.tierId = tierId;
        return weapon;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private static String pickWeaponId(Role.Tipo role) {
        if (role == null) {
            // sin sesgo: distribución plana entre los 12
            String[] all = concat(MELEE_IDS, RANGED_IDS, MAGIC_IDS);
            return all[MathUtils.random(all.length - 1)];
        }
        String[] primary, secondary, tertiary;
        switch (role) {
            case CABALLERO:
                primary = MELEE_IDS;
                secondary = MAGIC_IDS;
                tertiary = RANGED_IDS;
                break;
            case MAGO:
                primary = MAGIC_IDS;
                secondary = MELEE_IDS;
                tertiary = RANGED_IDS;
                break;
            case SHOOTER:
                primary   = RANGED_IDS;
                secondary = MAGIC_IDS;
                tertiary  = MELEE_IDS;
                break;
            default:
                primary   = MELEE_IDS;
                secondary = MAGIC_IDS;
                tertiary  = RANGED_IDS;
                break;
        }
        float r = MathUtils.random();
        if (r < 0.60f) return primary[MathUtils.random(primary.length - 1)];
        if (r < 0.85f) return secondary[MathUtils.random(secondary.length - 1)];
        return tertiary[MathUtils.random(tertiary.length - 1)];
    }

    /**
     * Devuelve la afinidad de rol implícita según el tipo de arma.
     */
    public static Role.Tipo typeToAffinity(WeaponType wt) {
        if (wt == WeaponType.ESPADA_VERDUGO) return Role.Tipo.CABALLERO;
        if (wt == WeaponType.BACULO_ARCANO) return Role.Tipo.MAGO;
        if (wt == WeaponType.PISTOLAS_GEMELAS) return Role.Tipo.SHOOTER;
        return null;
    }

    public static String depthToTier(int depth) {
        if (depth <= 1) return "T1";
        if (depth <= 4) return "T2";
        if (depth <= 8) return "T3";
        if (depth <= 13) return "T4";
        return "T5";
    }

    private static String[] concat(String[] a, String[] b, String[] c) {
        String[] r = new String[a.length + b.length + c.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        System.arraycopy(c, 0, r, a.length + b.length, c.length);
        return r;
    }
}
