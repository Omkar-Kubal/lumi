package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.ui.viewmodel.ChangePasswordViewModel

private val CPRose       = Color(0xFFFF637E)
private val CPBackground = Color(0xFFFCFCFC)
private val CPCard       = Color.White
private val CPText       = Color(0xFF0A0A0A)
private val CPMuted      = Color(0xFF525252)
private val CPMutedBg    = Color(0xFFF5F5F5)
private val CPRed        = Color(0xFFDC2626)
private val CPBorder     = Color(0xFFFFCCD3)

@Composable
internal fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    viewModel: ChangePasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCurrentPw  by remember { mutableStateOf(false) }
    var showNewPw      by remember { mutableStateOf(false) }
    var showConfirmPw  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    Box(modifier = Modifier.fillMaxSize().background(CPBackground)) {

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CPRose)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Top bar ───────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CPCard)
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = CPText)
                    }
                    Text(
                        "Change Password",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = CPText,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(20.dp),
                            color = CPRose,
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = { viewModel.save { onBack() } },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text("Save", color = CPRose, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }

                // ── Scrollable body ───────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))

                    // Banner for first-time password setup
                    if (!uiState.hasExistingPassword) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CPMutedBg, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "You haven't set a local password yet. Create one below.",
                                fontSize = 13.sp,
                                color = CPMuted,
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // Current Password (only when one exists)
                    if (uiState.hasExistingPassword) {
                        OutlinedTextField(
                            value = uiState.currentPassword,
                            onValueChange = viewModel::updateCurrentPassword,
                            label = { Text("Current Password") },
                            singleLine = true,
                            visualTransformation = if (showCurrentPw) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            isError = uiState.currentPasswordError != null,
                            supportingText = uiState.currentPasswordError?.let {
                                { Text(it, color = CPRed, fontSize = 12.sp) }
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPw = !showCurrentPw }) {
                                    Icon(
                                        if (showCurrentPw) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = CPMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // New Password
                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = viewModel::updateNewPassword,
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = if (showNewPw) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        isError = uiState.newPasswordError != null,
                        supportingText = {
                            Text(
                                uiState.newPasswordError ?: "At least 8 characters",
                                color = if (uiState.newPasswordError != null) CPRed else CPMuted,
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showNewPw = !showNewPw }) {
                                Icon(
                                    if (showNewPw) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = CPMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Confirm New Password
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::updateConfirmPassword,
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = if (showConfirmPw) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        isError = uiState.confirmPasswordError != null,
                        supportingText = uiState.confirmPasswordError?.let {
                            { Text(it, color = CPRed, fontSize = 12.sp) }
                        },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPw = !showConfirmPw }) {
                                Icon(
                                    if (showConfirmPw) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = CPMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.navigationBarsPadding().height(24.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        ) { data ->
            Snackbar(snackbarData = data, containerColor = Color(0xFF1A1A1A), contentColor = Color.White)
        }
    }
}
