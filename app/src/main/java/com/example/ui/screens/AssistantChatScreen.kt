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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
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
import com.example.ui.components.AudioWaveform
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.FuturisticPurple
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun AssistantChatScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSpeaking by viewModel.voiceAssistant.isSpeaking.collectAsStateWithLifecycle()
    val selectedAnalysis by viewModel.selectedAnalysis.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_avatar")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                text = "المساعد الذكي",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Box(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Cybernetic Avatar Portrait (matching reference exactly)
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(avatarScale)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.4f), Color.Transparent)))
                .border(2.5.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ai_assistant_avatar),
                contentDescription = "AI Assistant",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Message Bubble 1
        GlassCard(
            borderColor = NeonCyan.copy(alpha = 0.4f),
            backgroundColor = CardDark.copy(alpha = 0.9f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "مرحباً، هنا تحليل طعامك!\nالطبق الذي أمامك يحتوي على سعرات حرارية متوازنة وهذا يعتبر مناسباً لوجبتك الحالية.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Message Bubble 2 (Question prompt)
        GlassCard(
            borderColor = FuturisticPurple.copy(alpha = 0.4f),
            backgroundColor = CardDarkElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "هل ترغب في معرفة أكل أكثر صحية أو وصفات مشابهة؟",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeonCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Animated Audio Waveform (Matching reference position)
        AudioWaveform(
            isSpeaking = isSpeaking,
            barColor = NeonCyan,
            barCount = 32,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSpeaking) "المساعد يتحدث الآن..." else "تحدث الآن...",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Glowing Voice Mic Orb (matching reference bottom button)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(NeonCyan, CardDarkElevated)
                    )
                )
                .border(2.dp, NeonCyan, CircleShape)
                .testTag("voice_assistant_mic_button")
        ) {
            IconButton(
                onClick = {
                    if (isSpeaking) {
                        viewModel.stopVoice()
                    } else {
                        val sampleText = "مرحباً بك! طبقك الحالي يحتوي على بروتين ممتاز وألياف مغذية، هل تريد بدائل لتقليل السعرات؟"
                        viewModel.voiceAssistant.speak(sampleText)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Speak",
                    tint = SpaceBlack,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
