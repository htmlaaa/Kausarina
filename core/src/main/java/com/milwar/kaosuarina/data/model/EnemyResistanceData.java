package com.milwar.kaosuarina.data.model;

public class EnemyResistanceData {
    public String enemyId;
    public String dmgId;
    /**
     * Percentage resistance: positive = takes less damage, negative = takes more (vulnerability).
     */
    public int resistPct;
    public String notes;
}
