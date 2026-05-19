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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.ui.viewmodel.LoginViewModel
import com.appylab.lumi.ui.theme.PoppinsFont

private val LBackground = Color(0xFFFCFCFC)
private val LText       = Color(0xFF0A0A0A)
private val LMuted      = Color(0xFF737373)
private val LRose       = Color(0xFFFF637E)
private val LRed        = Color(0xFFDC2626)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPassword     by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Delete account and start over?") },
            text  = {
                Text(
                    "Your account data is stored only on this device and cannot be recovered. " +
                    "This will permanently delete all your scans and history."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForgotDialog = false
                        viewModel.deleteAccountAndStartOver()
                    }
                ) {
                    Text("Delete & Start Over", color = LRose, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel", color = LMuted)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(LBackground)) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = LRose
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(64.dp))

                Text(
                    "Welcome back",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = LText)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Sign in to continue",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 14.sp, color = LMuted)
                )

                Spacer(Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text("Email", fontSize = 11.sp, color = LMuted)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            uiState.storedEmail,
                            style = TextStyle(fontFamily = PoppinsFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = LText)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError?.let { err ->
                        { Text(err, color = LRed, fontSize = 12.sp) }
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = LMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { showForgotDialog = true }) {
                        Text("Forgot password?", fontSize = 13.sp, color = LMuted)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.login(onSuccess) },
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LRose,
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Sign In",
                            style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(Modifier.navigationBarsPadding().height(24.dp))
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
