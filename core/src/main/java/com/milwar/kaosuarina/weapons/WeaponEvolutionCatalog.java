package com.milwar.kaosuarina.weapons;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps weapon_id → evolved weapon_id for duplicate-trigger evolutions (CNT-05).
 * All entries mirror weapon_evolutions.json (type: "duplicate").
 */
public class WeaponEvolutionCatalog {

    private static final Map<String, String> EVOLUTIONS = new HashMap<>();

    static {
        EVOLUTIONS.put("W_SHORTSWORD", "W_LONGSWORD");
        EVOLUTIONS.put("W_DUAL_PISTOLS", "W_CUATRO_PISTOLAS");
        EVOLUTIONS.put("W_APP_STAFF", "W_ARCANE_CANON");
        EVOLUTIONS.put("W_FLAMEBLADE", "W_INFERNO_BLADE");
        EVOLUTIONS.put("W_HUNTBOW", "W_WARBOW");
        EVOLUTIONS.put("W_CHAOS_WAND", "W_VOID_CANNON");
    }

    /**
     * Returns the evolved weapon_id, or null if no evolution exists.
     */
    public static String getEvolution(String weaponId) {
        return EVOLUTIONS.get(weaponId);
    }

    public static boolean hasEvolution(String weaponId) {
        return EVOLUTIONS.containsKey(weaponId);
    }
}
