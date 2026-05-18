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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Texture
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.viewmodel.OnboardingViewModel

private val Ob2Background  = Color(0xFFFCFCFC)
private val Ob2Rose        = Color(0xFFFF637E)
private val Ob2RoseCard    = Color(0xFFFFF1F2)
private val Ob2TextPrimary = Color(0xFF0A0A0A)
private val Ob2TextMuted   = Color(0xFF737373)
private val Ob2Border      = Color(0xFFE0E0E0)

private data class GoalOption(val id: String, val icon: ImageVector)
private data class ConcernOption(val id: String, val icon: ImageVector)

private val beautyGoalOptions = listOf(
    GoalOption("Makeup", Icons.Outlined.Brush),
    GoalOption("Skincare", Icons.Outlined.Spa),
    GoalOption("Style", Icons.Outlined.Checkroom),
    GoalOption("Glow-Up", Icons.Outlined.AutoAwesome),
    GoalOption("All", Icons.Outlined.Apps)
)

private val skinConcernOptions = listOf(
    ConcernOption("Acne", Icons.Outlined.Face),
    ConcernOption("Dryness", Icons.Outlined.WaterDrop),
    ConcernOption("Oiliness", Icons.Outlined.Opacity),
    ConcernOption("Dark Spots", Icons.Outlined.Contrast),
    ConcernOption("Texture", Icons.Outlined.Texture),
    ConcernOption("Sensitivity", Icons.Outlined.Air)
)

private val ageRangeOptions = listOf("Under 18", "18–24", "25–34", "35–44", "45+")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen2(
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
    val persistedAge by (
        viewModel?.ageRange?.collectAsState()
            ?: remember { mutableStateOf("") }
        )

    var selectedGoals by remember(persistedGoals) {
        mutableStateOf(persistedGoals.ifEmpty { setOf("Glow-Up") })
    }
    var selectedConcerns by remember(persistedConcerns) {
        mutableStateOf(persistedConcerns.ifEmpty { setOf("Dryness") })
    }
    var selectedAge by remember(persistedAge) { mutableStateOf(persistedAge) }
    var ageDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob2Background)
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp)
        ) {
            // ── Top bar ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Ob2TextPrimary
                    )
                }
                Text(
                    text = "Step 2 of 3",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob2TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Step progress indicator ───────────────────────────────────
            OnboardingStepIndicator(
                currentStep = 2,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                // ── Beauty Goals ──────────────────────────────────────────
                Text(
                    text = "What are your beauty goals?",
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob2TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Select all that apply",
                    style = TextStyle(fontSize = 12.sp, color = Ob2TextMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))

                SelectionChipGrid(
                    options = beautyGoalOptions.map { it.id to it.icon },
                    selected = selectedGoals,
                    onToggle = { id ->
                        selectedGoals = if (id in selectedGoals)
                            selectedGoals - id else selectedGoals + id
                    },
                    columns = 3
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Skin Concerns ─────────────────────────────────────────
                Text(
                    text = "Select your skin concerns",
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob2TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Select all that apply",
                    style = TextStyle(fontSize = 12.sp, color = Ob2TextMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))

                SelectionChipGrid(
                    options = skinConcernOptions.map { it.id to it.icon },
                    selected = selectedConcerns,
                    onToggle = { id ->
                        selectedConcerns = if (id in selectedConcerns)
                            selectedConcerns - id else selectedConcerns + id
                    },
                    columns = 3
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Age Range ─────────────────────────────────────────────
                Text(
                    text = "What's your age range?",
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob2TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "This helps us personalize your results",
                    style = TextStyle(fontSize = 12.sp, color = Ob2TextMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = ageDropdownExpanded,
                    onExpandedChange = { ageDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedAge.ifEmpty { "Select your age range" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Ob2TextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Ob2TextMuted
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = if (selectedAge.isEmpty()) Ob2TextMuted else Ob2TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ob2Rose,
                            unfocusedBorderColor = Ob2Border,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = ageDropdownExpanded,
                        onDismissRequest = { ageDropdownExpanded = false },
                        containerColor = Color.White
                    ) {
                        ageRangeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = Ob2TextPrimary
                                        )
                                    )
                                },
                                onClick = {
                                    selectedAge = option
                                    ageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // ── Fixed bottom Continue button ──────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Ob2Background)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    viewModel?.savePersonalization(
                        goals = selectedGoals,
                        concerns = selectedConcerns,
                        age = selectedAge
                    )
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob2TextPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

// ── Reusable multi-select chip grid ──────────────────────────────────────────

@Composable
private fun SelectionChipGrid(
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
                    SelectionChip(
                        icon = icon,
                        label = id,
                        selected = id in selected,
                        onClick = { onToggle(id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty cells in last row
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SelectionChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) Ob2Rose else Ob2Border
    val bgColor = if (selected) Ob2RoseCard else Color.White

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) Ob2Rose else Ob2TextMuted
            )
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = if (selected) Ob2Rose else Ob2TextPrimary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            // Selection indicator circle
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (selected) Ob2Rose else Color.Transparent)
                    .then(
                        if (!selected) Modifier.border(1.dp, Ob2Border, CircleShape)
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

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun OnboardingScreen2Preview() {
    LumiTheme {
        OnboardingScreen2()
    }
}
