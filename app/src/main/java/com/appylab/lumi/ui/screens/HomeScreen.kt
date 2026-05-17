package com.appylab.lumi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.appylab.lumi.data.model.BeautyTip
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.data.model.TrendingLook
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.viewmodel.HomeUiState
import com.appylab.lumi.ui.viewmodel.HomeViewModel

private val HBackground  = Color(0xFFFCFCFC)
private val HRose        = Color(0xFFFF637E)
private val HTextPrimary = Color(0xFF0A0A0A)
private val HTextMuted   = Color(0xFF737373)
private val HBorder      = Color(0xFFEDEDED)
private val HSurface     = Color.White
private val HMuted       = Color(0xFFF5F5F5)
private val HAmber       = Color(0xFFF59E0B)

private data class FeatureTile(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val title: String,
    val subtitle: String,
    val key: String
)

private val featureTiles = listOf(
    FeatureTile(Icons.Outlined.Palette,     HRose,             Color(0xFFFFF1F2), "Color",   "Find your perfect season & tones",            "color"),
    FeatureTile(Icons.Outlined.AutoAwesome, HAmber,            Color(0xFFFFFBEB), "Glow-Up", "Personalised skincare recommendations",        "glowup"),
    FeatureTile(Icons.Outlined.Brush,       Color(0xFFEC4899), Color(0xFFFDF2F8), "Makeup",  "Looks that enhance your features",            "makeup"),
    FeatureTile(Icons.Outlined.Checkroom,   Color(0xFF6366F1), Color(0xFFF5F3FF), "Style",   "Outfits that fit your vibe & body",           "style"),
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onStartScanClick: () -> Unit = {},
    onViewResultsClick: () -> Unit = {},
    onFeatureTileClick: (String) -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onUpgradeBannerClick: () -> Unit = {},
    onExploreLooksClick: () -> Unit = {},
    onResultsTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        onStartScanClick = onStartScanClick,
        onViewResultsClick = onViewResultsClick,
        onFeatureTileClick = onFeatureTileClick,
        onAvatarClick = onAvatarClick,
        onBellClick = {
            viewModel.onBellTapped()
            onBellClick()
        },
        onUpgradeBannerClick = onUpgradeBannerClick,
        onUpgradeBannerDismiss = viewModel::dismissBanner,
        onBookmarkTip = viewModel::toggleBookmark,
        onTipPageChange = viewModel::navigateTipWindow,
        onExploreLooksClick = onExploreLooksClick,
        onResultsTabClick = {
            viewModel.clearResultsBadge()
            onResultsTabClick()
        },
        onProfileTabClick = onProfileTabClick
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onStartScanClick: () -> Unit,
    onViewResultsClick: () -> Unit,
    onFeatureTileClick: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onBellClick: () -> Unit,
    onUpgradeBannerClick: () -> Unit,
    onUpgradeBannerDismiss: () -> Unit,
    onBookmarkTip: (Int) -> Unit,
    onTipPageChange: (Int) -> Unit,
    onExploreLooksClick: () -> Unit,
    onResultsTabClick: () -> Unit,
    onProfileTabClick: () -> Unit
) {
    var showComingSoonDialog by remember { mutableStateOf(false) }

    if (showComingSoonDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showComingSoonDialog = false },
            title = {
                Text(
                    "Style — Coming Soon",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary)
                )
            },
            text = {
                Text(
                    "Style recommendations are on the way. We're crafting outfits tailored to your vibe and body — stay tuned!",
                    style = TextStyle(fontSize = 13.sp, color = HTextMuted, lineHeight = 20.sp)
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showComingSoonDialog = false }) {
                    Text("Got it", style = TextStyle(color = HRose, fontWeight = FontWeight.SemiBold))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 80.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            GreetingHeader(
                displayName = uiState.displayName,
                photoUrl = uiState.photoUrl,
                greetingTime = uiState.greetingTime,
                greetingSubtitle = uiState.greetingSubtitle,
                unreadCount = uiState.unreadNotificationCount,
                onAvatarClick = onAvatarClick,
                onBellClick = onBellClick
            )
            Spacer(Modifier.height(20.dp))
            ScanCtaButton(onClick = onStartScanClick)
            Spacer(Modifier.height(24.dp))

            if (uiState.lastScan != null) {
                SectionLabel(title = "Last scan summary", actionText = "View results", onActionClick = onViewResultsClick)
                Spacer(Modifier.height(10.dp))
                ScanSummaryCard(scan = uiState.lastScan, onViewResultsClick = onViewResultsClick)
            } else {
                NoScanCard(onStartScanClick = onStartScanClick)
            }

            Spacer(Modifier.height(24.dp))
            FeatureTilesGrid(
                tier = uiState.subscriptionTier,
                hasScan = uiState.lastScan != null,
                onTileClick = { key ->
                    if (key == "style") showComingSoonDialog = true
                    else onFeatureTileClick(key)
                }
            )
            Spacer(Modifier.height(24.dp))

            if (uiState.dailyTip != null) {
                DailyBeautyTipCard(
                    tip = uiState.dailyTip,
                    windowIndex = uiState.currentTipWindowIndex,
                    windowSize = uiState.tipsWindow.size,
                    isBookmarked = uiState.dailyTip.id in uiState.savedTipIds,
                    onBookmark = { onBookmarkTip(uiState.dailyTip.id) },
                    onPageChange = onTipPageChange
                )
                Spacer(Modifier.height(24.dp))
            }

            TrendingSection(
                looks = uiState.trendingLooks,
                onExploreLooksClick = onExploreLooksClick
            )

            if (uiState.showUpsellBanner) {
                Spacer(Modifier.height(16.dp))
                UpgradeBanner(
                    onUpgrade = onUpgradeBannerClick,
                    onDismiss = onUpgradeBannerDismiss
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        HomeBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            resultsUnviewed = uiState.resultsUnviewed,
            onScanClick = onStartScanClick,
            onResultsClick = onResultsTabClick,
            onProfileClick = onProfileTabClick
        )
    }
}

// ── Greeting header ───────────────────────────────────────────────────────────

@Composable
private fun GreetingHeader(
    displayName: String,
    photoUrl: String?,
    greetingTime: String,
    greetingSubtitle: String,
    unreadCount: Int,
    onAvatarClick: () -> Unit,
    onBellClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(HMuted)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else if (displayName.isNotEmpty()) {
                Text(
                    text = displayName.first().uppercaseChar().toString(),
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = HTextMuted
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = greetingTime.ifEmpty { "Hello," },
                    style = TextStyle(fontSize = 12.sp, color = HTextMuted)
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                    tint = HRose
                )
            }
            if (displayName.isNotEmpty()) {
                Text(
                    text = displayName,
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = HTextPrimary, lineHeight = 26.sp)
                )
            }
            Text(
                text = greetingSubtitle.ifEmpty { "Welcome back" },
                style = TextStyle(fontSize = 12.sp, color = HTextMuted)
            )
        }

        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(containerColor = HRose)
                }
            }
        ) {
            IconButton(onClick = onBellClick) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(24.dp),
                    tint = HTextPrimary
                )
            }
        }
    }
}

// ── Scan CTA button ───────────────────────────────────────────────────────────

@Composable
private fun ScanCtaButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HTextPrimary,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.CenterFocusWeak,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Start your scan",
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        )
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String, actionText: String? = null, onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary)
        )
        if (actionText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onActionClick)
            ) {
                Text(
                    text = actionText,
                    style = TextStyle(fontSize = 12.sp, color = HTextMuted)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = HTextMuted
                )
            }
        }
    }
}

// ── No-scan empty state ───────────────────────────────────────────────────────

@Composable
private fun NoScanCard(onStartScanClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HSurface,
        border = BorderStroke(1.dp, HBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(HMuted),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CenterFocusWeak,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = HTextMuted
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No scan yet",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tap Start your scan to begin",
                style = TextStyle(fontSize = 12.sp, color = HTextMuted)
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onStartScanClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HTextPrimary),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = "Start your scan",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                )
            }
        }
    }
}

// ── Scan summary card ─────────────────────────────────────────────────────────

@Composable
private fun ScanSummaryCard(scan: FaceAnalysis, onViewResultsClick: () -> Unit) {
    val verdict = HomeViewModel.glowScoreVerdict(scan.glowUpScore)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HSurface,
        border = BorderStroke(1.dp, HBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HMuted)
                        .clickable(onClick = onViewResultsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFCCCCCC)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Glow Score",
                        style = TextStyle(fontSize = 11.sp, color = HTextMuted)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${scan.glowUpScore} / 100",
                        style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = HTextPrimary, lineHeight = 30.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = verdict,
                        style = TextStyle(fontSize = 11.sp, color = HTextMuted)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = HBorder, thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScanMetric(Icons.Outlined.Face,       "Face Shape", scan.faceShape.ifEmpty { "—" })
                ScanMetric(Icons.Outlined.Circle,     "Skin Tone",  scan.skinTone.ifEmpty { "—" })
                ScanMetric(Icons.Outlined.Palette,    "Undertone",  scan.undertone.ifEmpty { "—" })
                ScanMetric(Icons.Outlined.Visibility, "Eye Shape",  scan.eyeShape.ifEmpty { "—" })
            }
        }
    }
}

@Composable
private fun ScanMetric(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = HTextMuted)
        Text(label, style = TextStyle(fontSize = 9.sp, color = HTextMuted))
        Text(value, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary))
    }
}

// ── Feature tiles 2×2 grid ────────────────────────────────────────────────────

@Composable
private fun FeatureTilesGrid(
    tier: SubscriptionTier,
    hasScan: Boolean,
    onTileClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        featureTiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { tile ->
                    FeatureTileCard(
                        tile = tile,
                        locked = tier == SubscriptionTier.FREE,
                        modifier = Modifier.weight(1f),
                        onClick = { onTileClick(tile.key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureTileCard(
    tile: FeatureTile,
    locked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = HSurface,
        border = BorderStroke(1.dp, HBorder)
    ) {
        Box {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(tile.iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tile.icon,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = tile.iconTint
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = HTextMuted
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = tile.title,
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tile.subtitle,
                    style = TextStyle(fontSize = 10.sp, color = HTextMuted, lineHeight = 14.sp)
                )
            }
        }
    }
}

// ── Daily beauty tip ──────────────────────────────────────────────────────────

@Composable
private fun DailyBeautyTipCard(
    tip: BeautyTip,
    windowIndex: Int,
    windowSize: Int,
    isBookmarked: Boolean,
    onBookmark: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF8F9)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily beauty tip",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary)
                )
                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark tip",
                        modifier = Modifier.size(18.dp),
                        tint = if (isBookmarked) HRose else HTextMuted
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = tip.text,
                style = TextStyle(fontSize = 12.sp, color = HTextMuted, lineHeight = 18.sp)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(windowSize.coerceAtMost(5)) { index ->
                    val isActive = index == windowIndex
                    Box(
                        modifier = Modifier
                            .then(
                                if (isActive) Modifier.width(14.dp).height(5.dp)
                                else Modifier.size(5.dp)
                            )
                            .clip(if (isActive) RoundedCornerShape(3.dp) else CircleShape)
                            .background(if (isActive) HTextPrimary else HBorder)
                            .clickable { onPageChange(index) }
                    )
                }
            }
        }
    }
}

// ── Trending now ──────────────────────────────────────────────────────────────

@Composable
private fun TrendingSection(
    looks: List<TrendingLook>,
    onExploreLooksClick: () -> Unit
) {
    if (looks.isEmpty()) return

    val listState = rememberLazyListState()
    val activeIndex = listState.firstVisibleItemIndex

    Text(
        text = looks.first().tag,
        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = HTextMuted)
    )
    Spacer(Modifier.height(8.dp))

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(looks) { look ->
            TrendingCard(look = look, onExploreLooksClick = onExploreLooksClick)
        }
    }

    if (looks.size > 1) {
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(looks.size.coerceAtMost(5)) { index ->
                val isActive = index == activeIndex
                Box(
                    modifier = Modifier
                        .then(
                            if (isActive) Modifier.width(14.dp).height(5.dp)
                            else Modifier.size(5.dp)
                        )
                        .clip(if (isActive) RoundedCornerShape(3.dp) else CircleShape)
                        .background(if (isActive) HTextPrimary else HBorder)
                )
            }
        }
    }
}

@Composable
private fun TrendingCard(look: TrendingLook, onExploreLooksClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(162.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFE4E8), Color(0xFFFBD0D6))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .size(110.dp)
                .clip(CircleShape)
                .background(Color(0x28FF637E))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 14.dp)
        ) {
            Text(
                text = look.title,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HTextPrimary)
            )
            Text(
                text = look.subtitle,
                style = TextStyle(fontSize = 11.sp, color = HTextMuted)
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onExploreLooksClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HTextPrimary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "Explore looks",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                )
            }
        }
    }
}

// ── Upgrade banner ────────────────────────────────────────────────────────────

@Composable
private fun UpgradeBanner(onUpgrade: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HSurface,
        border = BorderStroke(1.dp, HBorder)
    ) {
        Box {
            Row(
                modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFFBEB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = HAmber
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unlock your full glow",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Get advanced insights, saved scans, and personalised recommendations.",
                        style = TextStyle(fontSize = 10.sp, color = HTextMuted, lineHeight = 14.sp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onUpgrade,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HTextPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Upgrade",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(14.dp),
                    tint = HTextMuted
                )
            }
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun HomeBottomBar(
    modifier: Modifier = Modifier,
    resultsUnviewed: Boolean = false,
    onScanClick: () -> Unit = {},
    onResultsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        color = HSurface,
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
            BottomNavItem(icon = Icons.Filled.Home,        label = "Home",    selected = true,  badge = false,            onClick = {})
            BottomNavItem(icon = Icons.Outlined.CameraAlt, label = "Scan",    selected = false, badge = false,            onClick = onScanClick)
            BottomNavItem(icon = Icons.Outlined.BarChart,  label = "Results", selected = false, badge = resultsUnviewed,  onClick = onResultsClick)
            BottomNavItem(icon = Icons.Outlined.Person,    label = "Profile", selected = false, badge = false,            onClick = onProfileClick)
        }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean, badge: Boolean, onClick: () -> Unit) {
    BadgedBox(
        badge = {
            if (badge) Badge(containerColor = HRose)
        }
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
                tint = if (selected) HRose else HTextMuted
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = if (selected) HRose else HTextMuted,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun HomeScreenPreview() {
    LumiTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                displayName = "Ayesha",
                greetingTime = "Good morning,",
                greetingSubtitle = "Let's enhance your natural glow",
                unreadNotificationCount = 2,
                lastScan = FaceAnalysis(
                    id = 1, userId = 1, glowUpScore = 86,
                    faceShape = "Oval", skinTone = "Medium", undertone = "Warm", eyeShape = "Almond",
                    imageUrl = "", timestamp = System.currentTimeMillis()
                ),
                subscriptionTier = SubscriptionTier.FREE,
                dailyTip = BeautyTip(1, "Always apply sunscreen as the last step of your skincare routine, even indoors!", "skincare"),
                currentTipWindowIndex = 0,
                tipsWindow = List(5) { BeautyTip(it, "Tip text", "skincare") },
                savedTipIds = setOf(1),
                trendingLooks = listOf(TrendingLook(1, "Trending now", "Glass Skin Look", "Dewy, clean & radiant ✦", "")),
                showUpsellBanner = true
            ),
            onStartScanClick = {},
            onViewResultsClick = {},
            onFeatureTileClick = {},
            onAvatarClick = {},
            onBellClick = {},
            onUpgradeBannerClick = {},
            onUpgradeBannerDismiss = {},
            onBookmarkTip = {},
            onTipPageChange = {},
            onExploreLooksClick = {},
            onResultsTabClick = {},
            onProfileTabClick = {}
        )
    }
}
