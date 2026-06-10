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
    public static final int STATUS_BURN_DAMAGE = 5;    // por tick (cada 0.5s) — reducido: Mago sobrevive 1 proyectil CAOS + BURN completo
    public static final float STATUS_POISON_DURATION = 5f;
    public static final int STATUS_POISON_DAMAGE = 5;  // por tick (cada 1s)
    public static final float LIFESTEAL_BASE_PERCENT = 0.03f;

    // FUEGO — daño base amplificado + DoT temporal
    public static final float STATUS_FIRE_DURATION = 4.5f; // duración del DoT de fuego
    public static final int STATUS_FIRE_DMG_MIN = 2;    // mínimo tick de fuego (cada 0.5s)
    public static final int STATUS_FIRE_DMG_MAX = 3;    // máximo tick de fuego (varía por hit)
    public static final float STATUS_FUEGO_DMG_MULT = 1.15f; // multiplicador de daño base en hit de FUEGO

    // CAOS_PRIMORDIAL — daño verdadero + ralentización
    public static final float CAOS_PRIMORDIAL_SLOW_DURATION = 2f;
    public static final float CAOS_PRIMORDIAL_SLOW_MULT = 0.5f;

    // PSK_SLOW_ON_HIT — weapon passive slow
    public static final float PSK_SLOW_DURATION = 2.5f;
    public static final float PSK_SLOW_MULT = 0.4f;
    // PSK_ARMOR_BREAK — weapon passive defense reduction
    public static final float PSK_ARMOR_BREAK_DURATION = 4f;
    public static final float PSK_ARMOR_BREAK_REDUCTION = 0.15f;

    // Mana system (S5-03)
    public static final float MAGE_PASSIVE_REGEN = 2.0f;
    public static final float RELIQUIA_MAGO_REGEN_PER_UPGRADE = 0.5f;
    public static final int OVERKILL_DIVISOR = 20;
    public static final int MANA_MAX_CABALLERO_BASE = 40;
    public static final int MANA_MAX_MAGO_BASE = 120;
    public static final int MANA_MAX_TIRADOR_BASE = 30;

    // Enemy movesets ──────────────────────────────────────────────────────────
    // BASICO lunge
    public static final float BASICO_LUNGE_COOLDOWN_MIN = 3.0f;
    public static final float BASICO_LUNGE_COOLDOWN_MAX = 5.0f;
    public static final float BASICO_LUNGE_DURATION     = 0.50f;
    public static final float BASICO_LUNGE_RANGE        = 400f;
    public static final float BASICO_LUNGE_SPEED_MULT   = 3.5f;
    // TANQUE ground stomp
    public static final float TANQUE_STOMP_INTERVAL = 4.5f;
    public static final float TANQUE_STOMP_RADIUS   = 170f;
    public static final int   TANQUE_STOMP_DMG      = 20;
    // MALDITO poison nova
    public static final float MALDITO_NOVA_INTERVAL  = 3.0f;
    public static final int   MALDITO_NOVA_DMG       = 8;
    public static final float MALDITO_NOVA_RANGE     = 260f;
    // ESPECTRAL phase teleport
    public static final float ESPECTRAL_TELEPORT_INTERVAL     = 4.5f;
    public static final float ESPECTRAL_TELEPORT_TRIGGER_RANGE = 320f;
    public static final float ESPECTRAL_TELEPORT_TARGET_DIST   = 380f;
    // SHIELDER shield bash
    public static final float SHIELDER_BASH_INTERVAL = 5.0f;
    public static final float SHIELDER_BASH_DURATION = 0.35f;
    public static final float SHIELDER_BASH_SPEED    = 400f;
    public static final float SHIELDER_BASH_RANGE    = 220f;

    // Weapon system (ADR-006)
    public static final int WEAPON_SLOTS = 2;
    public static final float WEAPON_AFFINITY_DMG_MULT = 1.15f;
    public static final float WEAPON_AFFINITY_CD_MULT = 0.9f;
    public static final float MIN_WEAPON_CD = 0.06f;
    public static final float WEAPON_PICKUP_RADIUS = 40f;
    public static final float WEAPON_SWAP_PAUSE_DURATION = 0.5f;
    public static final float WEAPON_SWAP_TIMEOUT = 5f;
    public static final int CHEST_SPAWN_INTERVAL_MIN = 2;
    public static final int CHEST_SPAWN_INTERVAL_MAX = 3;

    // Inscripciones (S6-01)
    public static final float STUN_DURATION_SISMICA = 0.3f;
    public static final int CICATRIZ_DIVISOR = 4;

    // Scroll de inscripción (S6-02)
    public static final int SCROLL_SPAWN_INTERVAL_MIN = 3;
    public static final int SCROLL_SPAWN_INTERVAL_MAX = 4;
    public static final float SCROLL_PICKUP_RADIUS = 40f;
    public static final float SCROLL_SWAP_TIMEOUT = 5f;

    // Amuletos (S6-03)
    public static final int AMULET_SPAWN_INTERVAL_MIN = 4;
    public static final int AMULET_SPAWN_INTERVAL_MAX = 6;
    public static final float AMULET_PICKUP_RADIUS = 40f;
    public static final float AMULET_SWAP_TIMEOUT = 5f;
    public static final int SED_DE_SANGRE_GAIN_AMULET = 5;
    public static final int GUARDIAN_ARENA_GAIN = 20;

    // Invulnerabilidad post-golpe por rol
    public static final float INVULNERABILITY_CABALLERO = 0.30f;
    public static final float INVULNERABILITY_MAGO = 0.42f;  // Mago necesita margen: bajo HP + gestión de maná
    public static final float INVULNERABILITY_SHOOTER = 0.25f;  // leve buffer para contactos accidentales

    // Minijefe Guardián (S6-05)
    public static final int GUARDIAN_HP = 900;
    public static final float GUARDIAN_SPEED = 60f;
    public static final int GUARDIAN_CONTACT_DMG = 25;
    public static final float GUARDIAN_ATTACK_INTERVAL = 3f;
    public static final float GUARDIAN_SHOCKWAVE_RADIUS = 150f;
    public static final int GUARDIAN_SHOCKWAVE_DMG = 20;
    public static final int MINIBOSS_GUARDIAN_WAVE = 10;

    // Minijefe Arquero (S7-03)
    public static final int ARQUERO_HP = 400;
    public static final float ARQUERO_SPEED = 90f;
    public static final int ARQUERO_CONTACT_DMG = 15;
    public static final int ARQUERO_PROJECTILE_DMG = 18;
    public static final float ARQUERO_SHOOT_INTERVAL = 2f;
    public static final float ARQUERO_TELEPORT_INTERVAL = 4f;
    public static final int ARQUERO_MINIBOSS_WAVE = 14;

    // Partículas (S7-02)
    public static final int PARTICLE_POOL_SIZE = 400;
    public static final int PARTICLE_COUNT_DEATH = 12;
    public static final int PARTICLE_COUNT_IMPACT = 4;
    public static final int PARTICLE_COUNT_EXPLOSION = 20;
    public static final float PARTICLE_SPEED_DEATH = 120f;
    public static final float PARTICLE_SPEED_IMPACT = 60f;
    public static final float PARTICLE_LIFE_DEATH = 0.6f;
    public static final float PARTICLE_LIFE_IMPACT = 0.25f;

    // Dificultad de oleadas (SpawnManager)
    // "Kausarina Verzente": agresivo desde el inicio pero escala suave en mid-late para evitar patrones triviales
    public static final float SPAWN_INTERVAL_BASE = 15f;   // inicia más agresivo (era 18s)
    public static final float SPAWN_INTERVAL_MIN = 4.0f;  // techo de intensidad menos extremo (era 3.5s)
    public static final float DIFICULTAD_RAMP_INTERVAL = 35f;   // cada Xs escala la dificultad
    public static final float DIFICULTAD_RAMP_FACTOR = 0.86f; // rampa más gradual en mid-late (era 0.84)

    // Spawn de enemigos por oleada
    public static final int SPAWN_BASE_COUNT = 7;     // wave 1 más controlada (era 12)
    public static final int SPAWN_PER_LEVEL = 5;     // escala más pronunciada por nivel (era 3)
    public static final int SPAWN_RANDOM_EXTRA = 5;     // variación aleatoria ±
    public static final int SPAWN_ELITE_WAVE_START = 4;     // oleada en que empiezan elites

    // Tirador — auto-disparo basado en rango (sin slot de arma)
    public static final float SHOOTER_AUTO_RANGE = 600f;
    public static final int SHOOTER_AUTO_DAMAGE = 18;

    // Nuevos enemigos Sprint 4
    public static final float BERSERKER_SPEED_RAGE = 420f;
    public static final float HEALER_HEAL_RADIUS = 150f;
    public static final float HEALER_HEAL_PER_SEC = 10f;
    public static final float ELITE_CHARGE_TELEGRAPH = 1.5f;
    public static final float ELITE_CHARGE_SPEED = 800f;
    public static final float ELITE_SUMMON_INTERVAL = 8f;
    public static final float ELITE_ZONE_RADIUS = 120f;
    public static final float ELITE_ZONE_SLOW_MULT = 0.45f;

    // Boss El Fragmentado (Sprint 4)
    public static final int FRAGMENTADO_HP = 1600;
    public static final float FRAGMENTADO_SPEED_P1 = 80f;
    public static final float FRAGMENTADO_SPEED_P2 = 110f;
    public static final float FRAGMENTADO_SPEED_P3 = 140f;
    public static final int FRAGMENTADO_CONTACT_DMG = 22;
    public static final float FRAGMENTADO_ATTACK_INT = 2.5f;
    public static final int FRAGMENTADO_WAVE = 16;
    public static final float FRAGMENTADO_PHASE2_THR = 0.67f;
    public static final float FRAGMENTADO_PHASE3_THR = 0.33f;

    // Boss Final — Devastador del Caos (S8-01)
    public static final int DEVASTADOR_HP = 2200;
    public static final float DEVASTADOR_SPEED_P1 = 60f;
    public static final float DEVASTADOR_SPEED_P2 = 100f;
    public static final int DEVASTADOR_CONTACT_DMG = 25;
    public static final int DEVASTADOR_PROJECTILE_DMG = 22;
    public static final float DEVASTADOR_SHOCKWAVE_P1 = 3f;
    public static final float DEVASTADOR_SHOCKWAVE_P2 = 1.5f;
    public static final float DEVASTADOR_SPIRAL_P1 = 5f;
    public static final float DEVASTADOR_SPIRAL_P2 = 3f;
    public static final float DEVASTADOR_SHOCKWAVE_RADIUS = 350f;
    public static final int DEVASTADOR_SPIRAL_BULLETS = 8;
    public static final int DEVASTADOR_FINAL_WAVE = 20;
    public static final float DEVASTADOR_PHASE2_THRESHOLD = 0.5f;
    public static final int DEVASTADOR_PHASE2_SPAWNS = 3;
    public static final int DEVASTADOR_SHOCKWAVE_DMG = 30;
    public static final int DEVASTADOR_XP = 500;

    // Velocidades de proyectil por categoría (S12-VIS-02)
    public static final float BALA_SPEED_MAGICO = 360f;  // 40% más lento que físico
    public static final float BALA_SPEED_DISTANCIA = 900f;
}
