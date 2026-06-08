package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.milwar.kaosuarina.weapons.WeaponType;

public class Bullet {
    private static final float DEFAULT_SPEED = 600f;
    private static final float SIZE = 14f;
    private static final float DEFAULT_MAX_DST = 1500f;

    public Vector2 position;
    public Vector2 velocity;
    public boolean active;
    public int damage;
    public DamageType damageType;
    public WeaponType sourceWeapon;
    public int sourceSlot = -1;
    public int pierceLeft;
    public int bouncesLeft;
    public float oscTimer = 0f;
    public float maxDst;
    public boolean ignoresDefense = false;
    public float addFireDmg = 0f;
    public float addPoisonDmg = 0f;
    public float addChaosDmg = 0f;
    // Weapon passive skill flags (PSK_*)
    public float burnProcChance = 0f;
    public float poisonProcChance = 0f;
    public float lifeStealPctWeapon = 0f;
    public boolean slowOnHit = false;
    public float armorBreakChance = 0f;
    public float chaosProc = 0f;

    private final Vector2 spawnPosition;

    public Bullet() {
        position = new Vector2();
        velocity = new Vector2();
        spawnPosition = new Vector2();
        active = false;
        damage = 1;
        damageType = DamageType.FISICO;
        sourceWeapon = null;
        pierceLeft = 0;
        bouncesLeft = 0;
        maxDst = DEFAULT_MAX_DST;
    }

    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce, int bounces,
                         DamageType type) {
        activate(x, y, dirX, dirY, damage, pierce, bounces, type, DEFAULT_SPEED, DEFAULT_MAX_DST, null);
    }

    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce, int bounces) {
        activate(x, y, dirX, dirY, damage, pierce, bounces, DamageType.FISICO, DEFAULT_SPEED, DEFAULT_MAX_DST, null);
    }

    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce, int bounces,
                         DamageType type, float speed, float maxDst, WeaponType source) {
        position.set(x, y);
        spawnPosition.set(x, y);
        velocity.set(dirX, dirY).nor().scl(speed);
        active = true;
        this.damage = damage;
        this.damageType = type;
        this.sourceWeapon = source;
        this.sourceSlot = -1;
        this.pierceLeft = pierce + 1;
        this.bouncesLeft = bounces;
        this.maxDst = maxDst;
        this.ignoresDefense = false;
        this.addFireDmg = 0f;
        this.addPoisonDmg = 0f;
        this.addChaosDmg = 0f;
        this.burnProcChance = 0f;
        this.poisonProcChance = 0f;
        this.lifeStealPctWeapon = 0f;
        this.slowOnHit = false;
        this.armorBreakChance = 0f;
        this.chaosProc = 0f;
        this.oscTimer = 0f;
    }

    public void update(float delta) {
        if (!active) return;
        oscTimer += delta;
        position.add(velocity.x * delta, velocity.y * delta);
        if (position.dst(spawnPosition) > maxDst) active = false;
    }

    public boolean onHit() {
        pierceLeft--;
        if (pierceLeft <= 0) {
            active = false;
            return false;
        }
        return true;
    }

    public void bounce(float targetX, float targetY) {
        velocity.set(targetX - position.x, targetY - position.y).nor().scl(DEFAULT_SPEED);
        spawnPosition.set(position);
        bouncesLeft--;
        active = true;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        float size;
        float r, g, b;
        float renderX = position.x;
        float renderY = position.y;
        switch (damageType) {
            case MAGICO:
                size = 22f; r = 0.3f; g = 0.6f; b = 1.0f;
                break;
            case FUEGO:
                size = 18f; r = 1.0f; g = 0.4f; b = 0.1f;
                break;
            case VENENO:
                size = 16f; r = 0.3f; g = 1.0f; b = 0.3f;
                break;
            case CAOS:
                size = 20f; r = 0.8f; g = 0.2f; b = 1.0f;
                float len = velocity.len();
                if (len > 0.001f) {
                    float perp = com.badlogic.gdx.math.MathUtils.sin(oscTimer * 8f) * 8f;
                    renderX += (-velocity.y / len) * perp;
                    renderY += (velocity.x / len) * perp;
                }
                break;
            case CAOS_PRIMORDIAL:
                size = 24f; r = 0.5f; g = 0.0f; b = 1.0f;
                break;
            default:
                size = 14f; r = 1.0f; g = 1.0f; b = 1.0f;
                break;
        }
        batch.setColor(r, g, b, 1f);
        batch.draw(SharedTextures.getBala(), renderX - size / 2f, renderY - size / 2f, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void dispose() {
    }
}
