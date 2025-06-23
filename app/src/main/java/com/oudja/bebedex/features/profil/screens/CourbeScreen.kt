package com.oudja.bebedex.features.profil.screens

import android.content.Context
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import com.oudja.bebedex.features.profil.utils.GrowthUtils.croissanceList
import com.oudja.bebedex.features.profil.utils.GrowthUtils.loadGrowthHistory
import com.oudja.bebedex.features.profil.utils.GrowthUtils.clearGrowthHistory
import com.oudja.bebedex.features.profil.pixelTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import com.oudja.bebedex.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CourbeScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        loadGrowthHistory(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB2FEFA), Color(0xFF0ED2F7), Color(0xFFFAFFD1)),
                    startY = 0f, endY = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(12.dp))
            Image(
                painter = painterResource(id = R.drawable.reglebalance),
                contentDescription = "Logo règle et balance pixel art",
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = 4.dp)
            )
            Text(
                "Courbes de Croissance",
                style = pixelTextStyle.copy(fontSize = 18.sp),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Spacer(Modifier.height(8.dp))
            if (croissanceList.isEmpty()) {
                Text("Aucune donnée de croissance enregistrée.", style = pixelTextStyle)
            } else {
                // Carte historique des mesures
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(3.dp, Color.White),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .heightIn(max = 220.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(croissanceList) { it ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFB3E5FC)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    "${it.datetime} : ${it.taille} cm, ${it.poids} kg",
                                    style = pixelTextStyle,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                // Carte courbes de croissance
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(4.dp, Color.White),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        // Titre taille
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.regle),
                                contentDescription = "Icône règle pixel art",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Taille (cm)", style = pixelTextStyle.copy(fontSize = 15.sp))
                        }
                        PokemonLineChart(
                            data = croissanceList.map { it.datetime to it.taille },
                            color = Color(0xFF1976D2),
                            axisColor = Color(0xFF1976D2),
                            unit = "",
                            alignLeft = true
                        )
                        Spacer(Modifier.height(24.dp))
                        // Titre poids
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.balance),
                                contentDescription = "Icône balance pixel art",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Poids (kg)", style = pixelTextStyle.copy(fontSize = 15.sp))
                        }
                        PokemonLineChart(
                            data = croissanceList.map { it.datetime to it.poids },
                            color = Color(0xFFD32F2F),
                            axisColor = Color(0xFFD32F2F),
                            unit = "",
                            alignLeft = true
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(50),
                    containerColor = Color(0xFF7EC4CF),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour", modifier = Modifier.size(28.dp))
                }
                Button(
                    onClick = {
                        croissanceList.clear()
                        context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE).edit()
                            .remove("croissance_data")
                            .remove("taille")
                            .remove("poids")
                            .apply()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7EC4CF)),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Réinitialiser", style = pixelTextStyle)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun PokemonLineChart(
    data: List<Pair<String, Float>>,
    color: Color,
    axisColor: Color,
    unit: String,
    alignLeft: Boolean = false
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(start = if (alignLeft) 0.dp else 32.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        val maxY = data.maxOfOrNull { it.second } ?: return@Canvas
        val minY = data.minOfOrNull { it.second } ?: return@Canvas
        val labelPaint = Paint().apply {
            this.color = android.graphics.Color.DKGRAY
            textSize = 24f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        val labelOffset = if (alignLeft) 64f else 60f
        // Axes colorés fins
        drawLine(
            color = axisColor,
            start = androidx.compose.ui.geometry.Offset(labelOffset, 0f),
            end = androidx.compose.ui.geometry.Offset(labelOffset, size.height),
            strokeWidth = 3.5f
        )
        drawLine(
            color = axisColor,
            start = androidx.compose.ui.geometry.Offset(labelOffset, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = 3.5f
        )
        // Graduations
        for (i in 0..4) {
            val y = size.height - (i / 4f) * size.height
            val value = minY + i * (maxY - minY) / 4
            drawLine(
                color = axisColor,
                start = androidx.compose.ui.geometry.Offset(labelOffset - 8f, y),
                end = androidx.compose.ui.geometry.Offset(labelOffset, y),
                strokeWidth = 2.5f
            )
            drawContext.canvas.nativeCanvas.drawText("%.1f".format(value), 0f, y, labelPaint)
        }
        // Ombre sous la courbe
        val shadowPath = Path()
        data.forEachIndexed { index, (_, value) ->
            val x = labelOffset + (size.width - labelOffset) * index / (data.size - 1).coerceAtLeast(1)
            val y = size.height - ((value - minY) / (maxY - minY)) * size.height
            if (index == 0) shadowPath.moveTo(x, y + 6f) else shadowPath.lineTo(x, y + 6f)
        }
        drawPath(shadowPath, color.copy(alpha = 0.25f), style = Stroke(width = 12f, cap = StrokeCap.Round))
        // Courbe principale
        val path = Path()
        data.forEachIndexed { index, (_, value) ->
            val x = labelOffset + (size.width - labelOffset) * index / (data.size - 1).coerceAtLeast(1)
            val y = size.height - ((value - minY) / (maxY - minY)) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 6f, cap = StrokeCap.Round))
        // Points stylés
        data.forEachIndexed { index, (label, value) ->
            val x = labelOffset + (size.width - labelOffset) * index / (data.size - 1).coerceAtLeast(1)
            val y = size.height - ((value - minY) / (maxY - minY)) * size.height
            // Contour noir
            drawCircle(
                color = Color.Black,
                radius = 11f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
            // Remplissage coloré
            drawCircle(
                color = color,
                radius = 8f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
            // Reflet blanc
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = androidx.compose.ui.geometry.Offset(x - 3f, y - 3f)
            )
        }
        // Labels pixel art (affiche la première, la dernière, et une sur deux si beaucoup de points)
        val showAll = data.size <= 6
        data.forEachIndexed { index, (label, _) ->
            val x = labelOffset + (size.width - labelOffset) * index / (data.size - 1).coerceAtLeast(1)
            val isFirst = index == 0
            val isLast = index == data.lastIndex
            val showLabel = showAll || isFirst || isLast || index % 2 == 0
            if (showLabel) {
                val offset = if (isFirst) 0f else if (isLast) -64f else -32f
                val smallPaint = Paint(labelPaint)
                smallPaint.textSize = if (showAll) 24f else 18f
                drawContext.canvas.nativeCanvas.drawText(label, x + offset, size.height + 32f, smallPaint)
            }
        }
    }
}
