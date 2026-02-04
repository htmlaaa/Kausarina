package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class pozoEnemy {
    private Array<Enemy> enemies;
    private static final int POOL_SIZE = 100;

    public pozoEnemy() {
        enemies = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            enemies.add(new Enemy());
        }
    }

    public void spawn(float x, float y) {
        for (Enemy enemy : enemies) {
            if (!enemy.active) {
                enemy.init(x, y);
                return;
            }
        }
    }

    public void update(float delta, Vector2 playerPos) {
        for (Enemy enemy : enemies) {
            enemy.update(delta, playerPos);
        }
    }

    public void render(SpriteBatch batch) {
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
    }

    public Array<Enemy> getActiveEnemies() {
        return enemies;
    }

    public void dispose() {
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
    }
}
