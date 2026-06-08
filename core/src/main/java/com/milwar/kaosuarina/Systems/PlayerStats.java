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
    public float physicalDefense = 0f;
    public float magicResistance = 0f;
    public float rango = 1500f; // distancia máxima de proyectiles

    // ── Maná (S2-05) ──────────────────────────────────────────────────────
    // Mago: maxMana=120, manaRegen=2f (regen pasiva única del rol)
    // Caballero/Tirador: maxMana=50, manaRegen=0 (solo recuperan por kills/armas)
    public float maxMana = 0f;  // 0 = sin maná hasta que Role lo configure
    public float mana = 0f;  // maná actual
    public float manaRegen = 0f; // maná por segundo (pasivo; solo Mago lo tiene)
    /**
     * Bonus de regen de maná aportado por afijos de armas equipadas. Recalculado cada frame.
     */
    public float weaponAffixMpRegen = 0f;
    /**
     * Bonus de lifesteal aportado por afijos de armas equipadas. Recalculado cada frame.
     */
    public float weaponAffixLifesteal = 0f;

    // ── Stats base inmutables del rol (S5-02) ─────────────────────────────────
    // Snapshotted by Player constructor. Used by recalcStats() to restore from 0.
    public int hpBase = 0;
    public float defBase = 0f;
    public float resMagBase = 0f;
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
    // Multiplicador del radio de explosión mágica (upgrade Resonancia)
    public float blastRadiusMult = 1f;
    // HP regenerado por segundo (amuleto Tótem de Regen)
    public float hpRegenPerSec = 0f;

    // ── Base de velocidad (snapshot del rol; MEC-01 amulet recalc) ─────────────
    public float baseSpeedBase = 0f;

    // ── Amuletos — bonificadores recalculados desde equippedAmulets[] ────────
    public boolean hasSedDeSangre = false;
    public boolean hasGuardianArena = false;
    public int amuletHpBonus = 0;
    public float amuletSpeedBonus = 0f;
    public float amuletManaBonus = 0f;
    public float amuletRegenBonus = 0f;
    public float amuletLifesteal = 0f;
    public float amuletCritBonus = 0f;   // AMULETO_CRITICO
    public float amuletDefBonus = 0f;   // AMULETO_ARMADURA
    public boolean hasAmuletExplosion = false; // AMULETO_EXPLOSION
    public boolean hasAmuletEspectros = false; // AMULETO_ESPECTROS
    public boolean hasAmuletTiempo = false; // AMULETO_TIEMPO
    public float amuletTiempoTimer = 0f;   // cooldown de AMULETO_TIEMPO
    public int espectrosKillCounter = 0;    // contador kills para AMULETO_ESPECTROS

    // ── Estado de nuevos upgrades (CNT-03) ───────────────────────────────────
    public boolean escudoTemporalConsumed = false;
    public int contragolpeNivel = 0;
    public boolean hasAuraVeneno = false;
    public boolean hasCriticoEncadenado = false;
    public int critStreak = 0;
    public boolean hasPesoMuerto = false;
    public float killSpeedBoostTimer = 0f;
    public boolean hasAuraIntimidacion = false;
    public boolean hasGolpeTierra = false;
    public boolean hasManaOvercharge = false;
    public boolean manaOverchargeReady = false;  // siguiente hechizo hace ×2
    public int barreraMagicaNivel = 0;
    public boolean hasHechizoesferas = false;
    public boolean hasBalaExplosiva = false;
    public boolean hasMunicionEnvenenada = false;
    public boolean hasDisparoTenso = false;
    public float disparoTensoCharge = 0f;    // acumula mientras se mantiene botón

    // ── Estadísticas de run (S4-05) ───────────────────────────────────────────
    public float totalManaSpent = 0f;

    /**
     * Consume maná. Devuelve true si había suficiente.
     */
    public boolean consumeMana(float coste) {
        if (mana < coste) return false;
        mana -= coste;
        totalManaSpent += coste;
        return true;
    }
}
