package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionVampiricaMana implements Inscription {
    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        p.getStats().addMana(2);
    }

    @Override
    public String getName() {
        return "Vampirica Mana";
    }
}
