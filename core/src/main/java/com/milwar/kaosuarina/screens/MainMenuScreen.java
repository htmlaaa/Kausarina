package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.ui.FontManager;
import com.milwar.kaosuarina.utils.Constants;

public class MainMenuScreen implements Screen {

    private final KaosuarinaGame game;
    private final SpriteBatch    batch;
    private final OrthographicCamera camera;
    private final FitViewport    viewport;

    public MainMenuScreen(KaosuarinaGame game) {
        this.game = game;
        batch    = new SpriteBatch();
        camera   = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
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

        ScreenUtils.clear(0, 0, 0, 1f);
        viewport.apply(true);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH / 2f;
        float cy = Constants.SCREEN_HEIGHT / 2f;

        FontManager.get().title.setColor(Color.WHITE);
        FontManager.get().title.draw(batch, "KAOSUARINA", cx - 310, cy + 200);

        FontManager.get().large.setColor(Color.WHITE);
        FontManager.get().large.draw(batch, "[ENTER]  Jugar",    cx - 130, cy + 20);
        FontManager.get().large.draw(batch, "[L]      Records",  cx - 130, cy - 40);
        FontManager.get().large.draw(batch, "[C]      Creditos", cx - 130, cy - 100);
        FontManager.get().large.draw(batch, "[ESC]    Salir",    cx - 130, cy - 160);

        batch.end();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
    }
}
