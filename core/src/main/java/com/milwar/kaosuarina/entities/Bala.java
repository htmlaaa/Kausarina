package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.utils.SharedTextures;

public class Bala {
    private static final float SPEED   = 600f;
    private static final float SIZE    = 8f;
    private static final float MAX_DST = 1500f; // distancia desde spawn, no desde origen

    public Vector2 position;
    public Vector2 velocity;
    public boolean active;
    public int damage;
    public int pierceLeft;

    private Vector2 spawnPosition; // ← para medir distancia correctamente

    public Bala() {
        position      = new Vector2();
        velocity      = new Vector2();
        spawnPosition = new Vector2();
        active        = false;
        damage        = 1;
        pierceLeft    = 0;
    }

    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce) {
        position.set(x, y);
        spawnPosition.set(x, y);
        velocity.set(dirX, dirY).nor().scl(SPEED);
        active        = true;
        this.damage   = damage;
        // pierce=0 significa que NO perfora: la bala muere al primer impacto
        // pierce=1 → atraviesa 1 enemigo extra, etc.
        this.pierceLeft = pierce + 1; // +1 para contar el primer impacto
    }

    public void update(float delta) {
        if (!active) return;
        position.add(velocity.x * delta, velocity.y * delta);
        if (position.dst(spawnPosition) > MAX_DST) {
            active = false;
        }
    }

    /** Llamar al impactar un enemigo. Devuelve true si la bala sigue activa. */
    public boolean onHit() {
        pierceLeft--;
        if (pierceLeft <= 0) {
            active = false;
            return false;
        }
        return true;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(SharedTextures.getBala(), position.x - SIZE / 2, position.y - SIZE / 2, SIZE, SIZE);
    }

    public void dispose() {
    }
}
