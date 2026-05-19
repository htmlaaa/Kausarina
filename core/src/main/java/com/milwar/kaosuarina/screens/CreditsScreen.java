package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.utils.Constants;

public class CreditsScreen implements Screen {

    private final KaosuarinaGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;

    public CreditsScreen(KaosuarinaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4.5f);
        bodyFont = new BitmapFont();
        bodyFont.getData().setScale(2f);
    }

    @Override
    public void show() {}

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

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH / 2f;
        float top = Constants.SCREEN_HEIGHT;

        titleFont.setColor(Color.GOLD);
        titleFont.draw(batch, "KAOSUARINA", cx - 225, top - 55);

        bodyFont.getData().setScale(1.7f);
        bodyFont.setColor(0.7f, 0.7f, 0.9f, 1f);
        bodyFont.draw(batch, "Roguelite 2D Top-Down Shooter", cx - 260, top - 115);

        float y = top - 180;
        section("DESARROLLO", cx, y);
        bodyFont.getData().setScale(1.6f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Autor:       Milwar Chapi", cx - 250, y - 42);
        bodyFont.draw(batch, "Proyecto:    TFG  —  GS Desarrollo de Aplicaciones Multiplataforma", cx - 250, y - 80);
        bodyFont.draw(batch, "Curso:       2025 / 2026", cx - 250, y - 118);

        y -= 175;
        section("TECNOLOGIAS", cx, y);
        bodyFont.getData().setScale(1.6f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Motor:       LibGDX 1.14.0  /  Java 8  /  LWJGL3", cx - 250, y - 42);
        bodyFont.draw(batch, "Graficos:    Procedural (Pixmap)  +  PixelLab MCP", cx - 250, y - 80);
        bodyFont.draw(batch, "Base datos:  MySQL  (patron DAO  +  JDBC)", cx - 250, y - 118);
        bodyFont.draw(batch, "Build:       Gradle  /  gdx-liftoff", cx - 250, y - 156);

        y -= 210;
        section("AGRADECIMIENTOS", cx, y);
        bodyFont.getData().setScale(1.6f);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Kenney.nl  —  assets de audio CC0", cx - 250, y - 42);
        bodyFont.draw(batch, "LibGDX community  —  documentacion y ejemplos", cx - 250, y - 80);

        bodyFont.getData().setScale(1.4f);
        bodyFont.setColor(Color.GRAY);
        bodyFont.draw(batch, "ESC / ENTER / SPACE  para volver", cx - 200, 34);

        batch.end();
    }

    private void section(String title, float cx, float y) {
        bodyFont.getData().setScale(1.9f);
        bodyFont.setColor(Color.CYAN);
        bodyFont.draw(batch, title, cx - title.length() * 6, y);
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();
    }
}
