package com.appylab.lumi.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
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


// Dropdown option lists
private val SKIN_TYPE_OPTIONS = listOf("Oily", "Dry", "Combination", "Normal", "Sensitive")
private val SKIN_TONE_OPTIONS = listOf("Fair", "Light", "Medium", "Tan", "Deep")
private val UNDERTONE_OPTIONS  = listOf("Warm", "Cool", "Neutral")

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onViewAllScans: () -> Unit = {},
    onViewScanHistory: () -> Unit = {},
    onViewSavedPalettes: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onUpgrade: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val isPro = uiState.subscriptionTier == SubscriptionTier.PRO

    fun checkNotifPermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    var hasNotifPermission by remember { mutableStateOf(checkNotifPermission()) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) hasNotifPermission = checkNotifPermission()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize().background(PBackground)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Profile",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PText,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = { scope.launch { listState.animateScrollToItem(Int.MAX_VALUE) } },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Notification settings", tint = PMuted)
                }
            }

            // ── Content ───────────────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── 0: User identity ──────────────────────────────────────
                item {
                    ProfileHeaderCard(
                        displayName = uiState.displayName,
                        email = uiState.email,
                        onEditClick = onEditProfile
                    )
                }

                // ── 1: Subscription ───────────────────────────────────────
                item {
                    SubscriptionCard(
                        isPro = isPro,
                        onManageBilling = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/account/subscriptions"))
                            context.startActivity(intent)
                        },
                        onUpgrade = onUpgrade
                    )
                }

                // ── 2: Scan history ───────────────────────────────────────
                item {
                    ScanHistorySection(
                        scans = uiState.recentScans,
                        isPro = isPro,
                        onViewAll = onViewScanHistory,
                        onLockedTap = onUpgrade
                    )
                }

                // ── 3: Saved Palettes ─────────────────────────────────────
                item {
                    SavedContentCard(
                        title = "Saved Palettes",
                        lockBody = "Save your seasonal color palettes with Pro.",
                        isPro = isPro,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onViewAll = onViewSavedPalettes,
                        onUpgrade = onUpgrade
                    )
                }

                // ── 4: Personal details ───────────────────────────────────
                item {
                    PersonalDetailsCard(
                        age = uiState.age,
                        skinType = uiState.skinType,
                        skinTone = uiState.skinTone,
                        undertone = uiState.undertone,
                        location = uiState.location,
                        isEditing = uiState.isEditingPersonalDetails,
                        ageDraft = uiState.ageDraft,
                        skinTypeDraft = uiState.skinTypeDraft,
                        skinToneDraft = uiState.skinToneDraft,
                        undertoneDraft = uiState.undertoneDraft,
                        locationDraft = uiState.locationDraft,
                        saveError = uiState.saveError,
                        onStartEdit = viewModel::startEditPersonalDetails,
                        onCancelEdit = viewModel::cancelEditPersonalDetails,
                        onSave = viewModel::savePersonalDetails,
                        onAgeDraftChange = viewModel::updateAgeDraft,
                        onSkinTypeDraftChange = viewModel::updateSkinTypeDraft,
                        onSkinToneDraftChange = viewModel::updateSkinToneDraft,
                        onUndertoneDraftChange = viewModel::updateUndertoneDraft,
                        onLocationDraftChange = viewModel::updateLocationDraft
                    )
                }

                // ── 5: Notification preferences ───────────────────────────
                item {
                    NotificationPreferencesCard(
                        notifScanReminders = uiState.notifScanReminders,
                        notifPromotions = uiState.notifPromotions,
                        notifUpdates = uiState.notifUpdates,
                        hasPermission = hasNotifPermission,
                        onScanRemindersChange = viewModel::setNotifScanReminders,
                        onPromotionsChange = viewModel::setNotifPromotions,
                        onUpdatesChange = viewModel::setNotifUpdates,
                        onPermissionBannerTap = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                // ── 6: App actions ────────────────────────────────────────
                item {
                    ProfileSection(title = null) {
                        AppActionRow(
                            icon = Icons.Outlined.Star,
                            title = "Rate App",
                            subtitle = "Tell us how we're doing",
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=${context.packageName}"))
                                    )
                                }.onFailure {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                    )
                                }
                            }
                        )
                        AppActionRow(
                            icon = Icons.Outlined.Share,
                            title = "Share App",
                            subtitle = "Share with your friends",
                            isLast = true,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT,
                                        "Check out Lumi — the AI beauty analysis app! " +
                                        "https://play.google.com/store/apps/details?id=${context.packageName}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Lumi"))
                            }
                        )
                    }
                }

                // ── 7: Account actions ────────────────────────────────────
                item {
                    Spacer(Modifier.height(4.dp))
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
                            subtitle = "Sign out from your account",
                            isLast = true,
                            tint = PRed,
                            onClick = { viewModel.requestSignOut() }
                        )
                    }
                }

                item { Spacer(Modifier.navigationBarsPadding().padding(bottom = 60.dp)) }
            }
        }

        // ── Sign out dialog ───────────────────────────────────────────────────
        if (uiState.showSignOutDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissSignOut,
                title = { Text("Sign out?", fontWeight = FontWeight.SemiBold) },
                text = { Text("You'll need to sign in again to access your results.", color = PMuted) },
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

        // ── Delete account bottom sheet ───────────────────────────────────────
        if (uiState.showDeleteDialog) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::dismissDelete,
                sheetState = sheetState,
                containerColor = PCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Delete your account?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PText)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "This will permanently delete your account, all scan history, and saved data. This cannot be undone.",
                        color = PMuted, fontSize = 14.sp
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
                            onClick = { viewModel.confirmDelete(onSignOut) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PRed)
                        ) { Text("Delete Account", color = Color.White) }
                    }
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }

    }
}

// ── Profile Header Card ───────────────────────────────────────────────────────

@Composable
private fun ProfileHeaderCard(displayName: String, email: String, onEditClick: () -> Unit) {
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
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(PMutedBg),
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
                Text(email, fontSize = 13.sp, color = PMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = PMuted, modifier = Modifier.size(14.dp))
    }
}

// ── Subscription Card ─────────────────────────────────────────────────────────

@Composable
private fun SubscriptionCard(isPro: Boolean, onManageBilling: () -> Unit, onUpgrade: () -> Unit) {
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
            Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = PRose, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (isPro) "Premium Plan" else "Free Plan",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = PText,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isPro) PGreenBg else PMutedBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isPro) "Active" else "Free",
                    color = if (isPro) PGreen else PMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (isPro) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("You have full access to all features.", color = PMuted, fontSize = 12.sp)
                TextButton(onClick = onManageBilling, colors = ButtonDefaults.textButtonColors(contentColor = PRose)) {
                    Text("Manage Billing", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
        } else {
            Text("Upgrade to unlock full analysis and all features.", color = PMuted, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PDark)) {
                Text("Upgrade Now", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Scan History Section ──────────────────────────────────────────────────────

@Composable
private fun ScanHistorySection(
    scans: List<FaceAnalysis>,
    isPro: Boolean,
    onViewAll: () -> Unit,
    onLockedTap: () -> Unit
) {
    val limit = if (isPro) 5 else 3
    val displayedScans = scans.take(limit)
    val showLockedSlot = !isPro && scans.size > 3

    ProfileSection(title = "Scan History", actionLabel = "View all", onAction = onViewAll) {
        if (scans.isEmpty()) {
            Text(
                "No scans yet — tap Scan to get started.",
                color = PMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                displayedScans.forEach { scan -> ScanHistoryCard(scan = scan) }
                if (showLockedSlot) {
                    LockedScanCard(onTap = onLockedTap)
                }
            }
        }
    }
}

@Composable
private fun ScanHistoryCard(scan: FaceAnalysis) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(scan.timestamp))
    Column(modifier = Modifier.width(90.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PMutedBg)
                .border(1.dp, PBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(28.dp))
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

@Composable
private fun LockedScanCard(onTap: () -> Unit) {
    Column(
        modifier = Modifier.width(90.dp).clickable(onClick = onTap),
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = PMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.height(4.dp))
                Text("Pro", fontSize = 9.sp, color = PRose, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("View all", fontSize = 10.sp, color = PRose, maxLines = 1)
        Text("with Pro", fontSize = 10.sp, color = PRose, maxLines = 1)
    }
}

// ── Saved Content Card ────────────────────────────────────────────────────────

@Composable
private fun SavedContentCard(
    title: String,
    lockBody: String,
    isPro: Boolean,
    modifier: Modifier = Modifier,
    onViewAll: () -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PCard)
            .border(1.dp, PBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PText, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        TextButton(onClick = onViewAll, colors = ButtonDefaults.textButtonColors(contentColor = PRose), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text("View all", fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        if (!isPro) {
            Icon(Icons.Outlined.Lock, contentDescription = null, tint = PMuted, modifier = Modifier.size(22.dp).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(6.dp))
            Text(lockBody, fontSize = 11.sp, color = PMuted)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PDark),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) { Text("Upgrade", color = Color.White, fontSize = 12.sp) }
        } else {
            Text("No items saved yet.", fontSize = 12.sp, color = PMuted)
        }
    }
}

// ── Personal Details Card ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalDetailsCard(
    age: Int,
    skinType: String,
    skinTone: String,
    undertone: String,
    location: String,
    isEditing: Boolean,
    ageDraft: String,
    skinTypeDraft: String,
    skinToneDraft: String,
    undertoneDraft: String,
    locationDraft: String,
    saveError: Boolean,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onAgeDraftChange: (String) -> Unit,
    onSkinTypeDraftChange: (String) -> Unit,
    onSkinToneDraftChange: (String) -> Unit,
    onUndertoneDraftChange: (String) -> Unit,
    onLocationDraftChange: (String) -> Unit
) {
    ProfileSection(
        title = "Personal Details",
        actionLabel = if (isEditing) null else "Edit",
        onAction = if (isEditing) null else onStartEdit
    ) {
        if (isEditing) {
            // Edit mode
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = ageDraft,
                onValueChange = { v ->
                    if (v.length <= 2 && v.all { it.isDigit() }) onAgeDraftChange(v)
                },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = saveError && ageDraft.isNotEmpty() && (ageDraft.toIntOrNull() ?: 0).let { it < 10 || it > 99 },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            PersonalDetailDropdown("Skin Type", skinTypeDraft, SKIN_TYPE_OPTIONS, onSkinTypeDraftChange)
            Spacer(Modifier.height(10.dp))
            PersonalDetailDropdown("Skin Tone", skinToneDraft, SKIN_TONE_OPTIONS, onSkinToneDraftChange)
            Spacer(Modifier.height(10.dp))
            PersonalDetailDropdown("Undertone", undertoneDraft, UNDERTONE_OPTIONS, onUndertoneDraftChange)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = locationDraft,
                onValueChange = { if (it.length <= 60) onLocationDraftChange(it) },
                label = { Text("Location") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (saveError) {
                Spacer(Modifier.height(6.dp))
                Text("Please enter a valid age (10–99).", fontSize = 12.sp, color = PRed)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onCancelEdit, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PDark)
                ) { Text("Save", color = Color.White) }
            }
        } else {
            // Display mode
            PersonalDetailRow(Icons.Outlined.CalendarToday, "Age",       if (age > 0) age.toString() else "—")
            PersonalDetailRow(Icons.Outlined.Opacity,       "Skin Type", skinType.ifEmpty { "—" })
            PersonalDetailRow(Icons.Outlined.Palette,       "Skin Tone", skinTone.ifEmpty { "—" })
            PersonalDetailRow(Icons.Outlined.WbSunny,       "Undertone", undertone.ifEmpty { "—" })
            PersonalDetailRow(Icons.Outlined.LocationOn,    "Location",  location.ifEmpty { "—" }, isLast = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalDetailDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onValueChange(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun PersonalDetailRow(icon: ImageVector, label: String, value: String, isLast: Boolean = false) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = PMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 14.sp, color = PText, modifier = Modifier.weight(1f))
            Text(value, fontSize = 14.sp, color = PMuted)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(12.dp))
        }
        if (!isLast) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PBorder))
    }
}

// ── Notification Preferences Card ────────────────────────────────────────────

@Composable
private fun NotificationPreferencesCard(
    notifScanReminders: Boolean,
    notifPromotions: Boolean,
    notifUpdates: Boolean,
    hasPermission: Boolean,
    onScanRemindersChange: (Boolean) -> Unit,
    onPromotionsChange: (Boolean) -> Unit,
    onUpdatesChange: (Boolean) -> Unit,
    onPermissionBannerTap: () -> Unit
) {
    ProfileSection(title = null) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = PRose, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Notification Preferences", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = PText)
        }
        if (!hasPermission) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PMutedBg)
                    .clickable(onClick = onPermissionBannerTap)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = PMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Enable notifications in Settings to receive updates.", fontSize = 12.sp, color = PMuted, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = PMuted, modifier = Modifier.size(12.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
        NotifToggleRow(
            icon = Icons.Outlined.Schedule,
            title = "Reminders",
            subtitle = "Scan reminders, daily tips",
            checked = notifScanReminders,
            enabled = hasPermission,
            onCheckedChange = onScanRemindersChange
        )
        NotifToggleRow(
            icon = Icons.Outlined.LocalOffer,
            title = "Promotions",
            subtitle = "Offers, new features",
            checked = notifPromotions,
            enabled = hasPermission,
            onCheckedChange = onPromotionsChange
        )
        NotifToggleRow(
            icon = Icons.Outlined.Campaign,
            title = "Updates",
            subtitle = "App updates, announcements",
            checked = notifUpdates,
            enabled = hasPermission,
            onCheckedChange = onUpdatesChange,
            isLast = true
        )
    }
}

@Composable
private fun NotifToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    isLast: Boolean = false
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (enabled) PMuted else Color(0xFFCCCCCC), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = if (enabled) PText else PMuted, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 11.sp, color = Color(0xFFAAAAAA))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PRose,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDDDDDD),
                    disabledCheckedTrackColor = Color(0xFFDDDDDD),
                    disabledUncheckedTrackColor = Color(0xFFDDDDDD)
                )
            )
        }
        if (!isLast) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PBorder))
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = PText)
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction, colors = ButtonDefaults.textButtonColors(contentColor = PRose)) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(actionLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        content()
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
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = tint, fontWeight = FontWeight.Medium)
                if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = PMuted)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = PMuted, modifier = Modifier.size(13.dp))
        }
        if (!isLast) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PBorder))
    }
}
