package com.milwar.kaosuarina;

import com.badlogic.gdx.Game;
import com.milwar.kaosuarina.screens.CharacterSelectScreen;
import com.milwar.kaosuarina.utils.AnimationSheets;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.milwar.kaosuarina.utils.SpriteSheets;

public class KaosuarinaGame extends Game {
    @Override
    public void create() {
        SharedTextures.load();
        SpriteSheets.load();
        AnimationSheets.load();
        setScreen(new CharacterSelectScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        SharedTextures.dispose();
        SpriteSheets.dispose();
        AnimationSheets.dispose();
    }
}
