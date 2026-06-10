package com.milwar.kaosuarina.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.milwar.kaosuarina.KaosuarinaGame;
import com.milwar.kaosuarina.db.DBManager;
import com.milwar.kaosuarina.db.vo.RunVO;
import com.milwar.kaosuarina.entities.*;
import com.milwar.kaosuarina.screens.managers.BossManager;
import com.milwar.kaosuarina.screens.managers.SpawnManager;
import com.milwar.kaosuarina.systems.Upgrade;
import com.milwar.kaosuarina.systems.UpgradeManager;
import com.milwar.kaosuarina.roles.Role;
import com.milwar.kaosuarina.ui.HUD;
import com.milwar.kaosuarina.utils.AudioManager;
import com.milwar.kaosuarina.utils.CollisionManager;
import com.milwar.kaosuarina.utils.Constants;
import com.badlogic.gdx.graphics.Color;
import com.milwar.kaosuarina.weapons.Weapon;
import com.milwar.kaosuarina.weapons.WeaponCategory;
import com.milwar.kaosuarina.weapons.WeaponDropper;
import com.milwar.kaosuarina.weapons.WeaponNormal;
import com.milwar.kaosuarina.weapons.WeaponPool;
import com.milwar.kaosuarina.weapons.WeaponSkill;
import com.milwar.kaosuarina.weapons.WeaponType;
import com.milwar.kaosuarina.utils.SharedTextures;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.List;

public class GameScreen implements Screen {

    private static final Map<String, String> WEAPON_NAMES = new HashMap<>();

    static {
        WEAPON_NAMES.put("W_SHORTSWORD", "Espada_Corta");
        WEAPON_NAMES.put("W_FLAMEBLADE", "Hoja_Llameante");
        WEAPON_NAMES.put("W_HEAVYBLADE", "Mandoble");
        WEAPON_NAMES.put("W_VAMP_DAGGER", "Daga_Vampirica");
        WEAPON_NAMES.put("W_HUNTBOW", "Arco_de_Caza");
        WEAPON_NAMES.put("W_POISON_BOW", "Arco_Venenoso");
        WEAPON_NAMES.put("W_BALLISTA", "Ballesta");
        WEAPON_NAMES.put("W_DUAL_PISTOLS", "Pistolas_Gemelas");
        WEAPON_NAMES.put("W_APP_STAFF", "Baculo_Aprendiz");
        WEAPON_NAMES.put("W_FIRE_STAFF", "Baculo_de_Fuego");
        WEAPON_NAMES.put("W_CHAOS_WAND", "Vara_del_Caos");
        WEAPON_NAMES.put("W_PLAGUE_GRIM", "Grimorio_Plaga");
    }

    private final KaosuarinaGame game;
    private final Role roleInicial;
    private final Difficulty difficulty;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport gameViewport;

    private Player player;
    private BulletPool poolBalas;
    private EnemyPool poolEnemigos;
    private EnemyBulletPool poolBalasEnemigas;

    private HUD hud;
    private UpgradeManager upgradeManager;
    private LevelUpScreen levelUpScreen;

    private BossManager bossManager;
    private SpawnManager spawnManager;

    private int nivelAnterior;
    private int pendingLevelUps;
    private float aimAngle;
    private boolean leftClickJust;
    private boolean rightClickJust;
    private int lastMouseX, lastMouseY;
    private float mouseActiveTimer = 0f;
    private static final float MOUSE_ACTIVE_DURATION = 1.5f;
    private float tiempoSupervivencia;
    private boolean runGuardada;
    private boolean gameWon;

    private int lastComboCount = 0;

    private int nextChestWave;
    private float chestX, chestY;
    private boolean chestActive;
    private Weapon chestWeapon;
    private Weapon pendingPickupWeapon;
    private boolean swapMenuActive;
    private float swapMenuTimer;
    private boolean pauseMenuActive;
    private int currentDepthForDrops = 1;
    private float hpRegenAccum = 0f;

    // Inventario de 6 slots (TAB)
    private boolean inventoryOpen = false;
    private int inventorySelected = -1;

    // Pool de drops de arma en mundo (enemy loot)
    private static final int DROP_POOL = 20;
    private static final float DROP_LIFETIME = 30f;
    private static final float DROP_FADE_DIST = 1400f;
    private final float[] dropX = new float[DROP_POOL];
    private final float[] dropY = new float[DROP_POOL];
    private final Weapon[] dropWeapon = new Weapon[DROP_POOL];
    private final boolean[] dropActive = new boolean[DROP_POOL];
    private final float[] dropTimer = new float[DROP_POOL];

    // S6-02 — Scroll de Inscripción
    private int nextScrollWave;
    private float scrollX, scrollY;
    private boolean scrollActive;
    private com.milwar.kaosuarina.weapons.Inscription pendingInscription;
    private boolean scrollMenuActive;
    private float scrollMenuTimer;

    // S6-03 — Amuletos
    private int nextAmuletWave;
    private float amuletX, amuletY;
    private com.milwar.kaosuarina.items.AmuletType amuletType;
    private boolean amuletActive;
    private boolean damageTakenThisWave;
    // MEC-01 — Amulet swap menu (when slots full)
    private com.milwar.kaosuarina.items.AmuletType pendingAmuletType;
    private boolean amuletSwapMenuActive;
    private float amuletSwapMenuTimer;
    private int amuletSwapSelected = 0;

    public GameScreen(KaosuarinaGame game, Role role, Difficulty difficulty) {
        this.game = game;
        this.roleInicial = role;
        this.difficulty = difficulty;
    }

    @Override
    public void show() {
        inicializar();
    }

    private void inicializar() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        gameViewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);

        player = new Player(0, 0, roleInicial);
        poolBalas = new BulletPool();
        poolEnemigos = new EnemyPool();
        poolBalasEnemigas = new EnemyBulletPool();
        hud = new HUD(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        upgradeManager = new UpgradeManager();
        upgradeManager.setRole(player.getRole().tipo);
        player.setUpgradeManager(upgradeManager);

        levelUpScreen = new LevelUpScreen(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        nivelAnterior = 1;
        pendingLevelUps = 0;
        tiempoSupervivencia = 0f;
        runGuardada = false;
        gameWon = false;

        bossManager = new BossManager(player, poolEnemigos, hud);
        spawnManager = new SpawnManager(player, poolEnemigos,
            difficulty.eliteWaveStart, difficulty.intensityMult);
        currentDepthForDrops = difficulty.initialDepth;

        nextChestWave = MathUtils.random(Constants.CHEST_SPAWN_INTERVAL_MIN, Constants.CHEST_SPAWN_INTERVAL_MAX);
        chestActive = false;
        chestWeapon = null;
        pendingPickupWeapon = null;
        swapMenuActive = false;
        swapMenuTimer = 0f;
        pauseMenuActive = false;
        com.milwar.kaosuarina.weapons.inscriptions.EchoQueue.clear();
        com.milwar.kaosuarina.utils.ParticlePool.clear();
        AudioManager.startMusic();

        nextScrollWave = Constants.SCROLL_SPAWN_INTERVAL_MIN + 2;
        scrollActive = false;
        scrollMenuActive = false;
        scrollMenuTimer = 0f;
        pendingInscription = null;

        nextAmuletWave = Constants.AMULET_SPAWN_INTERVAL_MIN;
        amuletActive = false;
        damageTakenThisWave = false;
        amuletSwapMenuActive = false;
        amuletSwapMenuTimer = 0f;
        amuletSwapSelected = 0;
        pendingAmuletType = null;
        lastComboCount = 0;
    }

    @Override
    public void render(float delta) {
        if (gameWon) {
            renderVictoria();
            return;
        }

        if (levelUpScreen.isActive()) {
            procesarLevelUp();
            ScreenUtils.clear(0, 0, 0, 1f);
            levelUpScreen.render(batch);
            return;
        }

        if (!player.isAlive()) {
            renderGameOver();
            return;
        }

        // TAB opens/closes inventory
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB) && !swapMenuActive && !scrollMenuActive && !pauseMenuActive) {
            inventoryOpen = !inventoryOpen;
            inventorySelected = -1;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (inventoryOpen) {
                inventoryOpen = false;
                inventorySelected = -1;
            } else if (!swapMenuActive && !scrollMenuActive) {
                pauseMenuActive = !pauseMenuActive;
            }
        }

        boolean anyOverlay = pauseMenuActive || swapMenuActive || scrollMenuActive || inventoryOpen || amuletSwapMenuActive;
        if (!anyOverlay) {
            procesarInput();
            actualizarJuego(delta);
            procesarColisiones();
            detectarLevelUp();
        } else if (swapMenuActive) {
            updateSwapMenu(delta);
        } else if (scrollMenuActive) {
            updateScrollMenu(delta);
        }

        actualizarAffixBonuses();
        actualizarHudSlots();
        renderizar();

        if (inventoryOpen) {
            int hovered = hud.getInventoryHoveredSlot(Gdx.input.getX(), Gdx.input.getY());
            hud.renderInventoryOverlay(batch, hovered, inventorySelected);
            if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT) && hovered >= 0) {
                procesarClickInventario(hovered);
            }
        }

        if (pauseMenuActive && !inventoryOpen) {
            hud.renderPauseMenu(batch);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                pauseMenuActive = false;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                liberarRecursos();
                game.setScreen(new MainMenuScreen(game));
            }
        }
    }

    /**
     * Recalcula cada frame los bonos de afijo de arma para lifesteal y mp_regen.
     */
    private void actualizarAffixBonuses() {
        float ls = 0f, mr = 0f;
        for (int i = 0; i < 2; i++) {
            com.milwar.kaosuarina.weapons.Weapon w = player.getWeaponAtSlot(i);
            if (w != null && w.rolledInstance != null) {
                ls += w.rolledInstance.getSumStat("lifesteal_pct") / 100f;
                mr += w.rolledInstance.getSumStat("mp_regen");
            }
        }
        player.getStats().weaponAffixLifesteal = ls;
        player.getStats().weaponAffixMpRegen = mr;
    }

    private void actualizarHudSlots() {
        // Slots 0-1: activos (con cooldown y mana lock)
        for (int i = 0; i < 2; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            String insc = (w != null && w.inscription != null) ? w.inscription.getName() : null;
            String tier = (w != null) ? w.tierId : null;
            if (w == null) {
                hud.setWeaponSlot(i, "-", false, 0f, false, null, null);
                hud.setSlotAffix(i, null);
            } else if (w instanceof WeaponSkill) {
                WeaponSkill sk = (WeaponSkill) w;
                float frac = sk.skillCooldownBase > 0 ? Math.min(1f, sk.skillCooldownTimer / sk.skillCooldownBase) : 0f;
                boolean locked = player.getStats().mana < sk.manaCost;
                hud.setWeaponSlot(i, specificWeaponName(w), true, frac, locked, insc, tier);
                hud.setSlotAffix(i, buildAffixLabel(w));
            } else {
                float frac = w.cooldownBase > 0 ? Math.min(1f, w.cooldownTimer / w.cooldownBase) : 0f;
                hud.setWeaponSlot(i, specificWeaponName(w), false, frac, false, insc, tier);
                hud.setSlotAffix(i, buildAffixLabel(w));
            }
        }
        // Slots 2-5: almacenamiento
        for (int i = 0; i < 4; i++) {
            Weapon w = player.getStorageWeapon(i);
            if (w != null) {
                String insc = w.inscription != null ? w.inscription.getName() : null;
                hud.setWeaponSlot(i + 2, specificWeaponName(w), false, 0f, false, insc, w.tierId);
                hud.setSlotAffix(i + 2, buildAffixLabel(w));
            } else {
                hud.setWeaponSlot(i + 2, "-", false, 0f, false, null, null);
                hud.setSlotAffix(i + 2, null);
            }
        }
    }

    private void procesarClickInventario(int clicked) {
        if (inventorySelected == -1) {
            inventorySelected = clicked;
        } else if (inventorySelected == clicked) {
            inventorySelected = -1; // deselect
        } else {
            // Swap the two slots
            int a = inventorySelected, b = clicked;
            boolean aActive = a < 2, bActive = b < 2;
            if (aActive && bActive) {
                // swap active ↔ active: swap via storage trick
                Weapon wA = player.getWeaponAtSlot(a);
                player.equipWeapon(a, player.getWeaponAtSlot(b));
                player.equipWeapon(b, wA);
            } else if (aActive) {
                player.swapActiveWithStorage(a, b - 2);
            } else if (bActive) {
                player.swapActiveWithStorage(b, a - 2);
            } else {
                player.swapStorageSlots(a - 2, b - 2);
            }
            inventorySelected = -1;
        }
    }

    private void procesarLevelUp() {
        levelUpScreen.setRerollsLeft(upgradeManager.getRerollsLeft());
        levelUpScreen.handleInput();
        // MEC-02 — Reroll
        if (levelUpScreen.isRerollRequested()) {
            com.badlogic.gdx.utils.Array<Upgrade> nuevas = upgradeManager.reroll(3);
            if (nuevas != null && nuevas.size > 0) levelUpScreen.show(nuevas);
            return;
        }
        Upgrade seleccionado = levelUpScreen.getSelectedUpgrade();
        if (seleccionado != null) {
            upgradeManager.applyUpgrade(seleccionado);
            aplicarBonusUpgrade(seleccionado);
            levelUpScreen.hide();
        }
    }

    private void aplicarBonusUpgrade(Upgrade u) {
        com.milwar.kaosuarina.systems.PlayerStats st = player.getStats();
        switch (u.tipo) {
            case VIDA_MAXIMA_UP:
                player.increaseMaxHealth(20);
                break;
            case GOLPE_PESADO:
                st.meleeLightDamage = Math.round(st.meleeLightDamage * 1.30f);
                st.meleeHeavyDamage = Math.round(st.meleeHeavyDamage * 1.30f);
                break;
            case DEFENSA:
                st.physicalDefense += 8f;
                st.defBase += 8f;
                break;
            case MANA_MAXIMO_UP:
                st.maxMana += 30f;
                st.manaMaxBase += 30f;
                break;
            case RESONANCIA:
                st.blastRadiusMult *= 1.40f;
                break;
            case CADENCIA_MAGICA:
                st.lightAttackCooldown = Math.max(0.10f, st.lightAttackCooldown * 0.80f);
                break;
            case RECARGA_RAPIDA:
                st.baseShootCooldown = Math.max(0.08f, st.baseShootCooldown * 0.85f);
                break;
            // ── CNT-03 Genéricos ─────────────────────────────────────────────
            case REGENERACION:
                // +1 HP/s stacks; tracked via amuletRegenBonus alternative — use dedicated field
                st.hpRegenPerSec += 1f;
                break;
            case AURA_VENENO:
                st.hasAuraVeneno = true;
                break;
            case CRITICO_ENCADENADO:
                st.hasCriticoEncadenado = true;
                break;
            case PESO_MUERTO:
                st.hasPesoMuerto = true;
                break;
            // ── CNT-03 Caballero ─────────────────────────────────────────────
            case CONTRAGOLPE:
                st.contragolpeNivel++;
                break;
            case AURA_INTIMIDACION:
                st.hasAuraIntimidacion = true;
                break;
            case GOLPE_TIERRA:
                st.hasGolpeTierra = true;
                break;
            // ── CNT-03 Mago ──────────────────────────────────────────────────
            case MANA_SOBRECARGA:
                st.hasManaOvercharge = true;
                break;
            case BARRERA_MAGICA:
                st.barreraMagicaNivel++;
                break;
            case HECHIZO_ESFERAS:
                st.hasHechizoesferas = true;
                break;
            // ── CNT-03 Tirador ───────────────────────────────────────────────
            case BALA_EXPLOSIVA:
                st.hasBalaExplosiva = true;
                break;
            case MUNICION_ENVENENADA:
                st.hasMunicionEnvenenada = true;
                break;
            case DISPARO_TENSO:
                st.hasDisparoTenso = true;
                break;
            default:
                break;
        }
    }

    private void procesarInput() {
        player.velocity.set(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.velocity.y = player.getCurrentSpeed();
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.velocity.y = -player.getCurrentSpeed();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.velocity.x = -player.getCurrentSpeed();
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.velocity.x = player.getCurrentSpeed();

        if (player.velocity.len2() > 0) player.velocity.nor().scl(player.getCurrentSpeed());

        int curMouseX = Gdx.input.getX();
        int curMouseY = Gdx.input.getY();
        float mouseX = curMouseX;
        float mouseY = Gdx.graphics.getHeight() - curMouseY;
        aimAngle = (float) Math.atan2(
            mouseY - Gdx.graphics.getHeight() / 2f,
            mouseX - Gdx.graphics.getWidth() / 2f
        );
        if (Math.abs(curMouseX - lastMouseX) > 2 || Math.abs(curMouseY - lastMouseY) > 2) {
            mouseActiveTimer = MOUSE_ACTIVE_DURATION;
        }
        lastMouseX = curMouseX;
        lastMouseY = curMouseY;
        if (mouseActiveTimer > 0) mouseActiveTimer -= Gdx.graphics.getDeltaTime();

        leftClickJust = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        rightClickJust = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
    }

    private void actualizarJuego(float delta) {
        tiempoSupervivencia += delta;
        com.milwar.kaosuarina.weapons.inscriptions.EchoQueue.tick(delta);
        com.milwar.kaosuarina.utils.ParticlePool.update(delta);
        player.update(delta, poolBalas, aimAngle);

        // HP regen pasiva (amuleto Tótem de Regen)
        float regenRate = player.getStats().hpRegenPerSec;
        if (regenRate > 0) {
            hpRegenAccum += regenRate * delta;
            if (hpRegenAccum >= 1f) {
                player.heal((int) hpRegenAccum);
                hpRegenAccum -= (int) hpRegenAccum;
            }
        }

        // Manual attacks (Caballero / Mago)
        if (player.getRole().attackMode != com.milwar.kaosuarina.roles.Role.AttackMode.AUTO_SHOOT) {
            if (leftClickJust) recompensarKills(player.triggerLightAttack(poolBalas, aimAngle, poolEnemigos));
            if (rightClickJust) recompensarKills(player.triggerHeavyAttack(poolBalas, aimAngle, poolEnemigos));
        }

        // Weapon auto-fire (S5-04, ADR-006) — Caballero / Mago via slots
        player.updateWeaponCooldowns(delta);
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            if (w != null && w.category == WeaponCategory.NORMAL && w.cooldownTimer <= 0) {
                ((WeaponNormal) w).shoot(player, poolBalas, poolEnemigos, aimAngle, i);
                w.cooldownTimer = calcCooldownEfectivo(w);
            }
        }

        // Shooter built-in range-based auto-fire (no weapon slot)
        if (player.getRole().tipo == com.milwar.kaosuarina.roles.Role.Tipo.SHOOTER) {
            player.updateShooterAutoFire(delta, poolBalas, poolEnemigos,
                aimAngle, mouseActiveTimer > 0);
        }

        // Skill cooldown timers (S5-05)
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            if (w instanceof WeaponSkill) {
                WeaponSkill sk = (WeaponSkill) w;
                if (sk.skillCooldownTimer > 0) sk.skillCooldownTimer -= delta;
            }
        }

        // Skill activation: Q → slot 0, E → slot 1 (S5-05)
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) activarSkill(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) activarSkill(1);

        poolBalas.update(delta);
        poolBalasEnemigas.update(delta);
        poolEnemigos.update(delta, player.position, poolBalasEnemigas);
        procesarSeñalesEnemigosSprint4(delta);
        hud.update(delta);

        // Wave spawning + difficulty scaling
        currentDepthForDrops = hud.getLevel();
        poolEnemigos.setCurrentDepth(currentDepthForDrops);
        if (spawnManager.update(delta, hud.getLevel())) {
            onWaveFired();
        }

        hud.setHealth(player.getCurrentHealth(), player.getMaxHealth());
        hud.setMana(player.getStats().mana, player.getStats().maxMana);

        checkChestProximity();
        procesarLootDrops();
        updateDropTimers(delta);
        checkPlayerDropCollision();
        updateSwapMenu(delta);
        checkPlayerScrollCollision();
        updateScrollMenu(delta);
        checkPlayerAmuletCollision();
        updateAmuletSwapMenu(delta);

        // Boss lifecycle (delegated to BossManager)
        bossManager.update(delta);
        if (bossManager.damageTakenThisFrame) damageTakenThisWave = true;
        if (bossManager.victoryTriggered) gameWon = true;
        if (bossManager.guardianDied) spawnScrollAt(bossManager.guardianDeathX, bossManager.guardianDeathY);
        if (bossManager.arqueroDied) spawnChestAt(bossManager.arqueroDeathX, bossManager.arqueroDeathY);

        // Amuleto slots HUD (MEC-01)
        com.milwar.kaosuarina.items.AmuletType[] slots = new com.milwar.kaosuarina.items.AmuletType[3];
        for (int i = 0; i < 3; i++) slots[i] = player.getAmuletAtSlot(i);
        hud.setAmuletSlots(slots);
        hud.setAmuletFlags(player.getStats().hasSedDeSangre, player.getStats().hasGuardianArena);

        // Relic display HUD
        hud.setWave(spawnManager.getWaveCount());
        actualizarRelicDisplay();

    }

    /**
     * Called by SpawnManager when a wave fires. Handles collectibles and arena bonus.
     */
    private void onWaveFired() {
        if (player.getStats().hasGuardianArena && !damageTakenThisWave && spawnManager.getWaveCount() > 1) {
            player.getStats().addMana(Constants.GUARDIAN_ARENA_GAIN);
        }
        damageTakenThisWave = false;

        int wave = spawnManager.getWaveCount();
        if (!chestActive && wave >= nextChestWave) {
            spawnChest();
            nextChestWave = wave + MathUtils.random(Constants.CHEST_SPAWN_INTERVAL_MIN, Constants.CHEST_SPAWN_INTERVAL_MAX);
        }
        if (!scrollActive && wave >= nextScrollWave) {
            spawnScroll();
            nextScrollWave = wave + MathUtils.random(Constants.SCROLL_SPAWN_INTERVAL_MIN, Constants.SCROLL_SPAWN_INTERVAL_MAX);
        }
        if (!amuletActive && wave >= nextAmuletWave) {
            spawnAmulet();
            nextAmuletWave = wave + MathUtils.random(Constants.AMULET_SPAWN_INTERVAL_MIN, Constants.AMULET_SPAWN_INTERVAL_MAX);
        }
    }

    private void procesarColisiones() {
        CollisionManager.rebuildGrid(poolEnemigos);
        recompensarKills(CollisionManager.checkBulletsVsEnemies(player, poolBalas, poolEnemigos));
        recompensarKills(CollisionManager.checkMalditoExplosions(poolEnemigos));

        int contactDmg = CollisionManager.checkPlayerVsEnemies(player, poolEnemigos);
        if (contactDmg > 0) {
            int hpBefore = player.getCurrentHealth();
            player.takeDamage(contactDmg);
            if (player.getCurrentHealth() < hpBefore) damageTakenThisWave = true;
        }

        int hpBefore = player.getCurrentHealth();
        CollisionManager.checkEnemyBullets(poolBalasEnemigas, player);
        if (player.getCurrentHealth() < hpBefore) damageTakenThisWave = true;
    }

    private void recompensarKills(int n) {
        if (n <= 0) return;
        hud.addScore(n * 10);
        hud.addExperience(n * 25f);
        for (int i = 0; i < n; i++) {
            player.onKill();
            player.getStats().addMana(5f);
            if (player.getStats().hasSedDeSangre) {
                player.getStats().addMana(Constants.SED_DE_SANGRE_GAIN_AMULET);
            }
        }
    }

    private void detectarLevelUp() {
        int nivelActual = hud.getLevel();
        if (nivelActual > nivelAnterior) {
            pendingLevelUps += nivelActual - nivelAnterior;
            nivelAnterior = nivelActual;
        }
        if (pendingLevelUps > 0 && !levelUpScreen.isActive()) {
            com.badlogic.gdx.utils.Array<com.milwar.kaosuarina.systems.Upgrade> opts =
                upgradeManager.getRandomUpgrades(3);
            pendingLevelUps--;
            if (opts.size > 0) {
                AudioManager.playLevelUp();
                levelUpScreen.show(opts);
            }
            // opts vacío = todos los upgrades al máximo; consumir el pending en silencio
        }
    }

    private void renderizar() {
        gameViewport.apply(true);
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();

        ScreenUtils.clear(0.10f, 0.08f, 0.06f, 1f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Arena floor — suelo rocoso/terreno ligeramente mas claro
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.18f, 0.14f, 0.10f, 1f);
        shapeRenderer.circle(0, 0, Constants.ARENA_RADIUS, 128);
        shapeRenderer.end();

        // Arena boundary ring
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.55f, 0.42f, 0.28f, 0.85f);
        shapeRenderer.circle(0, 0, Constants.ARENA_RADIUS, 128);
        shapeRenderer.end();

        // Attack effects (sword arc / magic explosion)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        player.renderAttackEffect(shapeRenderer);
        if (chestActive) {
            // Cofre: cuerpo oscuro
            shapeRenderer.setColor(0.30f, 0.20f, 0.04f, 1f);
            shapeRenderer.rect(chestX - 16, chestY - 16, 32, 24);
            // Tapa
            shapeRenderer.setColor(0.40f, 0.28f, 0.06f, 1f);
            shapeRenderer.rect(chestX - 16, chestY + 8, 32, 10);
            // Broche dorado
            shapeRenderer.setColor(1f, 0.82f, 0.1f, 1f);
            shapeRenderer.rect(chestX - 5, chestY - 3, 10, 7);
        }
        if (scrollActive) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.8f, 1f);
            shapeRenderer.circle(scrollX, scrollY, 12, 12);
        }
        if (amuletActive) {
            shapeRenderer.setColor(0.6f, 0.1f, 0.9f, 1f);
            shapeRenderer.circle(amuletX, amuletY, 10, 12);
        }
        shapeRenderer.end();

        // ── Enemy AoE zone visuals ────────────────────────────────────────────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : poolEnemigos.getEnemies()) {
            if (!e.active) continue;
            switch (e.tipo) {
                case ELITE_ZONE:
                    shapeRenderer.setColor(0.55f, 0f, 0.85f, 0.18f);
                    shapeRenderer.circle(e.position.x, e.position.y, Constants.ELITE_ZONE_RADIUS, 48);
                    break;
                case HEALER:
                    shapeRenderer.setColor(0.1f, 0.9f, 0.25f, 0.10f);
                    shapeRenderer.circle(e.position.x, e.position.y, Constants.HEALER_HEAL_RADIUS, 32);
                    break;
                case BERSERKER:
                    if (e.berserkActive) {
                        shapeRenderer.setColor(1f, 0.05f, 0.05f, 0.20f);
                        shapeRenderer.circle(e.position.x, e.position.y, 55f, 24);
                    }
                    break;
                case MALDITO:
                    shapeRenderer.setColor(0.1f, 0.85f, 0.1f, 0.07f);
                    shapeRenderer.circle(e.position.x, e.position.y, Constants.MALDITO_EXPLOSION_RADIUS, 32);
                    break;
                default:
                    break;
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        for (Enemy e : poolEnemigos.getEnemies()) {
            if (!e.active) continue;
            switch (e.tipo) {
                case ELITE_ZONE:
                    shapeRenderer.setColor(0.75f, 0.1f, 1f, 0.65f);
                    shapeRenderer.circle(e.position.x, e.position.y, Constants.ELITE_ZONE_RADIUS, 48);
                    break;
                case HEALER:
                    shapeRenderer.setColor(0.1f, 0.9f, 0.25f, 0.35f);
                    shapeRenderer.circle(e.position.x, e.position.y, Constants.HEALER_HEAL_RADIUS, 32);
                    break;
                case ELITE_CHARGE:
                    if (e.chargeState == 1) {
                        shapeRenderer.setColor(1f, 0.35f, 0f, 0.85f);
                        shapeRenderer.line(e.position.x, e.position.y,
                            e.position.x + e.chargeDirX * 300f,
                            e.position.y + e.chargeDirY * 300f);
                        shapeRenderer.circle(e.position.x, e.position.y, 38f, 24);
                    }
                    break;
                default:
                    break;
            }
        }
        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();

        // Boss blast rings + spiral (delegated to BossManager)
        bossManager.renderEffects(shapeRenderer);

        shapeRenderer.setProjectionMatrix(camera.combined);
        com.milwar.kaosuarina.utils.ParticlePool.render(shapeRenderer);

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        poolBalas.render(batch);
        poolBalasEnemigas.render(batch);
        poolEnemigos.render(batch);
        player.render(batch);
        renderDrops(batch);
        batch.end();

        hud.render(batch);
        if (swapMenuActive && pendingPickupWeapon != null) {
            HUD.WeaponCard newCard = buildWeaponCard(pendingPickupWeapon);
            HUD.WeaponCard card0 = buildWeaponCard(player.getWeaponAtSlot(0));
            HUD.WeaponCard card1 = buildWeaponCard(player.getWeaponAtSlot(1));
            hud.renderSwapMenuCards(batch, newCard, card0, card1, swapMenuTimer);
        }
        if (scrollMenuActive && pendingInscription != null) {
            boolean s0w = player.getWeaponAtSlot(0) != null;
            boolean s1w = player.getWeaponAtSlot(1) != null;
            hud.renderScrollMenu(batch, s0w, s1w, pendingInscription.getName(), scrollMenuTimer);
        }
    }

    private void renderGameOver() {
        if (runGuardada) return;
        runGuardada = true;
        int scoreVal = hud.getScore();
        int levelVal = hud.getLevel();
        int waves = spawnManager.getWaveCount();
        DBManager.guardarRun(construirRunData(false));
        int tokens = DBManager.calcularTokens(scoreVal, levelVal, waves);
        int tokensTotal = DBManager.addTokens(tokens);
        String[] lb = buildLeaderboard(DBManager.getTop10());
        int tiempo = (int) tiempoSupervivencia;
        int pid = personajeIdDe(player.getRole().tipo);
        String armas = hud.getArmasText();
        int[] kills = poolEnemigos.getKillsByType().clone();
        liberarRecursos();
        game.setScreen(new GameOverScreen(game, scoreVal, tiempo, levelVal, waves, pid, armas, kills, lb, tokens, tokensTotal));
    }

    private void renderVictoria() {
        if (runGuardada) return;
        runGuardada = true;
        int scoreVal = hud.getScore();
        int tiempo = (int) tiempoSupervivencia;
        int levelVal = hud.getLevel();
        int waves = spawnManager.getWaveCount();
        DBManager.guardarRun(construirRunData(true));
        String[] lb = buildLeaderboard(DBManager.getTop10());
        int pid = personajeIdDe(player.getRole().tipo);
        String armas = hud.getArmasText();
        liberarRecursos();
        game.setScreen(new WinScreen(game, scoreVal, tiempo, levelVal, waves, pid, armas, lb));
    }

    private String[] buildLeaderboard(List<RunVO> runs) {
        if (runs == null || runs.isEmpty()) return new String[0];
        int n = Math.min(runs.size(), 5);
        String[] lines = new String[n];
        for (int i = 0; i < n; i++) {
            RunVO v = runs.get(i);
            lines[i] = String.format("#%d  %-3s  %5d pts  Lv%-2d  %02d:%02ds",
                i + 1, rolAbrev(v.personajeId), v.score, v.nivelAlcanzado,
                v.tiempoSegundos / 60, v.tiempoSegundos % 60);
        }
        return lines;
    }

    private static String rolAbrev(int id) {
        switch (id) {
            case 1:
                return "CAB";
            case 2:
                return "MAG";
            case 3:
                return "SHT";
            default:
                return "???";
        }
    }

    private DBManager.RunData construirRunData(boolean completada) {
        DBManager.RunData d = new DBManager.RunData();
        d.personajeId = personajeIdDe(player.getRole().tipo);
        d.score = hud.getScore();
        d.tiempoSegundos = (int) tiempoSupervivencia;
        d.nivelAlcanzado = hud.getLevel();
        d.manaTotal = (int) player.getStats().totalManaSpent;
        d.completada = completada;
        d.killsPorTipo = poolEnemigos.getKillsByType();
        d.armasEquipadas = new String[Constants.WEAPON_SLOTS];
        d.inscripcionesEquipadas = new String[Constants.WEAPON_SLOTS];
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            if (w != null) {
                d.armasEquipadas[i] = w.type.name();
                d.inscripcionesEquipadas[i] = (w.inscription != null) ? w.inscription.getName() : null;
            }
        }
        return d;
    }

    private void activarSkill(int slot) {
        Weapon w = player.getWeaponAtSlot(slot);
        if (!(w instanceof WeaponSkill)) return;
        WeaponSkill sk = (WeaponSkill) w;
        if (sk.skillCooldownTimer > 0) return;
        if (!player.consumeMana(sk.manaCost)) {
            hud.showManaInsuficienteFeedback();
            return;
        }
        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touch);
        Vector2 target = new Vector2(touch.x, touch.y);
        sk.activate(player, poolBalas, poolEnemigos, target);
        sk.skillCooldownTimer = sk.skillCooldownBase;
        if (sk.type == WeaponType.MARTILLO_JUICIO || sk.type == WeaponType.TOMO_CAOS) {
            float r = sk.type == WeaponType.MARTILLO_JUICIO ? 200f : 250f;
            bossManager.notifySkillBlast(target.x, target.y, r, sk.type == WeaponType.TOMO_CAOS);
        }
    }

    /**
     * cd_efectivo = max(cd_base × affinityMult / multiplicadorCadencia × (1-afijo%), MIN_WEAPON_CD)
     */
    private float calcCooldownEfectivo(Weapon w) {
        float cd = w.cooldownBase;
        if (w.hasAffinityFor(player.getRole().tipo)) cd *= Constants.WEAPON_AFFINITY_CD_MULT;
        float cadMult = (upgradeManager != null) ? upgradeManager.getAttackSpeedMultiplier() : 1f;
        cd /= cadMult;
        if (w.rolledInstance != null) {
            float pct = w.rolledInstance.getSumStat("reduced_cd_pct")
                + w.rolledInstance.getSumStat("atk_speed_pct");
            if (pct > 0) cd *= (1f - pct / 100f);
        }
        return Math.max(Constants.MIN_WEAPON_CD, cd);
    }

    private static int personajeIdDe(Role.Tipo tipo) {
        switch (tipo) {
            case CABALLERO:
                return 1;
            case MAGO:
                return 2;
            case SHOOTER:
                return 3;
            default:
                return 0;
        }
    }

    private void spawnChest() {
        float angulo = MathUtils.random(MathUtils.PI2);
        float dist = 300f + MathUtils.random(200f);
        chestX = player.position.x + MathUtils.cos(angulo) * dist;
        chestY = player.position.y + MathUtils.sin(angulo) * dist;
        chestWeapon = WeaponDropper.generate(currentDepthForDrops, player.getRole().tipo);
        chestActive = true;
    }

    private void spawnChestAt(float x, float y) {
        if (chestActive) return;
        chestX = x;
        chestY = y;
        chestWeapon = WeaponDropper.generate(currentDepthForDrops, player.getRole().tipo);
        chestActive = true;
        nextChestWave = spawnManager.getWaveCount() +
            MathUtils.random(Constants.CHEST_SPAWN_INTERVAL_MIN, Constants.CHEST_SPAWN_INTERVAL_MAX);
    }

    private void checkChestProximity() {
        if (!chestActive || swapMenuActive || scrollMenuActive) {
            if (!chestActive) hud.clearChestPickupPrompt();
            return;
        }
        float dx = player.position.x - chestX;
        float dy = player.position.y - chestY;
        float proximityR = Constants.WEAPON_PICKUP_RADIUS * 2f;
        if (dx * dx + dy * dy < proximityR * proximityR) {
            hud.setChestPickupPrompt(chestWeapon != null ? specificWeaponName(chestWeapon) : "ARMA");
            if (Gdx.input.isKeyJustPressed(Input.Keys.F) && chestWeapon != null) {
                chestActive = false;
                hud.clearChestPickupPrompt();
                Weapon toPickup = chestWeapon;
                chestWeapon = null;
                triggerWeaponPickup(toPickup);
            }
        } else {
            hud.clearChestPickupPrompt();
        }
    }

    private void renderDrops(SpriteBatch b) {
        for (int i = 0; i < DROP_POOL; i++) {
            if (!dropActive[i]) continue;
            float alpha = dropTimer[i] < 6f ? Math.max(0.15f, dropTimer[i] / 6f) : 1f;
            Color c = tierColor(dropWeapon[i]);
            b.setColor(c.r, c.g, c.b, alpha);
            b.draw(SharedTextures.getWeaponDrop(), dropX[i] - 12, dropY[i] - 12, 24, 24);
            b.setColor(Color.WHITE);
        }
    }

    private void updateDropTimers(float delta) {
        for (int i = 0; i < DROP_POOL; i++) {
            if (!dropActive[i]) continue;
            dropTimer[i] -= delta;
            if (dropTimer[i] <= 0f) {
                dropActive[i] = false;
                continue;
            }
            float dx = player.position.x - dropX[i];
            float dy = player.position.y - dropY[i];
            if (dx * dx + dy * dy > DROP_FADE_DIST * DROP_FADE_DIST) {
                dropActive[i] = false;
            }
        }
    }

    // ── Sprint 4 — Procesado de señales de nuevos enemigos ────────────────────

    private void procesarSeñalesEnemigosSprint4(float delta) {
        com.badlogic.gdx.utils.Array<com.milwar.kaosuarina.entities.Enemy> lista = poolEnemigos.getEnemies();
        for (int i = 0; i < lista.size; i++) {
            com.milwar.kaosuarina.entities.Enemy e = lista.get(i);

            // TANQUE stomp: AoE ground pound damages player
            if (e.stompThisFrame) {
                e.stompThisFrame = false;
                float sd = e.position.dst(player.position);
                if (sd <= com.milwar.kaosuarina.utils.Constants.TANQUE_STOMP_RADIUS) {
                    player.takeDamage(com.milwar.kaosuarina.utils.Constants.TANQUE_STOMP_DMG);
                    damageTakenThisWave = true;
                }
            }

            // SPLITTER: spawn 2 copias al morir
            if (e.pendingSplit) {
                e.pendingSplit = false;
                int splitHp = Math.max(1, (int) (e.maxHealth * 0.40f));
                for (int s = 0; s < 2; s++) {
                    float ox = com.badlogic.gdx.math.MathUtils.random(-30f, 30f);
                    float oy = com.badlogic.gdx.math.MathUtils.random(-30f, 30f);
                    poolEnemigos.spawnAt(e.position.x + ox, e.position.y + oy,
                        com.milwar.kaosuarina.entities.Enemy.Tipo.BASICO);
                }
            }

            if (!e.active) continue;

            // HEALER: cura aliados en radio 150u cada segundo
            if (e.healThisFrame) {
                e.healThisFrame = false;
                for (int j = 0; j < lista.size; j++) {
                    com.milwar.kaosuarina.entities.Enemy ally = lista.get(j);
                    if (!ally.active || ally == e) continue;
                    float dx = ally.position.x - e.position.x;
                    float dy = ally.position.y - e.position.y;
                    float r2 = com.milwar.kaosuarina.utils.Constants.HEALER_HEAL_RADIUS;
                    if (dx * dx + dy * dy <= r2 * r2) {
                        ally.health = Math.min(ally.maxHealth,
                            ally.health + (int) com.milwar.kaosuarina.utils.Constants.HEALER_HEAL_PER_SEC);
                    }
                }
            }

            // ELITE_SUMMON: invocar 3 BASICO
            if (e.summonThisFrame) {
                e.summonThisFrame = false;
                for (int s = 0; s < 3; s++) {
                    float ang = com.badlogic.gdx.math.MathUtils.random(com.badlogic.gdx.math.MathUtils.PI2);
                    poolEnemigos.spawnAt(
                        e.position.x + com.badlogic.gdx.math.MathUtils.cos(ang) * 80f,
                        e.position.y + com.badlogic.gdx.math.MathUtils.sin(ang) * 80f,
                        com.milwar.kaosuarina.entities.Enemy.Tipo.BASICO);
                }
            }

            if (e.slowZoneActive) {
                player.getStats().baseSpeed = player.getStats().baseSpeedBase + player.getStats().amuletSpeedBonus;
                float dx = player.position.x - e.position.x;
                float dy = player.position.y - e.position.y;
                float zr = com.milwar.kaosuarina.utils.Constants.ELITE_ZONE_RADIUS;
                if (dx * dx + dy * dy <= zr * zr) {
                    player.getStats().baseSpeed = player.getStats().baseSpeedBase
                        * com.milwar.kaosuarina.utils.Constants.ELITE_ZONE_SLOW_MULT;
                }
            }

            // FRAGMENTADO: update HUD boss bar
            if (e.tipo == com.milwar.kaosuarina.entities.Enemy.Tipo.FRAGMENTADO) {
                hud.setBossHealth(e.health, e.maxHealth, 3);
            }
        }
    }

    private void procesarLootDrops() {
        for (Enemy e : poolEnemigos.getEnemies()) {
            if (!e.pendingLootDrop) continue;
            e.pendingLootDrop = false;

            float chance = dropChancePor(e.tipo);
            if (MathUtils.random() >= chance) continue;

            for (int i = 0; i < DROP_POOL; i++) {
                if (!dropActive[i]) {
                    dropX[i] = e.position.x;
                    dropY[i] = e.position.y;
                    dropWeapon[i] = WeaponDropper.generate(currentDepthForDrops, player.getRole().tipo);
                    dropActive[i] = true;
                    dropTimer[i] = DROP_LIFETIME;
                    break;
                }
            }
        }
    }

    private float dropChancePor(Enemy.Tipo tipo) {
        switch (tipo) {
            case GUARDIAN:
            case ARQUERO:
            case DEVASTADOR:
                return 1.0f;
            case TANQUE:
            case ESPECTRAL:
                return 0.50f;
            default:
                return 0.12f;
        }
    }

    private void checkPlayerDropCollision() {
        if (swapMenuActive || scrollMenuActive) return;
        for (int i = 0; i < DROP_POOL; i++) {
            if (!dropActive[i]) continue;
            float dx = player.position.x - dropX[i];
            float dy = player.position.y - dropY[i];
            if (dx * dx + dy * dy < Constants.WEAPON_PICKUP_RADIUS * Constants.WEAPON_PICKUP_RADIUS) {
                triggerWeaponPickup(dropWeapon[i]);
                dropActive[i] = false;
                break;
            }
        }
    }

    private Color tierColor(Weapon w) {
        if (w == null || w.tierId == null) return Color.WHITE;
        switch (w.tierId) {
            case "T2":
                return Color.GREEN;
            case "T3":
                return Color.CYAN;
            case "T4":
                return new Color(0.7f, 0.3f, 1f, 1f);
            case "T5":
                return Color.GOLD;
            default:
                return Color.WHITE;
        }
    }

    private void triggerWeaponPickup(Weapon w) {
        if (player.getRole().tipo == com.milwar.kaosuarina.roles.Role.Tipo.SHOOTER
            && w.type == com.milwar.kaosuarina.weapons.WeaponType.PISTOLAS_GEMELAS) return;

        // ── CNT-05 — Evolution check ──────────────────────────────────────────
        if (w.rolledInstance != null) {
            String incomingId = w.rolledInstance.weaponId;
            if (com.milwar.kaosuarina.weapons.WeaponEvolutionCatalog.hasEvolution(incomingId)) {
                for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
                    Weapon eq = player.getWeaponAtSlot(i);
                    if (eq != null && eq.rolledInstance != null
                        && eq.rolledInstance.weaponId.equals(incomingId)) {
                        String resultId = com.milwar.kaosuarina.weapons.WeaponEvolutionCatalog.getEvolution(incomingId);
                        Weapon evolved = com.milwar.kaosuarina.weapons.WeaponDropper.generateById(
                            resultId, Math.min(currentDepthForDrops + 2, 14));
                        if (evolved != null) {
                            evolved.inscription = eq.inscription; // hereda inscripción
                            player.equipWeapon(i, evolved);
                            hud.showEvolutionNotification(specificWeaponName(evolved));
                        }
                        return; // consumed as evolution
                    }
                }
            }
        }

        // Try active slots first
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            if (player.getWeaponAtSlot(i) == null) {
                player.equipWeapon(i, w);
                return;
            }
        }
        // Try storage slots
        if (player.storeWeapon(w)) return;
        // All 6 slots full — offer swap of active slots
        pendingPickupWeapon = w;
        swapMenuActive = true;
        swapMenuTimer = Constants.WEAPON_SWAP_TIMEOUT;
    }

    private void updateSwapMenu(float delta) {
        if (!swapMenuActive) return;
        swapMenuTimer -= delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            player.equipWeapon(0, pendingPickupWeapon);
            closeSwapMenu();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            player.equipWeapon(1, pendingPickupWeapon);
            closeSwapMenu();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.X) || swapMenuTimer <= 0) {
            closeSwapMenu();
        }
    }

    private void closeSwapMenu() {
        swapMenuActive = false;
        pendingPickupWeapon = null;
        swapMenuTimer = 0f;
    }

    // ── S6-02 Scroll de Inscripción ───────────────────────────────────────────

    private void spawnScroll() {
        float angulo = MathUtils.random(MathUtils.PI2);
        float dist = 300f + MathUtils.random(200f);
        scrollX = player.position.x + MathUtils.cos(angulo) * dist;
        scrollY = player.position.y + MathUtils.sin(angulo) * dist;
        scrollActive = true;
    }

    private void spawnScrollAt(float x, float y) {
        if (scrollActive) return;
        scrollX = x;
        scrollY = y;
        scrollActive = true;
        nextScrollWave = spawnManager.getWaveCount() +
            MathUtils.random(Constants.SCROLL_SPAWN_INTERVAL_MIN, Constants.SCROLL_SPAWN_INTERVAL_MAX);
    }

    private void checkPlayerScrollCollision() {
        if (!scrollActive || swapMenuActive || scrollMenuActive) return;
        float dx = player.position.x - scrollX;
        float dy = player.position.y - scrollY;
        if (dx * dx + dy * dy < Constants.SCROLL_PICKUP_RADIUS * Constants.SCROLL_PICKUP_RADIUS) {
            scrollActive = false;
            triggerScrollPickup(com.milwar.kaosuarina.weapons.InscriptionPool.getRandom());
        }
    }

    private void triggerScrollPickup(com.milwar.kaosuarina.weapons.Inscription ins) {
        int occupied = 0;
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            if (player.getWeaponAtSlot(i) != null) occupied++;
        }
        if (occupied == 0) return;
        if (occupied == 1) {
            for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
                if (player.getWeaponAtSlot(i) != null) {
                    player.getWeaponAtSlot(i).inscription = ins;
                    return;
                }
            }
        }
        pendingInscription = ins;
        scrollMenuActive = true;
        scrollMenuTimer = Constants.SCROLL_SWAP_TIMEOUT;
    }

    private void updateScrollMenu(float delta) {
        if (!scrollMenuActive) return;
        scrollMenuTimer -= delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            if (player.getWeaponAtSlot(0) != null) player.getWeaponAtSlot(0).inscription = pendingInscription;
            closeScrollMenu();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            if (player.getWeaponAtSlot(1) != null) player.getWeaponAtSlot(1).inscription = pendingInscription;
            closeScrollMenu();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.X) || scrollMenuTimer <= 0) {
            closeScrollMenu();
        }
    }

    private void closeScrollMenu() {
        scrollMenuActive = false;
        pendingInscription = null;
        scrollMenuTimer = 0f;
    }

    // ── S6-03 Amuletos ────────────────────────────────────────────────────────

    private void spawnAmulet() {
        float angulo = MathUtils.random(MathUtils.PI2);
        float dist = 300f + MathUtils.random(200f);
        amuletX = player.position.x + MathUtils.cos(angulo) * dist;
        amuletY = player.position.y + MathUtils.sin(angulo) * dist;
        amuletType = com.milwar.kaosuarina.items.AmuletPool.getRandom();
        amuletActive = true;
    }

    private void checkPlayerAmuletCollision() {
        if (!amuletActive) return;
        float dx = player.position.x - amuletX;
        float dy = player.position.y - amuletY;
        if (dx * dx + dy * dy < Constants.AMULET_PICKUP_RADIUS * Constants.AMULET_PICKUP_RADIUS) {
            amuletActive = false;
            if (!player.equipAmulet(amuletType)) {
                // Slots llenos → menú de swap (MEC-01)
                pendingAmuletType = amuletType;
                amuletSwapMenuActive = true;
                amuletSwapMenuTimer = Constants.AMULET_SWAP_TIMEOUT;
                amuletSwapSelected = 0;
            }
        }
    }

    private void updateAmuletSwapMenu(float delta) {
        if (!amuletSwapMenuActive) return;
        amuletSwapMenuTimer -= delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
            amuletSwapSelected = (amuletSwapSelected + 2) % 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
            amuletSwapSelected = (amuletSwapSelected + 1) % 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.swapAmulet(amuletSwapSelected, pendingAmuletType);
            closeAmuletSwapMenu();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.X) || amuletSwapMenuTimer <= 0) {
            closeAmuletSwapMenu(); // timeout = descarta el nuevo amuleto
        }
    }

    private void closeAmuletSwapMenu() {
        amuletSwapMenuActive = false;
        pendingAmuletType = null;
        amuletSwapMenuTimer = 0f;
    }

    // ── Relic display en HUD ─────────────────────────────────────────────────

    private void actualizarRelicDisplay() {
        switch (player.getRole().tipo) {
            case CABALLERO:
                hud.setRelicDisplay("Armadura", player.getReliquiaStacks());
                break;
            case SHOOTER: {
                int combo = player.getComboCount();
                hud.setRelicDisplay("Combo", combo);
                if (combo > lastComboCount) {
                    hud.showComboFloater(combo);
                    if (combo == 10) hud.flashComboMax();
                }
                lastComboCount = combo;
                break;
            }
            default:
                hud.setRelicDisplay("", 0);
                lastComboCount = 0;
                break;
        }
    }

    private String specificWeaponName(Weapon w) {
        if (w.rolledInstance != null) {
            String mapped = WEAPON_NAMES.get(w.rolledInstance.weaponId);
            if (mapped != null) return mapped;
        }
        return w.type.name();
    }

    private HUD.WeaponCard buildWeaponCard(Weapon w) {
        if (w == null) return null;
        String name = HUD.formatWeaponName(specificWeaponName(w));
        String dmgType = w.damageType != null ? w.damageType.name() : "FISICO";
        int damage = w.baseDamage;
        String insc = w.inscription != null ? w.inscription.getName() : null;
        boolean matches = w.hasAffinityFor(player.getRole().tipo);
        String affLabel = affinityLabel(w);
        return new HUD.WeaponCard(name, w.tierId, dmgType, damage, insc, matches, affLabel, buildAffixLabel(w));
    }

    private String buildAffixLabel(Weapon w) {
        if (w == null || w.rolledInstance == null) return null;
        StringBuilder sb = new StringBuilder();
        for (com.milwar.kaosuarina.data.RolledAffix a : w.rolledInstance.getAffixes()) {
            if (sb.length() > 0) sb.append("  ");
            switch (a.stat) {
                case "dmg_flat":
                    sb.append(String.format("+%.0f DMG", a.value));
                    break;
                case "dmg_pct":
                    sb.append(String.format("+%.0f%% DMG", a.value));
                    break;
                case "atk_speed_pct":
                    sb.append(String.format("+%.0f%% VEL", a.value));
                    break;
                case "crit_chance":
                    sb.append(String.format("+%.0f%% CRIT", a.value));
                    break;
                case "crit_dmg":
                    sb.append(String.format("+%.0f%% xCRIT", a.value));
                    break;
                case "lifesteal_pct":
                    sb.append(String.format("+%.0f%% VIDA", a.value));
                    break;
                case "add_fire_dmg":
                    sb.append(String.format("+%.0f FUEGO", a.value));
                    break;
                case "add_poison_dmg":
                    sb.append(String.format("+%.0f VEN", a.value));
                    break;
                case "add_chaos_dmg":
                    sb.append(String.format("+%.0f CAOS", a.value));
                    break;
                case "mp_regen":
                    sb.append(String.format("+%.0f MP/s", a.value));
                    break;
                case "reduced_cd_pct":
                    sb.append(String.format("-%.0f%% CD", a.value));
                    break;
                default:
                    break;
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String affinityLabel(Weapon w) {
        if (w.hasAffinityFor(Role.Tipo.CABALLERO)) return "Caballero";
        if (w.hasAffinityFor(Role.Tipo.MAGO)) return "Mago";
        if (w.hasAffinityFor(Role.Tipo.SHOOTER)) return "Shooter";
        return "Neutro";
    }

    private void reiniciar() {
        liberarRecursos();
        inicializar();
    }

    private void liberarRecursos() {
        player.dispose();
        poolBalas.dispose();
        poolEnemigos.dispose();
        poolBalasEnemigas.dispose();
        hud.dispose();
        levelUpScreen.dispose();
        if (shapeRenderer != null) { shapeRenderer.dispose(); shapeRenderer = null; }
        if (batch != null) { batch.dispose(); batch = null; }
    }

    @Override
    public void resize(int w, int h) {
        gameViewport.update(w, h, true);
        hud.resize(w, h);
        levelUpScreen.resize(w, h);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (batch != null) {
            liberarRecursos();
        }
    }
}
