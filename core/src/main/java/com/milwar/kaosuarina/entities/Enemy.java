package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.utils.SharedTextures;

public class Enemy {

    // Rangos de comportamiento del SHOOTER
    private static final float SHOOTER_HUIR_DIST = 300f;
    private static final float SHOOTER_ACERCAR_DIST = 500f;
    private static final float SHOOTER_COOLDOWN = 2f;
    private final Color renderColor = new Color();
    private final Color oldColor    = new Color();
    private final Color color       = new Color();
    private final Vector2 tmp       = new Vector2();
    public Vector2 position;
    public Vector2 velocity;
    public boolean active;
    public int health;
    public int maxHealth;
    public Tipo tipo;
    private float speed;
    private float size;
    private float shootTimer;

    public Enemy() {
        position   = new Vector2();
        velocity   = new Vector2();
        active     = false;
        shootTimer = 0;
    }

    public void activate(float x, float y, Tipo tipo) {
        position.set(x, y);
        velocity.set(0, 0);
        active = true;
        this.tipo = tipo;
        shootTimer = MathUtils.random(0f, SHOOTER_COOLDOWN);

        switch (tipo) {
            case BASICO:
                speed = 150f; size = 32f; health = 3;  maxHealth = 3;
                color.set(0.2f, 0.9f,  0.3f,  1f);
                break;
            case RAPIDO:
                speed = 300f; size = 24f; health = 1;  maxHealth = 1;
                color.set(0.3f, 0.6f,  1f,    1f);
                break;
            case TANQUE:
                speed = 80f;  size = 48f; health = 10; maxHealth = 10;
                color.set(0.9f, 0.2f,  0.2f,  1f);
                break;
            case SHOOTER:
                speed = 100f; size = 32f; health = 5;  maxHealth = 5;
                color.set(0.95f, 0.85f, 0.2f, 1f);
                break;
        }
    }

    public void update(float delta, Vector2 playerPos, PoolBalasEnemigas bulletPool) {
        if (!active) return;

        switch (tipo) {
            case BASICO:
            case RAPIDO:
            case TANQUE:
                velocity.set(playerPos).sub(position).nor().scl(speed);
                break;

            case SHOOTER:
                float dist = position.dst(playerPos);
                if (dist < SHOOTER_HUIR_DIST) {
                    velocity.set(position).sub(playerPos).nor().scl(speed);
                } else if (dist > SHOOTER_ACERCAR_DIST) {
                    velocity.set(playerPos).sub(position).nor().scl(speed * 0.5f);
                } else {
                    tmp.set(playerPos).sub(position);
                    velocity.set(-tmp.y, tmp.x).nor().scl(speed);
                }

                shootTimer -= delta;
                if (shootTimer <= 0 && bulletPool != null) {
                    shootTimer = SHOOTER_COOLDOWN;
                    tmp.set(playerPos).sub(position).nor();
                    bulletPool.spawn(position.x, position.y, tmp.x, tmp.y);
                }
                break;
        }

        position.add(velocity.x * delta, velocity.y * delta);
    }

    public void recibirDanio(int damage) {
        health -= damage;
        if (health <= 0) active = false;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;

        float pct = (float) health / maxHealth;
        renderColor.set(color);
        if (pct < 0.5f) renderColor.mul(0.7f + pct * 0.6f);

        oldColor.set(batch.getColor());
        batch.setColor(renderColor);
        batch.draw(SharedTextures.getEnemyWhite(), position.x - size / 2, position.y - size / 2, size, size);
        batch.setColor(oldColor);
    }

    public float getRadio() {
        return size / 2;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void dispose() {
    }

    public enum Tipo {BASICO, RAPIDO, TANQUE, SHOOTER}
}
