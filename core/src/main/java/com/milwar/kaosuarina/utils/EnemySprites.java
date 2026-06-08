package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.milwar.kaosuarina.entities.Enemy;

/**
 * Animated sprites for BASICO, RAPIDO and TANQUE enemies.
 * All other enemy types continue using SharedTextures circles.
 * Null-safe: returns null if files are missing so render falls back gracefully.
 *
 * Asset paths: characters/enemies/{name}/walk/{dir}/frame_NNN.png
 *              characters/enemies/{name}/{dir}.png  (static rotation)
 */
public class EnemySprites {

    public static final int DIR_SOUTH = 0;
    public static final int DIR_EAST  = 1;
    public static final int DIR_NORTH = 2;
    public static final int DIR_WEST  = 3;

    private static final float FPS_BASICO = 8f;
    private static final float FPS_RAPIDO = 14f;
    private static final float FPS_TANQUE = 7f;

    private static final String[] DIRS = {"south", "east", "north", "west"};

    // [dir][frame] per enemy type
    private static Texture[][] basico;
    private static Texture[][] rapido;
    private static Texture[][] tanque;

    public static void load() {
        basico = loadEnemy("basico", 6);
        rapido = loadEnemy("rapido", 4);
        tanque = loadEnemy("tanque", 6);
    }

    private static Texture[][] loadEnemy(String name, int maxFrames) {
        Texture[][] result = new Texture[DIRS.length][];
        for (int d = 0; d < DIRS.length; d++) {
            Texture[] buf = new Texture[maxFrames];
            int loaded = 0;
            for (int f = 0; f < maxFrames; f++) {
                String path = String.format("characters/enemies/%s/walk/%s/frame_%03d.png",
                    name, DIRS[d], f);
                if (Gdx.files.internal(path).exists()) {
                    Texture t = new Texture(Gdx.files.internal(path));
                    t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    buf[f] = t;
                    loaded++;
                } else {
                    break;
                }
            }
            if (loaded > 0) {
                Texture[] trimmed = new Texture[loaded];
                System.arraycopy(buf, 0, trimmed, 0, loaded);
                result[d] = trimmed;
            }
        }
        return result;
    }

    /**
     * Returns the current animation frame for an enemy, or null if no sprite loaded.
     *
     * @param tipo      enemy type
     * @param dir       direction (DIR_SOUTH/EAST/NORTH/WEST)
     * @param animTimer accumulated time in seconds (wraps internally)
     */
    public static Texture getFrame(Enemy.Tipo tipo, int dir, float animTimer) {
        Texture[][] sheet = sheetFor(tipo);
        if (sheet == null) return null;
        Texture[] arr = sheet[dir & 3];
        if (arr == null) return null;
        float fps = fpsFor(tipo);
        int frame = (int)(animTimer * fps) % arr.length;
        return arr[frame];
    }

    private static Texture[][] sheetFor(Enemy.Tipo tipo) {
        switch (tipo) {
            case BASICO: return basico;
            case RAPIDO: return rapido;
            case TANQUE: return tanque;
            default:     return null;
        }
    }

    private static float fpsFor(Enemy.Tipo tipo) {
        switch (tipo) {
            case RAPIDO: return FPS_RAPIDO;
            case TANQUE: return FPS_TANQUE;
            default:     return FPS_BASICO;
        }
    }

    public static void dispose() {
        disposeSheet(basico);
        disposeSheet(rapido);
        disposeSheet(tanque);
        basico = rapido = tanque = null;
    }

    private static void disposeSheet(Texture[][] sheet) {
        if (sheet == null) return;
        for (Texture[] arr : sheet)
            if (arr != null)
                for (Texture t : arr)
                    if (t != null) t.dispose();
    }
}
