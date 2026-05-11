package com.milwar.kaosuarina.Systems;

public class PlayerStats {

    // ── Stats de combate base ──────────────────────────────────────────────
    public float baseSpeed;
    public float baseShootCooldown;
    public float baseDamage;
    public int   baseBulletCount;
    public float bulletSpread;
    public int   maxHealth;
    public float bulletSpeed;
    public int   basePierce;

    // ── Stats RPG (S2-05) ──────────────────────────────────────────────────
    public float defensa           = 0f;   // reduce daño físico recibido
    public float resistenciaMagica = 0f;   // reduce daño mágico recibido
    public float rango             = 1500f; // distancia máxima de proyectiles

    // ── Maná (S2-05) ──────────────────────────────────────────────────────
    // Mago: maxMana=120, manaRegen=2f (regen pasiva única del rol)
    // Caballero/Tirador: maxMana=50, manaRegen=0 (solo recuperan por kills/armas)
    public float maxMana  = 0f;  // 0 = sin maná hasta que Role lo configure
    public float mana     = 0f;  // maná actual
    public float manaRegen = 0f; // maná por segundo (pasivo; solo Mago lo tiene)

    public PlayerStats() {
        baseSpeed         = 400f;
        baseShootCooldown = 0.2f;
        baseDamage        = 1f;
        baseBulletCount   = 1;
        bulletSpread      = 15f;
        maxHealth         = 100;
        bulletSpeed       = 600f;
        basePierce        = 0;
    }

    public PlayerStats(float baseSpeed, float baseShootCooldown, float baseDamage,
                       int baseBulletCount, float bulletSpread, int maxHealth) {
        this();
        this.baseSpeed         = baseSpeed;
        this.baseShootCooldown = baseShootCooldown;
        this.baseDamage        = baseDamage;
        this.baseBulletCount   = baseBulletCount;
        this.bulletSpread      = bulletSpread;
        this.maxHealth         = maxHealth;
    }

    /** Añade maná al personaje sin superar el máximo. */
    public void añadirMana(float cantidad) {
        mana = Math.min(maxMana, mana + cantidad);
    }

    /** Consume maná. Devuelve true si había suficiente. */
    public boolean consumirMana(float coste) {
        if (mana < coste) return false;
        mana -= coste;
        return true;
    }
}
