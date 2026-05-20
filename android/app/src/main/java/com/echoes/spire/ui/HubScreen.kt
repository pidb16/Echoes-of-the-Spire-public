package com.echoes.spire.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoes.spire.R
import com.echoes.spire.data.*
import com.echoes.spire.game.AppScreen
import com.echoes.spire.game.GameUiState
import com.echoes.spire.game.GameViewModel
import com.echoes.spire.game.HubTab
import com.echoes.spire.ui.components.UpgradeRow
import com.echoes.spire.ui.theme.*

fun heroDrawableRes(classId: String): Int = when (classId) {
    "wanderer"    -> R.drawable.hero_wanderer
    "arcanist"    -> R.drawable.hero_arcanist
    "pyromancer"  -> R.drawable.hero_pyromancer
    "ironclad"    -> R.drawable.hero_ironclad
    "paladin"     -> R.drawable.hero_paladin
    "shadowblade" -> R.drawable.hero_shadowblade
    "spellblade"  -> R.drawable.hero_spellblade
    else          -> R.drawable.hero_wanderer
}

fun classAccentColor(id: String): Color = when (id) {
    "wanderer"    -> AccentColor
    "arcanist"    -> Color(0xFFa855f7)
    "pyromancer"  -> FireColor
    "ironclad"    -> TextSecondary
    "paladin"     -> GoldColor
    "shadowblade" -> Color(0xFF7c3aed)
    "spellblade"  -> FrostColor
    else          -> AccentColor
}

@Composable
fun OrnateCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier) {
        content()
        Image(painterResource(R.drawable.ic_corner_tl), null, modifier = Modifier.size(20.dp).align(Alignment.TopStart))
        Image(painterResource(R.drawable.ic_corner_tr), null, modifier = Modifier.size(20.dp).align(Alignment.TopEnd))
        Image(painterResource(R.drawable.ic_corner_bl), null, modifier = Modifier.size(20.dp).align(Alignment.BottomStart))
        Image(painterResource(R.drawable.ic_corner_br), null, modifier = Modifier.size(20.dp).align(Alignment.BottomEnd))
    }
}

@Composable
fun HubScreen(state: GameUiState, vm: GameViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .padding(bottom = 8.dp)
        ) {
            when (state.hubTab) {
                HubTab.EXPEDITION -> ExpeditionTab(state, vm)
                HubTab.FORGE      -> ForgeTab(state, vm)
                HubTab.RESEARCH   -> ResearchTab(state, vm)
                HubTab.PRESTIGE   -> PrestigeTab(state, vm)
                HubTab.RIFT       -> RiftTab(state, vm)
            }
        }

        // Bottom navigation
        BottomNav(state, vm)
    }
}

// ─── Bottom Nav ───────────────────────────────────────────────────────────────

@Composable
fun BottomNav(state: GameUiState, vm: GameViewModel) {
    // Tab: (HubTab, iconId, label)
    val tabs = listOf(
        Triple(HubTab.EXPEDITION, "sword",     "Expedition"),
        Triple(HubTab.FORGE,      "lightning", "Forge"),
        Triple(HubTab.RESEARCH,   "soul",      "Research"),
        Triple(HubTab.PRESTIGE,   "ribbon",    "Astral"),
        Triple(HubTab.RIFT,       "skull",     "Rift")
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xF207080f),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top border line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x336366f1))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (tab, iconId, label) ->
                    val active = state.hubTab == tab
                    val scale by animateFloatAsState(
                        targetValue = if (active) 1.10f else 1.0f,
                        animationSpec = tween(180),
                        label = "navScale"
                    )

                    Column(
                        modifier = Modifier
                            .scale(scale)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) Color(0x2E6366f1) else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { vm.setHubTab(tab) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .defaultMinSize(minWidth = 52.dp, minHeight = 44.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        GameIcon(
                            iconId = iconId,
                            tint = if (active) Color(0xFFc4b5fd) else TextMuted,
                            size = 20.dp
                        )
                        Text(
                            text = label,
                            color = if (active) Color(0xFFc4b5fd) else TextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CinzelFamily,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        )
                        // Active indicator — gradient underline
                        if (active) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(width = 16.dp, height = 2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(AccentGradient)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Expedition Tab ───────────────────────────────────────────────────────────

@Composable
fun ExpeditionTab(state: GameUiState, vm: GameViewModel) {
    Text(
        text = "Choose class and begin the climb.",
        color = TextSecondary, fontSize = 11.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
        textAlign = TextAlign.Center
    )

    // Class grid
    val classList = CLASSES.entries.toList()
    OrnateCard(modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            for (row in classList.chunked(2)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    row.forEach { (id, cls) ->
                        val active = state.selClass == id
                        ClassCard(
                            id = id, cls = cls, active = active,
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setSelClass(id) }
                        )
                    }
                    if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    // Weapon selector — frosted glass panel
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        // Blur layer — blurs own content creating a soft glow bloom
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            AccentColor.copy(alpha = 0.08f),
                            Color(0xFF070810).copy(alpha = 0.92f)
                        )
                    )
                )
                .blur(radius = 24.dp)
        )
        // Frosted glass surface
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xCC070810))
                .border(1.dp, AccentColor.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
        )
        // Actual content
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(bottom = 7.dp)
            ) {
                GameIcon(iconId = "sword", tint = AccentColor, size = 12.dp)
                Text(
                    text = "WEAPON",
                    color = AccentColor, fontSize = 9.sp,
                    fontFamily = CinzelFamily,
                    letterSpacing = 2.sp
                )
            }
            val weaponList = WEAPONS.entries.toList()
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                weaponList.take(4).forEach { (id, w) ->
                    WeaponTile(
                        id = id, w = w,
                        selected = state.selWeapon == id,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.setSelWeapon(id) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                weaponList.drop(4).forEach { (id, w) ->
                    WeaponTile(
                        id = id, w = w,
                        selected = state.selWeapon == id,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.setSelWeapon(id) }
                    )
                }
            }
            WEAPONS[state.selWeapon]?.desc?.let { desc ->
                Text(
                    text = desc,
                    color = TextSecondary, fontSize = 10.sp,
                    modifier = Modifier.padding(top = 7.dp)
                )
            }
        }
    }

    // Oracle path
    GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(bottom = 7.dp)
        ) {
            GameIcon(iconId = "soul", tint = SoulsColor, size = 12.dp)
            Text(
                text = "ORACLE PATH",
                color = AccentColor, fontSize = 9.sp,
                fontFamily = CinzelFamily,
                letterSpacing = 2.sp
            )
        }
        val paths = listOf(
            Triple("balanced", "shield",    "Balanced"),
            Triple("gold",     "gold",      "Gold"),
            Triple("relics",   "lightning", "Relics"),
            Triple("elite",    "skull",     "Elites")
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            paths.forEach { (id, iconId, label) ->
                val active = state.oracle1 == id
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) Color(0x336366f1) else Color.Transparent)
                        .border(
                            1.dp,
                            if (active) AccentColor else Color(0xFF1e293b),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { vm.setOracle1(id) }
                        .padding(7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameIcon(iconId = iconId, tint = if (active) AccentColor else TextMuted, size = 18.dp)
                    Text(label, color = if (active) AccentColor else TextMuted, fontSize = 8.sp,
                        modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }

    // Corruption notice
    if (state.corruption > 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 9.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0AFFFFFF))
                .border(1.dp, Color(0x667c3aed), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                GameIcon(iconId = "skull", tint = Color(0xFFc4b5fd), size = 12.dp)
                Text(
                    text = "Corruption ${state.corruption}: +${state.corruption * 30}% harder · +${state.corruption * 25}% loot",
                    color = Color(0xFFc4b5fd), fontSize = 11.sp
                )
            }
        }
    }

    // Start buttons
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { vm.startRun() },
            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4f46e5))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GameIcon(iconId = "sword", tint = Color.White, size = 14.dp)
                Text("Expedition", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        Button(
            onClick = { vm.startDailyRun() },
            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065f46))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📅 Daily", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Best F${state.dailyBest}", fontSize = 9.sp, color = Color(0xFFa7f3d0))
            }
        }
    }

    // Ascend button
    if (state.totalRuns >= 3 && state.souls >= 400) {
        Button(
            onClick = { vm.setScreen(AppScreen.ASCEND) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).defaultMinSize(minHeight = 44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF78350f))
        ) {
            Text("🌅 Ascend — ×2.5 Ancient Power", color = Color(0xFFfef3c7),
                fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }

    // Stats
    GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Runs: ${state.totalRuns}", color = TextSecondary, fontSize = 10.sp)
                Text("Ascensions: ${state.ascensions}", color = TextSecondary, fontSize = 10.sp)
                Text("Daily: F${state.dailyBest}", color = TextSecondary, fontSize = 10.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Best: ", color = TextSecondary, fontSize = 10.sp)
                    GradientText("F${state.bestFloor}", AccentTextBrush, 10.sp)
                }
                Text("AP: ×${String.format("%.1f", state.ancPow)}", color = TextSecondary, fontSize = 10.sp)
                Text("Rift: ${if (state.riftUnlocked) "Open" else "F100"}", color = TextSecondary, fontSize = 10.sp)
            }
        }
    }

    // Run history
    if (state.runHistory.isNotEmpty()) {
        Text(
            text = "RECENT RUNS",
            color = Color(0xFF334155), fontSize = 9.sp,
            fontFamily = CinzelFamily,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        state.runHistory.take(3).forEach { rec ->
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(CLASSES[rec.cls]?.icon ?: "⚔️", fontSize = 16.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${CLASSES[rec.cls]?.name} · ${WEAPONS[rec.weapon]?.icon}",
                            color = Color(0xFFc4b5fd), fontSize = 10.sp
                        )
                        Text(
                            text = "${rec.date} · ${rec.kills} kills",
                            color = TextSecondary, fontSize = 9.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("F${rec.floor}", color = Color(0xFF818cf8), fontWeight = FontWeight.Bold,
                            fontSize = 14.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StatBadge("gold", fmtN(rec.gold), GoldColor)
                            StatBadge("soul", fmtN(rec.souls), SoulsColor)
                        }
                    }
                }
            }
        }
    }
}

// ─── Forge Tab ────────────────────────────────────────────────────────────────

@Composable
fun ForgeTab(state: GameUiState, vm: GameViewModel) {
    Text(
        text = "Permanent weapon upgrades. Costs scale exponentially.",
        color = TextSecondary, fontSize = 11.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
        textAlign = TextAlign.Center
    )
    FORGE_ITEMS.forEach { item ->
        UpgradeRow(
            icon = item.icon,
            name = item.name,
            desc = item.desc,
            level = state.forge[item.id] ?: 0,
            maxLevel = item.maxLv,
            cost = item.cost(state.forge[item.id] ?: 0),
            currency = "💰",
            balance = state.gold,
            accent = Color(0xFF4f46e5),
            onBuy = { vm.buyForge(item) }
        )
    }
}

// ─── Research Tab ─────────────────────────────────────────────────────────────

@Composable
fun ResearchTab(state: GameUiState, vm: GameViewModel) {
    Text(
        text = "Permanent character upgrades. Costs scale exponentially.",
        color = TextSecondary, fontSize = 11.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
        textAlign = TextAlign.Center
    )
    RESEARCH_ITEMS.forEach { item ->
        UpgradeRow(
            icon = item.icon,
            name = item.name,
            desc = item.desc,
            level = state.research[item.id] ?: 0,
            maxLevel = item.maxLv,
            cost = item.cost(state.research[item.id] ?: 0),
            currency = "💜",
            balance = state.souls,
            accent = Color(0xFF7c3aed),
            onBuy = { vm.buyResearch(item) }
        )
    }
}

// ─── Prestige Tab ─────────────────────────────────────────────────────────────

@Composable
fun PrestigeTab(state: GameUiState, vm: GameViewModel) {
    // Ribbon info header
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x0A34d399))
            .border(1.dp, RibbonGreen, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                GameIcon(iconId = "ribbon", tint = RibbonGreen, size = 16.dp)
                Text(
                    "Astral Ribbons: ${state.ribbons}",
                    color = RibbonGreen,
                    fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
            }
            Text(
                text = "Earned by reaching Floor 100. Costs scale with owned skills.",
                color = TextSecondary, fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }

    PRESTIGE_SKILLS.forEach { sk ->
        val owned = state.prestige.contains(sk.id)
        val dCost = sk.baseCost + state.prestige.size
        val canBuy = !owned && state.ribbons >= dCost

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x09FFFFFF))
                .border(
                    1.dp,
                    if (owned) RibbonGreen else BorderColor,
                    RoundedCornerShape(12.dp)
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(sk.icon, fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (owned) "${sk.name} ✓" else sk.name,
                    color = if (owned) RibbonGreen else TextPrimary,
                    fontWeight = FontWeight.Bold, fontSize = 11.sp
                )
                Text(sk.desc, color = TextSecondary, fontSize = 10.sp)
                if (!owned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        GameIcon(iconId = "ribbon", tint = TextMuted, size = 10.dp)
                        Text(
                            text = "$dCost (base ${sk.baseCost}+${state.prestige.size})",
                            color = TextMuted, fontSize = 8.sp
                        )
                    }
                }
            }
            Button(
                onClick = { vm.buyPrestige(sk) },
                enabled = canBuy,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 0.dp),
                modifier = Modifier.defaultMinSize(minWidth = 50.dp, minHeight = 44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canBuy) Color(0xFF065f46) else Color(0xFF1e293b),
                    contentColor   = if (canBuy) RibbonGreen else if (owned) RibbonGreen else Color(0xFF374151),
                    disabledContainerColor = Color(0xFF1e293b),
                    disabledContentColor   = if (owned) RibbonGreen else Color(0xFF374151)
                )
            ) {
                if (owned) {
                    Text("✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        GameIcon(iconId = "ribbon", tint = if (canBuy) RibbonGreen else Color(0xFF374151), size = 10.dp)
                        Text("$dCost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Rift Tab ─────────────────────────────────────────────────────────────────

@Composable
fun RiftTab(state: GameUiState, vm: GameViewModel) {
    if (!state.riftUnlocked) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔒 Reach Floor 100 to unlock the Infinite Rift.",
                color = Color(0xFF334155), fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            borderColor = Color(0x667c3aed)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                GameIcon(iconId = "skull", tint = Color(0xFFc4b5fd), size = 14.dp)
                Text("Corruption: ${state.corruption}", color = Color(0xFFc4b5fd),
                    fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text(
                text = "+30%/tier enemy power · +25%/tier loot",
                color = TextSecondary, fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 9.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Button(
                    onClick = { vm.setCorruption(state.corruption - 1) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp),
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1e293b))
                ) {
                    Text("− Lower", color = Color(0xFF94a3b8), fontSize = 12.sp)
                }
                Button(
                    onClick = { vm.setCorruption(state.corruption + 1) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp),
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4c1d95))
                ) {
                    Text("+ Raise", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                GameIcon(iconId = "ribbon", tint = AccentColor, size = 12.dp)
                Text(
                    "LEADERBOARD",
                    color = AccentColor, fontSize = 9.sp,
                    fontFamily = CinzelFamily,
                    letterSpacing = 1.sp
                )
            }
            Text("Best: F${state.bestFloor}", color = TextSecondary, fontSize = 10.sp)
            Text("Daily: F${state.dailyBest}", color = TextSecondary, fontSize = 10.sp)
            Text("AP: ×${String.format("%.2f", state.ancPow)}", color = TextSecondary, fontSize = 10.sp)
            Text("Runs: ${state.totalRuns}", color = TextSecondary, fontSize = 10.sp)
        }
    }
}

// ─── Shared Composables ───────────────────────────────────────────────────────

@Composable
fun ClassCard(
    id: String,
    cls: ClassDef,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = classAccentColor(id)
    val selectedBrush = Brush.verticalGradient(listOf(accent, accent.copy(alpha = 0.2f)))

    DepthCard(
        accentColor = accent,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() },
        borderBrush = if (active) selectedBrush else CardBorderGradient
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Image(
                painter = painterResource(heroDrawableRes(id)),
                contentDescription = cls.name,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
            )
            Text(
                cls.name,
                color = Color(0xFFc4b5fd),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(cls.desc, color = TextSecondary, fontSize = 9.sp,
                modifier = Modifier.padding(top = 2.dp), lineHeight = 12.sp)
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    GameIcon(iconId = "heart", tint = HpHigh, size = 12.dp)
                    Text(cls.hp.toString(), style = textShadowStyle(10.sp, HpHigh))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    GameIcon(iconId = "sword", tint = GoldColor, size = 12.dp)
                    Text(cls.atk.toString(), style = textShadowStyle(10.sp, GoldColor))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    GameIcon(iconId = "shield", tint = FrostColor, size = 12.dp)
                    Text(cls.def.toString(), style = textShadowStyle(10.sp, FrostColor))
                }
            }
        }
    }
}

@Composable
fun WeaponTile(
    id: String,
    w: WeaponDef,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0x386366f1) else Color(0x08FFFFFF))
            .border(
                1.dp,
                if (selected) AccentColor else Color(0xFF1e293b),
                RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(w.icon, fontSize = 18.sp)
        Text(w.name, color = Color(0xFF94a3b8), fontSize = 8.sp,
            modifier = Modifier.padding(top = 2.dp), textAlign = TextAlign.Center)
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderColor,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        // Blurred foundational background layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFF0A0E1A).copy(alpha = 0.85f))
                .blur(radius = 16.dp)
        )
        // Frosted foreground block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0x0CFFFFFF), Color(0x120A0E1A)))
                )
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            content()
        }
    }
}
