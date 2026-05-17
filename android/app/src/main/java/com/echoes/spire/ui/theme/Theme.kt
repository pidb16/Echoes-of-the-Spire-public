package com.echoes.spire.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.echoes.spire.R

// ── Google Fonts ──────────────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

val CinzelFamily = FontFamily(
    Font(googleFont = GoogleFont("Cinzel"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Cinzel"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Cinzel Decorative"), fontProvider = provider, weight = FontWeight.Bold),
)

val InterFamily = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Bold),
)

// ── Color Tokens ──────────────────────────────────────────────────────────────

val AccentColor    = Color(0xFF6366f1)
val AccentMuted    = Color(0xFF3730a3)
val GoldColor      = Color(0xFFfbbf24)
val GoldDim        = Color(0xFF78350f)
val SoulsColor     = Color(0xFFa78bfa)
val HpHigh         = Color(0xFF22c55e)
val HpMid          = Color(0xFFf59e0b)
val HpLow          = Color(0xFFef4444)
val TextPrimary    = Color(0xFFe2e8f0)
val TextSecondary  = Color(0xFF94a3b8)
val TextMuted      = Color(0xFF475569)
val SurfaceDark    = Color(0xFF0a0e1a)
val SurfaceCard    = Color(0xFF0f1628)
val ScreenBg       = Color(0xFF07080f)
val PoisonColor    = Color(0xFFa3e635)
val FrostColor     = Color(0xFF93c5fd)
val FireColor      = Color(0xFFf97316)

// Kept for backwards compatibility with existing callers
val AccentIndigo   = AccentColor
val BgDark         = ScreenBg
val BorderColor    = Color(0x2F6366f1)
val RibbonGreen    = Color(0xFF34d399)

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

// ── Gradient Brushes ──────────────────────────────────────────────────────────

val GoldGradient     = Brush.horizontalGradient(listOf(Color(0xFFf59e0b), Color(0xFFfcd34d)))
val DangerGradient   = Brush.horizontalGradient(listOf(Color(0xFFb91c1c), Color(0xFFef4444)))
val HealthGradient   = Brush.horizontalGradient(listOf(Color(0xFF15803d), Color(0xFF22c55e)))
val MidHpGradient    = Brush.horizontalGradient(listOf(Color(0xFF92400e), Color(0xFFf59e0b)))
val FrostGradient    = Brush.horizontalGradient(listOf(Color(0xFF1d4ed8), Color(0xFF93c5fd)))
val PoisonGradient   = Brush.horizontalGradient(listOf(Color(0xFF365314), Color(0xFFa3e635)))
val SpireGradient    = Brush.verticalGradient(listOf(Color(0xFF1a0533), Color(0xFF07080f)))
val AccentGradient   = Brush.verticalGradient(listOf(AccentColor, AccentMuted))
val BurstGradient    = Brush.horizontalGradient(listOf(Color(0xFFb45309), Color(0xFFf59e0b), Color(0xFFfcd34d), Color(0xFFf59e0b)))

// Multi-layer card border gradients
val CardBorderGradient   = Brush.verticalGradient(listOf(AccentColor, AccentColor.copy(alpha = 0.15f)))
val GoldBorderGradient   = Brush.verticalGradient(listOf(GoldColor, GoldColor.copy(alpha = 0.1f)))
val DangerBorderGradient = Brush.verticalGradient(listOf(HpLow, HpLow.copy(alpha = 0.1f)))

// ── Typography ────────────────────────────────────────────────────────────────

val EchoesTypography = Typography(
    displayLarge  = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Bold,   fontSize = 28.sp, letterSpacing = 1.sp),
    displayMedium = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Bold,   fontSize = 22.sp, letterSpacing = 0.8.sp),
    displaySmall  = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, letterSpacing = 0.5.sp),
    headlineLarge = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Bold,   fontSize = 16.sp, letterSpacing = 0.5.sp),
    headlineMedium= TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 0.3.sp),
    titleLarge    = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Bold,   fontSize = 13.sp, letterSpacing = 0.3.sp),
    titleMedium   = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.2.sp),
    titleSmall    = TextStyle(fontFamily = InterFamily,  fontWeight = FontWeight.Bold,   fontSize = 11.sp),
    bodyLarge     = TextStyle(fontFamily = InterFamily,  fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodyMedium    = TextStyle(fontFamily = InterFamily,  fontWeight = FontWeight.Normal, fontSize = 12.sp),
    bodySmall     = TextStyle(fontFamily = InterFamily,  fontWeight = FontWeight.Normal, fontSize = 10.sp),
    labelLarge    = TextStyle(fontFamily = InterFamily,  fontWeight = FontWeight.Bold,   fontSize = 11.sp, letterSpacing = 0.5.sp),
    labelMedium   = TextStyle(fontFamily = InterFamily,  fontWeight = FontWeight.Medium, fontSize = 9.sp,  letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontFamily = InterFamily,  fontWeight = FontWeight.Normal, fontSize = 8.sp,  letterSpacing = 0.3.sp),
)

// ── Color Scheme ──────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary        = AccentColor,
    onPrimary      = TextPrimary,
    secondary      = GoldColor,
    onSecondary    = SurfaceDark,
    background     = ScreenBg,
    onBackground   = TextPrimary,
    surface        = SurfaceCard,
    onSurface      = TextPrimary,
    surfaceVariant = Color(0xFF1e293b),
    error          = HpLow,
)

@Composable
fun EchoesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = EchoesTypography,
        content     = content
    )
}
