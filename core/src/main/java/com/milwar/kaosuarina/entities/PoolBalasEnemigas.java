package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class PoolBalasEnemigas {
    private static final int POOL_SIZE = 200;
    public Array<BalaEnemiga> balas;

    public PoolBalasEnemigas() {
        balas = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            balas.add(new BalaEnemiga());
        }
    }

    public void spawn(float x, float y, float dirX, float dirY) {
        for (BalaEnemiga bala : balas) {
            if (!bala.active) {
                bala.activate(x, y, dirX, dirY);
                return;
            }
        }
    }

    public void update(float delta) {
        for (BalaEnemiga bala : balas) bala.update(delta);
    }

    public void render(SpriteBatch batch) {
        for (BalaEnemiga bala : balas) bala.render(batch);
    }

    public void dispose() {
        for (BalaEnemiga bala : balas) bala.dispose();
    }
}
