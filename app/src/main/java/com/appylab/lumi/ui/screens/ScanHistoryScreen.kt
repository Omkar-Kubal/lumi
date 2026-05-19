package com.appylab.lumi.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.data.db.FaceAnalysisEntity
import com.appylab.lumi.ui.viewmodel.ScanFilter
import com.appylab.lumi.ui.viewmodel.ScanHistoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SHRose       = Color(0xFFFF637E)
private val SHRoseBg     = Color(0xFFFFF1F2)
private val SHBackground = Color(0xFFFCFCFC)
private val SHCard       = Color.White
private val SHBorder     = Color(0xFFFFCCD3)
private val SHText       = Color(0xFF0A0A0A)
private val SHMuted      = Color(0xFF525252)
private val SHMutedBg    = Color(0xFFF5F5F5)
private val SHRed        = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScanHistoryScreen(
    onBack: () -> Unit = {},
    onOpenResult: (Long) -> Unit = {},
    viewModel: ScanHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SHBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SHCard)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = SHText)
                }
                Text(
                    "Scan History",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = SHText,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // ── Content ───────────────────────────────────────────────────
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SHRose)
                }
            } else if (uiState.allScans.isEmpty()) {
                EmptyScanHistoryState(onBack = onBack)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── Progress chart ────────────────────────────────────
                    if (uiState.progressData.size >= 2) {
                        item {
                            ProgressChartCard(
                                data = uiState.progressData,
                                projectedScore = uiState.projectedScore,
                                projectedDate = uiState.projectedDate
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // ── Filter chips ──────────────────────────────────────
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ScanFilter.entries) { filter ->
                                FilterChip(
                                    selected = uiState.activeFilter == filter,
                                    onClick = { viewModel.setFilter(filter) },
                                    label = { Text(filter.label, fontSize = 13.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SHRose,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = uiState.activeFilter == filter,
                                        borderColor = SHBorder,
                                        selectedBorderColor = SHRose
                                    )
                                )
                            }
                        }
                    }

                    // ── Empty-filter state ────────────────────────────────
                    if (uiState.filteredScans.isEmpty() && uiState.activeFilter != ScanFilter.ALL) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No ${uiState.activeFilter.label} scans yet.",
                                    color = SHMuted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // ── Scan rows ─────────────────────────────────────────
                    items(
                        items = uiState.filteredScans,
                        key = { it.id }
                    ) { scan ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.requestDelete(scan.id)
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
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) SHRed
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
                            ScanRowCard(scan = scan, onClick = { onOpenResult(scan.id) })
                        }
                    }

                    item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
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
        val dateFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val targetScan = uiState.allScans.firstOrNull { it.id == pendingId }
        val dateLabel = targetScan?.let { dateFmt.format(Date(it.timestamp)) } ?: "this scan"

        ModalBottomSheet(
            onDismissRequest = viewModel::dismissDelete,
            sheetState = sheetState,
            containerColor = SHCard
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
                        .background(SHRoseBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = SHRose, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Delete this scan?", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SHText)
                Spacer(Modifier.height(8.dp))
                Text(
                    "This will permanently remove your scan from $dateLabel and all related results. This cannot be undone.",
                    color = SHMuted,
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
                                    message = "Scan deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoDelete()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SHRed)
                    ) { Text("Delete", color = Color.White) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Progress Chart Card ───────────────────────────────────────────────────────

@Composable
private fun ProgressChartCard(
    data: List<FaceAnalysisEntity>,
    projectedScore: Int? = null,
    projectedDate: Long? = null
) {
    val scores = data.map { it.glowUpScore }
    val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
    val totalScans = data.size
    val avgImprovement = if (data.size >= 2) {
        (data.last().glowUpScore - data.first().glowUpScore).toFloat() / (data.size - 1)
    } else 0f

    // When a projected point exists we allocate one extra x-slot so the last real
    // point sits at (N-1)/N and the projected point at N/N = right edge.
    val totalSlots = if (projectedScore != null) scores.size else (scores.size - 1).coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SHCard)
            .border(1.dp, SHBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = SHRose, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Glow Score Over Time", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SHText)
        }
        Spacer(Modifier.height(4.dp))
        Text("Your glow-up journey over time.", fontSize = 12.sp, color = SHMuted)
        Spacer(Modifier.height(16.dp))

        // Canvas chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val chartW = size.width
            val chartH = size.height - 24.dp.toPx()

            val gridColor = Color(0xFFEEEEEE)
            listOf(25f, 50f, 75f, 100f).forEach { gridVal ->
                val y = chartH - (gridVal / 100f) * chartH
                drawLine(gridColor, Offset(0f, y), Offset(chartW, y), strokeWidth = 1f)
            }

            if (scores.size >= 2) {
                val pts = scores.mapIndexed { i, score ->
                    val x = i.toFloat() / totalSlots * chartW
                    val y = chartH - (score.coerceIn(0, 100) / 100f) * chartH
                    Offset(x, y)
                }

                val path = Path().apply {
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        val cx = (pts[i - 1].x + pts[i].x) / 2f
                        cubicTo(cx, pts[i - 1].y, cx, pts[i].y, pts[i].x, pts[i].y)
                    }
                }
                drawPath(path, SHRose, style = Stroke(width = 2.5f))

                pts.forEach { pt ->
                    drawCircle(SHRose, radius = 6f, center = pt)
                    drawCircle(Color.White, radius = 3f, center = pt)
                }

                // Projected point: dashed line + open circle
                if (projectedScore != null) {
                    val lastPt = pts.last()
                    val projX = chartW
                    val projY = chartH - (projectedScore.coerceIn(0, 100) / 100f) * chartH
                    val projPt = Offset(projX, projY)

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
                    drawLine(
                        color = SHRose.copy(alpha = 0.55f),
                        start = lastPt,
                        end = projPt,
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )
                    // Open circle (outline only)
                    drawCircle(SHRose.copy(alpha = 0.55f), radius = 6f, center = projPt, style = Stroke(width = 2f))
                }
            }
        }

        // X-axis labels
        if (data.size >= 2) {
            val labelIndices = if (data.size <= 6) data.indices.toList()
            else listOf(0, data.size / 4, data.size / 2, 3 * data.size / 4, data.size - 1).distinct()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labelIndices.forEach { i ->
                    Text(
                        dateFmt.format(Date(data[i].timestamp)),
                        fontSize = 10.sp,
                        color = SHMuted
                    )
                }
                if (projectedScore != null && projectedDate != null) {
                    Text(
                        dateFmt.format(Date(projectedDate)),
                        fontSize = 10.sp,
                        color = SHMuted.copy(alpha = 0.55f)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Scans: $totalScans", fontSize = 12.sp, color = SHMuted)
            val avgLabel = if (avgImprovement == 0f) "—" else "+%.1f/scan".format(avgImprovement)
            Text("Avg improvement: $avgLabel", fontSize = 12.sp, color = SHMuted)
        }
        if (projectedScore != null) {
            Spacer(Modifier.height(4.dp))
            Text("Projected next: $projectedScore", fontSize = 12.sp, color = SHMuted.copy(alpha = 0.7f))
        }
    }
}

// ── Scan Row Card ─────────────────────────────────────────────────────────────

@Composable
private fun ScanRowCard(scan: FaceAnalysisEntity, onClick: () -> Unit) {
    val dateFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = dateFmt.format(Date(scan.timestamp))
    val score = scan.glowUpScore

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SHCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SHMutedBg)
                    .border(1.dp, SHBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (scan.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = scan.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(24.dp))
                }
                if (score > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SHRose)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text("$score", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Face + Skin", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SHText)
                    Text(dateStr, fontSize = 12.sp, color = SHMuted)
                }
                if (score > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text("Glow Score: $score/100", fontSize = 13.sp, color = SHMuted)
                    Spacer(Modifier.height(6.dp))
                    // Mini progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SHBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(score / 100f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SHRose)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    val potential = when {
                        score >= 80 -> "High potential"
                        score >= 60 -> "Medium potential"
                        else -> "Growing potential"
                    }
                    Text(potential, fontSize = 11.sp, color = SHMuted)
                }
            }

            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = SHMuted, modifier = Modifier.size(13.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyScanHistoryState(onBack: () -> Unit) {
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
                .background(SHRoseBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = SHRose, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("No scans yet", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = SHText)
        Spacer(Modifier.height(8.dp))
        Text(
            "Complete your first scan to see your history and track your progress over time.",
            color = SHMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SHRose),
            border = BorderStroke(1.dp, SHRose)
        ) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Start a Scan", fontWeight = FontWeight.Medium)
        }
    }
}
