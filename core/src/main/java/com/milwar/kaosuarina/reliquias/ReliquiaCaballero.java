package com.milwar.kaosuarina.reliquias;

import com.milwar.kaosuarina.entities.Player;

/**
 * Reliquia del Caballero — Fortaleza Reactiva.
 * Al recibir daño: gana 1 stack de armadura (max 5, -8% daño por stack).
 * Sin daño durante 4s → pierde 1 stack/s hasta 0.
 */
public class ReliquiaCaballero implements Reliquia {

    private static final int MAX_STACKS = 5;
    private static final float DECAY_DELAY = 4f;
    private static final float REDUCTION_PER_STACK = 0.08f;

    private int armorStacks = 0;
    private float timeSinceLastHit = 0f;
    private float decayAccumulator = 0f;

    @Override
    public void onDamageReceived(Player player, int damage) {
        if (armorStacks > 0) armorStacks--;
        armorStacks = Math.min(armorStacks + 1, MAX_STACKS);
        timeSinceLastHit = 0f;
        decayAccumulator = 0f;
    }

    @Override
    public void onUpdate(Player player, float delta) {
        timeSinceLastHit += delta;
        if (timeSinceLastHit > DECAY_DELAY && armorStacks > 0) {
            decayAccumulator += delta;
            while (decayAccumulator >= 1f && armorStacks > 0) {
                armorStacks--;
                decayAccumulator -= 1f;
            }
        }
    }

    @Override
    public void onKill(Player player) {
    }

    @Override
    public float getDamageReduction() {
        return REDUCTION_PER_STACK * armorStacks;
    }

    public int getArmorStacks() {
        return armorStacks;
    }
}
