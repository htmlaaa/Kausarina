package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionVampirica implements Inscription {
    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        p.heal(Math.max(1, rawDamage / 10));
    }

    @Override
    public float damageMult() {
        return 0.9f;
    }

    @Override
    public String getName() {
        return "Vampirica";
    }
}
