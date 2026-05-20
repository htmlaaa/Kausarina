package com.milwar.kaosuarina;

import com.badlogic.gdx.Game;
import com.milwar.kaosuarina.screens.MainMenuScreen;
import com.milwar.kaosuarina.ui.FontManager;
import com.milwar.kaosuarina.utils.AnimationSheets;
import com.milwar.kaosuarina.utils.AudioManager;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.milwar.kaosuarina.utils.SpriteSheets;
import com.milwar.kaosuarina.data.DataManager;
import com.milwar.kaosuarina.weapons.InscriptionPool;
import com.milwar.kaosuarina.weapons.WeaponPool;

public class KaosuarinaGame extends Game {
    @Override
    public void create() {
        SharedTextures.load();
        SpriteSheets.load();
        AnimationSheets.load();
        WeaponPool.init();
        InscriptionPool.init();
        AudioManager.load();
        FontManager.get();
        DataManager.getInstance();
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        SharedTextures.dispose();
        SpriteSheets.dispose();
        AnimationSheets.dispose();
        WeaponPool.dispose();
        AudioManager.dispose();
        FontManager.disposeAll();
    }
}
