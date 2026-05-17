package com.echoes.spire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.echoes.spire.data.fmtN
import com.echoes.spire.game.AppScreen
import com.echoes.spire.game.GameUiState
import com.echoes.spire.game.GameViewModel
import com.echoes.spire.game.OfflineReward
import com.echoes.spire.ui.*
import com.echoes.spire.ui.theme.*
import com.echoes.spire.data.getBiome

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EchoesTheme {
                EchoesApp()
            }
        }
    }
}

@Composable
fun EchoesApp() {
    val vm: GameViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    val bgColor = when {
        state.screen == AppScreen.RUN -> {
            try {
                val biome = getBiome(state.runFloor)
                Color(android.graphics.Color.parseColor(biome.bgHex))
            } catch (e: Exception) { BgDark }
        }
        else -> BgDark
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {

            // ── Top Info Bar ──
            TopInfoBar(state)

            // ── Screen Content ──
            Box(modifier = Modifier.weight(1f)) {
                when (state.screen) {
                    AppScreen.HUB    -> HubScreen(state, vm)
                    AppScreen.RUN    -> RunScreen(state, vm)
                    AppScreen.ASCEND -> AscendScreen(state, vm)
                }
            }
        }
    }

    // Offline reward dialog
    state.offlineReward?.let { reward ->
        OfflineRewardDialog(reward) { vm.dismissOfflineReward() }
    }
}

// ─── Top Info Bar ─────────────────────────────────────────────────────────────

@Composable
fun TopInfoBar(state: GameUiState) {
    val biome = if (state.screen == AppScreen.RUN) getBiome(state.runFloor) else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xEB07080f))
            .border(
                width = 1.dp,
                color = Color(0x336366f1),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatBadge("gold",   fmtN(state.gold),         GoldColor)
            StatBadge("soul",   fmtN(state.souls),         SoulsColor)
            StatBadge("ribbon", state.ribbons.toString(),  RibbonGreen)
        }

        Text(
            text = if (state.screen == AppScreen.RUN && biome != null)
                "F${state.runFloor} · ${biome.name}"
            else
                "Echoes of the Spire",
            color = AccentColor,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = CinzelFamily,
            letterSpacing = 1.sp
        )
    }
}

// ─── Offline Stat Cell ────────────────────────────────────────────────────────

@Composable
fun OfflineStatCell(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0xFF1e293b), RoundedCornerShape(10.dp))
            .padding(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 18.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(top = 2.dp))
        Text(label, color = TextSecondary, fontSize = 9.sp)
    }
}

// ─── Offline Reward Dialog ────────────────────────────────────────────────────

@Composable
fun OfflineRewardDialog(
    reward: OfflineReward,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0f1628))
                .border(2.dp, AccentColor, RoundedCornerShape(20.dp))
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🌙", fontSize = 36.sp, modifier = Modifier.padding(bottom = 6.dp))
            Text(
                text = "Welcome Back!",
                color = Color(0xFFc4b5fd),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                fontFamily = CinzelFamily,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "The Oracle kept climbing.",
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OfflineStatCell("💰", "Gold",   fmtN(reward.gold),   modifier = Modifier.weight(1f))
                OfflineStatCell("💜", "Souls",  fmtN(reward.souls),  modifier = Modifier.weight(1f))
                OfflineStatCell("🏔️", "Floors", reward.floors.toString(), modifier = Modifier.weight(1f))
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4f46e5)
                )
            ) {
                Text("Claim ✨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
