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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.HealthyAlternative
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.HealthyGreen
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun AlternativesScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("الطعام") }

    val foodAlternatives = listOf(
        FoodAlternativeItem(
            name = "سلطة دجاج مشوي بالأفوكادو",
            details = "250 سعرة | 46 غ بروتين",
            badge = "الأفضل لك",
            badgeColor = HealthyGreen,
            imageRes = R.drawable.sample_food_scan
        ),
        FoodAlternativeItem(
            name = "سمك مشوي مع خضروات",
            details = "336 سعرة | 42 غ بروتين",
            badge = "ممتاز",
            badgeColor = NeonCyan,
            imageRes = R.drawable.sample_food_scan
        ),
        FoodAlternativeItem(
            name = "كينوا مع خضار مشوية",
            details = "310 سعرة | 24 غ بروتين",
            badge = "ممتاز",
            badgeColor = NeonCyan,
            imageRes = R.drawable.sample_food_scan
        )
    )

    val drinksAlternatives = listOf(
        DrinkAlternativeItem("ماء مع الليمون", "0 سعرة", Color(0xFF64B5F6)),
        DrinkAlternativeItem("شاي أخضر", "2 سعرة", Color(0xFF81C784)),
        DrinkAlternativeItem("عصير طبيعي", "80 سعرة", Color(0xFFFFB74D)),
        DrinkAlternativeItem("مشروب بروتين", "140 سعرة", Color(0xFFBA68C8))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(16.dp)
    ) {
        // Top Navigation Bar
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
                text = "بدائل صحية مقترحة",
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

        // Food / Drinks Toggle Tabs (matching reference exact pills)
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
                    ToggleChip(
                        label = "الطعام",
                        isSelected = selectedCategory == "الطعام",
                        onClick = { selectedCategory = "الطعام" }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ToggleChip(
                        label = "المشروبات",
                        isSelected = selectedCategory == "المشروبات",
                        onClick = { selectedCategory = "المشروبات" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Food Alternatives List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(foodAlternatives) { item ->
                FoodAlternativeCard(item = item)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Drinks row section
                Text(
                    text = "مشروبات صحية مقترحة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    drinksAlternatives.forEach { drink ->
                        DrinkMiniPillCard(drink = drink)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom CTA button
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
                .testTag("more_options_button")
        ) {
            Text(
                text = "عرض المزيد من الخيارات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class FoodAlternativeItem(
    val name: String,
    val details: String,
    val badge: String,
    val badgeColor: Color,
    val imageRes: Int
)

data class DrinkAlternativeItem(
    val name: String,
    val cal: String,
    val tintColor: Color
)

@Composable
fun FoodAlternativeCard(item: FoodAlternativeItem) {
    GlassCard(
        borderColor = GlassBorder,
        backgroundColor = CardDark.copy(alpha = 0.85f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Food Avatar Image
            Box(
                modifier = Modifier
                    .size(62.dp)
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Badge Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.badgeColor.copy(alpha = 0.2f))
                    .border(1.dp, item.badgeColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = item.badgeColor
                )
            }
        }
    }
}

@Composable
fun DrinkMiniPillCard(drink: DrinkAlternativeItem) {
    Box(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardDarkElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(drink.tintColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(drink.tintColor)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = drink.name,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ToggleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) NeonCyan else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) SpaceBlack else TextSecondary
        )
    }
}
