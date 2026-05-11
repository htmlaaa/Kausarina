package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.milwar.kaosuarina.roles.Role;

/**
 * Carga y sirve los sprites direccionales de cada personaje.
 * Llamar load() en KaosuarinaGame.create() y dispose() en dispose().
 */
public class SpriteSheets {

    public static final int DIR_SOUTH = 0;
    public static final int DIR_EAST  = 1;
    public static final int DIR_NORTH = 2;
    public static final int DIR_WEST  = 3;

    // sprites[roleTipo.ordinal()][dirección]
    private static Texture[][] sprites;

    public static void load() {
        String[] roles = { "caballero", "mago", "shooter" };
        String[] dirs  = { "south", "east", "north", "west" };

        sprites = new Texture[roles.length][dirs.length];
        for (int r = 0; r < roles.length; r++) {
            for (int d = 0; d < dirs.length; d++) {
                String path = "characters/" + roles[r] + "/" + dirs[d] + ".png";
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                sprites[r][d] = t;
            }
        }
    }

    public static Texture getSprite(Role.Tipo tipo, int dir) {
        int roleIdx = roleIndex(tipo);
        if (sprites == null || roleIdx < 0) return null;
        return sprites[roleIdx][dir & 3];
    }

    private static int roleIndex(Role.Tipo tipo) {
        switch (tipo) {
            case CABALLERO: return 0;
            case MAGO:      return 1;
            case SHOOTER:   return 2;
            default:        return -1;
        }
    }

    public static void dispose() {
        if (sprites == null) return;
        for (Texture[] row : sprites)
            for (Texture t : row)
                if (t != null) t.dispose();
        sprites = null;
    }
}
