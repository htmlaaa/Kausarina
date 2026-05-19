package com.milwar.kaosuarina.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Texturas compartidas entre todas las instancias del pool.
 * Cargar una vez en KaosuarinaGame.create(), liberar en dispose().
 * Todas las texturas son círculos con filtro Linear para evitar pixelado.
 */
public class SharedTextures {

    private static Texture player;
    private static Texture enemyWhite;
    private static Texture guardian;
    private static Texture arquero;
    private static Texture devastador;
    private static Texture bala;
    private static Texture balaEnemiga;

    public static void load() {
        // Jugador — círculo blanco; Player.render() lo tinta con el color del rol
        player = circleWithRing(64, 1f, 1f, 1f);

        // Enemigo — círculo blanco; Enemy.render() lo tinta con batch.setColor()
        enemyWhite = circle(32, 1f, 1f, 1f);

        // Guardián (minijefe) — círculo blanco 64px; Enemy.render() lo tinta naranja
        guardian = circle(64, 1f, 1f, 1f);

        // Arquero (minijefe) — círculo blanco 48px; Enemy.render() lo tinta azul
        arquero = circle(48, 1f, 1f, 1f);

        // Devastador del Caos (boss final) — círculo con anillo y marcas de runas 80px
        devastador = devastadorTexture(80);

        // Balas del jugador — círculo amarillo brillante
        bala = circleGlow(14, 1f, 0.95f, 0.3f);

        // Balas enemigas — círculo rojo
        balaEnemiga = circleGlow(14, 1f, 0.3f, 0.25f);
    }

    // ── Helpers de Pixmap ─────────────────────────────────────────────────────

    /**
     * Círculo sólido con borde suave via alpha.
     */
    private static Texture circle(int size, float r, float g, float b) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        int cx = size / 2, cy = size / 2, rad = size / 2 - 1;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx, dy = y - cy;
                float dst = (float) Math.sqrt(dx * dx + dy * dy);
                if (dst <= rad) {
                    float alpha = Math.min(1f, (rad - dst) + 0.5f);
                    p.setColor(r, g, b, alpha);
                    p.drawPixel(x, y);
                }
            }
        }
        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    /**
     * Círculo con anillo interior — para el jugador.
     */
    private static Texture circleWithRing(int size, float r, float g, float b) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        int cx = size / 2, cy = size / 2;
        float outerR = size / 2f - 1f;
        float innerR = outerR * 0.55f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx, dy = y - cy;
                float dst = (float) Math.sqrt(dx * dx + dy * dy);
                if (dst > outerR) continue;
                float alpha = Math.min(1f, (outerR - dst) + 0.5f);
                if (dst < innerR) {
                    // Núcleo central más claro
                    float bright = 0.85f + 0.15f * (1f - dst / innerR);
                    p.setColor(r * bright, g * bright, b * bright, alpha * 0.75f);
                } else {
                    // Anillo exterior opaco
                    p.setColor(r, g, b, alpha);
                }
                p.drawPixel(x, y);
            }
        }
        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    /**
     * Boss final: círculo oscuro con anillo rojo exterior, anillo interno y cruz de runas.
     * Enemy.render() sobreescribe el color del batch para la fase 2.
     */
    private static Texture devastadorTexture(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        int cx = size / 2, cy = size / 2;
        float outerR = size / 2f - 1f;
        float midR   = outerR * 0.72f;
        float innerR = outerR * 0.38f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx, dy = y - cy;
                float dst = (float) Math.sqrt(dx * dx + dy * dy);
                if (dst > outerR) continue;
                float alpha = Math.min(1f, (outerR - dst) + 0.5f);
                if (dst > midR) {
                    // Outer ring: bright red-orange
                    p.setColor(1f, 0.2f, 0.1f, alpha);
                } else if (dst > innerR) {
                    // Middle zone: dark void
                    p.setColor(0.08f, 0f, 0.05f, alpha * 0.95f);
                } else {
                    // Inner core: pulsing magenta
                    float glow = 0.6f + 0.4f * (1f - dst / innerR);
                    p.setColor(glow, 0f, glow * 0.5f, alpha);
                }
                p.drawPixel(x, y);
            }
        }
        // Cross / rune marks on middle ring (4 tick marks)
        p.setColor(1f, 0.5f, 0.2f, 1f);
        int armLen = (int)(outerR * 0.22f);
        int armW   = 2;
        // N/S/E/W marks
        p.fillRectangle(cx - armW / 2, cy - (int)outerR + 2,         armW, armLen);
        p.fillRectangle(cx - armW / 2, cy + (int)outerR - 2 - armLen, armW, armLen);
        p.fillRectangle(cx - (int)outerR + 2,         cy - armW / 2, armLen, armW);
        p.fillRectangle(cx + (int)outerR - 2 - armLen, cy - armW / 2, armLen, armW);
        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    /**
     * Círculo con halo suave — para balas.
     */
    private static Texture circleGlow(int size, float r, float g, float b) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        int cx = size / 2, cy = size / 2;
        float coreR = size / 2f * 0.5f;
        float glowR = size / 2f - 0.5f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx, dy = y - cy;
                float dst = (float) Math.sqrt(dx * dx + dy * dy);
                if (dst > glowR) continue;
                float alpha;
                if (dst <= coreR) {
                    alpha = 1f;
                } else {
                    alpha = 1f - (dst - coreR) / (glowR - coreR);
                }
                p.setColor(r, g, b, alpha * alpha);
                p.drawPixel(x, y);
            }
        }
        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public static Texture getPlayer() {
        return player;
    }

    public static Texture getEnemyWhite() {
        return enemyWhite;
    }

    public static Texture getGuardian() {
        return guardian;
    }

    public static Texture getArquero() {
        return arquero;
    }

    public static Texture getDevastador() {
        return devastador;
    }

    public static Texture getBala() {
        return bala;
    }

    public static Texture getBalaEnemiga() {
        return balaEnemiga;
    }

    public static void dispose() {
        if (player != null) {
            player.dispose();
            player = null;
        }
        if (enemyWhite != null) {
            enemyWhite.dispose();
            enemyWhite = null;
        }
        if (guardian != null) {
            guardian.dispose();
            guardian = null;
        }
        if (arquero != null) {
            arquero.dispose();
            arquero = null;
        }
        if (devastador != null) {
            devastador.dispose();
            devastador = null;
        }
        if (bala != null) {
            bala.dispose();
            bala = null;
        }
        if (balaEnemiga != null) {
            balaEnemiga.dispose();
            balaEnemiga = null;
        }
    }
}
