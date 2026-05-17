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
        // 1. Dark track
        drawRect(Color(0xFF06080f))

        // 2. Fill with brush
        if (animatedProg > 0f) {
            drawRect(brush = fillBrush, size = size.copy(width = size.width * animatedProg))
        }

        // 3. Diagonal gloss highlight — white streak at 15% opacity
        if (animatedProg > 0.05f) {
            val w = size.width * animatedProg
            val h = size.height
            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.Transparent,
                    0.4f to Color.White.copy(alpha = 0.15f),
                    0.55f to Color.White.copy(alpha = 0.08f),
                    1f to Color.Transparent,
                    start = Offset(0f, 0f),
                    end = Offset(w * 0.6f, h)
                ),
                size = size.copy(width = w)
            )
        }

        // 4. Top edge highlight line for depth
        drawRect(
            color = Color.White.copy(alpha = 0.12f),
            topLeft = Offset(0f, 0f),
            size = size.copy(width = size.width * animatedProg, height = size.height * 0.25f)
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
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "burstOffset"
    )

    val burstBrush = if (isCharged) {
        Brush.horizontalGradient(
            0f to Color(0xFF92400e),
            (offset * 0.5f) to Color(0xFFfbbf24),
            ((offset * 0.5f + 0.25f).coerceAtMost(1f)) to Color(0xFFfef3c7),
            ((offset * 0.5f + 0.5f).coerceAtMost(1f)) to Color(0xFFf59e0b),
            1f to Color(0xFF92400e),
        )
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF4338ca), Color(0xFF6366f1), Color(0xFF818cf8)))
    }

    GlassProgressBar(
        progress = progress,
        fillBrush = burstBrush,
        glowColor = if (isCharged) GoldColor else AccentColor,
        modifier = modifier,
        height = height
    )
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
