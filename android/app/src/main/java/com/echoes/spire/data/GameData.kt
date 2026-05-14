package com.echoes.spire.data

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

// ─── Static Data ──────────────────────────────────────────────────────────────

data class ClassDef(
    val id: String,
    val name: String,
    val icon: String,
    val hp: Int,
    val atk: Int,
    val def: Int,
    val arcane: Int,
    val intel: Int,
    val faith: Int,
    val crit: Double,
    val desc: String
)

data class WeaponDef(
    val id: String,
    val name: String,
    val icon: String,
    val atkMul: Double,
    val spdMs: Int,
    val critAdd: Double,
    val defPen: Double,
    val twin: Boolean,
    val stun: Double,
    val focus: Boolean,
    val desc: String
)

data class RelicDef(
    val id: String,
    val name: String,
    val icon: String,
    val rarity: String,
    val desc: String
)

data class BlessingDef(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String
)

data class PrestigeSkillDef(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val baseCost: Int
)

data class ForgeItem(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val maxLv: Int,
    val baseCost: Int
) {
    fun cost(lv: Int): Int = ceil(baseCost * 1.5.pow(lv.toDouble())).toInt()
}

data class ResearchItem(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val maxLv: Int,
    val baseCost: Int
) {
    fun cost(lv: Int): Int = if (id == "oraclePlus") 500 else ceil(baseCost * 1.5.pow(lv.toDouble())).toInt()
}

data class EnemyTemplate(
    val type: String,
    val name: String,
    val icon: String,
    val hpM: Double,
    val atkM: Double,
    val gold: Int,
    val souls: Int,
    val defBypass: Double = 0.0
)

data class MilestoneRelic(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val rarity: String = "mythic",
    val atkBonus: Double = 0.0,
    val hpPenalty: Double = 0.0,
    val leechBonus: Double = 0.0,
    val defPenalty: Double = 0.0,
    val soulMul: Int = 1,
    val goldPenalty: Double = 0.0,
    val zeroDef: Boolean = false
)

data class BiomeDef(
    val name: String,
    val bgHex: String,
    val accentHex: String
)

// ─── Static Game Tables ───────────────────────────────────────────────────────

val CLASSES: Map<String, ClassDef> = mapOf(
    "wanderer"    to ClassDef("wanderer",    "Wanderer",    "⚔️",  140, 14,  5,  5,  3,  3, 0.05, "Balanced. Any weapon or build."),
    "arcanist"    to ClassDef("arcanist",    "Arcanist",    "🔮",   90, 10,  2, 15, 10,  2, 0.06, "Poison & Frost master."),
    "pyromancer"  to ClassDef("pyromancer",  "Pyromancer",  "🔥",  110, 12,  3,  3,  6, 14, 0.07, "Fire & Holy — Consecrated healer."),
    "ironclad"    to ClassDef("ironclad",    "Ironclad",    "🛡️", 220, 10, 15,  2,  2,  5, 0.03, "Tank. Life-steal synergy."),
    "paladin"     to ClassDef("paladin",     "Paladin",     "⚜️",  180, 11, 12,  1,  2, 16, 0.04, "Holy Shield absorbs damage every 10s."),
    "shadowblade" to ClassDef("shadowblade", "Shadowblade", "🌑",   80, 16,  2,  4,  3,  1, 0.15, "Every 3 hits = guaranteed crit (Stealth)."),
    "spellblade"  to ClassDef("spellblade",  "Spellblade",  "✨",  105, 11,  4,  8, 12,  4, 0.08, "Hybrid: scales off both ATK and Intelligence.")
)

val WEAPONS: Map<String, WeaponDef> = mapOf(
    "fists"      to WeaponDef("fists",      "Fists",       "👊",  1.00, 1200, 0.00, 0.0,  false, 0.0,  false, "Raw power."),
    "dagger"     to WeaponDef("dagger",     "Daggers",     "🗡️", 0.75,  680, 0.12, 0.0,  false, 0.0,  false, "Very fast, high crit."),
    "sword"      to WeaponDef("sword",      "Longsword",   "⚔️",  1.00, 1050, 0.04, 0.0,  false, 0.0,  false, "Balanced, reliable."),
    "greatsword" to WeaponDef("greatsword", "Greatsword",  "🪓",  1.60, 1750, 0.09, 0.0,  false, 0.0,  false, "Slow but devastating."),
    "spellstaff" to WeaponDef("spellstaff", "Spell Staff", "🪄",  0.90,  930, 0.05, 0.0,  false, 0.0,  false, "Scales Arcane/Intel/Faith."),
    "warhammer"  to WeaponDef("warhammer",  "Warhammer",   "🔨",  1.40, 2200, 0.06, 0.50, false, 0.20, false, "-50% enemy DEF, 20% stun."),
    "twinblades" to WeaponDef("twinblades", "Twin Blades", "🗡🗡", 0.55,  500, 0.08, 0.0,  true,  0.0,  false, "Hits twice per interval."),
    "bow"        to WeaponDef("bow",        "Bow",         "🏹",  0.70,  780, 0.06, 0.0,  false, 0.0,  true,  "Every 3rd shot = 300% dmg.")
)

val RARITY_WEIGHTS: Map<String, Int> = mapOf(
    "common" to 50, "uncommon" to 30, "rare" to 15, "mythic" to 4, "cursed" to 1
)

val RELICS: List<RelicDef> = listOf(
    RelicDef("lifeLeech",    "Bloodstone Shard", "💎", "common",   "Restore 8% damage as HP."),
    RelicDef("jailBurden",   "Jailer's Burden",  "⛓️", "uncommon", "+2 ATK per Prisoner killed."),
    RelicDef("frostCore",    "Frost Core",       "❄️", "uncommon", "Frost +40% faster. Snap Freeze 2×."),
    RelicDef("echoMirror",   "Echo Mirror",      "🪞", "rare",     "30% chance to strike twice."),
    RelicDef("soulAnchor",   "Soul Anchor",      "⚓", "rare",     "+1 Soul per kill."),
    RelicDef("arcaneCoil",   "Arcane Coil",      "🌀", "uncommon", "Poison faster; poisoned = gold."),
    RelicDef("voidHeart",    "Void Heart",       "🖤", "mythic",   "50% damage dealt → HP."),
    RelicDef("misersTotem",  "Miser's Totem",    "🪙", "common",   "+30% gold."),
    RelicDef("burstCrystal", "Burst Crystal",    "💥", "rare",     "Burst Strike +120% damage."),
    RelicDef("consecGround", "Holy Mantle",      "✝️", "rare",     "Faith heals 0.5% HP per enemy hit."),
    RelicDef("glacialHeart", "Glacial Heart",    "🧊", "mythic",   "Snap Freeze: +5 stacks each."),
    RelicDef("enchantEdge",  "Enchanted Edge",   "✨", "rare",     "Intel adds 0.8× to ATK (Spellblade)."),
    RelicDef("cursedCoin",   "Cursed Coin",      "💀", "cursed",   "+200% Gold — lose 5 HP/sec."),
    RelicDef("bloodpact",    "Blood Pact",       "🩸", "cursed",   "+150% ATK — enemy DEF doubled."),
    RelicDef("soulhunger",   "Soul Hunger",      "😤", "cursed",   "+300% Souls — max HP halved."),
    RelicDef("berserker",    "Berserker's Rage", "💢", "cursed",   "+100%–200% ATK scaling with low HP.")
)

val MILESTONE_RELICS: List<MilestoneRelic> = listOf(
    MilestoneRelic("warlordCrown", "Warlord's Crown", "👑", "+80% ATK, −25% max HP.",  atkBonus = 0.80, hpPenalty = 0.25),
    MilestoneRelic("vampireFang",  "Vampire Fang",    "🦷", "+50% leech, −20% DEF.",   leechBonus = 0.50, defPenalty = 0.20),
    MilestoneRelic("soulPact",     "Soul Pact",       "📜", "×3 Souls, −30% gold.",    soulMul = 3, goldPenalty = 0.30),
    MilestoneRelic("glasscannon",  "Glass Cannon",    "💣", "+120% ATK, DEF → 0.",     atkBonus = 1.20, zeroDef = true)
)

val ENEMY_POOL: List<EnemyTemplate> = listOf(
    EnemyTemplate("prisoner", "Prisoner",      "👤", 0.80, 0.70,  8,  1),
    EnemyTemplate("specter",  "Specter",       "👻", 0.90, 1.00, 11,  1),
    EnemyTemplate("golem",    "Stone Golem",   "🗿", 2.20, 0.85, 22,  3),
    EnemyTemplate("wraith",   "Spire Wraith",  "💀", 1.20, 1.50, 16,  2),
    EnemyTemplate("demon",    "Spire Demon",   "😈", 1.50, 1.70, 20,  3),
    EnemyTemplate("mimic",    "Spire Mimic",   "📦", 1.00, 2.20, 35,  5),
    EnemyTemplate("void",     "Void Elemental","🌀", 1.30, 1.40, 25,  4, defBypass = 0.40)
)

val ELITE_TEMPLATE = EnemyTemplate("elite", "Floor Guardian", "👹", 4.0, 2.0, 90, 12)
val BOSS_TEMPLATE  = EnemyTemplate("boss",  "Spire Warden",   "🐉", 9.0, 3.0, 230, 38)

val BLESSINGS: List<BlessingDef> = listOf(
    BlessingDef("atkUp",      "+25% ATK",    "⚔️",  "ATK ×1.25 this run."),
    BlessingDef("hpRegen",    "Regeneration","💚",  "Restore 1.5% max HP/tick."),
    BlessingDef("goldRush",   "Gold Rush",   "💰",  "+60% gold from kills."),
    BlessingDef("quickBurst", "Quick Burst", "⚡",  "Burst Strike +80% dmg."),
    BlessingDef("defUp",      "+20% DEF",    "🛡️", "DEF ×1.20 this run."),
    BlessingDef("soulFeast",  "Soul Feast",  "💜",  "+5 souls/kill."),
    BlessingDef("snapPower",  "Snap+",       "🧊",  "Snap Freeze at 70% frost."),
    BlessingDef("holyGround", "Holy Ground", "✝️",  "Faith aura +0.5% more.")
)

val PRESTIGE_SKILLS: List<PrestigeSkillDef> = listOf(
    PrestigeSkillDef("startWithRelic", "Fortune's Favour", "🍀", "Start every run with a relic.",    2),
    PrestigeSkillDef("doubleSouls",    "Soul Amplifier",   "💜", "Double all Soul gains.",           3),
    PrestigeSkillDef("extraBlessing",  "Blessed Climber",  "✨", "Extra Blessing every 5 floors.",   3),
    PrestigeSkillDef("weaponMastery",  "Weapon Mastery",   "⚔️", "+30% ATK on all weapons.",        4),
    PrestigeSkillDef("ironWill",       "Iron Will",        "🛡️", "Survive one killing blow.",       5)
)

val FORGE_ITEMS: List<ForgeItem> = listOf(
    ForgeItem("wepDmg",     "Weapon Damage", "⚔️",  "+9 ATK/lv",         20, 50),
    ForgeItem("atkSpd",     "Attack Speed",  "💨",  "-6% interval/lv",   10, 75),
    ForgeItem("critChance", "Critical Edge", "🎯",  "+4% crit/lv",       10, 90),
    ForgeItem("critDmg",    "Crit Power",    "💢",  "+15% crit mult/lv", 10, 110)
)

val RESEARCH_ITEMS: List<ResearchItem> = listOf(
    ResearchItem("baseHp",    "Iron Constitution", "❤️",  "+25 HP/lv",         10, 40),
    ResearchItem("baseAtk",   "Sharpened Edge",    "🗡️", "+4 ATK/lv",         10, 60),
    ResearchItem("baseDef",   "Iron Skin",         "🛡️", "+3 DEF/lv",          8, 50),
    ResearchItem("soulMult",  "Soul Harvest",      "💜",  "+20% souls/lv",      8, 100),
    ResearchItem("oraclePlus","Oracle Mastery",    "🔮",  "Unlock 2nd Oracle",  1, 500)
)

val BIOMES: List<BiomeDef> = listOf(
    BiomeDef("Dungeon",      "#07080f", "#6366f1"),
    BiomeDef("Catacombs",    "#0b0609", "#f472b6"),
    BiomeDef("Crystal Cave", "#040d13", "#22d3ee"),
    BiomeDef("Infernal",     "#0e0602", "#f97316"),
    BiomeDef("The Void",     "#05050a", "#a78bfa")
)

// ─── Runtime Game State Objects ───────────────────────────────────────────────

data class HeroState(
    val cls: String,
    val weapon: String,
    var maxHp: Int,
    var hp: Int,
    var atk: Int,
    var def: Int,
    val spd: Long,
    val crit: Double,
    val critMul: Double,
    val arcane: Int,
    val intel: Int,
    val faith: Int,
    val relics: MutableList<RelicDef> = mutableListOf(),
    val blessings: MutableList<String> = mutableListOf(),
    var prisonerKills: Int = 0,
    var snapFreezeStacks: Int = 0,
    var ironWillUsed: Boolean = false,
    val prestigeSkills: List<String> = emptyList(),
    var holyShield: Int = 0,
    var holyShieldMax: Int = 0,
    var holyShieldTimer: Long = 0L,
    var stealthHits: Int = 0,
    var stealthReady: Boolean = false,
    var focusShots: Int = 0
) {
    fun hasRelic(id: String) = relics.any { it.id == id }
    fun hasBlessing(id: String) = blessings.contains(id)
}

data class EnemyState(
    val type: String,
    val name: String,
    val icon: String,
    var maxHp: Int,
    var hp: Int,
    var atk: Int,
    var def: Int,
    val gold: Int,
    val souls: Int,
    val floor: Int = 1,
    val defBypass: Double = 0.0,
    val isElite: Boolean = false,
    val isBoss: Boolean = false,
    var poisonBuildup: Double = 0.0,
    var frostBuildup: Double = 0.0,
    var poisoned: Boolean = false,
    var frozen: Boolean = false,
    var frozenLeft: Int = 0,
    var snapFreezeReady: Boolean = false,
    var stunned: Boolean = false,
    var stunTimer: Long = 0L
)

data class LogEntry(val text: String, val type: String, val floor: Int)

enum class RunPhase { FIGHTING, BLESSING, RELIC, MILESTONE, DEAD }

data class RunState(
    var hero: HeroState,
    var enemy: EnemyState?,
    var floor: Int = 1,
    var runGold: Double = 0.0,
    var runSouls: Double = 0.0,
    var burstCharge: Int = 0,
    val burstMax: Int = 6500,
    var phase: RunPhase = RunPhase.FIGHTING,
    var blessingChoices: List<BlessingDef>? = null,
    var relicChoices: List<RelicDef>? = null,
    var milestoneRelic: MilestoneRelic? = null,
    val combatLog: ArrayDeque<LogEntry> = ArrayDeque(),
    var killCount: Int = 0,
    val ancPow: Double,
    val research: Map<String, Int>,
    val corruption: Int,
    val oraclePath: String,
    val oracleMastery: Boolean,
    val adGoldMul: Double = 1.0,
    var goldPerMin: Int = 0,
    var soulsPerMin: Double = 0.0,
    val runStart: Long = System.currentTimeMillis()
)

// ─── Serializable save data ───────────────────────────────────────────────────

@Serializable
data class RunRecord(
    val floor: Int,
    val kills: Int,
    val gold: Int,
    val souls: Int,
    val cls: String,
    val weapon: String,
    val relics: List<String>,
    val date: String
)

@Serializable
data class SaveData(
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
    val lastSeen: Long = System.currentTimeMillis()
)

// ─── Builder helpers ──────────────────────────────────────────────────────────

fun getBiome(floor: Int): BiomeDef = BIOMES[min(BIOMES.size - 1, floor / 20)]


fun fmtN(n: Number): String {
    val v = n.toLong()
    return when {
        v >= 1_000_000L -> "%.1fM".format(v / 1_000_000.0)
        v >= 1_000L     -> "%.1fk".format(v / 1_000.0)
        else            -> v.toString()
    }
}

fun applyDef(raw: Double, def: Int, defPen: Double = 0.0, defBypass: Double = 0.0): Int {
    val effDef = max(0.0, def * (1 - defPen) * (1 - defBypass))
    return max(1, floor(raw * (100.0 / (100.0 + effDef))).toInt())
}

fun capCrit(c: Double): Double = min(0.80, c)

fun weightedRelicPick(pool: List<RelicDef>, count: Int = 3): List<RelicDef> {
    val weighted = mutableListOf<RelicDef>()
    pool.forEach { r ->
        val w = RARITY_WEIGHTS[r.rarity] ?: 10
        repeat(w) { weighted.add(r) }
    }
    weighted.shuffle()
    val seen = mutableSetOf<String>()
    val picks = mutableListOf<RelicDef>()
    for (r in weighted) {
        if (!seen.contains(r.id)) {
            seen.add(r.id)
            picks.add(r)
            if (picks.size >= count) break
        }
    }
    return picks
}

fun buildHero(
    cls: String,
    forge: Map<String, Int>,
    research: Map<String, Int>,
    weapon: String,
    prestigeSkills: List<String>
): HeroState {
    val base = CLASSES[cls] ?: CLASSES["wanderer"]!!
    val wep = WEAPONS[weapon] ?: WEAPONS["fists"]!!
    val wepMul = if (prestigeSkills.contains("weaponMastery")) wep.atkMul * 1.3 else wep.atkMul

    val maxHp = base.hp + (research["baseHp"] ?: 0) * 25
    val atk = floor(
        (base.atk + (research["baseAtk"] ?: 0) * 4 + (forge["wepDmg"] ?: 0) * 9).toDouble() * wepMul
    ).toInt()
    val def = base.def + (research["baseDef"] ?: 0) * 3
    val spd = max(280L, (wep.spdMs * (1 - (forge["atkSpd"] ?: 0) * 0.06)).toLong())
    val crit = capCrit(base.crit + wep.critAdd + (forge["critChance"] ?: 0) * 0.04)
    val critMul = 1.9 + (forge["critDmg"] ?: 0) * 0.15

    val startRelics = mutableListOf<RelicDef>()
    if (prestigeSkills.contains("startWithRelic")) {
        val nonCursed = RELICS.filter { it.rarity != "cursed" }
        weightedRelicPick(nonCursed, 1).firstOrNull()?.let { startRelics.add(it) }
    }
    if (cls == "spellblade") {
        RELICS.find { it.id == "enchantEdge" }?.let { startRelics.add(it) }
    }

    val holyShield = if (cls == "paladin") 40 + base.faith * 3 else 0

    return HeroState(
        cls = cls,
        weapon = weapon,
        maxHp = maxHp,
        hp = maxHp,
        atk = atk,
        def = def,
        spd = spd,
        crit = crit,
        critMul = critMul,
        arcane = base.arcane,
        intel = base.intel,
        faith = base.faith,
        relics = startRelics,
        prestigeSkills = prestigeSkills,
        holyShield = holyShield,
        holyShieldMax = holyShield
    )
}

fun buildEnemy(floor: Int, corruption: Int = 0): EnemyState {
    val isBoss = floor % 50 == 0 && floor > 0
    val isElite = !isBoss && floor % 10 == 0

    val rand = Random.Default
    val tmpl: EnemyTemplate = when {
        isBoss  -> BOSS_TEMPLATE
        isElite -> ELITE_TEMPLATE
        rand.nextDouble() < 0.06 -> ENEMY_POOL[5]  // mimic
        rand.nextDouble() < 0.05 -> ENEMY_POOL[6]  // void
        else -> ENEMY_POOL.subList(0, 5).random()
    }

    val baseScale = if (floor > 50) 0.16 else 0.15
    val scale = 1.0 + (floor - 1) * baseScale + corruption * 0.3
    val maxHp = max(8, (32 * tmpl.hpM * scale).toInt())
    val atk   = max(1, (9  * tmpl.atkM * scale).toInt())
    val def   = max(0, (tmpl.hpM * scale * 2).toInt())

    return EnemyState(
        type = tmpl.type,
        name = tmpl.name,
        icon = tmpl.icon,
        maxHp = maxHp,
        hp = maxHp,
        atk = atk,
        def = def,
        gold = tmpl.gold,
        souls = tmpl.souls,
        floor = floor,
        defBypass = tmpl.defBypass,
        isElite = isElite,
        isBoss = isBoss
    )
}

data class DmgResult(
    val dmg: Int,
    val crit: Boolean,
    val snapConsumed: Boolean,
    val isFocusShot: Boolean,
    val stealth: Boolean
)

fun calcHeroDmg(hero: HeroState, enemy: EnemyState, isSecondHit: Boolean = false): DmgResult {
    val wep = WEAPONS[hero.weapon] ?: WEAPONS["fists"]!!
    var dmg = hero.atk.toDouble()

    // Spellblade intel scaling
    if (hero.cls == "spellblade") {
        val edgeBonus = if (hero.hasRelic("enchantEdge")) 0.8 else 0.4
        dmg += hero.intel * edgeBonus
    }

    // Bow focus shot
    val isFocusShot = wep.focus && hero.focusShots >= 2
    if (isFocusShot) dmg *= 3.0

    // Echo Mirror
    if (!isSecondHit && hero.hasRelic("echoMirror") && Random.nextDouble() < 0.30) dmg *= 2.0

    // Snap freeze bonus
    if (enemy.snapFreezeReady || (hero.hasRelic("frostCore") && enemy.frozen)) dmg *= 2.0
    if (hero.snapFreezeStacks > 0) dmg *= (1.0 + hero.snapFreezeStacks * 0.5)

    // Berserker
    if (hero.hasRelic("berserker")) {
        val hpRatio = hero.hp.toDouble() / hero.maxHp
        dmg *= if (hpRatio < 0.20) 3.0 else 2.0
    }

    // Blood pact
    val hasBP = hero.hasRelic("bloodpact")
    if (hasBP) dmg *= 2.5

    val isCrit = hero.stealthReady || Random.nextDouble() < hero.crit
    if (isCrit) dmg *= hero.critMul

    val effDef = if (hasBP) enemy.def * 2 else enemy.def
    val finalDmg = applyDef(dmg, effDef, wep.defPen, enemy.defBypass)

    return DmgResult(
        dmg = max(1, finalDmg),
        crit = isCrit,
        snapConsumed = enemy.snapFreezeReady,
        isFocusShot = isFocusShot,
        stealth = hero.stealthReady
    )
}
