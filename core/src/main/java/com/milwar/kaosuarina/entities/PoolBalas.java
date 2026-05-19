package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.weapons.WeaponType;

public class PoolBalas {
    private static final int POOL_SIZE = 500;
    public Array<Bala> balas;

    public PoolBalas() {
        balas = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            balas.add(new Bala());
        }
    }

    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes, DamageType type) {
        for (Bala bala : balas) {
            if (!bala.active) {
                bala.activate(x, y, dirX, dirY, damage, pierce, rebotes, type);
                return;
            }
        }
        // Pool agotado: silent fail intencional para no romper frame
    }

    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes) {
        spawn(x, y, dirX, dirY, damage, pierce, rebotes, DamageType.FISICO);
    }

    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce) {
        spawn(x, y, dirX, dirY, damage, pierce, 0, DamageType.FISICO);
    }

    /** Weapon-system spawn: custom speed, range, and weapon identity. */
    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes,
                      DamageType type, float speed, float maxDst, WeaponType source) {
        for (Bala bala : balas) {
            if (!bala.active) {
                bala.activate(x, y, dirX, dirY, damage, pierce, rebotes, type, speed, maxDst, source);
                return;
            }
        }
    }

    /** Like the weapon-system spawn but returns the activated Bala, or null if pool is full. */
    public Bala spawnReturning(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes,
                               DamageType type, float speed, float maxDst, WeaponType source) {
        for (Bala bala : balas) {
            if (!bala.active) {
                bala.activate(x, y, dirX, dirY, damage, pierce, rebotes, type, speed, maxDst, source);
                return bala;
            }
        }
        return null;
    }

    public void update(float delta) {
        for (Bala bala : balas) bala.update(delta);
    }

    public void render(SpriteBatch batch) {
        for (Bala bala : balas) bala.render(batch);
    }

    public void dispose() {
        for (Bala bala : balas) bala.dispose();
    }
}
