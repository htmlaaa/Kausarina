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
import com.milwar.kaosuarina.weapons.Weapon;
import com.milwar.kaosuarina.weapons.WeaponType;

public class Player {
    private final float invulnerabilityTime;
    private final Role role;
    private final PlayerStats stats;
    private final Reliquia reliquia;
    private final Color roleAccent;
    private final Vector2 arenaDir = new Vector2();
    private int lastDir = SpriteSheets.DIR_SOUTH;
    public Vector2 position;
    public Vector2 velocity;
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

    // Cicatriz de Combate (S6-04): maná al expirar el i-frame (solo Caballero)
    private int lastDamageReceived = 0;

    // Shooter auto-fire (sin slot de arma, basado en rango)
    private float shooterCooldown = 0f;

    // Veneno de contacto (aplicado por MALDITO)
    private float poisonTimer = 0f;
    private float poisonTickTimer = 0f;
    private int poisonDps = 0;
    private static final float POISON_TICK_INTERVAL = 1f;
    private static final Color POISON_PLAYER_TINT = new Color(0.5f, 1f, 0.5f, 1f);

    // ── Weapon slots (S5-02, ADR-006) ────────────────────────────────────────
    private final Weapon[] equippedWeapons = new Weapon[Constants.WEAPON_SLOTS];
    private final Weapon[] storageWeapons  = new Weapon[4];

    private UpgradeManager upgradeManager;

    public Player(float x, float y, Role role) {
        this.role = role;
        this.stats = role.stats;
        this.reliquia = role.reliquia;
        // Snapshot immutable base stats so recalcStats() can always derive from zero
        stats.hpBase      = stats.maxHealth;
        stats.defBase     = stats.defensa;
        stats.resMagBase  = stats.resistenciaMagica;
        stats.manaMaxBase = stats.maxMana;
        switch (role.tipo) {
            case CABALLERO: roleAccent = new Color(0f, 0.898f, 0.8f, 1f); break;
            case MAGO:      roleAccent = new Color(0.608f, 0.188f, 1f, 1f); break;
            case SHOOTER:   roleAccent = new Color(1f, 0.722f, 0f, 1f); break;
            default:        roleAccent = Color.WHITE.cpy(); break;
        }
        switch (role.tipo) {
            case CABALLERO: invulnerabilityTime = Constants.INVULNERABILITY_CABALLERO; break;
            case SHOOTER:   invulnerabilityTime = Constants.INVULNERABILITY_SHOOTER;   break;
            default:        invulnerabilityTime = Constants.INVULNERABILITY_MAGO;      break;
        }
        position = new Vector2(x, y);
        velocity = new Vector2();
        invulnerabilityTimer = 0;
        maxHealth = stats.maxHealth;
        currentHealth = maxHealth;
        stats.mana = stats.maxMana;
        alive = true;
    }

    public void setUpgradeManager(UpgradeManager manager) {
        this.upgradeManager = manager;
    }

    /** Returns the inscription on the weapon matching type t, or null. */
    public com.milwar.kaosuarina.weapons.Inscription getInscriptionForWeaponType(
            com.milwar.kaosuarina.weapons.WeaponType t) {
        if (t == null) return null;
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            Weapon w = equippedWeapons[i];
            if (w != null && w.type == t && w.inscription != null) return w.inscription;
        }
        return null;
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

        boolean wasInvul = invulnerabilityTimer > 0;
        if (invulnerabilityTimer > 0) invulnerabilityTimer -= delta;
        if (wasInvul && invulnerabilityTimer <= 0 && role.tipo == Role.Tipo.CABALLERO && lastDamageReceived > 0) {
            stats.addMana(lastDamageReceived / Constants.CICATRIZ_DIVISOR);
            lastDamageReceived = 0;
        }

        // Regen pasiva de maná (Mago base + bonus de afijo de arma)
        float totalMpRegen = stats.manaRegen + stats.weaponAffixMpRegen;
        if (totalMpRegen > 0) stats.addMana(totalMpRegen * delta);

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

    }

    public void recibirDanio(int damage) {
        if (!alive || invulnerabilityTimer > 0) return;

        // El core reduce el daño (p.ej. CaballeroCore con stacks de armadura)
        float reduction = reliquia.getDamageReduction();
        int actualDamage = Math.max(1, Math.round(damage * (1f - reduction)));

        // El core actualiza su estado interno (consume/gana stacks, etc.)
        reliquia.onDamageReceived(this, damage);

        currentHealth -= actualDamage;
        lastDamageReceived = actualDamage;
        invulnerabilityTimer = invulnerabilityTime;

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
                if (stats.consumirMana(stats.magicHeavyManaCost)) {
                    float blastR = Constants.MAGIC_BLAST_RADIUS * stats.blastRadiusMult;
                    kills = ColisionManager.comprobarBlast(this, position, blastR,
                        stats.magicHeavyDamage, DamageType.MAGICO, poolEnemigos);
                }
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

    /**
     * Shooter auto-fire: autoaim al enemigo más cercano dentro de SHOOTER_AUTO_RANGE.
     * Si el ratón se ha movido recientemente (mouseActive=true), apunta al cursor en su lugar.
     */
    public void updateShooterAutoFire(float delta, PoolBalas poolBalas, PoolEnemigos poolEnemigos,
                                       float mouseAngle, boolean mouseActive) {
        shooterCooldown -= delta;
        if (shooterCooldown > 0) return;

        float angle;
        if (mouseActive) {
            angle = mouseAngle;
        } else {
            Enemy nearest = ColisionManager.nearestEnemy(poolEnemigos, position, Constants.SHOOTER_AUTO_RANGE);
            if (nearest == null) return;
            angle = MathUtils.atan2(nearest.position.y - position.y, nearest.position.x - position.x);
        }

        float mult   = (upgradeManager != null) ? upgradeManager.getMultiplicadorDanio() : 1f;
        int   dmg    = Math.max(1, Math.round(Constants.SHOOTER_AUTO_DAMAGE * mult));
        int   pierce = (upgradeManager != null) ? upgradeManager.getNivelPerforation() : 0;
        int   extra  = (upgradeManager != null) ? upgradeManager.getBalasExtra() : 0;
        int   total  = 2 + extra;
        float spread = 6f * MathUtils.degreesToRadians;

        for (int b = 0; b < total; b++) {
            float t = (total > 1) ? (b / (float)(total - 1) - 0.5f) * 2f : 0f;
            float a = angle + t * spread;
            poolBalas.spawnReturning(position.x, position.y,
                MathUtils.cos(a), MathUtils.sin(a),
                dmg, pierce, 0, DamageType.A_DISTANCIA, 900f, stats.rango, WeaponType.PISTOLAS_GEMELAS);
        }
        shooterCooldown = stats.baseShootCooldown;
        activarVisualAtaque(angle, false);
    }

    private void activarVisualAtaque(float aimAngle, boolean heavy) {
        attackVisualAngle = aimAngle;
        attackVisualHeavy = heavy;
        attackVisualTimer = heavy ? VISUAL_DUR_HEAVY : VISUAL_DUR_LIGHT;
        animState = heavy ? AnimationSheets.Anim.HEAVY_ATTACK : AnimationSheets.Anim.ATTACK;
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
        boolean isAttacking = animState == AnimationSheets.Anim.ATTACK || animState == AnimationSheets.Anim.HEAVY_ATTACK;
        float frameDur = isAttacking ? ATTACK_FRAME_DUR : WALK_FRAME_DUR;
        animTimer -= delta;
        if (animTimer <= 0) {
            animTimer += frameDur;
            animFrame++;
            int total = AnimationSheets.frameCount(role.tipo, animState);
            if (animFrame >= total) {
                if (isAttacking) {
                    boolean hasIdle = AnimationSheets.frameCount(role.tipo, AnimationSheets.Anim.IDLE) > 1;
                    animState = (velocity.len2() == 0 && hasIdle)
                        ? AnimationSheets.Anim.IDLE : AnimationSheets.Anim.WALK;
                }
                animFrame = 0;
            }
        }
        // WALK <-> IDLE transitions based on movement
        if (animState == AnimationSheets.Anim.WALK && velocity.len2() == 0) {
            boolean hasIdle = AnimationSheets.frameCount(role.tipo, AnimationSheets.Anim.IDLE) > 1;
            if (hasIdle) {
                animState = AnimationSheets.Anim.IDLE;
                animFrame = 0;
                animTimer = WALK_FRAME_DUR;
            } else {
                animFrame = 0;
                animTimer = WALK_FRAME_DUR;
            }
        } else if (animState == AnimationSheets.Anim.IDLE && velocity.len2() > 0) {
            animState = AnimationSheets.Anim.WALK;
            animFrame = 0;
            animTimer = WALK_FRAME_DUR;
        }
    }

    /**
     * Decrements cooldownTimer for each equipped weapon. Called by GameScreen each frame.
     * GameScreen then triggers shoot() when cooldownTimer reaches 0.
     */
    public void updateWeaponCooldowns(float delta) {
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            if (equippedWeapons[i] != null && equippedWeapons[i].cooldownTimer > 0) {
                equippedWeapons[i].cooldownTimer -= delta;
            }
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

    public float getMana() {
        return stats.mana;
    }

    public float getManaMax() {
        return stats.maxMana;
    }

    /** Adds mana without exceeding the current maximum. */
    public void gainMana(float amount) {
        stats.addMana(amount);
    }

    /** Consumes mana. Returns true if there was enough; false if mana was insufficient. */
    public boolean consumeMana(float cost) {
        return stats.consumirMana(cost);
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
        float speed = upgradeManager != null
            ? stats.baseSpeed * upgradeManager.getMultiplicadorVelocidad()
            : stats.baseSpeed;
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            if (equippedWeapons[i] != null) speed *= equippedWeapons[i].moveSpeedMult;
        }
        return speed;
    }

    // ── Weapon slot API (S5-02, ADR-006) ─────────────────────────────────────

    /**
     * Equips a weapon into the given slot. If the slot was occupied, the old weapon's
     * bonuses are removed first. Calls recalcStats() to apply bonuses cleanly.
     */
    public void equipWeapon(int slot, Weapon w) {
        equippedWeapons[slot] = w;
        recalcStats();
    }

    /**
     * Removes the weapon from the given slot and reverts its stat bonuses.
     */
    public void unequipWeapon(int slot) {
        equippedWeapons[slot] = null;
        recalcStats();
    }

    /** Returns the weapon in the given slot, or null if empty. */
    public Weapon getWeaponAtSlot(int slot) {
        return equippedWeapons[slot];
    }

    /** Stores a weapon in the first free storage slot. Returns true if stored. */
    public boolean storeWeapon(Weapon w) {
        for (int i = 0; i < storageWeapons.length; i++) {
            if (storageWeapons[i] == null) { storageWeapons[i] = w; return true; }
        }
        return false;
    }

    public boolean isStorageFull() {
        for (Weapon w : storageWeapons) if (w == null) return false;
        return true;
    }

    public Weapon getStorageWeapon(int slot) { return storageWeapons[slot]; }

    /** Swaps an active slot (0-1) with a storage slot (0-3). */
    public void swapActiveWithStorage(int activeSlot, int storageSlot) {
        Weapon tmp = equippedWeapons[activeSlot];
        equippedWeapons[activeSlot] = storageWeapons[storageSlot];
        storageWeapons[storageSlot] = tmp;
        recalcStats();
    }

    /** Swaps two storage slots. */
    public void swapStorageSlots(int a, int b) {
        Weapon tmp = storageWeapons[a];
        storageWeapons[a] = storageWeapons[b];
        storageWeapons[b] = tmp;
    }

    /**
     * Recalculates all weapon-affected stats from base values.
     * Called after any equip/unequip to keep stats ghost-free.
     */
    public void recalcStats() {
        int hpSum = 0, defSum = 0, resMagSum = 0, manaMaxSum = 0;
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            if (equippedWeapons[i] != null) {
                hpSum      += equippedWeapons[i].hpBonus;
                defSum     += equippedWeapons[i].defBonus;
                resMagSum  += equippedWeapons[i].resMagBonus;
                manaMaxSum += equippedWeapons[i].manaMaxBonus;
            }
        }
        maxHealth             = stats.hpBase      + hpSum;
        stats.defensa         = stats.defBase     + (float) defSum;
        stats.resistenciaMagica = stats.resMagBase + (float) resMagSum;
        stats.maxMana         = stats.manaMaxBase + (float) manaMaxSum;
        // Clamp current values so they never exceed the new maxima
        currentHealth         = Math.min(currentHealth, maxHealth);
        stats.mana            = Math.min(stats.mana, stats.maxMana);
    }

    /** Returns the armor stack count for CABALLERO's Fortaleza Reactiva, or 0 for other roles. */
    public int getReliquiaStacks() {
        if (reliquia instanceof com.milwar.kaosuarina.reliquias.ReliquiaCaballero) {
            return ((com.milwar.kaosuarina.reliquias.ReliquiaCaballero) reliquia).getArmorStacks();
        }
        return 0;
    }

    /** Returns the combo count for TIRADOR's Momentum de Combate, or 0 for other roles. */
    public int getComboCount() {
        if (reliquia instanceof com.milwar.kaosuarina.reliquias.ReliquiaTirador) {
            return ((com.milwar.kaosuarina.reliquias.ReliquiaTirador) reliquia).getComboCount();
        }
        return 0;
    }

    public void dispose() {
    }
}
