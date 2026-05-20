@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.echoes.spire.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.blur
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

// ─── Floating damage number helpers ──────────────────────────────────────────

private class FloatingDamage(
    val id: Long,
    val text: String,
    val color: Color,
    val offset: Animatable<Float, AnimationVector1D>,
    val alpha: Animatable<Float, AnimationVector1D>
)

@Composable
fun FloatingDamageNumbers(events: List<com.echoes.spire.data.DamageEvent>, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val seen  = remember { mutableSetOf<Long>() }
    val live  = remember { mutableStateListOf<FloatingDamage>() }

    LaunchedEffect(events) {
        for (evt in events) {
            if (seen.add(evt.id)) {
                val text = when {
                    evt.isBurst -> "⚡ ${fmtN(evt.amount)}"
                    evt.isCrit  -> "CRIT!  ${fmtN(evt.amount)}"
                    else        -> fmtN(evt.amount)
                }
                val color = when {
                    evt.isBurst -> GoldColor
                    evt.isCrit  -> Color(0xFFf97316)
                    else        -> Color(0xFFe2e8f0)
                }
                val fd = FloatingDamage(
                    id     = evt.id,
                    text   = text,
                    color  = color,
                    offset = Animatable(0f),
                    alpha  = Animatable(1f)
                )
                live.add(fd)
                scope.launch {
                    launch { fd.offset.animateTo(-76f, tween(1100, easing = FastOutSlowInEasing)) }
                    delay(380)
                    fd.alpha.animateTo(0f, tween(720))
                    live.remove(fd)
                }
            }
        }
        if (seen.size > 20) seen.clear()
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        live.forEach { fd ->
            Text(
                text = fd.text,
                style = textShadowStyle(
                    fontSize   = if (fd.text.startsWith("CRIT")) 15.sp else 13.sp,
                    color      = fd.color.copy(alpha = fd.alpha.value),
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = CinzelFamily
                ),
                modifier = Modifier.offset(y = fd.offset.value.dp)
            )
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

// ─── Status Chip ──────────────────────────────────────────────────────────────

@Composable
fun StatusChip(emoji: String, label: String, color: Color) {
    val iconId = when (emoji) {
        "☠️" -> "poison"
        "❄️" -> "frost"
        "💫" -> "stun"
        "⚡" -> "lightning"
        "💥" -> "lightning"
        else -> null
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (iconId != null) {
            GameIcon(iconId = iconId, tint = color, size = 10.dp)
        } else {
            Text(emoji, fontSize = 10.sp)
        }
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GradientText(
                        text = "Floor ${state.runFloor}",
                        brush = AccentTextBrush,
                        fontSize = 9.sp,
                        fontFamily = CinzelFamily,
                        letterSpacing = 2.sp
                    )
                    Text("·", color = TextMuted, fontSize = 9.sp)
                    StatBadge("skull", "${state.killCount} kills", TextMuted)
                }
                Text(getBiome(state.runFloor).name, color = biomeAccent, fontSize = 9.sp,
                    fontFamily = CinzelFamily, letterSpacing = 1.sp)
                if (state.goldPerMin > 0) {
                    StatBadge("gold", "${fmtN(state.goldPerMin)}/m", TextMuted)
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
    val hpColor = when {
        heroHpPct < 0.20f -> HpLow
        heroHpPct < 0.50f -> HpMid
        else              -> HpHigh
    }
    val hpBrush = when {
        heroHpPct < 0.20f -> DangerGradient
        heroHpPct < 0.50f -> MidHpGradient
        else              -> HealthGradient
    }
    val cls = CLASSES[state.heroCls]
    val wep = WEAPONS[state.heroWeapon]
    val staminaPct = if (state.heroStaminaMax > 0) state.heroStamina.toFloat() / state.heroStaminaMax else 0f

    // Hit flash animation
    val hitFlash = remember { Animatable(0f) }
    LaunchedEffect(state.heroHitSeq) {
        if (state.heroHitSeq > 0) {
            hitFlash.snapTo(0.55f)
            hitFlash.animateTo(0f, tween(420, easing = FastOutLinearInEasing))
        }
    }

    DepthCard(
        accentColor = biomeAccent,
        modifier = modifier,
        borderBrush = if (heroHpPct < 0.20f) DangerBorderGradient else CardBorderGradient
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(heroDrawableRes(state.heroCls)),
                contentDescription = state.heroCls,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
            )
            Text(
                cls?.name ?: "",
                color = Color(0xFFc4b5fd),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text("${wep?.icon ?: ""} ${wep?.name ?: ""}", color = TextSecondary, fontSize = 9.sp)

            // HP bar (glass)
            Spacer(Modifier.height(7.dp))
            GlassProgressBar(
                progress = heroHpPct,
                fillBrush = hpBrush,
                glowColor = hpColor,
                modifier = Modifier.fillMaxWidth(),
                height = 14.dp
            )
            GradientText(
                text = "${maxOf(0, state.heroHp)} / ${state.heroMaxHp}",
                brush = when {
                    heroHpPct > 0.5f  -> HpHighTextBrush
                    heroHpPct > 0.2f  -> GoldTextBrush
                    else              -> HpLowTextBrush
                },
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 3.dp)
            )

            // Stamina bar (glass)
            Spacer(Modifier.height(4.dp))
            GlassProgressBar(
                progress = staminaPct,
                fillBrush = Brush.horizontalGradient(
                    listOf(
                        if (state.heroStaminaDrained) Color(0xFFb91c1c) else Color(0xFF1d4ed8),
                        if (state.heroStaminaDrained) Color(0xFFef4444) else Color(0xFF60a5fa)
                    )
                ),
                glowColor = if (state.heroStaminaDrained) HpLow else Color(0xFF60a5fa),
                modifier = Modifier.fillMaxWidth(),
                height = 5.dp
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
                GlassProgressBar(
                    progress = shieldPct,
                    fillBrush = Brush.horizontalGradient(listOf(Color(0xFFca8a04), Color(0xFFfde68a))),
                    glowColor = Color(0xFFfde68a),
                    modifier = Modifier.fillMaxWidth(),
                    height = 5.dp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    GameIcon(iconId = "shield", tint = Color(0xFFfde68a), size = 10.dp)
                    Text("${state.heroHolyShield}", color = Color(0xFFfde68a), fontSize = 8.sp)
                }
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

            // Stats row with StatBadge
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatBadge("sword", state.heroAtk.toString(), GoldColor,
                    modifier = Modifier.padding(end = 8.dp))
                StatBadge("shield", state.heroDef.toString(), FrostColor)
            }

            if (state.heroSnapFreezeStacks > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    GameIcon(iconId = "frost", tint = Color(0xFF93c5fd), size = 10.dp)
                    Text("×${state.heroSnapFreezeStacks}", color = Color(0xFF93c5fd), fontSize = 8.sp)
                }
            }
        }
        // Red hit flash overlay
        if (hitFlash.value > 0.01f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFFEF4444).copy(alpha = hitFlash.value))
            )
        }
    }
}

// ─── Enemy Card ───────────────────────────────────────────────────────────────

@Composable
fun EnemyCard(state: GameUiState, modifier: Modifier = Modifier) {
    // Defeated overlay — shown for ~700ms between enemy kills
    if (state.enemyDefeated) {
        DepthCard(
            accentColor = HpLow,
            modifier = modifier.defaultMinSize(minHeight = 120.dp),
            borderBrush = DangerBorderGradient
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameIcon(iconId = "skull", tint = HpLow, size = 36.dp, glowRadius = 8.dp)
                GradientText(
                    text = "DEFEATED",
                    brush = HpLowTextBrush,
                    fontSize = 14.sp,
                    fontFamily = CinzelFamily,
                    letterSpacing = 3.sp
                )
            }
        }
        return
    }

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
    val enemyBorderBrush = when {
        state.enemyIsBoss  -> GoldBorderGradient
        state.enemyIsElite -> Brush.verticalGradient(listOf(Color(0xFFf472b6), Color(0xFFf472b6).copy(alpha = 0.1f)))
        else               -> DangerBorderGradient
    }
    val enemyGlowColor = when {
        state.enemyIsBoss  -> GoldColor
        state.enemyIsElite -> Color(0xFFf472b6)
        else               -> HpLow
    }

    // Check shatter readiness
    val shatterReady = state.enemyFrozen &&
            (state.heroWeapon == "greatsword" || state.heroWeapon == "warhammer")

    DepthCard(
        accentColor = enemyGlowColor,
        modifier = modifier,
        borderBrush = enemyBorderBrush
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconCircle(
                emoji = state.enemyIcon,
                size = 48.dp,
                glowColor = enemyGlowColor
            )
            Text(
                text = state.enemyName,
                color = if (state.enemyIsBoss) GoldColor else HpLow,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            if (state.enemyIsElite) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    GameIcon(iconId = "lightning", tint = Color(0xFFfb923c), size = 10.dp)
                    Text("ELITE", color = Color(0xFFfb923c), fontSize = 8.sp,
                        fontFamily = CinzelFamily, letterSpacing = 1.sp)
                }
            }
            if (state.enemyIsBoss) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    GameIcon(iconId = "ribbon", tint = GoldColor, size = 10.dp)
                    Text("WARDEN", color = GoldColor, fontSize = 8.sp,
                        fontFamily = CinzelFamily, letterSpacing = 1.sp)
                }
            }

            // HP bar (glass)
            Spacer(Modifier.height(7.dp))
            GlassProgressBar(
                progress = enemyHpPct,
                fillBrush = hpBrush,
                glowColor = enemyGlowColor,
                modifier = Modifier.fillMaxWidth(),
                height = 14.dp
            )
            GradientText(
                text = "${maxOf(0, state.enemyHp)} / ${state.enemyMaxHp}",
                brush = when {
                    enemyHpPct > 0.5f -> HpHighTextBrush
                    enemyHpPct > 0.2f -> GoldTextBrush
                    else              -> HpLowTextBrush
                },
                fontSize = 9.sp,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    GameIcon(iconId = "poison", tint = Color(0xFF86efac), size = 8.dp)
                    Text("${state.enemyPoisonBuildup.toInt()}%", color = Color(0xFF86efac), fontSize = 8.sp)
                }
            }
            if (!state.enemyFrozen && state.enemyFrostBuildup > 4.0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    GameIcon(iconId = "frost", tint = Color(0xFFbfdbfe), size = 8.dp)
                    Text("${state.enemyFrostBuildup.toInt()}%", color = Color(0xFFbfdbfe), fontSize = 8.sp)
                }
            }

            AttackBar(
                progress = state.enemyAttackProgress,
                color = HpLow,
                label = "ATK",
                stunned = state.enemyStunned
            )
        }
        // Floating damage numbers — centred on the card, animate upward
        FloatingDamageNumbers(
            events = state.damageEvents,
            modifier = Modifier.matchParentSize()
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
        GlassProgressBar(
            progress = if (stunned) 1f else progress,
            fillBrush = if (stunned) GoldGradient else SolidColor(color),
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GameIcon(iconId = "lightning", tint = if (burstReady) GoldColor else TextSecondary, size = 11.dp)
            Text(
                "Burst Charge ${(burstPct * 100).toInt()}%",
                color = TextSecondary, fontSize = 9.sp
            )
        }
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

    BurstProgressBar(
        progress = burstPct,
        isCharged = burstReady,
        modifier = Modifier.fillMaxWidth(),
        height = 10.dp
    )

    Spacer(Modifier.height(6.dp))

    if (burstReady) {
        GradientBorderCard(
            borderBrush = GoldBorderGradient,
            backgroundColor = Color(0xFF1a0e00),
            cornerRadius = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { vm.fireBurst() },
                enabled = burstReady && state.runPhase == RunPhase.FIGHTING,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldColor,
                    contentColor   = Color.Black,
                    disabledContainerColor = Color(0xFF1E293B),
                    disabledContentColor   = Color(0xFF475569)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameIcon(iconId = "lightning", tint = Color.White, size = 16.dp)
                    Text(
                        text = "BURST STRIKE!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        fontFamily = CinzelFamily
                    )
                }
            }
        }
    } else {
        Button(
            onClick = { vm.fireBurst() },
            enabled = false,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E293B),
                contentColor   = Color(0xFF475569),
                disabledContainerColor = Color(0xFF1E293B),
                disabledContentColor   = Color(0xFF475569)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GameIcon(iconId = "lightning", tint = Color(0xFF374151), size = 14.dp)
                Text(
                    text = "Charging...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ─── RunGlassCard — frosted glass primitive for live progression panels ───────

@Composable
fun RunGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFF0A0E1A).copy(alpha = 0.85f))
                .blur(radius = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0x0CFFFFFF), Color(0x120A0E1A)))
                )
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(9.dp)
        ) {
            content()
        }
    }
}

// ─── Relics + Blessings Panel ─────────────────────────────────────────────────

@Composable
fun RelicsBlessingsPanel(state: GameUiState) {
    RunGlassCard(modifier = Modifier.fillMaxWidth()) {
        if (state.heroRelics.isNotEmpty()) {
            Text(
                "RELICS",
                color = AccentColor,
                fontSize = 8.sp,
                fontFamily = CinzelFamily,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
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
            Text(
                "BLESSINGS",
                color = GoldColor,
                fontSize = 8.sp,
                fontFamily = CinzelFamily,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
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

    RunGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "LOG",
                color = Color(0xFF334155),
                fontSize = 8.sp,
                fontFamily = CinzelFamily,
                letterSpacing = 2.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                logFilters.forEach { f ->
                    val active = state.logFilter == f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (active) Color(0x336366f1) else Color.Transparent)
                            .border(1.dp, if (active) AccentColor else Color(0xFF1e293b), RoundedCornerShape(3.dp))
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
            if (e.type == "big") {
                Text(
                    text = e.text,
                    style = textShadowStyle(9.sp, GoldColor),
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            } else {
                Text(
                    text = e.text,
                    color = when {
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
                fontFamily = CinzelFamily,
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
                fontFamily = CinzelFamily,
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
            Text(
                "⚠️ BOSS RELIC",
                color = GoldColor, fontSize = 9.sp, letterSpacing = 4.sp,
                fontFamily = CinzelFamily,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
            GameIcon(iconId = "skull", tint = HpLow, size = 38.dp, glowRadius = 8.dp)
            Spacer(Modifier.height(6.dp))
            Text("The Hero Has Fallen", color = HpLow, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                fontFamily = CinzelFamily, modifier = Modifier.padding(bottom = 3.dp))
            Text(
                "Floor ${state.runFloor} · ${state.killCount} kills",
                color = TextSecondary, fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 7.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 1.dp)
            ) {
                GameIcon(iconId = "gold", tint = GoldColor, size = 12.dp)
                GradientText(text = "+${fmtN(state.runGold)}", brush = GoldTextBrush, fontSize = 12.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                GameIcon(iconId = "soul", tint = SoulsColor, size = 12.dp)
                GradientText(text = "+${fmtN(state.runSouls)}", brush = SoulsTextBrush, fontSize = 12.sp)
            }
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
