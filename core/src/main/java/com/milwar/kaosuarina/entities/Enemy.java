package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.data.model.DepthScalingData;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.milwar.kaosuarina.utils.StatusEffect;

public class Enemy {

    private static final float SHOOTER_HUIR_DIST = 300f;
    private static final float SHOOTER_ACERCAR_DIST = 500f;
    private static final float SHOOTER_COOLDOWN = 2f;

    private static final Color BURN_TINT = new Color(1f, 0.3f, 0f, 1f);
    private static final Color POISON_TINT = new Color(0.2f, 1f, 0.2f, 1f);

    private final Color renderColor = new Color();
    private final Color oldColor = new Color();
    private final Color color = new Color();
    private final Vector2 tmp = new Vector2();

    public Vector2 position;
    public Vector2 velocity;
    public boolean active;
    public int health;
    public int maxHealth;
    public Tipo tipo;
    public float defensa;           // reducción de daño físico
    public float resistenciaMagica; // reducción de daño mágico
    public boolean debeExplotar;      // MALDITO: set al morir, procesado por ColisionManager
    /** Set true on kill — GameScreen processes loot drop and clears this flag. */
    public boolean pendingLootDrop = false;
    /** JSON catalog ID — maps to enemy_id in enemies.json (S10-02). */
    public String enemyId = null;
    /** Stun timer: while > 0 the enemy skips movement. Set by InscripcionSismica. */
    public float stunTimer = 0f;
    /** Slow timer: while > 0 velocity is scaled by slowMult. Set by CAOS_PRIMORDIAL hits. */
    public float slowTimer = 0f;
    /** Speed multiplier while slowed (0.5 = half speed). */
    public float slowMult  = 1f;
    /** HP snapshot taken before each hit — used by ColisionManager for overkill mana (S6-04). */
    public int lastHpSnapshot = 0;
    /** Boss attack timer: counts down to next shockwave (GUARDIAN only). */
    public float bossAttackTimer = 0f;
    /** Set true for one frame when the Guardian emits a shockwave; GameScreen resets it. */
    public boolean shockwaveThisFrame = false;
    /** Countdown to next teleport (ARQUERO only). */
    public float arqueroTeleportTimer = 0f;
    /** Current phase 1 or 2 (DEVASTADOR only). */
    public int devastadorPhase = 1;
    /** Countdown to next spiral burst (DEVASTADOR only). */
    public float devastadorSpiralTimer = 0f;
    /** True once phase-2 transition has fired (DEVASTADOR only). */
    public boolean phase2Triggered = false;
    /** Set true for one frame when spiral fires; GameScreen resets it for visual. */
    public boolean spiralThisFrame = false;

    public final StatusEffect statusEffect = new StatusEffect();

    private float speed;
    private float size;
    private float shootTimer;

    public Enemy() {
        position = new Vector2();
        velocity = new Vector2();
        active = false;
        shootTimer = 0;
        debeExplotar = false;
    }

    public void activate(float x, float y, Tipo tipo) {
        position.set(x, y);
        velocity.set(0, 0);
        active = true;
        this.tipo = tipo;
        debeExplotar = false;
        enemyId        = null;
        pendingLootDrop = false;
        stunTimer = 0f;
        slowTimer = 0f;
        slowMult  = 1f;
        lastHpSnapshot = 0;
        bossAttackTimer = 0f;
        shockwaveThisFrame = false;
        arqueroTeleportTimer = 0f;
        devastadorPhase = 1;
        devastadorSpiralTimer = 0f;
        phase2Triggered = false;
        spiralThisFrame = false;
        shootTimer = MathUtils.random(0f, SHOOTER_COOLDOWN);
        statusEffect.clear();

        defensa = 0f;
        resistenciaMagica = 0f;

        switch (tipo) {
            case BASICO:
                enemyId = "EN_NORMAL";
                speed = 150f;
                size = 32f;
                health = 40;
                maxHealth = 40;
                color.set(0.2f, 0.9f, 0.3f, 1f);
                break;
            case RAPIDO:
                enemyId = "EN_KAMIKAZE";
                speed = 300f;
                size = 24f;
                health = 25;
                maxHealth = 25;
                color.set(0.3f, 0.6f, 1f, 1f);
                break;
            case TANQUE:
                enemyId = "EN_TANK";
                speed = 80f;
                size = 48f;
                health = 145;
                maxHealth = 145;
                defensa = 20f;
                color.set(0.9f, 0.2f, 0.2f, 1f);
                break;
            case SHOOTER:
                enemyId = "EN_ORBITER";
                speed = 100f;
                size = 32f;
                health = 48;
                maxHealth = 48;
                color.set(0.95f, 0.85f, 0.2f, 1f);
                break;
            case MALDITO:
                enemyId = "EN_MALDITO";
                speed = 130f;
                size = 30f;
                health = 45;
                maxHealth = 45;
                color.set(0.55f, 0.1f, 0.75f, 1f);    // morado oscuro
                break;
            case ESPECTRAL:
                enemyId = "EN_ESPECTRAL";
                speed = 160f;
                size = 28f;
                health = 35;
                maxHealth = 35;
                resistenciaMagica = 10f;
                color.set(0.5f, 0.9f, 1f, 0.45f); // cian semitransparente
                break;
            case GUARDIAN:
                enemyId = "EN_MINIBOSS";
                speed = com.milwar.kaosuarina.utils.Constants.GUARDIAN_SPEED;
                size = 64f;
                health = com.milwar.kaosuarina.utils.Constants.GUARDIAN_HP;
                maxHealth = com.milwar.kaosuarina.utils.Constants.GUARDIAN_HP;
                defensa = 30f;
                resistenciaMagica = 10f;
                bossAttackTimer = com.milwar.kaosuarina.utils.Constants.GUARDIAN_ATTACK_INTERVAL;
                color.set(1f, 0.5f, 0.05f, 1f); // naranja
                break;
            case ARQUERO:
                enemyId = "EN_ELITE_TP";
                speed = com.milwar.kaosuarina.utils.Constants.ARQUERO_SPEED;
                size = 48f;
                health = com.milwar.kaosuarina.utils.Constants.ARQUERO_HP;
                maxHealth = com.milwar.kaosuarina.utils.Constants.ARQUERO_HP;
                resistenciaMagica = 15f;
                arqueroTeleportTimer = MathUtils.random(2f, com.milwar.kaosuarina.utils.Constants.ARQUERO_TELEPORT_INTERVAL);
                shootTimer = MathUtils.random(1f, com.milwar.kaosuarina.utils.Constants.ARQUERO_SHOOT_INTERVAL);
                color.set(0.3f, 0.5f, 1f, 1f); // azul
                break;
            case DEVASTADOR:
                enemyId = "EN_BOSS_FINAL";
                size = 80f;
                health = com.milwar.kaosuarina.utils.Constants.DEVASTADOR_HP;
                maxHealth = com.milwar.kaosuarina.utils.Constants.DEVASTADOR_HP;
                defensa = 40f;
                resistenciaMagica = 30f;
                bossAttackTimer = com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SHOCKWAVE_P1;
                devastadorSpiralTimer = com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SPIRAL_P1;
                color.set(0.55f, 0f, 0.15f, 1f); // rojo oscuro
                break;
        }
    }

    public void update(float delta, Vector2 playerPos, PoolBalasEnemigas bulletPool) {
        if (!active) return;
        if (stunTimer > 0) { stunTimer -= delta; return; }

        // Efecto de estado: tick y daño
        if (statusEffect.isActive()) {
            statusEffect.duration -= delta;
            statusEffect.tickTimer -= delta;
            if (statusEffect.tickTimer <= 0) {
                statusEffect.tickTimer += statusEffect.tickInterval();
                health -= statusEffect.damage;
                if (health <= 0) {
                    if (tipo == Tipo.MALDITO) debeExplotar = true;
                    active = false;
                    return;
                }
            }
            if (!statusEffect.isActive()) statusEffect.clear();
        }

        switch (tipo) {
            case BASICO:
            case RAPIDO:
            case TANQUE:
            case MALDITO:
            case ESPECTRAL:
                velocity.set(playerPos).sub(position).nor().scl(speed);
                break;

            case GUARDIAN:
                velocity.set(playerPos).sub(position).nor().scl(speed);
                bossAttackTimer -= delta;
                if (bossAttackTimer <= 0) {
                    bossAttackTimer = com.milwar.kaosuarina.utils.Constants.GUARDIAN_ATTACK_INTERVAL;
                    shockwaveThisFrame = true;
                }
                break;

            case SHOOTER:
                float dist = position.dst(playerPos);
                if (dist < SHOOTER_HUIR_DIST) {
                    velocity.set(position).sub(playerPos).nor().scl(speed);
                } else if (dist > SHOOTER_ACERCAR_DIST) {
                    velocity.set(playerPos).sub(position).nor().scl(speed * 0.5f);
                } else {
                    tmp.set(playerPos).sub(position);
                    velocity.set(-tmp.y, tmp.x).nor().scl(speed);
                }
                shootTimer -= delta;
                if (shootTimer <= 0 && bulletPool != null) {
                    shootTimer = SHOOTER_COOLDOWN;
                    tmp.set(playerPos).sub(position).nor();
                    bulletPool.spawn(position.x, position.y, tmp.x, tmp.y);
                }
                break;

            case ARQUERO:
                float distAr = position.dst(playerPos);
                // Strafe to keep optimal range
                if (distAr < 350f) {
                    velocity.set(position).sub(playerPos).nor().scl(speed);
                } else if (distAr > 600f) {
                    velocity.set(playerPos).sub(position).nor().scl(speed * 0.6f);
                } else {
                    tmp.set(playerPos).sub(position);
                    velocity.set(-tmp.y, tmp.x).nor().scl(speed);
                }
                // Periodic teleport
                arqueroTeleportTimer -= delta;
                if (arqueroTeleportTimer <= 0) {
                    arqueroTeleportTimer = com.milwar.kaosuarina.utils.Constants.ARQUERO_TELEPORT_INTERVAL;
                    float tAngle = MathUtils.random(MathUtils.PI2);
                    position.set(playerPos.x + MathUtils.cos(tAngle) * 480f,
                                 playerPos.y + MathUtils.sin(tAngle) * 480f);
                    velocity.set(0, 0);
                }
                // Ranged attack
                shootTimer -= delta;
                if (shootTimer <= 0 && bulletPool != null) {
                    shootTimer = com.milwar.kaosuarina.utils.Constants.ARQUERO_SHOOT_INTERVAL;
                    tmp.set(playerPos).sub(position).nor();
                    bulletPool.spawnWithDamage(position.x, position.y, tmp.x, tmp.y,
                        com.milwar.kaosuarina.utils.Constants.ARQUERO_PROJECTILE_DMG);
                }
                break;

            case DEVASTADOR: {
                float devSpeed = (devastadorPhase == 1)
                    ? com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SPEED_P1
                    : com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SPEED_P2;
                velocity.set(playerPos).sub(position).nor().scl(devSpeed);
                // Shockwave timer
                float swInterval = (devastadorPhase == 1)
                    ? com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SHOCKWAVE_P1
                    : com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SHOCKWAVE_P2;
                bossAttackTimer -= delta;
                if (bossAttackTimer <= 0) {
                    bossAttackTimer = swInterval;
                    shockwaveThisFrame = true;
                }
                // Spiral burst
                float spiralInterval = (devastadorPhase == 1)
                    ? com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SPIRAL_P1
                    : com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SPIRAL_P2;
                devastadorSpiralTimer -= delta;
                if (devastadorSpiralTimer <= 0 && bulletPool != null) {
                    devastadorSpiralTimer = spiralInterval;
                    spiralThisFrame = true;
                    int n = com.milwar.kaosuarina.utils.Constants.DEVASTADOR_SPIRAL_BULLETS;
                    for (int i = 0; i < n; i++) {
                        float angle = MathUtils.PI2 * i / n;
                        bulletPool.spawnWithDamage(position.x, position.y,
                            MathUtils.cos(angle), MathUtils.sin(angle),
                            com.milwar.kaosuarina.utils.Constants.DEVASTADOR_PROJECTILE_DMG);
                    }
                }
                break;
            }
        }

        if (slowTimer > 0) {
            slowTimer -= delta;
            velocity.scl(slowMult);
        }
        position.add(velocity.x * delta, velocity.y * delta);
    }

    public void applyDepthScaling(DepthScalingData d) {
        if (d == null || d.hpMult <= 0) return;
        health    = Math.max(1, Math.round(health * d.hpMult));
        maxHealth = health;
        speed    *= d.speedMult;
    }

    public void recibirDanio(int damage) {
        if (damage <= 0) return;
        lastHpSnapshot = health;
        health -= damage;
        if (health <= 0) {
            if (tipo == Tipo.MALDITO) debeExplotar = true;
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;

        float pct = (float) health / maxHealth;
        renderColor.set(color);
        if (pct < 0.5f) renderColor.mul(0.7f + pct * 0.6f);

        // Tinte de efecto de estado (mezcla 50% con color base)
        if (statusEffect.isActive()) {
            if (statusEffect.tipo == StatusEffect.Tipo.BURN)
                renderColor.lerp(BURN_TINT, 0.5f);
            else if (statusEffect.tipo == StatusEffect.Tipo.POISON)
                renderColor.lerp(POISON_TINT, 0.5f);
        }

        oldColor.set(batch.getColor());
        batch.setColor(renderColor);
        if (tipo == Tipo.GUARDIAN) {
            batch.draw(SharedTextures.getGuardian(), position.x - size / 2, position.y - size / 2, size, size);
        } else if (tipo == Tipo.ARQUERO) {
            batch.draw(SharedTextures.getArquero(), position.x - size / 2, position.y - size / 2, size, size);
        } else if (tipo == Tipo.DEVASTADOR) {
            // Phase 2: overlay a brighter red tint
            if (devastadorPhase == 2) renderColor.set(1f, 0.15f, 0.1f, 1f);
            batch.setColor(renderColor);
            batch.draw(SharedTextures.getDevastador(), position.x - size / 2, position.y - size / 2, size, size);
        } else {
            batch.draw(SharedTextures.getEnemyWhite(), position.x - size / 2, position.y - size / 2, size, size);
        }
        batch.setColor(oldColor);
    }

    public float getRadio() {
        return size / 2;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void dispose() {
    }

    public enum Tipo {BASICO, RAPIDO, TANQUE, SHOOTER, MALDITO, ESPECTRAL, GUARDIAN, ARQUERO, DEVASTADOR}
}
