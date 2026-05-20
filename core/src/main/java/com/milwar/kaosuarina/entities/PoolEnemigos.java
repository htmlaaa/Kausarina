package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.milwar.kaosuarina.data.DataManager;
import java.util.Arrays;

public class PoolEnemigos {
    private final Array<Enemy> enemigos;
    private static final int POOL_SIZE = 150;
    private final int[] killsByType = new int[Enemy.Tipo.values().length];
    private int currentDepth = 1;

    public PoolEnemigos() {
        enemigos = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            enemigos.add(new Enemy());
        }
    }

    public void setCurrentDepth(int depth) {
        this.currentDepth = depth;
    }

    public void spawn(float x, float y, Enemy.Tipo tipo) {
        for (Enemy e : enemigos) {
            if (!e.active) {
                e.activate(x, y, tipo);
                e.applyDepthScaling(DataManager.getInstance().getDepthScaling(currentDepth));
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
        if (r < 35f) return Enemy.Tipo.BASICO;
        if (r < 62f) return Enemy.Tipo.RAPIDO;   // 27% kamikazes
        if (r < 72f) return Enemy.Tipo.TANQUE;
        if (r < 83f) return Enemy.Tipo.SHOOTER;
        if (r < 93f) return Enemy.Tipo.MALDITO;
        return Enemy.Tipo.ESPECTRAL;
    }

    public void spawnGuardian(float x, float y) {
        for (Enemy e : enemigos) {
            if (!e.active) {
                e.activate(x, y, Enemy.Tipo.GUARDIAN);
                return;
            }
        }
    }

    /** Returns the active GUARDIAN enemy, or null if none is alive. */
    public Enemy getActiveGuardian() {
        for (Enemy e : enemigos) {
            if (e.active && e.tipo == Enemy.Tipo.GUARDIAN) return e;
        }
        return null;
    }

    public void spawnArquero(float x, float y) {
        for (Enemy e : enemigos) {
            if (!e.active) {
                e.activate(x, y, Enemy.Tipo.ARQUERO);
                return;
            }
        }
    }

    /** Returns the active ARQUERO enemy, or null if none is alive. */
    public Enemy getActiveArquero() {
        for (Enemy e : enemigos) {
            if (e.active && e.tipo == Enemy.Tipo.ARQUERO) return e;
        }
        return null;
    }

    public void spawnDevastador(float x, float y) {
        for (Enemy e : enemigos) {
            if (!e.active) {
                e.activate(x, y, Enemy.Tipo.DEVASTADOR);
                return;
            }
        }
    }

    /** Returns the active DEVASTADOR, or null if none is alive. */
    public Enemy getActiveDevastador() {
        for (Enemy e : enemigos) {
            if (e.active && e.tipo == Enemy.Tipo.DEVASTADOR) return e;
        }
        return null;
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
