package com.milwar.kaosuarina.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class HUD implements Disposable {

    private final ShapeRenderer      shapeRenderer;
    private final OrthographicCamera hudCamera;
    private final FitViewport        hudViewport;
    private final GlyphLayout        layout = new GlyphLayout();

    private int   currentHealth;
    private int   maxHealth;
    private float currentMana;
    private float maxMana;
    private float manaFeedbackTimer;
    private int   score;
    private float survivalTime;
    private int   level;
    private float experience;
    private float expToNextLevel;
    private int   waveNumber;
    private final int screenWidth;
    private final int screenHeight;

    private final String[]  slotName        = {"-", "-"};
    private final boolean[] slotIsSkill     = new boolean[2];
    private final float[]   slotCdFraction  = new float[2];
    private final boolean[] slotManaLocked  = new boolean[2];
    private final String[]  slotInscription = {null, null};

    private String coreDisplayText = "";

    private int bossCurrentHealth = 0;
    private int bossMaxHealth     = 0;
    private int bossType          = 0;

    private boolean hudHasSedDeSangre   = false;
    private boolean hudHasGuardianArena = false;

    private String chestPickupPrompt = null;

    public HUD(int screenWidth, int screenHeight) {
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;

        shapeRenderer = new ShapeRenderer();
        hudCamera     = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudViewport   = new FitViewport(screenWidth, screenHeight, hudCamera);

        currentHealth  = 100;
        maxHealth      = 100;
        currentMana    = 0f;
        maxMana        = 0f;
        score          = 0;
        survivalTime   = 0;
        level          = 1;
        experience     = 0;
        expToNextLevel = 100;
        waveNumber     = 0;
    }

    public void resize(int w, int h) {
        hudViewport.update(w, h, true);
    }

    public void update(float delta) {
        survivalTime += delta;
        if (manaFeedbackTimer > 0) manaFeedbackTimer -= delta;
    }

    public void setChestPickupPrompt(String weaponName) { chestPickupPrompt = weaponName; }
    public void clearChestPickupPrompt()                { chestPickupPrompt = null; }

    public void render(SpriteBatch batch) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        renderHealthBar();
        if (maxMana > 0) renderManaBar();
        renderExperienceBar();
        renderWeaponSlots();
        if (bossMaxHealth > 0) renderBossBar();

        BitmapFont fLarge  = FontManager.get().large;
        BitmapFont fMedium = FontManager.get().medium;
        BitmapFont fSmall  = FontManager.get().small;

        batch.begin();

        // HP label + numbers inside bar
        float hpBarX = 100f, hpBarY = screenHeight - 40f, hpBarH = 20f;
        fSmall.setColor(Color.WHITE);
        fSmall.draw(batch, "HP", 25, screenHeight - 17);
        fSmall.draw(batch, currentHealth + " / " + maxHealth, hpBarX + 6, hpBarY + hpBarH - 3);

        // MP numbers inside bar
        if (maxMana > 0) {
            float mpBarY = screenHeight - 80f, mpBarH = 20f;
            fSmall.setColor(manaFeedbackTimer > 0 ? new Color(1f, 0.4f, 0.4f, 1f) : new Color(0.6f, 0.9f, 1f, 1f));
            fSmall.draw(batch, "MP", 25, screenHeight - 57);
            fSmall.draw(batch, (int) currentMana + " / " + (int) maxMana, hpBarX + 6, mpBarY + mpBarH - 3);
        }

        // Level (top-left, below bars)
        fSmall.setColor(new Color(0.75f, 0.75f, 0.75f, 1f));
        fSmall.draw(batch, "Lv " + level, 25, screenHeight - 90);

        // Amulet icons
        if (hudHasSedDeSangre || hudHasGuardianArena) {
            fSmall.setColor(new Color(0.8f, 0.3f, 1f, 1f));
            float iconX = 25;
            if (hudHasSedDeSangre)   { fSmall.draw(batch, "SDS", iconX, screenHeight - 108); iconX += 50; }
            if (hudHasGuardianArena) { fSmall.draw(batch, "GDA", iconX, screenHeight - 108); }
        }

        // Score (top-right)
        fMedium.setColor(Color.WHITE);
        fMedium.draw(batch, "Score: " + score, screenWidth - 235, screenHeight - 12);

        // Wave (top-right, below score)
        if (waveNumber > 0) {
            fSmall.setColor(new Color(0.75f, 0.75f, 0.75f, 1f));
            fSmall.draw(batch, "Oleada " + waveNumber, screenWidth - 168, screenHeight - 38);
        }

        // Core display (ARM X / CMB X)
        if (!coreDisplayText.isEmpty()) {
            fSmall.setColor(new Color(1f, 0.9f, 0.2f, 1f));
            fSmall.draw(batch, coreDisplayText, screenWidth - 120, screenHeight - 56);
        }
        fSmall.setColor(Color.WHITE);

        // Timer centered at top
        String timeText = String.format("%02d:%02d", (int)(survivalTime / 60), (int)(survivalTime % 60));
        layout.setText(fLarge, timeText);
        fLarge.setColor(Color.WHITE);
        fLarge.draw(batch, timeText, (screenWidth - layout.width) / 2f, screenHeight - 8);

        // Boss bar label
        if (bossMaxHealth > 0) {
            String bossName = bossType == 2 ? "DEVASTADOR" : bossType == 1 ? "ARQUERO" : "GUARDIAN";
            Color labelColor = bossType == 2 ? new Color(1f, 0.3f, 0.2f, 1f)
                             : bossType == 1 ? new Color(0.5f, 0.75f, 1f, 1f)
                             :                 new Color(1f, 0.7f, 0.3f, 1f);
            float bossBarX = (screenWidth - 400f) / 2f;
            fSmall.setColor(labelColor);
            fSmall.draw(batch, bossName + "  " + bossCurrentHealth + " / " + bossMaxHealth,
                bossBarX, screenHeight - 55);
            fSmall.setColor(Color.WHITE);
        }

        // Weapon slot labels
        float slotSize = 48f, slotGap = 10f;
        float slotsStartX = (screenWidth - (2 * slotSize + slotGap)) / 2f;
        for (int i = 0; i < 2; i++) {
            if (!slotName[i].equals("-")) {
                float sx = slotsStartX + i * (slotSize + slotGap);
                String abbrev = slotName[i].length() > 4 ? slotName[i].substring(0, 4) : slotName[i];
                fSmall.setColor(Color.WHITE);
                fSmall.draw(batch, abbrev, sx + 2, 60f + 32f);
                if (slotIsSkill[i]) {
                    fSmall.setColor(new Color(0.65f, 0.9f, 0.65f, 1f));
                    fSmall.draw(batch, i == 0 ? "[Q]" : "[E]", sx + 2, 60f + 16f);
                }
                if (slotInscription[i] != null) {
                    fSmall.setColor(new Color(0.5f, 0.8f, 1f, 1f));
                    fSmall.draw(batch, slotInscription[i], sx + 2, 60f - 2f);
                }
            }
        }
        fSmall.setColor(Color.WHITE);

        // Chest pickup prompt — centered above weapon slots
        if (chestPickupPrompt != null) {
            String prompt = "[F]  Recoger:  " + chestPickupPrompt;
            layout.setText(fMedium, prompt);
            fMedium.setColor(new Color(1f, 0.95f, 0.35f, 1f));
            fMedium.draw(batch, prompt, (screenWidth - layout.width) / 2f, 120f);
            fMedium.setColor(Color.WHITE);
        }

        batch.end();
    }

    // ── Bar renderers ────────────────────────────────────────────────────────────

    private void renderHealthBar() {
        float bw = 200f, bh = 20f, bx = 100f, by = screenHeight - 40f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        shapeRenderer.rect(bx, by, bw, bh);
        float pct = (float) currentHealth / maxHealth;
        Color c = pct > 0.6f ? new Color(0.2f, 0.9f, 0.3f, 1f)
                : pct > 0.3f ? new Color(0.95f, 0.85f, 0.2f, 1f)
                :              new Color(0.95f, 0.2f, 0.2f, 1f);
        shapeRenderer.setColor(c);
        shapeRenderer.rect(bx + 2, by + 2, (bw - 4) * pct, bh - 4);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1f);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void renderManaBar() {
        float bw = Math.min(200f, 50f + maxMana), bh = 20f, bx = 100f, by = screenHeight - 80f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        shapeRenderer.rect(bx, by, bw, bh);
        float pct = currentMana / maxMana;
        shapeRenderer.setColor(manaFeedbackTimer > 0 ? new Color(0.95f, 0.15f, 0.15f, 1f) : new Color(0.2f, 0.6f, 1f, 1f));
        shapeRenderer.rect(bx + 2, by + 2, (bw - 4) * pct, bh - 4);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1f);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void renderExperienceBar() {
        float bw = 700f, bh = 10f, bx = (screenWidth - 700f) / 2f, by = 35f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.7f);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.setColor(1f, 0.9f, 0.3f, 1f);
        shapeRenderer.rect(bx + 1, by + 1, (bw - 2) * (experience / expToNextLevel), bh - 2);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(1);
        shapeRenderer.setColor(0.6f, 0.6f, 0.6f, 0.8f);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();
    }

    private void renderWeaponSlots() {
        float slotSize = 48f, slotGap = 10f;
        float startX = (screenWidth - (2 * slotSize + slotGap)) / 2f, slotY = 60f;
        for (int i = 0; i < 2; i++) {
            float x = startX + i * (slotSize + slotGap);
            float cx = x + slotSize / 2f, cy = slotY + slotSize / 2f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 1f);
            shapeRenderer.rect(x, slotY, slotSize, slotSize);
            if (slotCdFraction[i] > 0.01f) {
                shapeRenderer.setColor(0.04f, 0.04f, 0.08f, 0.9f);
                float sweep = slotCdFraction[i] * 360f;
                shapeRenderer.arc(cx, cy, slotSize / 2f - 2f, 90f - sweep, sweep);
            }
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(2);
            shapeRenderer.setColor(slotManaLocked[i] ? 0.9f : 0.55f,
                                   slotManaLocked[i] ? 0.15f : 0.55f,
                                   slotManaLocked[i] ? 0.15f : 0.65f, 1f);
            shapeRenderer.rect(x, slotY, slotSize, slotSize);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);
        }
    }

    private void renderBossBar() {
        float bw = 400f, bh = 22f, bx = (screenWidth - 400f) / 2f, by = screenHeight - 50f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.04f, 0.08f, 0.92f);
        shapeRenderer.rect(bx, by, bw, bh);
        float pct = bossMaxHealth > 0 ? (float) bossCurrentHealth / bossMaxHealth : 0f;
        if      (bossType == 1) shapeRenderer.setColor(0.2f, 0.5f, 1f, 1f);
        else if (bossType == 2) shapeRenderer.setColor(0.75f, 0.05f, 0.1f, 1f);
        else                    shapeRenderer.setColor(1f, 0.45f, 0.05f, 1f);
        shapeRenderer.rect(bx + 2, by + 2, (bw - 4) * pct, bh - 4);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        if      (bossType == 1) shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        else if (bossType == 2) shapeRenderer.setColor(1f, 0.2f, 0.15f, 1f);
        else                    shapeRenderer.setColor(1f, 0.7f, 0.3f, 1f);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    // ── Panel helper ─────────────────────────────────────────────────────────────

    private void renderPanel(float x, float y, float w, float h) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.14f, 0.93f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(0.55f, 0.55f, 0.75f, 1f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // ── Public setters ───────────────────────────────────────────────────────────

    public void setHealth(int current, int max)           { currentHealth = current; maxHealth = max; }
    public void setMana(float current, float max)         { currentMana = current; maxMana = max; }
    public void showManaInsuficienteFeedback()            { manaFeedbackTimer = 0.2f; }
    public void addScore(int points)                      { score += points; }
    public void setLevel(int level)                       { this.level = level; }
    public void setWave(int wave)                         { waveNumber = wave; }
    public void setCoreDisplay(String text)               { coreDisplayText = text != null ? text : ""; }
    public void setAmuletFlags(boolean sds, boolean gda)  { hudHasSedDeSangre = sds; hudHasGuardianArena = gda; }

    public void addExperience(float exp) {
        experience += exp;
        while (experience >= expToNextLevel) {
            experience -= expToNextLevel;
            level++;
            expToNextLevel *= 1.2f;
        }
    }

    public float getSurvivalTime() { return survivalTime; }
    public int   getScore()        { return score; }
    public int   getLevel()        { return level; }

    public void setWeaponSlot(int i, String name, boolean isSkill, float cdFraction, boolean manaLocked) {
        setWeaponSlot(i, name, isSkill, cdFraction, manaLocked, null);
    }

    public void setWeaponSlot(int i, String name, boolean isSkill, float cdFraction, boolean manaLocked, String inscription) {
        if (i < 0 || i >= 2) return;
        slotName[i]        = name;
        slotIsSkill[i]     = isSkill;
        slotCdFraction[i]  = cdFraction;
        slotManaLocked[i]  = manaLocked;
        slotInscription[i] = inscription;
    }

    public void setBossHealth(int current, int max)                { setBossHealth(current, max, 0); }
    public void setBossHealth(int current, int max, boolean isArq) { setBossHealth(current, max, isArq ? 1 : 0); }
    public void setBossHealth(int current, int max, int type)      {
        bossCurrentHealth = Math.max(0, current); bossMaxHealth = max; bossType = type;
    }
    public void clearBossHealth() { bossMaxHealth = 0; bossCurrentHealth = 0; bossType = 0; }

    // ── Overlay renderers (legacy, referenced externally) ────────────────────────

    public void renderVictoria(SpriteBatch batch, String[] leaderboardLines) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        float cx = screenWidth / 2f, cy = screenHeight / 2f;
        BitmapFont fT = FontManager.get().title;
        BitmapFont fL = FontManager.get().large;
        BitmapFont fS = FontManager.get().small;
        fT.setColor(1f, 0.85f, 0.2f, 1f);
        fT.draw(batch, "VICTORIA!", cx - 230, cy + 200);
        fL.setColor(Color.WHITE);
        fL.draw(batch, "Score: " + score,                                                       cx - 160, cy + 100);
        fL.draw(batch, String.format("Tiempo: %02d:%02d", (int)(survivalTime/60), (int)(survivalTime%60)), cx - 160, cy + 62);
        fL.draw(batch, "Nivel: " + level,                                                       cx - 160, cy + 24);
        fL.setColor(new Color(0.7f, 0.85f, 1f, 1f));
        fL.draw(batch, buildArmasText(),                                                        cx - 160, cy - 14);
        renderLeaderboardLines(batch, leaderboardLines, cx, cy - 54, fS);
        fL.setColor(new Color(0.4f, 0.95f, 0.4f, 1f));
        fL.draw(batch, "[R]  Jugar de nuevo",   cx - 160, cy - 230);
        fL.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        fL.draw(batch, "[ESC]  Menu principal", cx - 160, cy - 268);
        fL.setColor(Color.WHITE);
        batch.end();
    }

    public void renderGameOver(SpriteBatch batch, String[] leaderboardLines) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        float cx = screenWidth / 2f, cy = screenHeight / 2f;
        BitmapFont fT = FontManager.get().title;
        BitmapFont fL = FontManager.get().large;
        BitmapFont fS = FontManager.get().small;
        fT.setColor(0.9f, 0.1f, 0.1f, 1f);
        fT.draw(batch, "GAME OVER", cx - 255, cy + 200);
        fL.setColor(Color.WHITE);
        fL.draw(batch, "Score: " + score,                                                       cx - 160, cy + 100);
        fL.draw(batch, String.format("Tiempo: %02d:%02d", (int)(survivalTime/60), (int)(survivalTime%60)), cx - 160, cy + 62);
        fL.draw(batch, "Nivel: " + level,                                                       cx - 160, cy + 24);
        fL.setColor(new Color(0.7f, 0.85f, 1f, 1f));
        fL.draw(batch, buildArmasText(),                                                        cx - 160, cy - 14);
        renderLeaderboardLines(batch, leaderboardLines, cx, cy - 54, fS);
        fL.setColor(new Color(0.4f, 0.95f, 0.4f, 1f));
        fL.draw(batch, "[R]  Jugar de nuevo",   cx - 160, cy - 230);
        fL.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        fL.draw(batch, "[ESC]  Menu principal", cx - 160, cy - 268);
        fL.setColor(Color.WHITE);
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

    private void renderLeaderboardLines(SpriteBatch batch, String[] lines, float cx, float topY, BitmapFont f) {
        if (lines == null || lines.length == 0) return;
        f.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        f.draw(batch, "TOP SCORES", cx - 70, topY);
        f.setColor(new Color(0.82f, 0.82f, 0.82f, 1f));
        for (int i = 0; i < lines.length; i++) f.draw(batch, lines[i], cx - 170, topY - 20 - i * 20);
        f.setColor(Color.WHITE);
    }

    // ── Menu overlays ────────────────────────────────────────────────────────────

    public void renderScrollMenu(SpriteBatch batch, boolean slot0HasWeapon, boolean slot1HasWeapon,
                                 String inscriptionName, float timer) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        float pw = 440f, ph = 190f, px = (screenWidth - pw) / 2f, py = screenHeight / 2f - 40f;
        renderPanel(px, py, pw, ph);

        BitmapFont fL = FontManager.get().large;
        BitmapFont fM = FontManager.get().medium;
        batch.begin();
        float tx = px + 18f, ty = py + ph - 16f;
        fM.setColor(new Color(0.7f, 0.9f, 1f, 1f));
        fM.draw(batch, "INSCRIPCION:  " + inscriptionName, tx, ty);
        fL.setColor(Color.WHITE);
        if (slot0HasWeapon) fL.draw(batch, "[1]  Aplicar a slot 1", tx, ty - 44);
        if (slot1HasWeapon) fL.draw(batch, "[2]  Aplicar a slot 2", tx, ty - 88);
        fL.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        fL.draw(batch, "[X]  Descartar     " + String.format("%.0fs", Math.max(0f, timer)), tx, ty - 140);
        fL.setColor(Color.WHITE);
        batch.end();
    }

    public void renderSwapMenu(SpriteBatch batch, String slot0Name, String slot1Name, String pendingName, float timer) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        float pw = 480f, ph = 210f, px = (screenWidth - pw) / 2f, py = screenHeight / 2f - 55f;
        renderPanel(px, py, pw, ph);

        BitmapFont fL = FontManager.get().large;
        BitmapFont fM = FontManager.get().medium;
        batch.begin();
        float tx = px + 18f, ty = py + ph - 16f;
        fM.setColor(new Color(1f, 0.9f, 0.4f, 1f));
        fM.draw(batch, "NUEVA ARMA:  " + pendingName, tx, ty);
        fL.setColor(Color.WHITE);
        fL.draw(batch, "[1]  Reemplazar:  " + slot0Name, tx, ty - 46);
        fL.draw(batch, "[2]  Reemplazar:  " + slot1Name, tx, ty - 92);
        fL.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        fL.draw(batch, "[X]  Descartar     " + String.format("%.0fs", Math.max(0f, timer)), tx, ty - 150);
        fL.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
