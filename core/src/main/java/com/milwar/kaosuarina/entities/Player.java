package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.Systems.Upgrade;
import com.milwar.kaosuarina.Systems.UpgradeManager;
import com.milwar.kaosuarina.utils.Constants;

public class Player {
    private static final float BASE_SHOOT_COOLDOWN = 0.2f;
    private static final float BASE_SPEED = 400f;
    private static final float INVULNERABILITY_TIME = 1f;

    public Vector2 position;
    public Vector2 velocity;

    private Texture texture;
    private float shootTimer;
    private float invulnerabilityTimer; // tiempo restante de invulnerabilidad tras recibir daño

    private int maxHealth;
    private int currentHealth;
    private boolean alive;

    private UpgradeManager upgradeManager;

    public Player(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        shootTimer = 0;
        invulnerabilityTimer = 0;
        maxHealth = 100;
        currentHealth = maxHealth;
        alive = true;

        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0, 0, 1);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void setUpgradeManager(UpgradeManager manager) {
        this.upgradeManager = manager;
    }

    public void aumentarVidaMaxima(int bonus) {
        maxHealth += bonus;
        currentHealth = Math.min(currentHealth + bonus, maxHealth);
    }

    public void update(float delta, PoolBalas poolBalas) {
        if (!alive) return;

        if (invulnerabilityTimer > 0) invulnerabilityTimer -= delta;

        // Límite de arena: empujar suavemente hacia el centro
        if (position.len() > Constants.ARENA_RADIUS) {
            Vector2 toCenter = new Vector2(position).nor().scl(-1);
            velocity.add(toCenter.scl(BASE_SPEED * 2f));
        }

        position.add(velocity.x * delta, velocity.y * delta);

        float cooldown = BASE_SHOOT_COOLDOWN;
        if (upgradeManager != null) cooldown /= upgradeManager.getMultiplicadorCadencia();

        shootTimer -= delta;
        if (shootTimer <= 0) {
            disparar(poolBalas);
            shootTimer = cooldown;
        }
    }

    public void recibirDanio(int damage) {
        if (!alive || invulnerabilityTimer > 0) return;

        currentHealth -= damage;
        invulnerabilityTimer = INVULNERABILITY_TIME;

        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }

    private void disparar(PoolBalas poolBalas) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float dirX = mouseX - Gdx.graphics.getWidth() / 2f;
        float dirY = mouseY - Gdx.graphics.getHeight() / 2f;

        float anguloBase = (float) Math.atan2(dirY, dirX);

        int totalBalas = 3 + (upgradeManager != null ? upgradeManager.getBalasExtra() : 0);
        float spread = 15f;
        float inicio = -(totalBalas - 1) * spread / 2f;

        int damage = 1;
        int pierce = 0;
        if (upgradeManager != null) {
            damage = Math.max(1, Math.round(upgradeManager.getMultiplicadorDanio()));
            pierce = upgradeManager.getNivelPerforation();
        }

        for (int i = 0; i < totalBalas; i++) {
            float offset = inicio + (i * spread);
            float angulo = anguloBase + offset * MathUtils.degreesToRadians;
            poolBalas.spawn(position.x, position.y, MathUtils.cos(angulo), MathUtils.sin(angulo), damage, pierce);
        }
    }

    public void render(SpriteBatch batch) {
        if (!alive) return;
        // Parpadeo durante invulnerabilidad
        if (invulnerabilityTimer > 0 && ((int) (invulnerabilityTimer * 10) % 2 == 0)) return;
        batch.draw(texture, position.x - 32, position.y - 32, 64, 64);
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return alive;
    }

    public float getRadio() {
        return 32f;
    }

    public float getVelocidadActual() {
        return upgradeManager != null
            ? BASE_SPEED * upgradeManager.getMultiplicadorVelocidad()
            : BASE_SPEED;
    }

    public void dispose() {
        texture.dispose();
    }
}
