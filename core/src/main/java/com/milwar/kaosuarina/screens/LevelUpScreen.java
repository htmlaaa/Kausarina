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
import com.milwar.kaosuarina.systems.Upgrade;
import com.milwar.kaosuarina.ui.FontManager;

public class LevelUpScreen implements Disposable {

    private final ShapeRenderer  shapeRenderer;
    private final OrthographicCamera camera;

    private Array<Upgrade> options;
    private int selectedIndex;
    private boolean isActive;

    private final int screenWidth;
    private final int screenHeight;

    public LevelUpScreen(int screenWidth, int screenHeight) {
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;

        shapeRenderer = new ShapeRenderer();
        camera        = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);

        options       = new Array<>();
        selectedIndex = 0;
        isActive      = false;
    }

    public void show(Array<Upgrade> upgrades) {
        options       = upgrades;
        selectedIndex = 0;
        isActive      = true;
    }

    public void hide()       { isActive = false; }
    public boolean isActive(){ return isActive; }

    public void handleInput() {
        if (!isActive) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
            selectedIndex = Math.max(0, selectedIndex - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
            selectedIndex = Math.min(options.size - 1, selectedIndex + 1);
    }

    public Upgrade getSelectedUpgrade() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            return options.get(selectedIndex);
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

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        renderCards(batch);

        batch.begin();

        BitmapFont fH = FontManager.get().heading;
        BitmapFont fS = FontManager.get().small;

        fH.setColor(Color.GOLD);
        fH.draw(batch, "LEVEL UP!", (screenWidth - 210) / 2f, screenHeight - 40);

        fS.setColor(Color.GRAY);
        fS.draw(batch, "A/D o Flechas para seleccionar  |  ENTER/SPACE confirmar  |  1/2/3 elegir directo",
            50, 72);

        batch.end();
    }

    private void renderCards(SpriteBatch batch) {
        float cardWidth = 300f, cardHeight = 400f, spacing = 50f;
        float totalWidth = (cardWidth * options.size) + (spacing * (options.size - 1));
        float startX = (screenWidth - totalWidth) / 2f;
        float cardY  = (screenHeight - cardHeight) / 2f;

        BitmapFont fL = FontManager.get().large;
        BitmapFont fM = FontManager.get().medium;
        BitmapFont fS = FontManager.get().small;

        for (int i = 0; i < options.size; i++) {
            float cardX = startX + i * (cardWidth + spacing);
            boolean sel = i == selectedIndex;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(sel ? 0.3f : 0.2f, sel ? 0.25f : 0.2f, sel ? 0.1f : 0.2f, 1f);
            shapeRenderer.rect(cardX, cardY, cardWidth, cardHeight);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(sel ? 4 : 2);
            shapeRenderer.setColor(sel ? Color.GOLD : Color.GRAY);
            shapeRenderer.rect(cardX, cardY, cardWidth, cardHeight);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);

            batch.begin();
            Upgrade upgrade = options.get(i);

            fL.setColor(Color.GOLD);
            fL.draw(batch, "" + (i + 1), cardX + 18, cardY + cardHeight - 14);

            fL.setColor(Color.WHITE);
            fL.draw(batch, upgrade.nombre, cardX + 18, cardY + cardHeight - 60, cardWidth - 36, 1, true);

            fM.setColor(Color.LIGHT_GRAY);
            fM.draw(batch, upgrade.descripcion, cardX + 18, cardY + cardHeight - 130, cardWidth - 36, 1, true);

            fM.setColor(Color.CYAN);
            fM.draw(batch, "[" + upgrade.nivel + " / " + upgrade.nivelMax + "]", cardX + 18, cardY + 36);

            batch.end();
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
