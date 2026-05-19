package com.appylab.lumi.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.appylab.lumi.ui.viewmodel.OnboardingViewModel
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob7Background  = Color(0xFFFCFCFC)
private val Ob7Rose        = Color(0xFFFF637E)
private val Ob7RoseCard    = Color(0xFFFFF1F2)
private val Ob7TextPrimary = Color(0xFF0A0A0A)
private val Ob7TextMuted   = Color(0xFF737373)
private val Ob7Border      = Color(0xFFE0E0E0)
private val Ob7MutedCard   = Color(0xFFF5F5F5)

@Composable
fun OnboardingScreen7(
    viewModel: OnboardingViewModel? = null,
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val context = LocalContext.current
    val localCameraPermission    = remember { mutableStateOf(false) }
    val localNotificationPermission = remember { mutableStateOf(false) }

    val savedAuthType by (
        viewModel?.authType?.collectAsState()
            ?: remember { mutableStateOf("") }
        )
    val cameraEnabled by (
        viewModel?.cameraPermissionGranted?.collectAsState()
            ?: localCameraPermission
        )
    val notificationEnabled by (
        viewModel?.notificationPermissionGranted?.collectAsState()
            ?: localNotificationPermission
        )
    var selectedAuthType by remember(savedAuthType) { mutableStateOf(savedAuthType) }

    fun updateCameraPermission(granted: Boolean) {
        viewModel?.setCameraPermissionGranted(granted)
            ?: run { localCameraPermission.value = granted }
    }

    fun updateNotificationPermission(granted: Boolean) {
        viewModel?.setNotificationPermissionGranted(granted)
            ?: run { localNotificationPermission.value = granted }
    }

    fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        if (hasPermission(Manifest.permission.CAMERA)) {
            updateCameraPermission(true)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            updateNotificationPermission(true)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> updateCameraPermission(granted) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> updateNotificationPermission(granted) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob7Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)
        ) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Ob7TextPrimary
                    )
                }
                Text(
                    text = "7 of 8",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, color = Ob7TextMuted)
                )
            }

            Spacer(Modifier.height(28.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                // Heading
                Text(
                    text = "Let's personalise your\nexperience",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob7TextPrimary,
                        lineHeight = 30.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Choose how you'd like to get started",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 13.sp,
                        color = Ob7TextMuted,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // Sign-in options
                Ob7SignInOptionRow(
                    icon = null,
                    googleLetter = true,
                    title = "Continue with Google",
                    subtitle = "Coming soon",
                    selected = false,
                    enabled = false,
                    onClick = {}
                )

                Spacer(Modifier.height(10.dp))

                Ob7SignInOptionRow(
                    icon = Icons.Outlined.Email,
                    googleLetter = false,
                    title = "Continue with Email",
                    subtitle = null,
                    selected = selectedAuthType == "email",
                    onClick = {
                        selectedAuthType = "email"
                        viewModel?.setAuthType("email")
                    }
                )

                Spacer(Modifier.height(14.dp))

                // OR divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Ob7Border, thickness = 1.dp)
                    Text(text = "  OR  ", style = TextStyle(fontFamily = PoppinsFont, fontSize = 12.sp, color = Ob7TextMuted))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Ob7Border, thickness = 1.dp)
                }

                Spacer(Modifier.height(14.dp))

                Ob7SignInOptionRow(
                    icon = Icons.Outlined.Person,
                    googleLetter = false,
                    title = "Skip to Home",
                    subtitle = "Limited features available",
                    selected = selectedAuthType == "skip",
                    onClick = {
                        selectedAuthType = "skip"
                        viewModel?.setAuthType("skip")
                    }
                )

                Spacer(Modifier.height(20.dp))

                QuoteCard(
                    text = "LUMI doesn't compare you to anyone.\nIt only ever compares you to your best self.",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Allow permissions to get the most out of LUMI",
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob7TextPrimary
                    )
                )

                Spacer(Modifier.height(12.dp))

                Ob7PermissionToggleRow(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Camera Access",
                    description = "Required for face analysis and AR try-on",
                    checked = cameraEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            updateCameraPermission(false)
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))

                Ob7PermissionToggleRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Notification Access",
                    description = "Get personalized beauty tips and reminders",
                    checked = notificationEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                updateNotificationPermission(true)
                            }
                        } else {
                            updateNotificationPermission(false)
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Privacy notice
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Ob7MutedCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Ob7TextMuted
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Your data is encrypted and never sold. You can change these permissions anytime in Settings.",
                            style = TextStyle(fontFamily = PoppinsFont, 
                                fontSize = 11.sp,
                                color = Ob7TextMuted,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // Fixed bottom overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Ob7Background)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    viewModel?.saveAccessSettings(
                        authType = selectedAuthType.ifEmpty { "skip" },
                        cameraGranted = cameraEnabled,
                        notifGranted = notificationEnabled
                    )
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob7TextPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = buildAnnotatedString {
                    append("By continuing, you agree to our ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("Terms of Use")
                    }
                    append(" and acknowledge our ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("Privacy Policy")
                    }
                    append(".")
                },
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 10.sp,
                    color = Ob7TextMuted,
                    lineHeight = 15.sp
                )
            )
        }
    }
}

@Composable
private fun QuoteCard(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF1F2)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Text(
                "❝",
                style = TextStyle(fontFamily = PoppinsFont, 
                    fontSize = 22.sp,
                    color = Color(0xFFFF637E),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text,
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 13.sp,
                        color = Color(0xFF0A0A0A),
                        lineHeight = 20.sp,
                        fontStyle = FontStyle.Italic
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "— Lumi ✦",
                    style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = Color(0xFFFF637E))
                )
            }
        }
    }
}

@Composable
private fun Ob7SignInOptionRow(
    icon: ImageVector?,
    googleLetter: Boolean,
    title: String,
    subtitle: String?,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val borderColor = when {
        !enabled  -> Ob7Border.copy(alpha = 0.5f)
        selected  -> Ob7Rose
        else      -> Ob7Border
    }
    val textColor   = if (enabled) Ob7TextPrimary else Ob7TextMuted.copy(alpha = 0.6f)
    val iconTint    = if (enabled) (if (selected) Ob7Rose else Ob7TextMuted) else Ob7TextMuted.copy(alpha = 0.4f)
    val bgColor     = if (enabled) Color.White else Color(0xFFFAFAFA)

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (googleLetter) {
                    Text(
                        text = "G",
                        style = TextStyle(fontFamily = PoppinsFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) Ob7Rose else Ob7Rose.copy(alpha = 0.4f)
                        )
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(fontFamily = PoppinsFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = Ob7TextMuted.copy(alpha = if (enabled) 1f else 0.6f))
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }
    }
}

@Composable
private fun Ob7PermissionToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Ob7RoseCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Ob7Rose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ob7TextPrimary
                    )
                )
                Text(
                    text = description,
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 11.sp,
                        color = Ob7TextMuted,
                        lineHeight = 16.sp
                    )
                )
            }

            Spacer(Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Ob7Rose,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = Ob7Border,
                    uncheckedThumbColor = Color.White
                )
            )
        }
    }
}
