package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.utils.Constants;

public class Player {
    private static final float SHOOT_COOLDOWN = 0.2f;
    private static final float BASE_SPEED = 400f;
    private static final float SPEED_INCREMENT = 50f;
    private static final float SPEED_INTERVAL = 10f;
    public Vector2 position;
    public Vector2 velocity;
    private Texture texture;
    private float shootTimer;
    // Sistema de velocidad progresiva
    private float currentSpeed;

    // ← NUEVO: Sistema de vida
    private int currentHealth;
    private int maxHealth;
    private boolean isAlive;
    private int currentMana;
    private int maxMana;


    public Player(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        shootTimer = 0;
        currentSpeed = BASE_SPEED;

        // ← NUEVO
        maxHealth = 100;
        currentHealth = maxHealth;
        isAlive = true;
        maxMana = 50;
        currentMana = maxMana;

        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0, 0, 1);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta, pozoBala pozoBala) {
        if (!isAlive) return; // ← NUEVO: no actualizar si está muerto

        position.add(velocity.x * delta, velocity.y * delta);

        if (position.len() > Constants.ARENA_RADIUS) {
            position.scl(-0.9f);
        }

        // Auto-shoot
        shootTimer -= delta;
        if (shootTimer <= 0) {
            shoot(pozoBala);
            shootTimer = SHOOT_COOLDOWN;
        }
    }

    // ← NUEVO: Recibir daño
    public void takeDamage(int damage) {
        if (!isAlive) return;

        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isAlive = false;
            System.out.println("PLAYER DIED!");
        }
    }

    // ← NUEVO: Getters
    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    private void shoot(pozoBala pozoBala) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float dirX = mouseX - Gdx.graphics.getWidth() / 2f;
        float dirY = mouseY - Gdx.graphics.getHeight() / 2f;

        float baseAngle = (float) Math.atan2(dirY, dirX);

        float[] angles = {-15f, 0f, 15f};

        for (float angleOffset : angles) {
            float angleRad = baseAngle + angleOffset * MathUtils.degreesToRadians;
            float newDirX = MathUtils.cos(angleRad);
            float newDirY = MathUtils.sin(angleRad);

            pozoBala.spawn(position.x, position.y, newDirX, newDirY);
        }
    }

    public void render(SpriteBatch batch) {
        if (!isAlive) return; // ← NUEVO: no renderizar si está muerto
        batch.draw(texture, position.x - 32, position.y - 32, 64, 64);
    }

    public void dispose() {
        texture.dispose();
    }
}
