package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.PoppinsFont

private val ObcRose     = Color(0xFFFF637E)
private val ObcDark     = Color(0xFF0A0A0A)
private val ObcInactive = Color(0xFFD4D4D4)

private val GOAL_STEPS = listOf("Intro", "Proof", "Proof", "Goals", "Access")

@Composable
fun OnboardingPageDots(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        for (i in 1..totalPages) {
            val isActive = i == currentPage
            Box(
                modifier = Modifier
                    .then(
                        if (isActive)
                            Modifier
                                .width(16.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(ObcRose)
                        else
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(ObcInactive)
                    )
            )
            if (i < totalPages) {
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun OnboardingGoalProgress(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GOAL_STEPS.forEachIndexed { index, label ->
            val stepNum     = index + 1
            val isCompleted = stepNum < currentStep
            val isActive    = stepNum == currentStep

            val dotColor = when {
                isCompleted -> ObcDark
                isActive    -> ObcRose
                else        -> ObcInactive
            }
            val labelColor = when {
                isCompleted -> ObcDark
                isActive    -> ObcRose
                else        -> ObcInactive
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 8.sp, color = labelColor)
                )
            }

            if (index < GOAL_STEPS.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 14.dp)
                        .height(1.5.dp)
                        .background(
                            if (isCompleted) ObcDark else ObcInactive,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}
