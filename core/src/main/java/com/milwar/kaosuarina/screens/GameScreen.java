package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.milwar.kaosuarina.entities.*;
import com.milwar.kaosuarina.Systems.Upgrade;
import com.milwar.kaosuarina.Systems.UpgradeManager;
import com.milwar.kaosuarina.ui.HUD;
import com.milwar.kaosuarina.utils.ColisionManager;
import com.milwar.kaosuarina.utils.Constants;

public class GameScreen implements Screen {

    // ── Estado de juego ────────────────────────────────────────────────────
    private SpriteBatch batch;
    private OrthographicCamera camera;

    private Player player;
    private PoolBalas poolBalas;
    private PoolEnemigos poolEnemigos;
    private PoolBalasEnemigas poolBalasEnemigas;

    private HUD hud;
    private UpgradeManager upgradeManager;
    private LevelUpScreen levelUpScreen;

    private int nivelAnterior;
    private float timerSpawn;
    private float intervaloSpawnBase;
    private float timerDificultad;
    private float aimAngle;

    @Override
    public void show() {
        inicializar();
    }

    private void inicializar() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        player = new Player(0, 0);
        poolBalas = new PoolBalas();
        poolEnemigos = new PoolEnemigos();
        poolBalasEnemigas = new PoolBalasEnemigas();
        hud = new HUD(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        upgradeManager = new UpgradeManager();
        player.setUpgradeManager(upgradeManager);

        levelUpScreen = new LevelUpScreen(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        nivelAnterior = 1;
        timerSpawn = 0;
        intervaloSpawnBase = 2f;
        timerDificultad = 0;
    }

    @Override
    public void render(float delta) {
        // ── Pantalla de level up (pausa el juego) ─────────────────────────
        if (levelUpScreen.isActive()) {
            procesarLevelUp();
            ScreenUtils.clear(0, 0, 0, 1f);
            levelUpScreen.render(batch);
            return;
        }

        // ── Game over ─────────────────────────────────────────────────────
        if (!player.isAlive()) {
            renderGameOver();
            return;
        }

        procesarInput();
        actualizarJuego(delta);
        actualizarDificultad(delta);
        procesarColisiones();
        detectarLevelUp();
        renderizar();
    }

    private void procesarLevelUp() {
        levelUpScreen.handleInput();
        Upgrade seleccionado = levelUpScreen.getSelectedUpgrade();
        if (seleccionado != null) {
            int hpBonus = upgradeManager.aplicarUpgrade(seleccionado);
            if (hpBonus > 0) player.aumentarVidaMaxima(hpBonus);
            levelUpScreen.hide();
        }
    }

    private void procesarInput() {
        player.velocity.set(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.velocity.y = player.getVelocidadActual();
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.velocity.y = -player.getVelocidadActual();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.velocity.x = -player.getVelocidadActual();
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.velocity.x = player.getVelocidadActual();

        if (player.velocity.len2() > 0) {
            player.velocity.nor().scl(player.getVelocidadActual());
        }

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        aimAngle = (float) Math.atan2(
            mouseY - Gdx.graphics.getHeight() / 2f,
            mouseX - Gdx.graphics.getWidth()  / 2f
        );
    }

    private void actualizarJuego(float delta) {
        player.update(delta, poolBalas, aimAngle);
        poolBalas.update(delta);
        poolBalasEnemigas.update(delta);
        poolEnemigos.update(delta, player.position, poolBalasEnemigas);
        hud.update(delta);

        // Spawn de enemigos
        timerSpawn -= delta;
        if (timerSpawn <= 0) {
            spawnOleada();
            timerSpawn = intervaloSpawnBase;
        }

        hud.setHealth(player.getCurrentHealth(), player.getMaxHealth());
    }

    private void actualizarDificultad(float delta) {
        timerDificultad += delta;
        if (timerDificultad >= 30f) {
            timerDificultad = 0;
            intervaloSpawnBase = Math.max(0.3f, intervaloSpawnBase * 0.85f);
        }
    }

    private void procesarColisiones() {
        int muertes = ColisionManager.comprobarBalasVsEnemigos(poolBalas, poolEnemigos);
        if (muertes > 0) {
            hud.addScore(muertes * 10);
            hud.addExperience(muertes * 25f);
        }

        if (ColisionManager.comprobarJugadorVsEnemigos(player.position, player.getRadio(), poolEnemigos)) {
            player.recibirDanio(10);
        }

        ColisionManager.comprobarBalasEnemigas(poolBalasEnemigas, player);
    }

    private void detectarLevelUp() {
        int nivelActual = hud.getLevel();
        if (nivelActual > nivelAnterior) {
            nivelAnterior = nivelActual;
            levelUpScreen.show(upgradeManager.getUpgradesAleatorios(3));
        }
    }

    private void spawnOleada() {
        int cantidad = 1 + (hud.getLevel() / 3);
        float distSpawn = 800f;

        for (int i = 0; i < cantidad; i++) {
            float angulo = MathUtils.random(MathUtils.PI2);
            float x = player.position.x + MathUtils.cos(angulo) * distSpawn;
            float y = player.position.y + MathUtils.sin(angulo) * distSpawn;
            poolEnemigos.spawn(x, y);
        }
    }

    private void renderizar() {
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();

        ScreenUtils.clear(0.1f, 0.05f, 0.15f, 1f);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        poolBalas.render(batch);
        poolBalasEnemigas.render(batch);
        poolEnemigos.render(batch);
        player.render(batch);
        batch.end();

        hud.render(batch);
    }

    private void renderGameOver() {
        ScreenUtils.clear(0, 0, 0, 1f);
        hud.render(batch);
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reiniciar();
        }
    }

    private void reiniciar() {
        // Liberar recursos del juego anterior
        player.dispose();
        poolBalas.dispose();
        poolEnemigos.dispose();
        poolBalasEnemigas.dispose();
        hud.dispose();
        levelUpScreen.dispose();

        // Reinicializar todo desde cero
        inicializar();
    }

    @Override
    public void resize(int width, int height) {
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
        player.dispose();
        poolBalas.dispose();
        poolEnemigos.dispose();
        poolBalasEnemigas.dispose();
        hud.dispose();
        levelUpScreen.dispose();
    }
}
