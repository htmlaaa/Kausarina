package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class PoolEnemigos {
    private Array<Enemy> enemigos;
    private static final int POOL_SIZE = 150;

    public PoolEnemigos() {
        enemigos = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            enemigos.add(new Enemy());
        }
    }

    public void spawn(float x, float y, Enemy.Tipo tipo) {
        for (Enemy e : enemigos) {
            if (!e.active) {
                e.activate(x, y, tipo);
                return;
            }
        }
    }

    /** Spawn con tipo aleatorio según pesos fijos */
    public void spawn(float x, float y) {
        spawn(x, y, tipoAleatorio());
    }

    private Enemy.Tipo tipoAleatorio() {
        float r = MathUtils.random(100f);
        if (r < 50f) return Enemy.Tipo.BASICO;
        if (r < 75f) return Enemy.Tipo.RAPIDO;
        if (r < 90f) return Enemy.Tipo.TANQUE;
        return Enemy.Tipo.SHOOTER;
    }

    public void update(float delta, Vector2 playerPos, PoolBalasEnemigas bulletPool) {
        for (Enemy e : enemigos) e.update(delta, playerPos, bulletPool);
    }

    public void render(SpriteBatch batch) {
        for (Enemy e : enemigos) e.render(batch);
    }

    public Array<Enemy> getEnemigos() { return enemigos; }

    public void dispose() {
        for (Enemy e : enemigos) e.dispose();
    }
}
