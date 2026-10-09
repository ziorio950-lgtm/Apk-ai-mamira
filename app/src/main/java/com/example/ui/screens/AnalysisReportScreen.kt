package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.FoodAnalysis
import com.example.ui.components.AudioWaveform
import com.example.ui.components.GlassCard
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed
import com.example.viewmodel.MainViewModel

@Composable
fun AnalysisReportScreen(
    analysis: FoodAnalysis,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToAlternatives: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpeaking by viewModel.voiceAssistant.isSpeaking.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondary
                )
            }

            // Top Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(FuturisticPurple.copy(alpha = 0.3f))
                    .border(1.dp, FuturisticPurple, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isSpeaking) "المساعد يتحدث الآن..." else "أحلل الآن...",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Box(modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sci-Fi Avatar Hologram Header
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .scale(glowPulse)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.4f), Color.Transparent)))
                    .border(2.dp, NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ai_assistant_avatar),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center Title: تحليل الطعام
        Text(
            text = "تحليل الطعام",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Health Score & Status Tag Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Badge: 86%
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(CardDarkElevated)
                    .border(1.5.dp, HealthyGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${analysis.healthScore}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HealthyGreen
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Status Badge: صحي • متوازن
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(HealthyGreen.copy(alpha = 0.15f))
                    .border(1.dp, HealthyGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "صحي • متوازن",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HealthyGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Food Description Subtext
        Text(
            text = "النوع: ${analysis.detectedName} (${analysis.portionEstimate})",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nutrients Table Matrix (Matching reference design exact items)
        GlassCard(
            borderColor = GlassBorder,
            backgroundColor = CardDark.copy(alpha = 0.88f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NutrientExactRow(
                    label = "السعرات الحرارية",
                    amount = "${analysis.calories} سعرة",
                    indicatorColor = AlertYellow
                )
                NutrientExactRow(
                    label = "البروتين",
                    amount = "${analysis.proteinGrams.toInt()} غرام",
                    indicatorColor = FuturisticPurple
                )
                NutrientExactRow(
                    label = "الدهون الصحية",
                    amount = "${analysis.fatGrams.toInt()} غرام",
                    indicatorColor = HealthyGreen
                )
                NutrientExactRow(
                    label = "الكربوهيدرات",
                    amount = "${analysis.carbsGrams.toInt()} غرام",
                    indicatorColor = NeonBlue
                )
                NutrientExactRow(
                    label = "الألياف",
                    amount = "${analysis.fiberGrams.toInt()} غرام",
                    indicatorColor = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Evaluation Summary Card
        GlassCard(
            borderColor = GlassBorder,
            backgroundColor = CardDark.copy(alpha = 0.88f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "التقييم العام",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = analysis.healthScoreReason.ifEmpty { "وجبة صحية ومتوازنة، تحتوي على بروتين عالي وقليل من الدهون المشبعة." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio Waveform & Speech Playback Bar (Matches Reference Bottom Bar)
        GlassCard(
            borderColor = NeonCyan.copy(alpha = 0.4f),
            backgroundColor = CardDarkElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AudioWaveform(
                    isSpeaking = isSpeaking,
                    barColor = NeonCyan,
                    barCount = 20,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isSpeaking) "جارٍ التحدث..." else "استمع إلى الصوتي",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                            .testTag("play_voice_report_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (isSpeaking) viewModel.stopVoice() else viewModel.speakAnalysis(analysis)
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = SpaceBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom CTA Buttons
        Button(
            onClick = onNavigateToAlternatives,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = SpaceBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("report_view_alternatives_button")
        ) {
            Text(
                text = "عرض البدائل الصحية المقترحة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NutrientExactRow(
    label: String,
    amount: String,
    indicatorColor: Color
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
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
