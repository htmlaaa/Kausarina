package com.milwar.kaosuarina.screens.managers;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.entities.PoolEnemigos;
import com.milwar.kaosuarina.utils.AudioManager;
import com.milwar.kaosuarina.utils.Constants;

/**
 * Controls wave timing, difficulty scaling, and enemy/boss spawning.
 */
public class SpawnManager {

    private final Player player;
    private final PoolEnemigos poolEnemigos;

    private float timerSpawn;
    private float intervaloSpawnBase;
    private float timerDificultad;
    private int waveCount;

    public SpawnManager(Player player, PoolEnemigos poolEnemigos) {
        this.player = player;
        this.poolEnemigos = poolEnemigos;
        timerSpawn = 0f;
        intervaloSpawnBase = Constants.SPAWN_INTERVAL_BASE;
        timerDificultad = 0f;
        waveCount = 0;
    }

    public int getWaveCount() {
        return waveCount;
    }

    /**
     * Advances wave and difficulty timers.
     * Returns true when a wave fires (enemies + bosses have been spawned).
     * The caller (GameScreen) handles collectible spawns and arena bonus on true.
     */
    public boolean update(float delta, int playerLevel) {
        timerDificultad += delta;
        if (timerDificultad >= Constants.DIFICULTAD_RAMP_INTERVAL) {
            timerDificultad = 0f;
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
        int base = Constants.SPAWN_BASE_COUNT + playerLevel * Constants.SPAWN_PER_LEVEL;
        int cantidad = base + MathUtils.random(0, Constants.SPAWN_RANDOM_EXTRA);

        for (int i = 0; i < cantidad; i++) {
            float angulo = MathUtils.random(MathUtils.PI2);
            float dist = 700f + MathUtils.random(0f, 250f);
            poolEnemigos.spawn(
                player.position.x + MathUtils.cos(angulo) * dist,
                player.position.y + MathUtils.sin(angulo) * dist);
        }

        // Elites desde oleada 4: grupo especial adicional (máx 3 simultáneos para no crear picos de dificultad no intencionales)
        if (waveCount >= Constants.SPAWN_ELITE_WAVE_START) {
            int elites = 1 + (waveCount / 6);
            elites = Math.min(elites, 3);
            for (int i = 0; i < elites; i++) {
                float angulo = MathUtils.random(MathUtils.PI2);
                float dist = 600f + MathUtils.random(100f);
                poolEnemigos.spawn(
                    player.position.x + MathUtils.cos(angulo) * dist,
                    player.position.y + MathUtils.sin(angulo) * dist,
                    eliteTipo(waveCount));
            }
        }
    }

    private com.milwar.kaosuarina.entities.Enemy.Tipo eliteTipo(int wave) {
        float r = MathUtils.random(100f);
        if (wave < 8) {
            // Oleadas 4-7: TANQUE y MALDITO
            return r < 55f ? com.milwar.kaosuarina.entities.Enemy.Tipo.TANQUE
                : com.milwar.kaosuarina.entities.Enemy.Tipo.MALDITO;
        } else if (wave < 15) {
            // Oleadas 8-14: añade ESPECTRAL y más MALDITO
            if (r < 35f) return com.milwar.kaosuarina.entities.Enemy.Tipo.TANQUE;
            if (r < 65f) return com.milwar.kaosuarina.entities.Enemy.Tipo.MALDITO;
            return com.milwar.kaosuarina.entities.Enemy.Tipo.ESPECTRAL;
        } else {
            // Oleada 15+: mix completo de elites
            if (r < 30f) return com.milwar.kaosuarina.entities.Enemy.Tipo.TANQUE;
            if (r < 55f) return com.milwar.kaosuarina.entities.Enemy.Tipo.MALDITO;
            if (r < 75f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ESPECTRAL;
            return com.milwar.kaosuarina.entities.Enemy.Tipo.SHOOTER;
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
