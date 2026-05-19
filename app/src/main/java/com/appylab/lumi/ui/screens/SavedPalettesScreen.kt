package com.appylab.lumi.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.ui.viewmodel.PaletteSummary
import com.appylab.lumi.ui.viewmodel.SavedPalettesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SPRose       = Color(0xFFFF637E)
private val SPRoseBg     = Color(0xFFFFF1F2)
private val SPBackground = Color(0xFFFCFCFC)
private val SPCard       = Color.White
private val SPBorder     = Color(0xFFFFCCD3)
private val SPText       = Color(0xFF0A0A0A)
private val SPMuted      = Color(0xFF525252)
private val SPRed        = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SavedPalettesScreen(
    onBack: () -> Unit = {},
    onOpenColorAnalysis: (Long) -> Unit = {},
    viewModel: SavedPalettesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SPBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SPCard)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = SPText)
                }
                Text(
                    "Saved Palettes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = SPText,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // ── Content ───────────────────────────────────────────────────
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SPRose)
                }
            } else if (uiState.palettes.isEmpty()) {
                EmptyPalettesState(onStartScan = onBack)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    item {
                        Text(
                            "Your Saved Palettes",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = SPMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    items(
                        items = uiState.palettes,
                        key = { it.faceAnalysisId }
                    ) { palette ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.requestDelete(palette.faceAnalysisId)
                                }
                                false
                            },
                            positionalThreshold = { it * 0.35f }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val bgColor by animateColorAsState(
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) SPRed
                                    else Color.Transparent,
                                    label = "bg"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(bgColor)
                                        .padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(22.dp))
                                        Spacer(Modifier.height(2.dp))
                                        Text("Delete", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        ) {
                            PaletteCard(
                                palette = palette,
                                onClick = { onOpenColorAnalysis(palette.faceAnalysisId) }
                            )
                        }
                    }

                    // Extra height absorbs Snackbar (≈48 dp) so last item is never hidden
                    item { Spacer(Modifier.navigationBarsPadding().height(80.dp)) }
                }
            }
        }

        // ── Snackbar ──────────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        )
    }

    // ── Delete confirmation sheet ─────────────────────────────────────────────
    val pendingId = uiState.pendingDeleteId
    if (pendingId != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val targetPalette = uiState.palettes.firstOrNull { it.faceAnalysisId == pendingId }
        val seasonName = targetPalette?.season ?: "this palette"

        ModalBottomSheet(
            onDismissRequest = viewModel::dismissDelete,
            sheetState = sheetState,
            containerColor = SPCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SPRoseBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = SPRose, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Delete this palette?", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SPText)
                Spacer(Modifier.height(8.dp))
                Text(
                    "This will permanently remove your $seasonName palette. This cannot be undone.",
                    color = SPMuted,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = viewModel::dismissDelete,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            viewModel.confirmDelete(pendingId)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Palette deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoDelete()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SPRed)
                    ) { Text("Delete", color = Color.White) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EmptyPalettesState(onStartScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SPRoseBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Palette, contentDescription = null, tint = SPRose, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("No palettes saved yet", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = SPText)
        Spacer(Modifier.height(8.dp))
        Text(
            "Save your color palettes from your\nColor Analysis scans to view them here.",
            color = SPMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onStartScan,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SPRose),
            border = BorderStroke(1.dp, SPRose)
        ) {
            Icon(Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Start a Color Analysis scan", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PaletteCard(palette: PaletteSummary, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(palette.updatedAt))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SPCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(palette.season, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = SPText)
                if (palette.attributes.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SPRoseBg)
                            .border(1.dp, SPBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(palette.attributes, fontSize = 10.sp, color = SPRose, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text("Updated $dateStr", fontSize = 12.sp, color = SPMuted)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                palette.swatchHexes.forEach { hex ->
                    ColorSwatch(hex = hex)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = SPMuted, modifier = Modifier.size(14.dp))
    }

    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
}

@Composable
private fun ColorSwatch(hex: String) {
    val color = remember(hex) {
        try {
            val clean = hex.removePrefix("#")
            val parsed = when (clean.length) {
                6 -> android.graphics.Color.parseColor("#$clean")
                8 -> android.graphics.Color.parseColor("#$clean")
                else -> android.graphics.Color.parseColor("#CCCCCC")
            }
            Color(parsed)
        } catch (_: Exception) { Color(0xFFCCCCCC) }
    }
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color(0x1A000000), CircleShape)
    )
}
