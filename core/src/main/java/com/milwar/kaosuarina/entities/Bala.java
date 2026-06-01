package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.milwar.kaosuarina.weapons.WeaponType;

public class Bala {
    private static final float DEFAULT_SPEED = 600f;
    private static final float SIZE = 14f;
    private static final float DEFAULT_MAX_DST = 1500f;

    public Vector2 position;
    public Vector2 velocity;
    public boolean active;
    public int damage;
    public DamageType damageType;
    /** Which weapon spawned this bullet. Used by ColisionManager for affinity. */
    public WeaponType sourceWeapon;
    public int pierceLeft;
    public int rebotesRestantes;
    /** Maximum travel distance before the bullet deactivates. */
    public float maxDst;
    /** If true, ColisionManager skips enemy defense when calculating damage. */
    public boolean ignoresDefense = false;
    /** Daño elemental secundario de afijos de arma (+X FUEGO / VENENO / CAOS). 0 = sin efecto. */
    public float addFireDmg   = 0f;
    public float addPoisonDmg = 0f;
    public float addChaosDmg  = 0f;

    private final Vector2 spawnPosition;

    public Bala() {
        position = new Vector2();
        velocity = new Vector2();
        spawnPosition = new Vector2();
        active = false;
        damage = 1;
        damageType = DamageType.FISICO;
        sourceWeapon = null;
        pierceLeft = 0;
        rebotesRestantes = 0;
        maxDst = DEFAULT_MAX_DST;
    }

    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes, DamageType type) {
        activate(x, y, dirX, dirY, damage, pierce, rebotes, type, DEFAULT_SPEED, DEFAULT_MAX_DST, null);
    }

    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes) {
        activate(x, y, dirX, dirY, damage, pierce, rebotes, DamageType.FISICO, DEFAULT_SPEED, DEFAULT_MAX_DST, null);
    }

    /** Full activate with custom speed, range, and weapon source. */
    public void activate(float x, float y, float dirX, float dirY, int damage, int pierce, int rebotes,
                         DamageType type, float speed, float maxDst, WeaponType source) {
        position.set(x, y);
        spawnPosition.set(x, y);
        velocity.set(dirX, dirY).nor().scl(speed);
        active = true;
        this.damage = damage;
        this.damageType = type;
        this.sourceWeapon = source;
        this.pierceLeft = pierce + 1;
        this.rebotesRestantes = rebotes;
        this.maxDst = maxDst;
        this.ignoresDefense = false;
        this.addFireDmg   = 0f;
        this.addPoisonDmg = 0f;
        this.addChaosDmg  = 0f;
    }

    public void update(float delta) {
        if (!active) return;
        position.add(velocity.x * delta, velocity.y * delta);
        if (position.dst(spawnPosition) > maxDst) active = false;
    }

    /**
     * Llamar al impactar un enemigo. Devuelve true si la bala sigue activa (perforación).
     */
    public boolean onHit() {
        pierceLeft--;
        if (pierceLeft <= 0) {
            active = false;
            return false;
        }
        return true;
    }

    /**
     * Redirige la bala hacia un objetivo y descuenta un rebote. Reactiva la bala si estaba inactiva.
     */
    public void rebotar(float targetX, float targetY) {
        velocity.set(targetX - position.x, targetY - position.y).nor().scl(DEFAULT_SPEED);
        spawnPosition.set(position);  // medir MAX_DST desde el punto de rebote
        rebotesRestantes--;
        active = true;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(SharedTextures.getBala(), position.x - SIZE / 2, position.y - SIZE / 2, SIZE, SIZE);
    }

    public void dispose() {
    }
}
