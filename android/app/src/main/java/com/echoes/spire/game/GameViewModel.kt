package com.echoes.spire.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.echoes.spire.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

// ─── UI State ─────────────────────────────────────────────────────────────────

enum class AppScreen { HUB, RUN, ASCEND }
enum class HubTab { EXPEDITION, FORGE, RESEARCH, PRESTIGE, RIFT }

data class GameUiState(
    val screen: AppScreen = AppScreen.HUB,
    val hubTab: HubTab = HubTab.EXPEDITION,
    // persistent
    val gold: Int = 60,
    val souls: Int = 0,
    val ancPow: Double = 1.0,
    val ribbons: Int = 0,
    val ascensions: Int = 0,
    val forge: Map<String, Int> = emptyMap(),
    val research: Map<String, Int> = emptyMap(),
    val prestige: List<String> = emptyList(),
    val bestFloor: Int = 0,
    val totalRuns: Int = 0,
    val dailyBest: Int = 0,
    val riftUnlocked: Boolean = false,
    val corruption: Int = 0,
    val selClass: String = "wanderer",
    val selWeapon: String = "sword",
    val oracle1: String = "balanced",
    val oracle2: String = "relics",
    val runHistory: List<RunRecord> = emptyList(),
    // run state (snapshot for UI)
    val runFloor: Int = 1,
    val runPhase: RunPhase = RunPhase.FIGHTING,
    val heroHp: Int = 0,
    val heroMaxHp: Int = 1,
    val heroAtk: Int = 0,
    val heroDef: Int = 0,
    val heroSpd: Long = 1000L,
    val heroCls: String = "wanderer",
    val heroWeapon: String = "sword",
    val heroRelics: List<RelicDef> = emptyList(),
    val heroBlessings: List<String> = emptyList(),
    val heroHolyShield: Int = 0,
    val heroHolyShieldMax: Int = 0,
    val heroStealthReady: Boolean = false,
    val heroStealthHits: Int = 0,
    val heroSnapFreezeStacks: Int = 0,
    val heroStamina: Int = 100,
    val heroStaminaMax: Int = 100,
    val heroStaminaDrained: Boolean = false,
    val enemyHp: Int = 0,
    val enemyMaxHp: Int = 1,
    val enemyName: String = "",
    val enemyIcon: String = "",
    val enemyAtk: Int = 0,
    val enemyDef: Int = 0,
    val enemyIsElite: Boolean = false,
    val enemyIsBoss: Boolean = false,
    val enemyPoisoned: Boolean = false,
    val enemyFrozen: Boolean = false,
    val enemyFrozenLeft: Int = 0,
    val enemySnapFreezeReady: Boolean = false,
    val enemyStunned: Boolean = false,
    val enemyPoisonBuildup: Double = 0.0,
    val enemyFrostBuildup: Double = 0.0,
    val enemyShattered: Boolean = false,
    val burstCharge: Int = 0,
    val burstMax: Int = 6500,
    val autoBurst: Boolean = false,
    val combatLog: List<LogEntry> = emptyList(),
    val logFilter: String = "all",
    val blessingChoices: List<BlessingDef>? = null,
    val relicChoices: List<RelicDef>? = null,
    val milestoneRelic: MilestoneRelic? = null,
    val runGold: Int = 0,
    val runSouls: Int = 0,
    val killCount: Int = 0,
    val goldPerMin: Int = 0,
    val soulsPerMin: Double = 0.0,
    val heroAttackProgress: Float = 0f,
    val enemyAttackProgress: Float = 0f,
    val offlineReward: OfflineReward? = null
)

data class OfflineReward(val gold: Int, val souls: Int, val floors: Int)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("eots_save", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    // mutable runtime run state (not Compose state, mutated in coroutines)
    private var runState: RunState? = null
    private var lastHeroAttack: Long = 0L
    private var lastEnemyAttack: Long = 0L
    private var lastStaminaRegen: Long = 0L

    private var combatJob: Job? = null
    private var burstJob: Job? = null
    private var cursedJob: Job? = null
    private var progressJob: Job? = null

    init {
        loadSave()
        checkOfflineProgress()
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    private fun loadSave() {
        val raw = prefs.getString("save", null)
        val saved = if (raw != null) {
            try { json.decodeFromString<SaveData>(raw) } catch (e: Exception) { SaveData() }
        } else SaveData()

        _state.value = _state.value.copy(
            gold = saved.gold,
            souls = saved.souls,
            ancPow = saved.ancPow,
            ribbons = saved.ribbons,
            ascensions = saved.ascensions,
            forge = saved.forge,
            research = saved.research,
            prestige = saved.prestige,
            bestFloor = saved.bestFloor,
            totalRuns = saved.totalRuns,
            dailyBest = saved.dailyBest,
            riftUnlocked = saved.riftUnlocked,
            corruption = saved.corruption,
            selClass = saved.selClass,
            selWeapon = saved.selWeapon,
            oracle1 = saved.oracle1,
            oracle2 = saved.oracle2,
            runHistory = saved.runHistory
        )
    }

    private fun writeSave() {
        val s = _state.value
        val data = SaveData(
            gold = s.gold,
            souls = s.souls,
            ancPow = s.ancPow,
            ribbons = s.ribbons,
            ascensions = s.ascensions,
            forge = s.forge,
            research = s.research,
            prestige = s.prestige,
            bestFloor = s.bestFloor,
            totalRuns = s.totalRuns,
            dailyBest = s.dailyBest,
            riftUnlocked = s.riftUnlocked,
            corruption = s.corruption,
            selClass = s.selClass,
            selWeapon = s.selWeapon,
            oracle1 = s.oracle1,
            oracle2 = s.oracle2,
            runHistory = s.runHistory,
            lastSeen = System.currentTimeMillis()
        )
        prefs.edit().putString("save", json.encodeToString(data)).apply()
    }

    private fun checkOfflineProgress() {
        val raw = prefs.getString("save", null) ?: return
        val saved = try { json.decodeFromString<SaveData>(raw) } catch (e: Exception) { return }
        val elapsed = System.currentTimeMillis() - saved.lastSeen
        if (elapsed > 3 * 60 * 1000L) {
            val floors = (elapsed / 30_000L).toInt()
            val og = floors * 10
            val os = (floors * 0.6).toInt()
            _state.value = _state.value.copy(
                gold = _state.value.gold + og,
                souls = _state.value.souls + os,
                offlineReward = OfflineReward(og, os, floors)
            )
        }
    }

    fun dismissOfflineReward() {
        _state.value = _state.value.copy(offlineReward = null)
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    fun setHubTab(tab: HubTab) {
        _state.value = _state.value.copy(hubTab = tab)
    }

    fun setScreen(screen: AppScreen) {
        _state.value = _state.value.copy(screen = screen)
    }

    fun setSelClass(id: String) {
        _state.value = _state.value.copy(selClass = id)
        writeSave()
    }

    fun setSelWeapon(id: String) {
        _state.value = _state.value.copy(selWeapon = id)
        writeSave()
    }

    fun setOracle1(path: String) {
        _state.value = _state.value.copy(oracle1 = path)
        writeSave()
    }

    fun setLogFilter(filter: String) {
        _state.value = _state.value.copy(logFilter = filter)
    }

    fun setAutoBurst(on: Boolean) {
        _state.value = _state.value.copy(autoBurst = on)
    }

    fun setCorruption(v: Int) {
        _state.value = _state.value.copy(corruption = max(0, v))
        writeSave()
    }

    // ─── Start Run ────────────────────────────────────────────────────────────

    private fun dailySeed(): Int {
        val cal = java.util.Calendar.getInstance()
        return cal.get(java.util.Calendar.YEAR) * 10000 +
                (cal.get(java.util.Calendar.MONTH) + 1) * 100 +
                cal.get(java.util.Calendar.DAY_OF_MONTH)
    }

    fun startDailyRun() {
        val s = _state.value
        val seed = dailySeed()
        val classKeys = com.echoes.spire.data.CLASSES.keys.toList()
        val dailyCls = classKeys[seed % classKeys.size]
        val paths = listOf("balanced", "gold", "relics", "elite")
        val dailyPath = paths[(seed * 7) % paths.size]
        val nonCursed = com.echoes.spire.data.RELICS.filter { it.rarity != "cursed" }
        val forceRelic = nonCursed[(seed * 13) % nonCursed.size]

        stopAllJobs()
        val hero = buildHero(dailyCls, s.forge, s.research, s.selWeapon, s.prestige)
        if (hero.relics.none { it.id == forceRelic.id }) hero.relics.add(forceRelic)

        val enemy = buildEnemy(1, s.corruption)
        val rs = RunState(
            hero = hero,
            enemy = enemy,
            floor = 1,
            ancPow = s.ancPow,
            research = s.research,
            corruption = s.corruption,
            oraclePath = dailyPath,
            oracleMastery = (s.research["oraclePlus"] ?: 0) > 0
        )
        rs.combatLog.addFirst(LogEntry("📅 Daily Run — ${enemy.name} ${enemy.icon} awaits.", "info", 1))
        runState = rs

        val now = System.currentTimeMillis()
        lastHeroAttack = now - hero.spd + 100L
        lastEnemyAttack = now - 1600L + 300L
        lastStaminaRegen = now

        _state.value = _state.value.copy(
            screen = AppScreen.RUN,
            totalRuns = s.totalRuns + 1
        )
        publishRunState()
        startCombatLoops()
        writeSave()
    }

    fun startRun() {
        stopAllJobs()
        val s = _state.value
        val hero = buildHero(s.selClass, s.forge, s.research, s.selWeapon, s.prestige)
        val enemy = buildEnemy(1, s.corruption)
        val rs = RunState(
            hero = hero,
            enemy = enemy,
            floor = 1,
            ancPow = s.ancPow,
            research = s.research,
            corruption = s.corruption,
            oraclePath = s.oracle1,
            oracleMastery = (s.research["oraclePlus"] ?: 0) > 0
        )
        rs.combatLog.addFirst(LogEntry("✨ Entered the Spire — ${enemy.name} ${enemy.icon} awaits.", "info", 1))
        runState = rs

        val now = System.currentTimeMillis()
        lastHeroAttack = now - hero.spd + 100L
        lastEnemyAttack = now - 1600L + 300L
        lastStaminaRegen = now

        _state.value = _state.value.copy(
            screen = AppScreen.RUN,
            totalRuns = s.totalRuns + 1
        )
        publishRunState()
        startCombatLoops()
        writeSave()
    }

    private fun startCombatLoops() {
        combatJob = viewModelScope.launch {
            while (true) {
                combatTick()
                delay(40L)
            }
        }
        burstJob = viewModelScope.launch {
            while (true) {
                delay(70L)
                val rs = runState ?: continue
                if (rs.phase != RunPhase.FIGHTING) continue
                rs.burstCharge = min(rs.burstMax, rs.burstCharge + 65)
                if (_state.value.autoBurst && rs.burstCharge >= rs.burstMax) {
                    fireBurst()
                } else {
                    publishRunState()
                }
            }
        }
        cursedJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val rs = runState ?: continue
                if (rs.phase != RunPhase.FIGHTING) continue
                if (rs.hero.hasRelic("cursedCoin")) rs.hero.hp = max(1, rs.hero.hp - 5)
                if (rs.hero.hasRelic("soulhunger"))  rs.hero.hp = max(1, rs.hero.hp - 2)
                // Stamina recovery: +8 per second
                rs.hero.stamina = min(rs.hero.staminaMax, rs.hero.stamina + 8)
                publishRunState()
            }
        }
        progressJob = viewModelScope.launch {
            while (true) {
                delay(40L)
                val rs = runState ?: continue
                if (rs.phase != RunPhase.FIGHTING) continue
                val now = System.currentTimeMillis()
                // If stamina drained, attack bar fills at half speed
                val effectiveSpd = if (rs.hero.staminaDrained) rs.hero.spd * 2 else rs.hero.spd
                val hp = if (effectiveSpd > 0) ((now - lastHeroAttack).toFloat() / effectiveSpd).coerceIn(0f, 1f) else 0f
                val enemy = rs.enemy
                val ep = if (enemy != null && !enemy.stunned) ((now - lastEnemyAttack).toFloat() / 1600f).coerceIn(0f, 1f) else 0f
                _state.value = _state.value.copy(heroAttackProgress = hp, enemyAttackProgress = ep)
            }
        }
    }

    private fun stopAllJobs() {
        combatJob?.cancel(); combatJob = null
        burstJob?.cancel();  burstJob = null
        cursedJob?.cancel(); cursedJob = null
        progressJob?.cancel(); progressJob = null
    }

    // ─── Combat Tick ──────────────────────────────────────────────────────────

    private fun combatTick() {
        val rs = runState ?: return
        if (rs.phase != RunPhase.FIGHTING) return
        val hero = rs.hero
        val enemy = rs.enemy ?: return
        if (hero.hp <= 0) return

        val now = System.currentTimeMillis()
        var dirty = false

        // Determine effective hero attack interval (stamina drained = double interval)
        val effectiveHeroSpd = if (hero.staminaDrained) hero.spd * 2 else hero.spd

        // Hero attacks
        if (now - lastHeroAttack >= effectiveHeroSpd) {
            val cost = staminaCost(hero.weapon)

            // Check stamina before swinging
            if (hero.stamina < cost) {
                // Not enough stamina — mark drained, skip attack but reset timer so bar refills
                hero.staminaDrained = true
                lastHeroAttack = now
                dirty = true
            } else {
                // Deduct stamina and proceed
                hero.stamina = max(0, hero.stamina - cost)
                hero.staminaDrained = false
                lastHeroAttack = now

                val wep = WEAPONS[hero.weapon] ?: WEAPONS["fists"]!!

                // Shadowblade stealth counter
                if (hero.cls == "shadowblade" && !hero.stealthReady) {
                    hero.stealthHits++
                    if (hero.stealthHits >= 3) {
                        hero.stealthReady = true
                        hero.stealthHits = 0
                    }
                }

                // Bow focus tracking
                if (wep.focus) {
                    if (hero.focusShots >= 2) hero.focusShots = 0 else hero.focusShots++
                }

                val hits = if (wep.twin) 2 else 1
                var totalDmg = 0
                var anyCrit = false

                for (h in 0 until hits) {
                    val res = calcHeroDmg(hero, enemy, h > 0)
                    enemy.hp = max(0, enemy.hp - res.dmg)
                    totalDmg += res.dmg
                    if (res.crit) anyCrit = true
                    if (res.snapConsumed) { enemy.snapFreezeReady = false; enemy.frozen = false }
                    if (res.stealth) hero.stealthReady = false
                    if (hero.snapFreezeStacks > 0) hero.snapFreezeStacks = max(0, hero.snapFreezeStacks - 1)

                    // Shatter reaction
                    if (res.shatter) {
                        enemy.hp = max(0, enemy.hp - res.shatterDmg)
                        enemy.frozen = false
                        enemy.snapFreezeReady = false
                        addLog(rs, "💥 SHATTER! +${fmtN(res.shatterDmg)} pierce!", "big")
                    }

                    // Life leech
                    val vf    = hero.hasRelic("voidHeart")
                    val ll    = hero.hasRelic("lifeLeech")
                    val vfang = hero.hasRelic("vampireFang")
                    val lp = when {
                        vf     -> 0.50
                        vfang  -> 0.50 + hero.bonusLeech
                        ll     -> 0.08 + hero.bonusLeech
                        hero.bonusLeech > 0 -> hero.bonusLeech.toDouble()
                        else   -> 0.0
                    }
                    if (lp > 0) hero.hp = min(hero.maxHp, hero.hp + (res.dmg * lp).toInt())

                    // Warhammer stun
                    if (wep.stun > 0 && Math.random() < wep.stun && !enemy.stunned) {
                        enemy.stunned = true
                        enemy.stunTimer = 2200L
                        addLog(rs, "⚡ Warhammer stun!", "relic")
                    }

                    if (res.isFocusShot) addLog(rs, "🏹 Bow: Focus Shot!", "big")
                }

                if (anyCrit) addLog(rs, "💥 CRIT! ${fmtN(totalDmg)} damage!", "big")
                if (hero.cls == "shadowblade" && hero.stealthReady) addLog(rs, "🌑 Stealth ready!", "relic")

                // Poison buildup
                if (!enemy.poisoned) {
                    val ab = hero.arcane * 0.9 + (if (hero.hasRelic("arcaneCoil")) 13.0 else 0.0)
                    enemy.poisonBuildup = min(100.0, enemy.poisonBuildup + ab * 0.18)
                    if (enemy.poisonBuildup >= 100.0) {
                        enemy.poisoned = true
                        enemy.poisonBuildup = 0.0
                        addLog(rs, "☠️ Poisoned!", "relic")
                    }
                }

                // Frost buildup
                if (!enemy.frozen && !enemy.snapFreezeReady) {
                    val fb = hero.arcane * 0.5 + hero.intel * 0.4 + (if (hero.hasRelic("frostCore")) 15.0 else 0.0)
                    val thresh = if (hero.hasBlessing("snapPower")) 70.0 else 100.0
                    enemy.frostBuildup = min(100.0, enemy.frostBuildup + fb * 0.13)
                    if (enemy.frostBuildup >= thresh) {
                        enemy.frozen = true
                        enemy.frozenLeft = 3
                        enemy.frostBuildup = 0.0
                        enemy.snapFreezeReady = true
                        if (hero.hasRelic("glacialHeart")) hero.snapFreezeStacks += 5
                        addLog(rs, "❄️ Snap Freeze!", "relic")
                    }
                }

                // Poison damage
                if (enemy.poisoned) {
                    val pd = (hero.atk * 0.28).toInt()
                    enemy.hp = max(0, enemy.hp - pd)
                    if (hero.hasRelic("arcaneCoil")) rs.runGold++
                }

                dirty = true
            }
        }

        // Stun timer
        if (enemy.stunned) {
            enemy.stunTimer -= 40L
            if (enemy.stunTimer <= 0) { enemy.stunned = false; enemy.stunTimer = 0L }
        }

        // Enemy attacks
        if (!enemy.frozen && !enemy.stunned && enemy.hp > 0 && now - lastEnemyAttack >= 1600L) {
            lastEnemyAttack = now
            var edmg = applyDef(enemy.atk.toDouble(), hero.def)

            // Paladin holy shield
            if (hero.cls == "paladin" && hero.holyShield > 0) {
                val absorbed = min(hero.holyShield, edmg)
                edmg = max(0, edmg - absorbed)
                hero.holyShield = max(0, hero.holyShield - absorbed)
            }

            // Iron will
            if (hero.hp - edmg <= 0 && hero.prestigeSkills.contains("ironWill") && !hero.ironWillUsed) {
                edmg = hero.hp - 1
                hero.ironWillUsed = true
                addLog(rs, "🛡️ Iron Will!", "big")
            }

            hero.hp = max(0, hero.hp - edmg)

            // HP Regen blessing
            if (hero.hasBlessing("hpRegen")) {
                hero.hp = min(hero.maxHp, hero.hp + (hero.maxHp * 0.015).toInt())
            }

            // Faith heal
            if (hero.faith >= 10 || hero.hasRelic("consecGround")) {
                val hh = (hero.maxHp * (0.005 + if (hero.hasBlessing("holyGround")) 0.005 else 0.0)).toInt()
                hero.hp = min(hero.maxHp, hero.hp + hh)
                val hd = (hero.faith * 0.35).toInt()
                if (hd > 0) enemy.hp = max(0, enemy.hp - hd)
            }

            // Paladin shield recharge
            if (hero.cls == "paladin") {
                hero.holyShieldTimer += 1600L
                if (hero.holyShieldTimer >= 10000L) {
                    hero.holyShield = hero.holyShieldMax
                    hero.holyShieldTimer = 0L
                    addLog(rs, "🛡️ Holy Shield recharged!", "relic")
                }
            }

            // Frozen counter
            if (enemy.frozen) {
                enemy.frozenLeft--
                if (enemy.frozenLeft <= 0) enemy.frozen = false
            }

            dirty = true
        }

        if (!dirty) return

        // Update gold/souls per min
        val elapsed = (System.currentTimeMillis() - rs.runStart) / 60_000.0
        if (elapsed > 0) {
            rs.goldPerMin = (rs.runGold / elapsed).toInt()
            rs.soulsPerMin = rs.runSouls / elapsed
        }

        if (enemy.hp <= 0) {
            onEnemyKilled()
            return
        }
        if (hero.hp <= 0) {
            onHeroDead()
            return
        }

        publishRunState()
    }

    // ─── Enemy killed ─────────────────────────────────────────────────────────

    private fun onEnemyKilled() {
        val rs = runState ?: return
        val hero = rs.hero
        val enemy = rs.enemy ?: return
        val s = _state.value

        // Epidemic: spread poison buildup to next enemy
        if (enemy.poisoned || enemy.poisonBuildup > 0) {
            rs.epidemicCarry = (hero.arcane * 0.4f).coerceAtMost(60f)
        } else {
            rs.epidemicCarry = 0f
        }

        val sM = (1 + (rs.research["soulMult"] ?: 0) * 0.20) *
                (if (s.prestige.contains("doubleSouls")) 2 else 1) *
                (if (hero.hasRelic("soulhunger")) 4 else 1)
        val spRelic = hero.relics.find { it.id == "soulPact" }
        val spData  = MILESTONE_RELICS.find { it.id == "soulPact" }
        val goldPen = spData?.goldPenalty?.takeIf { spRelic != null }?.toDouble() ?: 0.0
        val soulMulBonus = spData?.soulMul?.takeIf { spRelic != null } ?: 1

        val gM = (if (hero.hasBlessing("goldRush")) 1.6 else 1.0) *
                (if (hero.hasRelic("misersTotem")) 1.3 else 1.0) *
                (1.0 - goldPen) *
                (if (hero.hasRelic("cursedCoin")) 3.0 else 1.0) *
                rs.adGoldMul

        val sB = (if (hero.hasBlessing("soulFeast")) 5 else 0) +
                (if (hero.hasRelic("soulAnchor")) 1 else 0) * soulMulBonus

        val eg = max(1, (enemy.gold * gM * rs.ancPow).toInt())
        val es = max(0, ((enemy.souls + sB) * sM * rs.ancPow).toInt())

        rs.runGold += eg
        rs.runSouls += es
        rs.killCount++

        if (enemy.type == "prisoner" && hero.hasRelic("jailBurden")) {
            hero.prisonerKills++
            hero.atk += 2
        }
        if (enemy.type == "mimic") addLog(rs, "📦 The Mimic reveals itself!", "big")

        // currentFloor is the floor we just cleared
        val currentFloor = rs.floor
        val nextFloor = currentFloor + 1
        rs.floor = nextFloor

        // Update best floor
        if (nextFloor > s.bestFloor) {
            _state.value = _state.value.copy(bestFloor = nextFloor)
            writeSave()
        }

        if (nextFloor >= 100) {
            _state.value = _state.value.copy(riftUnlocked = true)
        }

        // Milestone relic at every 10 floors (check floor just cleared)
        if (currentFloor % 10 == 0) {
            rs.milestoneRelic = MILESTONE_RELICS.shuffled().first()
            rs.enemy = null
            rs.phase = RunPhase.MILESTONE
            addLog(rs, "👑 Boss Relic!", "big")
            publishRunState()
            return
        }

        // Extra blessing from prestige
        if (s.prestige.contains("extraBlessing") && currentFloor % 5 == 0 && currentFloor % 10 != 0) {
            rs.blessingChoices = BLESSINGS.shuffled().take(3)
            rs.enemy = null
            rs.phase = RunPhase.BLESSING
            addLog(rs, "✨ Prestige Blessing!", "relic")
            publishRunState()
            return
        }

        // Relic chest chance
        val baseV = when {
            rs.oraclePath == "relics" -> 0.28
            rs.oracleMastery -> 0.18
            else -> 0.09
        }
        val vC = if (enemy.isElite) baseV + 0.20 else baseV
        if (Math.random() < vC) {
            val pool = RELICS.filter { r -> hero.relics.none { it.id == r.id } }
            val picks = weightedRelicPick(pool, 3)
            if (picks.isNotEmpty()) {
                rs.relicChoices = picks
                rs.enemy = null
                rs.phase = RunPhase.RELIC
                addLog(rs, "🎁 Relic Vault!", "relic")
                publishRunState()
                return
            }
        }

        spawnFloor(nextFloor)
    }

    private fun onHeroDead() {
        val rs = runState ?: return
        rs.phase = RunPhase.DEAD
        addLog(rs, "💀 Fallen.", "big")
        stopAllJobs()

        val s = _state.value
        val date = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date())
        val rec = RunRecord(
            floor = rs.floor,
            kills = rs.killCount,
            gold = rs.runGold.toInt(),
            souls = rs.runSouls.toInt(),
            cls = rs.hero.cls,
            weapon = rs.hero.weapon,
            relics = rs.hero.relics.map { it.name },
            date = date
        )
        val history = listOf(rec) + s.runHistory.take(4)
        _state.value = _state.value.copy(runHistory = history)
        publishRunState()
        writeSave()
    }

    private fun spawnFloor(fl: Int) {
        val rs = runState ?: return
        val hero = rs.hero

        // Blood Forged Anvil — lose 2 max HP per floor
        if (hero.relics.any { it.id == "bloodForgedAnvil" }) {
            hero.maxHp = max(10, hero.maxHp - 2)
            hero.hp = min(hero.hp, hero.maxHp)
        }

        val ne = buildEnemy(fl, rs.corruption)

        // Epidemic carry — spread poison buildup to new enemy
        if (rs.epidemicCarry > 0f) {
            ne.poisonBuildup = rs.epidemicCarry.toDouble()
            rs.epidemicCarry = 0f
        }

        rs.enemy = ne
        rs.phase = RunPhase.FIGHTING
        lastHeroAttack = System.currentTimeMillis() - hero.spd + 150L
        lastEnemyAttack = System.currentTimeMillis() - 1600L + 320L
        addLog(rs, "🏔️ Floor $fl: ${ne.name} ${ne.icon} (${ne.maxHp} HP)", "info")
        publishRunState()
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    fun fireBurst() {
        val rs = runState ?: return
        if (rs.phase != RunPhase.FIGHTING || rs.burstCharge < rs.burstMax) return
        // Broken Hourglass blocks burst
        if (rs.hero.noBurst) return
        val hero = rs.hero
        val enemy = rs.enemy ?: return
        val dmg = (hero.atk * 4.5 *
                (if (hero.hasBlessing("quickBurst")) 1.8 else 1.0) *
                (if (hero.hasRelic("burstCrystal")) 2.2 else 1.0)).toInt()
        enemy.hp = max(0, enemy.hp - dmg)
        rs.burstCharge = 0
        addLog(rs, "⚡ Burst Strike! ${fmtN(dmg)} dmg!", "big")
        if (enemy.hp <= 0) { onEnemyKilled(); return }
        publishRunState()
    }

    fun chooseBlessing(b: BlessingDef) {
        val rs = runState ?: return
        rs.hero.blessings.add(b.id)
        if (b.id == "atkUp") rs.hero.atk = (rs.hero.atk * 1.25).toInt()
        if (b.id == "defUp") rs.hero.def = (rs.hero.def * 1.20).toInt()
        rs.blessingChoices = null
        addLog(rs, "✨ Blessing: ${b.name}!", "relic")
        spawnFloor(rs.floor)
    }

    fun chooseRelic(r: RelicDef) {
        val rs = runState ?: return
        rs.hero.relics.add(r)
        rs.relicChoices = null
        addLog(rs, "🎁 ${r.name}!", "relic")
        spawnFloor(rs.floor)
    }

    fun skipRelic() {
        val rs = runState ?: return
        rs.relicChoices = null
        spawnFloor(rs.floor)
    }

    fun chooseMilestone(r: MilestoneRelic) {
        val rs = runState ?: return
        val hero = rs.hero

        if (r.atkBonus > 0) hero.atk = (hero.atk * (1 + r.atkBonus)).toInt()
        if (r.hpPenalty > 0) {
            hero.maxHp = max(10, (hero.maxHp * (1 - r.hpPenalty)).toInt())
            hero.hp = min(hero.hp, hero.maxHp)
        }
        if (r.zeroDef) hero.def = 0
        if (r.defPenalty > 0) hero.def = max(0, (hero.def * (1 - r.defPenalty)).toInt())
        if (r.leechBonus > 0) hero.bonusLeech = r.leechBonus
        if (r.doubleSpeed) hero.spd = max(150L, hero.spd / 2)
        if (r.noBurst) hero.noBurst = true

        // Convert MilestoneRelic to RelicDef for hero.relics list
        val asDef = RelicDef(r.id, r.name, r.icon, r.rarity, r.desc)
        rs.hero.relics.add(asDef)
        rs.milestoneRelic = null
        rs.blessingChoices = BLESSINGS.shuffled().take(3)
        rs.phase = RunPhase.BLESSING
        addLog(rs, "👑 ${r.name}!", "big")
        publishRunState()
    }

    fun skipMilestone() {
        val rs = runState ?: return
        rs.milestoneRelic = null
        rs.blessingChoices = BLESSINGS.shuffled().take(3)
        rs.phase = RunPhase.BLESSING
        publishRunState()
    }

    fun returnToHub() {
        stopAllJobs()
        val rs = runState
        if (rs != null) {
            val s = _state.value
            val newGold = s.gold + rs.runGold.toInt()
            val newSouls = s.souls + rs.runSouls.toInt()
            val newRibbons = if (rs.floor >= 100) s.ribbons + 1 else s.ribbons
            _state.value = _state.value.copy(
                gold = newGold,
                souls = newSouls,
                ribbons = newRibbons,
                screen = AppScreen.HUB
            )
        } else {
            _state.value = _state.value.copy(screen = AppScreen.HUB)
        }
        runState = null
        writeSave()
    }

    // ─── Hub Upgrades ─────────────────────────────────────────────────────────

    fun buyForge(item: ForgeItem) {
        val s = _state.value
        val lv = s.forge[item.id] ?: 0
        if (lv >= item.maxLv) return
        val cost = item.cost(lv)
        if (s.gold < cost) return
        _state.value = _state.value.copy(
            gold = s.gold - cost,
            forge = s.forge + (item.id to lv + 1)
        )
        writeSave()
    }

    fun buyResearch(item: ResearchItem) {
        val s = _state.value
        val lv = s.research[item.id] ?: 0
        if (lv >= item.maxLv) return
        val cost = item.cost(lv)
        if (s.souls < cost) return
        _state.value = _state.value.copy(
            souls = s.souls - cost,
            research = s.research + (item.id to lv + 1)
        )
        writeSave()
    }

    fun buyPrestige(sk: PrestigeSkillDef) {
        val s = _state.value
        if (s.prestige.contains(sk.id)) return
        val dCost = sk.baseCost + s.prestige.size
        if (s.ribbons < dCost) return
        _state.value = _state.value.copy(
            ribbons = s.ribbons - dCost,
            prestige = s.prestige + sk.id
        )
        writeSave()
    }

    fun doAscend() {
        val s = _state.value
        _state.value = _state.value.copy(
            ancPow = s.ancPow * 2.5,
            ascensions = s.ascensions + 1,
            gold = 60,
            souls = 0,
            forge = emptyMap(),
            research = emptyMap(),
            screen = AppScreen.HUB
        )
        writeSave()
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun addLog(rs: RunState, text: String, type: String) {
        rs.combatLog.addFirst(LogEntry(text, type, rs.floor))
        while (rs.combatLog.size > 60) rs.combatLog.removeLast()
    }

    private fun publishRunState() {
        val rs = runState ?: return
        val hero = rs.hero
        val enemy = rs.enemy
        val now = System.currentTimeMillis()
        val effectiveSpd = if (hero.staminaDrained) hero.spd * 2 else hero.spd
        val hp = if (effectiveSpd > 0) ((now - lastHeroAttack).toFloat() / effectiveSpd).coerceIn(0f, 1f) else 0f
        val ep = if (enemy != null && !enemy.stunned) ((now - lastEnemyAttack).toFloat() / 1600f).coerceIn(0f, 1f) else 0f

        _state.value = _state.value.copy(
            runFloor = rs.floor,
            runPhase = rs.phase,
            heroHp = hero.hp,
            heroMaxHp = hero.maxHp,
            heroAtk = hero.atk,
            heroDef = hero.def,
            heroSpd = hero.spd,
            heroCls = hero.cls,
            heroWeapon = hero.weapon,
            heroRelics = hero.relics.toList(),
            heroBlessings = hero.blessings.toList(),
            heroHolyShield = hero.holyShield,
            heroHolyShieldMax = hero.holyShieldMax,
            heroStealthReady = hero.stealthReady,
            heroStealthHits = hero.stealthHits,
            heroSnapFreezeStacks = hero.snapFreezeStacks,
            heroStamina = hero.stamina,
            heroStaminaMax = hero.staminaMax,
            heroStaminaDrained = hero.staminaDrained,
            enemyHp = enemy?.hp ?: 0,
            enemyMaxHp = enemy?.maxHp ?: 1,
            enemyName = enemy?.name ?: "",
            enemyIcon = enemy?.icon ?: "",
            enemyAtk = enemy?.atk ?: 0,
            enemyDef = enemy?.def ?: 0,
            enemyIsElite = enemy?.isElite ?: false,
            enemyIsBoss = enemy?.isBoss ?: false,
            enemyPoisoned = enemy?.poisoned ?: false,
            enemyFrozen = enemy?.frozen ?: false,
            enemyFrozenLeft = enemy?.frozenLeft ?: 0,
            enemySnapFreezeReady = enemy?.snapFreezeReady ?: false,
            enemyStunned = enemy?.stunned ?: false,
            enemyPoisonBuildup = enemy?.poisonBuildup ?: 0.0,
            enemyFrostBuildup = enemy?.frostBuildup ?: 0.0,
            enemyShattered = enemy?.shattered ?: false,
            burstCharge = rs.burstCharge,
            burstMax = rs.burstMax,
            combatLog = rs.combatLog.toList(),
            blessingChoices = rs.blessingChoices,
            relicChoices = rs.relicChoices,
            milestoneRelic = rs.milestoneRelic,
            runGold = rs.runGold.toInt(),
            runSouls = rs.runSouls.toInt(),
            killCount = rs.killCount,
            goldPerMin = rs.goldPerMin,
            soulsPerMin = rs.soulsPerMin,
            heroAttackProgress = hp,
            enemyAttackProgress = ep
        )
    }
}
