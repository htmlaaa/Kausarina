package com.milwar.kaosuarina.roles;

import com.milwar.kaosuarina.systems.PlayerStats;
import com.milwar.kaosuarina.reliquias.Reliquia;
import com.milwar.kaosuarina.reliquias.ReliquiaCaballero;
import com.milwar.kaosuarina.reliquias.ReliquiaMago;
import com.milwar.kaosuarina.reliquias.ReliquiaTirador;
import com.milwar.kaosuarina.utils.Constants;

public class Role {

    public enum AttackMode {AUTO_SHOOT, MELEE, MAGIC}

    public final Tipo tipo;
    public final String nombre;
    public final PlayerStats stats;
    public final Reliquia reliquia;
    public final AttackMode attackMode;

    public Role(Tipo tipo, String nombre, PlayerStats stats, Reliquia reliquia) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.stats = stats;
        this.reliquia = reliquia;
        this.attackMode = tipo == Tipo.SHOOTER ? AttackMode.AUTO_SHOOT :
            tipo == Tipo.CABALLERO ? AttackMode.MELEE : AttackMode.MAGIC;
    }

    // ── Factory methods ────────────────────────────────────────────────────

    /**
     * Caballero — Fortaleza Reactiva. HP 200, Def 20, Maná 40.
     */
    public static Role caballero() {
        PlayerStats s = new PlayerStats(250f, 0.25f, 1.5f, 1, 15f, 200);
        s.defensa = 20f;
        s.maxMana = Constants.MANA_MAX_CABALLERO_BASE;
        s.mana    = Constants.MANA_MAX_CABALLERO_BASE;
        s.lightAttackCooldown = 0.20f;
        s.heavyAttackCooldown = 1.0f;
        s.meleeLightDamage = 38;
        s.meleeHeavyDamage = 72;
        return new Role(Tipo.CABALLERO, "Caballero", s, new ReliquiaCaballero());
    }

    /**
     * Mago — Resonancia Caótica. HP 70, Def 5, ResMag 15, Maná 120 + regen 2/s.
     */
    public static Role mago() {
        PlayerStats s = new PlayerStats(270f, 0.22f, 1f, 1, 20f, 70);
        s.defensa = 5f;
        s.resistenciaMagica = 15f;
        s.maxMana   = Constants.MANA_MAX_MAGO_BASE;
        s.mana      = Constants.MANA_MAX_MAGO_BASE;
        s.manaRegen = Constants.MAGE_PASSIVE_REGEN;
        s.lightAttackCooldown = 0.28f;
        s.heavyAttackCooldown = 1.5f;
        s.magicLightDamage = 34;
        s.magicHeavyDamage = 85;
        s.magicLightManaCost = 8f;
        s.magicHeavyManaCost = 35f;
        return new Role(Tipo.MAGO, "Mago", s, new ReliquiaMago());
    }

    /**
     * Tirador — Momentum de Combate. HP 80, Def 5, Maná 30, vel 340, CD 0.16s.
     */
    public static Role shooter() {
        PlayerStats s = new PlayerStats(340f, 0.16f, 1.2f, 2, 12f, 80);
        s.defensa = 5f;
        s.maxMana = Constants.MANA_MAX_TIRADOR_BASE;
        s.mana    = Constants.MANA_MAX_TIRADOR_BASE;
        return new Role(Tipo.SHOOTER, "Tirador", s, new ReliquiaTirador());
    }

    public enum Tipo {CABALLERO, MAGO, SHOOTER}
}
