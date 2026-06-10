package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionDeVacio implements Inscription {
    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        // Defense bypass handled via bala.ignoresDefense=true set at spawn time
    }

    @Override
    public float damageMult() {
        return 0.75f;
    }

    @Override
    public boolean bypassesDefense() {
        return true;
    }

    @Override
    public String getName() {
        return "De Vacio";
    }
}
