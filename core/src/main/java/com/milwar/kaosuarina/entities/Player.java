package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.systems.PlayerStats;
import com.milwar.kaosuarina.systems.UpgradeManager;
import com.milwar.kaosuarina.reliquias.Reliquia;
import com.milwar.kaosuarina.roles.Role;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.milwar.kaosuarina.utils.AnimationSheets;
import com.milwar.kaosuarina.utils.ColisionManager;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.milwar.kaosuarina.utils.SpriteSheets;

public class Player {
    private static final float INVULNERABILITY_TIME = 1f;
    private final Role role;
    private final PlayerStats stats;
    private final Reliquia reliquia;
    private final Color roleAccent;
    private final Vector2 arenaDir = new Vector2();
    private int lastDir = SpriteSheets.DIR_SOUTH;
    public Vector2 position;
    public Vector2 velocity;
    private float shootTimer;
    private float invulnerabilityTimer;

    private int maxHealth;
    private int currentHealth;
    private boolean alive;

    // Animation state
    private AnimationSheets.Anim animState = AnimationSheets.Anim.WALK;
    private int animFrame = 0;
    private float animTimer = 0;
    private static final float WALK_FRAME_DUR = 1f / 8f;
    private static final float ATTACK_FRAME_DUR = 1f / 12f;

    // Manual attack cooldowns (Caballero / Mago)
    private float lightCooldownTimer = 0;
    private float heavyCooldownTimer = 0;

    // Attack visual flash (arc/blast drawn by ShapeRenderer)
    private float attackVisualTimer = 0;
    private float attackVisualAngle = 0;
    private boolean attackVisualHeavy = false;
    private static final float VISUAL_DUR_LIGHT = 0.15f;
    private static final float VISUAL_DUR_HEAVY = 0.25f;

    // Veneno de contacto (aplicado por MALDITO)
    private float poisonTimer = 0f;
    private float poisonTickTimer = 0f;
    private int poisonDps = 0;
    private static final float POISON_TICK_INTERVAL = 1f;
    private static final Color POISON_PLAYER_TINT = new Color(0.5f, 1f, 0.5f, 1f);

    private UpgradeManager upgradeManager;

    public Player(float x, float y, Role role) {
        this.role = role;
        this.stats = role.stats;
        this.reliquia = role.reliquia;
        switch (role.tipo) {
            case CABALLERO:
                roleAccent = new Color(0f, 0.898f, 0.8f, 1f);
                break;
            case MAGO:
                roleAccent = new Color(0.608f, 0.188f, 1f, 1f);
                break;
            case SHOOTER:
                roleAccent = new Color(1f, 0.722f, 0f, 1f);
                break;
            default:
                roleAccent = Color.WHITE.cpy();
                break;
        }
        position = new Vector2(x, y);
        velocity = new Vector2();
        shootTimer = 0;
        invulnerabilityTimer = 0;
        maxHealth = stats.maxHealth;
        currentHealth = maxHealth;
        stats.mana = stats.maxMana;
        alive = true;
    }

    public void setUpgradeManager(UpgradeManager manager) {
        this.upgradeManager = manager;
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public void aumentarVidaMaxima(int bonus) {
        maxHealth += bonus;
        currentHealth = Math.min(currentHealth + bonus, maxHealth);
    }

    public void update(float delta, PoolBalas poolBalas, float aimAngle) {
        if (!alive) return;

        if (invulnerabilityTimer > 0) invulnerabilityTimer -= delta;

        // Regen pasiva de maná (exclusiva del Mago; los demás tienen manaRegen=0)
        if (stats.manaRegen > 0) stats.añadirMana(stats.manaRegen * delta);

        reliquia.onUpdate(this, delta);

        if (position.len() > Constants.ARENA_RADIUS) {
            arenaDir.set(position).nor().scl(-stats.baseSpeed * 2f);
            velocity.add(arenaDir);
        }

        position.add(velocity.x * delta, velocity.y * delta);

        updateAnimation(delta);

        // Veneno de contacto (MALDITO) — no respeta invulnerabilidad
        if (poisonTimer > 0) {
            poisonTimer -= delta;
            poisonTickTimer -= delta;
            if (poisonTickTimer <= 0) {
                poisonTickTimer += POISON_TICK_INTERVAL;
                currentHealth -= poisonDps;
                if (currentHealth <= 0) {
                    currentHealth = 0;
                    alive = false;
                }
            }
        }

        // Cooldowns de ataque manual (no afectan al Tirador)
        if (lightCooldownTimer > 0) lightCooldownTimer -= delta;
        if (heavyCooldownTimer > 0) heavyCooldownTimer -= delta;
        if (attackVisualTimer > 0) attackVisualTimer -= delta;

        // Auto-shoot exclusivo del Tirador
        if (role.attackMode == Role.AttackMode.AUTO_SHOOT) {
            float cooldown = stats.baseShootCooldown * reliquia.getCooldownMultiplier();
            if (upgradeManager != null) cooldown /= upgradeManager.getMultiplicadorCadencia();
            shootTimer -= delta;
            if (shootTimer <= 0) {
                disparar(poolBalas, aimAngle);
                shootTimer = cooldown;
            }
        }
    }

    public void recibirDanio(int damage) {
        if (!alive || invulnerabilityTimer > 0) return;

        // El core reduce el daño (p.ej. CaballeroCore con stacks de armadura)
        float reduction = reliquia.getDamageReduction();
        int actualDamage = Math.max(1, Math.round(damage * (1f - reduction)));

        // El core actualiza su estado interno (consume/gana stacks, etc.)
        reliquia.onDamageReceived(this, damage);

        currentHealth -= actualDamage;
        invulnerabilityTimer = INVULNERABILITY_TIME;

        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }

    public void onKill() {
        reliquia.onKill(this);
    }

    /**
     * Cura al jugador. No supera maxHealth.
     */
    public void curar(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    /**
     * Aplica veneno de contacto (de MALDITO). Extiende duración si ya está envenenado.
     */
    public void aplicarVeneno(float duration, int dps) {
        if (duration > poisonTimer) poisonTimer = duration;
        poisonDps = dps;
        if (poisonTickTimer <= 0) poisonTickTimer = POISON_TICK_INTERVAL;
    }

    // ── Ataque manual (Caballero / Mago) ─────────────────────────────────────

    /**
     * Dispara ataque ligero. Devuelve kills para que GameScreen actualice HUD.
     */
    public int triggerLightAttack(PoolBalas poolBalas, float aimAngle, PoolEnemigos poolEnemigos) {
        if (!alive || lightCooldownTimer > 0) return 0;
        int kills = 0;
        switch (role.tipo) {
            case CABALLERO:
                kills = ColisionManager.comprobarMelee(this, position, aimAngle,
                    MathUtils.degreesToRadians * Constants.MELEE_LIGHT_ARC_DEG,
                    Constants.MELEE_LIGHT_RADIUS, stats.meleeLightDamage, DamageType.FISICO, poolEnemigos);
                break;
            case MAGO:
                if (stats.consumirMana(stats.magicLightManaCost))
                    lanzarBoltMago(poolBalas, aimAngle, poolEnemigos, false);
                break;
            default:
                break;
        }
        activarVisualAtaque(aimAngle, false);
        lightCooldownTimer = stats.lightAttackCooldown;
        return kills;
    }

    /**
     * Dispara ataque pesado. Devuelve kills.
     */
    public int triggerHeavyAttack(PoolBalas poolBalas, float aimAngle, PoolEnemigos poolEnemigos) {
        if (!alive || heavyCooldownTimer > 0) return 0;
        int kills = 0;
        switch (role.tipo) {
            case CABALLERO:
                kills = ColisionManager.comprobarMelee(this, position, aimAngle,
                    MathUtils.degreesToRadians * Constants.MELEE_HEAVY_ARC_DEG,
                    Constants.MELEE_HEAVY_RADIUS, stats.meleeHeavyDamage, DamageType.FISICO, poolEnemigos);
                break;
            case MAGO:
                if (stats.consumirMana(stats.magicHeavyManaCost))
                    kills = ColisionManager.comprobarBlast(this, position, Constants.MAGIC_BLAST_RADIUS,
                        stats.magicHeavyDamage, DamageType.MAGICO, poolEnemigos);
                break;
            default:
                break;
        }
        activarVisualAtaque(aimAngle, true);
        heavyCooldownTimer = stats.heavyAttackCooldown;
        return kills;
    }

    private void lanzarBoltMago(PoolBalas poolBalas, float aimAngle, PoolEnemigos poolEnemigos, boolean heavy) {
        // Auto-aim: si hay enemigo en rango, apunta a él; si no, usa el ratón
        Enemy nearest = ColisionManager.nearestEnemy(poolEnemigos, position, Constants.MAGIC_LIGHT_RANGE);
        float shootAngle = (nearest != null)
            ? (float) Math.atan2(nearest.position.y - position.y, nearest.position.x - position.x)
            : aimAngle;
        int dmg = heavy ? stats.magicHeavyDamage : stats.magicLightDamage;
        poolBalas.spawn(position.x, position.y,
            MathUtils.cos(shootAngle), MathUtils.sin(shootAngle),
            dmg, 0, 0, DamageType.MAGICO);
    }

    private void activarVisualAtaque(float aimAngle, boolean heavy) {
        attackVisualAngle = aimAngle;
        attackVisualHeavy = heavy;
        attackVisualTimer = heavy ? VISUAL_DUR_HEAVY : VISUAL_DUR_LIGHT;
        animState = AnimationSheets.Anim.ATTACK;
        animFrame = 0;
        animTimer = ATTACK_FRAME_DUR;
    }

    /**
     * Dibuja el arco de espada o el círculo mágico en el ShapeRenderer (ya abierto en Filled).
     */
    public void renderAttackEffect(ShapeRenderer sr) {
        if (attackVisualTimer <= 0 || !alive) return;
        float alpha = attackVisualTimer / (attackVisualHeavy ? VISUAL_DUR_HEAVY : VISUAL_DUR_LIGHT);

        switch (role.tipo) {
            case CABALLERO: {
                float arcDeg = attackVisualHeavy ? Constants.MELEE_HEAVY_ARC_DEG : Constants.MELEE_LIGHT_ARC_DEG;
                float radius = attackVisualHeavy ? Constants.MELEE_HEAVY_RADIUS : Constants.MELEE_LIGHT_RADIUS;
                float startDeg = attackVisualAngle * MathUtils.radiansToDegrees - arcDeg * 0.5f;
                if (attackVisualHeavy) sr.setColor(0.4f, 0.85f, 1f, 0.55f * alpha);
                else sr.setColor(1f, 0.9f, 0.3f, 0.55f * alpha);
                sr.arc(position.x, position.y, radius, startDeg, arcDeg, 48);
                break;
            }
            case MAGO: {
                if (attackVisualHeavy) {
                    float progress = 1f - alpha;
                    float r = Constants.MAGIC_BLAST_RADIUS * (0.3f + 0.7f * progress);
                    sr.setColor(0.6f, 0f, 1f, 0.5f * alpha);
                    sr.circle(position.x, position.y, r, 48);
                }
                // El bolt ligero se ve como proyectil (Bala existente)
                break;
            }
            default:
                break;
        }
    }

    private void updateAnimation(float delta) {
        float frameDur = (animState == AnimationSheets.Anim.ATTACK) ? ATTACK_FRAME_DUR : WALK_FRAME_DUR;
        animTimer -= delta;
        if (animTimer <= 0) {
            animTimer += frameDur;
            animFrame++;
            int total = AnimationSheets.frameCount(role.tipo, animState);
            if (animFrame >= total) {
                // ATTACK plays once; WALK loops
                if (animState == AnimationSheets.Anim.ATTACK) {
                    animState = AnimationSheets.Anim.WALK;
                }
                animFrame = 0;
            }
        }
        // Hold walk at frame 0 while idle
        if (animState == AnimationSheets.Anim.WALK && velocity.len2() == 0) {
            animFrame = 0;
            animTimer = WALK_FRAME_DUR;
        }
    }

    private void disparar(PoolBalas poolBalas, float aimAngle) {
        // Solo inicia la animación si no está ya reproduciéndose (evita reset en cadencia rápida)
        if (animState != AnimationSheets.Anim.ATTACK
            && AnimationSheets.frameCount(role.tipo, AnimationSheets.Anim.ATTACK) > 1) {
            animState = AnimationSheets.Anim.ATTACK;
            animFrame = 0;
            animTimer = ATTACK_FRAME_DUR;
        }

        int totalBalas = stats.baseBulletCount + (upgradeManager != null ? upgradeManager.getBalasExtra() : 0);
        float inicio = -(totalBalas - 1) * stats.bulletSpread / 2f;

        float damageMultiplier = upgradeManager != null ? upgradeManager.getMultiplicadorDanio() : 1f;
        int damage = Math.max(1, Math.round(stats.baseDamage * damageMultiplier));
        int pierce = upgradeManager != null ? upgradeManager.getNivelPerforation() : 0;
        int rebotes = reliquia.getBounces();
        DamageType tipo = tipoDanio();

        for (int i = 0; i < totalBalas; i++) {
            float offset = inicio + (i * stats.bulletSpread);
            float angulo = aimAngle + offset * MathUtils.degreesToRadians;
            poolBalas.spawn(position.x, position.y, MathUtils.cos(angulo), MathUtils.sin(angulo), damage, pierce, rebotes, tipo);
        }
    }

    private DamageType tipoDanio() {
        switch (role.tipo) {
            case MAGO:
                return DamageType.MAGICO;
            case SHOOTER:
                return DamageType.A_DISTANCIA;
            default:
                return DamageType.FISICO;
        }
    }

    public void render(SpriteBatch batch) {
        if (!alive) return;
        if (invulnerabilityTimer > 0 && ((int) (invulnerabilityTimer * 10) % 2 == 0)) return;

        // Dirección por velocidad dominante; mantiene última si parado
        float vx = velocity.x, vy = velocity.y;
        if (Math.abs(vx) >= Math.abs(vy) && (vx != 0 || vy != 0)) {
            lastDir = vx > 0 ? SpriteSheets.DIR_EAST : SpriteSheets.DIR_WEST;
        } else if (vy != 0) {
            lastDir = vy > 0 ? SpriteSheets.DIR_NORTH : SpriteSheets.DIR_SOUTH;
        }

        com.badlogic.gdx.graphics.Texture sprite = AnimationSheets.getFrame(role.tipo, animState, lastDir, animFrame);
        if (sprite == null) sprite = SpriteSheets.getSprite(role.tipo, lastDir);
        if (sprite != null) {
            if (poisonTimer > 0) {
                Color prev = batch.getColor().cpy();
                batch.setColor(POISON_PLAYER_TINT);
                batch.draw(sprite, position.x - 46, position.y - 46, 92, 92);
                batch.setColor(prev);
            } else {
                batch.draw(sprite, position.x - 46, position.y - 46, 92, 92);
            }
        } else {
            Color prev = batch.getColor().cpy();
            batch.setColor(roleAccent);
            batch.draw(SharedTextures.getPlayer(), position.x - 32, position.y - 32, 64, 64);
            batch.setColor(prev);
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return alive;
    }

    public float getRadio() {
        return 32f;
    }

    public Role getRole() {
        return role;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public float getVelocidadActual() {
        return upgradeManager != null
            ? stats.baseSpeed * upgradeManager.getMultiplicadorVelocidad()
            : stats.baseSpeed;
    }

    public void dispose() {
    }
}
