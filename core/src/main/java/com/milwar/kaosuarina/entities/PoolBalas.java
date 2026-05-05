package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class PoolBalas {
    private static final int POOL_SIZE = 500;
    public Array<Bala> balas;

    public PoolBalas() {
        balas = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            balas.add(new Bala());
        }
    }

    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce) {
        for (Bala bala : balas) {
            if (!bala.active) {
                bala.activate(x, y, dirX, dirY, damage, pierce);
                return;
            }
        }
        // Pool agotado: no hacemos nada (silent fail intencional para no romper frame)
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
