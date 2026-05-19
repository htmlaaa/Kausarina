package com.milwar.kaosuarina.screens.managers;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.entities.PoolEnemigos;
import com.milwar.kaosuarina.utils.AudioManager;
import com.milwar.kaosuarina.utils.Constants;

/** Controls wave timing, difficulty scaling, and enemy/boss spawning. */
public class SpawnManager {

    private final Player       player;
    private final PoolEnemigos poolEnemigos;

    private float timerSpawn;
    private float intervaloSpawnBase;
    private float timerDificultad;
    private int   waveCount;

    public SpawnManager(Player player, PoolEnemigos poolEnemigos) {
        this.player        = player;
        this.poolEnemigos  = poolEnemigos;
        timerSpawn         = 0f;
        intervaloSpawnBase = Constants.SPAWN_INTERVAL_BASE;
        timerDificultad    = 0f;
        waveCount          = 0;
    }

    public int getWaveCount() { return waveCount; }

    /**
     * Advances wave and difficulty timers.
     * Returns true when a wave fires (enemies + bosses have been spawned).
     * The caller (GameScreen) handles collectible spawns and arena bonus on true.
     */
    public boolean update(float delta, int playerLevel) {
        timerDificultad += delta;
        if (timerDificultad >= Constants.DIFICULTAD_RAMP_INTERVAL) {
            timerDificultad    = 0f;
            intervaloSpawnBase = Math.max(Constants.SPAWN_INTERVAL_MIN,
                intervaloSpawnBase * Constants.DIFICULTAD_RAMP_FACTOR);
        }

        timerSpawn -= delta;
        if (timerSpawn > 0) return false;

        timerSpawn = intervaloSpawnBase;
        waveCount++;
        spawnEnemies(playerLevel);
        spawnBossesForWave();
        return true;
    }

    private void spawnEnemies(int playerLevel) {
        int   cantidad  = 1 + (playerLevel / 3);
        float distSpawn = 800f;
        for (int i = 0; i < cantidad; i++) {
            float angulo = MathUtils.random(MathUtils.PI2);
            poolEnemigos.spawn(
                player.position.x + MathUtils.cos(angulo) * distSpawn,
                player.position.y + MathUtils.sin(angulo) * distSpawn);
        }
    }

    private void spawnBossesForWave() {
        if (waveCount % Constants.MINIBOSS_WAVE_INTERVAL == 0) {
            float angle = MathUtils.random(MathUtils.PI2);
            poolEnemigos.spawnGuardian(
                player.position.x + MathUtils.cos(angle) * 600f,
                player.position.y + MathUtils.sin(angle) * 600f);
            AudioManager.playBoss();
        }
        if (waveCount % Constants.ARQUERO_MINIBOSS_WAVE == 0 && waveCount > 0) {
            float angle = MathUtils.random(MathUtils.PI2);
            poolEnemigos.spawnArquero(
                player.position.x + MathUtils.cos(angle) * 550f,
                player.position.y + MathUtils.sin(angle) * 550f);
            AudioManager.playBoss();
        }
        if (waveCount % Constants.DEVASTADOR_FINAL_WAVE == 0 && waveCount > 0) {
            float angle = MathUtils.random(MathUtils.PI2);
            poolEnemigos.spawnDevastador(
                player.position.x + MathUtils.cos(angle) * 700f,
                player.position.y + MathUtils.sin(angle) * 700f);
            AudioManager.playBoss();
        }
    }
}
