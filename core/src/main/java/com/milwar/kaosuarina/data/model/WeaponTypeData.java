package com.milwar.kaosuarina.data.model;

public class WeaponTypeData {
    public String wtypeId;
    public String name;
    public String category;
    /** Semicolon-separated char IDs, e.g. "CHAR_KNIGHT;CHAR_SHOOTER". Use split(";") to read. */
    public String allowedChars;
    public String hand;
    public float rangeUnits;
    public String description;
}
