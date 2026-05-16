@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.echoes.spire.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoes.spire.R
import com.echoes.spire.data.*
import com.echoes.spire.game.GameUiState
import com.echoes.spire.game.GameViewModel
import com.echoes.spire.ui.theme.*

// ─── Glow Progress Bar ────────────────────────────────────────────────────────

@Composable
fun GlowProgressBar(
    progress: Float,
    brush: Brush,
    glowColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "glowProgress"
    )
    Canvas(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
    ) {
        // Dark track
        drawRect(color = Color(0xFF0a0e1a))
        // Filled portion with brush
        if (animatedProgress > 0f) {
            drawRect(brush = brush, size = size.copy(width = size.width * animatedProgress))
        }
    }
}

// ─── Icon Circle ──────────────────────────────────────────────────────────────

@Composable
fun IconCircle(emoji: String, size: Dp = 56.dp, glowColor: Color = AccentColor) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(glowColor.copy(alpha = 0.3f), Color(0xFF0a0e1a))
                )
            )
            .border(1.dp, glowColor.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.5f).sp)
    }
}

private val AccentColor = Color(0xFF6366f1)

// ─── Status Chip ──────────────────────────────────────────────────────────────

@Composable
fun StatusChip(emoji: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(emoji, fontSize = 10.sp)
        Text(label, fontSize = 8.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

// ─── Run Screen ───────────────────────────────────────────────────────────────

@Composable
fun RunScreen(state: GameUiState, vm: GameViewModel) {
    val biome = getBiome(state.runFloor)
    val burstReady = state.burstCharge >= state.burstMax
    val heroHpPct = if (state.heroMaxHp > 0) state.heroHp.toFloat() / state.heroMaxHp else 0f
    val lowHp = heroHpPct < 0.20f
    val biomeAccent = Color(android.graphics.Color.parseColor(biome.accentHex))

    Column(modifier = Modifier.fillMaxSize()) {
        // CombatArenaCard — fixed, never scrolls
        CombatArena(state, vm, heroHpPct, biomeAccent, burstReady, lowHp)

        // Everything below scrolls independently
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Relics + Blessings
            if (state.heroRelics.isNotEmpty() || state.heroBlessings.isNotEmpty()) {
                RelicsBlessingsPanel(state)
            }

            // Ornate divider
            Image(
                painter = painterResource(R.drawable.ic_divider_ornate),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(12.dp).padding(horizontal = 8.dp)
            )

            // Combat log
            CombatLogPanel(state, vm)

            // Return to Hub button
            Button(
                onClick = { vm.returnToHub() },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x0AFFFFFF),
                    contentColor   = TextSecondary
                )
            ) {
                Text("💀 Collapse — Return to Hub", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ─── Combat Arena ─────────────────────────────────────────────────────────────

@Composable
fun CombatArena(
    state: GameUiState,
    vm: GameViewModel,
    heroHpPct: Float,
    biomeAccent: Color,
    burstReady: Boolean,
    lowHp: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arenaGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    val borderColor = when {
        burstReady -> GoldColor.copy(alpha = glowAlpha)
        lowHp      -> Color(0xFFEF4444).copy(alpha = glowAlpha)
        else       -> biomeAccent.copy(alpha = glowAlpha)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
            .background(Color(0xD90a0e1a))
            .border(1.dp, borderColor, RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
            .padding(14.dp)
    ) {
        Column {
            // Hero vs Enemy row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroCard(state, heroHpPct, biomeAccent, modifier = Modifier.weight(1f))
                Text("VS", color = Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                EnemyCard(state, modifier = Modifier.weight(1f))
            }

            // Run stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Floor ${state.runFloor} · ${state.killCount} kills", color = TextMuted, fontSize = 9.sp)
                Text(getBiome(state.runFloor).name, color = biomeAccent, fontSize = 9.sp)
                if (state.goldPerMin > 0) {
                    Text("${fmtN(state.goldPerMin)}/m💰", color = TextMuted, fontSize = 9.sp)
                }
            }

            // Burst bar
            BurstSection(state, vm, burstReady)
        }

        // Overlays
        when (state.runPhase) {
            RunPhase.BLESSING   -> BlessingOverlay(state, vm)
            RunPhase.RELIC      -> RelicChestOverlay(state, vm)
            RunPhase.MILESTONE  -> MilestoneRelicOverlay(state, vm)
            RunPhase.DEAD       -> DeadOverlay(state, vm)
            else                -> { /* fighting */ }
        }
    }
}

// ─── Hero Card ────────────────────────────────────────────────────────────────

@Composable
fun HeroCard(state: GameUiState, heroHpPct: Float, biomeAccent: Color, modifier: Modifier = Modifier) {
    val hpBrush = when {
        heroHpPct < 0.20f -> DangerGradient
        heroHpPct < 0.50f -> Brush.horizontalGradient(listOf(Color(0xFFb45309), HpMid))
        else              -> HealthGradient
    }
    val borderColor = if (heroHpPct < 0.20f) Color(0x80EF4444) else Color(0x386366f1)
    val cls = CLASSES[state.heroCls]
    val wep = WEAPONS[state.heroWeapon]
    val staminaPct = if (state.heroStaminaMax > 0) state.heroStamina.toFloat() / state.heroStaminaMax else 0f

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xD90a0e1a))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(heroDrawableRes(state.heroCls)),
            contentDescription = state.heroCls,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
        )
        Text(cls?.name ?: "", color = Color(0xFFc4b5fd), fontWeight = FontWeight.Bold, fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp))
        Text("${wep?.icon ?: ""} ${wep?.name ?: ""}", color = TextSecondary, fontSize = 9.sp)

        // HP bar (glow)
        Spacer(Modifier.height(7.dp))
        GlowProgressBar(
            progress = heroHpPct,
            brush = hpBrush,
            glowColor = if (heroHpPct < 0.20f) HpLow else HpHigh,
            modifier = Modifier.fillMaxWidth(),
            height = 14.dp
        )
        Text(
            text = "${maxOf(0, state.heroHp)} / ${state.heroMaxHp}",
            color = Color(0xFF94a3b8), fontSize = 9.sp,
            modifier = Modifier.padding(top = 3.dp)
        )

        // Stamina bar
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { staminaPct.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (state.heroStaminaDrained) Color(0xFFef4444) else Color(0xFF60a5fa),
            trackColor = Color(0xFF0a0e1a)
        )
        Text(
            text = "⚡${state.heroStamina}/${state.heroStaminaMax}${if (state.heroStaminaDrained) " LOW" else ""}",
            fontSize = 7.sp,
            color = if (state.heroStaminaDrained) HpLow else TextSecondary
        )

        // Paladin holy shield
        if (state.heroCls == "paladin" && state.heroHolyShield > 0) {
            Spacer(Modifier.height(4.dp))
            val shieldPct = if (state.heroHolyShieldMax > 0) state.heroHolyShield.toFloat() / state.heroHolyShieldMax else 0f
            GlowProgressBar(
                progress = shieldPct,
                brush = Brush.horizontalGradient(listOf(Color(0xFFca8a04), Color(0xFFfde68a))),
                glowColor = Color(0xFFfde68a),
                modifier = Modifier.fillMaxWidth(),
                height = 5.dp
            )
            Text("🛡️ ${state.heroHolyShield}", color = Color(0xFFfde68a), fontSize = 8.sp,
                modifier = Modifier.padding(top = 1.dp))
        }

        // Shadowblade stealth
        if (state.heroCls == "shadowblade") {
            Text(
                text = if (state.heroStealthReady) "🌑 STEALTH!" else "🌑 ${state.heroStealthHits}/3",
                color = if (state.heroStealthReady) SoulsColor else TextMuted,
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }

        // Attack bar
        AttackBar(
            progress = state.heroAttackProgress,
            color = biomeAccent,
            label = "ATK",
            stunned = false
        )

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚔️${state.heroAtk}", color = GoldColor, fontSize = 9.sp, modifier = Modifier.padding(end = 6.dp))
            Text("🛡️${state.heroDef}", color = Color(0xFF60a5fa), fontSize = 9.sp)
        }

        if (state.heroSnapFreezeStacks > 0) {
            Text("🧊×${state.heroSnapFreezeStacks}", color = Color(0xFF93c5fd), fontSize = 8.sp,
                modifier = Modifier.padding(top = 1.dp))
        }
    }
}

// ─── Enemy Card ───────────────────────────────────────────────────────────────

@Composable
fun EnemyCard(state: GameUiState, modifier: Modifier = Modifier) {
    if (state.enemyName.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("…advancing…", color = Color(0xFF334155), fontSize = 11.sp)
        }
        return
    }

    val enemyHpPct = if (state.enemyMaxHp > 0) state.enemyHp.toFloat() / state.enemyMaxHp else 0f
    val hpBrush = when {
        state.enemyIsBoss  -> Brush.horizontalGradient(listOf(Color(0xFF92400e), Color(0xFFf59e0b), Color(0xFFf472b6)))
        state.enemyIsElite -> Brush.horizontalGradient(listOf(Color(0xFF9d174d), Color(0xFFf472b6)))
        else               -> DangerGradient
    }
    val borderColor = when {
        state.enemyIsBoss  -> Color(0x66F59E0B)
        state.enemyIsElite -> Color(0x59F472B6)
        else               -> Color(0x38EF4444)
    }
    val enemyGlowColor = when {
        state.enemyIsBoss  -> GoldColor
        state.enemyIsElite -> Color(0xFFf472b6)
        else               -> HpLow
    }

    // Check shatter readiness
    val shatterReady = state.enemyFrozen &&
            (state.heroWeapon == "greatsword" || state.heroWeapon == "warhammer")

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xD9120820))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconCircle(
            emoji = state.enemyIcon,
            size = 48.dp,
            glowColor = enemyGlowColor
        )
        if (state.enemyIsBoss) {
            Text(state.enemyName, color = GoldColor, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                textAlign = TextAlign.Center)
        } else {
            Text(state.enemyName, color = HpLow, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                textAlign = TextAlign.Center)
        }
        if (state.enemyIsElite) Text("⚡ ELITE", color = Color(0xFFfb923c), fontSize = 8.sp)
        if (state.enemyIsBoss)  Text("👑 WARDEN", color = GoldColor, fontSize = 8.sp)

        // HP bar (glow)
        Spacer(Modifier.height(7.dp))
        GlowProgressBar(
            progress = enemyHpPct,
            brush = hpBrush,
            glowColor = enemyGlowColor,
            modifier = Modifier.fillMaxWidth(),
            height = 14.dp
        )
        Text(
            text = "${maxOf(0, state.enemyHp)} / ${state.enemyMaxHp}",
            color = Color(0xFF94a3b8), fontSize = 9.sp,
            modifier = Modifier.padding(top = 3.dp)
        )

        // Status chips
        FlowRow(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (state.enemyPoisoned) StatusChip("☠️", "POISON", Color(0xFF86efac))
            if (state.enemyFrozen)   StatusChip("❄️", "FROZEN", Color(0xFF93c5fd))
            if (state.enemySnapFreezeReady && !state.enemyFrozen) StatusChip("⚡", "SNAP", Color(0xFF60a5fa))
            if (state.enemyStunned)  StatusChip("💫", "STUNNED", GoldColor)
            if (shatterReady)        StatusChip("💥", "SHATTER READY", GoldColor)
        }

        if (!state.enemyPoisoned && state.enemyPoisonBuildup > 4.0) {
            Text("🟢${state.enemyPoisonBuildup.toInt()}%", color = Color(0xFF86efac), fontSize = 8.sp)
        }
        if (!state.enemyFrozen && state.enemyFrostBuildup > 4.0) {
            Text("🔵${state.enemyFrostBuildup.toInt()}%", color = Color(0xFFbfdbfe), fontSize = 8.sp)
        }

        AttackBar(
            progress = state.enemyAttackProgress,
            color = HpLow,
            label = "ATK",
            stunned = state.enemyStunned
        )
    }
}

// ─── Attack Bar ───────────────────────────────────────────────────────────────

@Composable
fun AttackBar(progress: Float, color: Color, label: String, stunned: Boolean) {
    Column(modifier = Modifier.padding(top = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextMuted, fontSize = 8.sp)
            Text(
                text = if (stunned) "STUNNED" else "${(progress * 100).toInt()}%",
                color = if (stunned) GoldColor else color,
                fontSize = 8.sp
            )
        }
        Spacer(Modifier.height(2.dp))
        GlowProgressBar(
            progress = if (stunned) 1f else progress,
            brush = if (stunned) GoldGradient else SolidColor(color),
            glowColor = if (stunned) GoldColor else color,
            modifier = Modifier.fillMaxWidth(),
            height = 4.dp
        )
    }
}

// ─── Burst Section ────────────────────────────────────────────────────────────

@Composable
fun BurstSection(state: GameUiState, vm: GameViewModel, burstReady: Boolean) {
    val burstPct = if (state.burstMax > 0) state.burstCharge.toFloat() / state.burstMax else 0f

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⚡ Burst Charge ${(burstPct * 100).toInt()}%", color = TextSecondary, fontSize = 9.sp)
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (state.autoBurst) Color(0x26F59E0B) else Color.Transparent)
                .border(1.dp, if (state.autoBurst) GoldColor else Color(0xFF334155), RoundedCornerShape(4.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { vm.setAutoBurst(!state.autoBurst) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AUTO ${if (state.autoBurst) "ON" else "OFF"}",
                color = if (state.autoBurst) GoldColor else TextMuted,
                fontSize = 8.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
    }

    GlowProgressBar(
        progress = burstPct,
        brush = if (burstReady) GoldGradient else Brush.horizontalGradient(listOf(AccentIndigo, Color(0xFF818cf8))),
        glowColor = if (burstReady) GoldColor else AccentIndigo,
        modifier = Modifier.fillMaxWidth(),
        height = 10.dp
    )

    Spacer(Modifier.height(6.dp))

    Button(
        onClick = { vm.fireBurst() },
        enabled = burstReady && state.runPhase == RunPhase.FIGHTING,
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (burstReady) Color(0xFFb45309) else Color(0xFF1e293b),
            contentColor   = if (burstReady) Color.White else Color(0xFF374151),
            disabledContainerColor = Color(0xFF1e293b),
            disabledContentColor   = Color(0xFF374151)
        )
    ) {
        Text(
            text = if (burstReady) "⚡ BURST STRIKE!" else "⚡ Charging...",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
    }
}

// ─── Relics + Blessings Panel ─────────────────────────────────────────────────

@Composable
fun RelicsBlessingsPanel(state: GameUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x14FFFFFF))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(9.dp)
    ) {
        if (state.heroRelics.isNotEmpty()) {
            Text("RELICS", color = AccentIndigo, fontSize = 8.sp, letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = if (state.heroBlessings.isNotEmpty()) 6.dp else 0.dp)
            ) {
                state.heroRelics.forEach { r ->
                    val accentColor = rarityColor(r.rarity)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0x0AFFFFFF))
                            .drawBehind {
                                // Colored left accent line (3dp wide)
                                drawRect(
                                    color = accentColor,
                                    topLeft = Offset(0f, 0f),
                                    size = size.copy(width = 3.dp.toPx())
                                )
                            }
                            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text("${r.icon} ${r.name}", color = accentColor, fontSize = 9.sp)
                    }
                }
            }
        }
        if (state.heroBlessings.isNotEmpty()) {
            Text("BLESSINGS", color = GoldColor, fontSize = 8.sp, letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.heroBlessings.forEach { bid ->
                    val b = BLESSINGS.find { it.id == bid }
                    if (b != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0x0AFFFFFF))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text("${b.icon} ${b.name}", color = GoldColor, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─── Combat Log Panel ─────────────────────────────────────────────────────────

@Composable
fun CombatLogPanel(state: GameUiState, vm: GameViewModel) {
    val logFilters = listOf("all", "big", "relics")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x14FFFFFF))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LOG", color = Color(0xFF334155), fontSize = 8.sp, letterSpacing = 2.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                logFilters.forEach { f ->
                    val active = state.logFilter == f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (active) Color(0x336366f1) else Color.Transparent)
                            .border(1.dp, if (active) AccentIndigo else Color(0xFF1e293b), RoundedCornerShape(3.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { vm.setLogFilter(f) }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (f) { "all" -> "All"; "big" -> "Big"; else -> "Relics" },
                            color = if (active) Color(0xFFc4b5fd) else TextMuted,
                            fontSize = 7.sp
                        )
                    }
                }
            }
        }

        val filtered = state.combatLog
            .filter { e ->
                when (state.logFilter) {
                    "big"    -> e.type == "big"
                    "relics" -> e.type == "relic"
                    else     -> true
                }
            }
            .take(8)

        filtered.forEachIndexed { i, e ->
            Text(
                text = e.text,
                color = when {
                    i == 0 && e.type == "big"   -> GoldColor
                    i == 0 && e.type == "relic" -> RibbonGreen
                    i == 0                      -> TextPrimary
                    else                        -> TextMuted
                },
                fontSize = 9.sp,
                lineHeight = 12.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

// ─── Overlays ─────────────────────────────────────────────────────────────────

@Composable
fun BlessingOverlay(state: GameUiState, vm: GameViewModel) {
    var overlayClickHandled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xF7050E0E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                "✨ CHOOSE A BLESSING",
                color = GoldColor, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 3
            ) {
                state.blessingChoices?.forEach { b ->
                    Button(
                        onClick = {
                            if (!overlayClickHandled) {
                                overlayClickHandled = true
                                vm.chooseBlessing(b)
                            }
                        },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 85.dp, minHeight = 44.dp)
                            .weight(1f, fill = false),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x266366f1))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(b.icon, fontSize = 22.sp)
                            Text(b.name, color = Color(0xFFc4b5fd), fontWeight = FontWeight.Bold,
                                fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
                            Text(b.desc, color = TextSecondary, fontSize = 9.sp,
                                modifier = Modifier.padding(top = 2.dp), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RelicChestOverlay(state: GameUiState, vm: GameViewModel) {
    var overlayClickHandled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xF7050E0E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            Text("📭", fontSize = 52.sp)
            Text(
                "RELIC VAULT",
                color = GoldColor, fontWeight = FontWeight.Bold, fontSize = 10.sp,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                state.relicChoices?.forEach { r ->
                    val rarColor = rarityColor(r.rarity)
                    val cursed = r.rarity == "cursed"
                    Button(
                        onClick = {
                            if (!overlayClickHandled) {
                                overlayClickHandled = true
                                vm.chooseRelic(r)
                            }
                        },
                        modifier = Modifier.defaultMinSize(minWidth = 100.dp, minHeight = 44.dp).weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cursed) Color(0x20DC2626) else Color(0x26818cf8)
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(r.icon, fontSize = 26.sp)
                            Text(r.name, color = rarColor, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp))
                            Text(r.rarity.uppercase(), color = rarColor, fontSize = 9.sp, letterSpacing = 1.sp)
                            if (cursed) Text("⚠️ CURSED", color = HpLow, fontSize = 9.sp,
                                modifier = Modifier.padding(top = 2.dp))
                            Text(r.desc, color = Color(0xFF94a3b8), fontSize = 9.sp,
                                modifier = Modifier.padding(top = 4.dp), textAlign = TextAlign.Center, lineHeight = 12.sp)
                        }
                    }
                }
            }
            Button(
                onClick = {
                    if (!overlayClickHandled) {
                        overlayClickHandled = true
                        vm.skipRelic()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1e293b))
            ) {
                Text("Skip", color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun MilestoneRelicOverlay(state: GameUiState, vm: GameViewModel) {
    val mr = state.milestoneRelic ?: return
    var overlayClickHandled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xF7050E0E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            Text("⚠️ BOSS RELIC", color = GoldColor, fontSize = 9.sp, letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 8.dp))
            IconCircle(emoji = mr.icon, size = 56.dp, glowColor = GoldColor)
            Spacer(Modifier.height(8.dp))
            Text(mr.name, color = GoldColor, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp))
            Text(mr.desc, color = Color(0xFF94a3b8), fontSize = 10.sp, textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 3.dp))
            Text("Powerful — with a trade-off.", color = HpLow, fontSize = 9.sp,
                modifier = Modifier.padding(bottom = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Button(
                    onClick = {
                        if (!overlayClickHandled) {
                            overlayClickHandled = true
                            vm.chooseMilestone(mr)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFb45309))
                ) {
                    Text("Accept", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        if (!overlayClickHandled) {
                            overlayClickHandled = true
                            vm.skipMilestone()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1e293b))
                ) {
                    Text("Decline", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun DeadOverlay(state: GameUiState, vm: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xF7050E0E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            Text("💀", fontSize = 38.sp, modifier = Modifier.padding(bottom = 6.dp))
            Text("The Hero Has Fallen", color = HpLow, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 3.dp))
            Text("Floor ${state.runFloor} · ${state.killCount} kills", color = TextSecondary, fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 7.dp))
            Text("+💰${fmtN(state.runGold)}", color = GoldColor, fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 1.dp))
            Text("+💜${fmtN(state.runSouls)}", color = SoulsColor, fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp))
            Button(
                onClick = { vm.returnToHub() },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4f46e5))
            ) {
                Text("Return to Hub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
