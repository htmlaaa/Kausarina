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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.ui.FontManager;
import com.milwar.kaosuarina.utils.Constants;

public class MainMenuScreen implements Screen {

    private final KaosuarinaGame  game;
    private final SpriteBatch     batch;
    private final ShapeRenderer   shapeRenderer;
    private final OrthographicCamera camera;
    private final FitViewport     viewport;

    public MainMenuScreen(KaosuarinaGame game) {
        this.game     = game;
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera        = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        viewport      = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            dispose();
            game.setScreen(new CharacterSelectScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            dispose();
            game.setScreen(new CreditsScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            dispose();
            game.setScreen(new LeaderboardScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        ScreenUtils.clear(0.04f, 0.02f, 0.08f, 1f);
        viewport.apply(true);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Subtle background accent lines
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.35f, 0.1f, 0.6f, 0.18f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, 4f);
        shapeRenderer.rect(0, Constants.SCREEN_HEIGHT - 4f, Constants.SCREEN_WIDTH, 4f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        float cx = Constants.SCREEN_WIDTH / 2f;
        float cy = Constants.SCREEN_HEIGHT / 2f;

        BitmapFont fTitle   = FontManager.get().title;
        BitmapFont fHeading = FontManager.get().heading;
        BitmapFont fLarge   = FontManager.get().large;

        batch.begin();

        fTitle.setColor(Color.GOLD);
        fTitle.draw(batch, "KAOSUARINA", cx - 330, cy + 210);

        fHeading.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        fHeading.draw(batch, "Un roguelite de supervivencia", cx - 250, cy + 140);

        fLarge.setColor(Color.WHITE);
        fLarge.draw(batch, "[ENTER]   Jugar",     cx - 140, cy + 20);
        fLarge.draw(batch, "[L]         Records",  cx - 140, cy - 26);
        fLarge.draw(batch, "[C]         Creditos", cx - 140, cy - 72);
        fLarge.draw(batch, "[ESC]      Salir",     cx - 140, cy - 118);

        fLarge.setColor(Color.WHITE);
        fTitle.setColor(Color.WHITE);
        fHeading.setColor(Color.WHITE);
        batch.end();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }
}
