package com.milwar.kaosuarina.utils;

public class Particle {
    public float x, y;
    public float vx, vy;
    public float life, maxLife;
    public float r, g, b;
    public boolean active;

    public void activate(float x, float y, float vx, float vy, float life, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.r = r;
        this.g = g;
        this.b = b;
        this.active = true;
    }

    public void update(float delta) {
        if (!active) return;
        x += vx * delta;
        y += vy * delta;
        life -= delta;
        if (life <= 0) active = false;
    }

    /**
     * Alpha 0→1 based on remaining life.
     */
    public float alpha() {
        return maxLife > 0 ? life / maxLife : 0f;
    }
}
