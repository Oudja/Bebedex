// BebeDetailActivity.kt
package com.oudja.bebedex

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Insets.add
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import java.time.LocalDate
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.Coil
import java.time.Period
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.key
import androidx.compose.material3.MenuAnchorType
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.example.bebedex.biberon.BiberonScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class BebeDetailActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        Coil.setImageLoader(imageLoader)

        val name = intent.getStringExtra("name") ?: "???"
        val level = intent.getIntExtra("level", 1)
        val xp = intent.getIntExtra("xp", 0)
        setContent {
            BebeDexTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(navController, name = name,level = level,  xp = xp)
                }
            }
        }
    }
}

@Composable
fun AnimatedGif(modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(R.drawable.bebe_gif) //
            .crossfade(true)
            .build(),
        contentDescription = "Bébé qui baille",
        modifier = modifier
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController, name: String, level: Int, xp: Int) {
    NavHost(navController = navController, startDestination = "profil") {
        composable("profil") {
            ProfilScreen(name = name, initialLevel = level, initialXp = xp, onNavigate = { navController.navigate(it) })
        }
        composable("stats") {
            StatScreen(onBack = { navController.popBackStack() })
        }
        composable("competences") {
            CompetencesScreen(name = name, onBack = { navController.popBackStack() })
        }
        composable("courbe") {
            CourbeScreen(onBack = { navController.popBackStack() })
        }
        composable("biberons") {
            BiberonScreen(onBack = { navController.popBackStack() })
        }

    }
}


data class CroissanceData(val datetime: String, val taille: Float, val poids: Float)

val croissanceList = mutableStateListOf<CroissanceData>()


@RequiresApi(Build.VERSION_CODES.O)
fun saveGrowthData(context: Context, taille: Float, poids: Float) {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    val updatedList = croissanceList.toMutableList().apply { add(CroissanceData(now, taille, poids)) }
    croissanceList.clear()
    croissanceList.addAll(updatedList)
    val json = gson.toJson(updatedList)
    prefs.edit()
        .putString("croissance_data", json)
        .putFloat("taille", taille)
        .putFloat("poids", poids)
        .apply()
}

fun loadGrowthData(context: Context): Pair<Float, Float> {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    val taille = prefs.getFloat("taille", 50f)
    val poids = prefs.getFloat("poids", 3.5f)
    return taille to poids
}

fun loadGrowthHistory(context: Context) {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    val json = prefs.getString("croissance_data", null)
    val gson = Gson()
    val type = object : TypeToken<List<CroissanceData>>() {}.type
    val list = gson.fromJson<List<CroissanceData>>(json, type) ?: emptyList()
    croissanceList.clear()
    croissanceList.addAll(list)
}

@Composable
fun XpBar(xp: Int, maxXp: Int = 100, modifier: Modifier = Modifier) {
    val progress = (xp.toFloat() / maxXp).coerceIn(0f, 1f)

    // ⚡ Animation de la progression
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "xp-animation"
    )

    val backgroundColor = Color(0xFFE0E0E0)
    val progressColor = Color(0xFFEE8130) // orange XP style Pokémon

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfilScreen(name: String, initialLevel: Int, initialXp: Int, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val db = BebeDatabase.getDatabase(context)
    val bebeDao = db.bebeDao()
    val bebe = remember { mutableStateOf<BebeEntity?>(null) }

    LaunchedEffect(Unit) {
        bebe.value = bebeDao.getByName(name)
    }

    val level = bebe.value?.level ?: 1
    val xp = bebe.value?.xp ?: 0

    val (tailleInit, poidsInit) = loadGrowthData(context)

    var birthDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var birthTime by remember { mutableStateOf("12:00") }
    var taille by remember { mutableStateOf(tailleInit) }
    var poids by remember { mutableStateOf(poidsInit) }
    var tailleText by remember { mutableStateOf(tailleInit.toString()) }
    var poidsText by remember { mutableStateOf(poidsInit.toString()) }
    var isEditing by remember { mutableStateOf(false) }

    var showLevelUpScreen by remember { mutableStateOf(false) }

    // Calcul âge
    val age = try {
        val birth = LocalDate.parse(birthDate)
        val today = LocalDate.now()
        val period = Period.between(birth, today)
        "${period.years} an(s), ${period.months} mois, ${period.days} jour(s)"
    } catch (e: Exception) {
        "Date invalide"
    }

    // Afficher écran Level Up si besoin
    LaunchedEffect(level) {
        val lastSeenLevel = loadLastSeenLevel(context)
        if (level > lastSeenLevel) {
            showLevelUpScreen = true
            saveLastSeenLevel(context, level)
        }
    }

    if (showLevelUpScreen) {
        LevelUpScreen(oldLevel = level - 1, newLevel = level) {
            showLevelUpScreen = false
        }
        return
    }

    Column(modifier = Modifier.padding(24.dp)) {
        AnimatedGif(
            modifier = Modifier
                .size(128.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Nom : $name", style = MaterialTheme.typography.headlineSmall)
        Text("Âge : $age", style = MaterialTheme.typography.bodyLarge)
        Text("Niveau : $level", style = MaterialTheme.typography.bodyLarge)
        Text("XP : $xp / 100", style = MaterialTheme.typography.bodyLarge)

        XpBar(xp = xp)

        if (isEditing) {
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("Date de naissance (yyyy-mm-dd)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = birthTime,
                onValueChange = { birthTime = it },
                label = { Text("Heure de naissance (hh:mm)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tailleText,
                onValueChange = {
                    tailleText = it
                    taille = it.toFloatOrNull() ?: 0f
                },
                label = { Text("Taille (cm)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            OutlinedTextField(
                value = poidsText,
                onValueChange = {
                    poidsText = it
                    poids = it.toFloatOrNull() ?: 0f
                },
                label = { Text("Poids (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                isEditing = false
                saveGrowthData(context, taille, poids)
                croissanceList.add(CroissanceData(LocalDate.now().toString(), taille, poids))
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Valider les modifications")
            }
        } else {
            Text("Date de naissance : $birthDate")
            Text("Heure de naissance : $birthTime")
            Text("Taille : ${tailleText} cm")
            Text("Poids : ${poidsText} kg")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { isEditing = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Modifier les informations")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigate("stats") }, modifier = Modifier.fillMaxWidth()) {
            Text("Voir les statistiques")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigate("competences") }, modifier = Modifier.fillMaxWidth()) {
            Text("Voir les compétences")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigate("courbe") }, modifier = Modifier.fillMaxWidth()) {
            Text("Courbes de croissance")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigate("biberons") }, modifier = Modifier.fillMaxWidth()) {
            Text("Suivi des biberons")
        }

    }
}

fun saveLastSeenLevel(context: Context, level: Int) {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    prefs.edit().putInt("last_seen_level", level).apply()
}

fun loadLastSeenLevel(context: Context): Int {
    val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("last_seen_level", 1)
}


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

        val maxY = data.maxOf { it.second }
        val minY = data.minOf { it.second }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetencesScreen(name: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = BebeDatabase.getDatabase(context)
    val bebeDao = db.bebeDao()
    val bebe = remember { mutableStateOf<BebeEntity?>(null) }

    LaunchedEffect(Unit) {
        bebe.value = bebeDao.getByName(name)
    }

    val allCompetences = remember {
        mutableStateListOf<Competence>().apply {
            val saved = loadSavedCompetences(context)
            if (saved.isNotEmpty()) addAll(saved)
            else addAll(loadCompetencesFromCSV(context))
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Button(onClick = onBack) {
            Text("⬅ Retour")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CompetenceSelector(
            competences = allCompetences,
            onCompetenceAdded = { nom ->
                allCompetences.add(Competence(nom = nom, acquise = true))
                CoroutineScope(Dispatchers.IO).launch {
                    bebe.value?.let {
                        val newXp = it.xp + 10
                        val newLevel = if (newXp >= 100) it.level + 1 else it.level
                        val updated = it.copy(
                            xp = if (newXp >= 100) newXp - 100 else newXp,
                            level = newLevel
                        )
                        bebeDao.update(updated)
                        bebe.value = updated
                    }
                }
            },
            onGainXP = {
                CoroutineScope(Dispatchers.IO).launch {
                    bebe.value?.let {
                        val newXp = it.xp + 10
                        val newLevel = if (newXp >= 100) it.level + 1 else it.level
                        val updated = it.copy(
                            xp = if (newXp >= 100) newXp - 100 else newXp,
                            level = newLevel
                        )
                        bebeDao.update(updated)
                        bebe.value = updated
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            resetProgress(context, allCompetences)
            CoroutineScope(Dispatchers.IO).launch {
                bebe.value?.let {
                    val reset = it.copy(level = 1, xp = 0)
                    bebeDao.update(reset)
                    bebe.value = reset
                }
            }
        }) {
            Text("Réinitialiser")
        }
    }
}






// Les fonctions utilitaires, composants et data classes à inclure :
data class BebeStats(val eveil: Float, val robustesse: Float, val beaute: Float, val grace: Float, val sociabilite: Float)
data class Competence(
    val nom: String,
    var acquise: Boolean = false,
)





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
    val inputStream = context.resources.openRawResource(R.raw.competences)
    val reader = BufferedReader(InputStreamReader(inputStream))

    reader.useLines { lines ->
        lines.forEach { line ->
            val parts = line.split(";")
            if (parts.size >= 1) {
                val nom = parts[0].trim()

                if (nom.isNotEmpty()) {
                    competences.add(Competence(nom = nom)) // Ajout uniquement du nom sans thème
                }
            }
        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompetenceSelector(
    competences: SnapshotStateList<Competence>,
    onCompetenceAdded: (String) -> Unit,
    onGainXP: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val filtered = competences.filter {
        !it.acquise && it.nom.contains(searchText, ignoreCase = true)
    }.sortedBy { it.nom }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Rechercher ou ajouter...") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onDone = {
                val trimmed = searchText.trim()
                if (trimmed.isNotEmpty() && competences.none { it.nom.equals(trimmed, ignoreCase = true) }) {
                    onCompetenceAdded(trimmed)
                    searchText = ""
                }
            })
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (searchText.isNotBlank()) {
            Text(
                "Suggestions :", style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                filtered.take(5).forEach { competence ->
                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val index = competences.indexOf(competence)
                                competences[index] = competence.copy(acquise = true)
                                searchText = ""
                                onGainXP()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                competence.nom,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        val selected = competences.filter { it.acquise }
        if (selected.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("✅ Compétences acquises :", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selected.forEach { comp ->
                    AssistChip(
                        onClick = {
                            val index = competences.indexOf(comp)
                            competences[index] = comp.copy(acquise = false)
                        },
                        label = { Text(comp.nom) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFB2DFDB))
                    )
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
