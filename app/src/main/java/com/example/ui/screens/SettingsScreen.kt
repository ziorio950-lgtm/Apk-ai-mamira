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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.HealthyGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isVoiceAutoPlay by viewModel.isVoiceAutoPlay.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var activeDietPreference by remember { mutableStateOf("متوازن") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var historySavingEnabled by remember { mutableStateOf(true) }

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
                text = "الإعدادات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Box(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Analysis Mode Chips (نمط التحليل: متقدم • سريع • تفصيلي)
        Text(
            text = "نمط التحليل",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip("متقدم", isSelected = true) {}
            ModeChip("سريع", isSelected = false) {}
            ModeChip("تفصيلي", isSelected = false) {}
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dietary Preferences Section (تفضيلاتك الغذائية: متوازن • كيتو • نباتي)
        Text(
            text = "تفضيلاتك الغذائية",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DietPill("متوازن", activeDietPreference == "متوازن", modifier = Modifier.weight(1f)) {
                activeDietPreference = "متوازن"
            }
            DietPill("كيتو", activeDietPreference == "كيتو", modifier = Modifier.weight(1f)) {
                activeDietPreference = "كيتو"
            }
            DietPill("نباتي", activeDietPreference == "نباتي", modifier = Modifier.weight(1f)) {
                activeDietPreference = "نباتي"
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Switches List (Exact match with reference design toggles)
        GlassCard(
            borderColor = GlassBorder,
            backgroundColor = CardDark.copy(alpha = 0.88f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SettingSwitchRow(
                    label = "إشعارات التنبيه الصحي",
                    icon = Icons.Default.Notifications,
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                SettingSwitchRow(
                    label = "التحليل الصوتي",
                    icon = Icons.Default.VolumeUp,
                    checked = isVoiceAutoPlay,
                    onCheckedChange = { viewModel.toggleVoiceAutoPlay() }
                )

                SettingSwitchRow(
                    label = "تاريخ التخزين المحلي",
                    icon = Icons.Default.Security,
                    checked = historySavingEnabled,
                    onCheckedChange = { historySavingEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language & Privacy Action Rows
        GlassCard(
            borderColor = GlassBorder,
            backgroundColor = CardDark.copy(alpha = 0.88f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingActionRow(
                    label = "اللغة",
                    value = "العربية (افتراضي)",
                    icon = Icons.Default.Language
                )
                SettingActionRow(
                    label = "حذف سجل البيانات",
                    value = "تنظيف الذاكرة",
                    icon = Icons.Default.Delete
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Branding Footer
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NutriAI v1.0 • أكثر من مجرد تطبيق، إنه رفيقك لصحة ذكية",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun SettingSwitchRow(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CardDarkElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SpaceBlack,
                checkedTrackColor = NeonCyan,
                uncheckedTrackColor = CardDarkElevated
            )
        )
    }
}

@Composable
fun SettingActionRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CardDarkElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun ModeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) NeonCyan else CardDarkElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) SpaceBlack else TextSecondary
        )
    }
}

@Composable
fun DietPill(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardDarkElevated)
            .border(1.dp, if (isSelected) NeonCyan else GlassBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) NeonCyan else TextSecondary
        )
    }
}
