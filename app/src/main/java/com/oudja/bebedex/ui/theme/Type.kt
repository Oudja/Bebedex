// Fichier : ui/theme/Type.kt (ou un nouveau fichier dans ce dossier)
package com.oudja.bebedex.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oudja.bebedex.R

val PressStart2P = FontFamily(
    Font(R.font.press_start_2p)
)

val CustomTypography = Typography(
    titleMedium = TextStyle(
        fontFamily = PressStart2P,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, // ↓ taille pour mieux tenir dans les cartes
        lineHeight = 14.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PressStart2P,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PressStart2P,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 10.sp
    )
)
