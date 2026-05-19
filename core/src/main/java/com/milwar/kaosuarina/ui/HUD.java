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
    private final BitmapFont font;
    private final BitmapFont timerFont;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera hudCamera;

    private int currentHealth;
    private int maxHealth;
    private float currentMana;
    private float maxMana;
    private float manaFeedbackTimer;
    private int score;
    private float survivalTime;
    private int level;
    private float experience;
    private float expToNextLevel;
    private final int screenWidth;
    private final int screenHeight;

    // Weapon slot display state (updated each frame from GameScreen)
    private final String[]  slotName        = {"-", "-"};
    private final boolean[] slotIsSkill     = new boolean[2];
    private final float[]   slotCdFraction  = new float[2];
    private final boolean[] slotManaLocked  = new boolean[2];
    // S6-07 — inscription name per slot (null = no inscription)
    private final String[]  slotInscription = {null, null};
    // S6-07 — core status display (ARM X / CMB X)
    private String coreDisplayText = "";
    // S6-05 — boss health bar (bossType: 0=guardian, 1=arquero, 2=devastador)
    private int     bossCurrentHealth = 0;
    private int     bossMaxHealth     = 0;
    private int     bossType          = 0;
    // S6-03 — amuleto flags
    private boolean hudHasSedDeSangre   = false;
    private boolean hudHasGuardianArena = false;

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
        currentMana = 0f;
        maxMana = 0f;
        score = 0;
        survivalTime = 0;
        level = 1;
        experience = 0;
        expToNextLevel = 100;
    }

    public void update(float delta) {
        survivalTime += delta;
        if (manaFeedbackTimer > 0) manaFeedbackTimer -= delta;
    }

    public void render(SpriteBatch batch) {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        // Renderizar barras primero
        renderHealthBar();
        if (maxMana > 0) renderManaBar();
        renderExperienceBar();
        renderWeaponSlots();
        if (bossMaxHealth > 0) renderBossBar();

        batch.begin();

        // HP (arriba izquierda)
        font.draw(batch, "HP", 25, screenHeight - 15);

        // Score (arriba derecha)
        String scoreText = "Score: " + score;
        font.draw(batch, scoreText, screenWidth - 250, screenHeight - 15);

        // Nivel (arriba izquierda, debajo de HP)
        font.draw(batch, "Lvl " + level, 25, screenHeight - 55);

        // Timer (CENTRO SUPERIOR, grande y sin texto)
        String timeText = String.format("%02d:%02d",
            (int) (survivalTime / 60),
            (int) (survivalTime % 60));

        // Centrar manualmente
        float timerX = (screenWidth / 2f) - 60; // Ajuste manual para centrar
        timerFont.draw(batch, timeText, timerX, screenHeight - 20);

        // Weapon slot labels + key hints + inscription
        float slotSize = 48f;
        float slotGap  = 10f;
        float slotsStartX = (screenWidth - (2 * slotSize + slotGap)) / 2f;
        font.getData().setScale(1.1f);
        for (int i = 0; i < 2; i++) {
            if (!slotName[i].equals("-")) {
                float sx = slotsStartX + i * (slotSize + slotGap);
                String abbrev = slotName[i].length() > 3 ? slotName[i].substring(0, 3) : slotName[i];
                font.draw(batch, abbrev, sx + 3, 60f + 28f);
                if (slotIsSkill[i]) {
                    font.draw(batch, i == 0 ? "[Q]" : "[E]", sx + 3, 60f + 12f);
                }
                if (slotInscription[i] != null) {
                    font.setColor(0.5f, 0.8f, 1f, 1f);
                    font.draw(batch, slotInscription[i], sx + 3, 60f - 6f);
                    font.setColor(Color.WHITE);
                }
            }
        }
        font.getData().setScale(2.5f);

        // Core display (S6-07): ARM X / CMB X
        if (!coreDisplayText.isEmpty()) {
            font.getData().setScale(1.3f);
            font.setColor(1f, 0.9f, 0.2f, 1f);
            font.draw(batch, coreDisplayText, 25, 115);
            font.setColor(Color.WHITE);
            font.getData().setScale(2.5f);
        }

        // Amuleto icons (S6-03)
        if (hudHasSedDeSangre || hudHasGuardianArena) {
            font.getData().setScale(0.9f);
            font.setColor(0.8f, 0.3f, 1f, 1f);
            int iconX = 25;
            if (hudHasSedDeSangre)   { font.draw(batch, "SDS", iconX, 90); iconX += 55; }
            if (hudHasGuardianArena) { font.draw(batch, "GDA", iconX, 90); }
            font.setColor(Color.WHITE);
            font.getData().setScale(2.5f);
        }

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

    // Barra de MANA (debajo de HP) — width escalado según maxMana
    private void renderManaBar() {
        float barWidth = Math.min(200f, 50f + maxMana);
        float barHeight = 20f;
        float barX = 100f;
        float barY = screenHeight - 80f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo oscuro
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Mana actual — azul cian normal, rojo cuando hay feedback de maná insuficiente
        float manaPercent = currentMana / maxMana;
        if (manaFeedbackTimer > 0) {
            shapeRenderer.setColor(0.95f, 0.15f, 0.15f, 1f);
        } else {
            shapeRenderer.setColor(0.2f, 0.6f, 1f, 1f);
        }
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

    public void setMana(float current, float max) {
        this.currentMana = current;
        this.maxMana = max;
    }

    /** Triggers a 0.2s red blink on the mana bar to signal insufficient mana. */
    public void showManaInsuficienteFeedback() {
        manaFeedbackTimer = 0.2f;
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

    public void setWeaponSlot(int i, String name, boolean isSkill, float cdFraction, boolean manaLocked) {
        setWeaponSlot(i, name, isSkill, cdFraction, manaLocked, null);
    }

    public void setWeaponSlot(int i, String name, boolean isSkill, float cdFraction, boolean manaLocked, String inscriptionName) {
        if (i < 0 || i >= 2) return;
        slotName[i]        = name;
        slotIsSkill[i]     = isSkill;
        slotCdFraction[i]  = cdFraction;
        slotManaLocked[i]  = manaLocked;
        slotInscription[i] = inscriptionName;
    }

    public void setCoreDisplay(String text) {
        coreDisplayText = text != null ? text : "";
    }

    public void setBossHealth(int current, int max) {
        setBossHealth(current, max, 0);
    }

    public void setBossHealth(int current, int max, boolean isArquero) {
        setBossHealth(current, max, isArquero ? 1 : 0);
    }

    public void setBossHealth(int current, int max, int type) {
        bossCurrentHealth = Math.max(0, current);
        bossMaxHealth = max;
        bossType = type;
    }

    public void clearBossHealth() {
        bossMaxHealth = 0;
        bossCurrentHealth = 0;
        bossType = 0;
    }

    public void setAmuletFlags(boolean sedDeSangre, boolean guardianArena) {
        hudHasSedDeSangre   = sedDeSangre;
        hudHasGuardianArena = guardianArena;
    }

    private void renderWeaponSlots() {
        float slotSize   = 48f;
        float slotGap    = 10f;
        float startX     = (screenWidth - (2 * slotSize + slotGap)) / 2f;
        float slotY      = 60f;

        for (int i = 0; i < 2; i++) {
            float x  = startX + i * (slotSize + slotGap);
            float cx = x + slotSize / 2f;
            float cy = slotY + slotSize / 2f;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Slot background
            shapeRenderer.setColor(0.22f, 0.22f, 0.22f, 1f);
            shapeRenderer.rect(x, slotY, slotSize, slotSize);

            // Cooldown arc — dark sector sweeping clockwise from top
            if (slotCdFraction[i] > 0.01f) {
                shapeRenderer.setColor(0.04f, 0.04f, 0.04f, 1f);
                float sweep = slotCdFraction[i] * 360f;
                shapeRenderer.arc(cx, cy, slotSize / 2f - 2f, 90f - sweep, sweep);
            }

            shapeRenderer.end();

            // Border — red if mana locked, grey otherwise
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(2);
            shapeRenderer.setColor(slotManaLocked[i] ? 0.9f : 0.65f,
                                   slotManaLocked[i] ? 0.15f : 0.65f,
                                   slotManaLocked[i] ? 0.15f : 0.65f, 1f);
            shapeRenderer.rect(x, slotY, slotSize, slotSize);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);
        }
    }

    private void renderBossBar() {
        float barWidth = 400f;
        float barHeight = 22f;
        float barX = (screenWidth - barWidth) / 2f;
        float barY = screenHeight - 50f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.05f, 0.1f, 0.9f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        float pct = bossMaxHealth > 0 ? (float) bossCurrentHealth / bossMaxHealth : 0f;
        if (bossType == 1) {
            shapeRenderer.setColor(0.2f, 0.5f, 1f, 1f);       // azul Arquero
        } else if (bossType == 2) {
            shapeRenderer.setColor(0.75f, 0.05f, 0.1f, 1f);   // rojo oscuro Devastador
        } else {
            shapeRenderer.setColor(1f, 0.45f, 0.05f, 1f);     // naranja Guardián
        }
        shapeRenderer.rect(barX + 3, barY + 3, (barWidth - 6) * pct, barHeight - 6);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        if (bossType == 1) {
            shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);       // azul claro
        } else if (bossType == 2) {
            shapeRenderer.setColor(1f, 0.2f, 0.15f, 1f);      // rojo brillante
        } else {
            shapeRenderer.setColor(1f, 0.7f, 0.3f, 1f);       // dorado
        }
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public void renderVictoria(SpriteBatch batch, String[] leaderboardLines) {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;

        timerFont.getData().setScale(5f);
        timerFont.setColor(1f, 0.85f, 0.2f, 1f);
        timerFont.draw(batch, "VICTORIA!", cx - 210, cy + 160);
        timerFont.getData().setScale(2f);
        timerFont.setColor(Color.WHITE);

        font.getData().setScale(1.9f);
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + score,                              cx - 150, cy + 80);
        font.draw(batch, String.format("Tiempo: %02d:%02d",
            (int)(survivalTime / 60), (int)(survivalTime % 60)),        cx - 150, cy + 45);
        font.draw(batch, "Nivel alcanzado: " + level,                   cx - 150, cy + 10);

        font.getData().setScale(1.4f);
        font.setColor(0.7f, 0.85f, 1f, 1f);
        font.draw(batch, buildArmasText(),                               cx - 150, cy - 18);

        renderLeaderboardLines(batch, leaderboardLines, cx, cy - 48);

        font.getData().setScale(1.6f);
        font.setColor(0.4f, 0.95f, 0.4f, 1f);
        font.draw(batch, "[R]  Jugar de nuevo",                         cx - 150, cy - 210);
        font.setColor(0.65f, 0.65f, 0.65f, 1f);
        font.draw(batch, "[ESC]  Menu principal",                       cx - 150, cy - 250);

        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        batch.end();
    }

    public void renderGameOver(SpriteBatch batch, String[] leaderboardLines) {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;

        timerFont.getData().setScale(5f);
        timerFont.setColor(0.9f, 0.1f, 0.1f, 1f);
        timerFont.draw(batch, "GAME OVER", cx - 235, cy + 160);
        timerFont.getData().setScale(2f);
        timerFont.setColor(Color.WHITE);

        font.getData().setScale(1.9f);
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + score,                             cx - 150, cy + 80);
        font.draw(batch, String.format("Tiempo: %02d:%02d",
            (int)(survivalTime / 60), (int)(survivalTime % 60)),       cx - 150, cy + 45);
        font.draw(batch, "Nivel alcanzado: " + level,                  cx - 150, cy + 10);

        font.getData().setScale(1.4f);
        font.setColor(0.7f, 0.85f, 1f, 1f);
        font.draw(batch, buildArmasText(),                              cx - 150, cy - 18);

        renderLeaderboardLines(batch, leaderboardLines, cx, cy - 48);

        font.getData().setScale(1.6f);
        font.setColor(0.4f, 0.95f, 0.4f, 1f);
        font.draw(batch, "[R]  Jugar de nuevo",                        cx - 150, cy - 210);
        font.setColor(0.65f, 0.65f, 0.65f, 1f);
        font.draw(batch, "[ESC]  Menu principal",                      cx - 150, cy - 250);

        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        batch.end();
    }

    public String getArmasText() { return buildArmasText(); }

    private String buildArmasText() {
        StringBuilder sb = new StringBuilder("Armas: ");
        boolean any = false;
        for (int i = 0; i < 2; i++) {
            if (!slotName[i].equals("-")) {
                if (any) sb.append("  |  ");
                sb.append(slotName[i]);
                if (slotInscription[i] != null) sb.append(" [").append(slotInscription[i]).append("]");
                any = true;
            }
        }
        if (!any) sb.append("ninguna");
        return sb.toString();
    }

    private void renderLeaderboardLines(SpriteBatch batch, String[] lines, float cx, float topY) {
        if (lines == null || lines.length == 0) return;
        font.getData().setScale(1.3f);
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "TOP SCORES", cx - 80, topY);
        font.setColor(0.82f, 0.82f, 0.82f, 1f);
        for (int i = 0; i < lines.length; i++) {
            font.draw(batch, lines[i], cx - 170, topY - 28 - i * 26);
        }
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
    }

    public void renderScrollMenu(SpriteBatch batch, boolean slot0HasWeapon, boolean slot1HasWeapon,
                                 String inscriptionName, float timer) {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        float cx = screenWidth / 2f - 190;
        float cy = screenHeight / 2f + 100;
        font.draw(batch, "INSCRIPCION: " + inscriptionName, cx, cy);
        if (slot0HasWeapon) font.draw(batch, "[1] Aplicar slot 1", cx, cy - 45);
        if (slot1HasWeapon) font.draw(batch, "[2] Aplicar slot 2", cx, cy - 90);
        font.draw(batch, "[X] Descartar  " + String.format("%.0f", Math.max(0f, timer)) + "s", cx, cy - 135);
        batch.end();
    }

    public void renderSwapMenu(SpriteBatch batch, String slot0Name, String slot1Name, String pendingName, float timer) {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        float cx = screenWidth / 2f - 160;
        float cy = screenHeight / 2f + 60;
        font.draw(batch, "NUEVA ARMA: " + pendingName,      cx, cy);
        font.draw(batch, "[1] Sustituir: " + slot0Name,     cx, cy - 45);
        font.draw(batch, "[2] Sustituir: " + slot1Name,     cx, cy - 90);
        font.draw(batch, "[X] Descartar  " + String.format("%.0f", Math.max(0f, timer)) + "s", cx, cy - 135);
        batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
        timerFont.dispose();
        shapeRenderer.dispose();
    }
}
