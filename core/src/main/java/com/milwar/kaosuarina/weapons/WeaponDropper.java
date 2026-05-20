package com.milwar.kaosuarina.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.data.DataManager;
import com.milwar.kaosuarina.data.WeaponInstanceFactory;
import com.milwar.kaosuarina.data.model.WeaponData;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.utils.DamageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Genera armas tiered+afijos desde el catálogo JSON con sesgo por rol.
 * Cada llamada crea una nueva WeaponNormal — nunca modifica WeaponPool.
 */
public class WeaponDropper {

    private static final String[] MELEE_IDS  = {
        "W_SHORTSWORD", "W_FLAMEBLADE", "W_HEAVYBLADE", "W_VAMP_DAGGER"
    };
    private static final String[] RANGED_IDS = {
        "W_HUNTBOW", "W_POISON_BOW", "W_BALLISTA", "W_DUAL_PISTOLS"
    };
    private static final String[] MAGIC_IDS  = {
        "W_APP_STAFF", "W_FIRE_STAFF", "W_CHAOS_WAND", "W_PLAGUE_GRIM"
    };

    private static final Map<String, WeaponType> TYPE_MAP = new HashMap<>();
    private static final Map<String, DamageType> DMG_MAP  = new HashMap<>();

    static {
        TYPE_MAP.put("W_SHORTSWORD",   WeaponType.ESPADA_VERDUGO);  DMG_MAP.put("W_SHORTSWORD",   DamageType.FISICO);
        TYPE_MAP.put("W_FLAMEBLADE",   WeaponType.ESPADA_VERDUGO);  DMG_MAP.put("W_FLAMEBLADE",   DamageType.FUEGO);
        TYPE_MAP.put("W_HEAVYBLADE",   WeaponType.ESPADA_VERDUGO);  DMG_MAP.put("W_HEAVYBLADE",   DamageType.FISICO);
        TYPE_MAP.put("W_VAMP_DAGGER",  WeaponType.ESPADA_VERDUGO);  DMG_MAP.put("W_VAMP_DAGGER",  DamageType.FISICO);
        TYPE_MAP.put("W_HUNTBOW",      WeaponType.PISTOLAS_GEMELAS); DMG_MAP.put("W_HUNTBOW",      DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_POISON_BOW",   WeaponType.PISTOLAS_GEMELAS); DMG_MAP.put("W_POISON_BOW",   DamageType.VENENO);
        TYPE_MAP.put("W_BALLISTA",     WeaponType.PISTOLAS_GEMELAS); DMG_MAP.put("W_BALLISTA",     DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_DUAL_PISTOLS", WeaponType.PISTOLAS_GEMELAS); DMG_MAP.put("W_DUAL_PISTOLS", DamageType.A_DISTANCIA);
        TYPE_MAP.put("W_APP_STAFF",    WeaponType.BACULO_ARCANO);    DMG_MAP.put("W_APP_STAFF",    DamageType.MAGICO);
        TYPE_MAP.put("W_FIRE_STAFF",   WeaponType.BACULO_ARCANO);    DMG_MAP.put("W_FIRE_STAFF",   DamageType.FUEGO);
        TYPE_MAP.put("W_CHAOS_WAND",   WeaponType.BACULO_ARCANO);    DMG_MAP.put("W_CHAOS_WAND",   DamageType.CAOS);
        TYPE_MAP.put("W_PLAGUE_GRIM",  WeaponType.BACULO_ARCANO);    DMG_MAP.put("W_PLAGUE_GRIM",  DamageType.VENENO);
    }

    /** Genera con sesgo de rol: 60% arma propia, 25% secundaria, 15% terciaria. */
    public static Weapon generate(int depth, Role.Tipo roleHint) {
        String tierId    = depthToTier(depth);
        String weaponId  = pickWeaponId(roleHint);

        WeaponData wd = DataManager.getInstance().getWeapon(weaponId);
        if (wd == null) { weaponId = "W_SHORTSWORD"; wd = DataManager.getInstance().getWeapon(weaponId); }

        WeaponType wt       = TYPE_MAP.get(weaponId);
        DamageType dt       = DMG_MAP.get(weaponId);
        Role.Tipo  affinity = typeToAffinity(wt);

        int   baseDmg = Math.round((wd.baseDmgMin + wd.baseDmgMax) / 2f);
        float cd      = (wd.baseAttackSpeed > 0) ? 1f / wd.baseAttackSpeed : 0.5f;

        WeaponNormal weapon = new WeaponNormal(wt, dt, baseDmg, cd, 0, 0, 0, 0, 1.0f, affinity);
        weapon.rolledInstance = WeaponInstanceFactory.getInstance().roll(weaponId, tierId);
        weapon.tierId         = tierId;
        if (MathUtils.random() < 0.30f) weapon.inscription = InscriptionPool.getRandom();
        return weapon;
    }

    /** Genera sin sesgo de rol (para cofres sin contexto de personaje). */
    public static Weapon generate(int depth) {
        return generate(depth, null);
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
            case CABALLERO: primary = MELEE_IDS;  secondary = MAGIC_IDS;  tertiary = RANGED_IDS; break;
            case MAGO:      primary = MAGIC_IDS;  secondary = MELEE_IDS;  tertiary = RANGED_IDS; break;
            // SHOOTER ya tiene auto-disparo nativo — nunca genera PISTOLAS_GEMELAS
            default:        primary = MELEE_IDS;  secondary = MAGIC_IDS;  tertiary = MELEE_IDS;  break;
        }
        float r = MathUtils.random();
        if (r < 0.60f) return primary  [MathUtils.random(primary.length   - 1)];
        if (r < 0.85f) return secondary[MathUtils.random(secondary.length - 1)];
        return            tertiary[MathUtils.random(tertiary.length  - 1)];
    }

    /** Devuelve la afinidad de rol implícita según el tipo de arma. */
    public static Role.Tipo typeToAffinity(WeaponType wt) {
        if (wt == WeaponType.ESPADA_VERDUGO)  return Role.Tipo.CABALLERO;
        if (wt == WeaponType.BACULO_ARCANO)   return Role.Tipo.MAGO;
        if (wt == WeaponType.PISTOLAS_GEMELAS) return Role.Tipo.SHOOTER;
        return null;
    }

    public static String depthToTier(int depth) {
        if (depth <= 1)  return "T1";
        if (depth <= 4)  return "T2";
        if (depth <= 8)  return "T3";
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
