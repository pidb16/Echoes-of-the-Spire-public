package com.echoes.spire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    accent: Color = AccentIndigo,
    onBuy: () -> Unit
) {
    val maxed = level >= maxLevel
    val canBuy = balance >= cost && !maxed
    val borderColor = if (maxed) Color(0xFF065f46) else BorderColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x09FFFFFF))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TextSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SurfaceDark)
            ) {
                val pct = if (maxLevel > 0) level.toFloat() / maxLevel else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (maxed) RibbonGreen else accent)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (maxed) "MAX" else "$level/$maxLevel",
                color = if (maxed) RibbonGreen else TextMuted,
                fontSize = 9.sp
            )
        }

        Button(
            onClick = onBuy,
            enabled = canBuy,
            shape = RoundedCornerShape(9.dp),
            contentPadding = PaddingValues(horizontal = 11.dp, vertical = 0.dp),
            modifier = Modifier.defaultMinSize(minWidth = 54.dp, minHeight = 44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canBuy) accent else Color(0xFF1e293b),
                contentColor   = if (canBuy) Color.White else Color(0xFF374151),
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
