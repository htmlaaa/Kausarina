package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.ui.FontManager;
import com.milwar.kaosuarina.utils.Constants;

public class CreditsScreen implements Screen {

    private final KaosuarinaGame game;
    private final SpriteBatch batch;
    private final FitViewport viewport;

    public CreditsScreen(KaosuarinaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            dispose();
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.03f, 0.01f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH / 2f;
        float top = Constants.SCREEN_HEIGHT - 40f;

        // Título
        FontManager.get().title.setColor(Color.GOLD);
        FontManager.get().title.draw(batch, "KAOSUARINA", cx - 220f, top);

        FontManager.get().medium.setColor(0.7f, 0.7f, 0.9f, 1f);
        FontManager.get().medium.draw(batch, "Roguelite 2D Top-Down Shooter", cx - 210f, top - 80f);

        float y = top - 150f;
        section(cx, y, "DESARROLLO");
        float ly = y - 40f;
        line(cx, ly, "Autor:      Milwar Chapi");
        line(cx, ly - 32f, "Proyecto:   TFG — GS Desarrollo de Aplicaciones Multiplataforma");
        line(cx, ly - 64f, "Curso:      2025 / 2026");

        y -= 180f;
        section(cx, y, "TECNOLOGIAS");
        ly = y - 40f;
        line(cx, ly, "Motor:      LibGDX 1.14.0  /  Java 8  /  LWJGL3");
        line(cx, ly - 32f, "Graficos:   Procedural (Pixmap)  +  PixelLab MCP");
        line(cx, ly - 64f, "Base datos: MySQL  (patron DAO + JDBC)");
        line(cx, ly - 96f, "Build:      Gradle  /  gdx-liftoff");

        y -= 200f;
        section(cx, y, "AGRADECIMIENTOS");
        ly = y - 40f;
        line(cx, ly, "Kenney.nl  —  assets de audio CC0");
        line(cx, ly - 32f, "LibGDX community  —  documentacion y ejemplos");

        FontManager.get().small.setColor(Color.GRAY);
        FontManager.get().small.draw(batch, "ESC / ENTER / SPACE  para volver al menu", cx - 160f, 30f);

        batch.end();
    }

    private void section(float cx, float y, String title) {
        FontManager.get().heading.setColor(Color.CYAN);
        FontManager.get().heading.draw(batch, title, cx - title.length() * 10f, y);
    }

    private void line(float cx, float y, String text) {
        FontManager.get().large.setColor(Color.WHITE);
        FontManager.get().large.draw(batch, text, cx - 310f, y);
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
    }
}
