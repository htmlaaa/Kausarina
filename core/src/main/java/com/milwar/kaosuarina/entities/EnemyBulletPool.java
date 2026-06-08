package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class EnemyBulletPool {
    private static final int POOL_SIZE = 200;
    public Array<EnemyBullet> bullets;

    public EnemyBulletPool() {
        bullets = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            bullets.add(new EnemyBullet());
        }
    }

    public void spawn(float x, float y, float dirX, float dirY) {
        for (EnemyBullet b : bullets) {
            if (!b.active) {
                b.activate(x, y, dirX, dirY);
                return;
            }
        }
    }

    public void spawnWithDamage(float x, float y, float dirX, float dirY, int damage) {
        for (EnemyBullet b : bullets) {
            if (!b.active) {
                b.activate(x, y, dirX, dirY, damage);
                return;
            }
        }
    }

    public void update(float delta) {
        for (EnemyBullet b : bullets) b.update(delta);
    }

    public void render(SpriteBatch batch) {
        for (EnemyBullet b : bullets) b.render(batch);
    }

    public void dispose() {
        for (EnemyBullet b : bullets) b.dispose();
    }
}
