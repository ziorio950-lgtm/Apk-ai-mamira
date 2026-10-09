package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.FoodAnalysis
import com.example.ui.components.GlassCard
import com.example.ui.screens.AlternativesScreen
import com.example.ui.screens.AnalysisReportScreen
import com.example.ui.screens.AssistantChatScreen
import com.example.ui.screens.BeverageAnalysisScreen
import com.example.ui.screens.CameraPermissionDialogScreen
import com.example.ui.screens.CameraScannerScreen
import com.example.ui.screens.HistoryScansScreen
import com.example.ui.screens.NutritionStatsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NutriAITheme
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CameraViewModel
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NutriAITheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    NutriAIApp(
                        viewModel = viewModel,
                        cameraViewModel = cameraViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun NutriAIApp(
    viewModel: MainViewModel,
    cameraViewModel: CameraViewModel
) {
    var showSplash by remember { mutableStateOf(true) }
    var showPermissionExplanation by remember { mutableStateOf(false) }
    var viewingReportForScan by remember { mutableStateOf<FoodAnalysis?>(null) }
    val currentNavIndex by viewModel.currentNavIndex.collectAsStateWithLifecycle()

    if (showSplash) {
        SplashScreen(
            onDismiss = {
                showSplash = false
                showPermissionExplanation = true
            }
        )
    } else if (showPermissionExplanation) {
        CameraPermissionDialogScreen(
            onPermissionGranted = {
                showPermissionExplanation = false
                viewModel.setNavIndex(0)
            },
            onDismiss = {
                showPermissionExplanation = false
                viewModel.setNavIndex(0)
            }
        )
    } else if (viewingReportForScan != null) {
        AnalysisReportScreen(
            analysis = viewingReportForScan!!,
            viewModel = viewModel,
            onBack = { viewingReportForScan = null },
            onNavigateToAlternatives = {
                viewingReportForScan = null
                viewModel.setNavIndex(3)
            },
            onNavigateToAssistant = {
                viewingReportForScan = null
                viewModel.setNavIndex(2)
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                FuturisticBottomNavigationBar(
                    selectedIndex = currentNavIndex,
                    onSelect = { index -> viewModel.setNavIndex(index) }
                )
            },
            containerColor = SpaceBlack
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentNavIndex, label = "screen_fade") { targetIndex ->
                    when (targetIndex) {
                        0 -> CameraScannerScreen(
                            viewModel = viewModel,
                            cameraViewModel = cameraViewModel,
                            onOpenFullReport = { scan -> viewingReportForScan = scan }
                        )
                        1 -> HistoryScansScreen(
                            viewModel = viewModel,
                            onSelectScan = { scan -> viewingReportForScan = scan }
                        )
                        2 -> AssistantChatScreen(viewModel = viewModel)
                        3 -> AlternativesScreen(viewModel = viewModel)
                        4 -> BeverageAnalysisScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setNavIndex(0) },
                            onViewAlternatives = { viewModel.setNavIndex(3) }
                        )
                        5 -> NutritionStatsScreen(viewModel = viewModel)
                        6 -> SettingsScreen(viewModel = viewModel)
                        else -> CameraScannerScreen(
                            viewModel = viewModel,
                            cameraViewModel = cameraViewModel,
                            onOpenFullReport = { scan -> viewingReportForScan = scan }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FuturisticBottomNavigationBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("الرئيسية", Icons.Default.CameraAlt, 0, "nav_camera"),
        NavItem("السجل", Icons.Default.History, 1, "nav_history"),
        NavItem("المساعد", Icons.AutoMirrored.Filled.Chat, 2, "nav_assistant"),
        NavItem("البدائل", Icons.Default.AutoAwesome, 3, "nav_alternatives"),
        NavItem("مشروبات", Icons.Default.LocalDrink, 4, "nav_beverages"),
        NavItem("التفاصيل", Icons.Default.BarChart, 5, "nav_stats"),
        NavItem("الإعدادات", Icons.Default.Settings, 6, "nav_settings")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        GlassCard(
            shape = RoundedCornerShape(22.dp),
            backgroundColor = CardDark.copy(alpha = 0.96f),
            borderColor = GlassBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedIndex == item.index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(item.index) }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag(item.testTag)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardDarkElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) NeonCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NeonCyan else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int,
    val testTag: String
)
