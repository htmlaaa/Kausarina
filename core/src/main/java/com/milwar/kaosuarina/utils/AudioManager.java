package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Static audio facade. All methods are no-ops if the audio file is missing,
 * so the game runs without assets. Place files at:
 *   assets/audio/shot.wav
 *   assets/audio/hit.wav
 *   assets/audio/death.wav
 *   assets/audio/levelup.wav
 *   assets/audio/boss.wav
 *   assets/audio/music.ogg
 */
public class AudioManager {

    private static Sound shotSound;
    private static Sound hitSound;
    private static Sound deathSound;
    private static Sound levelUpSound;
    private static Sound bossSound;
    private static Music bgMusic;

    private static float masterVolume = 0.7f;

    public static void load() {
        shotSound   = tryLoadSound("audio/shot.wav");
        hitSound    = tryLoadSound("audio/hit.wav");
        deathSound  = tryLoadSound("audio/death.wav");
        levelUpSound = tryLoadSound("audio/levelup.wav");
        bossSound   = tryLoadSound("audio/boss.wav");
        bgMusic     = tryLoadMusic("audio/music.ogg");
        if (bgMusic != null) {
            bgMusic.setLooping(true);
            bgMusic.setVolume(masterVolume * 0.5f);
        }
    }

    public static void startMusic() {
        if (bgMusic != null && !bgMusic.isPlaying()) bgMusic.play();
    }

    public static void stopMusic() {
        if (bgMusic != null) bgMusic.stop();
    }

    public static void playShot()    { play(shotSound,    0.25f); }
    public static void playHit()     { play(hitSound,     0.4f);  }
    public static void playDeath()   { play(deathSound,   0.5f);  }
    public static void playLevelUp() { play(levelUpSound, 0.8f);  }
    public static void playBoss()    { play(bossSound,    0.9f);  }

    public static void dispose() {
        disposeSound(shotSound);
        disposeSound(hitSound);
        disposeSound(deathSound);
        disposeSound(levelUpSound);
        disposeSound(bossSound);
        if (bgMusic != null) { bgMusic.dispose(); bgMusic = null; }
    }

    private static Sound tryLoadSound(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                return Gdx.audio.newSound(Gdx.files.internal(path));
            }
        } catch (Exception e) {
            Gdx.app.log("AudioManager", "Missing: " + path);
        }
        return null;
    }

    private static Music tryLoadMusic(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                return Gdx.audio.newMusic(Gdx.files.internal(path));
            }
        } catch (Exception e) {
            Gdx.app.log("AudioManager", "Missing: " + path);
        }
        return null;
    }

    private static void play(Sound sound, float volume) {
        if (sound != null) sound.play(masterVolume * volume);
    }

    private static void disposeSound(Sound s) {
        if (s != null) s.dispose();
    }
}
