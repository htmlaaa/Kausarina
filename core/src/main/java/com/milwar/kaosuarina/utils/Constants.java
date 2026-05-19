package com.milwar.kaosuarina.utils;

public class Constants {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final float ARENA_RADIUS = 8000f;
    public static final float BALA_RADIO = 4f;
    public static final float BALA_ENEMIGA_RADIO = 5f;

    // Daño de contacto por tipo de enemigo
    public static final int CONTACT_DAMAGE_DEFAULT = 10;
    public static final int CONTACT_DAMAGE_BASICO = 8;
    public static final int CONTACT_DAMAGE_RAPIDO = 15;  // kamikaze, pena alta por contacto
    public static final int CONTACT_DAMAGE_TANQUE = 15;
    public static final int CONTACT_DAMAGE_SHOOTER = 8;

    // Caballero — ataque de espada en arco
    public static final float MELEE_LIGHT_RADIUS = 140f;
    public static final float MELEE_LIGHT_ARC_DEG = 120f;
    public static final float MELEE_HEAVY_RADIUS = 210f;
    public static final float MELEE_HEAVY_ARC_DEG = 200f;

    // Mago — bolt auto-aim y explosión
    public static final float MAGIC_LIGHT_RANGE = 400f; // radio de búsqueda del enemigo más cercano
    public static final float MAGIC_BLAST_RADIUS = 180f;

    // Daño de contacto — nuevos enemigos Sprint 3
    public static final int CONTACT_DAMAGE_MALDITO = 10;
    public static final int CONTACT_DAMAGE_ESPECTRAL = 7;

    // MALDITO — explosión al morir y veneno de contacto al jugador
    public static final float MALDITO_EXPLOSION_RADIUS = 120f;
    public static final int MALDITO_EXPLOSION_DAMAGE = 25;
    public static final int MALDITO_POISON_DPS = 5;   // daño/tick al jugador (cada 1s)

    // Efectos de estado sobre enemigos (FUEGO → BURN, VENENO → POISON)
    public static final float STATUS_BURN_DURATION = 3f;
    public static final int STATUS_BURN_DAMAGE = 8;   // por tick (cada 0.5s)
    public static final float STATUS_POISON_DURATION = 5f;
    public static final int STATUS_POISON_DAMAGE = 5;   // por tick (cada 1s)
    public static final float LIFESTEAL_BASE_PERCENT = 0.15f;

    // Mana system (S5-03)
    public static final float MAGE_PASSIVE_REGEN              = 2.0f;
    public static final float RELIQUIA_MAGO_REGEN_PER_UPGRADE = 0.5f;
    public static final int   OVERKILL_DIVISOR                = 20;
    public static final int   MANA_MAX_CABALLERO_BASE         = 40;
    public static final int   MANA_MAX_MAGO_BASE              = 120;
    public static final int   MANA_MAX_TIRADOR_BASE           = 30;

    // Weapon system (ADR-006)
    public static final int   WEAPON_SLOTS               = 2;
    public static final float WEAPON_AFFINITY_DMG_MULT   = 1.15f;
    public static final float WEAPON_AFFINITY_CD_MULT    = 0.9f;
    public static final float MIN_WEAPON_CD              = 0.06f;
    public static final float WEAPON_PICKUP_RADIUS       = 40f;
    public static final float WEAPON_SWAP_PAUSE_DURATION = 0.5f;
    public static final float WEAPON_SWAP_TIMEOUT        = 5f;
    public static final int   CHEST_SPAWN_INTERVAL_MIN   = 2;
    public static final int   CHEST_SPAWN_INTERVAL_MAX   = 3;

    // Inscripciones (S6-01)
    public static final float STUN_DURATION_SISMICA = 0.3f;
    public static final int   CICATRIZ_DIVISOR      = 4;

    // Scroll de inscripción (S6-02)
    public static final int   SCROLL_SPAWN_INTERVAL_MIN = 3;
    public static final int   SCROLL_SPAWN_INTERVAL_MAX = 4;
    public static final float SCROLL_PICKUP_RADIUS      = 40f;
    public static final float SCROLL_SWAP_TIMEOUT       = 5f;

    // Amuletos (S6-03)
    public static final int   AMULET_SPAWN_INTERVAL_MIN  = 6;
    public static final int   AMULET_SPAWN_INTERVAL_MAX  = 8;
    public static final float AMULET_PICKUP_RADIUS       = 40f;
    public static final int   SED_DE_SANGRE_GAIN_AMULET  = 5;
    public static final int   GUARDIAN_ARENA_GAIN        = 20;

    // Minijefe Guardián (S6-05)
    public static final int   GUARDIAN_HP               = 800;
    public static final float GUARDIAN_SPEED            = 60f;
    public static final int   GUARDIAN_CONTACT_DMG      = 25;
    public static final float GUARDIAN_ATTACK_INTERVAL  = 3f;
    public static final float GUARDIAN_SHOCKWAVE_RADIUS = 150f;
    public static final int   GUARDIAN_SHOCKWAVE_DMG    = 20;
    public static final int   MINIBOSS_WAVE_INTERVAL    = 10;

    // Minijefe Arquero (S7-03)
    public static final int   ARQUERO_HP                  = 500;
    public static final float ARQUERO_SPEED               = 90f;
    public static final int   ARQUERO_CONTACT_DMG         = 15;
    public static final int   ARQUERO_PROJECTILE_DMG      = 18;
    public static final float ARQUERO_SHOOT_INTERVAL      = 2f;
    public static final float ARQUERO_TELEPORT_INTERVAL   = 4f;
    public static final int   ARQUERO_MINIBOSS_WAVE       = 20;

    // Partículas (S7-02)
    public static final int   PARTICLE_POOL_SIZE       = 400;
    public static final int   PARTICLE_COUNT_DEATH     = 12;
    public static final int   PARTICLE_COUNT_IMPACT    =  4;
    public static final int   PARTICLE_COUNT_EXPLOSION = 20;
    public static final float PARTICLE_SPEED_DEATH     = 120f;
    public static final float PARTICLE_SPEED_IMPACT    =  60f;
    public static final float PARTICLE_LIFE_DEATH      = 0.6f;
    public static final float PARTICLE_LIFE_IMPACT     = 0.25f;

    // Dificultad de oleadas (SpawnManager)
    // MODO TEST: oleada 50 en ~10 min. Para producción usar: 90f / 12f / 150f / 0.92f
    public static final float SPAWN_INTERVAL_BASE      = 25f;   // segundos entre oleadas al inicio
    public static final float SPAWN_INTERVAL_MIN       = 4f;    // intervalo mínimo (máxima intensidad)
    public static final float DIFICULTAD_RAMP_INTERVAL = 50f;   // cada Xs escala la dificultad
    public static final float DIFICULTAD_RAMP_FACTOR   = 0.88f; // reducción del intervalo por ramp

    // Boss Final — Devastador del Caos (S8-01)
    public static final int   DEVASTADOR_HP               = 2000;
    public static final float DEVASTADOR_SPEED_P1         = 60f;
    public static final float DEVASTADOR_SPEED_P2         = 100f;
    public static final int   DEVASTADOR_CONTACT_DMG      = 25;
    public static final int   DEVASTADOR_PROJECTILE_DMG   = 22;
    public static final float DEVASTADOR_SHOCKWAVE_P1     = 3f;
    public static final float DEVASTADOR_SHOCKWAVE_P2     = 1.5f;
    public static final float DEVASTADOR_SPIRAL_P1        = 5f;
    public static final float DEVASTADOR_SPIRAL_P2        = 3f;
    public static final float DEVASTADOR_SHOCKWAVE_RADIUS = 350f;
    public static final int   DEVASTADOR_SPIRAL_BULLETS   = 8;
    public static final int   DEVASTADOR_FINAL_WAVE       = 50;
    public static final float DEVASTADOR_PHASE2_THRESHOLD = 0.5f;
    public static final int   DEVASTADOR_PHASE2_SPAWNS    = 3;
    public static final int   DEVASTADOR_SHOCKWAVE_DMG    = 30;
    public static final int   DEVASTADOR_XP               = 500;
}
