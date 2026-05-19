package com.milwar.kaosuarina.weapons;

import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.utils.DamageType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Static pool of the 6 pre-allocated Weapon instances (ADR-006).
 * Follows the SharedTextures / AnimationSheets pattern.
 *
 * Call WeaponPool.init() once in KaosuarinaGame.create() before any weapon access.
 * Never call new WeaponNormal() / new WeaponSkill() outside this class.
 */
public class WeaponPool {

    private static final Map<WeaponType, Weapon> pool = new EnumMap<>(WeaponType.class);
    private static final WeaponType[] ALL_TYPES = WeaponType.values();
    private static final Random rng = new Random();

    /** Allocates the 6 weapon instances. Idempotent — safe to call twice (e.g. in tests). */
    public static void init() {
        if (!pool.isEmpty()) return;

        // ── WeaponNormal(type, damageType, baseDmg, cdBase, hpBonus, defBonus, resMagBonus, manaMaxBonus, moveSpeedMult, affinity)
        pool.put(WeaponType.ESPADA_VERDUGO,
            new WeaponNormal(WeaponType.ESPADA_VERDUGO, DamageType.FISICO,
                35, 0.45f,  20, 5, 0,  0,  1.0f,  Role.Tipo.CABALLERO));

        pool.put(WeaponType.BACULO_ARCANO,
            new WeaponNormal(WeaponType.BACULO_ARCANO, DamageType.MAGICO,
                28, 0.60f,   0, 0, 15, 20, 1.0f,  Role.Tipo.MAGO));

        pool.put(WeaponType.PISTOLAS_GEMELAS,
            new WeaponNormal(WeaponType.PISTOLAS_GEMELAS, DamageType.A_DISTANCIA,
                18, 0.16f,   0, 0, 0,  0,  1.1f,  Role.Tipo.SHOOTER));

        // ── WeaponSkill(type, damageType, baseDmg, manaCost, skillCdBase, hpBonus, defBonus, resMagBonus, manaMaxBonus, moveSpeedMult, affinity)
        pool.put(WeaponType.MARTILLO_JUICIO,
            new WeaponSkill(WeaponType.MARTILLO_JUICIO, DamageType.FISICO,
                80,  25, 4.0f,  30, 10, 0,  0,  0.9f,  Role.Tipo.CABALLERO));

        pool.put(WeaponType.TOMO_CAOS,
            new WeaponSkill(WeaponType.TOMO_CAOS, DamageType.CAOS_PRIMORDIAL,
                60,  35, 5.0f,   0,  0, 25, 40, 1.0f,  Role.Tipo.MAGO));

        pool.put(WeaponType.RIFLE_PRECISION,
            new WeaponSkill(WeaponType.RIFLE_PRECISION, DamageType.A_DISTANCIA,
                120, 20, 2.0f,  -5,  0, 0,  0,  1.05f, Role.Tipo.SHOOTER));
    }

    /**
     * Returns the pre-allocated instance for the given type. Always the same reference.
     * Returns null if init() has not been called.
     */
    public static Weapon get(WeaponType type) {
        return pool.get(type);
    }

    /** Returns a uniformly random weapon from the pool (used by chest pickups, S5-06). */
    public static Weapon getRandom() {
        return pool.get(ALL_TYPES[rng.nextInt(ALL_TYPES.length)]);
    }

    /** No-op — Weapon holds no Disposable resources in the current implementation. */
    public static void dispose() {}
}
