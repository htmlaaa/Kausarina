package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionDelEco implements Inscription {
    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        EchoQueue.add(e, rawDamage * 0.4f);
    }

    @Override
    public String getName() {
        return "ECO";
    }
}
