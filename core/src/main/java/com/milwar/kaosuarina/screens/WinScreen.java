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

    private final KaosuarinaGame game;
    private final SpriteBatch    batch;
    private final OrthographicCamera camera;
    private final FitViewport    viewport;

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
            dispose();
            game.setScreen(new CharacterSelectScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            dispose();
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        ScreenUtils.clear(0.02f, 0f, 0.08f, 1f);
        viewport.apply(true);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH  / 2f;
        float cy = Constants.SCREEN_HEIGHT / 2f;

        BitmapFont fT = FontManager.get().title;
        BitmapFont fL = FontManager.get().large;
        BitmapFont fM = FontManager.get().medium;
        BitmapFont fS = FontManager.get().small;

        fT.setColor(1f, 0.85f, 0.2f, 1f);
        fT.draw(batch, "¡VICTORIA!", cx - 215, cy + 260);

        fM.setColor(new Color(0.85f, 0.85f, 1f, 1f));
        fM.draw(batch, rolNombre + "  (" + reliquiaNombre + ")", cx - 160, cy + 170);

        fL.setColor(Color.WHITE);
        fL.draw(batch, "Score: " + score,                                           cx - 160, cy + 130);
        fL.draw(batch, String.format("Tiempo: %02d:%02d",
            tiempoSegundos / 60, tiempoSegundos % 60),                             cx - 160, cy + 92);
        fL.draw(batch, "Nivel: " + level + "    Oleada: " + waveCount,             cx - 160, cy + 54);

        fM.setColor(new Color(0.5f, 0.9f, 1f, 1f));
        fM.draw(batch, armasText, cx - 160, cy + 20);

        renderLeaderboard(cx, cy - 18, fS);

        fL.setColor(new Color(0.4f, 0.95f, 0.4f, 1f));
        fL.draw(batch, "[R]    Jugar de nuevo",  cx - 160, cy - 230);
        fL.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        fL.draw(batch, "[ESC]  Menu principal",  cx - 160, cy - 270);

        batch.end();
    }

    private void renderLeaderboard(float cx, float topY, BitmapFont f) {
        if (leaderboard == null || leaderboard.length == 0) return;
        f.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        f.draw(batch, "TOP SCORES", cx - 70, topY);
        f.setColor(new Color(0.82f, 0.82f, 0.82f, 1f));
        for (int i = 0; i < leaderboard.length; i++) {
            f.draw(batch, leaderboard[i], cx - 170, topY - 22 - i * 22);
        }
        f.setColor(Color.WHITE);
    }

    private static String rolNombreDe(int id) {
        switch (id) {
            case 1: return "Caballero";
            case 2: return "Mago";
            case 3: return "Tirador";
            default: return "???";
        }
    }

    private static String reliquiaNombreDe(int id) {
        switch (id) {
            case 1: return "Fortaleza Reactiva";
            case 2: return "Resonancia Caotica";
            case 3: return "Momentum de Combate";
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
