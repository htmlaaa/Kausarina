package com.milwar.kaosuarina.weapons.inscriptions;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.StatusEffect;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionDelCaos implements Inscription {
    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        int roll = MathUtils.random(3);
        switch (roll) {
            case 0:
                if (e.active) e.statusEffect.apply(StatusEffect.Tipo.BURN,
                    Constants.STATUS_BURN_DURATION, Constants.STATUS_BURN_DAMAGE);
                break;
            case 1:
                if (e.active) e.statusEffect.apply(StatusEffect.Tipo.POISON,
                    Constants.STATUS_POISON_DURATION, Constants.STATUS_POISON_DAMAGE);
                break;
            case 2:
                p.heal(5);
                break;
            case 3:
                p.getStats().addMana(5);
                break;
        }
    }

    @Override
    public String getName() {
        return "CAO";
    }
}
