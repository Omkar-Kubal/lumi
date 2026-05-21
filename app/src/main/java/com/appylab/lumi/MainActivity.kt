package com.appylab.lumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.ui.screens.ChangePasswordScreen
import com.appylab.lumi.ui.screens.ColorAnalysisScreen
import com.appylab.lumi.ui.screens.EditProfileScreen
import com.appylab.lumi.ui.screens.FeatureAnalysisScreen
import com.appylab.lumi.ui.screens.GlowUpResultScreen
import com.appylab.lumi.ui.screens.HomeScreen
import com.appylab.lumi.ui.screens.NotificationsScreen
import com.appylab.lumi.ui.screens.PlaceholderScreen
import com.appylab.lumi.ui.screens.ProfileScreen
import com.appylab.lumi.ui.screens.ResultScreen
import com.appylab.lumi.ui.screens.SavedPalettesScreen
import com.appylab.lumi.ui.screens.ScanHistoryScreen
import com.appylab.lumi.ui.screens.ScanScreen
import com.appylab.lumi.ui.screens.SplashScreen
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.screens.LoginScreen
import com.appylab.lumi.ui.screens.Onboarding1Screen
import com.appylab.lumi.ui.screens.Onboarding2Screen
import com.appylab.lumi.ui.screens.Onboarding3Screen
import com.appylab.lumi.ui.screens.Onboarding4Screen
import com.appylab.lumi.ui.screens.Onboarding5Screen
import com.appylab.lumi.ui.screens.Onboarding6Screen
import com.appylab.lumi.ui.screens.Onboarding7Screen
import com.appylab.lumi.ui.screens.Onboarding8Screen
import com.appylab.lumi.ui.viewmodel.HomeViewModel
import com.appylab.lumi.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

private val NavRose    = Color(0xFFFF637E)
private val NavMuted   = Color(0xFF737373)
private val NavSurface = Color.White

private enum class AppScreen {
    Splash,
    Onboarding1, Onboarding2, Onboarding3, Onboarding4, Onboarding5, Onboarding6, Onboarding7,
    Onboarding8,
    Login,
    Main, Scan, Results, Profile, Notifications, Placeholder,
    ColorAnalysis, GlowUp, FeatureAnalysis,
    EditProfile, ScanHistory, SavedPalettes, ChangePassword
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumiTheme {
                val loginViewModel: LoginViewModel = viewModel()
                val homeViewModel: HomeViewModel = viewModel()
                val homeUiState by homeViewModel.uiState.collectAsState()

                var screen by remember { mutableStateOf(AppScreen.Splash) }
                var placeholderTitle by remember { mutableStateOf("") }
                var placeholderSubtitle by remember { mutableStateOf("This screen is coming soon") }
                var featureFaceAnalysisId by remember { mutableStateOf(0L) }
                var historyResultId by remember { mutableStateOf<Long?>(null) }
                var showNoScanDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(3_000L)
                    screen = AppScreen.Onboarding1
                }

                fun goPlaceholder(title: String, subtitle: String = "This screen is coming soon") {
                    placeholderTitle = title
                    placeholderSubtitle = subtitle
                    screen = AppScreen.Placeholder
                }

                val showBottomBar = screen !in setOf(
                    AppScreen.Splash,
                    AppScreen.Onboarding1, AppScreen.Onboarding2, AppScreen.Onboarding3,
                    AppScreen.Onboarding4, AppScreen.Onboarding5, AppScreen.Onboarding6,
                    AppScreen.Onboarding7, AppScreen.Onboarding8,
                    AppScreen.Login,
                    AppScreen.Scan,
                    AppScreen.ColorAnalysis, AppScreen.GlowUp, AppScreen.FeatureAnalysis,
                    AppScreen.EditProfile, AppScreen.ScanHistory, AppScreen.SavedPalettes,
                    AppScreen.ChangePassword
                )

                if (showNoScanDialog) {
                    AlertDialog(
                        onDismissRequest = { showNoScanDialog = false },
                        title = { Text("No scan yet") },
                        text  = { Text("Scan your face first to unlock this feature.") },
                        confirmButton = {
                            TextButton(onClick = { showNoScanDialog = false; screen = AppScreen.Scan }) {
                                Text("Scan Now")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNoScanDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (screen) {
                        AppScreen.Splash -> SplashScreen()

                        AppScreen.Onboarding1 -> Onboarding1Screen(
                            onNext = { screen = AppScreen.Onboarding2 },
                            onSkip = { screen = AppScreen.Login }
                        )

                        AppScreen.Onboarding2 -> Onboarding2Screen(
                            onBack = { screen = AppScreen.Onboarding1 },
                            onNext = { screen = AppScreen.Onboarding3 }
                        )

                        AppScreen.Onboarding3 -> Onboarding3Screen(
                            onBack = { screen = AppScreen.Onboarding2 },
                            onNext = { screen = AppScreen.Onboarding4 }
                        )

                        AppScreen.Onboarding4 -> Onboarding4Screen(
                            onBack = { screen = AppScreen.Onboarding3 },
                            onNext = { screen = AppScreen.Onboarding5 }
                        )

                        AppScreen.Onboarding5 -> Onboarding5Screen(
                            onBack = { screen = AppScreen.Onboarding4 },
                            onNext = { screen = AppScreen.Onboarding6 }
                        )

                        AppScreen.Onboarding6 -> Onboarding6Screen(
                            onBack = { screen = AppScreen.Onboarding5 },
                            onNext = { _, _, _ -> screen = AppScreen.Onboarding7 }
                        )

                        AppScreen.Onboarding7 -> Onboarding7Screen(
                            onBack           = { screen = AppScreen.Onboarding6 },
                            onContinueGoogle = { screen = AppScreen.Onboarding8 },
                            onContinueEmail  = { screen = AppScreen.Onboarding8 },
                            onSkipToHome     = { screen = AppScreen.Main },
                            onContinue       = { screen = AppScreen.Onboarding8 }
                        )

                        AppScreen.Onboarding8 -> Onboarding8Screen(
                            onStartScan    = { screen = AppScreen.Main },
                            onExploreFirst = { screen = AppScreen.Main }
                        )

                        AppScreen.Login -> LoginScreen(
                            viewModel = loginViewModel,
                            onSuccess = { screen = AppScreen.Main }
                        )

                        AppScreen.Main -> HomeScreen(
                            onStartScanClick   = { screen = AppScreen.Scan },
                            onViewResultsClick = {
                                homeViewModel.clearResultsBadge()
                                screen = AppScreen.Results
                            },
                            onFeatureTileClick = { key ->
                                val lastId = homeUiState.lastScan?.id
                                when (key) {
                                    "color" -> {
                                        if (lastId != null) { featureFaceAnalysisId = lastId; screen = AppScreen.ColorAnalysis }
                                        else showNoScanDialog = true
                                    }
                                    "glowup" -> {
                                        if (lastId != null) { featureFaceAnalysisId = lastId; screen = AppScreen.GlowUp }
                                        else showNoScanDialog = true
                                    }
                                    "features" -> {
                                        if (lastId != null) { featureFaceAnalysisId = lastId; screen = AppScreen.FeatureAnalysis }
                                        else showNoScanDialog = true
                                    }
                                    else -> goPlaceholder(key.replaceFirstChar { it.uppercase() })
                                }
                            },
                            onAvatarClick = { screen = AppScreen.Profile },
                            onBellClick   = { screen = AppScreen.Notifications }
                        )

                        AppScreen.Scan -> ScanScreen(
                            onBack         = { screen = AppScreen.Main },
                            onScanComplete = {
                                homeViewModel.clearResultsBadge()
                                screen = AppScreen.Results
                            }
                        )

                        AppScreen.Results -> ResultScreen(
                            faceAnalysisId = historyResultId,
                            onBack   = { historyResultId = null; screen = AppScreen.Main },
                            onRescan = { historyResultId = null; screen = AppScreen.Scan }
                        )

                        AppScreen.Profile -> ProfileScreen(
                            onViewAllScans      = { screen = AppScreen.Results },
                            onViewScanHistory   = { screen = AppScreen.ScanHistory },
                            onViewSavedPalettes = { screen = AppScreen.SavedPalettes },
                            onEditProfile       = { screen = AppScreen.EditProfile },
                            onSignOut           = { screen = AppScreen.Main }
                        )

                        AppScreen.Notifications -> NotificationsScreen(
                            onBack = { screen = AppScreen.Main },
                            onOpen = { homeViewModel.onBellTapped() }
                        )

                        AppScreen.ColorAnalysis -> ColorAnalysisScreen(
                            faceAnalysisId = featureFaceAnalysisId,
                            onBack = { screen = AppScreen.Main }
                        )

                        AppScreen.GlowUp -> GlowUpResultScreen(
                            faceAnalysisId = featureFaceAnalysisId,
                            onBack = { screen = AppScreen.Main }
                        )

                        AppScreen.FeatureAnalysis -> FeatureAnalysisScreen(
                            faceAnalysisId = featureFaceAnalysisId,
                            onBack = { screen = AppScreen.Main }
                        )

                        AppScreen.EditProfile -> EditProfileScreen(
                            onBack = { screen = AppScreen.Profile },
                            onChangePasswordClick = { screen = AppScreen.ChangePassword }
                        )

                        AppScreen.ChangePassword -> ChangePasswordScreen(
                            onBack = { screen = AppScreen.EditProfile }
                        )

                        AppScreen.ScanHistory -> ScanHistoryScreen(
                            onBack = { screen = AppScreen.Profile },
                            onOpenResult = { id -> historyResultId = id; screen = AppScreen.Results }
                        )

                        AppScreen.SavedPalettes -> SavedPalettesScreen(
                            onBack = { screen = AppScreen.Profile },
                            onOpenColorAnalysis = { id -> featureFaceAnalysisId = id; screen = AppScreen.ColorAnalysis }
                        )

                        AppScreen.Placeholder -> PlaceholderScreen(
                            title    = placeholderTitle,
                            subtitle = placeholderSubtitle,
                            onBack   = { screen = AppScreen.Main }
                        )
                    }

                    if (showBottomBar) {
                        AppBottomBar(
                            modifier        = Modifier.align(Alignment.BottomCenter),
                            currentScreen   = screen,
                            resultsUnviewed = homeUiState.resultsUnviewed,
                            onHomeClick     = { screen = AppScreen.Main },
                            onScanClick     = { screen = AppScreen.Scan },
                            onResultsClick  = {
                                homeViewModel.clearResultsBadge()
                                screen = AppScreen.Results
                            },
                            onProfileClick  = { screen = AppScreen.Profile }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    modifier: Modifier = Modifier,
    currentScreen: AppScreen,
    resultsUnviewed: Boolean,
    onHomeClick: () -> Unit,
    onScanClick: () -> Unit,
    onResultsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = NavSurface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon     = Icons.Filled.Home,
                label    = "Home",
                selected = currentScreen == AppScreen.Main,
                badge    = false,
                onClick  = onHomeClick
            )
            BottomNavItem(
                icon     = Icons.Outlined.CameraAlt,
                label    = "Scan",
                selected = false,
                badge    = false,
                onClick  = onScanClick
            )
            BottomNavItem(
                icon     = Icons.Outlined.BarChart,
                label    = "Results",
                selected = currentScreen == AppScreen.Results,
                badge    = resultsUnviewed,
                onClick  = onResultsClick
            )
            BottomNavItem(
                icon     = Icons.Outlined.Person,
                label    = "Profile",
                selected = currentScreen == AppScreen.Profile,
                badge    = false,
                onClick  = onProfileClick
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    badge: Boolean,
    onClick: () -> Unit
) {
    BadgedBox(
        badge = { if (badge) Badge(containerColor = NavRose) }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (selected) NavRose else NavMuted
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = if (selected) NavRose else NavMuted,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}
