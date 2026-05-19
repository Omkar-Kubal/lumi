package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylab.lumi.data.db.NotificationEntity
import com.appylab.lumi.ui.viewmodel.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val NBackground = Color(0xFFFCFCFC)
private val NText       = Color(0xFF0A0A0A)
private val NMuted      = Color(0xFF737373)
private val NRose       = Color(0xFFFF637E)
private val NRoseSoft   = Color(0xFFFFF0F2)
private val NDivider    = Color(0xFFF0F0F0)

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpen: () -> Unit = {}
) {
    val notifications by viewModel.notifications.collectAsState()

    LaunchedEffect(Unit) { onOpen() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NText,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                "Notifications",
                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = NText),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFF5F5F5), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.NotificationsNone,
                        contentDescription = null,
                        tint = NRose,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "You're all caught up",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NText)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Scan reminders and personalised updates will appear here.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = NMuted,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding()
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationItem(notif)
                    HorizontalDivider(color = NDivider, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notif: NotificationEntity) {
    val bgColor = if (notif.isRead) NBackground else NRoseSoft

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF5F5F5), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconFor(notif.type),
                contentDescription = null,
                tint = NRose,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notif.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = if (notif.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = NText
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                notif.body,
                style = TextStyle(fontSize = 13.sp, color = NMuted, lineHeight = 18.sp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            formatTimestamp(notif.timestamp),
            style = TextStyle(fontSize = 11.sp, color = NMuted)
        )
    }
}

private fun iconFor(type: String): ImageVector = when (type) {
    "scan_complete"  -> Icons.Outlined.CameraAlt
    "palette_saved"  -> Icons.Outlined.BookmarkBorder
    "glow_up_ready"  -> Icons.Outlined.AutoAwesome
    else             -> Icons.Outlined.NotificationsNone
}

private fun formatTimestamp(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000L        -> "Just now"
        diff < 3_600_000L     -> "${diff / 60_000}m ago"
        diff < 86_400_000L    -> "${diff / 3_600_000}h ago"
        diff < 172_800_000L   -> "Yesterday"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(ts))
    }
}
