package com.milwar.kaosuarina.data;

import com.badlogic.gdx.Gdx;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.milwar.kaosuarina.data.model.*;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Singleton that loads all JSON catalogs at startup.
 * Call DataManager.getInstance() after LibGDX is initialized (inside create() or show()).
 */
public class DataManager {

    private static DataManager instance;

    private static final Gson GSON = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    // Weapons catalogs
    private final Map<String, CharacterData>      characters    = new HashMap<>();
    private final Map<String, WeaponTypeData>      weaponTypes   = new HashMap<>();
    private final Map<String, DamageTypeData>      damageTypes   = new HashMap<>();
    private final Map<String, TierData>            tiers         = new HashMap<>();
    private final Map<String, WeaponData>          weapons       = new HashMap<>();
    private final Map<String, SkillData>           skills        = new HashMap<>();
    private final Map<String, EffectData>          effects       = new HashMap<>();
    private final Map<String, WeaponAffixData>     affixes       = new HashMap<>();
    private final List<WeaponAffixData>            affixList     = new ArrayList<>();
    private final Map<String, PlayerAttributeData> playerAttribs = new HashMap<>();

    // Enemy catalogs
    private final Map<String, EnemyData>           enemies          = new HashMap<>();
    private final List<EnemyResistanceData>        resistances      = new ArrayList<>();
    private final Map<String, EnemyAttackData>     enemyAttacks     = new HashMap<>();
    private final List<EnemyAttackPoolData>        attackPool       = new ArrayList<>();
    private final List<EnemyImmunityData>          immunities       = new ArrayList<>();
    private final List<BossPhaseData>              bossPhases       = new ArrayList<>();
    private final List<LootTableEntryData>         lootTableEntries = new ArrayList<>();
    private final Map<String, EnemyLootData>       enemyLoot        = new HashMap<>();
    private final List<DepthScalingData>           depthScaling     = new ArrayList<>();

    private DataManager() {
        loadWeapons();
        loadEnemies();
    }

    public static DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    // -------------------------------------------------------------------------
    // Internal loaders
    // -------------------------------------------------------------------------

    private static final String W = "game_data_json/weapons/";
    private static final String E = "game_data_json/enemies/";

    private void loadWeapons() {
        for (CharacterData c : load(W + "characters.json", CharacterData.class))
            characters.put(c.charId, c);
        for (WeaponTypeData wt : load(W + "weapontypes.json", WeaponTypeData.class))
            weaponTypes.put(wt.wtypeId, wt);
        for (DamageTypeData dt : load(W + "damagetypes.json", DamageTypeData.class))
            damageTypes.put(dt.dmgId, dt);
        for (TierData t : load(W + "tiers.json", TierData.class))
            tiers.put(t.tierId, t);
        for (WeaponData w : load(W + "weapons.json", WeaponData.class))
            weapons.put(w.weaponId, w);
        for (SkillData s : load(W + "skills.json", SkillData.class))
            skills.put(s.skillId, s);
        for (EffectData e : load(W + "effects.json", EffectData.class))
            effects.put(e.effectId, e);
        List<WeaponAffixData> ax = load(W + "weaponaffixes.json", WeaponAffixData.class);
        affixList.addAll(ax);
        for (WeaponAffixData a : ax) affixes.put(a.affixId, a);
        for (PlayerAttributeData pa : load(W + "playerattributes.json", PlayerAttributeData.class))
            playerAttribs.put(pa.attrId, pa);
    }

    private void loadEnemies() {
        for (EnemyData e : load(E + "enemies.json", EnemyData.class))
            enemies.put(e.enemyId, e);
        resistances.addAll(load(E + "enemyresistances.json", EnemyResistanceData.class));
        for (EnemyAttackData ea : load(E + "enemyattacks.json", EnemyAttackData.class))
            enemyAttacks.put(ea.attackId, ea);
        attackPool.addAll(load(E + "enemyattackpool.json", EnemyAttackPoolData.class));
        immunities.addAll(load(E + "enemyimmunities.json", EnemyImmunityData.class));
        bossPhases.addAll(load(E + "bossphases.json", BossPhaseData.class));
        lootTableEntries.addAll(load(E + "loottables.json", LootTableEntryData.class));
        for (EnemyLootData el : load(E + "enemyloot.json", EnemyLootData.class))
            enemyLoot.put(el.enemyId, el);
        depthScaling.addAll(load(E + "depthscaling.json", DepthScalingData.class));
    }

    private <T> List<T> load(String path, Class<T> clazz) {
        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        try (InputStreamReader r = new InputStreamReader(Gdx.files.internal(path).read(), "UTF-8")) {
            return GSON.fromJson(r, type);
        } catch (Exception e) {
            Gdx.app.error("DataManager", "Error cargando " + path + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // -------------------------------------------------------------------------
    // Getters — weapons
    // -------------------------------------------------------------------------

    public WeaponData          getWeapon(String id)       { return weapons.get(id); }
    public TierData            getTier(String id)         { return tiers.get(id); }
    public WeaponAffixData     getAffix(String id)        { return affixes.get(id); }
    public List<WeaponAffixData> getAffixList()           { return Collections.unmodifiableList(affixList); }
    public SkillData           getSkill(String id)        { return skills.get(id); }
    public EffectData          getEffect(String id)       { return effects.get(id); }
    public CharacterData       getCharacter(String id)    { return characters.get(id); }
    public WeaponTypeData      getWeaponType(String id)   { return weaponTypes.get(id); }
    public DamageTypeData      getDamageType(String id)   { return damageTypes.get(id); }
    public PlayerAttributeData getPlayerAttrib(String id) { return playerAttribs.get(id); }
    public Collection<WeaponData> getAllWeapons()         { return Collections.unmodifiableCollection(weapons.values()); }
    public Collection<TierData>   getAllTiers()           { return Collections.unmodifiableCollection(tiers.values()); }

    // -------------------------------------------------------------------------
    // Getters — enemies
    // -------------------------------------------------------------------------

    public EnemyData      getEnemy(String id)        { return enemies.get(id); }
    public EnemyAttackData getEnemyAttack(String id) { return enemyAttacks.get(id); }
    public EnemyLootData  getEnemyLoot(String id)    { return enemyLoot.get(id); }

    public int getResistPct(String enemyId, String dmgId) {
        for (EnemyResistanceData r : resistances)
            if (enemyId.equals(r.enemyId) && dmgId.equals(r.dmgId)) return r.resistPct;
        return 0;
    }

    public boolean isImmune(String enemyId, String effectId) {
        for (EnemyImmunityData i : immunities)
            if (enemyId.equals(i.enemyId) && effectId.equals(i.effectId)) return true;
        return false;
    }

    public List<BossPhaseData> getPhasesFor(String enemyId) {
        List<BossPhaseData> result = new ArrayList<>();
        for (BossPhaseData p : bossPhases)
            if (enemyId.equals(p.enemyId)) result.add(p);
        return result;
    }

    public List<EnemyAttackPoolData> getAttackPoolFor(String enemyId) {
        List<EnemyAttackPoolData> result = new ArrayList<>();
        for (EnemyAttackPoolData ap : attackPool)
            if (enemyId.equals(ap.enemyId)) result.add(ap);
        return result;
    }

    public List<LootTableEntryData> getLootTable(String lootTableId) {
        List<LootTableEntryData> result = new ArrayList<>();
        for (LootTableEntryData lt : lootTableEntries)
            if (lootTableId.equals(lt.lootTableId)) result.add(lt);
        return result;
    }

    public DepthScalingData getDepthScaling(int depth) {
        for (DepthScalingData d : depthScaling)
            if (d.depth == depth) return d;
        return null;
    }
}
