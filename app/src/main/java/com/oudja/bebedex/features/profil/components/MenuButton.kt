package com.oudja.bebedex.features.profil.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.features.profil.pixelTextStyle

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE6F2F5), // bleu pastel doux
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text, style = pixelTextStyle)
    }
}
