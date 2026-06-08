package com.milwar.kaosuarina.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontManager {

    private static FontManager instance;

    private static final String EXTRA_CHARS = "ÁÉÍÓÚÜÑáéíóúüñ¡¿";

    public final BitmapFont title;    // 68px bold — KAOSUARINA, GAME OVER, VICTORIA
    public final BitmapFont heading;  // 40px bold — LEVEL UP!, section headers
    public final BitmapFont large;    // 28px regular — menu items, stats
    public final BitmapFont medium;   // 20px regular — HUD labels, card text
    public final BitmapFont small;    // 14px regular — hints, slot labels, inscriptions

    private FontManager() {
        FreeTypeFontGenerator genReg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/regular.ttf"));
        FreeTypeFontGenerator genBold = new FreeTypeFontGenerator(Gdx.files.internal("fonts/bold.ttf"));

        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARS;

        p.size = 68;
        title = genBold.generateFont(p);
        p.size = 40;
        heading = genBold.generateFont(p);
        p.size = 28;
        large = genReg.generateFont(p);
        p.size = 20;
        medium = genReg.generateFont(p);
        p.size = 14;
        small = genReg.generateFont(p);

        genReg.dispose();
        genBold.dispose();
    }

    public static FontManager get() {
        if (instance == null) instance = new FontManager();
        return instance;
    }

    public static void disposeAll() {
        if (instance != null) {
            instance.title.dispose();
            instance.heading.dispose();
            instance.large.dispose();
            instance.medium.dispose();
            instance.small.dispose();
            instance = null;
        }
    }
}
