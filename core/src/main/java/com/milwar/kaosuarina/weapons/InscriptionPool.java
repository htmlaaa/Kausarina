package com.milwar.kaosuarina.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.weapons.inscriptions.*;

public class InscriptionPool {

    @SuppressWarnings("unchecked")
    private static final Class<? extends Inscription>[] TYPES = new Class[]{
        InscripcionIgnea.class,
        InscripcionVampirica.class,
        InscripcionDelEco.class,
        InscripcionDeVacio.class,
        InscripcionSismica.class,
        InscripcionDelCaos.class,
        InscripcionResonante.class,
        InscripcionEspectral.class,
        InscripcionVampiricaMana.class
    };

    /**
     * Call once at startup — registers types without allocating instances.
     */
    public static void init() {
    }

    /**
     * Returns a fresh instance of a random inscription. Safe to call mid-game (scroll pickup only).
     */
    public static Inscription getRandom() {
        int idx = MathUtils.random(TYPES.length - 1);
        try {
            return TYPES[idx].newInstance();
        } catch (Exception e) {
            return new InscripcionIgnea();
        }
    }
}
