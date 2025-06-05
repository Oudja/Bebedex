package com.oudja.bebedex.features.profil.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.oudja.bebedex.R
import com.oudja.bebedex.features.profil.pixelTextStyle

@Composable
fun BebeCardHeader(
    name: String,
    gender: String,
    level: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC5E1E6)),
        border = BorderStroke(3.dp, Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedGif(modifier = Modifier.size(96.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = pixelTextStyle.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(6.dp))
                    when (gender.lowercase()) {
                        "fille" -> Text("♀", color = Color(0xFFE91E63), style = pixelTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        "garçon", "garcon" -> Text("♂", color = Color(0xFF2196F3), style = pixelTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                }
                Text("Niveau $level", style = pixelTextStyle)
            }
        }
    }
}

@Composable
fun AnimatedGif(modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(R.drawable.bebe_gif)
            .crossfade(true)
            .build(),
        contentDescription = "Bébé qui baille",
        modifier = modifier
    )
}
