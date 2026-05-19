package com.milwar.kaosuarina.weapons.inscriptions;

import com.milwar.kaosuarina.entities.Enemy;
import java.util.ArrayList;
import java.util.Iterator;

/** Static queue for InscripcionDelEco delayed hits. Ticked by GameScreen each frame. */
public class EchoQueue {

    private static final ArrayList<PendingHit> queue = new ArrayList<>();

    public static void add(Enemy target, float damage) {
        queue.add(new PendingHit(target, damage, 2.0f));
    }

    public static void tick(float delta) {
        Iterator<PendingHit> it = queue.iterator();
        while (it.hasNext()) {
            PendingHit h = it.next();
            h.timer -= delta;
            if (h.timer <= 0) {
                if (h.target.active) h.target.recibirDanio(Math.max(1, (int) h.damage));
                it.remove();
            }
        }
    }

    public static void clear() {
        queue.clear();
    }

    private static class PendingHit {
        final Enemy target;
        final float damage;
        float timer;
        PendingHit(Enemy t, float d, float timer) { target = t; damage = d; this.timer = timer; }
    }
}
