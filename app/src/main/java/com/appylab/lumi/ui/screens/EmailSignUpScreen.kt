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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.viewmodel.EmailSignUpViewModel
import com.appylab.lumi.ui.theme.PoppinsFont

private val ESBackground = Color(0xFFFCFCFC)
private val ESCard       = Color.White
private val ESText       = Color(0xFF0A0A0A)
private val ESMuted      = Color(0xFF737373)
private val ESRose       = Color(0xFFFF637E)
private val ESRed        = Color(0xFFDC2626)

@Composable
fun EmailSignUpScreen(
    viewModel: EmailSignUpViewModel,
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPassword        by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    Box(modifier = Modifier.fillMaxSize().background(ESBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ESCard)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = ESText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    "Create Account",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ESText),
                    modifier = Modifier.align(Alignment.Center)
                )
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(20.dp),
                        color = ESRose,
                        strokeWidth = 2.dp
                    )
                } else {
                    TextButton(
                        onClick = { viewModel.signUp(onSuccess) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Sign Up", color = ESRose, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                Text(
                    "Your account is stored locally on this device.",
                    fontSize = 13.sp,
                    color = ESMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError?.let { err ->
                        { Text(err, color = ESRed, fontSize = 12.sp) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    isError = uiState.passwordError != null,
                    supportingText = {
                        Text(
                            uiState.passwordError ?: "At least 8 characters",
                            color = if (uiState.passwordError != null) ESRed else ESMuted,
                            fontSize = 12.sp
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = ESMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    isError = uiState.confirmPasswordError != null,
                    supportingText = uiState.confirmPasswordError?.let { err ->
                        { Text(err, color = ESRed, fontSize = 12.sp) }
                    },
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                if (showConfirmPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = ESMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

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
