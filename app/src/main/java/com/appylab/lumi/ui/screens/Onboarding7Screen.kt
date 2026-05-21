package com.appylab.lumi.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appylab.lumi.ui.theme.LumiTheme
import com.appylab.lumi.ui.theme.PoppinsFont

// ─── Colours ─────────────────────────────────────────────────────────────────

private val Ob7Background  = Color(0xFFFCFCFC)
private val Ob7Rose        = Color(0xFFFF637E)
private val Ob7RoseCard    = Color(0xFFFFF1F2)
private val Ob7Text        = Color(0xFF0A0A0A)
private val Ob7Muted       = Color(0xFF737373)
private val Ob7ChipBorder  = Color(0xFFEBEBEB)
private val Ob7DotInactive = Color(0xFFE0E0E0)
private val Ob7Divider     = Color(0xFFE8E8E8)
private val GoogleBlue     = Color(0xFF4285F4)
private val GoogleRed      = Color(0xFFEA4335)
private val GoogleYellow   = Color(0xFFFBBC05)
private val GoogleGreen    = Color(0xFF34A853)

private const val OB7_TOTAL   = 8
private const val OB7_CURRENT = 6   // 0-indexed → page 7

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun Onboarding7Screen(
    onBack: () -> Unit,
    onContinueGoogle: () -> Unit,
    onContinueEmail: () -> Unit,
    onSkipToHome: () -> Unit,
    onContinue: () -> Unit
) {
    var cameraEnabled       by remember { mutableStateOf(true) }
    var notifEnabled        by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob7Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ob7Text,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "7 of 8",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob7Muted
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Headline ───────────────────────────────────────────────────
            Text(
                text = "Let\u2019s personalise your experience",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ob7Text,
                    textAlign = TextAlign.Start,
                    lineHeight = 30.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Create an account to save your results and track your glow.",
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ob7Muted,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Auth buttons ───────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Google
                AuthButton(
                    onClick = onContinueGoogle,
                    leadingContent = {
                        GoogleGIcon(modifier = Modifier.size(22.dp))
                    },
                    label = "Continue with Google"
                )

                // Email
                AuthButton(
                    onClick = onContinueEmail,
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = Ob7Rose,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = "Continue with Email"
                )

                // OR divider
                OrDivider()

                // Skip
                AuthButton(
                    onClick = onSkipToHome,
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Ob7Muted,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = "Skip to Home",
                    sublabel = "Limited features available",
                    muted = true
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Quote card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ob7RoseCard)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Column {
                    Text(
                        text = "\u201C\u201C",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ob7Rose,
                            lineHeight = 16.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "LUMI doesn\u2019t compare you to anyone.\nIt only ever compares you to your best self.",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ob7Text,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("— Lumi ")
                            withStyle(SpanStyle(color = Ob7Rose)) { append("\u2736") }
                        },
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            color = Ob7Muted,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Permissions ────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Allow permissions to get the most out of LUMI",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob7Muted
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Camera
                PermissionRow(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Camera Access",
                    description = "Required for face scans and personalised analysis.",
                    checked = cameraEnabled,
                    onCheckedChange = { cameraEnabled = it }
                )

                Spacer(Modifier.height(12.dp))

                // Notifications
                PermissionRow(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "Notification Access",
                    description = "Get reminders, beauty tips, and personalised updates.",
                    checked = notifEnabled,
                    onCheckedChange = { notifEnabled = it }
                )

                Spacer(Modifier.height(12.dp))

                // Privacy note
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Ob7RoseCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = Ob7Rose,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Your privacy is important to us. We never share your personal data. Your information is secure and used only to personalise your experience.",
                        style = TextStyle(
                            fontFamily = PoppinsFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Ob7Muted,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))

            // ── Page dots ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(OB7_TOTAL) { index ->
                    val isActive = index == OB7_CURRENT
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Ob7Rose else Ob7DotInactive)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── CTA button ─────────────────────────────────────────────────
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ob7Text,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue  \u2192",
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── ToS footer ─────────────────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    append("By continuing, you agree to our ")
                    withStyle(SpanStyle(color = Ob7Rose, fontWeight = FontWeight.Medium)) {
                        append("Terms of Use")
                    }
                    append(" and acknowledge our ")
                    withStyle(SpanStyle(color = Ob7Rose, fontWeight = FontWeight.Medium)) {
                        append("Privacy Policy")
                    }
                },
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ob7Muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun AuthButton(
    onClick: () -> Unit,
    leadingContent: @Composable () -> Unit,
    label: String,
    sublabel: String? = null,
    muted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Ob7ChipBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = if (sublabel != null) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingContent()
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (muted) Ob7Muted else Ob7Text
                )
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    style = TextStyle(
                        fontFamily = PoppinsFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Ob7Muted
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Ob7Muted,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Ob7Divider)
        )
        Text(
            text = "  OR  ",
            style = TextStyle(
                fontFamily = PoppinsFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Ob7Muted
            )
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Ob7Divider)
        )
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Ob7RoseCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Ob7Rose,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ob7Text
                )
            )
            Text(
                text = description,
                style = TextStyle(
                    fontFamily = PoppinsFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ob7Muted,
                    lineHeight = 16.sp
                )
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Ob7Rose,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Ob7DotInactive,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

/** Simplified Google "G" logo — four-colour arc quadrants */
@Composable
private fun GoogleGIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Ob7ChipBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GoogleBlue
            )
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
private fun Onboarding7ScreenPreview() {
    LumiTheme {
        Onboarding7Screen(
            onBack = {},
            onContinueGoogle = {},
            onContinueEmail = {},
            onSkipToHome = {},
            onContinue = {}
        )
    }
}
