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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.viewmodel.OnboardingViewModel
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob6Background  = Color(0xFFFCFCFC)
private val Ob6Rose        = Color(0xFFFF637E)
private val Ob6TextPrimary = Color(0xFF0A0A0A)
private val Ob6TextMuted   = Color(0xFF737373)
private val Ob6Border      = Color(0xFFE0E0E0)
private val Ob6MutedBg     = Color(0xFFF5F5F5)

private val ageRanges = listOf("Under 18", "18–24", "25–34", "35–44", "45+")
private val skinTypes = listOf("Oily", "Dry", "Combination", "Normal", "Sensitive")

private data class SkinToneOption(val label: String, val color: Color)

private val skinTones = listOf(
    SkinToneOption("Fair",   Color(0xFFF5D5C0)),
    SkinToneOption("Light",  Color(0xFFE8B89A)),
    SkinToneOption("Medium", Color(0xFFC68B6B)),
    SkinToneOption("Tan",    Color(0xFFA06040)),
    SkinToneOption("Deep",   Color(0xFF5C3120))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen6(
    viewModel: OnboardingViewModel? = null,
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val persistedAge by (
        viewModel?.ageRange?.collectAsState()
            ?: remember { mutableStateOf("") }
        )
    val persistedSkinType by (
        viewModel?.skinType?.collectAsState()
            ?: remember { mutableStateOf("") }
        )
    val persistedSkinTone by (
        viewModel?.skinTone?.collectAsState()
            ?: remember { mutableStateOf("") }
        )

    var selectedAge      by remember(persistedAge)      { mutableStateOf(persistedAge) }
    var ageExpanded      by remember { mutableStateOf(false) }
    var selectedSkinType by remember(persistedSkinType) { mutableStateOf(persistedSkinType.ifEmpty { "Combination" }) }
    var selectedSkinTone by remember(persistedSkinTone) { mutableStateOf(persistedSkinTone.ifEmpty { "Medium" }) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob6Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp)
        ) {
            // Top bar
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
                        tint = Ob6TextPrimary
                    )
                }
                Text(
                    text = "6 of 8",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, color = Ob6TextMuted)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "A few more details to make your results truly personal.",
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 12.sp,
                    color = Ob6TextMuted,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Age range section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "What's your age range?",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob6TextPrimary
                    )
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "This helps personalise your results.",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, color = Ob6TextMuted)
                )
                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = ageExpanded,
                    onExpandedChange = { ageExpanded = it }
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
                                tint = Ob6TextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Ob6TextMuted
                            )
                        },
                        textStyle = TextStyle(fontFamily = PoppinsFont, 
                            fontSize = 14.sp,
                            color = if (selectedAge.isEmpty()) Ob6TextMuted else Ob6TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ob6Rose,
                            unfocusedBorderColor = Ob6Border,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = ageExpanded,
                        onDismissRequest = { ageExpanded = false },
                        containerColor = Color.White
                    ) {
                        ageRanges.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        style = TextStyle(fontFamily = PoppinsFont, 
                                            fontSize = 14.sp,
                                            color = Ob6TextPrimary
                                        )
                                    )
                                },
                                onClick = {
                                    selectedAge = option
                                    ageExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Skin type section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "What's your skin type?",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob6TextPrimary
                    )
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(skinTypes) { type ->
                        val isSelected = type == selectedSkinType
                        Surface(
                            onClick = { selectedSkinType = type },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Ob6Rose else Ob6MutedBg,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = type,
                                    style = TextStyle(fontFamily = PoppinsFont, 
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else Ob6TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Skin tone section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "What's your skin tone?",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob6TextPrimary
                    )
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skinTones.forEach { tone ->
                        val isSelected = tone.label == selectedSkinTone
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                onClick = { selectedSkinTone = tone.label },
                                shape = CircleShape,
                                color = tone.color,
                                modifier = Modifier
                                    .size(48.dp)
                                    .then(
                                        if (isSelected)
                                            Modifier.border(2.5.dp, Ob6Rose, CircleShape)
                                        else
                                            Modifier
                                    )
                            ) {}
                            Spacer(Modifier.height(6.dp))
                            if (isSelected) {
                                Text(
                                    text = tone.label,
                                    style = TextStyle(fontFamily = PoppinsFont, 
                                        fontSize = 11.sp,
                                        color = Ob6TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Every skin tone is stunning.\nLUMI is built for all of them.",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 11.sp,
                        color = Ob6TextMuted,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        // Fixed bottom overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Ob6Background)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    viewModel?.saveSkinDetails(selectedAge, selectedSkinType, selectedSkinTone)
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob6TextPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Almost there →",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
