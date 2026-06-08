package com.milwar.kaosuarina.data.model;

public class BossPhaseData {
    public String enemyId;
    public int phase;
    public float hpThresholdPct;
    public String name;
    public String behaviorChange;
    /**
     * Semicolon-separated attack IDs unlocked in this phase. Use split(";") to read.
     */
    public String unlockedAttacks;
    public float speedMult;
    public float dmgMult;
}
