package com.appylab.lumi.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.ui.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Palette ──────────────────────────────────────────────────────────────────
private val PRose       = Color(0xFFFF637E)
private val PBackground = Color(0xFFFCFCFC)
private val PCard       = Color.White
private val PBorder     = Color(0xFFFFCCD3)
private val PText       = Color(0xFF0A0A0A)
private val PMuted      = Color(0xFF525252)
private val PMutedBg    = Color(0xFFF5F5F5)
private val PDark       = Color(0xFF0A0A0A)
private val PGreen      = Color(0xFF16A34A)
private val PGreenBg    = Color(0xFFDCFCE7)
private val PRed        = Color(0xFFDC2626)

// ── Static placeholder data ───────────────────────────────────────────────────

private data class PlaceholderRoutine(val icon: ImageVector, val title: String, val subtitle: String)
private data class PlaceholderPalette(val swatches: List<Color>, val title: String, val subtitle: String)

private val PLACEHOLDER_ROUTINES = listOf(
    PlaceholderRoutine(Icons.Outlined.WbSunny, "Everyday Natural", "Updated May 12, 2025"),
    PlaceholderRoutine(Icons.Outlined.Bedtime, "Night Glow", "Updated Jan 28, 2025"),
)

private val PLACEHOLDER_PALETTES = listOf(
    PlaceholderPalette(
        swatches = listOf(Color(0xFF9FBFDF), Color(0xFF7BA3C9), Color(0xFF5B88B3), Color(0xFF3B6D9D)),
        title = "Cool Summer",
        subtitle = "Updated May 08, 2025"
    ),
    PlaceholderPalette(
        swatches = listOf(Color(0xFFE8C4A0), Color(0xFFD4A87A), Color(0xFFC08C54), Color(0xFFAC702E)),
        title = "Warm Autumn",
        subtitle = "Updated Apr 24, 2025"
    )
)

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onManageBilling: () -> Unit = {},
    onViewAllScans: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PText
                )
            }

            // Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Header ────────────────────────────────────────────────
                item {
                    ProfileHeaderCard(
                        displayName = uiState.displayName,
                        email = uiState.email,
                        photoUrl = uiState.photoUrl,
                        onEditClick = { viewModel.showComingSoon() }
                    )
                }

                // ── Subscription ──────────────────────────────────────────
                item {
                    SubscriptionCard(
                        tier = uiState.subscriptionTier,
                        onManageBilling = onManageBilling,
                        onUpgrade = onManageBilling
                    )
                }

                // ── Scan History ──────────────────────────────────────────
                item {
                    ProfileSection(title = "Scan History", actionLabel = "View all", onAction = onViewAllScans) {
                        if (uiState.recentScans.isEmpty()) {
                            Text(
                                text = "No scans yet — tap Scan to get started.",
                                color = PMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                uiState.recentScans.forEach { scan ->
                                    ScanHistoryCard(scan = scan)
                                }
                            }
                        }
                    }
                }

                // ── Saved Routines ────────────────────────────────────────
                item {
                    ProfileSection(title = "Saved Routines", actionLabel = "View all", onAction = { viewModel.showComingSoon() }) {
                        PLACEHOLDER_ROUTINES.forEach { routine ->
                            RoutineRow(
                                icon = routine.icon,
                                title = routine.title,
                                subtitle = routine.subtitle,
                                onClick = { viewModel.showComingSoon() }
                            )
                        }
                    }
                }

                // ── Saved Color Palettes ──────────────────────────────────
                item {
                    ProfileSection(title = "Saved Color Palettes", actionLabel = "View all", onAction = { viewModel.showComingSoon() }) {
                        PLACEHOLDER_PALETTES.forEach { palette ->
                            PaletteRow(
                                swatches = palette.swatches,
                                title = palette.title,
                                subtitle = palette.subtitle,
                                onClick = { viewModel.showComingSoon() }
                            )
                        }
                    }
                }

                // ── Personal Details ──────────────────────────────────────
                item {
                    ProfileSection(
                        title = "Personal Details",
                        actionLabel = "Edit",
                        onAction = { viewModel.showComingSoon() }
                    ) {
                        PersonalDetailRow(Icons.Outlined.Person, "Age", uiState.ageRange.ifEmpty { "—" })
                        PersonalDetailRow(Icons.Outlined.Star, "Skin Type", uiState.skinType.ifEmpty { "—" })
                        PersonalDetailRow(Icons.Outlined.Palette, "Skin Tone", uiState.skinTone.ifEmpty { "—" })
                        PersonalDetailRow(Icons.Outlined.WbSunny, "Undertone", uiState.undertone.ifEmpty { "—" })
                        PersonalDetailRow(Icons.Outlined.LocationOn, "Location", uiState.location.ifEmpty { "—" }, isLast = true)
                    }
                }

                // ── Notification Preferences ──────────────────────────────
                item {
                    ProfileSection(title = "Notification Preferences") {
                        NotifToggleRow(
                            title = "Scan reminders, daily tips",
                            checked = uiState.notifScanReminders,
                            onCheckedChange = viewModel::setNotifScanReminders
                        )
                        NotifToggleRow(
                            title = "Promotions",
                            checked = uiState.notifPromotions,
                            onCheckedChange = viewModel::setNotifPromotions
                        )
                        NotifToggleRow(
                            title = "Updates",
                            checked = uiState.notifUpdates,
                            onCheckedChange = viewModel::setNotifUpdates,
                            isLast = true
                        )
                    }
                }

                // ── App ───────────────────────────────────────────────────
                item {
                    ProfileSection(title = null) {
                        AppActionRow(
                            icon = Icons.Outlined.Star,
                            title = "Rate App",
                            subtitle = "Tell us how we're doing",
                            onClick = { viewModel.showComingSoon() }
                        )
                        AppActionRow(
                            icon = Icons.Outlined.Share,
                            title = "Share App",
                            subtitle = "Share with your friends",
                            isLast = true,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Check out Lumi — the AI beauty analysis app!")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Lumi"))
                            }
                        )
                    }
                }

                // ── Account ───────────────────────────────────────────────
                item {
                    ProfileSection(title = null) {
                        AppActionRow(
                            icon = Icons.Outlined.DeleteOutline,
                            title = "Delete Account",
                            subtitle = "Permanently delete your account and data",
                            tint = PRed,
                            onClick = { viewModel.requestDelete() }
                        )
                        AppActionRow(
                            icon = Icons.AutoMirrored.Outlined.Logout,
                            title = "Sign Out",
                            isLast = true,
                            tint = PRed,
                            onClick = { viewModel.requestSignOut() }
                        )
                    }
                }

                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }

        // ── Dialogs & sheets ──────────────────────────────────────────────────

        if (uiState.showSignOutDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissSignOut,
                title = { Text("Sign out?", fontWeight = FontWeight.SemiBold) },
                text = { Text("You'll need to sign in again to access your data.", color = PMuted) },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.confirmSignOut(onSignOut) },
                        colors = ButtonDefaults.textButtonColors(contentColor = PRed)
                    ) { Text("Sign Out") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissSignOut) { Text("Cancel") }
                },
                containerColor = PCard
            )
        }

        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissDelete,
                title = { Text("Delete account?", fontWeight = FontWeight.SemiBold) },
                text = { Text("This action is permanent and cannot be undone. All your data will be removed.", color = PMuted) },
                confirmButton = {
                    TextButton(
                        onClick = viewModel::dismissDelete, // Placeholder — real deletion deferred
                        colors = ButtonDefaults.textButtonColors(contentColor = PRed)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissDelete) { Text("Cancel") }
                },
                containerColor = PCard
            )
        }

        if (uiState.showComingSoon) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::dismissComingSoon,
                sheetState = sheetState,
                containerColor = PCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = PRose,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Coming Soon", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PText)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This feature is on its way — stay tuned for updates!",
                        color = PMuted,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = viewModel::dismissComingSoon,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PDark)
                    ) {
                        Text("Got it", color = Color.White)
                    }
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

// ── Profile Header Card ───────────────────────────────────────────────────────

@Composable
private fun ProfileHeaderCard(
    displayName: String,
    email: String,
    photoUrl: String,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PCard)
            .border(1.dp, PBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onEditClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PMutedBg),
            contentAlignment = Alignment.Center
        ) {
            val initials = displayName
                .split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2)
                .joinToString("")
            if (initials.isNotEmpty()) {
                Text(initials, color = PMuted, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            } else {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = PMuted, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName.ifEmpty { "Your Name" },
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (email.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(text = email, fontSize = 13.sp, color = PMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = "Edit profile",
            tint = PMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}

// ── Subscription Card ─────────────────────────────────────────────────────────

@Composable
private fun SubscriptionCard(
    tier: SubscriptionTier,
    onManageBilling: () -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PCard)
            .border(1.dp, PBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.WorkspacePremium,
                contentDescription = null,
                tint = PRose,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (tier == SubscriptionTier.PRO) "Premium Plan" else "Free Plan",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = PText,
                modifier = Modifier.weight(1f)
            )
            // Status badge
            if (tier == SubscriptionTier.PRO) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PGreenBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Active", color = PGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PMutedBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Free", color = PMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (tier == SubscriptionTier.PRO) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Renews on June 18, 2025", color = PMuted, fontSize = 12.sp)
                TextButton(
                    onClick = onManageBilling,
                    colors = ButtonDefaults.textButtonColors(contentColor = PRose)
                ) {
                    Text("Manage Billing", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
        } else {
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PDark)
            ) {
                Text("Upgrade to Pro", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Section wrapper ───────────────────────────────────────────────────────────

@Composable
private fun ProfileSection(
    title: String?,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PCard)
            .border(1.dp, PBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        if (title != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = PText)
                if (actionLabel != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        colors = ButtonDefaults.textButtonColors(contentColor = PRose)
                    ) {
                        Text(actionLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        content()
    }
}

// ── Scan History Card ─────────────────────────────────────────────────────────

@Composable
private fun ScanHistoryCard(scan: FaceAnalysis) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(scan.timestamp))
    Column(
        modifier = Modifier.width(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PMutedBg)
                .border(1.dp, PBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.CameraAlt,
                contentDescription = null,
                tint = Color(0xFFCCCCCC),
                modifier = Modifier.size(28.dp)
            )
            // Score badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PRose)
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text("${scan.glowUpScore}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(dateStr, fontSize = 10.sp, color = PMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("Face Scan", fontSize = 10.sp, color = Color(0xFFAAAAAA), maxLines = 1)
    }
}

// ── Routine Row ───────────────────────────────────────────────────────────────

@Composable
private fun RoutineRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PMutedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PMuted, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = PText)
                Text(subtitle, fontSize = 12.sp, color = PMuted)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = PMuted, modifier = Modifier.size(13.dp))
        }
        if (!isLast) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PBorder))
        }
    }
}

// ── Palette Row ───────────────────────────────────────────────────────────────

@Composable
private fun PaletteRow(
    swatches: List<Color>,
    title: String,
    subtitle: String,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Swatch strip
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                swatches.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = PText)
                Text(subtitle, fontSize = 12.sp, color = PMuted)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = PMuted, modifier = Modifier.size(13.dp))
        }
        if (!isLast) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PBorder))
        }
    }
}

// ── Personal Detail Row ───────────────────────────────────────────────────────

@Composable
private fun PersonalDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 14.sp, color = PText, modifier = Modifier.weight(1f))
            Text(value, fontSize = 14.sp, color = PMuted)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(12.dp))
        }
        if (!isLast) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PBorder))
        }
    }
}

// ── Notification Toggle Row ───────────────────────────────────────────────────

@Composable
private fun NotifToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = PMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, color = PText, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PRose,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDDDDDD)
                )
            )
        }
        if (!isLast) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PBorder))
        }
    }
}

// ── App Action Row ────────────────────────────────────────────────────────────

@Composable
private fun AppActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isLast: Boolean = false,
    tint: Color = PText,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = tint, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = PMuted)
                }
            }
            if (tint == PText) {
                Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = PMuted, modifier = Modifier.size(13.dp))
            }
        }
        if (!isLast) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PBorder))
        }
    }
}
