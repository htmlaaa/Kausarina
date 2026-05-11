package com.milwar.kaosuarina.roles;

import com.milwar.kaosuarina.Systems.PlayerStats;
import com.milwar.kaosuarina.reliquias.Reliquia;
import com.milwar.kaosuarina.reliquias.ReliquiaCaballero;
import com.milwar.kaosuarina.reliquias.ReliquiaMago;
import com.milwar.kaosuarina.reliquias.ReliquiaTirador;

public class Role {

    public enum Tipo { CABALLERO, MAGO, SHOOTER }

    public final Tipo        tipo;
    public final String      nombre;
    public final PlayerStats stats;
    public final Reliquia    reliquia;

    public Role(Tipo tipo, String nombre, PlayerStats stats, Reliquia reliquia) {
        this.tipo     = tipo;
        this.nombre   = nombre;
        this.stats    = stats;
        this.reliquia = reliquia;
    }

    // ── Factory methods ────────────────────────────────────────────────────

    /** Caballero — Fortaleza Reactiva. HP 200, Def 20, Maná 50. */
    public static Role caballero() {
        PlayerStats s = new PlayerStats(250f, 0.25f, 1.5f, 1, 15f, 200);
        s.defensa = 20f;
        s.maxMana = 50f;
        s.mana    = 50f;
        return new Role(Tipo.CABALLERO, "Caballero", s, new ReliquiaCaballero());
    }

    /** Mago — Resonancia Caótica. HP 70, Def 5, ResMag 15, Maná 120 + regen 2/s. */
    public static Role mago() {
        PlayerStats s = new PlayerStats(270f, 0.22f, 1f, 1, 20f, 70);
        s.defensa           = 5f;
        s.resistenciaMagica = 15f;
        s.maxMana           = 120f;
        s.mana              = 120f;
        s.manaRegen         = 2f;
        return new Role(Tipo.MAGO, "Mago", s, new ReliquiaMago());
    }

    /** Tirador — Momentum de Combate. HP 80, Def 5, Maná 30, vel 340, CD 0.16s. */
    public static Role shooter() {
        PlayerStats s = new PlayerStats(340f, 0.16f, 1.2f, 2, 12f, 80);
        s.defensa = 5f;
        s.maxMana = 30f;
        s.mana    = 30f;
        return new Role(Tipo.SHOOTER, "Tirador", s, new ReliquiaTirador());
    }
}
