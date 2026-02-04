package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Bala {
    private static final float SPEED = 600f;
    private static final float SIZE = 8f;
    public Vector2 position;
    public Vector2 velocity;
    public boolean active;
    private Texture texture;

    public Bala() {
        position = new Vector2();
        velocity = new Vector2();
        active = false;

        // Bala amarilla brillante
        Pixmap pixmap = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 0, 1); // amarillo
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void init(float x, float y, float dirX, float dirY) {
        position.set(x, y);
        velocity.set(dirX, dirY).nor().scl(SPEED);
        active = true;
    }

    public void update(float delta) {
        if (!active) return;
        position.add(velocity.x * delta, velocity.y * delta);

        // Desaparece si esta lejos
        if (position.len() > 3000f) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(texture, position.x - SIZE / 2, position.y - SIZE / 2, SIZE, SIZE);
    }

    public void dispose() {
        texture.dispose();
    }
}
