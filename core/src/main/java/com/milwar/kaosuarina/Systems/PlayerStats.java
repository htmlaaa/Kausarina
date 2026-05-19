package com.milwar.kaosuarina.systems;

public class PlayerStats {

    // ── Stats de combate base ──────────────────────────────────────────────
    public float baseSpeed;
    public float baseShootCooldown;
    public float baseDamage;
    public int baseBulletCount;
    public float bulletSpread;
    public int maxHealth;
    public float bulletSpeed;
    public int basePierce;

    // ── Stats RPG (S2-05) ──────────────────────────────────────────────────
    public float defensa = 0f;   // reduce daño físico recibido
    public float resistenciaMagica = 0f;   // reduce daño mágico recibido
    public float rango = 1500f; // distancia máxima de proyectiles

    // ── Maná (S2-05) ──────────────────────────────────────────────────────
    // Mago: maxMana=120, manaRegen=2f (regen pasiva única del rol)
    // Caballero/Tirador: maxMana=50, manaRegen=0 (solo recuperan por kills/armas)
    public float maxMana = 0f;  // 0 = sin maná hasta que Role lo configure
    public float mana = 0f;  // maná actual
    public float manaRegen = 0f; // maná por segundo (pasivo; solo Mago lo tiene)

    // ── Stats base inmutables del rol (S5-02) ─────────────────────────────────
    // Snapshotted by Player constructor. Used by recalcStats() to restore from 0.
    public int   hpBase      = 0;
    public float defBase     = 0f;
    public float resMagBase  = 0f;
    public float manaMaxBase = 0f;

    public PlayerStats() {
        baseSpeed = 400f;
        baseShootCooldown = 0.2f;
        baseDamage = 1f;
        baseBulletCount = 1;
        bulletSpread = 15f;
        maxHealth = 100;
        bulletSpeed = 600f;
        basePierce = 0;
    }

    public PlayerStats(float baseSpeed, float baseShootCooldown, float baseDamage,
                       int baseBulletCount, float bulletSpread, int maxHealth) {
        this();
        this.baseSpeed = baseSpeed;
        this.baseShootCooldown = baseShootCooldown;
        this.baseDamage = baseDamage;
        this.baseBulletCount = baseBulletCount;
        this.bulletSpread = bulletSpread;
        this.maxHealth = maxHealth;
    }

    /**
     * Añade maná al personaje sin superar el máximo.
     */
    public void addMana(float cantidad) {
        mana = Math.min(maxMana, mana + cantidad);
    }

    // ── Sistema de ataque manual (Caballero / Mago) ───────────────────────────
    // Tirador no los usa (tiene baseShootCooldown para auto-shoot).
    public float lightAttackCooldown = 0.4f;
    public float heavyAttackCooldown = 1.2f;

    // Daño base de los ataques manuales
    public int meleeLightDamage = 20;
    public int meleeHeavyDamage = 40;
    public int magicLightDamage = 22;
    public int magicHeavyDamage = 55;
    public float magicLightManaCost = 8f;
    public float magicHeavyManaCost = 35f;

    // ── Robo de vida (Sprint 3 — hook para Sprint 4 armas/reliquias) ──────────
    // 0.0 = deshabilitado; 0.1 = roba 10% del daño causado como HP
    public float lifeStealPercent = 0f;

    // ── Amuletos recogidos en la run (S6-03) ─────────────────────────────────
    public boolean hasSedDeSangre   = false;
    public boolean hasGuardianArena = false;

    // ── Estadísticas de run (S4-05) ───────────────────────────────────────────
    public float manaGastadoTotal = 0f;

    /** Consume maná. Devuelve true si había suficiente. */
    public boolean consumirMana(float coste) {
        if (mana < coste) return false;
        mana -= coste;
        manaGastadoTotal += coste;
        return true;
    }
}
