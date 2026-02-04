package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.milwar.kaosuarina.entities.pozoBala;
import com.milwar.kaosuarina.entities.pozoEnemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.ui.HUD;
import com.milwar.kaosuarina.utils.ControlColision;

import static com.milwar.kaosuarina.utils.Constants.*;

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Player player;
    private pozoBala pozoBala;
    private pozoEnemy pozoEnemy;
    //private GridRenderer gridRenderer;
    private HUD hud;

    private float enemySpawnTimer;
    private static final float ENEMY_SPAWN_INTERVAL = 1f;

    private int killCount;

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        player = new Player(0, 0);
        pozoBala = new pozoBala();
        pozoEnemy = new pozoEnemy();
       //gridRenderer = new GridRenderer();
        hud = new HUD(SCREEN_WIDTH, SCREEN_HEIGHT); // ← CAMBIO: pasar dimensiones

        enemySpawnTimer = 0;
        killCount = 0;
    }

    @Override
    public void render(float delta) {
        if (!player.isAlive()) {
            renderGameOver();
            return;
        }

        handleInput();

        player.update(delta, pozoBala);
        pozoBala.update(delta);
        pozoEnemy.update(delta, player.position);
        hud.update(delta);

        // Spawn enemigos
        enemySpawnTimer -= delta;
        if (enemySpawnTimer <= 0) {
            spawnEnemy();
            enemySpawnTimer = ENEMY_SPAWN_INTERVAL;
        }

        // Colisiones balas-enemigos
        int enemiesKilledThisFrame = ControlColision.checkBulletEnemyCollisions(pozoBala, pozoEnemy);
        if (enemiesKilledThisFrame > 0) {
            killCount += enemiesKilledThisFrame;
            hud.addScore(enemiesKilledThisFrame * 10);
            System.out.println("Score: " + hud.getScore() + " | Kills: " + killCount); // DEBUG
        }
        hud.setHealth(player.getCurrentHealth(), player.getMaxHealth());
        hud.setMana(player.getCurrentMana(), player.getMaxMana());

        // Colisión player-enemigos
        if (ControlColision.isPlayerCollidingWithEnemy(player.position, 32, pozoEnemy)) {
            player.takeDamage(1);
        }
        if (enemiesKilledThisFrame > 0) {
            killCount += enemiesKilledThisFrame;
            hud.addScore(enemiesKilledThisFrame * 10);
            hud.addExperience(enemiesKilledThisFrame * 25f); // ← NUEVO: 25 exp por enemigo
        }

        // Actualizar HUD
        hud.setLevel(hud.getLevel());
        hud.setHealth(player.getCurrentHealth(), player.getMaxHealth());

        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();

        ScreenUtils.clear(0.1f, 0.05f, 0.15f, 1f);

       // gridRenderer.render(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        pozoBala.render(batch);
        pozoEnemy.render(batch);
        player.render(batch);
        batch.end();

        // Renderizar HUD (sin cámara del mundo)
        hud.render(batch);
    }

    private void renderGameOver() {
        ScreenUtils.clear(0, 0, 0, 1f);

        // Renderizar HUD con stats finales
        hud.render(batch);

        // Texto de Game Over
        batch.begin();
        // Por ahora usamos la font del HUD (después mejoraremos)
        batch.end();

        // ← ARREGLO: Presiona R para reiniciar
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restart(); // ← Método nuevo
        }
    }

    // ← NUEVO: Método para reiniciar
    private void restart() {
        // Limpiar recursos viejos
        player.dispose();
        pozoBala.dispose();
        pozoEnemy.dispose();
        hud.dispose();

        // Recrear todo
        player = new Player(0, 0);
        pozoBala = new pozoBala();
        pozoEnemy = new pozoEnemy();
        hud = new HUD(SCREEN_WIDTH, SCREEN_HEIGHT);

        enemySpawnTimer = 0;
        killCount = 0;

        System.out.println("Game restarted!");
    }

    private void spawnEnemy() {
        float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
        float distance = 800f;
        float x = player.position.x + MathUtils.cos(angle) * distance;
        float y = player.position.y + MathUtils.sin(angle) * distance;

        pozoEnemy.spawn(x, y);
    }

    private void handleInput() {
        player.velocity.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.velocity.y = player.getCurrentSpeed();
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.velocity.y = -player.getCurrentSpeed();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.velocity.x = -player.getCurrentSpeed();
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.velocity.x = player.getCurrentSpeed();

        if (player.velocity.len() > 0) {
            player.velocity.nor().scl(player.getCurrentSpeed());
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
        pozoBala.dispose();
        pozoEnemy.dispose();
       //
        // gridRenderer.dispose();
        hud.dispose();
    }
}
