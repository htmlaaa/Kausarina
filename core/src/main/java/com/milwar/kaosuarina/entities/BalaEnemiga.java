package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class BalaEnemiga {
    private static final float SPEED = 450f;
    private static final float SIZE = 10f;
    private static final float MAX_DST = 1500f;
    private static final int DAMAGE = 8;

    public Vector2 position;
    public Vector2 velocity;
    public boolean active;

    private Vector2 spawnPosition;
    private Texture texture;

    public BalaEnemiga() {
        position = new Vector2();
        velocity = new Vector2();
        spawnPosition = new Vector2();
        active = false;

        Pixmap pixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.9f, 0.2f, 0.2f, 1f);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void activate(float x, float y, float dirX, float dirY) {
        position.set(x, y);
        spawnPosition.set(x, y);
        velocity.set(dirX, dirY).nor().scl(SPEED);
        active = true;
    }

    public void update(float delta) {
        if (!active) return;
        position.add(velocity.x * delta, velocity.y * delta);
        if (position.dst(spawnPosition) > MAX_DST) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(texture, position.x - SIZE / 2, position.y - SIZE / 2, SIZE, SIZE);
    }

    public void deactivate() {
        active = false;
    }

    public int getDamage() {
        return DAMAGE;
    }

    public void dispose() {
        texture.dispose();
    }
}
