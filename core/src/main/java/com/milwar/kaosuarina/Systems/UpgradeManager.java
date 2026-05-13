package com.milwar.kaosuarina.systems;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UpgradeManager {
    private final Array<Upgrade> todosLosUpgrades;
    private final Array<Upgrade> upgradesActivos;
    private final List<Upgrade>  upgradesAplicados = new ArrayList<>();
    private final Random random;

    public UpgradeManager() {
        todosLosUpgrades = new Array<>();
        upgradesActivos = new Array<>();
        random = new Random();
        inicializarUpgrades();
    }

    private void inicializarUpgrades() {
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.DANIO_UP, "Daño +40%", "Aumenta el daño de tus balas", 5));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.CADENCIA_UP, "Cadencia +15%", "Disparas más rápido", 5));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.VELOCIDAD_UP, "Velocidad +10%", "Te mueves más rápido", 5));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.VIDA_MAXIMA_UP, "Vida Máxima +20", "Aumenta tu vida máxima", 3));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.PERFORACION, "Perforación", "Balas atraviesan enemigos", 3));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.BALA_EXTRA, "Bala Extra", "Dispara 1 bala adicional", 3));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.FILO_IGNEO, "Filo Ígneo", "Ataques aplican Quemadura", 1));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.CUCHILLA_VENENO, "Cuchilla Venenosa", "Ataques aplican Veneno", 1));
        todosLosUpgrades.add(new Upgrade(Upgrade.Tipo.VAMPIRISMO, "Vampirismo", "Roba 15% del daño como HP", 1));
    }

    public Array<Upgrade> getUpgradesAleatorios(int cantidad) {
        Array<Upgrade> disponibles = new Array<>();
        for (Upgrade u : todosLosUpgrades) {
            if (u.puedeMejorar()) disponibles.add(u);
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

    /**
     * Returns HP bonus to apply to the player (20 for VIDA_MAXIMA_UP, 0 otherwise).
     */
    public int aplicarUpgrade(Upgrade upgrade) {
        upgrade.mejorar();
        if (!upgradesActivos.contains(upgrade, true)) {
            upgradesActivos.add(upgrade);
        }
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

    /**
     * Nivel de perforación actual (0 = sin perforación)
     */
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
            if (u.tipo == Upgrade.Tipo.FILO_IGNEO && u.nivel > 0) return com.milwar.kaosuarina.utils.DamageType.FUEGO;
            if (u.tipo == Upgrade.Tipo.CUCHILLA_VENENO && u.nivel > 0)
                return com.milwar.kaosuarina.utils.DamageType.VENENO;
        }
        return null;
    }

    public float getLifeStealPercent() {
        for (Upgrade u : upgradesActivos)
            if (u.tipo == Upgrade.Tipo.VAMPIRISMO && u.nivel > 0)
                return com.milwar.kaosuarina.utils.Constants.LIFESTEAL_BASE_PERCENT;
        return 0f;
    }

    public Array<Upgrade> getUpgradesActivos() {
        return upgradesActivos;
    }

    public List<Upgrade> getUpgradesAplicados() {
        return Collections.unmodifiableList(upgradesAplicados);
    }
}
