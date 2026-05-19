package com.appylab.lumi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.appylab.lumi.R

val PoppinsFont = FontFamily(
    Font(R.font.poppins_thin,              FontWeight.Thin),
    Font(R.font.poppins_thinitalic,        FontWeight.Thin,      FontStyle.Italic),
    Font(R.font.poppins_extra_light,       FontWeight.ExtraLight),
    Font(R.font.poppins_extra_lightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.poppins_light,             FontWeight.Light),
    Font(R.font.poppins_lightitalic,       FontWeight.Light,     FontStyle.Italic),
    Font(R.font.poppins_regular,           FontWeight.Normal),
    Font(R.font.poppins_italic,            FontWeight.Normal,    FontStyle.Italic),
    Font(R.font.poppins_medium,            FontWeight.Medium),
    Font(R.font.poppins_mediumitalic,      FontWeight.Medium,    FontStyle.Italic),
    Font(R.font.poppins_semibold,          FontWeight.SemiBold),
    Font(R.font.poppins_semibolditalic,    FontWeight.SemiBold,  FontStyle.Italic),
    Font(R.font.poppins_bold,              FontWeight.Bold),
    Font(R.font.poppins_bolditalic,        FontWeight.Bold,      FontStyle.Italic),
    Font(R.font.poppins_extra_bold,        FontWeight.ExtraBold),
    Font(R.font.poppins_extra_bolditalic,  FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.poppins_black,             FontWeight.Black),
    Font(R.font.poppins_blackitalic,       FontWeight.Black,     FontStyle.Italic),
)

val Typography = Typography(
    // Screen-level headings
    headlineSmall = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Top app bar titles, major detail titles
    titleLarge = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Section headings, card titles
    titleMedium = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    // Dense labels, compact section headers
    titleSmall = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // Primary reading text
    bodyLarge = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    // Secondary text, list supporting text
    bodyMedium = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // Metadata, helper text
    bodySmall = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    // Button labels
    labelLarge = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // Chips, badges, small controls
    labelMedium = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
)
