// BebeDetailActivity.kt
package com.oudja.bebedex

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oudja.bebedex.ui.theme.BebeDexTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class BebeDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra("name") ?: "???"

        setContent {
            BebeDexTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(navController, name)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, name: String) {
    NavHost(navController = navController, startDestination = "profil") {
        composable("profil") {
            ProfilScreen(name = name, onNavigate = { navController.navigate(it) })
        }
        composable("stats") {
            StatScreen(onBack = { navController.popBackStack() })
        }
        composable("competences") {
            CompetencesScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun ProfilScreen(name: String, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val (level, xp) = loadProgress(context)

    Column(modifier = Modifier.padding(24.dp)) {
        Image(
            painter = painterResource(id = R.drawable.bebe),
            contentDescription = null,
            modifier = Modifier
                .size(128.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Nom : $name", style = MaterialTheme.typography.headlineSmall)
        Text("Niveau : $level", style = MaterialTheme.typography.bodyLarge)
        Text("XP : $xp / 100", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { onNavigate("stats") }, modifier = Modifier.fillMaxWidth()) {
            Text("Voir les statistiques")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onNavigate("competences") }, modifier = Modifier.fillMaxWidth()) {
            Text("Voir les compétences")
        }
    }
}

@Composable
fun StatScreen(onBack: () -> Unit) {
    val stats = remember { BebeStats(7.5f, 6.0f, 8.0f, 5.5f, 9.0f) }
    Column(modifier = Modifier.padding(24.dp)) {
        Button(onClick = onBack) {
            Text("⬅ Retour")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Statistiques :", style = MaterialTheme.typography.titleMedium)
        StatRadar(stats = stats)
    }
}

@Composable
fun CompetencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val competences = remember {
        mutableStateListOf<Competence>().apply {
            val saved = loadSavedCompetences(context)
            if (saved.isNotEmpty()) addAll(saved) else addAll(loadCompetencesFromCSV(context))
        }
    }
    val (initialLevel, initialXp) = loadProgress(context)
    var level by remember { mutableStateOf(initialLevel) }
    var experience by remember { mutableStateOf(initialXp) }

    LaunchedEffect(competences) {
        snapshotFlow { competences.map { it.nom to it.acquise } }.collect {
            saveCompetences(context, competences)
            saveProgress(context, level, experience)
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Button(onClick = onBack) {
            Text("⬅ Retour")
        }
        Spacer(modifier = Modifier.height(16.dp))

        CompetenceSelector(
            competences = competences,
            onCompetenceAdded = { newName ->
                competences.add(Competence(newName, true))
                experience += 10
                if (experience >= 100) {
                    experience -= 100
                    level++
                }
            },
            onGainXP = {
                experience += 10
                if (experience >= 100) {
                    experience -= 100
                    level++
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            resetProgress(context, competences)
            level = 1
            experience = 0
        }) {
            Text("Réinitialiser")
        }

        val checked = competences.filter { it.acquise }
        if (checked.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("✅ Compétences acquises :", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            checked.forEach {
                Text("• ${it.nom}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Les fonctions utilitaires, composants et data classes à inclure :
data class BebeStats(val eveil: Float, val robustesse: Float, val beaute: Float, val grace: Float, val sociabilite: Float)
data class Competence(val nom: String, var acquise: Boolean = false)

fun saveProgress(context: Context, level: Int, xp: Int) {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    prefs.edit().apply {
        putInt("level", level)
        putInt("experience", xp)
        apply()
    }
}

fun loadProgress(context: Context): Pair<Int, Int> {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    val level = prefs.getInt("level", 1)
    val xp = prefs.getInt("experience", 0)
    return level to xp
}

fun saveCompetences(context: Context, competences: List<Competence>) {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    val json = Gson().toJson(competences)
    prefs.edit().putString("competences", json).apply()
}

fun loadSavedCompetences(context: Context): List<Competence> {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    val json = prefs.getString("competences", null)
    return if (json != null) {
        val type = object : TypeToken<List<Competence>>() {}.type
        Gson().fromJson(json, type)
    } else emptyList()
}

fun loadCompetencesFromCSV(context: Context): List<Competence> {
    val competences = mutableListOf<Competence>()
    try {
        val inputStream = context.resources.openRawResource(R.raw.competences)
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.forEachLine { line ->
            val nom = line.trim()
            if (nom.isNotEmpty()) {
                competences.add(Competence(nom))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return competences
}

fun resetProgress(context: Context, competences: SnapshotStateList<Competence>) {
    competences.forEachIndexed { index, competence ->
        competences[index] = competence.copy(acquise = false)
    }
    saveCompetences(context, competences)
    saveProgress(context, 1, 0)
}

@Composable
fun CompetenceSelector(
    competences: SnapshotStateList<Competence>,
    onCompetenceAdded: (String) -> Unit,
    onGainXP: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val filtered = competences.filter {
        !it.acquise && it.nom.contains(searchText, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Rechercher ou ajouter...") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                val trimmed = searchText.trim()
                if (trimmed.isNotEmpty() && competences.none { it.nom.equals(trimmed, true) }) {
                    onCompetenceAdded(trimmed)
                    searchText = ""
                }
            })
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchText.isNotEmpty()) {
            Text("🔍 Résultats de recherche :", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            filtered.forEach { competence ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = competence.acquise,
                        onCheckedChange = { checked ->
                            val index = competences.indexOf(competence)
                            competences[index] = competence.copy(acquise = checked)
                            if (checked) {
                                onGainXP()
                                searchText = ""
                            }
                        }
                    )
                    Text(competence.nom)
                }
            }
        }
    }
}

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
                    android.graphics.Paint().apply {
                        textSize = 28f
                        color = android.graphics.Color.DKGRAY
                        textAlign = android.graphics.Paint.Align.CENTER
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
