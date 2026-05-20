package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.data.DataManager;
import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.weapons.Inscription;

public class InscripcionSismica implements Inscription {
    @Override
    public void onHit(Player p, Enemy e, int rawDamage) {
        if (e.enemyId != null && DataManager.getInstance().isImmune(e.enemyId, "EFF_STUN")) return;
        e.stunTimer = Constants.STUN_DURATION_SISMICA;
    }

    @Override
    public String getName() { return "SIS"; }
}
