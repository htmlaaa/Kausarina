package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.utils.Array;
import com.milwar.kaosuarina.entities.Enemy;

import java.util.HashMap;

/**
 * Broad-phase spatial hash grid for bullet-vs-enemy collision detection.
 * Reduces O(n*m) to O(k) per bullet by only checking nearby cells.
 *
 * Cell size of 200u means a 8000-radius arena needs at most 80×80 = 6400 cells.
 * Each bullet query checks a 3×3 cell neighbourhood = at most 9 cells.
 */
public class SpatialGrid {

    private static final float CELL_SIZE = 200f;

    private final HashMap<Long, Array<Enemy>> cells = new HashMap<>();
    private final Array<Array<Enemy>> usedBuckets = new Array<>();

    public void clear() {
        for (Array<Enemy> bucket : usedBuckets) bucket.clear();
        usedBuckets.clear();
        cells.clear();
    }

    public void insert(Enemy e) {
        if (!e.active) return;
        long key = cellKey(e.position.x, e.position.y);
        Array<Enemy> bucket = cells.get(key);
        if (bucket == null) {
            bucket = new Array<>(8);
            cells.put(key, bucket);
            usedBuckets.add(bucket);
        }
        bucket.add(e);
    }

    /**
     * Returns all active enemies in cells overlapping the circle centred at (x,y) with the given radius.
     * The returned array is reused across calls — copy it if needed.
     */
    public Array<Enemy> getNearby(float x, float y, float radius) {
        nearby.clear();
        int minCX = cellCoord(x - radius);
        int maxCX = cellCoord(x + radius);
        int minCY = cellCoord(y - radius);
        int maxCY = cellCoord(y + radius);

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cy = minCY; cy <= maxCY; cy++) {
                Array<Enemy> bucket = cells.get(packKey(cx, cy));
                if (bucket == null) continue;
                for (int i = 0; i < bucket.size; i++) {
                    Enemy e = bucket.get(i);
                    if (e.active) nearby.add(e);
                }
            }
        }
        return nearby;
    }

    private final Array<Enemy> nearby = new Array<>(32);

    private long cellKey(float x, float y) {
        return packKey(cellCoord(x), cellCoord(y));
    }

    private static int cellCoord(float v) {
        return (int) Math.floor(v / CELL_SIZE);
    }

    private static long packKey(int cx, int cy) {
        return ((long) cx << 32) | (cy & 0xFFFFFFFFL);
    }
}
