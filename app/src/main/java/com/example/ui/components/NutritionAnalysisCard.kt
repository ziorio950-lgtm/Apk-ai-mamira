package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FoodAnalysis
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.FuturisticPurple
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.HealthyGreen
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed

/**
 * NutritionAnalysisCard
 * A futuristic glassmorphic card component that displays the complete results returned
 * from the AI Food Recognition & Nutrition service with calories, macros, health score,
 * and audio assistance affordance in a dark neon sci-fi aesthetic.
 */
@Composable
fun NutritionAnalysisCard(
    analysis: FoodAnalysis,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    onOpenFullReport: () -> Unit = {},
    onListenAudio: () -> Unit = {},
    onClose: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon_pulse")
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val scoreColor = when {
        analysis.healthScore >= 80 -> HealthyGreen
        analysis.healthScore >= 65 -> NeonCyan
        analysis.healthScore >= 50 -> AlertYellow
        else -> WarningRed
    }

    // Glassmorphic Outer Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceGlass,
                        CardDark.copy(alpha = 0.94f)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(NeonCyan.copy(alpha = borderGlowAlpha), GlassBorder, NeonBlue.copy(alpha = 0.3f))
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = NeonCyan.copy(alpha = 0.3f),
                spotColor = FuturisticPurple.copy(alpha = 0.3f)
            )
            .padding(18.dp)
            .testTag("nutrition_analysis_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar: Detection Badge, Confidence & Optional Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(scoreColor)
                            .shadow(4.dp, CircleShape, ambientColor = scoreColor, spotColor = scoreColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (analysis.isBeverage) "مشروب ذكي" else "وجبة غذائية",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• نسبة الثقة: ${analysis.confidence}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                if (onClose != null) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Food Name & Health Score Ring Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = analysis.detectedName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${analysis.detectedNameEn} • ${analysis.portionEstimate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Sci-fi Glowing Health Score Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDarkElevated)
                        .border(1.2.dp, scoreColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${analysis.healthScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = "/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Macro Metrics Matrix (Glass Capsules with Neon Colors)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroGlassItem(
                    label = "السعرات",
                    value = "${analysis.calories}",
                    unit = "kcal",
                    color = AlertYellow,
                    modifier = Modifier.weight(1f)
                )
                MacroGlassItem(
                    label = "بروتين",
                    value = "${analysis.proteinGrams.toInt()}",
                    unit = "g",
                    color = HealthyGreen,
                    modifier = Modifier.weight(1f)
                )
                MacroGlassItem(
                    label = "كارب",
                    value = "${analysis.carbsGrams.toInt()}",
                    unit = "g",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MacroGlassItem(
                    label = "دهون",
                    value = "${analysis.fatGrams.toInt()}",
                    unit = "g",
                    color = ElectricMagenta,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Health Summary / Explanation Snippet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDarkElevated.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = analysis.healthScoreReason.ifEmpty { analysis.explanation },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Action Button: Full Details Report
                NeonButton(
                    text = "عرض التقرير الكامل",
                    onClick = onOpenFullReport,
                    glowColor = NeonCyan,
                    modifier = Modifier.weight(1f),
                    testTag = "card_open_full_report_btn"
                )

                // Secondary Audio Assistant Playback Capsule
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDarkElevated)
                        .border(1.dp, if (isSpeaking) NeonCyan else GlassBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onListenAudio)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = if (isSpeaking) NeonCyan else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSpeaking) {
                            Spacer(modifier = Modifier.width(6.dp))
                            AudioWaveform(isSpeaking = true, barCount = 6, barColor = NeonCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroGlassItem(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDarkElevated)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 1.dp, bottom = 1.dp)
                )
            }
        }
    }
}
