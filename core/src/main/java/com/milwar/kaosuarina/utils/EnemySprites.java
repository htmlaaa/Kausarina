package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.milwar.kaosuarina.entities.Enemy;

/**
 * Animated walking sprites for enemy types.
 * Null-safe: returns null if files are missing so render falls back gracefully.
 *
 * Asset paths: characters/enemies/{name}/walk/{dir}/frame_NNN.png
 */
public class    EnemySprites {

    public static final int DIR_SOUTH = 0;
    public static final int DIR_EAST  = 1;
    public static final int DIR_NORTH = 2;
    public static final int DIR_WEST  = 3;

    private static final float FPS_BASICO      = 8f;
    private static final float FPS_RAPIDO      = 14f;
    private static final float FPS_TANQUE      = 7f;
    private static final float FPS_SHOOTER     = 8f;
    private static final float FPS_MALDITO     = 8f;
    private static final float FPS_BERSERKER   = 12f;
    private static final float FPS_HEALER      = 7f;
    private static final float FPS_SHIELDER    = 6f;
    private static final float FPS_GUARDIAN    = 7f;
    private static final float FPS_ARQUERO     = 10f;
    private static final float FPS_ESPECTRAL   = 8f;
    private static final float FPS_SPLITTER    = 7f;
    private static final float FPS_ELITE_SUMMON= 8f;
    private static final float FPS_ELITE_ZONE  = 9f;
    private static final float FPS_ELITE_CHARGE= 7f;

    private static final String[] DIRS = {"south", "east", "north", "west"};

    // [dir][frame] per enemy type
    private static Texture[][] basico;
    private static Texture[][] rapido;
    private static Texture[][] tanque;
    private static Texture[][] shooter;
    private static Texture[][] maldito;
    private static Texture[][] berserker;
    private static Texture[][] healer;
    private static Texture[][] shielder;
    private static Texture[][] guardian;
    private static Texture[][] arquero;
    private static Texture[][] espectral;
    private static Texture[][] splitter;
    private static Texture[][] eliteSummon;
    private static Texture[][] eliteZone;
    private static Texture[][] eliteCharge;

    public static void load() {
        basico      = loadEnemy("basico",       6);
        rapido      = loadEnemy("rapido",       4);
        tanque      = loadEnemy("tanque",       6);
        shooter     = loadEnemy("shooter",      8);
        maldito     = loadEnemy("maldito",      4);
        berserker   = loadEnemy("berserker",    4);
        healer      = loadEnemy("healer",       4);
        shielder    = loadEnemy("shielder",     6);
        guardian    = loadEnemy("guardian",     4);
        arquero     = loadEnemy("arquero",      6);
        espectral   = loadEnemy("espectral",    5);
        splitter    = loadEnemy("splitter",     5);
        eliteSummon = loadEnemy("elite_summon", 5);
        eliteZone   = loadEnemy("elite_zone",   5);
        eliteCharge = loadEnemy("elite_charge", 5);
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
            case BASICO:       return basico;
            case RAPIDO:       return rapido;
            case TANQUE:       return tanque;
            case SHOOTER:      return shooter;
            case MALDITO:      return maldito;
            case BERSERKER:    return berserker;
            case HEALER:       return healer;
            case SHIELDER:     return shielder;
            case GUARDIAN:     return guardian;
            case ARQUERO:      return arquero;
            case ESPECTRAL:    return espectral;
            case SPLITTER:     return splitter;
            case ELITE_SUMMON: return eliteSummon;
            case ELITE_ZONE:   return eliteZone;
            case ELITE_CHARGE: return eliteCharge;
            default:           return null;
        }
    }

    private static float fpsFor(Enemy.Tipo tipo) {
        switch (tipo) {
            case RAPIDO:       return FPS_RAPIDO;
            case TANQUE:       return FPS_TANQUE;
            case SHOOTER:      return FPS_SHOOTER;
            case MALDITO:      return FPS_MALDITO;
            case BERSERKER:    return FPS_BERSERKER;
            case HEALER:       return FPS_HEALER;
            case SHIELDER:     return FPS_SHIELDER;
            case GUARDIAN:     return FPS_GUARDIAN;
            case ARQUERO:      return FPS_ARQUERO;
            case ESPECTRAL:    return FPS_ESPECTRAL;
            case SPLITTER:     return FPS_SPLITTER;
            case ELITE_SUMMON: return FPS_ELITE_SUMMON;
            case ELITE_ZONE:   return FPS_ELITE_ZONE;
            case ELITE_CHARGE: return FPS_ELITE_CHARGE;
            default:           return FPS_BASICO;
        }
    }

    public static void dispose() {
        disposeSheet(basico);
        disposeSheet(rapido);
        disposeSheet(tanque);
        disposeSheet(shooter);
        disposeSheet(maldito);
        disposeSheet(berserker);
        disposeSheet(healer);
        disposeSheet(shielder);
        disposeSheet(guardian);
        disposeSheet(arquero);
        disposeSheet(espectral);
        disposeSheet(splitter);
        disposeSheet(eliteSummon);
        disposeSheet(eliteZone);
        disposeSheet(eliteCharge);
        basico = rapido = tanque = shooter = maldito = null;
        berserker = healer = shielder = guardian = arquero = null;
        espectral = splitter = eliteSummon = eliteZone = eliteCharge = null;
    }

    private static void disposeSheet(Texture[][] sheet) {
        if (sheet == null) return;
        for (Texture[] arr : sheet)
            if (arr != null)
                for (Texture t : arr)
                    if (t != null) t.dispose();
    }
}
