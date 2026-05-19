package com.appylab.lumi.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.appylab.lumi.ui.viewmodel.EditProfileViewModel
import java.io.File

private val EPRose       = Color(0xFFFF637E)
private val EPBackground = Color(0xFFFCFCFC)
private val EPCard       = Color.White
private val EPBorder     = Color(0xFFFFCCD3)
private val EPText       = Color(0xFF0A0A0A)
private val EPMuted      = Color(0xFF525252)
private val EPMutedBg    = Color(0xFFF5F5F5)
private val EPRed        = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditProfileScreen(
    onBack: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var cameraOutputUri by remember { mutableStateOf<Uri?>(null) }

    // Collect one-shot snackbar events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    // Intercept system back gesture
    BackHandler {
        if (uiState.hasChanges) showDiscardDialog = true else onBack()
    }

    // Gallery launcher — validate dimensions before accepting
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.validateAndSetPhoto(uri)
    }

    // Camera launcher — validate dimensions after capture
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { captured ->
        if (captured) cameraOutputUri?.let { viewModel.validateAndSetPhoto(it) }
    }

    fun launchCamera() {
        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraOutputUri = uri
        showPhotoSheet = false
        cameraLauncher.launch(uri)
    }

    fun handleBack() {
        if (uiState.hasChanges) showDiscardDialog = true else onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(EPBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EPCard)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = ::handleBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = EPText)
                }
                Text(
                    "Edit Profile",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = EPText,
                    modifier = Modifier.align(Alignment.Center)
                )
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(20.dp),
                        color = EPRose,
                        strokeWidth = 2.dp
                    )
                } else {
                    TextButton(
                        onClick = { viewModel.save { onBack() } },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Save", color = EPRose, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }

            // ── Scrollable body ───────────────────────────────────────────
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EPRose)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(28.dp))

                    // ── Avatar ────────────────────────────────────────────
                    Box(contentAlignment = Alignment.BottomEnd) {
                        val photoSource: Any? = uiState.pendingPhotoUri ?: uiState.photoUrl.ifEmpty { null }
                        if (photoSource != null) {
                            AsyncImage(
                                model = photoSource,
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(96.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(96.dp).clip(CircleShape).background(EPMutedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = uiState.displayName
                                    .split(" ")
                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                    .take(2).joinToString("")
                                if (initials.isNotEmpty()) {
                                    Text(initials, color = EPMuted, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
                                } else {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = EPMuted, modifier = Modifier.size(40.dp))
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(EPRose)
                                .clickable { showPhotoSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Change Photo",
                        color = EPRose,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showPhotoSheet = true }
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── Display Name field ────────────────────────────────
                    OutlinedTextField(
                        value = uiState.displayNameDraft,
                        onValueChange = viewModel::updateDisplayNameDraft,
                        label = { Text("Display Name") },
                        singleLine = true,
                        isError = uiState.displayNameError != null,
                        supportingText = {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    uiState.displayNameError ?: "",
                                    color = EPRed,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${uiState.displayNameDraft.length} / 40",
                                    color = if (uiState.displayNameDraft.length >= 40) EPRed else EPMuted,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // ── Email (read-only) ─────────────────────────────────
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = {},
                        label = { Text("Email") },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = EPMuted, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── Change Password (email auth only) ─────────────────
                    if (uiState.isEmailAuth) {
                        EPActionRow(title = "Change Password", onClick = onChangePasswordClick)
                        Spacer(Modifier.height(12.dp))
                    }

                    // ── Google Connected (Google auth only) ───────────────
                    if (uiState.isGoogleConnected) {
                        GoogleConnectedRow()
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Discard Changes (only when dirty) ─────────────────
                    if (uiState.hasChanges) {
                        TextButton(onClick = { showDiscardDialog = true }) {
                            Text("Discard Changes", color = EPRose, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.navigationBarsPadding().height(24.dp))
                }
            }
        }

        // ── Snackbar ──────────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        ) { data ->
            Snackbar(snackbarData = data, containerColor = Color(0xFF1A1A1A), contentColor = Color.White)
        }
    }

    // ── Photo picker sheet ────────────────────────────────────────────────────
    if (showPhotoSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPhotoSheet = false },
            sheetState = sheetState,
            containerColor = EPCard
        ) {
            Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 8.dp)) {
                PhotoSheetRow(
                    icon = Icons.Outlined.CameraAlt,
                    label = "Take Photo",
                    onClick = ::launchCamera
                )
                PhotoSheetRow(
                    icon = Icons.Outlined.Photo,
                    label = "Choose from Gallery",
                    onClick = {
                        showPhotoSheet = false
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
        }
    }

    // ── Discard changes dialog ────────────────────────────────────────────────
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?", fontWeight = FontWeight.SemiBold) },
            text = { Text("Your unsaved changes will be lost.", color = EPMuted) },
            confirmButton = {
                TextButton(
                    onClick = { showDiscardDialog = false; onBack() },
                    colors = ButtonDefaults.textButtonColors(contentColor = EPRed)
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep Editing") }
            },
            containerColor = EPCard
        )
    }
}

@Composable
private fun EPActionRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, EPBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, color = EPText, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = EPMuted, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun GoogleConnectedRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, EPBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF4285F4)),
            contentAlignment = Alignment.Center
        ) {
            Text("G", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text("Connected", fontSize = 15.sp, color = EPText)
    }
}

@Composable
private fun PhotoSheetRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = EPText, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, color = EPText)
    }
}
