package com.milwar.kaosuarina.screens.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.milwar.kaosuarina.entities.Enemy;
import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.entities.EnemyPool;
import com.milwar.kaosuarina.ui.HUD;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.utils.ParticlePool;

/**
 * Controls the full lifecycle of Guardian / Arquero / Devastador bosses.
 */
public class BossManager {

    private final Player player;
    private final EnemyPool poolEnemigos;
    private final HUD hud;
    // per-frame event flags — GameScreen reads these after update(), then reacts
    public boolean victoryTriggered = false;
    public boolean guardianDied = false;
    public float guardianDeathX, guardianDeathY;
    public boolean arqueroDied = false;
    public float arqueroDeathX, arqueroDeathY;
    public boolean damageTakenThisFrame = false;
    private Enemy guardianRef;
    private Enemy arqueroRef;
    private Enemy devastadorRef;
    // visual effect state (blast ring + spiral)
    private float blastEffectTimer;
    private float blastEffectX, blastEffectY, blastEffectRadius;
    private boolean blastEffectIsCaos;
    private boolean blastEffectIsDevastador;
    private float spiralEffectTimer;
    private float spiralEffectX, spiralEffectY;

    public BossManager(Player player, EnemyPool poolEnemigos, HUD hud) {
        this.player = player;
        this.poolEnemigos = poolEnemigos;
        this.hud = hud;
    }

    public void update(float delta) {
        guardianDied = false;
        arqueroDied = false;
        damageTakenThisFrame = false;
        victoryTriggered = false;

        if (blastEffectTimer > 0) blastEffectTimer -= delta;
        if (spiralEffectTimer > 0) spiralEffectTimer -= delta;

        updateDevastador();
        updateGuardian();
        updateArquero();

        if (devastadorRef != null && devastadorRef.active) {
            hud.setBossHealth(devastadorRef.health, Constants.DEVASTADOR_HP, 2);
        } else if (guardianRef != null && guardianRef.active) {
            hud.setBossHealth(guardianRef.health, Constants.GUARDIAN_HP, false);
        } else if (arqueroRef != null && arqueroRef.active) {
            hud.setBossHealth(arqueroRef.health, Constants.ARQUERO_HP, true);
        } else {
            hud.clearBossHealth();
        }
    }

    private void updateDevastador() {
        Enemy dev = poolEnemigos.getActiveDevastador();
        if (dev != null) devastadorRef = dev;
        if (devastadorRef == null) return;

        if (!devastadorRef.active) {
            hud.addScore(500);
            hud.addExperience(Constants.DEVASTADOR_XP);
            ParticlePool.spawn(devastadorRef.position.x, devastadorRef.position.y,
                30, 200f, 0.8f, 0.55f, 0f, 0.15f);
            devastadorRef = null;
            victoryTriggered = true;
            return;
        }

        if (!devastadorRef.phase2Triggered &&
            devastadorRef.health <= devastadorRef.maxHealth * Constants.DEVASTADOR_PHASE2_THRESHOLD) {
            devastadorRef.devastadorPhase = 2;
            devastadorRef.phase2Triggered = true;
            for (int i = 0; i < Constants.DEVASTADOR_PHASE2_SPAWNS; i++) {
                float spAngle = MathUtils.PI2 * i / Constants.DEVASTADOR_PHASE2_SPAWNS;
                poolEnemigos.spawn(
                    devastadorRef.position.x + MathUtils.cos(spAngle) * 150f,
                    devastadorRef.position.y + MathUtils.sin(spAngle) * 150f,
                    Enemy.Tipo.ESPECTRAL);
            }
        }

        if (devastadorRef.shockwaveThisFrame) {
            devastadorRef.shockwaveThisFrame = false;
            float ddx = player.position.x - devastadorRef.position.x;
            float ddy = player.position.y - devastadorRef.position.y;
            if (ddx * ddx + ddy * ddy <=
                Constants.DEVASTADOR_SHOCKWAVE_RADIUS * Constants.DEVASTADOR_SHOCKWAVE_RADIUS) {
                player.takeDamage(Constants.DEVASTADOR_SHOCKWAVE_DMG);
                damageTakenThisFrame = true;
            }
            blastEffectX = devastadorRef.position.x;
            blastEffectY = devastadorRef.position.y;
            blastEffectRadius = Constants.DEVASTADOR_SHOCKWAVE_RADIUS;
            blastEffectIsDevastador = true;
            blastEffectIsCaos = false;
            blastEffectTimer = 0.4f;
        }

        if (devastadorRef.spiralThisFrame) {
            devastadorRef.spiralThisFrame = false;
            spiralEffectX = devastadorRef.position.x;
            spiralEffectY = devastadorRef.position.y;
            spiralEffectTimer = 0.2f;
        }
    }

    private void updateGuardian() {
        Enemy g = poolEnemigos.getActiveGuardian();
        if (g != null) guardianRef = g;
        if (guardianRef == null) return;

        if (!guardianRef.active) {
            guardianDied = true;
            guardianDeathX = guardianRef.position.x;
            guardianDeathY = guardianRef.position.y;
            guardianRef = null;
            return;
        }

        if (guardianRef.shockwaveThisFrame) {
            guardianRef.shockwaveThisFrame = false;
            float dx = player.position.x - guardianRef.position.x;
            float dy = player.position.y - guardianRef.position.y;
            if (dx * dx + dy * dy <
                Constants.GUARDIAN_SHOCKWAVE_RADIUS * Constants.GUARDIAN_SHOCKWAVE_RADIUS) {
                player.takeDamage(Constants.GUARDIAN_SHOCKWAVE_DMG);
                damageTakenThisFrame = true;
            }
            blastEffectX = guardianRef.position.x;
            blastEffectY = guardianRef.position.y;
            blastEffectRadius = Constants.GUARDIAN_SHOCKWAVE_RADIUS;
            blastEffectIsDevastador = false;
            blastEffectIsCaos = false;
            blastEffectTimer = 0.3f;
        }
    }

    private void updateArquero() {
        Enemy ar = poolEnemigos.getActiveArquero();
        if (ar != null) arqueroRef = ar;

        if (arqueroRef != null && !arqueroRef.active) {
            arqueroDied = true;
            arqueroDeathX = arqueroRef.position.x;
            arqueroDeathY = arqueroRef.position.y;
            arqueroRef = null;
        }
    }

    /**
     * Called from activarSkill() when a skill blast (Martillo / Tomo) fires.
     */
    public void notifySkillBlast(float x, float y, float radius, boolean isCaos) {
        blastEffectX = x;
        blastEffectY = y;
        blastEffectRadius = radius;
        blastEffectIsCaos = isCaos;
        blastEffectIsDevastador = false;
        blastEffectTimer = 0.3f;
    }

    public void renderEffects(ShapeRenderer sr) {
        if (blastEffectTimer > 0) {
            sr.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(3);
            if (blastEffectIsDevastador) {
                sr.setColor(1f, 0.1f, 0.2f, 1f);
            } else if (blastEffectIsCaos) {
                sr.setColor(0.8f, 0.2f, 1f, 1f);
            } else {
                sr.setColor(1f, 0.85f, 0.1f, 1f);
            }
            sr.circle(blastEffectX, blastEffectY, blastEffectRadius, 48);
            sr.end();
            Gdx.gl.glLineWidth(1);
        }

        if (spiralEffectTimer > 0) {
            float alpha = spiralEffectTimer / 0.2f;
            sr.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(2);
            sr.setColor(1f, 0.2f, 0.1f, alpha);
            float lineLen = 280f;
            for (int i = 0; i < Constants.DEVASTADOR_SPIRAL_BULLETS; i++) {
                float ang = MathUtils.PI2 * i / Constants.DEVASTADOR_SPIRAL_BULLETS;
                sr.line(spiralEffectX, spiralEffectY,
                    spiralEffectX + MathUtils.cos(ang) * lineLen,
                    spiralEffectY + MathUtils.sin(ang) * lineLen);
            }
            sr.end();
            Gdx.gl.glLineWidth(1);
        }
    }
}
