package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.FoodAnalysis
import com.example.ui.components.GlassCard
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.HealthyGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed
import com.example.viewmodel.MainViewModel

@Composable
fun HistoryScansScreen(
    viewModel: MainViewModel,
    onSelectScan: (FoodAnalysis) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var activeFilter by remember { mutableStateOf("الكل") }

    val historyItems = listOf(
        HistoryExactItem(
            name = "دجاج مشوي مع خضروات",
            status = "صحي",
            statusColor = HealthyGreen,
            date = "2026/10/09 • 12:45",
            imageRes = R.drawable.sample_food_scan
        ),
        HistoryExactItem(
            name = "عصير برتقال طبيعي",
            status = "مقبول",
            statusColor = AlertYellow,
            date = "2026/10/08 • 09:30",
            imageRes = R.drawable.green_drink_cup
        ),
        HistoryExactItem(
            name = "بيتزا إيطالية بالجبن",
            status = "غير صحي",
            statusColor = WarningRed,
            date = "2026/10/07 • 20:15",
            imageRes = R.drawable.sample_food_scan
        ),
        HistoryExactItem(
            name = "سلطة خضراء طازجة",
            status = "صحي",
            statusColor = HealthyGreen,
            date = "2026/10/08 • 14:10",
            imageRes = R.drawable.sample_food_scan
        ),
        HistoryExactItem(
            name = "قهوة إسبريسو",
            status = "ممتاز",
            statusColor = NeonCyan,
            date = "2026/10/09 • 08:00",
            imageRes = R.drawable.green_drink_cup
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary
                    )
                }
            } else {
                Box(modifier = Modifier.size(24.dp))
            }

            Text(
                text = "سجل التحليلات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Category Tabs (الكل • وجبات • مشروبات • مفضلات)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardDarkElevated)
                    .padding(4.dp)
            ) {
                Row {
                    HistoryFilterChip("الكل", activeFilter == "الكل") { activeFilter = "الكل" }
                    Spacer(modifier = Modifier.width(4.dp))
                    HistoryFilterChip("وجبات", activeFilter == "وجبات") { activeFilter = "وجبات" }
                    Spacer(modifier = Modifier.width(4.dp))
                    HistoryFilterChip("مشروبات", activeFilter == "مشروبات") { activeFilter = "مشروبات" }
                    Spacer(modifier = Modifier.width(4.dp))
                    HistoryFilterChip("مفضلات", activeFilter == "مفضلات") { activeFilter = "مفضلات" }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exact List Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(historyItems) { item ->
                HistoryExactCard(item = item, onClick = {
                    val scan = FoodAnalysis(
                        detectedName = item.name,
                        healthScoreReason = "تحليل محفوظ في السجل",
                        healthScore = when (item.status) {
                            "صحي" -> 88
                            "ممتاز" -> 92
                            "مقبول" -> 72
                            else -> 48
                        }
                    )
                    onSelectScan(scan)
                })
            }
        }
    }
}

data class HistoryExactItem(
    val name: String,
    val status: String,
    val statusColor: Color,
    val date: String,
    val imageRes: Int
)

@Composable
fun HistoryExactCard(item: HistoryExactItem, onClick: () -> Unit) {
    GlassCard(
        borderColor = GlassBorder,
        backgroundColor = CardDark.copy(alpha = 0.88f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Food Avatar Image
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, NeonCyan, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(item.statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = item.statusColor
                    )
                }
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) NeonCyan else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) SpaceBlack else TextSecondary
        )
    }
}
