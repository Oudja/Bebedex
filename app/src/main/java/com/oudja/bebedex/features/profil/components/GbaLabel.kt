package com.oudja.bebedex.features.profil.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.features.profil.pixelTextStyle

@Composable
fun GbaLabel(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF388E8E))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, style = pixelTextStyle.copy(color = Color.White))
    }
}

@Composable
fun GbaLabelWithEdit(text: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF388E8E))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = pixelTextStyle.copy(color = Color.White))
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Modifier",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
