package com.echoes.spire.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoes.spire.game.AppScreen
import com.echoes.spire.game.GameUiState
import com.echoes.spire.game.GameViewModel
import com.echoes.spire.ui.theme.*

@Composable
fun AscendScreen(state: GameUiState, vm: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌅", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))

        Text(
            text = "Ascension ${state.ascensions + 1}",
            color = GoldColor,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        Text(
            text = "All gold, souls, forge and research are sacrificed. Ancient Power grows ×2.5 permanently.",
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(bottom = 12.dp)
        )

        Text(
            text = "New AP: ×${String.format("%.2f", state.ancPow * 2.5)}",
            color = Color(0xFFf472b6),
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Cost summary
        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("You will lose:", color = TextSecondary, fontSize = 11.sp)
            Text("• All Gold (reset to 60)", color = Color(0xFFfbbf24), fontSize = 11.sp)
            Text("• All Souls (reset to 0)", color = SoulsColor, fontSize = 11.sp)
            Text("• All Forge upgrades", color = TextMuted, fontSize = 11.sp)
            Text("• All Research upgrades", color = TextMuted, fontSize = 11.sp)
        }

        // Keeping list
        Column(
            modifier = Modifier.padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("You will keep:", color = TextSecondary, fontSize = 11.sp)
            Text("• Prestige (Astral) skills", color = RibbonGreen, fontSize = 11.sp)
            Text("• Astral Ribbons", color = RibbonGreen, fontSize = 11.sp)
            Text("• Best floor record", color = RibbonGreen, fontSize = 11.sp)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { vm.setScreen(AppScreen.HUB) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1e293b),
                    contentColor = Color(0xFF94a3b8)
                )
            ) {
                Text("← Back", fontSize = 12.sp)
            }
            Button(
                onClick = { vm.doAscend() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFb45309),
                    contentColor = Color(0xFFfef3c7)
                )
            ) {
                Text("🌅 Ascend", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
