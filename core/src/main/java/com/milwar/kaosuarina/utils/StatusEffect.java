package com.milwar.kaosuarina.utils;

public class StatusEffect {
    public enum Tipo {NONE, BURN, POISON}

    private static final float BURN_TICK_INTERVAL = 0.5f;
    private static final float POISON_TICK_INTERVAL = 1.0f;

    public Tipo tipo = Tipo.NONE;
    public float duration = 0f;
    public float tickTimer = 0f;
    public int damage = 0;

    /**
     * BURN takes priority over POISON. Same type refreshes/extends duration.
     */
    public void apply(Tipo tipo, float duration, int damage) {
        if (this.tipo == Tipo.BURN && tipo == Tipo.POISON) return;
        boolean typeChanged = (this.tipo != tipo);
        this.tipo = tipo;
        this.duration = Math.max(this.duration, duration);
        this.damage = damage;
        if (typeChanged || tickTimer <= 0) tickTimer = tickInterval();
    }

    public boolean isActive() {
        return tipo != Tipo.NONE && duration > 0f;
    }

    public float tickInterval() {
        return tipo == Tipo.BURN ? BURN_TICK_INTERVAL : POISON_TICK_INTERVAL;
    }

    public void clear() {
        tipo = Tipo.NONE;
        duration = 0f;
        tickTimer = 0f;
        damage = 0;
    }
}
