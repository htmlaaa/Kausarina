package com.milwar.kaosuarina.screens;

public enum Difficulty {
    NORMAL("Normal", "Depth 1 · elites desde oleada 4", 1, 4, 1.0f),
    BRUTAL("Brutal", "Depth 3 · elites desde oleada 1", 3, 1, 1.0f),
    CAOS("Caos", "Depth 5 · elites desde oleada 1 · +20% intensidad", 5, 1, 1.2f);

    public final String label;
    public final String desc;
    /**
     * Starting value for currentDepthForDrops in GameScreen.
     */
    public final int initialDepth;
    /**
     * Wave number at which elite enemies start appearing.
     */
    public final int eliteWaveStart;
    /**
     * Multiplier for spawn rate intensity (>1 = more aggressive).
     */
    public final float intensityMult;

    Difficulty(String label, String desc, int initialDepth, int eliteWaveStart, float intensityMult) {
        this.label = label;
        this.desc = desc;
        this.initialDepth = initialDepth;
        this.eliteWaveStart = eliteWaveStart;
        this.intensityMult = intensityMult;
    }
}
