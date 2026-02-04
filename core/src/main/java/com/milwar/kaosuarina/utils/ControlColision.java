package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.entities.Bala;
import com.milwar.kaosuarina.entities.pozoBala;
import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.pozoEnemy;

public class ControlColision {

    // ← CAMBIO: ahora devuelve int (enemigos muertos)
    public static int checkBulletEnemyCollisions(pozoBala pozoBala, pozoEnemy pozoEnemy) {
        int enemiesKilled = 0;

        for (Bala bala : pozoBala.bullets) {
            if (!bala.active) continue;

            for (Enemy enemy : pozoEnemy.getActiveEnemies()) {
                if (!enemy.active) continue;

                if (isColliding(bala.position, 4f, enemy.position, enemy.getRadius())) {
                    bala.active = false;

                    boolean wasAlive = enemy.active;
                    enemy.takeDamage(1);

                    if (wasAlive && !enemy.active) {
                        enemiesKilled++;
                    }

                    break;
                }
            }
        }

        return enemiesKilled;
    }

    public static boolean isPlayerCollidingWithEnemy(Vector2 playerPos, float playerRadius, pozoEnemy pozoEnemy) {
        for (Enemy enemy : pozoEnemy.getActiveEnemies()) {
            if (!enemy.active) continue;

            if (isColliding(playerPos, playerRadius, enemy.position, enemy.getRadius())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isColliding(Vector2 pos1, float radius1, Vector2 pos2, float radius2) {
        return pos1.dst(pos2) < (radius1 + radius2);
    }
}
