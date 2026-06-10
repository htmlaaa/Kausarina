package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.ui.FontManager;
import com.milwar.kaosuarina.utils.AnimationSheets;
import com.milwar.kaosuarina.utils.Constants;

public class CharacterSelectScreen implements Screen {

    private static final Color COLOR_CABALLERO = new Color(0f, 0.898f, 0.8f, 1f);
    private static final Color COLOR_MAGO = new Color(0.608f, 0.188f, 1f, 1f);
    private static final Color COLOR_SHOOTER = new Color(1f, 0.722f, 0f, 1f);

    private final Role[] ROLES = {Role.caballero(), Role.mago(), Role.shooter()};
    private static final Difficulty[] DIFFICULTIES = Difficulty.values();

    private static final String[][] CORE_LINES = {
        {"Fortaleza Reactiva:", "Recibir dano acumula stacks", "de armadura (max 5, -8% dmg c/u)"},
        {"Resonancia Caotica:", "Balas rebotan al enemigo", "mas cercano (radio 200, 1 rebote)"},
        {"Momentum de Combate:", "Kills acumulan Combo (max 10,", "+3% cadencia / decay -1/s)"}
    };

    private final KaosuarinaGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private int selectedIndex = 0;
    private int selectedDifficulty = 0;
    private float animTimer = 0f;

    public CharacterSelectScreen(KaosuarinaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        animTimer += delta;
        handleInput();

        Gdx.gl.glClearColor(0.05f, 0.02f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(true);
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        renderCards();
        renderTitle();
        renderDifficulty();
        renderHint();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
            selectedIndex = Math.max(0, selectedIndex - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
            selectedIndex = Math.min(ROLES.length - 1, selectedIndex + 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP))
            selectedDifficulty = (selectedDifficulty + DIFFICULTIES.length - 1) % DIFFICULTIES.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN))
            selectedDifficulty = (selectedDifficulty + 1) % DIFFICULTIES.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedIndex = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) selectedIndex = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) selectedIndex = 2;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            confirmar();
    }

    private void confirmar() {
        game.setScreen(new GameScreen(game, ROLES[selectedIndex], DIFFICULTIES[selectedDifficulty]));
    }

    private void renderCards() {
        float cardW = 320f;
        float cardH = 450f;
        float spacing = 40f;
        float totalW = cardW * ROLES.length + spacing * (ROLES.length - 1);
        float startX = (Constants.SCREEN_WIDTH - totalW) / 2f;
        float cardY = (Constants.SCREEN_HEIGHT - cardH) / 2f - 30f;

        BitmapFont fHeading = FontManager.get().heading;
        BitmapFont fLarge = FontManager.get().large;
        BitmapFont fMedium = FontManager.get().medium;
        BitmapFont fSmall = FontManager.get().small;

        for (int i = 0; i < ROLES.length; i++) {
            float cardX = startX + i * (cardW + spacing);
            Role role = ROLES[i];
            boolean sel = i == selectedIndex;
            Color accent = accentColor(i);

            // Background
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(sel ? 0.18f : 0.10f, sel ? 0.12f : 0.06f, sel ? 0.28f : 0.14f, 1f);
            shapeRenderer.rect(cardX, cardY, cardW, cardH);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            // Border
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(sel ? 4 : 2);
            shapeRenderer.setColor(accent);
            shapeRenderer.rect(cardX, cardY, cardW, cardH);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);

            // Separador horizontal sobre la zona de reliquia
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.30f, 0.30f, 0.30f, 1f);
            shapeRenderer.rect(cardX + 8, cardY + 155f, cardW - 16, 1f);
            shapeRenderer.end();

            batch.begin();

            // Nombre — sin numeración, top-left
            fLarge.setColor(accent);
            fLarge.draw(batch, role.nombre, cardX + 12, cardY + cardH - 26);

            // Stats — columna izquierda, fSmall compacto
            fSmall.setColor(Color.WHITE);
            fSmall.draw(batch, "HP:    " + role.stats.maxHealth,                cardX + 12, cardY + cardH - 62);
            fSmall.draw(batch, "DEF:   " + (int) role.stats.physicalDefense,    cardX + 12, cardY + cardH - 80);
            fSmall.draw(batch, "VEL:   " + (int) role.stats.baseSpeed,          cardX + 12, cardY + cardH - 98);
            fSmall.draw(batch, "DMG:  " + role.stats.baseDamage + "x",          cardX + 12, cardY + cardH - 116);
            fSmall.draw(batch, "MP:    " + (int) role.stats.maxMana,            cardX + 12, cardY + cardH - 134);
            if (role.tipo != Role.Tipo.CABALLERO) {
                fSmall.draw(batch, "BALAS: " + role.stats.baseBulletCount,      cardX + 12, cardY + cardH - 152);
            }

            // Sprite — columna derecha, mismo tamaño y posición para todos
            int fc = Math.max(1, AnimationSheets.frameCount(role.tipo, AnimationSheets.Anim.IDLE));
            Texture sprite = AnimationSheets.getFrame(role.tipo, AnimationSheets.Anim.IDLE, 0,
                (int)(animTimer * 4f) % fc);
            if (sprite != null) {
                float sw = 175f;
                if (!sel) batch.setColor(0.6f, 0.6f, 0.6f, 0.8f);
                batch.draw(sprite, cardX + 132f, cardY + cardH - 44 - sw, sw, sw);
                batch.setColor(Color.WHITE);
            }

            // Reliquia — zona inferior, ancho completo de la card
            fSmall.setColor(new Color(0.45f, 0.85f, 0.95f, 1f));
            fSmall.draw(batch, "RELIQUIA:", cardX + 12, cardY + 140);
            fSmall.setColor(Color.LIGHT_GRAY);
            String[] lines = CORE_LINES[i];
            for (int l = 0; l < lines.length; l++) {
                fSmall.draw(batch, lines[l], cardX + 12, cardY + 120 - l * 18);
            }

            fSmall.setColor(Color.WHITE);
            fLarge.setColor(Color.WHITE);
            batch.end();
        }
    }

    private void renderDifficulty() {
        Difficulty diff = DIFFICULTIES[selectedDifficulty];
        BitmapFont fMedium = FontManager.get().medium;
        BitmapFont fSmall = FontManager.get().small;

        float centerX = Constants.SCREEN_WIDTH / 2f;
        float rowY = 90f;

        // Arrow buttons
        batch.begin();
        fMedium.setColor(0.6f, 0.6f, 0.6f, 1f);
        fMedium.draw(batch, "<", centerX - 150f, rowY + 14f);
        fMedium.draw(batch, ">", centerX + 125f, rowY + 14f);

        // Difficulty label
        Color labelColor;
        switch (selectedDifficulty) {
            case 1:
                labelColor = new Color(1f, 0.55f, 0.1f, 1f);
                break;
            case 2:
                labelColor = new Color(1f, 0.2f, 0.2f, 1f);
                break;
            default:
                labelColor = new Color(0.7f, 0.9f, 0.7f, 1f);
                break;
        }
        fMedium.setColor(labelColor);
        fMedium.draw(batch, diff.label, centerX - 50f, rowY + 14f);

        // Description
        fSmall.setColor(0.55f, 0.55f, 0.55f, 1f);
        fSmall.draw(batch, diff.desc, centerX - 140f, rowY - 6f);
        fSmall.setColor(Color.WHITE);
        fMedium.setColor(Color.WHITE);
        batch.end();
    }

    private void renderTitle() {
        BitmapFont fTitle = FontManager.get().title;
        batch.begin();
        fTitle.setColor(Color.GOLD);
        fTitle.draw(batch, "ELIGE TU PERSONAJE",
            (Constants.SCREEN_WIDTH - 620) / 2f, Constants.SCREEN_HEIGHT - 32);
        fTitle.setColor(Color.WHITE);
        batch.end();
    }

    private void renderHint() {
        BitmapFont fSmall = FontManager.get().small;
        batch.begin();
        fSmall.setColor(Color.GRAY);
        fSmall.draw(batch,
            "A/D  Personaje   |   W/S  Dificultad   |   1/2/3  Elegir directo   |   ENTER  Confirmar",
            28, 26);
        fSmall.setColor(Color.WHITE);
        batch.end();
    }

    private Color accentColor(int index) {
        switch (index) {
            case 0:
                return COLOR_CABALLERO;
            case 1:
                return COLOR_MAGO;
            case 2:
                return COLOR_SHOOTER;
            default:
                return Color.WHITE;
        }
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }
}
