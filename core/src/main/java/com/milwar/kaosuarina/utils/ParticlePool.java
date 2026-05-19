package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.milwar.kaosuarina.entities.Enemy;

/**
 * Static pool of simple square particles. Accessed directly from ColisionManager and GameScreen.
 * Call init() once, clear() on restart, update() and render() each frame.
 */
public class ParticlePool {

    private static final int POOL_SIZE = Constants.PARTICLE_POOL_SIZE;
    private static final Particle[] pool = new Particle[POOL_SIZE];
    private static int nextSlot = 0;

    static {
        for (int i = 0; i < POOL_SIZE; i++) pool[i] = new Particle();
    }

    public static void clear() {
        for (Particle p : pool) p.active = false;
        nextSlot = 0;
    }

    public static void update(float delta) {
        for (Particle p : pool) {
            if (p.active) p.update(delta);
        }
    }

    public static void render(ShapeRenderer sr) {
        boolean anyActive = false;
        for (Particle p : pool) { if (p.active) { anyActive = true; break; } }
        if (!anyActive) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : pool) {
            if (!p.active) continue;
            sr.setColor(p.r, p.g, p.b, p.alpha());
            sr.rect(p.x - 1.5f, p.y - 1.5f, 3f, 3f);
        }
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /** Spawn count particles at (x,y) with random direction and given speed/color. */
    public static void spawn(float x, float y, int count, float speed, float life,
                             float r, float g, float b) {
        for (int i = 0; i < count; i++) {
            Particle p = nextFree();
            float angle = (float)(Math.random() * Math.PI * 2);
            float s = speed * (0.5f + (float)Math.random() * 0.5f);
            p.activate(x, y, (float)Math.cos(angle) * s, (float)Math.sin(angle) * s,
                       life * (0.6f + (float)Math.random() * 0.4f), r, g, b);
        }
    }

    /** Convenience: spawn death particles with color matching enemy type. */
    public static void spawnDeath(float x, float y, Enemy.Tipo tipo) {
        float[] c = colorForTipo(tipo);
        spawn(x, y, Constants.PARTICLE_COUNT_DEATH, Constants.PARTICLE_SPEED_DEATH,
              Constants.PARTICLE_LIFE_DEATH, c[0], c[1], c[2]);
    }

    /** Convenience: spawn bullet impact sparks. */
    public static void spawnImpact(float x, float y) {
        spawn(x, y, Constants.PARTICLE_COUNT_IMPACT, Constants.PARTICLE_SPEED_IMPACT,
              Constants.PARTICLE_LIFE_IMPACT, 1f, 0.9f, 0.3f);
    }

    /** Convenience: spawn MALDITO explosion burst. */
    public static void spawnExplosion(float x, float y) {
        spawn(x, y, Constants.PARTICLE_COUNT_EXPLOSION, 180f,
              Constants.PARTICLE_LIFE_DEATH, 0.6f, 0f, 0.9f);
    }

    private static Particle nextFree() {
        for (int i = 0; i < POOL_SIZE; i++) {
            int idx = (nextSlot + i) % POOL_SIZE;
            if (!pool[idx].active) {
                nextSlot = (idx + 1) % POOL_SIZE;
                return pool[idx];
            }
        }
        // Pool full: recycle oldest (nextSlot)
        Particle p = pool[nextSlot];
        nextSlot = (nextSlot + 1) % POOL_SIZE;
        return p;
    }

    private static float[] colorForTipo(Enemy.Tipo tipo) {
        switch (tipo) {
            case RAPIDO:    return new float[]{1f, 0.9f, 0.1f};
            case TANQUE:    return new float[]{0.5f, 0.3f, 0.1f};
            case SHOOTER:   return new float[]{0.3f, 0.5f, 1f};
            case MALDITO:   return new float[]{0.6f, 0f, 0.9f};
            case ESPECTRAL: return new float[]{0.2f, 0.9f, 0.9f};
            case GUARDIAN:  return new float[]{1f, 0.5f, 0.1f};
            case ARQUERO:    return new float[]{0.3f, 0.5f, 1f};
            case DEVASTADOR: return new float[]{0.4f, 0f, 0.6f};
            default:         return new float[]{0.7f, 0.7f, 0.7f};
        }
    }
}
