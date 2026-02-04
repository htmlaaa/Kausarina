package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public Vector2 position;
    public Vector2 velocity;
    public boolean active;
    public int health;
    private Texture texture;
    private static final float SPEED = 200f;
    private static final float SIZE = 32f;

    public Enemy() {
        position = new Vector2();
        velocity = new Vector2();
        active = false;
        health = 3;


        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 1, 0, 1); // verde
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void init(float x, float y) {
        position.set(x, y);
        velocity.set(0, 0);
        active = true;
        health = 3;
    }

    public void update(float delta, Vector2 playerPos) {
        if (!active) return;

        // Perseguir al jugador
        velocity.set(playerPos).sub(position).nor().scl(SPEED);
        position.add(velocity.x * delta, velocity.y * delta);
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(texture, position.x - SIZE/2, position.y - SIZE/2, SIZE, SIZE);
    }

    public float getRadius() {
        return SIZE / 2;
    }

    public void dispose() {
        texture.dispose();
    }
}
