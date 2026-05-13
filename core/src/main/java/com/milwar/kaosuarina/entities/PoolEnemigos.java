package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;

public class PoolEnemigos {
    private final Array<Enemy> enemigos;
    private static final int POOL_SIZE = 150;
    private final int[] killsByType = new int[Enemy.Tipo.values().length];

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

    /**
     * Spawn con tipo aleatorio según pesos fijos
     */
    public void spawn(float x, float y) {
        spawn(x, y, tipoAleatorio());
    }

    private Enemy.Tipo tipoAleatorio() {
        float r = MathUtils.random(100f);
        if (r < 40f) return Enemy.Tipo.BASICO;
        if (r < 60f) return Enemy.Tipo.RAPIDO;
        if (r < 70f) return Enemy.Tipo.TANQUE;
        if (r < 80f) return Enemy.Tipo.SHOOTER;
        if (r < 92f) return Enemy.Tipo.MALDITO;
        return Enemy.Tipo.ESPECTRAL;
    }

    public void update(float delta, Vector2 playerPos, PoolBalasEnemigas bulletPool) {
        for (Enemy e : enemigos) e.update(delta, playerPos, bulletPool);
    }

    public void render(SpriteBatch batch) {
        for (Enemy e : enemigos) e.render(batch);
    }

    public void registrarKill(Enemy.Tipo tipo) {
        killsByType[tipo.ordinal()]++;
    }

    public int[] getKillsByType() {
        return killsByType;
    }

    public void resetKills() {
        Arrays.fill(killsByType, 0);
    }

    public Array<Enemy> getEnemigos() {
        return enemigos;
    }

    public void dispose() {
        for (Enemy e : enemigos) e.dispose();
    }
}
