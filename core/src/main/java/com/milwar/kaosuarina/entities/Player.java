package com.milwar.kaosuarina.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milwar.kaosuarina.Systems.PlayerStats;
import com.milwar.kaosuarina.Systems.UpgradeManager;
import com.milwar.kaosuarina.reliquias.Reliquia;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.DamageType;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.milwar.kaosuarina.utils.SpriteSheets;

public class Player {
    private static final float INVULNERABILITY_TIME = 1f;
    private final Role     role;
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

    private UpgradeManager upgradeManager;

    public Player(float x, float y, Role role) {
        this.role     = role;
        this.stats    = role.stats;
        this.reliquia = role.reliquia;
        switch (role.tipo) {
            case CABALLERO: roleAccent = new Color(0f,     0.898f, 0.8f, 1f); break;
            case MAGO:      roleAccent = new Color(0.608f, 0.188f, 1f,   1f); break;
            case SHOOTER:   roleAccent = new Color(1f,     0.722f, 0f,   1f); break;
            default:        roleAccent = Color.WHITE.cpy(); break;
        }
        position = new Vector2(x, y);
        velocity = new Vector2();
        shootTimer = 0;
        invulnerabilityTimer = 0;
        maxHealth = stats.maxHealth;
        currentHealth = maxHealth;
        alive = true;
    }

    public void setUpgradeManager(UpgradeManager manager) {
        this.upgradeManager = manager;
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

        float cooldown = stats.baseShootCooldown * reliquia.getCooldownMultiplier();
        if (upgradeManager != null) cooldown /= upgradeManager.getMultiplicadorCadencia();

        shootTimer -= delta;
        if (shootTimer <= 0) {
            disparar(poolBalas, aimAngle);
            shootTimer = cooldown;
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

    private void disparar(PoolBalas poolBalas, float aimAngle) {
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
            case MAGO:    return DamageType.MAGICO;
            case SHOOTER: return DamageType.A_DISTANCIA;
            default:      return DamageType.FISICO;
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

        com.badlogic.gdx.graphics.Texture sprite = SpriteSheets.getSprite(role.tipo, lastDir);
        if (sprite != null) {
            batch.draw(sprite, position.x - 32, position.y - 32, 64, 64);
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
