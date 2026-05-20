package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.ui.FontManager;
import com.milwar.kaosuarina.utils.Constants;

public class WinScreen implements Screen {

    private final KaosuarinaGame  game;
    private final SpriteBatch     batch;
    private final OrthographicCamera camera;
    private final FitViewport     viewport;

    private final int      score;
    private final int      tiempoSegundos;
    private final int      level;
    private final int      waveCount;
    private final String   rolNombre;
    private final String   reliquiaNombre;
    private final String   armasText;
    private final String[] leaderboard;

    public WinScreen(KaosuarinaGame game, int score, int tiempoSegundos, int level,
                     int waveCount, int personajeId, String armasText, String[] leaderboard) {
        this.game           = game;
        this.score          = score;
        this.tiempoSegundos = tiempoSegundos;
        this.level          = level;
        this.waveCount      = waveCount;
        this.rolNombre      = rolNombreDe(personajeId);
        this.reliquiaNombre = reliquiaNombreDe(personajeId);
        this.armasText      = armasText;
        this.leaderboard    = leaderboard;

        batch    = new SpriteBatch();
        camera   = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            dispose(); game.setScreen(new CharacterSelectScreen(game)); return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            dispose(); game.setScreen(new MainMenuScreen(game)); return;
        }

        ScreenUtils.clear(0.02f, 0f, 0.08f, 1f);
        viewport.apply(true);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH  / 2f;
        float cy = Constants.SCREEN_HEIGHT / 2f;

        BitmapFont fTitle   = FontManager.get().title;
        BitmapFont fHeading = FontManager.get().heading;
        BitmapFont fLarge   = FontManager.get().large;
        BitmapFont fMedium  = FontManager.get().medium;
        BitmapFont fSmall   = FontManager.get().small;

        fTitle.setColor(1f, 0.85f, 0.2f, 1f);
        fTitle.draw(batch, "¡VICTORIA!", cx - 270, cy + 290);

        fHeading.setColor(new Color(0.75f, 0.8f, 1f, 1f));
        fHeading.draw(batch, rolNombre + "  —  " + reliquiaNombre, cx - 270, cy + 218);

        fLarge.setColor(Color.WHITE);
        fLarge.draw(batch, "Score:    " + score,                                              cx - 200, cy + 168);
        fLarge.draw(batch, String.format("Tiempo:   %02d:%02d", tiempoSegundos / 60, tiempoSegundos % 60), cx - 200, cy + 130);
        fLarge.draw(batch, "Nivel:    " + level + "     Oleada: " + waveCount,                cx - 200, cy + 92);

        fMedium.setColor(new Color(0.5f, 0.9f, 1f, 1f));
        fMedium.draw(batch, armasText, cx - 200, cy + 56);

        renderLeaderboard(cx, cy - 8);

        fLarge.setColor(new Color(0.4f, 0.95f, 0.4f, 1f));
        fLarge.draw(batch, "[R]      Jugar de nuevo",   cx - 200, cy - 220);
        fLarge.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        fLarge.draw(batch, "[ESC]  Menu principal",     cx - 200, cy - 258);

        fLarge.setColor(Color.WHITE);
        fTitle.setColor(Color.WHITE);
        fHeading.setColor(Color.WHITE);
        batch.end();
    }

    private void renderLeaderboard(float cx, float topY) {
        if (leaderboard == null || leaderboard.length == 0) return;
        BitmapFont fSmall = FontManager.get().small;
        fSmall.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        fSmall.draw(batch, "TOP SCORES", cx - 65, topY);
        fSmall.setColor(new Color(0.82f, 0.82f, 0.82f, 1f));
        for (int i = 0; i < leaderboard.length; i++) {
            fSmall.draw(batch, leaderboard[i], cx - 200, topY - 20 - i * 20);
        }
        fSmall.setColor(Color.WHITE);
    }

    private static String rolNombreDe(int id) {
        switch (id) {
            case 1:  return "Caballero";
            case 2:  return "Mago";
            case 3:  return "Tirador";
            default: return "???";
        }
    }

    private static String reliquiaNombreDe(int id) {
        switch (id) {
            case 1:  return "Fortaleza Reactiva";
            case 2:  return "Resonancia Caotica";
            case 3:  return "Momentum de Combate";
            default: return "-";
        }
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
