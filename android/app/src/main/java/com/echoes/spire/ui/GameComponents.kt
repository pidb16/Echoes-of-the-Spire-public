package com.echoes.spire.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoes.spire.R
import com.echoes.spire.ui.theme.*

// ── Icon ID → Drawable mapping ────────────────────────────────────────────────

@DrawableRes
fun gameIconRes(id: String): Int = when (id) {
    "sword"     -> R.drawable.ic_sword
    "shield"    -> R.drawable.ic_shield
    "heart"     -> R.drawable.ic_heart
    "lightning" -> R.drawable.ic_lightning
    "gold"      -> R.drawable.ic_gold_coin
    "soul"      -> R.drawable.ic_soul
    "ribbon"    -> R.drawable.ic_ribbon
    "skull"     -> R.drawable.ic_skull
    "flame"     -> R.drawable.ic_flame
    "frost"     -> R.drawable.ic_snowflake
    "poison"    -> R.drawable.ic_poison
    "stun"      -> R.drawable.ic_stun
    else        -> R.drawable.ic_sword
}

// ── AmbientNoiseLayer — subtle 3% opacity texture overlay ────────────────────
@Composable
fun AmbientNoiseLayer(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val dot = Color.White.copy(alpha = 0.03f)
        val step = 6f
        var x = 0f
        while (x < size.width) {
            var y = 0f
            while (y < size.height) {
                // deterministic pseudo-noise: every 3rd cell in a shifting grid
                val hash = ((x / step).toInt() * 31 + (y / step).toInt() * 17)
                if (hash % 4 == 0) {
                    drawRect(dot, topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(1.5f, 1.5f))
                }
                y += step
            }
            x += step
        }
    }
}

// ── GradientText — brush-filled text with optional drop shadow ───────────────
@Composable
fun GradientText(
    text: String,
    brush: Brush,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Bold,
    fontFamily: androidx.compose.ui.text.font.FontFamily = InterFamily,
    shadow: Boolean = true,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            brush = brush,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            shadow = if (shadow) Shadow(
                color = Color.Black.copy(alpha = 0.85f),
                offset = Offset(0f, 1.5f),
                blurRadius = 3f
            ) else null
        )
    )
}

// ── Pre-made gradient styles ──────────────────────────────────────────────────
val GoldTextBrush     = Brush.linearGradient(listOf(Color(0xFFf59e0b), Color(0xFFfef3c7), Color(0xFFf59e0b)))
val SoulsTextBrush    = Brush.linearGradient(listOf(Color(0xFFa78bfa), Color(0xFFddd6fe), Color(0xFFa78bfa)))
val HpHighTextBrush   = Brush.linearGradient(listOf(Color(0xFF16a34a), Color(0xFF86efac)))
val HpLowTextBrush    = Brush.linearGradient(listOf(Color(0xFFdc2626), Color(0xFFfca5a5)))
val AccentTextBrush   = Brush.linearGradient(listOf(Color(0xFF6366f1), Color(0xFFa5b4fc)))
val RibbonTextBrush   = Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF6ee7b7)))

// ── Combat shadow helpers ─────────────────────────────────────────────────────
val combatShadow = Shadow(color = Color.Black.copy(alpha = 0.9f), offset = Offset(0f, 1.5f), blurRadius = 2f)

fun textShadowStyle(
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    fontWeight: FontWeight = FontWeight.Bold,
    fontFamily: androidx.compose.ui.text.font.FontFamily = InterFamily
) = TextStyle(
    color = color,
    fontSize = fontSize,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    shadow = combatShadow
)

// ── GameIcon — tinted icon with optional glow ─────────────────────────────────

@Composable
fun GameIcon(
    iconId: String,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    glowRadius: Dp = 0.dp
) {
    val iconMod = if (glowRadius > 0.dp) {
        modifier.size(size).drawBehind {
            drawCircle(
                color = tint.copy(alpha = 0.35f),
                radius = size.toPx() * 0.7f,
                center = center
            )
        }
    } else {
        modifier.size(size)
    }
    Icon(
        painter = painterResource(gameIconRes(iconId)),
        contentDescription = iconId,
        tint = tint,
        modifier = iconMod
    )
}

// ── Stat Badge — icon + value in compact row ──────────────────────────────────

@Composable
fun StatBadge(iconId: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        GameIcon(iconId = iconId, tint = tint, size = 12.dp)
        Text(value, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            fontFamily = InterFamily)
    }
}

// ── GlassProgressBar — glossy liquid-crystal bar ─────────────────────────────

@Composable
fun GlassProgressBar(
    progress: Float,
    fillBrush: Brush,
    glowColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    animated: Boolean = true
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProg by if (animated) {
        animateFloatAsState(target, tween(350), label = "bar")
    } else {
        remember { mutableStateOf(target) }.let { s -> s.value = target; s }
    }

    Canvas(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
    ) {
        val w = size.width * animatedProg
        val h = size.height

        // 1. Dark track with subtle inner bevel
        drawRect(Color(0xFF06080f))
        drawRect(
            color = Color.White.copy(alpha = 0.04f),
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(size.width, h * 0.4f)
        )

        if (animatedProg > 0.01f) {
            // 2. Main fill
            drawRect(brush = fillBrush, size = androidx.compose.ui.geometry.Size(w, h))

            // 3. Specular highlight — diagonal white streak
            drawRect(
                brush = Brush.linearGradient(
                    0f   to Color.Transparent,
                    0.3f to Color.White.copy(alpha = 0.18f),
                    0.5f to Color.White.copy(alpha = 0.12f),
                    0.8f to Color.White.copy(alpha = 0.04f),
                    1f   to Color.Transparent,
                    start = Offset(0f, 0f),
                    end   = Offset(w * 0.7f, h)
                ),
                size = androidx.compose.ui.geometry.Size(w, h)
            )

            // 4. Top edge bright line (gloss rim)
            drawRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(w, 1.5f)
            )

            // 5. Fill edge inner glow (right edge of fill)
            if (animatedProg < 0.99f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, glowColor.copy(alpha = 0.5f)),
                        startX = (w - h * 2).coerceAtLeast(0f),
                        endX = w
                    ),
                    size = androidx.compose.ui.geometry.Size(w, h)
                )
            }
        }

        // 6. Bottom inner shadow line
        drawRect(
            color = Color.Black.copy(alpha = 0.35f),
            topLeft = Offset(0f, h - 1.5f),
            size = androidx.compose.ui.geometry.Size(size.width, 1.5f)
        )
    }
}

// ── BurstProgressBar — animated crackling energy bar ─────────────────────────

@Composable
fun BurstProgressBar(
    progress: Float,
    isCharged: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "burst")

    // Shimmer sweep position (0→1)
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue  =  1.4f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "shimmer"
    )

    // Crackling gradient offset
    val crackleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
        label = "crackle"
    )

    val burstBrush = if (isCharged) {
        Brush.horizontalGradient(
            (crackleOffset * 0.3f)                           to Color(0xFF92400e),
            (crackleOffset * 0.3f + 0.2f).coerceAtMost(1f)  to Color(0xFFfbbf24),
            (crackleOffset * 0.3f + 0.45f).coerceAtMost(1f) to Color(0xFFfef3c7),
            (crackleOffset * 0.3f + 0.7f).coerceAtMost(1f)  to Color(0xFFf59e0b),
            1f                                               to Color(0xFF92400e),
        )
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF3730a3), Color(0xFF6366f1), Color(0xFF818cf8)))
    }

    Box(modifier = modifier.height(height)) {
        GlassProgressBar(
            progress   = progress,
            fillBrush  = burstBrush,
            glowColor  = if (isCharged) GoldColor else AccentColor,
            modifier   = Modifier.fillMaxSize(),
            height     = height
        )
        // Shimmer sweep overlay when charged
        if (isCharged) {
            Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(height / 2))) {
                val sweepW = size.width * 0.25f
                val sweepX = size.width * shimmerOffset
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f   to Color.Transparent,
                        0.5f to Color.White.copy(alpha = 0.28f),
                        1f   to Color.Transparent,
                        startX = sweepX,
                        endX   = sweepX + sweepW
                    ),
                    size = size
                )
            }
        }
    }
}

// ── GradientBorderCard — multi-layer gradient border ─────────────────────────

@Composable
fun GradientBorderCard(
    borderBrush: Brush = CardBorderGradient,
    backgroundColor: Color = SurfaceCard,
    cornerRadius: Dp = 14.dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush = borderBrush)  // outer gradient border layer
            .padding(1.dp)                     // border thickness
            .clip(RoundedCornerShape(cornerRadius - 1.dp))
            .background(backgroundColor),
        content = content
    )
}

// ── DepthCard — card with radial illuminated epicenter ────────────────────────

@Composable
fun DepthCard(
    accentColor: Color = AccentColor,
    cornerRadius: Dp = 14.dp,
    modifier: Modifier = Modifier,
    borderBrush: Brush = CardBorderGradient,
    content: @Composable BoxScope.() -> Unit
) {
    GradientBorderCard(
        borderBrush = borderBrush,
        backgroundColor = Color(0xFF0A0D14),
        cornerRadius = cornerRadius,
        modifier = modifier
    ) {
        // Radial illuminated epicenter background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        0f to accentColor.copy(alpha = 0.06f),
                        0.5f to accentColor.copy(alpha = 0.02f),
                        1f to Color.Transparent
                    )
                )
        )
        content()
    }
}
