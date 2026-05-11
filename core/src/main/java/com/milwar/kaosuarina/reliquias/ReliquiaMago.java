package com.milwar.kaosuarina.reliquias;

import com.milwar.kaosuarina.entities.Player;

/**
 * Reliquia del Mago — Resonancia Caótica.
 * Las balas rebotan una vez hacia el enemigo más cercano en radio 200u.
 * La lógica de rebote vive en ColisionManager; este archivo solo declara getBounces()=1.
 */
public class ReliquiaMago implements Reliquia {

    @Override
    public void onDamageReceived(Player player, int damage) {}

    @Override
    public void onKill(Player player) {}

    @Override
    public void onUpdate(Player player, float delta) {}

    @Override
    public int getBounces() {
        return 1;
    }
}
