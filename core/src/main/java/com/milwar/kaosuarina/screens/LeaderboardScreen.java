package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.db.DBManager;
import com.milwar.kaosuarina.db.vo.RunVO;
import com.milwar.kaosuarina.ui.FontManager;
import com.milwar.kaosuarina.utils.Constants;

import java.util.List;

public class LeaderboardScreen implements Screen {

    private final KaosuarinaGame game;
    private final SpriteBatch batch;
    private final FitViewport viewport;
    private String[] rows;

    public LeaderboardScreen(KaosuarinaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
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
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            dispose();
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        ScreenUtils.clear(0.03f, 0.01f, 0.08f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH / 2f;
        float top = Constants.SCREEN_HEIGHT - 50f;

        FontManager.get().title.setColor(1f, 0.85f, 0.2f, 1f);
        FontManager.get().title.draw(batch, "RECORDS", cx - 130f, top);

        FontManager.get().medium.setColor(0.5f, 0.65f, 0.85f, 1f);
        FontManager.get().medium.draw(batch, "#     Rol           Score      Nivel   Tiempo", cx - 240f, top - 80f);

        if (rows != null) {
            for (int i = 0; i < rows.length; i++) {
                if (i == 0) FontManager.get().large.setColor(1f, 0.85f, 0.2f, 1f);
                else if (i == 1) FontManager.get().large.setColor(0.8f, 0.8f, 0.8f, 1f);
                else if (i == 2) FontManager.get().large.setColor(0.8f, 0.55f, 0.2f, 1f);
                else FontManager.get().large.setColor(Color.WHITE);
                FontManager.get().large.draw(batch, rows[i], cx - 240f, top - 120f - i * 40f);
            }
        }

        FontManager.get().small.setColor(0.55f, 0.55f, 0.55f, 1f);
        FontManager.get().small.draw(batch, "[ESC] / [ENTER]  Volver al menu", cx - 130f, 32f);

        batch.end();
    }

    private static String rolNombreDe(int id) {
        switch (id) {
            case 1:
                return "Caballero";
            case 2:
                return "Mago";
            case 3:
                return "Tirador";
            default:
                return "???";
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
    }
}
