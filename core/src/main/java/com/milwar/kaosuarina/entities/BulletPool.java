package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.weapons.WeaponType;

public class BulletPool {
    private static final int POOL_SIZE = 500;
    public Array<Bullet> bullets;

    public BulletPool() {
        bullets = new Array<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            bullets.add(new Bullet());
        }
    }

    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce, int bounces,
                      DamageType type) {
        for (Bullet b : bullets) {
            if (!b.active) {
                b.activate(x, y, dirX, dirY, damage, pierce, bounces, type);
                return;
            }
        }
    }

    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce, int bounces) {
        spawn(x, y, dirX, dirY, damage, pierce, bounces, DamageType.FISICO);
    }

    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce) {
        spawn(x, y, dirX, dirY, damage, pierce, 0, DamageType.FISICO);
    }

    public void spawn(float x, float y, float dirX, float dirY, int damage, int pierce, int bounces,
                      DamageType type, float speed, float maxDst, WeaponType source) {
        for (Bullet b : bullets) {
            if (!b.active) {
                b.activate(x, y, dirX, dirY, damage, pierce, bounces, type, speed, maxDst, source);
                return;
            }
        }
    }

    public Bullet spawnReturning(float x, float y, float dirX, float dirY, int damage, int pierce,
                                 int bounces, DamageType type, float speed, float maxDst,
                                 WeaponType source) {
        for (Bullet b : bullets) {
            if (!b.active) {
                b.activate(x, y, dirX, dirY, damage, pierce, bounces, type, speed, maxDst, source);
                return b;
            }
        }
        return null;
    }

    public void update(float delta) {
        for (Bullet b : bullets) b.update(delta);
    }

    public void render(SpriteBatch batch) {
        for (Bullet b : bullets) b.render(batch);
    }

    public void dispose() {
        for (Bullet b : bullets) b.dispose();
    }
}
