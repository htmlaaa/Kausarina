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
import com.milwar.kaosuarina.utils.ColisionManager;
import com.milwar.kaosuarina.utils.Constants;
import com.milwar.kaosuarina.weapons.Weapon;
import com.milwar.kaosuarina.weapons.WeaponCategory;
import com.milwar.kaosuarina.weapons.WeaponNormal;
import com.milwar.kaosuarina.weapons.WeaponPool;
import com.milwar.kaosuarina.weapons.WeaponSkill;
import com.milwar.kaosuarina.weapons.WeaponType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.List;

public class GameScreen implements Screen {

    private final KaosuarinaGame game;
    private final Role roleInicial;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private Player player;
    private PoolBalas poolBalas;
    private PoolEnemigos poolEnemigos;
    private PoolBalasEnemigas poolBalasEnemigas;

    private HUD hud;
    private UpgradeManager upgradeManager;
    private LevelUpScreen levelUpScreen;

    private BossManager  bossManager;
    private SpawnManager spawnManager;

    private int nivelAnterior;
    private int pendingLevelUps;
    private float aimAngle;
    private boolean leftClickJust;
    private boolean rightClickJust;
    private float   tiempoSupervivencia;
    private boolean runGuardada;
    private boolean gameWon;

    private int     nextChestWave;
    private float   chestX, chestY;
    private boolean chestActive;
    private Weapon  pendingPickupWeapon;
    private boolean swapMenuActive;
    private float   swapMenuTimer;

    // S6-02 — Scroll de Inscripción
    private int     nextScrollWave;
    private float   scrollX, scrollY;
    private boolean scrollActive;
    private com.milwar.kaosuarina.weapons.Inscription pendingInscription;
    private boolean scrollMenuActive;
    private float   scrollMenuTimer;

    // S6-03 — Amuletos
    private int     nextAmuletWave;
    private float   amuletX, amuletY;
    private com.milwar.kaosuarina.items.AmuletType amuletType;
    private boolean amuletActive;
    private boolean damageTakenThisWave;

    public GameScreen(KaosuarinaGame game, Role role) {
        this.game = game;
        this.roleInicial = role;
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

        player = new Player(0, 0, roleInicial);
        if (roleInicial.tipo == Role.Tipo.SHOOTER) {
            player.equipWeapon(0, WeaponPool.get(WeaponType.PISTOLAS_GEMELAS));
        }
        poolBalas = new PoolBalas();
        poolEnemigos = new PoolEnemigos();
        poolBalasEnemigas = new PoolBalasEnemigas();
        hud = new HUD(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        upgradeManager = new UpgradeManager();
        player.setUpgradeManager(upgradeManager);

        levelUpScreen = new LevelUpScreen(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        nivelAnterior = 1;
        pendingLevelUps = 0;
        tiempoSupervivencia = 0f;
        runGuardada = false;
        gameWon = false;

        bossManager  = new BossManager(player, poolEnemigos, hud);
        spawnManager = new SpawnManager(player, poolEnemigos);

        nextChestWave = MathUtils.random(Constants.CHEST_SPAWN_INTERVAL_MIN, Constants.CHEST_SPAWN_INTERVAL_MAX);
        chestActive = false;
        pendingPickupWeapon = null;
        swapMenuActive = false;
        swapMenuTimer = 0f;
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

        procesarInput();
        actualizarJuego(delta);
        procesarColisiones();
        detectarLevelUp();
        renderizar();
    }

    private void procesarLevelUp() {
        levelUpScreen.handleInput();
        Upgrade seleccionado = levelUpScreen.getSelectedUpgrade();
        if (seleccionado != null) {
            int hpBonus = upgradeManager.aplicarUpgrade(seleccionado);
            if (hpBonus > 0) player.aumentarVidaMaxima(hpBonus);
            levelUpScreen.hide();
        }
    }

    private void procesarInput() {
        player.velocity.set(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.velocity.y = player.getVelocidadActual();
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.velocity.y = -player.getVelocidadActual();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.velocity.x = -player.getVelocidadActual();
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.velocity.x = player.getVelocidadActual();

        if (player.velocity.len2() > 0) player.velocity.nor().scl(player.getVelocidadActual());

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        aimAngle = (float) Math.atan2(
            mouseY - Gdx.graphics.getHeight() / 2f,
            mouseX - Gdx.graphics.getWidth() / 2f
        );

        leftClickJust = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        rightClickJust = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
    }

    private void actualizarJuego(float delta) {
        tiempoSupervivencia += delta;
        com.milwar.kaosuarina.weapons.inscriptions.EchoQueue.tick(delta);
        com.milwar.kaosuarina.utils.ParticlePool.update(delta);
        player.update(delta, poolBalas, aimAngle);

        // Manual attacks (Caballero / Mago)
        if (player.getRole().attackMode != com.milwar.kaosuarina.roles.Role.AttackMode.AUTO_SHOOT) {
            if (leftClickJust) recompensarKills(player.triggerLightAttack(poolBalas, aimAngle, poolEnemigos));
            if (rightClickJust) recompensarKills(player.triggerHeavyAttack(poolBalas, aimAngle, poolEnemigos));
        }

        // Weapon auto-fire (S5-04, ADR-006)
        player.updateWeaponCooldowns(delta);
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            if (w != null && w.category == WeaponCategory.NORMAL && w.cooldownTimer <= 0) {
                ((WeaponNormal) w).shoot(player, poolBalas, aimAngle);
                w.cooldownTimer = calcCooldownEfectivo(w);
            }
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
        hud.update(delta);

        // Wave spawning + difficulty scaling
        if (spawnManager.update(delta, hud.getLevel())) {
            onWaveFired();
        }

        hud.setHealth(player.getCurrentHealth(), player.getMaxHealth());
        hud.setMana(player.getStats().mana, player.getStats().maxMana);

        checkPlayerChestCollision();
        updateSwapMenu(delta);
        checkPlayerScrollCollision();
        updateScrollMenu(delta);
        checkPlayerAmuletCollision();

        // Boss lifecycle (delegated to BossManager)
        bossManager.update(delta);
        if (bossManager.damageTakenThisFrame) damageTakenThisWave = true;
        if (bossManager.victoryTriggered)     gameWon = true;
        if (bossManager.guardianDied)         spawnScrollAt(bossManager.guardianDeathX, bossManager.guardianDeathY);
        if (bossManager.arqueroDied)          spawnChestAt(bossManager.arqueroDeathX, bossManager.arqueroDeathY);

        // Amuleto icons HUD (S6-03)
        hud.setAmuletFlags(player.getStats().hasSedDeSangre, player.getStats().hasGuardianArena);

        // Core display HUD (S6-07)
        hud.setCoreDisplay(getCoreDisplayText());

        // Weapon slots HUD con inscripción (S6-07)
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            String insc = (w != null && w.inscription != null) ? w.inscription.getName() : null;
            if (w == null) {
                hud.setWeaponSlot(i, "-", false, 0f, false, null);
            } else if (w instanceof WeaponSkill) {
                WeaponSkill sk = (WeaponSkill) w;
                float frac = sk.skillCooldownBase > 0 ? Math.min(1f, sk.skillCooldownTimer / sk.skillCooldownBase) : 0f;
                boolean locked = player.getStats().mana < sk.manaCost;
                hud.setWeaponSlot(i, w.type.name(), true, frac, locked, insc);
            } else {
                float frac = w.cooldownBase > 0 ? Math.min(1f, w.cooldownTimer / w.cooldownBase) : 0f;
                hud.setWeaponSlot(i, w.type.name(), false, frac, false, insc);
            }
        }
    }

    /** Called by SpawnManager when a wave fires. Handles collectibles and arena bonus. */
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
        recompensarKills(ColisionManager.comprobarBalasVsEnemigos(player, poolBalas, poolEnemigos));
        recompensarKills(ColisionManager.comprobarExplosionesMALDITO(poolEnemigos));

        int contactDmg = ColisionManager.comprobarJugadorVsEnemigos(player, poolEnemigos);
        if (contactDmg > 0) {
            int hpBefore = player.getCurrentHealth();
            player.recibirDanio(contactDmg);
            if (player.getCurrentHealth() < hpBefore) damageTakenThisWave = true;
        }

        int hpBefore = player.getCurrentHealth();
        ColisionManager.comprobarBalasEnemigas(poolBalasEnemigas, player);
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
            pendingLevelUps--;
            AudioManager.playLevelUp();
            levelUpScreen.show(upgradeManager.getUpgradesAleatorios(3));
        }
    }

    private void renderizar() {
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();

        ScreenUtils.clear(0.1f, 0.05f, 0.15f, 1f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Arena boundary
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.4f, 0.25f, 0.6f, 0.7f);
        shapeRenderer.circle(0, 0, Constants.ARENA_RADIUS, 128);
        shapeRenderer.end();

        // Attack effects (sword arc / magic explosion)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        player.renderAttackEffect(shapeRenderer);
        if (chestActive) {
            shapeRenderer.setColor(1f, 0.8f, 0.1f, 1f);
            shapeRenderer.rect(chestX - 16, chestY - 16, 32, 32);
        }
        if (scrollActive) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.8f, 1f);
            shapeRenderer.rect(scrollX - 12, scrollY - 12, 24, 24);
        }
        if (amuletActive) {
            shapeRenderer.setColor(0.6f, 0.1f, 0.9f, 1f);
            shapeRenderer.rect(amuletX - 10, amuletY - 10, 20, 20);
        }
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
        batch.end();

        hud.render(batch);
        if (swapMenuActive && pendingPickupWeapon != null) {
            String s0 = player.getWeaponAtSlot(0) != null ? player.getWeaponAtSlot(0).type.name() : "VACIO";
            String s1 = player.getWeaponAtSlot(1) != null ? player.getWeaponAtSlot(1).type.name() : "VACIO";
            hud.renderSwapMenu(batch, s0, s1, pendingPickupWeapon.type.name(), swapMenuTimer);
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
        DBManager.guardarRun(construirRunData());
        String[] lb         = buildLeaderboard(DBManager.getTop10());
        int      scoreVal   = hud.getScore();
        int      tiempo     = (int) tiempoSupervivencia;
        int      levelVal   = hud.getLevel();
        int      pid        = personajeIdDe(player.getRole().tipo);
        String   armas      = hud.getArmasText();
        int[]    kills      = poolEnemigos.getKillsByType().clone();
        int      waves      = spawnManager.getWaveCount();
        liberarRecursos();
        game.setScreen(new GameOverScreen(game, scoreVal, tiempo, levelVal, waves, pid, armas, kills, lb));
    }

    private void renderVictoria() {
        if (runGuardada) return;
        runGuardada = true;
        DBManager.guardarRun(construirRunData());
        String[] lb       = buildLeaderboard(DBManager.getTop10());
        int      scoreVal = hud.getScore();
        int      tiempo   = (int) tiempoSupervivencia;
        int      levelVal = hud.getLevel();
        int      pid      = personajeIdDe(player.getRole().tipo);
        String   armas    = hud.getArmasText();
        int      waves    = spawnManager.getWaveCount();
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
            case 1:  return "CAB";
            case 2:  return "MAG";
            case 3:  return "SHT";
            default: return "???";
        }
    }

    private DBManager.RunData construirRunData() {
        DBManager.RunData d = new DBManager.RunData();
        d.personajeId    = personajeIdDe(player.getRole().tipo);
        d.score          = hud.getScore();
        d.tiempoSegundos = (int) tiempoSupervivencia;
        d.nivelAlcanzado = hud.getLevel();
        d.manaTotal      = (int) player.getStats().manaGastadoTotal;
        d.killsPorTipo   = poolEnemigos.getKillsByType();
        d.reliquiaId     = personajeIdDe(player.getRole().tipo);

        List<Upgrade> aplicados = upgradeManager.getUpgradesAplicados();
        d.upgradesTipos   = new String[aplicados.size()];
        d.upgradesNiveles = new int[aplicados.size()];
        for (int i = 0; i < aplicados.size(); i++) {
            d.upgradesTipos[i]   = aplicados.get(i).tipo.name();
            d.upgradesNiveles[i] = aplicados.get(i).nivel;
        }

        d.armasEquipadas         = new String[Constants.WEAPON_SLOTS];
        d.inscripcionesEquipadas = new String[Constants.WEAPON_SLOTS];
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            if (w != null) {
                d.armasEquipadas[i]         = w.type.name();
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

    /** cd_efectivo = max(cd_base × affinityMult / multiplicadorCadencia, MIN_WEAPON_CD) */
    private float calcCooldownEfectivo(Weapon w) {
        float cd = w.cooldownBase;
        if (w.hasAffinityFor(player.getRole().tipo)) cd *= Constants.WEAPON_AFFINITY_CD_MULT;
        float cadMult = (upgradeManager != null) ? upgradeManager.getMultiplicadorCadencia() : 1f;
        return Math.max(Constants.MIN_WEAPON_CD, cd / cadMult);
    }

    private static int personajeIdDe(Role.Tipo tipo) {
        switch (tipo) {
            case CABALLERO: return 1;
            case MAGO:      return 2;
            case SHOOTER:   return 3;
            default:        return 0;
        }
    }

    private void spawnChest() {
        float angulo = MathUtils.random(MathUtils.PI2);
        float dist = 300f + MathUtils.random(200f);
        chestX = player.position.x + MathUtils.cos(angulo) * dist;
        chestY = player.position.y + MathUtils.sin(angulo) * dist;
        chestActive = true;
    }

    private void spawnChestAt(float x, float y) {
        if (chestActive) return;
        chestX = x;
        chestY = y;
        chestActive = true;
        nextChestWave = spawnManager.getWaveCount() +
            MathUtils.random(Constants.CHEST_SPAWN_INTERVAL_MIN, Constants.CHEST_SPAWN_INTERVAL_MAX);
    }

    private void checkPlayerChestCollision() {
        if (!chestActive) return;
        float dx = player.position.x - chestX;
        float dy = player.position.y - chestY;
        if (dx * dx + dy * dy < Constants.WEAPON_PICKUP_RADIUS * Constants.WEAPON_PICKUP_RADIUS) {
            chestActive = false;
            triggerWeaponPickup(WeaponPool.getRandom());
        }
    }

    private void triggerWeaponPickup(Weapon w) {
        for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
            if (player.getWeaponAtSlot(i) == null) {
                player.equipWeapon(i, w);
                return;
            }
        }
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
        if (!scrollActive) return;
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
            aplicarAmuleto(amuletType);
        }
    }

    private void aplicarAmuleto(com.milwar.kaosuarina.items.AmuletType t) {
        switch (t) {
            case SED_DE_SANGRE:
                player.getStats().hasSedDeSangre = true;
                break;
            case GUARDIAN_DE_LA_ARENA:
                player.getStats().hasGuardianArena = true;
                break;
        }
    }

    // ── S6-07 Core display text ───────────────────────────────────────────────

    private String getCoreDisplayText() {
        switch (player.getRole().tipo) {
            case CABALLERO: return "ARM " + player.getReliquiaStacks();
            case SHOOTER:   return "CMB " + player.getComboCount();
            default:        return "";
        }
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
        shapeRenderer.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int w, int h) {
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
