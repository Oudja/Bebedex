package com.oudja.bebedex.features.profil.screens

import android.content.Context
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import com.oudja.bebedex.features.profil.utils.GrowthUtils.croissanceList
import com.oudja.bebedex.features.profil.utils.GrowthUtils.loadGrowthHistory
import com.oudja.bebedex.features.profil.utils.GrowthUtils.clearGrowthHistory



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CourbeScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        loadGrowthHistory(context)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Courbes de croissance", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (croissanceList.isEmpty()) {
            Text("Aucune donnée de croissance enregistrée.", style = MaterialTheme.typography.bodyMedium)
        } else {
            croissanceList.forEach {
                Text("${it.datetime} : ${it.taille} cm, ${it.poids} kg")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                croissanceList.clear()
                context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE).edit()
                    .remove("croissance_data")
                    .remove("taille")
                    .remove("poids")
                    .apply()
            }) {
                Text("Réinitialiser")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Taille (cm)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
            LineChart(
                data = croissanceList.map { it.datetime to it.taille },
                color = Color.Blue,
                unit = "",
                alignLeft = true
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Poids (kg)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
            LineChart(
                data = croissanceList.map { it.datetime to it.poids },
                color = Color.Red,
                unit = "",
                alignLeft = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Retour")
        }
    }
}

@Composable
fun LineChart(data: List<Pair<String, Float>>, color: Color, unit: String, alignLeft: Boolean = false) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(start = if (alignLeft) 0.dp else 64.dp, end = 16.dp)) {

        val maxY = data.maxOfOrNull { it.second } ?: return@Canvas
        val minY = data.minOfOrNull { it.second } ?: return@Canvas

        val labelPaint = Paint().apply {
            this.color = android.graphics.Color.DKGRAY
            textSize = 24f
        }

        val labelOffset = if (alignLeft) 64f else 60f
        for (i in 0..4) {
            val y = size.height - (i / 4f) * size.height
            val value = minY + i * (maxY - minY) / 4
            drawContext.canvas.nativeCanvas.drawText("%.1f".format(value), 0f, y, labelPaint)
        }

        val path = Path()
        data.forEachIndexed { index, (_, value) ->
            val x = labelOffset + (size.width - labelOffset) * index / (data.size - 1).coerceAtLeast(1)
            val y = size.height - ((value - minY) / (maxY - minY)) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 4f))

        data.forEachIndexed { index, (label, _) ->
            val x = labelOffset + (size.width - labelOffset) * index / (data.size - 1).coerceAtLeast(1)
            drawContext.canvas.nativeCanvas.drawText(label, x - 50f, size.height + 32f, labelPaint)
        }
    }
}
