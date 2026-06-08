package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionEspectral implements Inscription {
    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        // Pierce handled via extraPierce() at spawn time
    }

    @Override
    public int extraPierce() {
        return 1;
    }

    @Override
    public String getName() {
        return "ESP";
    }
}
