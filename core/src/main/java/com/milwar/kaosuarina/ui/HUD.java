package com.milwar.kaosuarina.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class HUD implements Disposable {

    // ── WeaponCard DTO — filled by GameScreen, rendered here ───────────────────
    public static class WeaponCard {
        public final String name;          // pre-formatted ("Espada Verdugo")
        public final String tierId;        // "T1"-"T5" or null
        public final String dmgType;       // DamageType.name() e.g. "FISICO"
        public final int damage;        // baseDamage value
        public final String inscription;   // inscription name or null
        public final boolean matchesRole;   // affinity matches player's role
        public final String affinityLabel; // "Caballero", "Mago", "Shooter", "Neutro"

        public final String affixLabel;  // "+8 DMG  -10% CD" o null si no tiene afijos

        public WeaponCard(String name, String tierId, String dmgType, int damage,
                          String inscription, boolean matchesRole, String affinityLabel,
                          String affixLabel) {
            this.name = name;
            this.tierId = tierId;
            this.dmgType = dmgType;
            this.damage = damage;
            this.inscription = inscription;
            this.matchesRole = matchesRole;
            this.affinityLabel = affinityLabel;
            this.affixLabel = affixLabel;
        }
    }

    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera hudCamera;
    private final FitViewport hudViewport;
    private final int screenWidth;
    private final int screenHeight;
    // Slots 0-1 = activos (armas equipadas), 2-5 = almacenamiento
    private final String[] slotName = {"-", "-", "-", "-", "-", "-"};
    private final boolean[] slotIsSkill = new boolean[6];
    private final float[] slotCdFraction = new float[6];
    private final boolean[] slotManaLocked = new boolean[6];
    private final String[] slotInscription = new String[6];
    private final String[] slotTierId = new String[6];
    private final String[] slotAffix = new String[6];
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
    private int waveNumber;
    private String relicLabel = "";
    private int relicStacks = 0;

    // MEC-03 — Combo floater y flash al llegar a máximo
    private String comboFloaterText = "";
    private float comboFloaterTimer = 0f;
    private float comboFlashTimer = 0f;
    private static final float COMBO_FLOATER_DURATION = 1.2f;
    private static final float COMBO_FLASH_DURATION = 0.5f;

    // CNT-05 — Notificación de evolución de arma
    private String evolutionText = "";
    private float evolutionTimer = 0f;
    private static final float EVOLUTION_DURATION = 2.5f;

    private int bossCurrentHealth = 0;
    private int bossMaxHealth = 0;
    private int bossType = 0;

    private boolean hudHasSedDeSangre = false;
    private boolean hudHasGuardianArena = false;
    // MEC-01 — Amulet slots (3 slots visibles)
    private final String[] amuletSlotName = {"", "", ""};

    private String chestPickupPrompt = null;

    public HUD(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        shapeRenderer = new ShapeRenderer();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudViewport = new FitViewport(screenWidth, screenHeight, hudCamera);

        currentHealth = 100;
        maxHealth = 100;
        currentMana = 0f;
        maxMana = 0f;
        score = 0;
        survivalTime = 0;
        level = 1;
        experience = 0;
        expToNextLevel = 100;
        waveNumber = 0;
    }

    public void resize(int w, int h) {
        hudViewport.update(w, h, true);
    }

    public void update(float delta) {
        survivalTime += delta;
        if (manaFeedbackTimer > 0) manaFeedbackTimer -= delta;
        if (comboFloaterTimer > 0) comboFloaterTimer -= delta;
        if (comboFlashTimer > 0) comboFlashTimer -= delta;
        if (evolutionTimer > 0) evolutionTimer -= delta;
    }

    public void showEvolutionNotification(String weaponName) {
        evolutionText = "¡EVOLUCIÓN! " + weaponName;
        evolutionTimer = EVOLUTION_DURATION;
    }

    /**
     * Muestra texto "+COMBO xN" flotando sobre la barra de combo.
     */
    public void showComboFloater(int count) {
        comboFloaterText = "+COMBO x" + count;
        comboFloaterTimer = COMBO_FLOATER_DURATION;
    }

    /**
     * Activa el flash dorado de la barra al llegar al combo máximo.
     */
    public void flashComboMax() {
        comboFlashTimer = COMBO_FLASH_DURATION;
    }

    public void setChestPickupPrompt(String weaponName) {
        chestPickupPrompt = weaponName;
    }

    public void clearChestPickupPrompt() {
        chestPickupPrompt = null;
    }

    public void setRelicDisplay(String label, int stacks) {
        relicLabel = label != null ? label : "";
        relicStacks = stacks;
    }

    public void render(SpriteBatch batch) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        renderHealthBar();
        if (maxMana > 0) renderManaBar();
        renderExperienceBar();
        renderWeaponSlots();
        renderAmuletSlots();
        if (bossMaxHealth > 0) renderBossBar();
        renderRelicBar();

        BitmapFont fLarge = FontManager.get().large;
        BitmapFont fMedium = FontManager.get().medium;
        BitmapFont fSmall = FontManager.get().small;

        batch.begin();

        // ── Zona top-left: HP / MP / Lv ────────────────────────────────────
        float barLabelX = 20f;
        float barStartX = 68f;
        float hpBarY = screenHeight - 28f;
        float mpBarY = screenHeight - 52f;
        float lvY = screenHeight - 72f;

        // HP
        fSmall.setColor(Color.WHITE);
        fSmall.draw(batch, "HP", barLabelX, hpBarY + 14f);
        fSmall.draw(batch, currentHealth + " / " + maxHealth, barStartX + 4, hpBarY + 14f);

        // MP
        if (maxMana > 0) {
            Color mpColor = manaFeedbackTimer > 0 ? new Color(1f, 0.4f, 0.4f, 1f) : new Color(0.6f, 0.9f, 1f, 1f);
            fSmall.setColor(mpColor);
            fSmall.draw(batch, "MP", barLabelX, mpBarY + 14f);
            fSmall.draw(batch, (int) currentMana + " / " + (int) maxMana, barStartX + 4, mpBarY + 14f);
        }

        // Lv
        fSmall.setColor(new Color(0.75f, 0.75f, 0.75f, 1f));
        fSmall.draw(batch, "Lv " + level, barLabelX, lvY);

        // Relic indicator (slot pequeño debajo de MP)
        if (!relicLabel.isEmpty()) {
            fSmall.setColor(new Color(0.85f, 0.7f, 1f, 1f));
            String relicText = relicStacks > 0 ? relicLabel + " x" + relicStacks : relicLabel;
            fSmall.draw(batch, relicText, barLabelX, lvY - 18f);
            fSmall.setColor(Color.WHITE);
        }

        // ── Score (top-right) ─────────────────────────────────────────────
        fMedium.setColor(Color.WHITE);
        fMedium.draw(batch, "Score: " + score, screenWidth - 235, screenHeight - 12);

        // ── Timer (top-center) ────────────────────────────────────────────
        String timeText = String.format("%02d:%02d", (int) (survivalTime / 60), (int) (survivalTime % 60));
        fLarge.setColor(Color.WHITE);
        fLarge.draw(batch, timeText, screenWidth / 2f - 36, screenHeight - 12);

        // ── Left panel slot labels ────────────────────────────────────────
        float labelX = LSLOT_X + LSLOT_SIZE + 6f;
        for (int i = 0; i < 6; i++) {
            if (slotName[i].equals("-")) continue;
            float sy = lslotY(i);
            String display = formatWeaponName(slotName[i]);
            fSmall.setColor(tierColor(slotTierId[i]));
            fSmall.draw(batch, display, labelX, sy + LSLOT_SIZE - 2f);
            if (i < 2) {
                // Skill hint
                if (slotIsSkill[i]) {
                    fSmall.setColor(0.7f, 0.7f, 0.7f, 1f);
                    fSmall.draw(batch, i == 0 ? "[Q]" : "[E]", labelX, sy + 16f);
                }
                // Inscription
                if (slotInscription[i] != null) {
                    fSmall.setColor(0.5f, 0.8f, 1f, 1f);
                    fSmall.draw(batch, slotInscription[i], labelX, sy + 2f);
                }
            }
            fSmall.setColor(Color.WHITE);
        }
        // TAB hint
        fSmall.setColor(0.45f, 0.45f, 0.45f, 1f);
        fSmall.draw(batch, "[TAB]", LSLOT_X, lslotY(5) + LSLOT_SIZE + 16f);
        fSmall.setColor(Color.WHITE);

        // ── Amulet slot labels (MEC-01) ───────────────────────────────────
        renderAmuletLabels(batch);

        // ── Evolución de arma (CNT-05) ────────────────────────────────────
        if (evolutionTimer > 0 && !evolutionText.isEmpty()) {
            float alpha = Math.min(1f, evolutionTimer / (EVOLUTION_DURATION * 0.3f));
            float pulse = 0.8f + 0.2f * com.badlogic.gdx.math.MathUtils.sin(evolutionTimer * 10f);
            fMedium.setColor(1f, pulse * 0.85f, 0.1f * pulse, alpha);
            fMedium.draw(batch, evolutionText,
                screenWidth / 2f - 180f, screenHeight / 2f - 40f);
            fMedium.setColor(Color.WHITE);
        }

        // ── Combo floater (MEC-03) ────────────────────────────────────────
        if (comboFloaterTimer > 0 && !comboFloaterText.isEmpty()) {
            float alpha = Math.min(1f, comboFloaterTimer / (COMBO_FLOATER_DURATION * 0.4f));
            float barY = screenHeight - 110f;
            float ty = barY + 18f + 8f * (1f - (comboFloaterTimer / COMBO_FLOATER_DURATION));
            fSmall.setColor(1f, 0.65f, 0.1f, alpha);
            fSmall.draw(batch, comboFloaterText, 20f, ty);
            fSmall.setColor(Color.WHITE);
        }

        // ── Chest pickup prompt ───────────────────────────────────────────
        if (chestPickupPrompt != null) {
            fMedium.setColor(new Color(1f, 0.9f, 0.4f, 1f));
            fMedium.draw(batch, "[F]  Recoger: " + formatWeaponName(chestPickupPrompt),
                screenWidth / 2f - 140, screenHeight / 2f - 60);
            fMedium.setColor(Color.WHITE);
        }

        batch.end();
    }

    // ── Bar renderers ─────────────────────────────────────────────────────────

    private void renderHealthBar() {
        float barWidth = 200f, barHeight = 18f;
        float barX = 68f, barY = screenHeight - 30f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.12f, 0.12f, 0.88f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        float pct = maxHealth > 0 ? (float) currentHealth / maxHealth : 0f;
        Color c = pct > 0.6f ? new Color(0.2f, 0.9f, 0.3f, 1f)
            : pct > 0.3f ? new Color(0.95f, 0.85f, 0.2f, 1f)
              : new Color(0.95f, 0.2f, 0.2f, 1f);
        shapeRenderer.setColor(c);
        shapeRenderer.rect(barX + 2, barY + 2, (barWidth - 4) * pct, barHeight - 4);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(0.7f, 0.7f, 0.7f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void renderManaBar() {
        float barWidth = Math.min(200f, 50f + maxMana), barHeight = 18f;
        float barX = 68f, barY = screenHeight - 54f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.12f, 0.12f, 0.88f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        float pct = maxMana > 0 ? currentMana / maxMana : 0f;
        shapeRenderer.setColor(manaFeedbackTimer > 0 ? new Color(0.95f, 0.15f, 0.15f, 1f) : new Color(0.2f, 0.6f, 1f, 1f));
        shapeRenderer.rect(barX + 2, barY + 2, (barWidth - 4) * pct, barHeight - 4);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(0.7f, 0.7f, 0.7f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void renderExperienceBar() {
        float barWidth = 700f, barHeight = 10f;
        float barX = (screenWidth - barWidth) / 2f, barY = 35f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.7f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.setColor(1f, 0.9f, 0.3f, 1f);
        shapeRenderer.rect(barX + 2, barY + 2, (barWidth - 4) * (experience / expToNextLevel), barHeight - 4);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(0.7f, 0.7f, 0.7f, 0.9f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    // ── Left-panel slot layout constants ─────────────────────────────────────
    private static final float LSLOT_X = 10f;
    private static final float LSLOT_SIZE = 46f;
    private static final float LSLOT_GAP = 8f;
    private static final float LSLOT_SEP = 14f; // extra gap between active/storage groups

    // active slots at bottom (y=66, y=120); storage above (y=200..344)
    private static float lslotY(int i) {
        if (i == 0) return 66f;
        if (i == 1) return 66f + LSLOT_SIZE + LSLOT_GAP;
        return 66f + 2 * (LSLOT_SIZE + LSLOT_GAP) + LSLOT_SEP + (i - 2) * (LSLOT_SIZE + LSLOT_GAP);
    }

    private void renderWeaponSlots() {
        for (int i = 0; i < 6; i++) {
            float x = LSLOT_X;
            float y = lslotY(i);
            float cx = x + LSLOT_SIZE / 2f;
            float cy = y + LSLOT_SIZE / 2f;
            boolean active = i < 2;
            boolean manaLk = active && slotManaLocked[i];

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(active ? 0.20f : 0.12f, active ? 0.20f : 0.12f, active ? 0.20f : 0.12f, 1f);
            shapeRenderer.rect(x, y, LSLOT_SIZE, LSLOT_SIZE);
            if (active && slotCdFraction[i] > 0.01f) {
                shapeRenderer.setColor(0.04f, 0.04f, 0.04f, 1f);
                float sweep = slotCdFraction[i] * 360f;
                shapeRenderer.arc(cx, cy, LSLOT_SIZE / 2f - 2f, 90f - sweep, sweep);
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(active ? 2 : 1);
            if (manaLk) shapeRenderer.setColor(0.9f, 0.15f, 0.15f, 1f);
            else if (active && slotIsSkill[i]) shapeRenderer.setColor(1.0f, 0.85f, 0.0f, 0.9f);
            else if (active) shapeRenderer.setColor(0.75f, 0.75f, 0.75f, 1f);
            else shapeRenderer.setColor(0.38f, 0.38f, 0.38f, 1f);
            shapeRenderer.rect(x, y, LSLOT_SIZE, LSLOT_SIZE);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);
        }
        // Separator line between active/storage groups
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        float sepY = 66f + 2 * (LSLOT_SIZE + LSLOT_GAP) + 3f;
        shapeRenderer.rect(LSLOT_X, sepY, LSLOT_SIZE, 2f);
        shapeRenderer.end();
    }

    private void renderAmuletSlots() {
        float slotW = 40f, slotH = 18f, gap = 4f;
        float startX = LSLOT_X;
        // Place amulet row below the storage group + a small margin
        float rowY = lslotY(5) - slotH - 10f;

        BitmapFont fS = FontManager.get().small;
        for (int i = 0; i < 3; i++) {
            float x = startX + i * (slotW + gap);
            boolean filled = !amuletSlotName[i].isEmpty();

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(filled ? 0.18f : 0.10f, filled ? 0.18f : 0.08f, filled ? 0.08f : 0.06f, 1f);
            shapeRenderer.rect(x, rowY, slotW, slotH);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(filled ? 0.85f : 0.40f, filled ? 0.65f : 0.40f, 0.10f, 1f);
            shapeRenderer.rect(x, rowY, slotW, slotH);
            shapeRenderer.end();

            if (filled) {
                BitmapFont font = FontManager.get().small;
                com.badlogic.gdx.graphics.g2d.SpriteBatch sb = null;
                // We need a batch — use the batch already started in render(); render amulet text
                // deferred to the batch section below; store position for later
            }
        }
        // Draw labels in a separate batch block
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.end(); // flush
    }

    private void renderAmuletLabels(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        float slotW = 40f, slotH = 18f, gap = 4f;
        float startX = LSLOT_X;
        float rowY = lslotY(5) - slotH - 10f;
        BitmapFont fS = FontManager.get().small;
        for (int i = 0; i < 3; i++) {
            if (amuletSlotName[i].isEmpty()) continue;
            float x = startX + i * (slotW + gap);
            fS.setColor(0.95f, 0.75f, 0.25f, 1f);
            fS.draw(batch, amuletSlotName[i], x + 2f, rowY + slotH - 2f);
        }
        fS.setColor(Color.WHITE);
    }

    private void renderBossBar() {
        float bw = 400f, bh = 22f;
        float bx = (screenWidth - bw) / 2f, by = screenHeight - 50f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.05f, 0.1f, 0.9f);
        shapeRenderer.rect(bx, by, bw, bh);
        float pct = bossMaxHealth > 0 ? (float) bossCurrentHealth / bossMaxHealth : 0f;
        if (bossType == 1) shapeRenderer.setColor(0.2f, 0.5f, 1f, 1f);
        else if (bossType == 2) shapeRenderer.setColor(0.75f, 0.05f, 0.1f, 1f);
        else shapeRenderer.setColor(1f, 0.45f, 0.05f, 1f);
        shapeRenderer.rect(bx + 2, by + 2, (bw - 4) * pct, bh - 4);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        if (bossType == 1) shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        else if (bossType == 2) shapeRenderer.setColor(1f, 0.2f, 0.15f, 1f);
        else shapeRenderer.setColor(1f, 0.7f, 0.3f, 1f);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    // ── Panel helper ──────────────────────────────────────────────────────────

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

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static String formatWeaponName(String raw) {
        if (raw == null) return "";
        // ESPADA_VERDUGO → Espada Verdugo
        String[] parts = raw.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    // ── Tier color ────────────────────────────────────────────────────────────

    private Color tierColor(String tierId) {
        if (tierId == null) return Color.WHITE;
        switch (tierId) {
            case "T2":
                return Color.GREEN;
            case "T3":
                return Color.CYAN;
            case "T4":
                return new Color(0.7f, 0.3f, 1f, 1f);
            case "T5":
                return Color.GOLD;
            default:
                return Color.WHITE;
        }
    }

    // ── Public setters ────────────────────────────────────────────────────────

    public void setHealth(int current, int max) {
        currentHealth = current;
        maxHealth = max;
    }

    public void setMana(float current, float max) {
        currentMana = current;
        maxMana = max;
    }

    public void showManaInsuficienteFeedback() {
        manaFeedbackTimer = 0.2f;
    }

    public void addScore(int points) {
        score += points;
    }

    public void setWave(int wave) {
        waveNumber = wave;
    }

    public void setCoreDisplay(String text) { /* deprecated — use setRelicDisplay */ }

    public void setAmuletFlags(boolean sds, boolean gda) {
        hudHasSedDeSangre = sds;
        hudHasGuardianArena = gda;
    }

    public void setAmuletSlots(com.milwar.kaosuarina.items.AmuletType[] slots) {
        for (int i = 0; i < 3; i++) {
            amuletSlotName[i] = (slots != null && i < slots.length && slots[i] != null)
                ? amuletShortName(slots[i]) : "";
        }
    }

    private static String amuletShortName(com.milwar.kaosuarina.items.AmuletType t) {
        switch (t) {
            case SED_DE_SANGRE:
                return "Sed";
            case GUARDIAN_DE_LA_ARENA:
                return "Guarda";
            case PIEL_DE_PIEDRA:
                return "+40HP";
            case BOTAS_RAPIDAS:
                return "Botas";
            case COLLAR_VAMPIRICO:
                return "Vampiro";
            case TOTEM_REGEN:
                return "Regen";
            case TALISMAN_MANA:
                return "+25MP";
            case AMULETO_CRITICO:
                return "+Crit";
            case AMULETO_EXPLOSION:
                return "Explo";
            case AMULETO_ESPECTROS:
                return "Espect";
            case AMULETO_TIEMPO:
                return "Tiempo";
            case AMULETO_ARMADURA:
                return "+DEF";
            default:
                return t.name();
        }
    }

    public void addExperience(float exp) {
        experience += exp;
        while (experience >= expToNextLevel) {
            experience -= expToNextLevel;
            level++;
            expToNextLevel *= 1.2f;
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

    public void setLevel(int level) {
        this.level = level;
    }

    public void setWeaponSlot(int i, String name, boolean isSkill, float cdFraction, boolean manaLocked) {
        setWeaponSlot(i, name, isSkill, cdFraction, manaLocked, null, null);
    }

    public void setWeaponSlot(int i, String name, boolean isSkill, float cdFraction, boolean manaLocked, String inscription) {
        setWeaponSlot(i, name, isSkill, cdFraction, manaLocked, inscription, null);
    }

    public void setWeaponSlot(int i, String name, boolean isSkill, float cdFraction, boolean manaLocked, String inscription, String tierId) {
        if (i < 0 || i >= 6) return;
        slotName[i] = name != null ? name : "-";
        slotIsSkill[i] = isSkill;
        slotCdFraction[i] = cdFraction;
        slotManaLocked[i] = manaLocked;
        slotInscription[i] = inscription;
        slotTierId[i] = tierId;
    }

    public void setSlotAffix(int i, String affix) {
        if (i >= 0 && i < 6) slotAffix[i] = affix;
    }

    public void setBossHealth(int current, int max) {
        setBossHealth(current, max, 0);
    }

    public void setBossHealth(int current, int max, boolean isArq) {
        setBossHealth(current, max, isArq ? 1 : 0);
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

    // ── Overlay renderers ─────────────────────────────────────────────────────

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
        fL.draw(batch, "Score: " + score, cx - 160, cy + 100);
        fL.draw(batch, String.format("Tiempo: %02d:%02d", (int) (survivalTime / 60), (int) (survivalTime % 60)), cx - 160, cy + 62);
        fL.draw(batch, "Nivel: " + level, cx - 160, cy + 24);
        fL.setColor(new Color(0.7f, 0.85f, 1f, 1f));
        fL.draw(batch, buildArmasText(), cx - 160, cy - 14);
        renderLeaderboardLines(batch, leaderboardLines, cx, cy - 54, fS);
        fL.setColor(new Color(0.4f, 0.95f, 0.4f, 1f));
        fL.draw(batch, "[R]  Jugar de nuevo", cx - 160, cy - 230);
        fL.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        fL.draw(batch, "[ESC]  Menu principal", cx - 160, cy - 268);
        fL.setColor(Color.WHITE);
        fT.setColor(Color.WHITE);
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
        fL.draw(batch, "Score: " + score, cx - 160, cy + 100);
        fL.draw(batch, String.format("Tiempo: %02d:%02d", (int) (survivalTime / 60), (int) (survivalTime % 60)), cx - 160, cy + 62);
        fL.draw(batch, "Nivel: " + level, cx - 160, cy + 24);
        fL.setColor(new Color(0.7f, 0.85f, 1f, 1f));
        fL.draw(batch, buildArmasText(), cx - 160, cy - 14);
        renderLeaderboardLines(batch, leaderboardLines, cx, cy - 54, fS);
        fL.setColor(new Color(0.4f, 0.95f, 0.4f, 1f));
        fL.draw(batch, "[R]  Jugar de nuevo", cx - 160, cy - 230);
        fL.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        fL.draw(batch, "[ESC]  Menu principal", cx - 160, cy - 268);
        fL.setColor(Color.WHITE);
        fT.setColor(Color.WHITE);
        batch.end();
    }

    public String getArmasText() {
        return buildArmasText();
    }

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

    // ── Nightreign-style weapon swap menu ─────────────────────────────────────

    public void renderSwapMenuCards(SpriteBatch batch, WeaponCard newW,
                                    WeaponCard slot0, WeaponCard slot1, float timer) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        float pw = 730f, ph = 310f;
        float px = (screenWidth - pw) / 2f, py = screenHeight / 2f - ph / 2f;
        renderPanel(px, py, pw, ph);

        float divX = px + pw * 0.51f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.35f, 0.35f, 0.55f, 0.7f);
        shapeRenderer.rect(divX, py + 36f, 2f, ph - 52f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        BitmapFont fM = FontManager.get().medium;
        BitmapFont fS = FontManager.get().small;

        batch.begin();

        float lx = px + 18f;
        float ty = py + ph - 20f;

        // ── Left: new weapon ─────────────────────────────────────────────────
        fM.setColor(new Color(1f, 0.85f, 0.3f, 1f));
        fM.draw(batch, "NUEVA ARMA", lx, ty);

        if (newW != null) {
            fM.setColor(tierColor(newW.tierId));
            fM.draw(batch, newW.name, lx, ty - 32f);

            fS.setColor(tierColor(newW.tierId));
            fS.draw(batch, newW.tierId != null ? newW.tierId : "T1", lx, ty - 56f);

            if (newW.affixLabel != null) {
                fS.setColor(new Color(1f, 0.85f, 0.4f, 1f));
                fS.draw(batch, newW.affixLabel, lx, ty - 74f);
            }

            fS.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
            fS.draw(batch, dmgTypeLabel(newW.dmgType) + "   " + newW.damage + " dmg", lx, ty - (newW.affixLabel != null ? 92f : 74f));

            float shift = newW.affixLabel != null ? 18f : 0f;
            if (newW.matchesRole) {
                fS.setColor(new Color(0.3f, 1f, 0.3f, 1f));
                fS.draw(batch, "+" + newW.affinityLabel + "  +15% dmg", lx, ty - 92f - shift);
            } else {
                fS.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
                fS.draw(batch, newW.affinityLabel, lx, ty - 92f - shift);
            }

            if (newW.inscription != null) {
                fS.setColor(new Color(0.5f, 0.85f, 1f, 1f));
                fS.draw(batch, "Inscr: " + newW.inscription, lx, ty - 110f - shift);
            } else {
                fS.setColor(new Color(0.38f, 0.38f, 0.38f, 1f));
                fS.draw(batch, "Sin inscripción", lx, ty - 110f - shift);
            }
        }
        fS.setColor(Color.WHITE);
        fM.setColor(Color.WHITE);

        // ── Right: slot mini-cards ───────────────────────────────────────────
        float rx = divX + 14f;
        float cardW = (px + pw - rx - 14f) / 2f - 6f;
        drawMiniCard(batch, slot0, rx, ty, "[1]", fM, fS);
        drawMiniCard(batch, slot1, rx + cardW + 12f, ty, "[2]", fM, fS);

        // ── Footer ───────────────────────────────────────────────────────────
        fS.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        fS.draw(batch, "[X]  Descartar     " + String.format("%.0fs", Math.max(0f, timer)), lx, py + 18f);
        fS.setColor(Color.WHITE);

        batch.end();
    }

    private void drawMiniCard(SpriteBatch batch, WeaponCard card, float x, float ty,
                              String key, BitmapFont fM, BitmapFont fS) {
        fS.setColor(new Color(0.7f, 0.85f, 1f, 1f));
        fS.draw(batch, key + "  Reemplazar:", x, ty);

        if (card == null) {
            fS.setColor(new Color(0.35f, 0.35f, 0.35f, 1f));
            fS.draw(batch, "VACÍO", x, ty - 22f);
        } else {
            fS.setColor(tierColor(card.tierId));
            fS.draw(batch, card.name, x, ty - 22f);
            fS.setColor(new Color(0.62f, 0.62f, 0.62f, 1f));
            String info = (card.tierId != null ? card.tierId : "—") + "  " + dmgTypeLabel(card.dmgType);
            fS.draw(batch, info, x, ty - 40f);
            if (card.matchesRole) {
                fS.setColor(new Color(0.3f, 1f, 0.3f, 1f));
                fS.draw(batch, "+" + card.affinityLabel, x, ty - 58f);
            } else {
                fS.setColor(new Color(0.45f, 0.45f, 0.45f, 1f));
                fS.draw(batch, card.affinityLabel, x, ty - 58f);
            }
            if (card.inscription != null) {
                fS.setColor(new Color(0.5f, 0.85f, 1f, 1f));
                fS.draw(batch, card.inscription, x, ty - 76f);
            }
            if (card.affixLabel != null) {
                fS.setColor(new Color(1f, 0.85f, 0.4f, 1f));
                fS.draw(batch, card.affixLabel, x, ty - (card.inscription != null ? 94f : 76f));
            }
        }
        fS.setColor(Color.WHITE);
    }

    private static String dmgTypeLabel(String name) {
        if (name == null) return "Físico";
        switch (name) {
            case "FISICO":
                return "Físico";
            case "MAGICO":
                return "Mágico";
            case "A_DISTANCIA":
                return "A Dist.";
            case "FUEGO":
                return "Fuego";
            case "VENENO":
                return "Veneno";
            case "CAOS":
                return "Caos";
            case "CAOS_PRIMORDIAL":
                return "Primordial";
            default:
                return name;
        }
    }

    // ── Pause menu ────────────────────────────────────────────────────────────

    public void renderPauseMenu(SpriteBatch batch) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        float pw = 420f, ph = 250f;
        float px = (screenWidth - pw) / 2f, py = (screenHeight - ph) / 2f;
        renderPanel(px, py, pw, ph);

        BitmapFont fH = FontManager.get().heading;
        BitmapFont fL = FontManager.get().large;

        batch.begin();
        float lx = px + 28f;
        float ty = py + ph - 24f;

        fH.setColor(Color.WHITE);
        fH.draw(batch, "PAUSA", lx, ty);

        fL.setColor(new Color(0.4f, 0.95f, 0.4f, 1f));
        fL.draw(batch, "[1]  Continuar", lx, ty - 68f);

        fL.setColor(new Color(0.9f, 0.4f, 0.4f, 1f));
        fL.draw(batch, "[2]  Salir al menú principal", lx, ty - 114f);

        fL.setColor(new Color(0.55f, 0.55f, 0.55f, 1f));
        fL.draw(batch, "[ESC]  Continuar", lx, ty - 162f);

        fL.setColor(Color.WHITE);
        fH.setColor(Color.WHITE);
        batch.end();
    }

    // ── 6-slot inventory overlay ──────────────────────────────────────────────

    /**
     * Card rect for inventory slot i: {x, y, w, h} in HUD coordinates.
     */
    private static float[] inventoryCardRect(int slot) {
        float cardW = 290f, cardH = 108f, colGap = 20f, rowGap = 14f;
        float panelX = (1280f - 2 * cardW - colGap) / 2f;
        float panelTopY = 160f + 3 * cardH + 2 * rowGap; // top of topmost card
        int col = slot % 2;
        int row = slot / 2;
        float x = panelX + col * (cardW + colGap);
        float y = panelTopY - row * (cardH + rowGap) - cardH;
        return new float[]{x, y, cardW, cardH};
    }

    public int getInventoryHoveredSlot(int screenX, int screenY) {
        com.badlogic.gdx.math.Vector3 v = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
        hudViewport.unproject(v);
        float hx = v.x, hy = v.y;
        for (int i = 0; i < 6; i++) {
            float[] r = inventoryCardRect(i);
            if (hx >= r[0] && hx <= r[0] + r[2] && hy >= r[1] && hy <= r[1] + r[3]) return i;
        }
        return -1;
    }

    public void renderInventoryOverlay(SpriteBatch batch, int hoveredSlot, int selectedSlot) {
        hudViewport.apply();
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        // Dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.72f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        BitmapFont fM = FontManager.get().medium;
        BitmapFont fS = FontManager.get().small;
        BitmapFont fH = FontManager.get().heading;

        // Title
        batch.begin();
        fH.setColor(Color.WHITE);
        fH.draw(batch, "INVENTARIO  [TAB]  Cerrar", 340f, 575f);
        fH.setColor(Color.WHITE);
        batch.end();

        for (int i = 0; i < 6; i++) {
            float[] r = inventoryCardRect(i);
            boolean active = i < 2;
            boolean hovered = i == hoveredSlot;
            boolean selected = i == selectedSlot;
            boolean empty = slotName[i].equals("-");

            // Card background
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (selected) shapeRenderer.setColor(0.35f, 0.25f, 0.02f, 1f);
            else if (hovered) shapeRenderer.setColor(0.22f, 0.22f, 0.32f, 1f);
            else if (active) shapeRenderer.setColor(0.18f, 0.18f, 0.26f, 1f);
            else shapeRenderer.setColor(0.10f, 0.10f, 0.14f, 1f);
            shapeRenderer.rect(r[0], r[1], r[2], r[3]);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(selected ? 3 : hovered ? 2 : 1);
            if (selected) shapeRenderer.setColor(Color.GOLD);
            else if (hovered) shapeRenderer.setColor(Color.WHITE);
            else if (active) shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            else shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
            shapeRenderer.rect(r[0], r[1], r[2], r[3]);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);

            batch.begin();
            float tx = r[0] + 10f;
            float ty = r[1] + r[3] - 10f;

            // Slot type badge
            fS.setColor(active ? new Color(0.85f, 0.85f, 0.55f, 1f) : new Color(0.5f, 0.5f, 0.5f, 1f));
            String badge = active ? "ACTIVO " + (i + 1) : "GUARDADO " + (i - 1);
            fS.draw(batch, badge, tx, ty);

            if (empty) {
                fS.setColor(0.35f, 0.35f, 0.35f, 1f);
                fS.draw(batch, "VACÍO", tx, ty - 22f);
            } else {
                fM.setColor(tierColor(slotTierId[i]));
                fM.draw(batch, formatWeaponName(slotName[i]), tx, ty - 20f);
                fS.setColor(0.6f, 0.6f, 0.6f, 1f);
                String tier = slotTierId[i] != null ? slotTierId[i] : "T1";
                fS.draw(batch, tier, tx, ty - 44f);
                if (slotAffix[i] != null) {
                    fS.setColor(1f, 0.85f, 0.4f, 1f);
                    fS.draw(batch, slotAffix[i], tx, ty - 62f);
                }
                if (slotInscription[i] != null) {
                    fS.setColor(0.5f, 0.85f, 1f, 1f);
                    fS.draw(batch, slotInscription[i], tx, ty - 62f);
                }
                if (i < 2 && slotIsSkill[i]) {
                    fS.setColor(0.7f, 0.7f, 0.7f, 1f);
                    fS.draw(batch, i == 0 ? "[Q]" : "[E]", tx, ty - 80f);
                }
            }
            fS.setColor(Color.WHITE);
            fM.setColor(Color.WHITE);
            batch.end();
        }

        // Bottom hint
        batch.begin();
        fS.setColor(0.5f, 0.5f, 0.5f, 1f);
        fS.draw(batch, "Click  Seleccionar / Intercambiar     [ESC]  Cerrar", 320f, 134f);
        fS.setColor(Color.WHITE);
        batch.end();
    }

    private void renderRelicBar() {
        if (relicLabel.isEmpty() || relicStacks <= 0) return;
        int maxStacks;
        float r1, g1, b1, r2, g2, b2;
        if (relicLabel.equals("Combo")) {
            maxStacks = 10;
            r1 = 0.2f;
            g1 = 0.9f;
            b1 = 0.2f;
            r2 = 1.0f;
            g2 = 0.6f;
            b2 = 0.0f;
        } else if (relicLabel.equals("Armadura")) {
            maxStacks = 5;
            r1 = 0.4f;
            g1 = 0.7f;
            b1 = 1.0f;
            r2 = 0.4f;
            g2 = 0.7f;
            b2 = 1.0f;
        } else {
            return;
        }
        float segW = 8f, segH = 6f, gap = 2f;
        float startX = 20f;
        float barY = screenHeight - 110f;
        boolean flashing = relicLabel.equals("Combo") && comboFlashTimer > 0;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int s = 0; s < maxStacks; s++) {
            float t = maxStacks > 1 ? (float) s / (maxStacks - 1) : 1f;
            float r, g, b;
            if (flashing) {
                float pulse = 0.5f + 0.5f * com.badlogic.gdx.math.MathUtils.sin(
                    comboFlashTimer * 25f);
                r = 1f;
                g = 0.75f + 0.25f * pulse;
                b = 0f;
            } else {
                r = r1 + (r2 - r1) * t;
                g = g1 + (g2 - g1) * t;
                b = b1 + (b2 - b1) * t;
            }
            if (s < relicStacks) shapeRenderer.setColor(r, g, b, 1f);
            else shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.8f);
            shapeRenderer.rect(startX + s * (segW + gap), barY, segW, segH);
        }
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
