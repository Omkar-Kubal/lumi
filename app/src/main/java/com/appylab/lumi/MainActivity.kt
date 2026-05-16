package com.appylab.lumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
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
import com.appylab.lumi.ui.screens.SplashScreen
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.viewmodel.MainViewModel
import com.appylab.lumi.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay

private enum class AppScreen { Splash, Onboarding1, Onboarding2, Onboarding3, Main }

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

                    screen == AppScreen.Main -> HomeScreen()
                }
            }
        }
    }
}
