// BebeDetailActivity.kt
package com.oudja.bebedex.features.profil

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import com.oudja.bebedex.features.biberon.BiberonScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.oudja.bebedex.features.levelup.LevelUpScreen
import com.oudja.bebedex.R
import com.oudja.bebedex.data.BebeDatabase
import com.oudja.bebedex.data.BebeEntity


val pixelFontFamily = FontFamily(
    Font(R.font.press_start_2p)
)

val pixelTextStyle = TextStyle(
    fontFamily = pixelFontFamily,
    fontSize = 10.sp,
    lineHeight = 14.sp
)



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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF7E57C2),
                                    Color(0xFF26A69A),
                                    Color(0xFFFFF3E0)
                                )
                            )
                        )
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController, name = name, level = level, xp = xp)
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

    var birthDate by remember(bebe.value) {
        mutableStateOf(bebe.value?.dateNaissance ?: LocalDate.now().toString())
    }
    var birthTime by remember(bebe.value) { mutableStateOf(bebe.value?.heureNaissance ?: "12:00") }
    var taille by remember(bebe.value) { mutableStateOf(bebe.value?.taille ?: 50f) }
    var poids by remember(bebe.value) { mutableStateOf(bebe.value?.poids ?: 3.5f) }
    var tailleText by remember(bebe.value) { mutableStateOf(taille.toString()) }
    var poidsText by remember(bebe.value) { mutableStateOf(poids.toString()) }
    var isEditing by remember { mutableStateOf(false) }

    var showLevelUpScreen by remember { mutableStateOf(false) }

    val age = try {
        val birth = LocalDate.parse(birthDate)
        val today = LocalDate.now()
        val period = Period.between(birth, today)
        "${period.years} an(s), ${period.months} mois, ${period.days} jour(s)"
    } catch (e: Exception) {
        "Date invalide"
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent) // ou ta couleur de fond
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // ✅ centre verticalement si peu de contenu
        ) {
            // 🍼 Carte infos bébé
            var showDialog by remember { mutableStateOf(false) }

            if (bebe.value != null) {
                val bebeData = bebe.value!!

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BebeCardHeader(
                        name = bebeData.name,
                        gender = bebeData.gender,
                        level = bebeData.level,
                        modifier = Modifier.weight(1f)
                    )

                    ResumeCard(
                        age = age,
                        birthDate = bebeData.dateNaissance,
                        birthTime = bebeData.heureNaissance,
                        taille = bebeData.taille.toString(),
                        poids = bebeData.poids.toString(),
                        xp = bebeData.xp,
                        onEdit = { showDialog = true },
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }



            if (showDialog) {
                CustomEditDialog(
                    birthDate = birthDate,
                    onBirthDateChange = { birthDate = it },
                    birthTime = birthTime,
                    onBirthTimeChange = { birthTime = it },
                    tailleText = tailleText,
                    onTailleChange = {
                        tailleText = it
                        taille = it.toFloatOrNull() ?: 0f
                    },
                    poidsText = poidsText,
                    onPoidsChange = {
                        poidsText = it
                        poids = it.toFloatOrNull() ?: 0f
                    },
                    onDismiss = { showDialog = false },
                    onConfirm = {
                        showDialog = false
                        CoroutineScope(Dispatchers.IO).launch {
                            bebe.value?.let {
                                val updated = it.copy(
                                    dateNaissance = birthDate,
                                    heureNaissance = birthTime,
                                    taille = taille,
                                    poids = poids
                                )
                                bebeDao.update(updated)
                                bebe.value = updated
                            }
                        }
                    }
                )
            }



            // 🧭 Menu navigation
            Box(
                modifier = Modifier
                    .width(380.dp)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFC5E1E6))
                    .border(3.dp, Color.White, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuButton("📊 Statistiques") { onNavigate("stats") }
                    MenuButton("🧠 Compétences") { onNavigate("competences") }
                    MenuButton("📈 Courbe de croissance") { onNavigate("courbe") }
                    MenuButton("🍼 Suivi des biberons") { onNavigate("biberons") }
                }
            }
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
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
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
                                .padding(vertical = 8.dp, horizontal = 16.dp),
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

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE6F2F5), // plus proche visuellement de C5E1E6
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text, style = pixelTextStyle)
    }
}


@Composable
fun BebeCardHeader(
    name: String,
    gender: String,
    level: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .heightIn(min = 180.dp),
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
fun ResumeCard(
    age: String,
    birthDate: String,
    birthTime: String,
    taille: String,
    poids: String,
    xp: Int,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC5E1E6)),
        border = BorderStroke(3.dp, Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            GbaLabelWithEdit("RESUME", onEdit = onEdit)

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start // tu peux passer à CenterHorizontally si tu veux centrer le texte
            ) {
                val ageParts = age.split(", ").filterNot { it.contains("0 ") }
                Text(ageParts.joinToString(" et "), style = pixelTextStyle)
                Text("${birthDate.split("-").reversed().joinToString("/")} à $birthTime", style = pixelTextStyle)
                Text("$taille cm", style = pixelTextStyle)
                Text("$poids kg", style = pixelTextStyle)
            }

            XpBar(xp = xp, maxXp = 100, modifier = Modifier.padding(top = 12.dp))
        }
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
fun CustomEditDialog(
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    birthTime: String,
    onBirthTimeChange: (String) -> Unit,
    tailleText: String,
    onTailleChange: (String) -> Unit,
    poidsText: String,
    onPoidsChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        confirmButton = {},
        dismissButton = {},
        text = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC5E1E6)),
                border = BorderStroke(3.dp, Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GbaLabel("MODIFIER")

                    // 🍼 Date de naissance
                    GbaDataRow("Date de naissance", birthDate) {
                        onBirthDateChange(it)
                    }

                    // 🕒 Heure
                    GbaDataRow("Heure de naissance", birthTime) {
                        onBirthTimeChange(it)
                    }

                    // 📏 Taille
                    GbaDataRow("Taille (cm)", tailleText) {
                        onTailleChange(it)
                    }

                    // ⚖️ Poids
                    GbaDataRow("Poids (kg)", poidsText) {
                        onPoidsChange(it)
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            "Annuler",
                            modifier = Modifier.clickable { onDismiss() },
                            style = pixelTextStyle.copy(color = Color.DarkGray)
                        )
                        Text(
                            "Enregistrer",
                            modifier = Modifier.clickable { onConfirm() },
                            style = pixelTextStyle.copy(color = Color.DarkGray)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun GbaDataRow(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(label, style = pixelTextStyle.copy(color = Color.Black))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            textStyle = pixelTextStyle,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

