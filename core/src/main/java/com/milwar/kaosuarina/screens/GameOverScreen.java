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

public class GameOverScreen implements Screen {

    private static final String[] TIPO_NOMBRES = {
        "Basico", "Rapido", "Tanque", "Shooter",
        "Maldito", "Espectral", "Guardian", "Arquero", "Devastador"
    };

    private final KaosuarinaGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final int score;
    private final int tiempoSegundos;
    private final int level;
    private final int waveCount;
    private final String rolNombre;
    private final String armasText;
    private final int[] killsPorTipo;
    private final String[] leaderboard;
    private final int tokensEarned;
    private final int tokensTotal;

    public GameOverScreen(KaosuarinaGame game, int score, int tiempoSegundos, int level,
                          int waveCount, int personajeId, String armasText,
                          int[] killsPorTipo, String[] leaderboard) {
        this(game, score, tiempoSegundos, level, waveCount, personajeId,
            armasText, killsPorTipo, leaderboard, 0, 0);
    }

    public GameOverScreen(KaosuarinaGame game, int score, int tiempoSegundos, int level,
                          int waveCount, int personajeId, String armasText,
                          int[] killsPorTipo, String[] leaderboard,
                          int tokensEarned, int tokensTotal) {
        this.game = game;
        this.score = score;
        this.tiempoSegundos = tiempoSegundos;
        this.level = level;
        this.waveCount = waveCount;
        this.rolNombre = rolNombreDe(personajeId);
        this.armasText = armasText;
        this.killsPorTipo = killsPorTipo;
        this.leaderboard = leaderboard;
        this.tokensEarned = tokensEarned;
        this.tokensTotal = tokensTotal;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
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
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            dispose();
            game.setScreen(new CharacterSelectScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            dispose();
            game.setScreen(new LeaderboardScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            dispose();
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        ScreenUtils.clear(0.04f, 0.02f, 0.06f, 1f);
        viewport.apply(true);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH / 2f;
        float cy = Constants.SCREEN_HEIGHT / 2f;

        BitmapFont fTitle = FontManager.get().title;
        BitmapFont fHeading = FontManager.get().heading;
        BitmapFont fLarge = FontManager.get().large;
        BitmapFont fMedium = FontManager.get().medium;
        BitmapFont fSmall = FontManager.get().small;

        fTitle.setColor(0.9f, 0.12f, 0.12f, 1f);
        fTitle.draw(batch, "GAME OVER", cx - 310, cy + 290);

        fHeading.setColor(new Color(0.75f, 0.8f, 1f, 1f));
        fHeading.draw(batch, rolNombre, cx - 200, cy + 215);

        fLarge.setColor(Color.WHITE);
        fLarge.draw(batch, "Score:    " + score, cx - 200, cy + 165);
        fLarge.draw(batch, String.format("Tiempo:   %02d:%02d", tiempoSegundos / 60, tiempoSegundos % 60), cx - 200, cy + 127);
        fLarge.draw(batch, "Nivel:    " + level + "     Oleada: " + waveCount, cx - 200, cy + 89);

        fMedium.setColor(new Color(0.85f, 0.62f, 0.32f, 1f));
        fMedium.draw(batch, buildKillsTotal(), cx - 200, cy + 56);
        fMedium.draw(batch, buildKillsDetalle(), cx - 200, cy + 32);

        fMedium.setColor(new Color(0.5f, 0.9f, 1f, 1f));
        fMedium.draw(batch, armasText, cx - 200, cy + 4);

        // Meta-progresión tokens (MEC-01)
        if (tokensEarned > 0) {
            fMedium.setColor(new Color(1f, 0.82f, 0.2f, 1f));
            fMedium.draw(batch,
                String.format("Tokens ganados: +%d   (Total: %d)", tokensEarned, tokensTotal),
                cx - 200, cy - 18);
        }

        renderLeaderboardCompact(cx, cy - 62);

        fLarge.setColor(new Color(0.4f, 0.95f, 0.4f, 1f));
        fLarge.draw(batch, "[R]      Jugar de nuevo", cx - 200, cy - 185);
        fLarge.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        fLarge.draw(batch, "[L]      Ver Records completos", cx - 200, cy - 222);
        fLarge.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        fLarge.draw(batch, "[ESC]  Menu principal", cx - 200, cy - 258);

        fLarge.setColor(Color.WHITE);
        fTitle.setColor(Color.WHITE);
        fHeading.setColor(Color.WHITE);
        batch.end();
    }

    private String buildKillsTotal() {
        if (killsPorTipo == null || killsPorTipo.length == 0) return "";
        int total = 0;
        for (int k : killsPorTipo) total += k;
        return "Eliminados: " + total + " enemigos";
    }

    private String buildKillsDetalle() {
        if (killsPorTipo == null || killsPorTipo.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < Math.min(killsPorTipo.length, TIPO_NOMBRES.length); i++) {
            if (killsPorTipo[i] > 0) {
                if (!first) sb.append("  ");
                sb.append(TIPO_NOMBRES[i]).append(":").append(killsPorTipo[i]);
                first = false;
            }
        }
        return sb.toString();
    }

    private void renderLeaderboardCompact(float cx, float topY) {
        if (leaderboard == null || leaderboard.length == 0) return;
        BitmapFont fSmall = FontManager.get().small;
        fSmall.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        fSmall.draw(batch, "TOP 3", cx - 45, topY);
        fSmall.setColor(new Color(0.75f, 0.75f, 0.75f, 1f));
        int n = Math.min(leaderboard.length, 3);
        for (int i = 0; i < n; i++) {
            fSmall.draw(batch, leaderboard[i], cx - 200, topY - 18 - i * 18);
        }
        fSmall.setColor(Color.WHITE);
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
