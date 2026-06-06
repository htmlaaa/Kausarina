# Kausarina — Sprint Plan v1.0
> Unificación Plan A (inmediato) + Plan B (expansión) · Junio 2026

---

## ÍNDICE
1. [Bugs Críticos](#1-bugs-críticos)
2. [Decisiones de Diseño Pendientes](#2-decisiones-de-diseño-pendientes)
3. [Mejoras Visuales](#3-mejoras-visuales)
4. [Mejoras de Mecánicas Existentes](#4-mejoras-de-mecánicas-existentes)
5. [Contenido Nuevo](#5-contenido-nuevo)
6. [Sprint Breakdown](#6-sprint-breakdown)

---

## 1. BUGS CRÍTICOS

### BUG-01 — Inscripción busca por tipo de arma, no por slot
**Severidad**: Alta  
**Archivo**: `Player.java:108-116` + `ColisionManager.java:36`

**Problema**: `getInscriptionForWeaponType()` recorre los slots y devuelve la inscripción
del **primer arma que coincida en tipo**, ignorando el slot de origen del proyectil.
Si tienes slot 0: Espada + Inscripción Ígnea y slot 1: Espada + Inscripción Vampírica,
los proyectiles del slot 1 **siempre aplican Ígnea** en vez de Vampírica.

**Fix propuesto**: Pasar el `slotIndex` del arma que disparó al ColisionManager y consultar
la inscripción directamente por slot, no por tipo.

```java
// Antes
w.inscription = player.getInscriptionForWeaponType(w.type);
// Después
w.inscription = player.getWeaponAtSlot(slotIndex).inscription;
```

---

### BUG-02 — Habilidad activa con arma duplicada pone ambos slots en cooldown
**Severidad**: Alta  
**Archivo**: `WeaponSkill.java:21-24` + `WeaponPool.java:41-43`

**Problema**: `WeaponPool` guarda **una sola instancia** por tipo de arma. Si el mismo
tipo de arma está en slot 0 y slot 1, ambos slots comparten el mismo objeto `WeaponSkill`
y por tanto el mismo `skillCooldownTimer`. Activar Q pone E también en cooldown.

**Fix propuesto**: Al equipar un arma desde el pool, **clonar el objeto** en vez de
compartir la referencia. Agregar un método `Weapon.clonar()` que copie stats pero
tenga su propio `cooldownTimer`.

---

### BUG-03 — Inscripción pendiente se asigna a TODOS los slots con el mismo nombre
**Severidad**: Media  
**Archivo**: `GameScreen.java:933-937`

**Problema**: Cuando el jugador aplica una inscripción presionando 1 o 2, el código
asigna correctamente por slot. Pero si el sistema de loot aplica una inscripción
automáticamente al dropear un arma (30% de chance), busca por nombre/tipo y puede
sobrescribir la inscripción de otro arma con el mismo tipo.

**Fix propuesto**: La asignación automática de inscripción en el loot debe guardarse
en el objeto `Weapon` recién creado **antes** de equiparlo, nunca buscar en slots ya equipados.

---

## 2. DECISIONES DE DISEÑO PENDIENTES

### DISEÑO-01 — ¿Usamos los 6 slots o mantenemos 2 activos + 4 guardados?

**Estado actual**: 2 slots activos (`equippedWeapons[2]`) + 4 guardados (`storageWeapons[4]`).
Los 4 guardados no disparan, no tienen cooldown visual, solo se ven en el HUD.

**Opción A — Mantener 2 activos (recomendada para esta fase)**
- El juego se mantiene enfocado y legible
- Los slots guardados sirven como "reserva para swap"
- Menos carga de balanceo: solo 2 armas contribuyen al DPS
- Tiempo de implementación: 0 (ya está así)

**Opción B — 6 slots todos activos (estilo Vampire Survivors)**
- Más caótico y divertido en late-game
- Requiere rebalancear TODA la curva de dificultad (los enemigos aguantarían muy poco)
- Cambia la identidad del juego: pasa de "2 armas con sinergia" a "colección masiva"
- Tiempo de implementación: ~2-3 días + 1 semana de balanceo

**Decisión sugerida**: Mantener Opción A ahora. Evaluar Opción B cuando haya más armas
(mínimo 20+) para que los 6 slots se sientan variados.

---

### DISEÑO-02 — Slots de amuletos en HUD

**Estado actual**: Los amuletos existen (7 tipos) pero se aplican instantáneamente al
recogerlos, sin un slot visible. El jugador no sabe qué amuletos tiene equipados a
menos que mire un indicador pequeño.

**Propuesta**: 3 slots de amuletos visibles en el HUD (abajo o lateral).
- El jugador puede equipar máximo 3 amuletos simultáneamente
- Recoger un 4to amuleto con todos los slots llenos abre menú de swap (igual que armas)
- Esto convierte los amuletos en decisiones estratégicas, no en simples power-ups

---

## 3. MEJORAS VISUALES

### VIS-01 — Ataques cuerpo a cuerpo no deben verse como balas
**Prioridad**: Alta  
**Problema**: Los ataques de la espada, mazo, etc. generan proyectiles (`Bala`) que
vuelan como balas normales. Visualmente no se distingue un espadazo de un disparo.

**Propuesta**:
- Crear una clase `BalaCorte` (o `AtaqueMelee`) que NO avance en línea recta:
  se mueve en un **arco curvo** alrededor del player y desaparece en ~0.2s
- Visualmente: línea o polígono alargado con orientación hacia el enemigo objetivo
- Rango corto fijo: 150-180 unidades máximo, no viaja al infinito
- El arco visual ya existe para Caballero (`shapeRenderer.arc()`), hay que hacer que
  el **hitbox del daño** siga esa misma geometría en vez de crear una Bala recta

**Archivos a modificar**: `Bala.java`, `WeaponNormal.java`, `ColisionManager.java`

---

### VIS-02 — Proyectiles mágicos deben verse distintos a las balas físicas
**Prioridad**: Alta  
**Problema**: Los hechizos del Mago y los disparos del Tirador usan la misma textura
(`SharedTextures.getBala()`) y la misma velocidad. No se ve "mágico".

**Propuesta**:
- **Proyectil mágico**: tamaño mayor (20-24px), velocidad 40% más lenta, con efecto de
  *fade-in* al disparar y color según tipo de daño (azul=arcano, verde=veneno, rojo=fuego)
- **Proyectil de caos**: oscilación lateral sinusoidal leve mientras viaja, color violeta
- Usar `ShapeRenderer.circle()` con radio dinámico en vez de textura fija para magia,
  o crear una textura dedicada `bala_magica.png` de 20x20px

**Archivos a modificar**: `Bala.java` (añadir `TipoBala` enum), `Player.java` (pasar tipo al crear Bala)

---

### VIS-03 — Diferenciar visualmente armas NORMAL vs SKILL en el HUD
**Prioridad**: Media  
**Problema**: En el HUD las armas con habilidad activa [Q]/[E] se ven igual que las
normales excepto por el texto del atajo. Si el jugador no lo leyó, no sabe que puede presionar Q.

**Propuesta**:
- Borde de color distinto en el slot: dorado para armas SKILL, gris para NORMAL
- Icono pequeño de "rayo/estrella" en la esquina del slot cuando hay habilidad disponible
- Flash del slot cuando la habilidad sale de cooldown

---

## 4. MEJORAS DE MECÁNICAS EXISTENTES

### MEC-01 — Amulet Slots (3 slots con UI)
Refactorizar el sistema de amuletos de "pickup instantáneo sin límite" a:
- Array `equippedAmulets[3]` en `Player.java`
- Al recoger con slots llenos: menú de swap (como armas, con timeout de 5s)
- HUD muestra los 3 slots con icono y nombre corto del amuleto
- Ampliar a 10 amuletos totales (ver sección 5)

**Archivos**: `Player.java`, `GameScreen.java`, `HUD.java`, `AmuletType.java`

---

### MEC-02 — Reroll de upgrades (1 uso por run)
Al subir de nivel, añadir un botón "Mezclar [R]" que reordena aleatoriamente
las 3 opciones ofrecidas. Solo disponible 1 vez por run por defecto.
- Usar token visual (dado/flecha) que se "gasta" visualmente al usarlo
- En runs desbloqueadas (meta-progresión futura) podría subir a 2-3 rerolls

**Archivos**: `UpgradeManager.java`, HUD de nivel

---

### MEC-03 — Feedback visual del combo del Tirador
El combo meter actual decae sin que el jugador lo note. Cambios:
- Barra de combo dedicada en el HUD bajo los slots de armas
- Texto flotante "+COMBO x8" en verde al acumular
- A combo 10 (máximo): flash dorado + efecto de partícula breve en el arma activa
- Sonido de "clic" por cada combo acumulado (si hay assets de audio disponibles)

---

### MEC-04 — Dificultad seleccionable antes del run
En la pantalla de selección de personaje, añadir selector de dificultad:
- **Normal**: depth 1 desde el inicio (como está)
- **Brutal**: depth 3 al inicio, elites desde wave 1, +20% drop de tier 3+
- **Caos**: depth 5 al inicio, todos los multiplicadores ×1.2, doble drop legendarias

Solo modifica `currentDepth` inicial en `GameScreen` y multiplicadores de spawn.

---

### MEC-05 — Sinergias implícitas entre upgrades existentes
Sin añadir nuevos upgrades, añadir condiciones cruzadas:

| Si tienes | Y tienes | Bonus extra |
|-----------|----------|-------------|
| FILO_IGNEO | DANIO_UP nivel 3+ | Enemigos ardiendo reciben +20% de todo daño |
| VAMPIRISMO | VIDA_MAXIMA_UP | Lifesteal se aplica también en radio 60u al matar |
| BALA_EXTRA | PERFORACION | Cada bala extra perfora 1 enemigo adicional |
| CUCHILLA_VENENO | CADENCIA_UP nivel 3+ | Veneno apila 2 cargas en vez de 1 |

Se implementa en `UpgradeManager` con checks condicionales al calcular multiplicadores.

---

## 5. CONTENIDO NUEVO

### CNT-01 — Nuevas Armas (+12, de 6 → 18)

> Todas encajan en `weapons.json` sin cambios de estructura.
> Solo hay que añadir las filas y actualizar los arrays en `WeaponDropper.java`.

#### Melee — 4 nuevas

| weapon_id | Nombre | Tipo | Mecánica especial |
|-----------|--------|------|-------------------|
| W_GREATAXE | Hacha de Guerra | WT_GREATSWORD | AoE en cono de 90°, destroza armadura (-8 DEF) |
| W_RAPIER | Estoque | WT_DAGGER | Crit base 15%, cada crit consecutivo suma +5% hasta 40% |
| W_WARSCYTHE | Guadaña de Guerra | WT_GREATSWORD | Cada kill resetea el cooldown del ataque ligero |
| W_FLAIL | Mayal | WT_SWORD | Daño con varianza alta (±40%), affix `Volatile` exclusivo |

#### Ranged — 4 nuevas

| weapon_id | Nombre | Tipo | Mecánica especial |
|-----------|--------|------|-------------------|
| W_SNIPER | Rifle de Francotirador | WT_CROSSBOW | 1 bala, rango doble, daño ×3, sin auto-aim |
| W_SHOTGUN | Escopeta de Combate | WT_PISTOL | 5 balas en cono de 30°, rango máx 300u |
| W_EXPLOSIVE | Lanzagranadas | WT_BOW | Proyectil lento que explota en AoE (radio 80u) al impactar |
| W_BOLAS | Boleadoras | WT_BOW | Aplica SLOW 50% por 2s, daño físico moderado |

#### Magic — 4 nuevas

| weapon_id | Nombre | Tipo | Mecánica especial |
|-----------|--------|------|-------------------|
| W_CHAINSTAFF | Vara del Rayo | WT_WAND | El rayo encadena hasta 3 enemigos |
| W_BLOODTOME | Grimorio de Sangre | WT_GRIMOIRE | Daño de caos, coste en HP en vez de maná |
| W_VOIDSTAFF | Bastón del Vacío | WT_STAFF | Crea zona de daño estacionaria 3s en el suelo |
| W_FROSTORB | Orbe Glacial | WT_GRIMOIRE | Proyectil lento, congela en radio 60u al impactar |

---

### CNT-02 — Nuevos Enemigos (+8, de 9 → 17)

> Todos encajan en `enemies.json` + `enemyattacks.json` + `enemyresistances.json`.

#### Minions nuevos

| enemy_id | Nombre | Comportamiento | Mecánica |
|----------|--------|----------------|----------|
| EN_BERSERKER | Berserker | Carga directa | A <20% HP: ×3 velocidad, +50% daño |
| EN_SPLITTER | Particionador | Normal | Al morir: se divide en 2 copias a 40% HP |
| EN_HEALER | Curandero | Huye del player | Cura 10 HP/s a aliados en radio 150u, **prioridad de targeting** |
| EN_SHIELDER | Guardián | Lento | Bloquea 80% daño frontal; la espalda recibe daño normal |

#### Elites nuevos

| enemy_id | Nombre | Mecánica |
|----------|--------|----------|
| EN_ELITE_CHARGE | Cargador Élite | Telegrafía 1.5s → carga en línea recta, stun al impactar |
| EN_ELITE_SUMMON | Convocador | Invoca 3 EN_NORMAL cada 8s, huye del combate directo |
| EN_ELITE_ZONE | Guardián Territorial | Crea zona lenta (radio 120u) en el suelo, no la abandona |

#### Boss nuevo

| enemy_id | Nombre | HP | Mecánica |
|----------|--------|----|----------|
| EN_BOSS_PHASE | El Fragmentado | 1600 | 3 fases: cada una cambia su tipo de ataque (físico→mágico→caos) |

---

### CNT-03 — Nuevos Upgrades (+15, de 17 → 32)

> Solo requiere añadir al enum `Upgrade.Tipo` + instanciar en `UpgradeManager`.

#### Genéricos (todos los roles)

| Tipo | Efecto | Max niveles |
|------|--------|-------------|
| ESCUDO_TEMPORAL | 1 vez por run: invulnerabilidad 1.5s cuando recibirías daño mortal | 1 |
| REGENERACION | +1 HP/seg pasivo | 3 |
| AURA_VENENO | Envenena enemigos en radio 80u continuamente | 1 |
| CRITICO_ENCADENADO | Crits consecutivos suman +10% daño (máx 3 stacks, se rompe al fallar) | 1 |
| PESO_MUERTO | Al matar: +8% velocidad por 2s | 1 |

#### Caballero

| Tipo | Efecto | Max |
|------|--------|-----|
| CONTRAGOLPE | Al recibir daño: devuelve el 30% a todos los enemigos en radio 60u | 3 |
| AURA_INTIMIDACION | Enemigos en radio 100u atacan 15% más lento | 1 |
| GOLPE_TIERRA | Ataque pesado genera onda sísmica adicional (AoE 100u) | 1 |

#### Mago

| Tipo | Efecto | Max |
|------|--------|-----|
| MANA_SOBRECARGA | Al llegar a 0 maná: siguiente hechizo hace ×2 daño | 1 |
| BARRERA_MAGICA | Si maná > 80%: absorbe los primeros 25 de daño por golpe | 2 |
| HECHIZO_ESFERAS | Hechizo pesado genera 3 orbes que orbitan al player 5s | 1 |

#### Tirador

| Tipo | Efecto | Max |
|------|--------|-----|
| BALA_EXPLOSIVA | 15% de balas explotan en radio 40u al impactar | 1 |
| MUNICION_ENVENENADA | Balas aplican veneno con 40% de probabilidad | 1 |
| DISPARO_TENSO | Mantener botón de ataque acumula ×2.5 daño (liberar = dispara) | 1 |

---

### CNT-04 — Nuevos Amuletos (+5, de 7 → 12)

> El enum `AmuletType` se amplía. La lógica de efecto va en `GameScreen` donde ya están los otros.

| ID | Nombre | Efecto |
|----|--------|--------|
| AMULETO_CRITICO | Filo del Destino | +8% probabilidad de crítico global |
| AMULETO_EXPLOSION | Núcleo Inestable | Al matar con crítico: explosión en radio 50u (50% del daño del golpe) |
| AMULETO_ESPECTROS | Cadena de Almas | Cada 10 kills: proyectil espectral automático al enemigo más cercano |
| AMULETO_TIEMPO | Reloj Roto | 1 vez cada 45s: ralentiza todos los enemigos 60% por 3s |
| AMULETO_ARMADURA | Escamas de Piedra | +15 DEF permanente pero -10% velocidad de movimiento |

---

### CNT-05 — Sistema de Evolución de Armas (nuevo sistema)

**Mecánica**: Recoger el mismo `weapon_id` por segunda vez, teniendo la primera equipada,
la evoluciona en vez de ofrecerla como swap.

**Reglas**:
- La arma evolucionada sube un tier (T1→T2, etc.)
- Hereda la inscripción de la primera si la tenía
- Los affixes se re-tiradean al nuevo tier
- Se registra en un nuevo archivo `weapon_evolutions.json`

**Ejemplos de evoluciones**:

| Base | + Trigger | Resultado | Bonus especial |
|------|-----------|-----------|----------------|
| W_SHORTSWORD ×2 | duplicado | W_LONGSWORD | alcance +30u |
| W_BACULO_ARCANO + W_FROSTORB | combinación | W_GLACIAL_ARCANO | bola de hielo + proyectil encadenado |
| W_PISTOLAS_GEMELAS ×2 | duplicado | W_CUATRO_PISTOLAS | +2 balas por disparo |
| W_BLOODTOME + cualquier arma mágica | cualquiera | W_GRIMORIO_SANGUINEO | combina coste HP + efecto del arma combinada |

**Archivos nuevos**: `weapon_evolutions.json`  
**Archivos modificados**: `WeaponDropper.java` (detectar duplicado), `Player.java` (triggear evolución)

---

## 6. SPRINT BREAKDOWN

### Sprint 1 — Bugs + Fundamentos (1-2 semanas)
> Objetivo: el juego funciona sin inconsistencias. Nada nuevo roto.

| # | Tarea | Archivos | Esfuerzo |
|---|-------|----------|----------|
| 1 | **[BUG-01]** Fix inscripción por slot, no por tipo | `Player.java`, `ColisionManager.java` | S |
| 2 | **[BUG-02]** Fix cooldown compartido — clonar WeaponSkill al equipar | `WeaponPool.java`, `Player.java` | M |
| 3 | **[BUG-03]** Fix inscripción en loot no sobrescribe slots existentes | `GameScreen.java` | S |
| 4 | **[DISEÑO-01]** Decisión de slots: documentar y confirmar Opción A | — | S |
| 5 | **[MEC-04]** Selector de dificultad en pantalla de personaje | `GameScreen.java` | S |

---

### Sprint 2 — Visuales + Feedback (1-2 semanas)
> Objetivo: el juego se ve y se siente distinto según el tipo de ataque.

| # | Tarea | Archivos | Esfuerzo |
|---|-------|----------|----------|
| 6 | **[VIS-01]** Ataques melee viajan en arco, no en línea recta | `Bala.java`, `WeaponNormal.java`, `ColisionManager.java` | L |
| 7 | **[VIS-02]** Proyectiles mágicos: más lentos, más grandes, con color por tipo de daño | `Bala.java` (añadir TipoBala enum), `Player.java` | M |
| 8 | **[VIS-03]** HUD diferencia armas SKILL (borde dorado) vs NORMAL | `HUD.java` | S |
| 9 | **[MEC-03]** Barra de combo visible + texto flotante para Tirador | `HUD.java`, `GameScreen.java` | S |

---

### Sprint 3 — Amuletos + Upgrades (1-2 semanas)
> Objetivo: los amuletos son decisiones, no power-ups automáticos. Los upgrades tienen personalidad.

| # | Tarea | Archivos | Esfuerzo |
|---|-------|----------|----------|
| 10 | **[DISEÑO-02]** Sistema de 3 slots de amuletos con UI y swap | `Player.java`, `HUD.java`, `GameScreen.java` | L |
| 11 | **[CNT-04]** 5 amuletos nuevos | `AmuletType.java`, `GameScreen.java` | M |
| 12 | **[MEC-02]** Reroll de upgrades (1 uso por run, botón [R]) | `UpgradeManager.java`, HUD nivel | M |
| 13 | **[MEC-05]** Sinergias implícitas entre upgrades existentes | `UpgradeManager.java` | M |
| 14 | **[CNT-03]** 15 upgrades nuevos (por rol y genéricos) | `Upgrade.java`, `UpgradeManager.java` | L |

---

### Sprint 4 — Armas y Enemigos (2-3 semanas)
> Objetivo: primera expansión real de contenido. El pool de drops se siente amplio.

| # | Tarea | Archivos | Esfuerzo |
|---|-------|----------|----------|
| 15 | **[CNT-01]** 12 armas nuevas (JSON + WeaponDropper arrays) | `weapons.json`, `WeaponDropper.java` | L |
| 16 | **[CNT-02]** 8 enemigos nuevos (JSON + AI en ColisionManager) | `enemies.json`, `ColisionManager.java` | XL |
| 17 | **[CNT-02]** Boss nuevo EN_BOSS_PHASE con 3 fases | `ColisionManager.java`, `bossphases.json` | L |

---

### Sprint 5 — Sistemas Avanzados (2-3 semanas)
> Objetivo: rejugabilidad real. El jugador tiene razones para seguir después de la primera victoria.

| # | Tarea | Archivos | Esfuerzo |
|---|-------|----------|----------|
| 18 | **[MEC-01 / Plan A]** Meta-progresión: tokens de run, desbloqueo de upgrades entre runs | `DBManager.java`, nueva tabla `run_tokens` | L |
| 19 | **[CNT-05]** Sistema de evolución de armas | `WeaponDropper.java`, `Player.java`, `weapon_evolutions.json` | XL |

---

### Leyenda de esfuerzo
| Símbolo | Tiempo estimado |
|---------|------------------|
| S | < 2 horas |
| M | 2-4 horas |
| L | 4-8 horas (1 día) |
| XL | 8-16 horas (1-2 días) |

---

## RESUMEN DE IMPACTO

| Sprint | Tipo | Impacto en jugador |
|--------|------|--------------------|
| 1 | Bugs | El juego funciona como se diseñó |
| 2 | Visual | Se siente más pulido y legible |
| 3 | Mecánica | Las decisiones importan más |
| 4 | Contenido | La rejugabilidad se multiplica |
| 5 | Sistema | El jugador vuelve después de la primera victoria |
