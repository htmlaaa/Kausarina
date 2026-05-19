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

public class GameOverScreen implements Screen {

    private static final String[] TIPO_NOMBRES = {
        "Basico", "Rapido", "Tanque", "Shooter",
        "Maldito", "Espectral", "Guardian", "Arquero", "Devastador"
    };

    private final KaosuarinaGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final BitmapFont titleFont;
    private final BitmapFont font;

    private final int score;
    private final int tiempoSegundos;
    private final int level;
    private final int waveCount;
    private final String rolNombre;
    private final String armasText;
    private final int[] killsPorTipo;
    private final String[] leaderboard;

    public GameOverScreen(KaosuarinaGame game, int score, int tiempoSegundos, int level,
                          int waveCount, int personajeId, String armasText,
                          int[] killsPorTipo, String[] leaderboard) {
        this.game = game;
        this.score = score;
        this.tiempoSegundos = tiempoSegundos;
        this.level = level;
        this.waveCount = waveCount;
        this.rolNombre = rolNombreDe(personajeId);
        this.armasText = armasText;
        this.killsPorTipo = killsPorTipo;
        this.leaderboard = leaderboard;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5f);
        titleFont.setUseIntegerPositions(false);
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setUseIntegerPositions(false);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float cx = Constants.SCREEN_WIDTH / 2f;
        float cy = Constants.SCREEN_HEIGHT / 2f;

        titleFont.setColor(0.9f, 0.1f, 0.1f, 1f);
        titleFont.draw(batch, "GAME OVER", cx - 235, cy + 270);

        font.getData().setScale(2f);
        font.setColor(0.85f, 0.85f, 1f, 1f);
        font.draw(batch, "Rol: " + rolNombre, cx - 160, cy + 175);

        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + score, cx - 160, cy + 133);
        font.draw(batch, String.format("Tiempo: %02d:%02d",
            tiempoSegundos / 60, tiempoSegundos % 60), cx - 160, cy + 91);
        font.draw(batch, "Nivel: " + level + "    Oleada: " + waveCount, cx - 160, cy + 49);

        font.getData().setScale(1.4f);
        font.setColor(0.85f, 0.6f, 0.3f, 1f);
        font.draw(batch, buildKillsTotal(), cx - 160, cy + 14);
        font.draw(batch, buildKillsDetalle(), cx - 160, cy - 10);

        font.getData().setScale(1.5f);
        font.setColor(0.5f, 0.9f, 1f, 1f);
        font.draw(batch, armasText, cx - 160, cy - 40);

        renderLeaderboardCompact(cx, cy - 75);

        font.getData().setScale(1.8f);
        font.setColor(0.4f, 0.95f, 0.4f, 1f);
        font.draw(batch, "[R]    Jugar de nuevo", cx - 160, cy - 215);
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "[L]    Ver Records completos", cx - 160, cy - 250);
        font.setColor(0.65f, 0.65f, 0.65f, 1f);
        font.draw(batch, "[ESC]  Menu principal", cx - 160, cy - 285);

        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
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
        font.getData().setScale(1.2f);
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "TOP 3", cx - 40, topY);
        font.setColor(0.75f, 0.75f, 0.75f, 1f);
        int n = Math.min(leaderboard.length, 3);
        for (int i = 0; i < n; i++) {
            font.draw(batch, leaderboard[i], cx - 170, topY - 22 - i * 22);
        }
    }

    @Override
    public void resize(int w, int h) {
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
        titleFont.dispose();
        font.dispose();
    }
}
