package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class pozoBala {
    public Array<Bala> bullets; // ← Cambiar a public
    private static final int POOL_SIZE = 500;

    public pozoBala() {
        bullets = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            bullets.add(new Bala());
        }
    }

    public void spawn(float x, float y, float dirX, float dirY) {
        // Buscar bala inactiva
        for (Bala bala : bullets) {
            if (!bala.active) {
                bala.init(x, y, dirX, dirY);
                return;
            }
        }
    }

    public void update(float delta) {
        for (Bala bala : bullets) {
            bala.update(delta);
        }
    }

    public void render(SpriteBatch batch) {
        for (Bala bala : bullets) {
            bala.render(batch);
        }
    }

    public void dispose() {
        for (Bala bala : bullets) {
            bala.dispose();
        }
    }
}
