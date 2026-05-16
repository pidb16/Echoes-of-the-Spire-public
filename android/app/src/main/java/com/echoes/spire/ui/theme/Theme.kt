package com.echoes.spire.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Brand Colors ─────────────────────────────────────────────────────────────

val BgDark        = Color(0xFF07080f)
val SurfaceDark   = Color(0xFF0a0e1a)
val AccentIndigo  = Color(0xFF6366f1)
val AccentPurple  = Color(0xFF7c3aed)
val GoldColor     = Color(0xFFfbbf24)
val SoulsColor    = Color(0xFFa78bfa)
val HpHigh        = Color(0xFF22c55e)
val HpMid         = Color(0xFFf59e0b)
val HpLow         = Color(0xFFef4444)
val TextPrimary   = Color(0xFFe2e8f0)
val TextSecondary = Color(0xFF64748b)
val TextMuted     = Color(0xFF475569)
val BorderColor   = Color(0x2F6366f1) // rgba(99,102,241,.18)
val RibbonGreen   = Color(0xFF34d399)

// Rarity colors
val RarityCommon   = Color(0xFF9ca3af)
val RarityUncommon = Color(0xFF34d399)
val RarityRare     = Color(0xFF818cf8)
val RarityMythic   = Color(0xFFf59e0b)
val RarityCursed   = Color(0xFFdc2626)

fun rarityColor(rarity: String): Color = when (rarity) {
    "common"   -> RarityCommon
    "uncommon" -> RarityUncommon
    "rare"     -> RarityRare
    "mythic"   -> RarityMythic
    "cursed"   -> RarityCursed
    else       -> Color.White
}

// ─── Gradient Brushes ─────────────────────────────────────────────────────────

val SpireGradient   = Brush.verticalGradient(listOf(Color(0xFF1a0533), Color(0xFF07080f)))
val GoldGradient    = Brush.horizontalGradient(listOf(Color(0xFFf59e0b), Color(0xFFfcd34d)))
val DangerGradient  = Brush.horizontalGradient(listOf(Color(0xFFb91c1c), Color(0xFFef4444)))
val HealthGradient  = Brush.horizontalGradient(listOf(Color(0xFF15803d), Color(0xFF22c55e)))
val FrostGradient   = Brush.horizontalGradient(listOf(Color(0xFF1d4ed8), Color(0xFF93c5fd)))
val PoisonGradient  = Brush.horizontalGradient(listOf(Color(0xFF365314), Color(0xFFa3e635)))

// ─── Dark Color Scheme ────────────────────────────────────────────────────────

private val darkColorScheme = darkColorScheme(
    primary          = AccentIndigo,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF1e1b4b),
    secondary        = SoulsColor,
    onSecondary      = Color.White,
    tertiary         = GoldColor,
    background       = BgDark,
    onBackground     = TextPrimary,
    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = Color(0xFF0f1628),
    onSurfaceVariant = TextSecondary,
    outline          = Color(0xFF334155)
)

@Composable
fun EchoesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content
    )
}
