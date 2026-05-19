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
import com.milwar.kaosuarina.db.DBManager;
import com.milwar.kaosuarina.db.vo.RunVO;
import com.milwar.kaosuarina.utils.Constants;
import java.util.List;

public class LeaderboardScreen implements Screen {

    private final KaosuarinaGame game;
    private final SpriteBatch    batch;
    private final OrthographicCamera camera;
    private final BitmapFont     titleFont;
    private final BitmapFont     font;
    private String[]             rows;

    public LeaderboardScreen(KaosuarinaGame game) {
        this.game = game;
        batch  = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setUseIntegerPositions(false);
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        font = new BitmapFont();
        font.getData().setScale(1.8f);
        font.setUseIntegerPositions(false);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void show() {
        List<RunVO> runs = DBManager.getTop10();
        if (runs == null || runs.isEmpty()) {
            rows = new String[]{"Sin records todavia"};
            return;
        }
        int n = Math.min(runs.size(), 10);
        rows = new String[n];
        for (int i = 0; i < n; i++) {
            RunVO v = runs.get(i);
            rows[i] = String.format("#%2d  %-10s  %6d pts  Lv%-2d  %02d:%02ds",
                i + 1, rolNombreDe(v.personajeId), v.score, v.nivelAlcanzado,
                v.tiempoSegundos / 60, v.tiempoSegundos % 60);
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER)  ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            dispose();
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        ScreenUtils.clear(0.03f, 0.01f, 0.08f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float cx  = Constants.SCREEN_WIDTH  / 2f;
        float top = Constants.SCREEN_HEIGHT - 50;

        titleFont.setColor(1f, 0.85f, 0.2f, 1f);
        titleFont.draw(batch, "RECORDS", cx - 130, top);

        font.getData().setScale(1.4f);
        font.setColor(0.5f, 0.65f, 0.85f, 1f);
        font.draw(batch, "#     Rol           Score      Nivel   Tiempo", cx - 230, top - 65);

        font.getData().setScale(1.8f);
        if (rows != null) {
            for (int i = 0; i < rows.length; i++) {
                if      (i == 0) font.setColor(1f, 0.85f, 0.2f,  1f);
                else if (i == 1) font.setColor(0.8f, 0.8f, 0.8f, 1f);
                else if (i == 2) font.setColor(0.8f, 0.55f, 0.2f, 1f);
                else             font.setColor(Color.WHITE);
                font.draw(batch, rows[i], cx - 230, top - 100 - i * 44);
            }
        }

        font.getData().setScale(1.5f);
        font.setColor(0.55f, 0.55f, 0.55f, 1f);
        font.draw(batch, "[ESC] / [ENTER]  Volver al menu", cx - 165, 38);

        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
        batch.end();
    }

    private static String rolNombreDe(int id) {
        switch (id) {
            case 1: return "Caballero";
            case 2: return "Mago";
            case 3: return "Tirador";
            default: return "???";
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        font.dispose();
    }
}
