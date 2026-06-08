package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.StatusEffect;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionIgnea implements Inscription {
    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        if (!e.active) return;
        e.statusEffect.apply(StatusEffect.Tipo.BURN,
            Constants.STATUS_BURN_DURATION, Constants.STATUS_BURN_DAMAGE);
    }

    @Override
    public String getName() {
        return "IGN";
    }
}
