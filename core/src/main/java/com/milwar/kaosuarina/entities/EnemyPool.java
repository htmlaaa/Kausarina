package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.milwar.kaosuarina.data.DataManager;

import java.util.Arrays;

public class EnemyPool {
    private final Array<Enemy> enemies;
    private static final int POOL_SIZE = 150;
    private final int[] killsByType = new int[Enemy.Tipo.values().length];
    private int currentDepth = 1;

    public EnemyPool() {
        enemies = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            enemies.add(new Enemy());
        }
    }

    public void setCurrentDepth(int depth) {
        this.currentDepth = depth;
    }

    public void spawn(float x, float y, Enemy.Tipo tipo) {
        for (Enemy e : enemies) {
            if (!e.active) {
                e.activate(x, y, tipo);
                e.applyDepthScaling(DataManager.getInstance().getDepthScaling(currentDepth));
                return;
            }
        }
    }

    public void spawn(float x, float y) {
        spawn(x, y, randomType());
    }

    private Enemy.Tipo randomType() {
        float r = MathUtils.random(100f);
        if (r < 28f) return Enemy.Tipo.BASICO;
        if (r < 50f) return Enemy.Tipo.RAPIDO;
        if (r < 59f) return Enemy.Tipo.TANQUE;
        if (r < 68f) return Enemy.Tipo.SHOOTER;
        if (r < 76f) return Enemy.Tipo.MALDITO;
        if (r < 82f) return Enemy.Tipo.ESPECTRAL;
        if (r < 88f) return Enemy.Tipo.BERSERKER;
        if (r < 93f) return Enemy.Tipo.SPLITTER;
        if (r < 97f) return Enemy.Tipo.HEALER;
        return Enemy.Tipo.SHIELDER;
    }

    public void spawnAt(float x, float y, Enemy.Tipo tipo) {
        spawn(x, y, tipo);
    }

    public void spawnGuardian(float x, float y) {
        for (Enemy e : enemies) {
            if (!e.active) {
                e.activate(x, y, Enemy.Tipo.GUARDIAN);
                return;
            }
        }
    }

    public Enemy getActiveGuardian() {
        for (Enemy e : enemies) {
            if (e.active && e.tipo == Enemy.Tipo.GUARDIAN) return e;
        }
        return null;
    }

    public void spawnArquero(float x, float y) {
        for (Enemy e : enemies) {
            if (!e.active) {
                e.activate(x, y, Enemy.Tipo.ARQUERO);
                return;
            }
        }
    }

    public Enemy getActiveArquero() {
        for (Enemy e : enemies) {
            if (e.active && e.tipo == Enemy.Tipo.ARQUERO) return e;
        }
        return null;
    }

    public void spawnDevastador(float x, float y) {
        for (Enemy e : enemies) {
            if (!e.active) {
                e.activate(x, y, Enemy.Tipo.DEVASTADOR);
                return;
            }
        }
    }

    public Enemy getActiveDevastador() {
        for (Enemy e : enemies) {
            if (e.active && e.tipo == Enemy.Tipo.DEVASTADOR) return e;
        }
        return null;
    }

    public void spawnFragmentado(float x, float y) {
        for (Enemy e : enemies) {
            if (!e.active) {
                e.activate(x, y, Enemy.Tipo.FRAGMENTADO);
                return;
            }
        }
    }

    public Enemy getActiveFragmentado() {
        for (Enemy e : enemies) {
            if (e.active && e.tipo == Enemy.Tipo.FRAGMENTADO) return e;
        }
        return null;
    }

    public void update(float delta, Vector2 playerPos, EnemyBulletPool bulletPool) {
        for (Enemy e : enemies) e.update(delta, playerPos, bulletPool);
    }

    public void render(SpriteBatch batch) {
        for (Enemy e : enemies) e.render(batch);
    }

    public void registerKill(Enemy.Tipo tipo) {
        killsByType[tipo.ordinal()]++;
    }

    public int[] getKillsByType() {
        return killsByType;
    }

    public void resetKills() {
        Arrays.fill(killsByType, 0);
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }

    public void dispose() {
        for (Enemy e : enemies) e.dispose();
    }
}
