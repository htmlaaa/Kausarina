# Sprint — BossSelector + 2 Bosses Nuevos
> Implementación del sistema de selección dinámica de jefes basado en el build del jugador

---

## ÍNDICE
1. [Resumen del sistema](#1-resumen-del-sistema)
2. [Archivos a crear](#2-archivos-a-crear)
3. [Archivos a modificar](#3-archivos-a-modificar)
4. [Datos JSON nuevos](#4-datos-json-nuevos)
5. [Tareas paso a paso](#5-tareas-paso-a-paso)
6. [Comportamiento en ColisionManager](#6-comportamiento-en-colisionmanager)

---

## 1. RESUMEN DEL SISTEMA

**Idea**: En vez de spawnear siempre el mismo boss en cada boss-wave, el `SpawnManager`
consulta al `BossPathfinder` — éste analiza el build actual del jugador (qué upgrades tiene,
qué armas lleva) y elige el boss que más contrarresta esa build, dentro de un límite para
que no sea injusto.

**Flujo**:
```
SpawnManager.spawnBossesForWave()
  → construye PlayerBuild desde UpgradeManager + equippedWeapons
  → llama BossPathfinder.selectBoss(pool, build)
  → recibe el Boss elegido → spawna el Enemy.Tipo correspondiente
```

**El techo anti-imposible** (ya en el código original): si un boss contrarrestaría más del
70% del build, se descarta. Así nunca se crea una situación sin salida.

---

## 2. ARCHIVOS A CREAR

### 2.1 `BuildTag.java`
**Paquete**: `com.milwar.kaosuarina.systems`

```java
package com.milwar.kaosuarina.systems;

public enum BuildTag {
    LIFESTEAL,  // upgrade VAMPIRISMO + inscripcion vampirica
    FIRE,       // upgrade FILO_IGNEO + inscripcion ignea
    POISON,     // upgrade CUCHILLA_VENENO
    PHYSICAL,   // armas fisicas equipadas
    MAGIC,      // armas magicas equipadas
    BLEED       // (reservado para upgrades futuros)
}
```

---

### 2.2 `BossImmunityTag.java`
**Paquete**: `com.milwar.kaosuarina.systems`

```java
package com.milwar.kaosuarina.systems;

public enum BossImmunityTag {
    LIFESTEAL_IMMUNE,   // no-muertos, constructos
    FIRE_IMMUNE,
    POISON_IMMUNE,
    BLEED_IMMUNE,
    MAGIC_SHIELD        // escudo magico (SEÑOR_DEMONIO futuro)
}
```

---

### 2.3 `PlayerBuild.java`
**Paquete**: `com.milwar.kaosuarina.systems`

Construye el perfil del build leyendo del `UpgradeManager` y los `equippedWeapons` del jugador.

```java
package com.milwar.kaosuarina.systems;

import com.milwar.kaosuarina.entities.Player;
import com.milwar.kaosuarina.weapons.Weapon;
import com.milwar.kaosuarina.weapons.WeaponType;
import java.util.*;

public class PlayerBuild {

    private final Map<BuildTag, Integer> tagWeights = new EnumMap<>(BuildTag.class);

    /** Construye el perfil leyendo el estado actual del jugador */
    public static PlayerBuild fromPlayer(Player player) {
        PlayerBuild build = new PlayerBuild();
        var um = player.getUpgradeManager();

        // LIFESTEAL — upgrade VAMPIRISMO (binario nivel 0/1) + inscripcion
        int lifeWeight = 0;
        if (um.getNivel(Upgrade.Tipo.VAMPIRISMO) > 0)    lifeWeight += 6;
        if (player.tieneInscripcionVampirica())           lifeWeight += 3;
        if (lifeWeight > 0) build.addTag(BuildTag.LIFESTEAL, lifeWeight);

        // FIRE — upgrade FILO_IGNEO + inscripcion ignea
        int fireWeight = 0;
        if (um.getNivel(Upgrade.Tipo.FILO_IGNEO) > 0)    fireWeight += 6;
        if (player.tieneInscripcionIgnea())               fireWeight += 3;
        if (fireWeight > 0) build.addTag(BuildTag.FIRE, fireWeight);

        // POISON — upgrade CUCHILLA_VENENO
        if (um.getNivel(Upgrade.Tipo.CUCHILLA_VENENO) > 0)
            build.addTag(BuildTag.POISON, 6);

        // PHYSICAL — armas fisicas equipadas
        for (int i = 0; i < 2; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            if (w != null && w.type.category == WeaponCategory.MELEE)
                build.addTag(BuildTag.PHYSICAL, build.getWeight(BuildTag.PHYSICAL) + 4);
        }

        // MAGIC — armas magicas equipadas
        for (int i = 0; i < 2; i++) {
            Weapon w = player.getWeaponAtSlot(i);
            if (w != null && w.type.category == WeaponCategory.MAGIC)
                build.addTag(BuildTag.MAGIC, build.getWeight(BuildTag.MAGIC) + 4);
        }

        return build;
    }

    public void addTag(BuildTag tag, int weight) {
        tagWeights.put(tag, Math.min(10, Math.max(1, weight)));
    }

    public int getWeight(BuildTag tag) {
        return tagWeights.getOrDefault(tag, 0);
    }

    public Map<BuildTag, Integer> getTagWeights() {
        return Collections.unmodifiableMap(tagWeights);
    }
}
```

**Métodos a añadir en `Player.java`** para que el código anterior compile:
```java
// Devuelve true si algún arma equipada (slot 0-1) tiene InscripcionVampirica
public boolean tieneInscripcionVampirica() {
    for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
        Weapon w = equippedWeapons[i];
        if (w != null && w.inscription instanceof InscripcionVampirica) return true;
    }
    return false;
}

// Idem para InscripcionIgnea
public boolean tieneInscripcionIgnea() {
    for (int i = 0; i < Constants.WEAPON_SLOTS; i++) {
        Weapon w = equippedWeapons[i];
        if (w != null && w.inscription instanceof InscripcionIgnea) return true;
    }
    return false;
}
```

---

### 2.4 `BossProfile.java`
**Paquete**: `com.milwar.kaosuarina.systems`

Envuelve un `Enemy.Tipo` con sus inmunidades para que el Pathfinder pueda trabajar con él.

```java
package com.milwar.kaosuarina.systems;

import com.milwar.kaosuarina.entities.Enemy;
import java.util.*;

public class BossProfile {

    public final Enemy.Tipo tipo;
    public final int baseChallenge;   // 1-10: dificultad base
    final Set<BossImmunityTag> immunities;

    public BossProfile(Enemy.Tipo tipo, int baseChallenge, BossImmunityTag... tags) {
        this.tipo = tipo;
        this.baseChallenge = baseChallenge;
        this.immunities = tags.length > 0
            ? EnumSet.copyOf(Arrays.asList(tags))
            : EnumSet.noneOf(BossImmunityTag.class);
    }
}
```

---

### 2.5 `BossPathfinder.java`
**Paquete**: `com.milwar.kaosuarina.systems`

Motor de selección. Adaptación directa del demo, limpiando el bug del constructor.

```java
package com.milwar.kaosuarina.systems;

import com.badlogic.gdx.math.MathUtils;
import java.util.*;

public class BossPathfinder {

    private static final Map<BuildTag, BossImmunityTag> COUNTER_MAP =
            new EnumMap<>(BuildTag.class);
    static {
        COUNTER_MAP.put(BuildTag.LIFESTEAL, BossImmunityTag.LIFESTEAL_IMMUNE);
        COUNTER_MAP.put(BuildTag.FIRE,      BossImmunityTag.FIRE_IMMUNE);
        COUNTER_MAP.put(BuildTag.POISON,    BossImmunityTag.POISON_IMMUNE);
        COUNTER_MAP.put(BuildTag.BLEED,     BossImmunityTag.BLEED_IMMUNE);
        COUNTER_MAP.put(BuildTag.MAGIC,     BossImmunityTag.MAGIC_SHIELD);
    }

    private static final float MAX_COUNTER = 0.70f;
    private static final float MIN_COUNTER = 0.20f;

    public float computeCounterScore(BossProfile boss, PlayerBuild build) {
        Map<BuildTag, Integer> weights = build.getTagWeights();
        int total = 0;
        for (int w : weights.values()) total += w;
        if (total == 0) return 0f;

        int countered = 0;
        for (Map.Entry<BuildTag, Integer> e : weights.entrySet()) {
            BossImmunityTag immunity = COUNTER_MAP.get(e.getKey());
            if (immunity != null && boss.immunities.contains(immunity))
                countered += e.getValue();
        }
        return Math.min((float) countered / total, MAX_COUNTER);
    }

    /**
     * Elige el boss con más counter al build, sin superar el techo.
     * Si ninguno llega al mínimo, devuelve uno aleatorio del pool.
     */
    public BossProfile selectBoss(List<BossProfile> pool, PlayerBuild build) {
        BossProfile best = null;
        float bestScore = -1f;

        for (BossProfile b : pool) {
            float score = computeCounterScore(b, build);
            if (score < MIN_COUNTER) continue;
            // Desempate: si mismo score, prefiere el de menor dificultad base
            if (score > bestScore || (score == bestScore && best != null
                    && b.baseChallenge < best.baseChallenge)) {
                best = b;
                bestScore = score;
            }
        }
        return best != null ? best : pool.get(MathUtils.random(pool.size() - 1));
    }
}
```

---

## 3. ARCHIVOS A MODIFICAR

### 3.1 `Enemy.java` — Añadir 2 tipos nuevos al enum
**Línea actual** (enum Tipo):
```java
public enum Tipo {BASICO, RAPIDO, TANQUE, SHOOTER, MALDITO, ESPECTRAL, GUARDIAN, ARQUERO, DEVASTADOR}
```
**Después**:
```java
public enum Tipo {BASICO, RAPIDO, TANQUE, SHOOTER, MALDITO, ESPECTRAL, GUARDIAN, ARQUERO, DEVASTADOR, CONSTRUCTO, QUIMERA}
```

También añadir los bloques de inicialización (igual que GUARDIAN/ARQUERO):
```java
// En el switch de cargarDatosDesdeJSON() o donde se mapea enemy_id → Tipo:
case "EN_BOSS_CONSTRUCT": return Tipo.CONSTRUCTO;
case "EN_BOSS_CHIMERA":   return Tipo.QUIMERA;
```

---

### 3.2 `SpawnManager.java` — Añadir pool y lógica de selección

**Paso 1**: Añadir campo pool y pathfinder al `SpawnManager`:
```java
private final BossPathfinder pathfinder = new BossPathfinder();

// Pool de TODOS los bosses disponibles con sus inmunidades declaradas
private final List<BossProfile> BOSS_POOL = Arrays.asList(
    new BossProfile(Enemy.Tipo.GUARDIAN,   4, BossImmunityTag.LIFESTEAL_IMMUNE),
    new BossProfile(Enemy.Tipo.ARQUERO,    5, BossImmunityTag.POISON_IMMUNE),
    new BossProfile(Enemy.Tipo.DEVASTADOR, 9, BossImmunityTag.LIFESTEAL_IMMUNE,
                                              BossImmunityTag.FIRE_IMMUNE),
    new BossProfile(Enemy.Tipo.CONSTRUCTO, 5, BossImmunityTag.LIFESTEAL_IMMUNE,
                                              BossImmunityTag.BLEED_IMMUNE),
    new BossProfile(Enemy.Tipo.QUIMERA,    7, BossImmunityTag.POISON_IMMUNE,
                                              BossImmunityTag.FIRE_IMMUNE)
);
```

**Paso 2**: Reemplazar el `spawnBossesForWave()` fijo por selección dinámica en los boss-waves:

```java
private void spawnBossesForWave() {
    // --- Guardian wave (sin cambios, sigue siendo fijo como miniboss de oleada) ---
    if (waveCount % Constants.MINIBOSS_WAVE_INTERVAL == 0) {
        spawnByTipo(Enemy.Tipo.GUARDIAN, 600f);
        AudioManager.playBoss();
    }

    // --- Boss wave principal: selección dinámica ---
    if (waveCount % Constants.DEVASTADOR_FINAL_WAVE == 0 && waveCount > 0) {
        PlayerBuild build = PlayerBuild.fromPlayer(player);
        // Solo candidatos de dificultad ≥ 5 para la boss wave principal
        List<BossProfile> candidates = BOSS_POOL.stream()
                .filter(b -> b.baseChallenge >= 5)
                .collect(Collectors.toList());
        BossProfile chosen = pathfinder.selectBoss(candidates, build);
        spawnByTipo(chosen.tipo, 700f);
        AudioManager.playBoss();
    }
}

private void spawnByTipo(Enemy.Tipo tipo, float radius) {
    float angle = MathUtils.random(MathUtils.PI2);
    float x = player.position.x + MathUtils.cos(angle) * radius;
    float y = player.position.y + MathUtils.sin(angle) * radius;
    switch (tipo) {
        case GUARDIAN:   poolEnemigos.spawnGuardian(x, y);   break;
        case ARQUERO:    poolEnemigos.spawnArquero(x, y);    break;
        case DEVASTADOR: poolEnemigos.spawnDevastador(x, y); break;
        case CONSTRUCTO: poolEnemigos.spawnConstructo(x, y); break;
        case QUIMERA:    poolEnemigos.spawnQuimera(x, y);    break;
    }
}
```

---

### 3.3 `ColisionManager.java` — Comportamiento de los 2 bosses nuevos

Ver sección 6 para el detalle completo de IA.

Añadir cases en `calcularDanioContacto()`:
```java
case CONSTRUCTO: return Constants.CONSTRUCTO_CONTACT_DMG;  // añadir = 16
case QUIMERA:    return Constants.QUIMERA_CONTACT_DMG;     // añadir = 22
```

Añadir en el bloque de daño especial vs tipos (donde está la lógica del GUARDIAN):
```java
// CONSTRUCTO: inmune a lifesteal, vulnerable a magia/caos
if (enemy.tipo == Enemy.Tipo.CONSTRUCTO && tipo == DamageType.MAGICO)
    return Math.max(1, Math.round(raw * 2.0f));  // 2x daño mágico
if (enemy.tipo == Enemy.Tipo.CONSTRUCTO && tipo == DamageType.CAOS)
    return Math.max(1, Math.round(raw * 2.5f));  // caos destruye su escudo

// QUIMERA: vulnerable a fisico/caos, sus resistencias van en JSON
if (enemy.tipo == Enemy.Tipo.QUIMERA && tipo == DamageType.FISICO)
    return Math.max(1, Math.round(raw * 1.5f));
```

---

### 3.4 `PoolEnemigos.java` — Métodos de spawn nuevos

```java
public void spawnConstructo(float x, float y) {
    Enemy e = obtenerEnemigo();
    e.inicializar(x, y, Enemy.Tipo.CONSTRUCTO, currentDepth);
    enemigosActivos.add(e);
}

public void spawnQuimera(float x, float y) {
    Enemy e = obtenerEnemigo();
    e.inicializar(x, y, Enemy.Tipo.QUIMERA, currentDepth);
    enemigosActivos.add(e);
}
```

---

### 3.5 `Constants.java` — Añadir constantes

```java
public static final int CONSTRUCTO_CONTACT_DMG = 16;
public static final int QUIMERA_CONTACT_DMG    = 22;
```

---

## 4. DATOS JSON NUEVOS

### 4.1 `enemies.json` — 2 entradas nuevas

```json
{
  "enemy_id": "EN_BOSS_CONSTRUCT",
  "name": "Constructo de Acero",
  "enemy_class": "boss",
  "family": "construct",
  "ai_behavior": "boss_scripted",
  "hp": 1400,
  "dmg_min": 14,
  "dmg_max": 20,
  "primary_dmg_id": "DMG_PHYSICAL",
  "move_speed": 2.1,
  "attack_range": 3,
  "attack_cooldown_s": 1.8,
  "aggro_radius": 99,
  "xp_reward": 600,
  "gold_reward": 350,
  "sprite_id": "spr_constructo",
  "description": "Constructo mecanico. Inmune a sangrado y lifesteal. Escudo fisico, vulnerable a magia y caos."
},
{
  "enemy_id": "EN_BOSS_CHIMERA",
  "name": "Quimera Abismal",
  "enemy_class": "boss",
  "family": "aberration",
  "ai_behavior": "boss_scripted",
  "hp": 1800,
  "dmg_min": 18,
  "dmg_max": 28,
  "primary_dmg_id": "DMG_MAGIC",
  "move_speed": 2.8,
  "attack_range": 4,
  "attack_cooldown_s": 1.4,
  "aggro_radius": 99,
  "xp_reward": 700,
  "gold_reward": 400,
  "sprite_id": "spr_quimera",
  "description": "Criatura abisal. Inmune a veneno y fuego. 3 fases: cuerpo a cuerpo, ranged, caos total."
}
```

---

### 4.2 `bossphases.json` — Fases de ambos bosses

**Constructo de Acero (2 fases):**
```json
{ "enemy_id": "EN_BOSS_CONSTRUCT", "phase": 1,
  "hp_threshold_pct": 100, "name": "Blindado",
  "behavior_change": "normal",
  "unlocked_attacks": "EA_CONSTRUCT_SLAM;EA_CONSTRUCT_SHIELD",
  "speed_mult": 1.0, "dmg_mult": 1.0,
  "description": "Escudo fisico activo: redirige 80% del daño fisico al escudo (300 HP separados)" },

{ "enemy_id": "EN_BOSS_CONSTRUCT", "phase": 2,
  "hp_threshold_pct": 50, "name": "Nucleo Expuesto",
  "behavior_change": "enrage;aoe_phase",
  "unlocked_attacks": "EA_CONSTRUCT_SLAM;EA_CONSTRUCT_LASER",
  "speed_mult": 1.3, "dmg_mult": 1.5,
  "description": "El escudo se destruye. Vulnerable a todo. Gana laser de caos." }
```

**Quimera Abismal (3 fases):**
```json
{ "enemy_id": "EN_BOSS_CHIMERA", "phase": 1,
  "hp_threshold_pct": 100, "name": "Bestia Hambrienta",
  "behavior_change": "normal",
  "unlocked_attacks": "EA_CHIMERA_SLASH;EA_CHIMERA_CHARGE",
  "speed_mult": 1.0, "dmg_mult": 1.0,
  "description": "Cuerpo a cuerpo. Alterna velocidad lenta (80) y rapida (280) cada 6s." },

{ "enemy_id": "EN_BOSS_CHIMERA", "phase": 2,
  "hp_threshold_pct": 60, "name": "Forma Ranged",
  "behavior_change": "aoe_phase",
  "unlocked_attacks": "EA_CHIMERA_BOLT;EA_CHIMERA_BURST",
  "speed_mult": 0.8, "dmg_mult": 1.2,
  "description": "Pasa a ranged. Orbita al jugador, dispara rafagas de 3 proyectiles." },

{ "enemy_id": "EN_BOSS_CHIMERA", "phase": 3,
  "hp_threshold_pct": 30, "name": "Forma Caotica",
  "behavior_change": "enrage;summon;final_phase",
  "unlocked_attacks": "EA_CHIMERA_BOLT;EA_CHIMERA_BURST;EA_CHIMERA_NOVA",
  "speed_mult": 1.4, "dmg_mult": 1.6,
  "description": "Combina melee y ranged. Invoca 2 EN_RAPIDO cada 10s. Nova de caos al 10% HP." }
```

---

### 4.3 `enemyresistances.json` — Añadir filas

```json
{ "enemy_id": "EN_BOSS_CONSTRUCT", "dmg_id": "DMG_PHYSICAL", "resist_pct": 60,
  "notes": "Armadura de hierro" },
{ "enemy_id": "EN_BOSS_CONSTRUCT", "dmg_id": "DMG_RANGED",   "resist_pct": 40,
  "notes": "Placas absorbentes" },
{ "enemy_id": "EN_BOSS_CONSTRUCT", "dmg_id": "DMG_MAGIC",    "resist_pct": -50,
  "notes": "Nucleo electrico vulnerable" },
{ "enemy_id": "EN_BOSS_CONSTRUCT", "dmg_id": "DMG_CHAOS",    "resist_pct": -80,
  "notes": "El caos destruye su escudo directamente" },

{ "enemy_id": "EN_BOSS_CHIMERA", "dmg_id": "DMG_POISON",  "resist_pct": 100,
  "notes": "Inmune total a veneno" },
{ "enemy_id": "EN_BOSS_CHIMERA", "dmg_id": "DMG_FIRE",    "resist_pct": 100,
  "notes": "Inmune total a fuego" },
{ "enemy_id": "EN_BOSS_CHIMERA", "dmg_id": "DMG_PHYSICAL","resist_pct": -25,
  "notes": "Carne abisal, debil a corte" }
```

---

### 4.4 `enemyimmunities.json` — Añadir filas

```json
{ "enemy_id": "EN_BOSS_CONSTRUCT", "effect_id": "EFF_BLEED",
  "notes": "Constructo sin sangre" },
{ "enemy_id": "EN_BOSS_CONSTRUCT", "effect_id": "EFF_POISON",
  "notes": "Sin sistema biologico" },
{ "enemy_id": "EN_BOSS_CONSTRUCT", "effect_id": "EFF_STUN",
  "notes": "Inmune a CC" },

{ "enemy_id": "EN_BOSS_CHIMERA", "effect_id": "EFF_BURN",
  "notes": "Inmune al fuego" },
{ "enemy_id": "EN_BOSS_CHIMERA", "effect_id": "EFF_POISON",
  "notes": "Adaptada al veneno" },
{ "enemy_id": "EN_BOSS_CHIMERA", "effect_id": "EFF_STUN",
  "notes": "Inmune a CC" }
```

---

## 5. TAREAS PASO A PASO

| # | Tarea | Archivos | Esfuerzo |
|---|-------|----------|----------|
| 1 | Crear `BuildTag.java` | nuevo | S |
| 2 | Crear `BossImmunityTag.java` | nuevo | S |
| 3 | Crear `BossProfile.java` | nuevo | S |
| 4 | Crear `BossPathfinder.java` | nuevo | M |
| 5 | Crear `PlayerBuild.java` | nuevo | M |
| 6 | Añadir helpers `tieneInscripcionVampirica/Ignea()` en `Player.java` | Player.java | S |
| 7 | Añadir `CONSTRUCTO`, `QUIMERA` al enum `Enemy.Tipo` + mapeo JSON | Enemy.java | S |
| 8 | Añadir entradas JSON nuevas (4 archivos) | *.json | M |
| 9 | Añadir métodos `spawnConstructo/Quimera` en `PoolEnemigos.java` | PoolEnemigos.java | S |
| 10 | Añadir constantes de daño contacto en `Constants.java` | Constants.java | S |
| 11 | Refactorizar `spawnBossesForWave()` con `BossPathfinder` | SpawnManager.java | M |
| 12 | Añadir comportamiento IA de CONSTRUCTO en `ColisionManager.java` | ColisionManager.java | L |
| 13 | Añadir comportamiento IA de QUIMERA en `ColisionManager.java` | ColisionManager.java | L |
| 14 | Test: build vampirismo → spawnea CONSTRUCTO o DEVASTADOR | manual | S |
| 15 | Test: build veneno+fuego → spawnea CONSTRUCTO o GUARDIAN | manual | S |

**Total estimado**: 2-3 días

---

## 6. COMPORTAMIENTO EN COLISIONMANAGER

### Constructo de Acero — Mecánica del Escudo

El escudo es un campo separado en el objeto `Enemy`: `shieldHp = 300` en fase 1.
Todo daño físico se redirige al escudo primero. El caos lo destruye directamente.

```java
// En el bloque de actualización del CONSTRUCTO (Enemy.update o ColisionManager):
case CONSTRUCTO:
    // Fase 1: escudo activo
    if (enemy.phase == 1 && enemy.shieldHp > 0) {
        if (tipo == DamageType.CAOS) {
            enemy.shieldHp -= damage;  // caos rompe escudo directo
        } else if (tipo == DamageType.FISICO || tipo == DamageType.RANGED) {
            int absorbed = (int)(damage * 0.80f);
            enemy.shieldHp -= absorbed;
            damage -= absorbed;  // el 20% restante pasa al HP
        }
        if (enemy.shieldHp <= 0) {
            enemy.shieldHp = 0;
            enemy.triggerPhase(2);  // activa fase 2
        }
    }
    break;
```

**IA de movimiento**: igual que GUARDIAN (persecución directa), pero cada 5s hace un `SLAM`
en AoE de 120u alrededor de sí mismo.

---

### Quimera Abismal — Mecánica de Alternancia de Velocidad

```java
// En Enemy.update() para QUIMERA (usando el timer de fase):
case QUIMERA:
    quimeraTiempoFase += delta;
    if (quimeraTiempoFase >= 6f) {
        quimeraTiempoFase = 0f;
        quimeraRapida = !quimeraRapida;
        // El move_speed base del JSON se escala aquí:
        enemy.velocidadActual = quimeraRapida ? 280f : 80f;
    }
    break;
```

**Fase 2 (60% HP)**: pasa a orbitar al jugador como SHOOTER. El timer de velocidad continúa.

**Fase 3 (30% HP)**: combina melee + ranged. Cada 10s invoca 2 `EN_RAPIDO`.
Al llegar a 10% HP: `nova de caos` — círculo de proyectiles en todas direcciones (mismo
patrón que DEVASTADOR pero con daño de caos).

---

## LEYENDA DE ESFUERZO

| S | M | L |
|---|---|---|
| < 2h | 2-4h | 4-8h |
