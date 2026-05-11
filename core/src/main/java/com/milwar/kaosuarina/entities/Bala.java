package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.utils.SharedTextures;

public class Bala {
    private static final float SPEED = 600f;
    private static final float SIZE = 14f;
    private static final float MAX_DST = 1500f;

    public Vector2 position;
    public Vector2 velocity;
    public boolean    active;
    public int        damage;
    public DamageType damageType;
    public int        pierceLeft;
    public int        rebotesRestantes;

    private Vector2 spawnPosition;

    public Bala() {
        position = new Vector2();
        velocity = new Vector2();
        spawnPosition = new Vector2();
        active           = false;
        damage           = 1;
        damageType       = DamageType.FISICO;
        pierceLeft       = 0;
        rebotesRestantes = 0;
    }

    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes, DamageType type) {
        position.set(x, y);
        spawnPosition.set(x, y);
        velocity.set(dirX, dirY).nor().scl(SPEED);
        active           = true;
        this.damage      = damage;
        this.damageType  = type;
        this.pierceLeft  = pierce + 1;
        this.rebotesRestantes = rebotes;
    }

    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes) {
        activate(x, y, dirX, dirY, damage, pierce, rebotes, DamageType.FISICO);
    }

    public void update(float delta) {
        if (!active) return;
        position.add(velocity.x * delta, velocity.y * delta);
        if (position.dst(spawnPosition) > MAX_DST) active = false;
    }

    /**
     * Llamar al impactar un enemigo. Devuelve true si la bala sigue activa (perforación).
     */
    public boolean onHit() {
        pierceLeft--;
        if (pierceLeft <= 0) {
            active = false;
            return false;
        }
        return true;
    }

    /**
     * Redirige la bala hacia un objetivo y descuenta un rebote. Reactiva la bala si estaba inactiva.
     */
    public void rebotar(float targetX, float targetY) {
        velocity.set(targetX - position.x, targetY - position.y).nor().scl(SPEED);
        spawnPosition.set(position);  // medir MAX_DST desde el punto de rebote
        rebotesRestantes--;
        active = true;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(SharedTextures.getBala(), position.x - SIZE / 2, position.y - SIZE / 2, SIZE, SIZE);
    }

    public void dispose() {
    }
}
