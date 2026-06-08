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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.milwar.kaosuarina.systems.Upgrade;
import com.milwar.kaosuarina.ui.FontManager;

public class LevelUpScreen implements Disposable {

    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private Array<Upgrade> options;
    private int selectedIndex;
    private boolean isActive;

    private final int screenWidth;
    private final int screenHeight;

    public LevelUpScreen(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        viewport = new FitViewport(screenWidth, screenHeight, camera);

        options = new Array<>();
        selectedIndex = 0;
        isActive = false;
    }

    private boolean rerollRequested = false;
    private int rerollsLeft = 1;

    public void show(Array<Upgrade> upgrades) {
        this.options = upgrades;
        this.selectedIndex = 0;
        this.isActive = true;
        this.rerollRequested = false;
    }

    public void setRerollsLeft(int n) {
        this.rerollsLeft = n;
    }

    public boolean isRerollRequested() {
        if (rerollRequested) {
            rerollRequested = false;
            return true;
        }
        return false;
    }

    public void hide() {
        this.isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public void handleInput() {
        if (!isActive) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
            selectedIndex = Math.max(0, selectedIndex - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
            selectedIndex = Math.min(options.size - 1, selectedIndex + 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && rerollsLeft > 0)
            rerollRequested = true;
    }

    public Upgrade getSelectedUpgrade() {
        if (options.size == 0) return null;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            return options.get(selectedIndex);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && options.size >= 1) return options.get(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && options.size >= 2) return options.get(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && options.size >= 3) return options.get(2);
        return null;
    }

    public void render(SpriteBatch batch) {
        if (!isActive) return;

        viewport.apply(true);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Full-screen dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.82f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        renderCards(batch);

        BitmapFont fTitle = FontManager.get().title;
        BitmapFont fSmall = FontManager.get().small;

        batch.begin();

        fTitle.setColor(Color.GOLD);
        fTitle.draw(batch, "LEVEL UP!", (screenWidth - 340) / 2f, screenHeight - 42);
        fTitle.setColor(Color.WHITE);

        fSmall.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        fSmall.draw(batch,
            "A/D  Navegar   |   ENTER/SPACE  Confirmar   |   1 / 2 / 3  Elegir directo",
            60, 36);

        // Reroll token (MEC-02)
        if (rerollsLeft > 0) {
            fSmall.setColor(new Color(0.9f, 0.75f, 0.2f, 1f));
            fSmall.draw(batch, "[R]  Mezclar", screenWidth - 180f, 36);
        } else {
            fSmall.setColor(new Color(0.35f, 0.35f, 0.35f, 1f));
            fSmall.draw(batch, "[R]  Agotado", screenWidth - 180f, 36);
        }
        fSmall.setColor(Color.WHITE);

        batch.end();
    }

    private void renderCards(SpriteBatch batch) {
        float cardW = 340f;
        float cardH = 460f;
        float spacing = 36f;
        float totalW = (cardW * options.size) + (spacing * (options.size - 1));
        float startX = (screenWidth - totalW) / 2f;
        float cardY = (screenHeight - cardH) / 2f - 10f;

        BitmapFont fHeading = FontManager.get().heading;
        BitmapFont fLarge = FontManager.get().large;
        BitmapFont fMedium = FontManager.get().medium;
        BitmapFont fSmall = FontManager.get().small;

        for (int i = 0; i < options.size; i++) {
            float cardX = startX + i * (cardW + spacing);
            boolean sel = i == selectedIndex;

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(sel ? 0.28f : 0.18f, sel ? 0.22f : 0.14f, sel ? 0.08f : 0.04f, 1f);
            shapeRenderer.rect(cardX, cardY, cardW, cardH);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(sel ? 4 : 2);
            shapeRenderer.setColor(sel ? Color.GOLD : Color.GRAY);
            shapeRenderer.rect(cardX, cardY, cardW, cardH);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);

            Upgrade upgrade = options.get(i);
            batch.begin();

            // Number badge
            fHeading.setColor(Color.GOLD);
            fHeading.draw(batch, "" + (i + 1), cardX + 14, cardY + cardH - 10);

            // Name — 2 lines of space
            fLarge.setColor(Color.WHITE);
            fLarge.draw(batch, upgrade.nombre, cardX + 14, cardY + cardH - 55, cardW - 28, 1, true);

            // Description — generous wrapping area
            fMedium.setColor(Color.LIGHT_GRAY);
            fMedium.draw(batch, upgrade.descripcion, cardX + 14, cardY + cardH - 110,
                cardW - 28, 1, true);

            // Role tag (if role-specific)
            if (upgrade.roles != null && upgrade.roles.length > 0) {
                fSmall.setColor(new Color(0.8f, 0.6f, 1f, 1f));
                fSmall.draw(batch, "[ " + upgrade.roles[0].name() + " ]", cardX + 14, cardY + 52);
            }

            // Level indicator
            fSmall.setColor(Color.CYAN);
            fSmall.draw(batch, upgrade.nivel + " / " + upgrade.nivelMax, cardX + 14, cardY + 28);

            fSmall.setColor(Color.WHITE);
            fMedium.setColor(Color.WHITE);
            fLarge.setColor(Color.WHITE);
            fHeading.setColor(Color.WHITE);
            batch.end();
        }
    }

    public void resize(int w, int h) {
        viewport.update(w, h, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
