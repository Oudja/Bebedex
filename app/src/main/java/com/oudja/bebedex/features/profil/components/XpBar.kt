package com.oudja.bebedex.features.profil.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.tween

@Composable
fun XpBar(xp: Int, maxXp: Int = 100, modifier: Modifier = Modifier) {
    val progress = (xp.toFloat() / maxXp).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "xp-animation"
    )

    val backgroundColor = Color(0xFFB0BEC5)  // gris clair
    val progressColor = Color(0xFF42A5F5)    // bleu Pokémon

    Box(
        modifier
            .height(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(8.dp))
                .background(progressColor)
        )
    }
}
