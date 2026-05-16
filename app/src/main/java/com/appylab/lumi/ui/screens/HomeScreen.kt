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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Texture
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.LumiTheme

private val HBackground  = Color(0xFFFCFCFC)
private val HRose        = Color(0xFFFF637E)
private val HTextPrimary = Color(0xFF0A0A0A)
private val HTextMuted   = Color(0xFF737373)
private val HBorder      = Color(0xFFEDEDED)
private val HSurface     = Color.White
private val HMuted       = Color(0xFFF5F5F5)
private val HGreen       = Color(0xFF22C55E)
private val HAmber       = Color(0xFFF59E0B)

private data class QuickAction(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val title: String,
    val subtitle: String
)

private val quickActions = listOf(
    QuickAction(Icons.Outlined.Palette,     HRose,             Color(0xFFFFF1F2), "Color",   "Find your perfect season & palette"),
    QuickAction(Icons.Outlined.AutoAwesome, HAmber,            Color(0xFFFFFBEB), "Glow-Up", "Personalized skincare recommendations"),
    QuickAction(Icons.Outlined.Brush,       Color(0xFFEC4899), Color(0xFFFDF2F8), "Makeup",  "Looks that enhance your features"),
    QuickAction(Icons.Outlined.Checkroom,   Color(0xFF6366F1), Color(0xFFF5F3FF), "Style",   "Outfits that fit your vibe & body"),
)

@Composable
fun HomeScreen() {
    var showUpgradeBanner by remember { mutableStateOf(true) }

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
            GreetingHeader()
            Spacer(Modifier.height(20.dp))
            ScanCtaButton()
            Spacer(Modifier.height(24.dp))
            SectionLabel(title = "Last scan summary", actionText = "View results")
            Spacer(Modifier.height(10.dp))
            ScanSummaryCard()
            Spacer(Modifier.height(24.dp))
            QuickActionsGrid()
            Spacer(Modifier.height(24.dp))
            DailyBeautyTipCard()
            Spacer(Modifier.height(24.dp))
            TrendingSection()
            if (showUpgradeBanner) {
                Spacer(Modifier.height(16.dp))
                UpgradeBanner(onDismiss = { showUpgradeBanner = false })
            }
            Spacer(Modifier.height(8.dp))
        }

        HomeBottomBar(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ── Greeting header ───────────────────────────────────────────────────────────

@Composable
private fun GreetingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(HMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = HTextMuted
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Good morning,",
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
            Text(
                text = "Ayesha",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = HTextPrimary, lineHeight = 26.sp)
            )
            Text(
                text = "Let's enhance your natural glow",
                style = TextStyle(fontSize = 12.sp, color = HTextMuted)
            )
        }

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp),
                tint = HTextPrimary
            )
        }
    }
}

// ── Scan CTA button ───────────────────────────────────────────────────────────

@Composable
private fun ScanCtaButton() {
    Button(
        onClick = {},
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
private fun SectionLabel(title: String, actionText: String? = null) {
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
                modifier = Modifier.clickable {}
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

// ── Scan summary card ─────────────────────────────────────────────────────────

@Composable
private fun ScanSummaryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HSurface,
        border = BorderStroke(1.dp, HBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Face scan placeholder
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HMuted),
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
                        text = "Skin Health",
                        style = TextStyle(fontSize = 11.sp, color = HTextMuted)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "86 / 100",
                        style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = HTextPrimary, lineHeight = 30.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = HGreen
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Your skin looks healthy",
                            style = TextStyle(fontSize = 11.sp, color = HTextMuted)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = HBorder, thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkinMetric(Icons.Outlined.WaterDrop, "Hydration", "Good")
                SkinMetric(Icons.Outlined.Texture,   "Texture",   "Good")
                SkinMetric(Icons.Outlined.Opacity,   "Pores",     "Good")
                SkinMetric(Icons.Outlined.Contrast,  "Spots",     "Mild")
            }
        }
    }
}

@Composable
private fun SkinMetric(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = HTextMuted)
        Text(label, style = TextStyle(fontSize = 9.sp, color = HTextMuted))
        Text(value, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary))
    }
}

// ── Quick actions 2×2 grid ────────────────────────────────────────────────────

@Composable
private fun QuickActionsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        quickActions.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { action ->
                    QuickActionCard(action, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(action: QuickAction, modifier: Modifier = Modifier) {
    Surface(
        onClick = {},
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = HSurface,
        border = BorderStroke(1.dp, HBorder)
    ) {
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
                        .background(action.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = action.iconTint
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
                text = action.title,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HTextPrimary)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = action.subtitle,
                style = TextStyle(fontSize = 10.sp, color = HTextMuted, lineHeight = 14.sp)
            )
        }
    }
}

// ── Daily beauty tip ──────────────────────────────────────────────────────────

@Composable
private fun DailyBeautyTipCard() {
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
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = HTextMuted
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Always apply sunscreen as the last step of your skincare routine, even indoors!",
                style = TextStyle(fontSize = 12.sp, color = HTextMuted, lineHeight = 18.sp)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(14.dp).height(5.dp).clip(RoundedCornerShape(3.dp)).background(HTextPrimary))
                Box(Modifier.size(5.dp).clip(CircleShape).background(HBorder))
                Box(Modifier.size(5.dp).clip(CircleShape).background(HBorder))
            }
        }
    }
}

// ── Trending now ──────────────────────────────────────────────────────────────

@Composable
private fun TrendingSection() {
    Text(
        text = "Trending now",
        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = HTextMuted)
    )
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(162.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFE4E8), Color(0xFFFBD0D6))
                )
            )
    ) {
        // Decorative circle placeholder (represents face/model image on right)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .size(110.dp)
                .clip(CircleShape)
                .background(Color(0x28FF637E))
        )

        // Text + button overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 14.dp)
        ) {
            Text(
                text = "Glass Skin Look",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HTextPrimary)
            )
            Text(
                text = "Dewy, clean & radiant ✨",
                style = TextStyle(fontSize = 11.sp, color = HTextMuted)
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {},
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

        // Dot indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.width(14.dp).height(5.dp).clip(RoundedCornerShape(3.dp)).background(HTextPrimary))
            Box(Modifier.size(5.dp).clip(CircleShape).background(HBorder))
            Box(Modifier.size(5.dp).clip(CircleShape).background(HBorder))
        }
    }
}

// ── Upgrade banner ────────────────────────────────────────────────────────────

@Composable
private fun UpgradeBanner(onDismiss: () -> Unit) {
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
                // Crown icon box
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
                        text = "Get advanced insights, saved scans, and personalized recommendations.",
                        style = TextStyle(fontSize = 10.sp, color = HTextMuted, lineHeight = 14.sp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {},
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

            // Dismiss button
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
private fun HomeBottomBar(modifier: Modifier = Modifier) {
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
            BottomNavItem(icon = Icons.Filled.Home,           label = "Home",    selected = true)
            BottomNavItem(icon = Icons.Outlined.CameraAlt,    label = "Scan",    selected = false)
            BottomNavItem(icon = Icons.Outlined.BarChart,     label = "Results", selected = false)
            BottomNavItem(icon = Icons.Outlined.Person,       label = "Profile", selected = false)
        }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 6.dp)
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

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun HomeScreenPreview() {
    LumiTheme {
        HomeScreen()
    }
}
