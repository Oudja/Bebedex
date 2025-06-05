package com.oudja.bebedex.features.profil.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class BebeStats(
    val eveil: Float,
    val robustesse: Float,
    val beaute: Float,
    val grace: Float,
    val sociabilite: Float
)

@Composable
fun StatRadar(stats: BebeStats, modifier: Modifier = Modifier) {
    val values = listOf(stats.eveil, stats.robustesse, stats.beaute, stats.grace, stats.sociabilite)
    val labels = listOf("Éveil", "Robuste", "Beauté", "Grâce", "Sociabilité")
    val maxStat = 10f

    Row(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Canvas(modifier = Modifier.weight(1.5f).aspectRatio(1f)) {
            val radius = min(size.width, size.height) / 2.5f
            val center = Offset(size.width / 2, size.height / 2)
            val pointCount = values.size

            for (i in 1..5) {
                val path = Path()
                for (j in 0 until pointCount) {
                    val angle = j * (2 * PI / pointCount) - PI / 2
                    val r = radius * i / 5
                    val x = center.x + cos(angle).toFloat() * r
                    val y = center.y + sin(angle).toFloat() * r
                    if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, Color.LightGray, style = Stroke(1.dp.toPx()))
            }

            val statPath = Path()
            values.forEachIndexed { i, value ->
                val angle = i * (2 * PI / pointCount) - PI / 2
                val r = radius * (value / maxStat)
                val x = center.x + cos(angle).toFloat() * r
                val y = center.y + sin(angle).toFloat() * r
                if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
            }
            statPath.close()
            drawPath(statPath, Color(0xFF5E9EFF).copy(alpha = 0.4f))
            drawPath(statPath, Color(0xFF3478F6), style = Stroke(2.dp.toPx()))

            labels.forEachIndexed { i, label ->
                val angle = i * (2 * PI / pointCount) - PI / 2
                val x = center.x + cos(angle).toFloat() * (radius + 24.dp.toPx())
                val y = center.y + sin(angle).toFloat() * (radius + 24.dp.toPx())
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    y,
                    Paint().apply {
                        textSize = 28f
                        color = android.graphics.Color.DKGRAY
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }

        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text("Détails :", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Éveil : ${stats.eveil}")
            Text("Robuste : ${stats.robustesse}")
            Text("Beauté : ${stats.beaute}")
            Text("Grâce : ${stats.grace}")
            Text("Sociabilité : ${stats.sociabilite}")
        }
    }
}
