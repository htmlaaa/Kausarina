package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.utils.Constants;

public class CharacterSelectScreen implements Screen {

    private static final Color COLOR_CABALLERO = new Color(0f, 0.898f, 0.8f, 1f);
    private static final Color COLOR_MAGO = new Color(0.608f, 0.188f, 1f, 1f);
    private static final Color COLOR_SHOOTER = new Color(1f, 0.722f, 0f, 1f);

    private final Role[] ROLES = {Role.caballero(), Role.mago(), Role.shooter()};

    private static final String[][] CORE_LINES = {
        {"Fortaleza Reactiva:", "Recibir dano acumula", "stacks de armadura", "(max 5, -8% dmg c/u)"},
        {"Resonancia Caotica:", "Las balas rebotan al", "enemigo mas cercano", "(radio 200u, 1 rebote)"},
        {"Momentum de Combate:", "Kills acumulan Combo", "(max 10, +3% cadencia)", "Sin kills: decay -1/s"}
    };

    private final KaosuarinaGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final BitmapFont titleFont;
    private final BitmapFont cardFont;

    private int selectedIndex = 0;

    public CharacterSelectScreen(KaosuarinaGame game) {
        this.game = game;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);

        cardFont = new BitmapFont();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0.05f, 0.02f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        renderCards();
        renderTitle();
        renderHint();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
            selectedIndex = Math.max(0, selectedIndex - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
            selectedIndex = Math.min(ROLES.length - 1, selectedIndex + 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedIndex = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) selectedIndex = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) selectedIndex = 2;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            confirmar();
    }

    private void confirmar() {
        game.setScreen(new GameScreen(game, ROLES[selectedIndex]));
    }

    private void renderCards() {
        float cardW = 310f;
        float cardH = 430f;
        float spacing = 45f;
        float totalW = cardW * ROLES.length + spacing * (ROLES.length - 1);
        float startX = (Constants.SCREEN_WIDTH - totalW) / 2f;
        float cardY = (Constants.SCREEN_HEIGHT - cardH) / 2f - 30f;

        for (int i = 0; i < ROLES.length; i++) {
            float cardX = startX + i * (cardW + spacing);
            Role role = ROLES[i];
            boolean sel = i == selectedIndex;
            Color accent = accentColor(i);

            // Fondo
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(sel ? 0.18f : 0.1f, sel ? 0.12f : 0.06f, sel ? 0.28f : 0.14f, 1f);
            shapeRenderer.rect(cardX, cardY, cardW, cardH);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            // Borde
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(sel ? 4 : 2);
            shapeRenderer.setColor(accent);
            shapeRenderer.rect(cardX, cardY, cardW, cardH);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);

            // Texto
            batch.begin();

            // Número
            cardFont.getData().setScale(2.5f);
            cardFont.setColor(Color.GOLD);
            cardFont.draw(batch, String.valueOf(i + 1), cardX + 14, cardY + cardH - 10);

            // Nombre
            cardFont.getData().setScale(2.4f);
            cardFont.setColor(accent);
            cardFont.draw(batch, role.nombre, cardX + 14, cardY + cardH - 58);

            // Stats
            cardFont.getData().setScale(1.45f);
            cardFont.setColor(Color.WHITE);
            float sy = cardY + cardH - 120;
            cardFont.draw(batch, "HP:    " + role.stats.maxHealth, cardX + 14, sy);
            cardFont.draw(batch, "VEL:   " + (int) role.stats.baseSpeed, cardX + 14, sy - 32);
            cardFont.draw(batch, "DMG:   " + role.stats.baseDamage + "x", cardX + 14, sy - 64);
            cardFont.draw(batch, "BALAS: " + role.stats.baseBulletCount, cardX + 14, sy - 96);

            // Core
            cardFont.getData().setScale(1.35f);
            cardFont.setColor(Color.CYAN);
            cardFont.draw(batch, "RELIQUIA:", cardX + 14, sy - 145);

            cardFont.getData().setScale(1.2f);
            cardFont.setColor(Color.LIGHT_GRAY);
            String[] lines = CORE_LINES[i];
            for (int l = 0; l < lines.length; l++) {
                cardFont.draw(batch, lines[l], cardX + 14, sy - 172 - l * 26);
            }

            batch.end();
        }
    }

    private void renderTitle() {
        batch.begin();
        titleFont.setColor(Color.GOLD);
        titleFont.draw(batch, "ELIGE TU PERSONAJE",
            (Constants.SCREEN_WIDTH - 490) / 2f, Constants.SCREEN_HEIGHT - 38);
        batch.end();
    }

    private void renderHint() {
        batch.begin();
        cardFont.getData().setScale(1.35f);
        cardFont.setColor(Color.GRAY);
        cardFont.draw(batch,
            "A/D o Flechas para navegar  |  1/2/3 elegir directo  |  ENTER/SPACE confirmar",
            55, 34);
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
        titleFont.dispose();
        cardFont.dispose();
    }
}
