package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OnbStepRose     = Color(0xFFFF637E)
private val OnbStepDark     = Color(0xFF0A0A0A)
private val OnbStepInactive = Color(0xFFD4D4D4)
private val OnbStepMuted    = Color(0xFF737373)
private val OnbStepBg       = Color(0xFFFCFCFC)

private val ONBOARDING_STEPS = listOf("Intro", "Personalization", "Access")

/**
 * Shared step-progress indicator used on all 3 onboarding screens.
 * [currentStep] is 1-based.
 */
@Composable
internal fun OnboardingStepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ONBOARDING_STEPS.forEachIndexed { index, label ->
            val stepNum    = index + 1
            val isCompleted = stepNum < currentStep
            val isActive    = stepNum == currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> OnbStepDark
                                isActive    -> OnbStepRose
                                else        -> OnbStepBg
                            }
                        )
                        .then(
                            if (!isCompleted && !isActive)
                                Modifier.border(1.5.dp, OnbStepInactive, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    } else {
                        Text(
                            text = stepNum.toString(),
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isActive) Color.White else OnbStepInactive
                            )
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 9.5.sp,
                        color = when {
                            isActive    -> OnbStepRose
                            isCompleted -> OnbStepDark
                            else        -> OnbStepMuted
                        },
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }

            if (index < ONBOARDING_STEPS.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.5.dp)
                        .padding(bottom = 14.dp)
                        .background(
                            if (isCompleted) OnbStepDark else OnbStepInactive,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}
