package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.milwar.kaosuarina.roles.Role;

/**
 * Carga y sirve los fotogramas de animación de cada personaje.
 * Estructura de ficheros: characters/{rol}/animations/{anim}/{dir}/frame_NNN.png
 * Llamar load() en KaosuarinaGame.create() y dispose() en dispose().
 */
public class AnimationSheets {

    private static final String[] ROLES = {"caballero", "mago", "shooter"};
    private static final String[] DIRS = {"south", "east", "north", "west"};
    private static final String[] ATTACK_DIR_NAME = {"swing", "cast", "shoot"};
    private static final String[] HEAVY_ATTACK_DIR_NAME = {"swing2", "cast2", "shoot2"};
    private static final int WALK_FRAMES = 4;
    private static final int IDLE_FRAMES = 5;
    private static final int ATTACK_FRAMES = 6;
    private static final int HEAVY_ATTACK_FRAMES = 9;
    // frames[rol][anim][dir][frame] — puede ser null si el fichero no existe
    private static Texture[][][][] frames;

    public static void load() {
        frames = new Texture[ROLES.length][Anim.values().length][DIRS.length][];
        for (int r = 0; r < ROLES.length; r++) {
            for (int d = 0; d < DIRS.length; d++) {
                frames[r][Anim.WALK.ordinal()][d] = loadFrames(ROLES[r], "walk", DIRS[d], WALK_FRAMES);
                frames[r][Anim.IDLE.ordinal()][d] = loadFrames(ROLES[r], "idle", DIRS[d], IDLE_FRAMES);
                frames[r][Anim.ATTACK.ordinal()][d] = loadFrames(ROLES[r], ATTACK_DIR_NAME[r], DIRS[d], ATTACK_FRAMES);
                frames[r][Anim.HEAVY_ATTACK.ordinal()][d] = loadFrames(ROLES[r], HEAVY_ATTACK_DIR_NAME[r], DIRS[d], HEAVY_ATTACK_FRAMES);
            }
        }
    }

    private static Texture[] loadFrames(String role, String anim, String dir, int maxFrames) {
        Texture[] result = new Texture[maxFrames];
        int loaded = 0;
        for (int f = 0; f < maxFrames; f++) {
            String path = String.format("characters/%s/animations/%s/%s/frame_%03d.png", role, anim, dir, f);
            if (Gdx.files.internal(path).exists()) {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                result[f] = t;
                loaded++;
            } else {
                break;
            }
        }
        if (loaded == 0) return null;
        // Trim array to actual loaded count
        Texture[] trimmed = new Texture[loaded];
        System.arraycopy(result, 0, trimmed, 0, loaded);
        return trimmed;
    }

    /**
     * Devuelve el fotograma correspondiente, o null si no hay animación para esa combinación.
     */
    public static Texture getFrame(Role.Tipo tipo, Anim anim, int dir, int frame) {
        if (frames == null) return null;
        int r = roleIndex(tipo);
        if (r < 0) return null;
        Texture[] arr = frames[r][anim.ordinal()][dir & 3];
        if (arr == null) return null;
        return arr[frame % arr.length];
    }

    /**
     * Número de fotogramas de una animación para un rol. Devuelve 1 si no existe.
     */
    public static int frameCount(Role.Tipo tipo, Anim anim) {
        if (frames == null) return 1;
        int r = roleIndex(tipo);
        if (r < 0) return 1;
        Texture[] arr = frames[r][anim.ordinal()][0];
        return (arr != null) ? arr.length : 1;
    }

    private static int roleIndex(Role.Tipo tipo) {
        switch (tipo) {
            case CABALLERO:
                return 0;
            case MAGO:
                return 1;
            case SHOOTER:
                return 2;
            default:
                return -1;
        }
    }

    public static void dispose() {
        if (frames == null) return;
        for (Texture[][][] byAnim : frames)
            for (Texture[][] byDir : byAnim)
                for (Texture[] byFrame : byDir)
                    if (byFrame != null)
                        for (Texture t : byFrame)
                            if (t != null) t.dispose();
        frames = null;
    }

    public enum Anim {WALK, IDLE, ATTACK, HEAVY_ATTACK}
}
