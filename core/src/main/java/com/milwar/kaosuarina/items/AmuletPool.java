package com.milwar.kaosuarina.items;

import com.badlogic.gdx.math.MathUtils;

public class AmuletPool {

    private static final AmuletType[] ALL = AmuletType.values();

    public static AmuletType getRandom() {
        return ALL[MathUtils.random(ALL.length - 1)];
    }
}
