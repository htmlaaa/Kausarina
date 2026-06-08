package com.milwar.kaosuarina.systems;

import com.milwar.kaosuarina.roles.Role;

public class Upgrade {

    public enum Tipo {
        // Genéricos
        DANIO_UP,
        CADENCIA_UP,
        VELOCIDAD_UP,
        VIDA_MAXIMA_UP,
        PERFORACION,
        BALA_EXTRA,
        FILO_IGNEO,
        CUCHILLA_VENENO,
        VAMPIRISMO,
        // Caballero
        GOLPE_PESADO,
        DEFENSA,
        // Mago
        MANA_MAXIMO_UP,
        RESONANCIA,
        CADENCIA_MAGICA,
        // Tirador
        RECARGA_RAPIDA,
        // ── CNT-03 — Genéricos nuevos ──────────────────────────────────────────
        ESCUDO_TEMPORAL,
        REGENERACION,
        AURA_VENENO,
        CRITICO_ENCADENADO,
        PESO_MUERTO,
        // ── CNT-03 — Caballero nuevos ─────────────────────────────────────────
        CONTRAGOLPE,
        AURA_INTIMIDACION,
        GOLPE_TIERRA,
        // ── CNT-03 — Mago nuevos ──────────────────────────────────────────────
        MANA_SOBRECARGA,
        BARRERA_MAGICA,
        HECHIZO_ESFERAS,
        // ── CNT-03 — Tirador nuevos ───────────────────────────────────────────
        BALA_EXPLOSIVA,
        MUNICION_ENVENENADA,
        DISPARO_TENSO,
    }

    public Tipo tipo;
    public String nombre;
    public String descripcion;
    public int nivel;
    public int nivelMax;
    public Role.Tipo[] roles; // null = cualquier rol

    public Upgrade(Tipo tipo, String nombre, String descripcion, int nivelMax) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = 0;
        this.nivelMax = nivelMax;
        this.roles = null;
    }

    public Upgrade(Tipo tipo, String nombre, String descripcion, int nivelMax, Role.Tipo... rolesAllowed) {
        this(tipo, nombre, descripcion, nivelMax);
        this.roles = rolesAllowed.length > 0 ? rolesAllowed : null;
    }

    public boolean isAvailableFor(Role.Tipo role) {
        if (roles == null) return true;
        for (Role.Tipo r : roles) if (r == role) return true;
        return false;
    }

    public boolean puedeMejorar() {
        return nivel < nivelMax;
    }

    public void mejorar() {
        if (puedeMejorar()) nivel++;
    }
}
