package com.milwar.kaosuarina.items;

import com.badlogic.gdx.math.MathUtils;

public class AmuletPool {
    public static AmuletType getRandom() {
        return MathUtils.randomBoolean() ? AmuletType.SED_DE_SANGRE : AmuletType.GUARDIAN_DE_LA_ARENA;
    }
}
