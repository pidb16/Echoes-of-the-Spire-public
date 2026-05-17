package com.echoes.spire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoes.spire.data.fmtN
import com.echoes.spire.ui.GlassProgressBar
import com.echoes.spire.ui.GradientBorderCard
import com.echoes.spire.ui.StatBadge
import com.echoes.spire.ui.theme.*

@Composable
fun UpgradeRow(
    icon: String,
    name: String,
    desc: String,
    level: Int,
    maxLevel: Int,
    cost: Int,
    currency: String,
    balance: Int,
    accent: Color = AccentColor,
    onBuy: () -> Unit
) {
    val maxed = level >= maxLevel
    val canBuy = balance >= cost && !maxed

    GradientBorderCard(
        borderBrush = if (maxed) {
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(RibbonGreen, RibbonGreen.copy(alpha = 0.15f))
            )
        } else {
            CardBorderGradient
        },
        backgroundColor = SurfaceCard,
        cornerRadius = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = icon, fontSize = 20.sp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = desc, color = TextSecondary, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                // Glass progress bar
                GlassProgressBar(
                    progress = if (maxLevel > 0) level.toFloat() / maxLevel else 0f,
                    fillBrush = if (maxed) {
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(RibbonGreen.copy(alpha = 0.7f), RibbonGreen)
                        )
                    } else {
                        AccentGradient
                    },
                    glowColor = if (maxed) RibbonGreen else AccentColor,
                    modifier = Modifier.fillMaxWidth(),
                    height = 5.dp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (maxed) "MAX" else "$level/$maxLevel",
                    color = if (maxed) RibbonGreen else TextMuted,
                    fontSize = 9.sp
                )
            }

            if (canBuy) {
                // Buyable: gold gradient background button
                GradientBorderCard(
                    borderBrush = GoldBorderGradient,
                    backgroundColor = Color(0xFF1a0e00),
                    cornerRadius = 9.dp
                ) {
                    Button(
                        onClick = onBuy,
                        enabled = true,
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 0.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 54.dp, minHeight = 44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFb45309),
                            contentColor   = Color.White
                        )
                    ) {
                        Text(
                            text = "$currency${fmtN(cost)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onBuy,
                    enabled = canBuy,
                    shape = RoundedCornerShape(9.dp),
                    contentPadding = PaddingValues(horizontal = 11.dp, vertical = 0.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 54.dp, minHeight = 44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1e293b),
                        contentColor   = Color(0xFF374151),
                        disabledContainerColor = Color(0xFF1e293b),
                        disabledContentColor   = if (maxed) RibbonGreen else Color(0xFF374151)
                    )
                ) {
                    Text(
                        text = if (maxed) "✓" else "$currency${fmtN(cost)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
