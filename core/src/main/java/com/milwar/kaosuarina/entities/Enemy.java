package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.data.model.DepthScalingData;
import com.milwar.kaosuarina.utils.EnemySprites;
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
    /**
     * Set true on kill — GameScreen processes loot drop and clears this flag.
     */
    public boolean pendingLootDrop = false;
    /**
     * JSON catalog ID — maps to enemy_id in enemies.json (S10-02).
     */
    public String enemyId = null;
    /**
     * Stun timer: while > 0 the enemy skips movement. Set by InscripcionSismica.
     */
    public float stunTimer = 0f;
    /**
     * Slow timer: while > 0 velocity is scaled by slowMult. Set by CAOS_PRIMORDIAL hits.
     */
    public float slowTimer = 0f;
    /**
     * Speed multiplier while slowed (0.5 = half speed).
     */
    public float slowMult = 1f;
    /**
     * HP snapshot taken before each hit — used by ColisionManager for overkill mana (S6-04).
     */
    public int lastHpSnapshot = 0;
    /**
     * Boss attack timer: counts down to next shockwave (GUARDIAN only).
     */
    public float bossAttackTimer = 0f;
    /**
     * Set true for one frame when the Guardian emits a shockwave; GameScreen resets it.
     */
    public boolean shockwaveThisFrame = false;
    /**
     * Countdown to next teleport (ARQUERO only).
     */
    public float arqueroTeleportTimer = 0f;
    /**
     * Current phase 1 or 2 (DEVASTADOR only).
     */
    public int devastadorPhase = 1;
    /**
     * Countdown to next spiral burst (DEVASTADOR only).
     */
    public float devastadorSpiralTimer = 0f;
    /**
     * True once phase-2 transition has fired (DEVASTADOR only).
     */
    public boolean phase2Triggered = false;
    /**
     * Set true for one frame when spiral fires; GameScreen resets it for visual.
     */
    public boolean spiralThisFrame = false;

    // ── Sprint 4 — New enemy fields ───────────────────────────────────────────
    /**
     * BERSERKER: true when HP < 20% and rage is active.
     */
    public boolean berserkActive = false;
    /**
     * SPLITTER: set true on kill — GameScreen spawns 2 copies then clears.
     */
    public boolean pendingSplit = false;
    /**
     * HEALER: heal tick timer. Counts down; on 0 sets healThisFrame=true.
     */
    public float healTickTimer = 0f;
    /**
     * HEALER: set true each second — GameScreen heals nearby allies.
     */
    public boolean healThisFrame = false;
    /**
     * ELITE_CHARGE: 0=orbit, 1=telegraph, 2=charge.
     */
    public int chargeState = 0;
    public float chargeTimer = 0f;
    public float chargeDirX = 0f, chargeDirY = 0f;
    /**
     * ELITE_SUMMON: counts down to next summon.
     */
    public float summonTimer = 0f;
    /**
     * ELITE_SUMMON: set true each cycle — GameScreen spawns 3 BASICO.
     */
    public boolean summonThisFrame = false;
    /**
     * ELITE_ZONE: zone is always at this enemy's position when active.
     */
    public boolean slowZoneActive = false;
    /**
     * FRAGMENTADO: current phase 1/2/3.
     */
    public int fragmentadoPhase = 1;
    public boolean fragmentadoP2Triggered = false;
    public boolean fragmentadoP3Triggered = false;
    public float defReductionTimer = 0f;
    public float defReductionPct = 0f;

    public final StatusEffect statusEffect = new StatusEffect();

    /** TANQUE stomp: set true each cycle — GameScreen applies AoE damage to player. */
    public boolean stompThisFrame = false;

    /** Animation time accumulator — drives frame selection in EnemySprites. */
    public float animTimer = 0f;
    /** Facing direction: 0=south, 1=east, 2=north, 3=west (matches EnemySprites.DIR_*). */
    public int facing = 0;

    private float speed;
    private float size;
    private float shootTimer;
    private float zigzagTimer = 0f;
    private float zigzagAngle = 0f;

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
        enemyId = null;
        pendingLootDrop = false;
        stunTimer = 0f;
        slowTimer = 0f;
        slowMult = 1f;
        defReductionTimer = 0f;
        defReductionPct = 0f;
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
        berserkActive = false;
        pendingSplit = false;
        healTickTimer = 1f;
        healThisFrame = false;
        chargeState = 0;
        chargeTimer = 0f;
        chargeDirX = chargeDirY = 0f;
        stompThisFrame = false;
        zigzagTimer = 0f;
        zigzagAngle = 0f;
        animTimer = 0f;
        facing = 0;
        summonTimer = com.milwar.kaosuarina.utils.Constants .ELITE_SUMMON_INTERVAL;
        summonThisFrame = false;
        slowZoneActive = false;
        fragmentadoPhase = 1;
        fragmentadoP2Triggered = false;
        fragmentadoP3Triggered = false;

        defensa = 0f;
        resistenciaMagica = 0f;

        switch (tipo) {
            case BASICO:
                enemyId = "EN_NORMAL";
                speed = 150f;
                size = 42f;
                health = 40;
                maxHealth = 40;
                color.set(0.2f, 0.9f, 0.3f, 1f);
                chargeTimer = MathUtils.random(com.milwar.kaosuarina.utils.Constants.BASICO_LUNGE_COOLDOWN_MIN,
                    com.milwar.kaosuarina.utils.Constants.BASICO_LUNGE_COOLDOWN_MAX);
                break;
            case RAPIDO:
                enemyId = "EN_KAMIKAZE";
                speed = 300f;
                size = 32f;
                health = 25;
                maxHealth = 25;
                color.set(0.3f, 0.6f, 1f, 1f);
                break;
            case TANQUE:
                enemyId = "EN_TANK";
                speed = 80f;
                size = 70f;
                health = 145;
                maxHealth = 145;
                defensa = 20f;
                color.set(0.9f, 0.2f, 0.2f, 1f);
                bossAttackTimer = com.milwar.kaosuarina.utils.Constants.TANQUE_STOMP_INTERVAL;
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
                bossAttackTimer = MathUtils.random(1.5f, com.milwar.kaosuarina.utils.Constants.MALDITO_NOVA_INTERVAL);
                break;
            case ESPECTRAL:
                enemyId = "EN_ESPECTRAL";
                speed = 160f;
                size = 28f;
                health = 35;
                maxHealth = 35;
                resistenciaMagica = 10f;
                color.set(0.5f, 0.9f, 1f, 0.45f); // cian semitransparente
                arqueroTeleportTimer = com.milwar.kaosuarina.utils.Constants.ESPECTRAL_TELEPORT_INTERVAL;
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
                color.set(0.55f, 0f, 0.15f, 1f);
                break;
            // ── Sprint 4 — Minions ────────────────────────────────────────────
            case BERSERKER:
                enemyId = "EN_BERSERKER";
                speed = 160f;
                size = 30f;
                health = maxHealth = 50;
                color.set(0.9f, 0.3f, 0.1f, 1f); // rojo-naranja
                break;
            case SPLITTER:
                enemyId = "EN_SPLITTER";
                speed = 120f;
                size = 34f;
                health = maxHealth = 70;
                color.set(0.7f, 0.9f, 0.1f, 1f); // amarillo-verde
                break;
            case HEALER:
                enemyId = "EN_HEALER";
                speed = 140f;
                size = 28f;
                health = maxHealth = 40;
                color.set(0.2f, 1f, 0.6f, 1f); // verde brillante
                break;
            case SHIELDER:
                enemyId = "EN_SHIELDER";
                speed = 60f;
                size = 44f;
                health = maxHealth = 120;
                defensa = 35f;
                color.set(0.5f, 0.5f, 0.8f, 1f); // azul-gris
                chargeTimer = com.milwar.kaosuarina.utils.Constants.SHIELDER_BASH_INTERVAL;
                break;
            // ── Sprint 4 — Elites ─────────────────────────────────────────────
            case ELITE_CHARGE:
                enemyId = "EN_ELITE_CHARGE";
                speed = 100f;
                size = 38f;
                health = maxHealth = 200;
                defensa = 10f;
                chargeTimer = 3f; // orbit 3s antes de telegrafiar
                color.set(1f, 0.6f, 0.05f, 1f); // dorado
                break;
            case ELITE_SUMMON:
                enemyId = "EN_ELITE_SUMMON";
                speed = 80f;
                size = 36f;
                health = maxHealth = 180;
                resistenciaMagica = 10f;
                color.set(0.6f, 0.2f, 0.9f, 1f); // morado
                break;
            case ELITE_ZONE:
                enemyId = "EN_ELITE_ZONE";
                speed = 0f;
                size = 42f;
                health = maxHealth = 240;
                defensa = 20f;
                slowZoneActive = true;
                color.set(0.2f, 0.7f, 0.4f, 1f); // verde oscuro
                break;
            // ── Sprint 4 — Boss El Fragmentado ────────────────────────────────
            case FRAGMENTADO:
                enemyId = "EN_BOSS_PHASE";
                size = 70f;
                health = maxHealth = com.milwar.kaosuarina.utils.Constants.FRAGMENTADO_HP;
                defensa = 20f;
                resistenciaMagica = 20f;
                bossAttackTimer = com.milwar.kaosuarina.utils.Constants.FRAGMENTADO_ATTACK_INT;
                color.set(0.8f, 0.1f, 0.6f, 1f); // magenta
                break;
        }
    }

    public void update(float delta, Vector2 playerPos, EnemyBulletPool bulletPool) {
        if (!active) return;
        if (stunTimer > 0) {
            stunTimer -= delta;
            return;
        }
        if (defReductionTimer > 0) defReductionTimer -= delta;

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
            case BASICO: {
                // Periodic lunge: chargeState 0=approach, 1=lunging
                if (chargeState == 1) {
                    velocity.set(chargeDirX, chargeDirY).scl(speed * com.milwar.kaosuarina.utils.Constants.BASICO_LUNGE_SPEED_MULT);
                    chargeTimer -= delta;
                    if (chargeTimer <= 0) {
                        chargeState = 0;
                        chargeTimer = MathUtils.random(
                            com.milwar.kaosuarina.utils.Constants.BASICO_LUNGE_COOLDOWN_MIN,
                            com.milwar.kaosuarina.utils.Constants.BASICO_LUNGE_COOLDOWN_MAX);
                    }
                } else {
                    velocity.set(playerPos).sub(position).nor().scl(speed);
                    chargeTimer -= delta;
                    if (chargeTimer <= 0 && position.dst(playerPos) < com.milwar.kaosuarina.utils.Constants.BASICO_LUNGE_RANGE) {
                        chargeState = 1;
                        chargeTimer = com.milwar.kaosuarina.utils.Constants.BASICO_LUNGE_DURATION;
                        tmp.set(playerPos).sub(position).nor();
                        chargeDirX = tmp.x;
                        chargeDirY = tmp.y;
                    }
                }
                break;
            }

            case RAPIDO: {
                // Erratic zigzag approach — direction offset refreshed every 0.22s
                zigzagTimer -= delta;
                if (zigzagTimer <= 0) {
                    zigzagTimer = 0.22f;
                    zigzagAngle = MathUtils.random(-40f * MathUtils.degreesToRadians,
                        40f * MathUtils.degreesToRadians);
                }
                tmp.set(playerPos).sub(position).nor();
                float cos = MathUtils.cos(zigzagAngle);
                float sin = MathUtils.sin(zigzagAngle);
                velocity.set(cos * tmp.x - sin * tmp.y, sin * tmp.x + cos * tmp.y).scl(speed);
                break;
            }

            case TANQUE: {
                velocity.set(playerPos).sub(position).nor().scl(speed);
                bossAttackTimer -= delta;
                if (bossAttackTimer <= 0 &&
                    position.dst(playerPos) < com.milwar.kaosuarina.utils.Constants.TANQUE_STOMP_RADIUS * 1.15f) {
                    bossAttackTimer = com.milwar.kaosuarina.utils.Constants.TANQUE_STOMP_INTERVAL;
                    stompThisFrame = true;
                }
                break;
            }

            case MALDITO: {
                velocity.set(playerPos).sub(position).nor().scl(speed);
                // Periodic poison nova at close range
                bossAttackTimer -= delta;
                if (bossAttackTimer <= 0) {
                    bossAttackTimer = com.milwar.kaosuarina.utils.Constants.MALDITO_NOVA_INTERVAL;
                    if (bulletPool != null &&
                        position.dst(playerPos) < com.milwar.kaosuarina.utils.Constants.MALDITO_NOVA_RANGE) {
                        for (int i = 0; i < 6; i++) {
                            float a = MathUtils.PI2 * i / 6;
                            bulletPool.spawnWithDamage(position.x, position.y,
                                MathUtils.cos(a), MathUtils.sin(a),
                                com.milwar.kaosuarina.utils.Constants.MALDITO_NOVA_DMG);
                        }
                    }
                }
                break;
            }

            case ESPECTRAL: {
                velocity.set(playerPos).sub(position).nor().scl(speed);
                // Periodic flank teleport when player is too close
                arqueroTeleportTimer -= delta;
                if (arqueroTeleportTimer <= 0) {
                    arqueroTeleportTimer = com.milwar.kaosuarina.utils.Constants.ESPECTRAL_TELEPORT_INTERVAL;
                    if (position.dst(playerPos) < com.milwar.kaosuarina.utils.Constants.ESPECTRAL_TELEPORT_TRIGGER_RANGE) {
                        float tAngle = MathUtils.random(MathUtils.PI2);
                        float td = com.milwar.kaosuarina.utils.Constants.ESPECTRAL_TELEPORT_TARGET_DIST;
                        position.set(playerPos.x + MathUtils.cos(tAngle) * td,
                            playerPos.y + MathUtils.sin(tAngle) * td);
                    }
                }
                break;
            }

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

            // ── Sprint 4 — Minions ────────────────────────────────────────────
            case BERSERKER: {
                float bSpeed = berserkActive ? com.milwar.kaosuarina.utils.Constants.BERSERKER_SPEED_RAGE : speed;
                velocity.set(playerPos).sub(position).nor().scl(bSpeed);
                if (!berserkActive && health <= maxHealth * 0.20f) {
                    berserkActive = true;
                }
                break;
            }
            case SPLITTER:
                velocity.set(playerPos).sub(position).nor().scl(speed);
                break;
            case HEALER: {
                float dH = position.dst(playerPos);
                if (dH < 400f) velocity.set(position).sub(playerPos).nor().scl(speed);
                else velocity.set(0, 0);
                healTickTimer -= delta;
                if (healTickTimer <= 0) {
                    healTickTimer = 1f;
                    healThisFrame = true;
                }
                break;
            }
            case SHIELDER: {
                // Periodic shield bash: chargeState 0=approach, 1=bashing
                if (chargeState == 1) {
                    velocity.set(chargeDirX, chargeDirY).scl(com.milwar.kaosuarina.utils.Constants.SHIELDER_BASH_SPEED);
                    chargeTimer -= delta;
                    if (chargeTimer <= 0) {
                        chargeState = 0;
                        chargeTimer = com.milwar.kaosuarina.utils.Constants.SHIELDER_BASH_INTERVAL;
                    }
                } else {
                    velocity.set(playerPos).sub(position).nor().scl(speed);
                    chargeTimer -= delta;
                    if (chargeTimer <= 0 &&
                        position.dst(playerPos) < com.milwar.kaosuarina.utils.Constants.SHIELDER_BASH_RANGE) {
                        chargeState = 1;
                        chargeTimer = com.milwar.kaosuarina.utils.Constants.SHIELDER_BASH_DURATION;
                        tmp.set(playerPos).sub(position).nor();
                        chargeDirX = tmp.x;
                        chargeDirY = tmp.y;
                    }
                }
                break;
            }
            // ── Sprint 4 — Elites ─────────────────────────────────────────────
            case ELITE_CHARGE: {
                if (chargeState == 0) {
                    // Orbit
                    tmp.set(playerPos).sub(position);
                    velocity.set(-tmp.y, tmp.x).nor().scl(speed);
                    chargeTimer -= delta;
                    if (chargeTimer <= 0) {
                        chargeState = 1;
                        chargeTimer = com.milwar.kaosuarina.utils.Constants.ELITE_CHARGE_TELEGRAPH;
                        velocity.set(0, 0);
                        // Lock charge direction toward player
                        tmp.set(playerPos).sub(position).nor();
                        chargeDirX = tmp.x;
                        chargeDirY = tmp.y;
                    }
                } else if (chargeState == 1) {
                    // Telegraph (stay still)
                    chargeTimer -= delta;
                    velocity.set(0, 0);
                    if (chargeTimer <= 0) {
                        chargeState = 2;
                        chargeTimer = 0.5f;
                    }
                } else {
                    // Charge
                    velocity.set(chargeDirX, chargeDirY).scl(com.milwar.kaosuarina.utils.Constants.ELITE_CHARGE_SPEED);
                    chargeTimer -= delta;
                    if (chargeTimer <= 0) {
                        chargeState = 0;
                        chargeTimer = 2.5f;
                    }
                }
                break;
            }
            case ELITE_SUMMON:
                velocity.set(position).sub(playerPos).nor().scl(speed); // flee
                summonTimer -= delta;
                if (summonTimer <= 0) {
                    summonTimer = com.milwar.kaosuarina.utils.Constants.ELITE_SUMMON_INTERVAL;
                    summonThisFrame = true;
                }
                break;
            case ELITE_ZONE:
                velocity.set(0, 0); // stationary
                break;
            // ── Sprint 4 — Boss El Fragmentado ────────────────────────────────
            case FRAGMENTADO: {
                // Phase transitions
                float hpPct = (float) health / maxHealth;
                if (!fragmentadoP2Triggered && hpPct <= com.milwar.kaosuarina.utils.Constants.FRAGMENTADO_PHASE2_THR) {
                    fragmentadoPhase = 2;
                    fragmentadoP2Triggered = true;
                    defensa = 10f;
                    resistenciaMagica = 30f;
                }
                if (!fragmentadoP3Triggered && hpPct <= com.milwar.kaosuarina.utils.Constants.FRAGMENTADO_PHASE3_THR) {
                    fragmentadoPhase = 3;
                    fragmentadoP3Triggered = true;
                    defensa = 5f;
                    resistenciaMagica = 5f;
                }
                float fSpeed;
                switch (fragmentadoPhase) {
                    case 2:
                        fSpeed = com.milwar.kaosuarina.utils.Constants.FRAGMENTADO_SPEED_P2;
                        break;
                    case 3:
                        fSpeed = com.milwar.kaosuarina.utils.Constants.FRAGMENTADO_SPEED_P3;
                        break;
                    default:
                        fSpeed = com.milwar.kaosuarina.utils.Constants.FRAGMENTADO_SPEED_P1;
                        break;
                }
                velocity.set(playerPos).sub(position).nor().scl(fSpeed);
                bossAttackTimer -= delta;
                if (bossAttackTimer <= 0) {
                    bossAttackTimer = com.milwar.kaosuarina.utils.Constants.FRAGMENTADO_ATTACK_INT;
                    shockwaveThisFrame = true;
                }
                // Phase 3: also fires spirals
                if (fragmentadoPhase == 3 && bulletPool != null) {
                    devastadorSpiralTimer -= delta;
                    if (devastadorSpiralTimer <= 0) {
                        devastadorSpiralTimer = 4f;
                        spiralThisFrame = true;
                        for (int i = 0; i < 6; i++) {
                            float angle = MathUtils.PI2 * i / 6;
                            bulletPool.spawnWithDamage(position.x, position.y,
                                MathUtils.cos(angle), MathUtils.sin(angle), 18);
                        }
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

        // Update facing direction from velocity for sprite animation
        if (velocity.len2() > 1f) {
            float ax = Math.abs(velocity.x), ay = Math.abs(velocity.y);
            if (ax > ay) facing = (velocity.x > 0) ? EnemySprites.DIR_EAST : EnemySprites.DIR_WEST;
            else         facing = (velocity.y > 0) ? EnemySprites.DIR_NORTH : EnemySprites.DIR_SOUTH;
        }
        animTimer += delta;
    }

    public void applyDepthScaling(DepthScalingData d) {
        if (d == null || d.hpMult <= 0) return;
        health = Math.max(1, Math.round(health * d.hpMult));
        maxHealth = health;
        speed *= d.speedMult;
    }

    public void takeDamage(int damage) {
        if (damage <= 0) return;
        lastHpSnapshot = health;
        health -= damage;
        if (health <= 0) {
            if (tipo == Tipo.MALDITO) debeExplotar = true;
            if (tipo == Tipo.SPLITTER) pendingSplit = true;
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
            com.badlogic.gdx.graphics.Texture sprite = EnemySprites.getFrame(tipo, facing, animTimer);
            if (sprite != null) {
                // Sprites show original pixel colors — white base, light status tint only
                renderColor.set(1f, 1f, 1f, 1f);
                if (statusEffect.isActive()) {
                    if (statusEffect.tipo == StatusEffect.Tipo.BURN)
                        renderColor.lerp(BURN_TINT, 0.25f);
                    else if (statusEffect.tipo == StatusEffect.Tipo.POISON)
                        renderColor.lerp(POISON_TINT, 0.25f);
                }
                batch.setColor(renderColor);
                float sw = size * 1.4f;
                batch.draw(sprite, position.x - sw / 2, position.y - sw / 2, sw, sw);
            } else {
                batch.draw(SharedTextures.getEnemyWhite(), position.x - size / 2, position.y - size / 2, size, size);
            }
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

    public enum Tipo {
        BASICO, RAPIDO, TANQUE, SHOOTER, MALDITO, ESPECTRAL, GUARDIAN, ARQUERO, DEVASTADOR,
        // Sprint 4 — Minions
        BERSERKER, SPLITTER, HEALER, SHIELDER,
        // Sprint 4 — Elites
        ELITE_CHARGE, ELITE_SUMMON, ELITE_ZONE,
        // Sprint 4 — Boss
        FRAGMENTADO
    }
}
