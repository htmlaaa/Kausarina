package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.milwar.kaosuarina.utils.StatusEffect;

public class Enemy {

    private static final float SHOOTER_HUIR_DIST = 300f;
    private static final float SHOOTER_ACERCAR_DIST = 500f;
    private static final float SHOOTER_COOLDOWN = 2f;

    private static final Color BURN_TINT = new Color(1f, 0.3f, 0f, 1f);
    private static final Color POISON_TINT = new Color(0.2f, 1f, 0.2f, 1f);

    private final Color renderColor = new Color();
    private final Color oldColor = new Color();
    private final Color color = new Color();
    private final Vector2 tmp = new Vector2();

    public Vector2 position;
    public Vector2 velocity;
    public boolean active;
    public int health;
    public int maxHealth;
    public Tipo tipo;
    public float defensa;           // reducción de daño físico
    public float resistenciaMagica; // reducción de daño mágico
    public boolean debeExplotar;      // MALDITO: set al morir, procesado por ColisionManager

    public final StatusEffect statusEffect = new StatusEffect();

    private float speed;
    private float size;
    private float shootTimer;

    public Enemy() {
        position = new Vector2();
        velocity = new Vector2();
        active = false;
        shootTimer = 0;
        debeExplotar = false;
    }

    public void activate(float x, float y, Tipo tipo) {
        position.set(x, y);
        velocity.set(0, 0);
        active = true;
        this.tipo = tipo;
        debeExplotar = false;
        shootTimer = MathUtils.random(0f, SHOOTER_COOLDOWN);
        statusEffect.clear();

        defensa = 0f;
        resistenciaMagica = 0f;

        switch (tipo) {
            case BASICO:
                speed = 150f;
                size = 32f;
                health = 30;
                maxHealth = 30;
                color.set(0.2f, 0.9f, 0.3f, 1f);
                break;
            case RAPIDO:
                speed = 300f;
                size = 24f;
                health = 12;
                maxHealth = 12;
                color.set(0.3f, 0.6f, 1f, 1f);
                break;
            case TANQUE:
                speed = 80f;
                size = 48f;
                health = 180;
                maxHealth = 180;
                defensa = 20f;
                color.set(0.9f, 0.2f, 0.2f, 1f);
                break;
            case SHOOTER:
                speed = 100f;
                size = 32f;
                health = 50;
                maxHealth = 50;
                color.set(0.95f, 0.85f, 0.2f, 1f);
                break;
            case MALDITO:
                speed = 130f;
                size = 30f;
                health = 45;
                maxHealth = 45;
                color.set(0.55f, 0.1f, 0.75f, 1f);    // morado oscuro
                break;
            case ESPECTRAL:
                speed = 160f;
                size = 28f;
                health = 35;
                maxHealth = 35;
                resistenciaMagica = 10f;
                color.set(0.5f, 0.9f, 1f, 0.45f); // cian semitransparente
                break;
        }
    }

    public void update(float delta, Vector2 playerPos, PoolBalasEnemigas bulletPool) {
        if (!active) return;

        // Efecto de estado: tick y daño
        if (statusEffect.isActive()) {
            statusEffect.duration -= delta;
            statusEffect.tickTimer -= delta;
            if (statusEffect.tickTimer <= 0) {
                statusEffect.tickTimer += statusEffect.tickInterval();
                health -= statusEffect.damage;
                if (health <= 0) {
                    if (tipo == Tipo.MALDITO) debeExplotar = true;
                    active = false;
                    return;
                }
            }
            if (!statusEffect.isActive()) statusEffect.clear();
        }

        switch (tipo) {
            case BASICO:
            case RAPIDO:
            case TANQUE:
            case MALDITO:
            case ESPECTRAL:
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
        if (damage <= 0) return;
        health -= damage;
        if (health <= 0) {
            if (tipo == Tipo.MALDITO) debeExplotar = true;
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;

        float pct = (float) health / maxHealth;
        renderColor.set(color);
        if (pct < 0.5f) renderColor.mul(0.7f + pct * 0.6f);

        // Tinte de efecto de estado (mezcla 50% con color base)
        if (statusEffect.isActive()) {
            if (statusEffect.tipo == StatusEffect.Tipo.BURN)
                renderColor.lerp(BURN_TINT, 0.5f);
            else if (statusEffect.tipo == StatusEffect.Tipo.POISON)
                renderColor.lerp(POISON_TINT, 0.5f);
        }

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

    public enum Tipo {BASICO, RAPIDO, TANQUE, SHOOTER, MALDITO, ESPECTRAL}
}
