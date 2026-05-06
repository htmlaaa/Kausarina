package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.Systems.PlayerStats;
import com.milwar.kaosuarina.Systems.UpgradeManager;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.SharedTextures;

public class Player {
    private static final float INVULNERABILITY_TIME = 1f;

    public Vector2 position;
    public Vector2 velocity;

    private final PlayerStats stats;
    private final Vector2 arenaDir = new Vector2();

    private float shootTimer;
    private float invulnerabilityTimer;

    private int maxHealth;
    private int currentHealth;
    private boolean alive;

    private UpgradeManager upgradeManager;

    public Player(float x, float y) {
        this(x, y, new PlayerStats());
    }

    public Player(float x, float y, PlayerStats stats) {
        this.stats = stats;
        position   = new Vector2(x, y);
        velocity   = new Vector2();
        shootTimer = 0;
        invulnerabilityTimer = 0;
        maxHealth    = stats.maxHealth;
        currentHealth = maxHealth;
        alive = true;
    }

    public void setUpgradeManager(UpgradeManager manager) {
        this.upgradeManager = manager;
    }

    public void aumentarVidaMaxima(int bonus) {
        maxHealth += bonus;
        currentHealth = Math.min(currentHealth + bonus, maxHealth);
    }

    public void update(float delta, PoolBalas poolBalas, float aimAngle) {
        if (!alive) return;

        if (invulnerabilityTimer > 0) invulnerabilityTimer -= delta;

        if (position.len() > Constants.ARENA_RADIUS) {
            arenaDir.set(position).nor().scl(-stats.baseSpeed * 2f);
            velocity.add(arenaDir);
        }

        position.add(velocity.x * delta, velocity.y * delta);

        float cooldown = stats.baseShootCooldown;
        if (upgradeManager != null) cooldown /= upgradeManager.getMultiplicadorCadencia();

        shootTimer -= delta;
        if (shootTimer <= 0) {
            disparar(poolBalas, aimAngle);
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

    private void disparar(PoolBalas poolBalas, float aimAngle) {
        int totalBalas = stats.baseBulletCount + (upgradeManager != null ? upgradeManager.getBalasExtra() : 0);
        float inicio = -(totalBalas - 1) * stats.bulletSpread / 2f;

        int damage = 1;
        int pierce = 0;
        if (upgradeManager != null) {
            damage = Math.max(1, Math.round(upgradeManager.getMultiplicadorDanio()));
            pierce = upgradeManager.getNivelPerforation();
        }

        for (int i = 0; i < totalBalas; i++) {
            float offset = inicio + (i * stats.bulletSpread);
            float angulo = aimAngle + offset * MathUtils.degreesToRadians;
            poolBalas.spawn(position.x, position.y, MathUtils.cos(angulo), MathUtils.sin(angulo), damage, pierce);
        }
    }

    public void render(SpriteBatch batch) {
        if (!alive) return;
        if (invulnerabilityTimer > 0 && ((int) (invulnerabilityTimer * 10) % 2 == 0)) return;
        batch.draw(SharedTextures.getPlayer(), position.x - 32, position.y - 32, 64, 64);
    }

    public int getCurrentHealth()  { return currentHealth; }
    public int getMaxHealth()      { return maxHealth; }
    public boolean isAlive()       { return alive; }
    public float getRadio()        { return 32f; }

    public float getVelocidadActual() {
        return upgradeManager != null
            ? stats.baseSpeed * upgradeManager.getMultiplicadorVelocidad()
            : stats.baseSpeed;
    }

    public void dispose() {
    }
}
