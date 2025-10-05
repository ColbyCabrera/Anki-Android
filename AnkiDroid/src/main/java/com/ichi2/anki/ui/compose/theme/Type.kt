/* **************************************************************************************
 * Copyright (c) 2009 Andrew Dubya <andrewdubya@gmail.com>                              *
 * Copyright (c) 2009 Nicolas Raoul <nicolas.raoul@gmail.com>                           *
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Daniel Svard <daniel.svard@gmail.com>                             *
 * Copyright (c) 2010 Norbert Nagold <norbert.nagold@gmail.com>                         *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/
package com.ichi2.anki.ui.compose.theme

import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ichi2.anki.R

// Define Expressive Typography
val GoogleSansRounded = FontFamily(
    Font(R.font.google_sans_rounded_regular, FontWeight.Normal),
)

val GoogleSansFamily = FontFamily(
    Font(R.font.google_sans_family, FontWeight.Normal),
)

val RobotoFlex = FontFamily(
    Font(R.font.roboto_flex, FontWeight.Normal),
)



@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexLowWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    FontFamily(
        Font(
            R.font.google_sans_flex,
            variationSettings = FontVariation.Settings(
                FontVariation.width(3F),
                FontVariation.slant(-6f),
            )
        )
    )
} else {
    GoogleSansRounded
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ), displayLargeEmphasized = TextStyle(
        fontFamily = GoogleSansFlexLowWidth,
        fontWeight = FontWeight.Thin,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.5).sp,
    ), displayMedium = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ), displayMediumEmphasized = TextStyle(
        fontFamily = GoogleSansFlexLowWidth,
        fontWeight = FontWeight.Thin,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.25).sp
    ), displaySmall = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ), displaySmallEmphasized = TextStyle(
        fontFamily = GoogleSansFlexLowWidth,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp
    ),headlineLarge = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ), headlineMedium = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ), headlineSmall = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ), titleLarge = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ), titleLargeEmphasized = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),titleMedium = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ), titleSmall = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ), bodyLarge = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ), bodyMedium = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ), bodySmall = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ), labelLarge = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ), labelMedium = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ), labelSmall = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)