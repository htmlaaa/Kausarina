package com.milwar.kaosuarina.systems;

import com.badlogic.gdx.utils.Array;
import com.milwar.kaosuarina.roles.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UpgradeManager {
    private final Array<Upgrade> allUpgrades;
    private final Array<Upgrade> activeUpgrades;
    private final List<Upgrade> appliedUpgrades = new ArrayList<>();
    private final Random random;
    private Role.Tipo currentRole = null;
    private int rerollsLeft = 1;

    public UpgradeManager() {
        allUpgrades = new Array<>();
        activeUpgrades = new Array<>();
        random = new Random();
        initializeUpgrades();
    }

    public void setRole(Role.Tipo role) {
        this.currentRole = role;
    }

    private void initializeUpgrades() {
        // ── Genéricos (todos los roles) ───────────────────────────────────────
        allUpgrades.add(new Upgrade(Upgrade.Tipo.DANIO_UP,
            "Daño +40%", "Aumenta el daño de todos tus ataques un 40%.", 5));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.CADENCIA_UP,
            "Cadencia +15%", "Dispararás un 15% más rápido.", 5));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.VELOCIDAD_UP,
            "Velocidad +10%", "Te mueves un 10% más rápido.", 5));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.VIDA_MAXIMA_UP,
            "Vida Máx +20", "Aumenta tu vida máxima en 20 puntos.", 3));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.FILO_IGNEO,
            "Filo Ígneo", "Tus ataques aplican Quemadura al enemigo.", 1));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.CUCHILLA_VENENO,
            "Cuchilla Venenosa", "Tus ataques aplican Veneno al enemigo.", 1));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.VAMPIRISMO,
            "Vampirismo", "Roba el 3% del daño causado como vida.", 1));

        // ── Tirador ───────────────────────────────────────────────────────────
        allUpgrades.add(new Upgrade(Upgrade.Tipo.PERFORACION,
            "Perforación", "Las balas atraviesan enemigos adicionales.",
            3, Role.Tipo.SHOOTER));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.BALA_EXTRA,
            "Bala Extra", "Dispara 1 bala adicional por ráfaga.",
            3, Role.Tipo.SHOOTER));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.RECARGA_RAPIDA,
            "Recarga Veloz", "Reduce el cooldown de disparo un 15%.",
            4, Role.Tipo.SHOOTER));

        // ── Caballero ─────────────────────────────────────────────────────────
        allUpgrades.add(new Upgrade(Upgrade.Tipo.GOLPE_PESADO,
            "Golpe Letal", "Ataques cuerpo a cuerpo infligen un 30% más de daño.",
            4, Role.Tipo.CABALLERO));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.DEFENSA,
            "Armadura Reforzada", "Aumenta tu defensa física en 8 puntos.",
            4, Role.Tipo.CABALLERO));

        // ── Mago ──────────────────────────────────────────────────────────────
        allUpgrades.add(new Upgrade(Upgrade.Tipo.MANA_MAXIMO_UP,
            "Pozo Arcano", "Aumenta tu maná máximo en 30 puntos.",
            4, Role.Tipo.MAGO));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.RESONANCIA,
            "Resonancia Amplificada", "El radio de la explosión mágica crece un 40%.",
            3, Role.Tipo.MAGO));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.CADENCIA_MAGICA,
            "Foco Arcano", "Lanza el bolt mágico un 20% más rápido.",
            4, Role.Tipo.MAGO));

        // ── CNT-03 Genéricos ──────────────────────────────────────────────────
        allUpgrades.add(new Upgrade(Upgrade.Tipo.ESCUDO_TEMPORAL,
            "Escudo Temporal", "Una vez por run: sobrevives con 1 HP el golpe mortal.",
            1));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.REGENERACION,
            "Regeneración", "+1 HP por segundo pasivamente.",
            3));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.AURA_VENENO,
            "Aura Venenosa", "Envenenas enemigos en radio 80u continuamente.",
            1));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.CRITICO_ENCADENADO,
            "Crít. Encadenado", "Crits consecutivos suman +10% daño (máx 3 stacks).",
            1));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.PESO_MUERTO,
            "Peso Muerto", "Al matar: +8% velocidad durante 2 segundos.",
            1));

        // ── CNT-03 Caballero ─────────────────────────────────────────────────
        allUpgrades.add(new Upgrade(Upgrade.Tipo.CONTRAGOLPE,
            "Contragolpe", "Al recibir daño: devuelves el 30% a enemigos en radio 60u.",
            3, Role.Tipo.CABALLERO));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.AURA_INTIMIDACION,
            "Aura Intimidación", "Enemigos en radio 100u atacan un 15% más lento.",
            1, Role.Tipo.CABALLERO));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.GOLPE_TIERRA,
            "Golpe Tierra", "El ataque pesado genera una onda sísmica adicional.",
            1, Role.Tipo.CABALLERO));

        // ── CNT-03 Mago ───────────────────────────────────────────────────────
        allUpgrades.add(new Upgrade(Upgrade.Tipo.MANA_SOBRECARGA,
            "Maná Sobrecarga", "A 0 maná: el siguiente hechizo inflige el doble de daño.",
            1, Role.Tipo.MAGO));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.BARRERA_MAGICA,
            "Barrera Mágica", "Si maná > 80%: absorbe los primeros 25 de daño por golpe.",
            2, Role.Tipo.MAGO));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.HECHIZO_ESFERAS,
            "Hechizo: Esferas", "El hechizo pesado genera 3 orbes que orbitan 5 segundos.",
            1, Role.Tipo.MAGO));

        // ── CNT-03 Tirador ────────────────────────────────────────────────────
        allUpgrades.add(new Upgrade(Upgrade.Tipo.BALA_EXPLOSIVA,
            "Bala Explosiva", "15% de tus balas explotan en radio 40u al impactar.",
            1, Role.Tipo.SHOOTER));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.MUNICION_ENVENENADA,
            "Munición Venenosa", "Tus balas aplican Veneno con 40% de probabilidad.",
            1, Role.Tipo.SHOOTER));
        allUpgrades.add(new Upgrade(Upgrade.Tipo.DISPARO_TENSO,
            "Disparo Tenso", "Mantener el botón de ataque acumula ×2.5 daño al liberar.",
            1, Role.Tipo.SHOOTER));
    }

    // ── MEC-02 — Reroll ────────────────────────────────────────────────────────

    public int getRerollsLeft() {
        return rerollsLeft;
    }

    public Array<Upgrade> reroll(int cantidad) {
        if (rerollsLeft <= 0) return null;
        rerollsLeft--;
        return getRandomUpgrades(cantidad);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    public boolean hasUpgrade(Upgrade.Tipo tipo) {
        for (Upgrade u : activeUpgrades)
            if (u.tipo == tipo && u.nivel > 0) return true;
        return false;
    }

    public int getUpgradeLevel(Upgrade.Tipo tipo) {
        for (Upgrade u : activeUpgrades)
            if (u.tipo == tipo) return u.nivel;
        return 0;
    }

    public Array<Upgrade> getRandomUpgrades(int cantidad) {
        Array<Upgrade> disponibles = new Array<>();
        for (Upgrade u : allUpgrades) {
            if (u.puedeMejorar() && u.isAvailableFor(currentRole)) disponibles.add(u);
        }
        if (disponibles.size <= cantidad) return disponibles;

        Array<Upgrade> seleccionados = new Array<>();
        Array<Upgrade> temp = new Array<>(disponibles);
        for (int i = 0; i < cantidad; i++) {
            int idx = random.nextInt(temp.size);
            seleccionados.add(temp.get(idx));
            temp.removeIndex(idx);
        }
        return seleccionados;
    }

    public int applyUpgrade(Upgrade upgrade) {
        upgrade.mejorar();
        if (!activeUpgrades.contains(upgrade, true)) activeUpgrades.add(upgrade);
        appliedUpgrades.add(upgrade);
        return upgrade.tipo == Upgrade.Tipo.VIDA_MAXIMA_UP ? 20 : 0;
    }

    // ── Getters de multiplicadores ──────────────────────────────────────────

    public float getDamageMultiplier() {
        float m = 1f;
        for (Upgrade u : activeUpgrades)
            if (u.tipo == Upgrade.Tipo.DANIO_UP) m += 0.4f * u.nivel;
        // MEC-05 — Sinergia CRITICO_ENCADENADO con DANIO_UP
        if (hasUpgrade(Upgrade.Tipo.CRITICO_ENCADENADO) && getUpgradeLevel(Upgrade.Tipo.DANIO_UP) >= 3)
            m += 0.10f;
        return m;
    }

    public float getAttackSpeedMultiplier() {
        float m = 1f;
        for (Upgrade u : activeUpgrades)
            if (u.tipo == Upgrade.Tipo.CADENCIA_UP) m += 0.15f * u.nivel;
        return m;
    }

    public float getSpeedMultiplier() {
        float m = 1f;
        for (Upgrade u : activeUpgrades)
            if (u.tipo == Upgrade.Tipo.VELOCIDAD_UP) m += 0.1f * u.nivel;
        return m;
    }

    public int getExtraBullets() {
        int extra = 0;
        for (Upgrade u : activeUpgrades)
            if (u.tipo == Upgrade.Tipo.BALA_EXTRA) extra += u.nivel;
        return extra;
    }

    public int getPerforationLevel() {
        for (Upgrade u : activeUpgrades)
            if (u.tipo == Upgrade.Tipo.PERFORACION) return u.nivel;
        return 0;
    }

    public int getNivelDanio() {
        for (Upgrade u : activeUpgrades)
            if (u.tipo == Upgrade.Tipo.DANIO_UP) return u.nivel;
        return 0;
    }

    public com.milwar.kaosuarina.utils.DamageType getElementalOnHit() {
        for (Upgrade u : activeUpgrades) {
            if (u.tipo == Upgrade.Tipo.FILO_IGNEO && u.nivel > 0) return com.milwar.kaosuarina.utils.DamageType.FUEGO;
            if (u.tipo == Upgrade.Tipo.CUCHILLA_VENENO && u.nivel > 0)
                return com.milwar.kaosuarina.utils.DamageType.VENENO;
        }
        return null;
    }

    public float getLifeStealPercent() {
        for (Upgrade u : activeUpgrades)
            if (u.tipo == Upgrade.Tipo.VAMPIRISMO && u.nivel > 0)
                return com.milwar.kaosuarina.utils.Constants.LIFESTEAL_BASE_PERCENT;
        return 0f;
    }

    // ── MEC-05 — Sinergia: VAMPIRISMO + VIDA_MAXIMA_UP ────────────────────────

    /**
     * Returns true if the Vampirismo synergy (radial lifesteal on kill) is active.
     */
    public boolean hasVampirismoSinergia() {
        return hasUpgrade(Upgrade.Tipo.VAMPIRISMO)
            && getUpgradeLevel(Upgrade.Tipo.VIDA_MAXIMA_UP) >= 3;
    }

    // ── MEC-05 — Sinergia: BALA_EXTRA + PERFORACION ──────────────────────────

    /**
     * Extra pierce granted by the BALA_EXTRA+PERFORACION synergy.
     */
    public int getSinergiaPierceBonus() {
        return (hasUpgrade(Upgrade.Tipo.BALA_EXTRA) && hasUpgrade(Upgrade.Tipo.PERFORACION))
            ? getExtraBullets() : 0;
    }

    // ── MEC-05 — Sinergia: CUCHILLA_VENENO + CADENCIA_UP ────────────────────

    /**
     * Returns 2 if the Cuchilla+Cadencia synergy is active (double poison stacks), else 1.
     */
    public int getPoisonStackMultiplier() {
        return (hasUpgrade(Upgrade.Tipo.CUCHILLA_VENENO)
            && getUpgradeLevel(Upgrade.Tipo.CADENCIA_UP) >= 3) ? 2 : 1;
    }

    public Array<Upgrade> getActiveUpgrades() {
        return activeUpgrades;
    }

    public List<Upgrade> getAppliedUpgrades() {
        return Collections.unmodifiableList(appliedUpgrades);
    }
}
