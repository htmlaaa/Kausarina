package com.milwar.kaosuarina.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

public class HUD implements Disposable {
    private BitmapFont font;
    private BitmapFont timerFont;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera hudCamera;

    private int currentHealth;
    private int maxHealth;
    private int currentMana;
    private int maxMana;
    private int score;
    private float survivalTime;
    private int level;
    private float experience;
    private float expToNextLevel;

    private int screenWidth;
    private int screenHeight;

    public HUD(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Fuente normal para stats (mejorada)
        font = new BitmapFont();
        font.getData().setScale(2.5f); // Más grande
        font.setColor(Color.WHITE);
        font.setUseIntegerPositions(false); // Suavizado
        font.getRegion().getTexture().setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        // Fuente gigante para timer
        timerFont = new BitmapFont();
        timerFont.getData().setScale(2f); // Muy grande
        timerFont.setColor(Color.WHITE);
        timerFont.setUseIntegerPositions(false);
        timerFont.getRegion().getTexture().setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        shapeRenderer = new ShapeRenderer();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenWidth, screenHeight);

        // Valores iniciales
        currentHealth = 100;
        maxHealth = 100;
        currentMana = 10;
        maxMana = 50;
        score = 0;
        survivalTime = 0;
        level = 1;
        experience = 0;
        expToNextLevel = 100;
    }

    public void update(float delta) {
        survivalTime += delta;
    }

    public void render(SpriteBatch batch) {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        // Renderizar barras primero
        renderHealthBar();
        renderManaBar();
        renderExperienceBar();

        batch.begin();

        // HP (arriba izquierda)
        font.draw(batch, "HP", 25, screenHeight - 15);

        // Mana (debajo de HP)
        font.draw(batch, "MP", 25, screenHeight - 55);

        // Score (arriba derecha)
        String scoreText = "Score: " + score;
        font.draw(batch, scoreText, screenWidth - 250, screenHeight - 15);

        // Nivel (arriba izquierda, debajo del mana)
        font.draw(batch, "Lvl " + level, 25, screenHeight - 95);

        // Timer (CENTRO SUPERIOR, grande y sin texto)
        String timeText = String.format("%02d:%02d",
            (int)(survivalTime / 60),
            (int)(survivalTime % 60));

        // Centrar manualmente
        float timerX = (screenWidth / 2f) - 60; // Ajuste manual para centrar
        timerFont.draw(batch, timeText, timerX, screenHeight - 20);

        batch.end();
    }

    // Barra de VIDA (arriba izquierda)
    private void renderHealthBar() {
        float barWidth = 200f;
        float barHeight = 20f;
        float barX = 100f;
        float barY = screenHeight - 40f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo oscuro con transparencia
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Vida actual (gradiente)
        float healthPercent = (float) currentHealth / maxHealth;

        Color barColor;
        if (healthPercent > 0.6f) {
            barColor = new Color(0.2f, 0.9f, 0.3f, 1f); // Verde brillante
        } else if (healthPercent > 0.3f) {
            barColor = new Color(0.95f, 0.85f, 0.2f, 1f); // Amarillo
        } else {
            barColor = new Color(0.95f, 0.2f, 0.2f, 1f); // Rojo brillante
        }

        shapeRenderer.setColor(barColor);
        shapeRenderer.rect(barX + 3, barY + 3, (barWidth - 6) * healthPercent, barHeight - 6);

        shapeRenderer.end();

        // Borde brillante
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(0.9f, 0.9f, 0.9f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    // Barra de MANA (debajo de HP)
    private void renderManaBar() {
        float barWidth = 100f;
        float barHeight = 20f;
        float barX = 100f;
        float barY = screenHeight - 80f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo oscuro
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Mana actual (azul cian brillante)
        float manaPercent = (float) currentMana / maxMana;
        shapeRenderer.setColor(0.2f, 0.6f, 1f, 1f);
        shapeRenderer.rect(barX + 3, barY + 3, (barWidth - 6) * manaPercent, barHeight - 6);

        shapeRenderer.end();

        // Borde
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(0.9f, 0.9f, 0.9f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    // Barra de EXPERIENCIA (centro inferior, MUY fina)
    private void renderExperienceBar() {
        float barWidth = 700f;  // Muy ancha
        float barHeight = 10f;  // Muy fina
        float barX = (screenWidth - barWidth) / 2;
        float barY = 35f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo oscuro sutil
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.7f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Experiencia actual (dorado brillante)
        float expPercent = experience / expToNextLevel;
        shapeRenderer.setColor(1f, 0.9f, 0.3f, 1f);
        shapeRenderer.rect(barX + 2, barY + 2, (barWidth - 4) * expPercent, barHeight - 4);

        shapeRenderer.end();

        // Borde sutil
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(0.7f, 0.7f, 0.7f, 0.9f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    // Métodos públicos
    public void setHealth(int current, int max) {
        this.currentHealth = current;
        this.maxHealth = max;
    }

    public void setMana(int current, int max) {
        this.currentMana = current;
        this.maxMana = max;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addExperience(float exp) {
        this.experience += exp;

        while (this.experience >= expToNextLevel) {
            this.experience -= expToNextLevel;
            this.level++;
            this.expToNextLevel *= 1.2f;
            System.out.println("LEVEL UP! Nivel " + level);
        }
    }

    public float getSurvivalTime() {
        return survivalTime;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public void dispose() {
        font.dispose();
        timerFont.dispose();
        shapeRenderer.dispose();
    }
}
