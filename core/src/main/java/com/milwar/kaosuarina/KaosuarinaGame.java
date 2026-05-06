package com.milwar.kaosuarina;

import com.badlogic.gdx.Game;
import com.milwar.kaosuarina.screens.GameScreen;
import com.milwar.kaosuarina.utils.SharedTextures;

public class KaosuarinaGame extends Game {
    @Override
    public void create() {
        SharedTextures.load();
        setScreen(new GameScreen());
    }

    @Override
    public void dispose() {
        super.dispose();
        SharedTextures.dispose();
    }
}
