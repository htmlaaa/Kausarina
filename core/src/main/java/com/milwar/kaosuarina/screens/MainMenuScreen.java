package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.utils.Constants;

public class MainMenuScreen implements Screen {

    private final KaosuarinaGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final BitmapFont titleFont;
    private final BitmapFont subtitleFont;
    private final BitmapFont menuFont;

    public MainMenuScreen(KaosuarinaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5.0f);
        titleFont.setColor(Color.WHITE);
        titleFont.setUseIntegerPositions(false);
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        subtitleFont = new BitmapFont();
        subtitleFont.getData().setScale(2.0f);
        subtitleFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        subtitleFont.setUseIntegerPositions(false);
        subtitleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        menuFont = new BitmapFont();
        menuFont.getData().setScale(2.5f);
        menuFont.setColor(Color.WHITE);
        menuFont.setUseIntegerPositions(false);
        menuFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH / 2f;
        float cy = Constants.SCREEN_HEIGHT / 2f;

        titleFont.draw(batch, "KAOSUARINA", cx - 340, cy + 180);
        menuFont.draw(batch, "[ENTER]  Jugar",     cx - 130, cy);
        menuFont.draw(batch, "[L]      Records",   cx - 130, cy - 60);
        menuFont.draw(batch, "[C]      Creditos",  cx - 130, cy - 120);
        menuFont.draw(batch, "[ESC]    Salir",     cx - 130, cy - 180);

        batch.end();
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        subtitleFont.dispose();
        menuFont.dispose();
    }
}
