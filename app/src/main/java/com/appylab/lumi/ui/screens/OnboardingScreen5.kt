package com.appylab.lumi.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Texture
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.viewmodel.OnboardingViewModel
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob5Background  = Color(0xFFFCFCFC)
private val Ob5Rose        = Color(0xFFFF637E)
private val Ob5RoseCard    = Color(0xFFFFF1F2)
private val Ob5TextPrimary = Color(0xFF0A0A0A)
private val Ob5TextMuted   = Color(0xFF737373)
private val Ob5Border      = Color(0xFFE0E0E0)

private data class GoalOption(val id: String, val icon: ImageVector)
private data class ConcernOption(val id: String, val icon: ImageVector)

private val beautyGoalOptions = listOf(
    GoalOption("Makeup",           Icons.Outlined.Brush),
    GoalOption("Skincare",         Icons.Outlined.Spa),
    GoalOption("Style",            Icons.Outlined.Checkroom),
    GoalOption("Glow-Up",          Icons.Outlined.AutoAwesome),
    GoalOption("Color Analysis",   Icons.Outlined.Palette),
    GoalOption("All of the above", Icons.Outlined.Apps)
)

private val skinConcernOptions = listOf(
    ConcernOption("Acne",        Icons.Outlined.WaterDrop),
    ConcernOption("Dryness",     Icons.Outlined.Opacity),
    ConcernOption("Oiliness",    Icons.Outlined.Spa),
    ConcernOption("Dark Spots",  Icons.Outlined.Contrast),
    ConcernOption("Texture",     Icons.Outlined.Texture),
    ConcernOption("Sensitivity", Icons.Outlined.Air)
)

@Composable
fun OnboardingScreen5(
    viewModel: OnboardingViewModel? = null,
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val persistedGoals by (
        viewModel?.beautyGoals?.collectAsState()
            ?: remember { mutableStateOf(emptySet<String>()) }
        )
    val persistedConcerns by (
        viewModel?.skinConcerns?.collectAsState()
            ?: remember { mutableStateOf(emptySet<String>()) }
        )

    var selectedGoals by remember(persistedGoals) {
        mutableStateOf(persistedGoals.ifEmpty { setOf("Glow-Up") })
    }
    var selectedConcerns by remember(persistedConcerns) {
        mutableStateOf(persistedConcerns.ifEmpty { setOf("Dryness") })
    }
    var showValidationError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob5Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Top bar — back arrow only
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Ob5TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            OnboardingGoalProgress(
                currentStep = 4,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Let's personalise LUMI for you. 30 seconds.",
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 12.sp,
                    color = Ob5TextMuted,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Goals section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "What are your beauty goals?",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob5TextPrimary
                    )
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Select all that apply",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, color = Ob5TextMuted)
                )
                Spacer(Modifier.height(12.dp))
                Ob5SelectionChipGrid(
                    options = beautyGoalOptions.map { it.id to it.icon },
                    selected = selectedGoals,
                    onToggle = { id ->
                        selectedGoals = if (id in selectedGoals) selectedGoals - id else selectedGoals + id
                        if (selectedGoals.isNotEmpty()) showValidationError = false
                    },
                    columns = 2
                )
            }

            Spacer(Modifier.height(24.dp))

            // Concerns section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "What are your skin concerns?",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob5TextPrimary
                    )
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Select all that apply — no judgement here.",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 12.sp,
                        color = Ob5TextMuted,
                        fontStyle = FontStyle.Italic
                    )
                )
                Spacer(Modifier.height(12.dp))
                Ob5SelectionChipGrid(
                    options = skinConcernOptions.map { it.id to it.icon },
                    selected = selectedConcerns,
                    onToggle = { id ->
                        selectedConcerns = if (id in selectedConcerns) selectedConcerns - id else selectedConcerns + id
                    },
                    columns = 2
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Every skin type is valid. Every concern is common.",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 11.sp,
                        color = Ob5TextMuted,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        // Fixed bottom overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Ob5Background)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showValidationError) {
                Text(
                    text = "At least one goal required",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 11.sp,
                        color = Ob5Rose,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    if (selectedGoals.isEmpty()) {
                        showValidationError = true
                        return@Button
                    }
                    viewModel?.savePersonalization(
                        goals    = selectedGoals,
                        concerns = selectedConcerns
                    )
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob5TextPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue →",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun Ob5SelectionChipGrid(
    options: List<Pair<String, ImageVector>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    columns: Int
) {
    val rows = options.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (id, icon) ->
                    Ob5SelectionChip(
                        icon = icon,
                        label = id,
                        selected = id in selected,
                        onClick = { onToggle(id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Ob5SelectionChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) Ob5Rose else Ob5Border
    val bgColor     = if (selected) Ob5RoseCard else Color.White

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) Ob5Rose else Ob5TextMuted
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 12.sp,
                    color = if (selected) Ob5Rose else Ob5TextPrimary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (selected) Ob5Rose else Color.Transparent)
                    .then(
                        if (!selected) Modifier.border(1.dp, Ob5Border, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(8.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
