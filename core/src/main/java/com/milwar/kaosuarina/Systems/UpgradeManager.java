package com.milwar.kaosuarina.systems;

import com.badlogic.gdx.utils.Array;
import com.milwar.kaosuarina.roles.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UpgradeManager {
    private final Array<Upgrade> todosLosUpgrades;
    private final Array<Upgrade> upgradesActivos;
    private final List<Upgrade>  upgradesAplicados = new ArrayList<>();
    private final Random         random;
    private Role.Tipo            currentRole = null;

    public UpgradeManager() {
        todosLosUpgrades = new Array<>();
        upgradesActivos  = new Array<>();
        random           = new Random();
        inicializarUpgrades();
    }

    public void setRole(Role.Tipo role) {
        this.currentRole = role;
    }

    private void inicializarUpgrades() {
        // ── Genéricos (todos los roles) ───────────────────────────────────────
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.DANIO_UP,
            "Daño +40%", "Aumenta el daño de todos tus ataques un 40%.", 5));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.CADENCIA_UP,
            "Cadencia +15%", "Dispararás un 15% más rápido.", 5));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.VELOCIDAD_UP,
            "Velocidad +10%", "Te mueves un 10% más rápido.", 5));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.VIDA_MAXIMA_UP,
            "Vida Máx +20", "Aumenta tu vida máxima en 20 puntos.", 3));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.FILO_IGNEO,
            "Filo Ígneo", "Tus ataques aplican Quemadura al enemigo.", 1));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.CUCHILLA_VENENO,
            "Cuchilla Venenosa", "Tus ataques aplican Veneno al enemigo.", 1));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.VAMPIRISMO,
            "Vampirismo", "Roba el 3% del daño causado como vida.", 1));

        // ── Tirador ───────────────────────────────────────────────────────────
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.PERFORACION,
            "Perforación", "Las balas atraviesan enemigos adicionales.",
            3, Role.Tipo.SHOOTER));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.BALA_EXTRA,
            "Bala Extra", "Dispara 1 bala adicional por ráfaga.",
            3, Role.Tipo.SHOOTER));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.RECARGA_RAPIDA,
            "Recarga Veloz", "Reduce el cooldown de disparo un 15%.",
            4, Role.Tipo.SHOOTER));

        // ── Caballero ─────────────────────────────────────────────────────────
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.GOLPE_PESADO,
            "Golpe Letal", "Ataques cuerpo a cuerpo infligen un 30% más de daño.",
            4, Role.Tipo.CABALLERO));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.DEFENSA,
            "Armadura Reforzada", "Aumenta tu defensa física en 8 puntos.",
            4, Role.Tipo.CABALLERO));

        // ── Mago ──────────────────────────────────────────────────────────────
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.MANA_MAXIMO_UP,
            "Pozo Arcano", "Aumenta tu maná máximo en 30 puntos.",
            4, Role.Tipo.MAGO));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.RESONANCIA,
            "Resonancia Amplificada", "El radio de la explosión mágica crece un 40%.",
            3, Role.Tipo.MAGO));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.CADENCIA_MAGICA,
            "Foco Arcano", "Lanza el bolt mágico un 20% más rápido.",
            4, Role.Tipo.MAGO));
    }

    public Array<Upgrade> getUpgradesAleatorios(int cantidad) {
        Array<Upgrade> disponibles = new Array<>();
        for (Upgrade u : todosLosUpgrades) {
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

    public int aplicarUpgrade(Upgrade upgrade) {
        upgrade.mejorar();
        if (!upgradesActivos.contains(upgrade, true)) upgradesActivos.add(upgrade);
        upgradesAplicados.add(upgrade);
        return upgrade.tipo == Upgrade.Tipo.VIDA_MAXIMA_UP ? 20 : 0;
    }

    // ── Getters de multiplicadores ──────────────────────────────────────────

    public float getMultiplicadorDanio() {
        float m = 1f;
        for (Upgrade u : upgradesActivos)
            if (u.tipo == Upgrade.Tipo.DANIO_UP) m += 0.4f * u.nivel;
        return m;
    }

    public float getMultiplicadorCadencia() {
        float m = 1f;
        for (Upgrade u : upgradesActivos)
            if (u.tipo == Upgrade.Tipo.CADENCIA_UP) m += 0.15f * u.nivel;
        return m;
    }

    public float getMultiplicadorVelocidad() {
        float m = 1f;
        for (Upgrade u : upgradesActivos)
            if (u.tipo == Upgrade.Tipo.VELOCIDAD_UP) m += 0.1f * u.nivel;
        return m;
    }

    public int getBalasExtra() {
        int extra = 0;
        for (Upgrade u : upgradesActivos)
            if (u.tipo == Upgrade.Tipo.BALA_EXTRA) extra += u.nivel;
        return extra;
    }

    public int getNivelPerforation() {
        for (Upgrade u : upgradesActivos)
            if (u.tipo == Upgrade.Tipo.PERFORACION) return u.nivel;
        return 0;
    }

    public int getNivelDanio() {
        for (Upgrade u : upgradesActivos)
            if (u.tipo == Upgrade.Tipo.DANIO_UP) return u.nivel;
        return 0;
    }

    public com.milwar.kaosuarina.utils.DamageType getElementalOnHit() {
        for (Upgrade u : upgradesActivos) {
            if (u.tipo == Upgrade.Tipo.FILO_IGNEO    && u.nivel > 0) return com.milwar.kaosuarina.utils.DamageType.FUEGO;
            if (u.tipo == Upgrade.Tipo.CUCHILLA_VENENO && u.nivel > 0) return com.milwar.kaosuarina.utils.DamageType.VENENO;
        }
        return null;
    }

    public float getLifeStealPercent() {
        for (Upgrade u : upgradesActivos)
            if (u.tipo == Upgrade.Tipo.VAMPIRISMO && u.nivel > 0)
                return com.milwar.kaosuarina.utils.Constants.LIFESTEAL_BASE_PERCENT;
        return 0f;
    }

    public Array<Upgrade>  getUpgradesActivos()  { return upgradesActivos; }
    public List<Upgrade>   getUpgradesAplicados() { return Collections.unmodifiableList(upgradesAplicados); }
}
