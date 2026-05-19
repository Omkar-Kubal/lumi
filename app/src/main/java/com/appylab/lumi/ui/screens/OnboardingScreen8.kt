package com.appylab.lumi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.appylab.lumi.ui.theme.PoppinsFont

private val Ob8Background  = Color(0xFFFCFCFC)
private val Ob8Rose        = Color(0xFFFF637E)
private val Ob8RoseCard    = Color(0xFFFFF1F2)
private val Ob8TextPrimary = Color(0xFF0A0A0A)
private val Ob8TextMuted   = Color(0xFF737373)

@Composable
fun OnboardingScreen8(
    displayName: String = "",
    onStartScan: () -> Unit = {},
    onExplore: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ob8Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        SparkleAvatar(displayName = displayName)

        Spacer(Modifier.height(20.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Ob8TextPrimary, fontWeight = FontWeight.Bold, fontSize = 26.sp)) {
                    append("Welcome, ")
                }
                withStyle(SpanStyle(color = Ob8TextPrimary, fontWeight = FontWeight.Bold, fontSize = 26.sp)) {
                    append(displayName.ifEmpty { "there" })
                }
                withStyle(SpanStyle(color = Ob8Rose, fontWeight = FontWeight.Bold, fontSize = 26.sp)) {
                    append(" ✦")
                }
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Your personalised beauty profile is ready.",
            style = TextStyle(fontFamily = PoppinsFont, 
                fontSize = 14.sp,
                color = Ob8TextMuted,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        QuoteCard(
            text = "You were beautiful before LUMI told you so.\nWe're just here to help you see it.",
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Achievement badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            listOf("Goals saved ✓", "Skin profile ready ✓", "LUMI activated ✓").forEach { badge ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Ob8Rose
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = badge,
                        style = TextStyle(fontFamily = PoppinsFont, fontSize = 11.sp, color = Ob8TextMuted)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Ob8Rose,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Start my first scan →",
                style = TextStyle(fontFamily = PoppinsFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onExplore,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Explore first",
                style = TextStyle(fontFamily = PoppinsFont, fontSize = 13.sp, color = Ob8TextMuted)
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SparkleAvatar(displayName: String) {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rose     = Color(0xFFFF637E)
            val softRose = Color(0xFFFFCCD3)
            val cx      = size.width / 2f
            val cy      = size.height / 2f
            val orbitR  = 52.dp.toPx()

            repeat(8) { i ->
                val angle = (i * 45.0) * (PI / 180.0)
                val sx    = (cx + orbitR * cos(angle)).toFloat()
                val sy    = (cy + orbitR * sin(angle)).toFloat()
                val r     = if (i % 2 == 0) 6.dp.toPx() else 4.dp.toPx()
                val tint  = if (i % 2 == 0) rose else softRose
                drawLine(tint, Offset(sx, sy - r), Offset(sx, sy + r), 1.5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(tint, Offset(sx - r, sy), Offset(sx + r, sy), 1.5.dp.toPx(), cap = StrokeCap.Round)
            }
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Ob8RoseCard)
                .border(2.dp, Ob8Rose, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (displayName.isNotEmpty()) {
                Text(
                    text = displayName.first().uppercaseChar().toString(),
                    style = TextStyle(fontFamily = PoppinsFont, 
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ob8Rose
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = Ob8Rose
                )
            }
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
