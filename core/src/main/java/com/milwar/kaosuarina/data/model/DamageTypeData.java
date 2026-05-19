package com.milwar.kaosuarina.data.model;

public class DamageTypeData {
    public String dmgId;
    public String name;
    public String family;
    public String scalingStat;
    /** Empty string means no resistance stat (e.g. DMG_CHAOS ignores resistances). */
    public String resistStat;
    public String description;
}
