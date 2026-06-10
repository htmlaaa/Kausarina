package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionResonante implements Inscription {
    private int stacks = 0;
    private Enemy lastTarget = null;

    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        if (e == lastTarget) {
            stacks = Math.min(stacks + 1, 10);
        } else {
            stacks = 0;
            lastTarget = e;
        }
    }

    @Override
    public float damageMult() {
        return 1.0f + 0.05f * stacks;
    }

    @Override
    public String getName() {
        return "Resonante";
    }
}
