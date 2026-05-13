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
    public static final int CONTACT_DAMAGE_RAPIDO = 6;
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
}
