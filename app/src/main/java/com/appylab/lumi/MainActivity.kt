package com.appylab.lumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.ui.screens.HomeScreen
import com.appylab.lumi.ui.screens.OnboardingScreen1
import com.appylab.lumi.ui.screens.OnboardingScreen2
import com.appylab.lumi.ui.screens.OnboardingScreen3
import com.appylab.lumi.ui.screens.PlaceholderScreen
import com.appylab.lumi.ui.screens.ScanScreen
import com.appylab.lumi.ui.screens.SplashScreen
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.viewmodel.MainViewModel
import com.appylab.lumi.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay

private enum class AppScreen {
    Splash, Onboarding1, Onboarding2, Onboarding3,
    Main, Scan, Results, Profile, Notifications, Paywall, Placeholder
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumiTheme {
                val mainViewModel: MainViewModel = viewModel()
                val onboardingViewModel: OnboardingViewModel = viewModel()
                val isOnboardingComplete by mainViewModel.isOnboardingComplete.collectAsState()

                var screen by remember { mutableStateOf(AppScreen.Splash) }
                var placeholderTitle by remember { mutableStateOf("") }
                var placeholderSubtitle by remember { mutableStateOf("This screen is coming soon") }

                LaunchedEffect(isOnboardingComplete) {
                    when (isOnboardingComplete) {
                        null -> Unit
                        true -> screen = AppScreen.Main
                        false -> {
                            screen = AppScreen.Splash
                            delay(3_000L)
                            screen = AppScreen.Onboarding1
                        }
                    }
                }

                fun goPlaceholder(title: String, subtitle: String = "This screen is coming soon") {
                    placeholderTitle = title
                    placeholderSubtitle = subtitle
                    screen = AppScreen.Placeholder
                }

                when {
                    isOnboardingComplete == null -> SplashScreen()
                    screen == AppScreen.Splash -> SplashScreen()

                    screen == AppScreen.Onboarding1 -> OnboardingScreen1(
                        onGetStarted = { screen = AppScreen.Onboarding2 },
                        onSkip = {
                            onboardingViewModel.skipOnboarding()
                            screen = AppScreen.Main
                        }
                    )

                    screen == AppScreen.Onboarding2 -> OnboardingScreen2(
                        viewModel = onboardingViewModel,
                        onBack = { screen = AppScreen.Onboarding1 },
                        onContinue = { screen = AppScreen.Onboarding3 }
                    )

                    screen == AppScreen.Onboarding3 -> OnboardingScreen3(
                        viewModel = onboardingViewModel,
                        onBack = { screen = AppScreen.Onboarding2 },
                        onContinue = { screen = AppScreen.Main }
                    )

                    screen == AppScreen.Main -> HomeScreen(
                        onStartScanClick    = { screen = AppScreen.Scan },
                        onViewResultsClick  = { screen = AppScreen.Results },
                        onFeatureTileClick  = { key ->
                            when (key) {
                                "color"  -> goPlaceholder("Color Analysis", "Discover your perfect colour season and palette.")
                                "glowup" -> goPlaceholder("Glow-Up", "Personalised skincare recommendations are on their way.")
                                "makeup" -> goPlaceholder("Makeup", "Looks crafted to enhance your unique features.")
                                else     -> goPlaceholder(key.replaceFirstChar { it.uppercase() })
                            }
                        },
                        onAvatarClick       = { screen = AppScreen.Profile },
                        onBellClick         = { screen = AppScreen.Notifications },
                        onUpgradeBannerClick = { screen = AppScreen.Paywall },
                        onExploreLooksClick = { goPlaceholder("Explore Looks", "A curated looks feed is coming soon.") },
                        onResultsTabClick   = { screen = AppScreen.Results },
                        onProfileTabClick   = { screen = AppScreen.Profile }
                    )

                    screen == AppScreen.Scan -> ScanScreen(
                        onBack        = { screen = AppScreen.Main },
                        onScanComplete = { screen = AppScreen.Results },
                        onGalleryClick = { goPlaceholder("Gallery Scan", "Scanning from gallery is coming soon.") }
                    )

                    screen == AppScreen.Results -> PlaceholderScreen(
                        title    = "Results",
                        subtitle = "Your scan history and analysis results will appear here.",
                        onBack   = { screen = AppScreen.Main }
                    )

                    screen == AppScreen.Profile -> PlaceholderScreen(
                        title    = "Profile",
                        subtitle = "Manage your account, preferences, and saved tips.",
                        onBack   = { screen = AppScreen.Main }
                    )

                    screen == AppScreen.Notifications -> PlaceholderScreen(
                        title    = "Notifications",
                        subtitle = "Your personalised notifications will appear here.",
                        onBack   = { screen = AppScreen.Main }
                    )

                    screen == AppScreen.Paywall -> PlaceholderScreen(
                        title    = "Upgrade to Pro",
                        subtitle = "Unlock unlimited scans, advanced insights, and personalised recommendations.",
                        onBack   = { screen = AppScreen.Main }
                    )

                    screen == AppScreen.Placeholder -> PlaceholderScreen(
                        title    = placeholderTitle,
                        subtitle = placeholderSubtitle,
                        onBack   = { screen = AppScreen.Main }
                    )
                }
            }
        }
    }
}
