package com.milwar.kaosuarina.screens.managers;

import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.entities.EnemyPool;
import com.milwar.kaosuarina.utils.AudioManager;
import com.milwar.kaosuarina.utils.Constants;

/**
 * Controls wave timing, difficulty scaling, and enemy/boss spawning.
 */
public class SpawnManager {

    private final Player player;
    private final EnemyPool poolEnemigos;
    private final int eliteWaveStart;

    private float timerSpawn;
    private float intervaloSpawnBase;
    private float timerDificultad;
    private int waveCount;

    public SpawnManager(Player player, EnemyPool poolEnemigos) {
        this(player, poolEnemigos, Constants.SPAWN_ELITE_WAVE_START, 1.0f);
    }

    /**
     * @param eliteWaveStart wave number at which elites begin (1 = from wave 1 in Brutal/Caos)
     * @param intensityMult  spawn rate multiplier (1.2 = 20% faster in Caos)
     */
    public SpawnManager(Player player, EnemyPool poolEnemigos, int eliteWaveStart, float intensityMult) {
        this.player = player;
        this.poolEnemigos = poolEnemigos;
        this.eliteWaveStart = eliteWaveStart;
        timerSpawn = 0f;
        intervaloSpawnBase = Constants.SPAWN_INTERVAL_BASE / intensityMult;
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

        if (waveCount >= eliteWaveStart) {
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
        if (wave < 5) {
            return r < 55f ? com.milwar.kaosuarina.entities.Enemy.Tipo.TANQUE
                : com.milwar.kaosuarina.entities.Enemy.Tipo.MALDITO;
        } else if (wave < 10) {
            if (r < 30f) return com.milwar.kaosuarina.entities.Enemy.Tipo.TANQUE;
            if (r < 55f) return com.milwar.kaosuarina.entities.Enemy.Tipo.MALDITO;
            if (r < 75f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ESPECTRAL;
            return com.milwar.kaosuarina.entities.Enemy.Tipo.ELITE_CHARGE;
        } else if (wave < 16) {
            if (r < 20f) return com.milwar.kaosuarina.entities.Enemy.Tipo.TANQUE;
            if (r < 38f) return com.milwar.kaosuarina.entities.Enemy.Tipo.MALDITO;
            if (r < 54f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ESPECTRAL;
            if (r < 70f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ELITE_CHARGE;
            if (r < 85f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ELITE_SUMMON;
            return com.milwar.kaosuarina.entities.Enemy.Tipo.ELITE_ZONE;
        } else {
            if (r < 15f) return com.milwar.kaosuarina.entities.Enemy.Tipo.TANQUE;
            if (r < 30f) return com.milwar.kaosuarina.entities.Enemy.Tipo.MALDITO;
            if (r < 44f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ESPECTRAL;
            if (r < 58f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ELITE_CHARGE;
            if (r < 72f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ELITE_SUMMON;
            if (r < 86f) return com.milwar.kaosuarina.entities.Enemy.Tipo.ELITE_ZONE;
            return com.milwar.kaosuarina.entities.Enemy.Tipo.SHOOTER;
        }
    }

    // Boss progression: wave 10 = Guardian, 14 = Arquero, 16 = Fragmentado, 20 = Devastador (WIN)
    private void spawnBossesForWave() {
        if (waveCount == Constants.MINIBOSS_GUARDIAN_WAVE) {
            float angle = MathUtils.random(MathUtils.PI2);
            poolEnemigos.spawnGuardian(
                player.position.x + MathUtils.cos(angle) * 600f,
                player.position.y + MathUtils.sin(angle) * 600f);
            AudioManager.playBoss();
        }
        if (waveCount == Constants.ARQUERO_MINIBOSS_WAVE) {
            float angle = MathUtils.random(MathUtils.PI2);
            poolEnemigos.spawnArquero(
                player.position.x + MathUtils.cos(angle) * 550f,
                player.position.y + MathUtils.sin(angle) * 550f);
            AudioManager.playBoss();
        }
        if (waveCount == Constants.FRAGMENTADO_WAVE) {
            float angle = MathUtils.random(MathUtils.PI2);
            poolEnemigos.spawnFragmentado(
                player.position.x + MathUtils.cos(angle) * 650f,
                player.position.y + MathUtils.sin(angle) * 650f);
            AudioManager.playBoss();
        }
        if (waveCount == Constants.DEVASTADOR_FINAL_WAVE) {
            float angle = MathUtils.random(MathUtils.PI2);
            poolEnemigos.spawnDevastador(
                player.position.x + MathUtils.cos(angle) * 700f,
                player.position.y + MathUtils.sin(angle) * 700f);
            AudioManager.playBoss();
        }
    }
}
