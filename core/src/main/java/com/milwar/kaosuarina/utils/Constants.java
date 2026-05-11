package com.milwar.kaosuarina.utils;

public class Constants {
    public static final int   SCREEN_WIDTH        = 1280;
    public static final int   SCREEN_HEIGHT       = 720;
    public static final float ARENA_RADIUS        = 8000f;
    public static final float BALA_RADIO          = 4f;
    public static final float BALA_ENEMIGA_RADIO  = 5f;

    // Daño de contacto por tipo de enemigo (diferenciado en S2-08 con ColisionManager)
    public static final int CONTACT_DAMAGE_DEFAULT = 10;
    public static final int CONTACT_DAMAGE_BASICO  = 8;
    public static final int CONTACT_DAMAGE_RAPIDO  = 6;
    public static final int CONTACT_DAMAGE_TANQUE  = 15;
    public static final int CONTACT_DAMAGE_SHOOTER = 8;
}
