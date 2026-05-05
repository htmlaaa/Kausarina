package com.milwar.kaosuarina;

import com.badlogic.gdx.Game;
import com.milwar.kaosuarina.screens.GameScreen;

public class KaosuarinaGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
