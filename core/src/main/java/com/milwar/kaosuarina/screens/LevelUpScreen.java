package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.milwar.kaosuarina.Systems.Upgrade;

public class LevelUpScreen implements Disposable {
    private BitmapFont titleFont;
    private BitmapFont cardFont;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private Array<Upgrade> options;
    private int selectedIndex;
    private boolean isActive;

    private int screenWidth;
    private int screenHeight;

    public LevelUpScreen(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setColor(Color.GOLD);

        cardFont = new BitmapFont();
        cardFont.getData().setScale(2f);
        cardFont.setColor(Color.WHITE);

        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);

        options = new Array<>();
        selectedIndex = 0;
        isActive = false;
    }

    public void show(Array<Upgrade> upgrades) {
        this.options = upgrades;
        this.selectedIndex = 0;
        this.isActive = true;
    }

    public void hide() {
        this.isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public void handleInput() {
        if (!isActive) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedIndex = Math.min(options.size - 1, selectedIndex + 1);
        }

    }

    public Upgrade getSelectedUpgrade() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return options.get(selectedIndex);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && options.size >= 1) return options.get(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && options.size >= 2) return options.get(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && options.size >= 3) return options.get(2);
        return null;
    }

    public void render(SpriteBatch batch) {
        if (!isActive) return;

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Fondo semitransparente
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Renderizar cards
        renderCards(batch);

        batch.begin();

        // Título
        String title = "LEVEL UP!";
        titleFont.draw(batch, title, (screenWidth - 200) / 2, screenHeight - 50);

        // Instrucciones
        cardFont.getData().setScale(1.5f);
        cardFont.draw(batch, "A/D o Flechas para seleccionar | ENTER/SPACE para confirmar | 1/2/3 para elegir directo",
            50, 80);
        cardFont.getData().setScale(2f);

        batch.end();
    }

    private void renderCards(SpriteBatch batch) {
        float cardWidth = 300f;
        float cardHeight = 400f;
        float spacing = 50f;
        float totalWidth = (cardWidth * options.size) + (spacing * (options.size - 1));
        float startX = (screenWidth - totalWidth) / 2;
        float cardY = (screenHeight - cardHeight) / 2;

        for (int i = 0; i < options.size; i++) {
            float cardX = startX + (i * (cardWidth + spacing));

            // Fondo de card
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            if (i == selectedIndex) {
                // Card seleccionada (dorada)
                shapeRenderer.setColor(0.3f, 0.25f, 0.1f, 1f);
            } else {
                // Card no seleccionada (gris oscuro)
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            }

            shapeRenderer.rect(cardX, cardY, cardWidth, cardHeight);
            shapeRenderer.end();

            // Borde
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(i == selectedIndex ? 5 : 2);
            shapeRenderer.setColor(i == selectedIndex ? Color.GOLD : Color.GRAY);
            shapeRenderer.rect(cardX, cardY, cardWidth, cardHeight);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);

            // Texto
            batch.begin();

            Upgrade upgrade = options.get(i);

            // Número de opción
            cardFont.getData().setScale(3f);
            cardFont.setColor(Color.GOLD);
            cardFont.draw(batch, "" + (i + 1), cardX + 20, cardY + cardHeight - 20);

            // Nombre
            cardFont.getData().setScale(2f);
            cardFont.setColor(Color.WHITE);
            cardFont.draw(batch, upgrade.nombre, cardX + 20, cardY + cardHeight - 80, cardWidth - 40, 1, true);

            // Descripción
            cardFont.getData().setScale(1.5f);
            cardFont.setColor(Color.LIGHT_GRAY);
            cardFont.draw(batch, upgrade.descripcion, cardX + 20, cardY + cardHeight - 180, cardWidth - 40, 1, true);

            // Nivel actual
            cardFont.getData().setScale(1.8f);
            cardFont.setColor(Color.CYAN);
            cardFont.draw(batch, "[" + upgrade.nivel + "/" + upgrade.nivelMax + "]",
                cardX + 20, cardY + 40);

            batch.end();
        }
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        cardFont.dispose();
        shapeRenderer.dispose();
    }
}
