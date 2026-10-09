package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.components.NutritionScoreRing
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.HealthyGreen
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed
import com.example.viewmodel.MainViewModel

@Composable
fun NutritionStatsScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf("العناصر الغذائية") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .verticalScroll(scrollState)
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
                text = "تفاصيل التغذية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Box(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Three Top Tabs (العناصر الغذائية • الفوائد • المخاطر)
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
                    StatTabChip("العناصر الغذائية", selectedTab == "العناصر الغذائية") { selectedTab = "العناصر الغذائية" }
                    Spacer(modifier = Modifier.width(4.dp))
                    StatTabChip("الفوائد", selectedTab == "الفوائد") { selectedTab = "الفوائد" }
                    Spacer(modifier = Modifier.width(4.dp))
                    StatTabChip("المخاطر", selectedTab == "المخاطر") { selectedTab = "المخاطر" }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Giant Circular Glowing Dial (Exact match: 320 سعرة حرارية)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(CardDarkElevated)
                    .border(3.dp, HealthyGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "320",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "سعرة حرارية",
                        style = MaterialTheme.typography.labelSmall,
                        color = HealthyGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Macro Nutrition Table Rows with exact percentages (38g 47%, 28g 25%, 12g 28%, 10% 8g)
        GlassCard(
            borderColor = GlassBorder,
            backgroundColor = CardDark.copy(alpha = 0.88f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NutritionDetailExactRow(label = "بروتين", amount = "38g", percent = "47%", barColor = HealthyGreen)
                NutritionDetailExactRow(label = "كربوهيدرات", amount = "28g", percent = "25%", barColor = AlertYellow)
                NutritionDetailExactRow(label = "دهون صحية", amount = "12g", percent = "28%", barColor = ElectricMagenta)
                NutritionDetailExactRow(label = "ألياف", amount = "8g", percent = "10%", barColor = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Health Benefits Grid Section (matching reference icons)
        Text(
            text = "الفوائد الصحية",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BenefitBox(title = "عضلية", sub = "بناء واستشفاء", modifier = Modifier.weight(1f))
            BenefitBox(title = "طاقة", sub = "نشاط مستمر", modifier = Modifier.weight(1f))
            BenefitBox(title = "هضمية", sub = "تحسين الأيض", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom CTA Button (إضافة إلى المفضلة)
        Button(
            onClick = {},
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = SpaceBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "إضافة إلى المفضلة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NutritionDetailExactRow(
    label: String,
    amount: String,
    percent: String,
    barColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = percent,
                style = MaterialTheme.typography.labelSmall,
                color = barColor
            )
        }
    }
}

@Composable
fun BenefitBox(
    title: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDarkElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun StatTabChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
