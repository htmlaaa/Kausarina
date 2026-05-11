package com.milwar.kaosuarina.reliquias;

import com.milwar.kaosuarina.entities.Player;

/**
 * Reliquia del Tirador — Momentum de Combate.
 * Kills acumulan Combo (max 10). Sin kill en 2s → -1 combo/s.
 * Cada punto de Combo = -3% cooldown (max -30% a combo 10).
 */
public class ReliquiaTirador implements Reliquia {

    private static final int   MAX_COMBO        = 10;
    private static final float DECAY_DELAY       = 2f;
    private static final float DECAY_RATE        = 1f;
    private static final float BONUS_PER_COMBO   = 0.03f;

    private int   comboCount         = 0;
    private float timeSinceLastKill  = 0f;
    private float decayAccumulator   = 0f;

    @Override
    public void onKill(Player player) {
        comboCount = Math.min(comboCount + 1, MAX_COMBO);
        timeSinceLastKill = 0f;
        decayAccumulator  = 0f;
    }

    @Override
    public void onUpdate(Player player, float delta) {
        timeSinceLastKill += delta;
        if (timeSinceLastKill > DECAY_DELAY && comboCount > 0) {
            decayAccumulator += delta;
            while (decayAccumulator >= 1f / DECAY_RATE) {
                comboCount = Math.max(0, comboCount - 1);
                decayAccumulator -= 1f / DECAY_RATE;
                if (comboCount == 0) { decayAccumulator = 0f; break; }
            }
        }
    }

    @Override
    public void onDamageReceived(Player player, int damage) {}

    @Override
    public float getCooldownMultiplier() {
        return 1f - BONUS_PER_COMBO * comboCount;
    }

    public int getComboCount() {
        return comboCount;
    }
}
