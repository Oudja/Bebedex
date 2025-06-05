package com.oudja.bebedex

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.oudja.bebedex.ui.theme.BebeDexTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.Period
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.oudja.bebedex.data.BebeDao
import com.oudja.bebedex.data.BebeDatabase
import com.oudja.bebedex.data.BebeEntity
import com.oudja.bebedex.features.profil.BebeDetailActivity

class MainActivity : ComponentActivity() {
    private lateinit var bebeDao: BebeDao

    private fun refreshBabies(bebeDao: BebeDao, babies: SnapshotStateList<BebeEntity>) {
        CoroutineScope(Dispatchers.IO).launch {
            val refreshed = bebeDao.getAll()
            withContext(Dispatchers.Main) {
                babies.clear()
                babies.addAll(refreshed)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BebeDexTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFDF6E3))
                ) {
                    val context = LocalContext.current
                    val db = remember { BebeDatabase.getDatabase(context) }
                    bebeDao = db.bebeDao()
                    val babies = remember { mutableStateListOf<BebeEntity>() }
                    val PressStart2P = FontFamily(
                        Font(R.font.press_start_2p, weight = FontWeight.Normal)
                    )

                    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        if (result.resultCode == ComponentActivity.RESULT_OK) {
                            refreshBabies(bebeDao, babies)
                        }
                    }


                    LaunchedEffect(Unit) {
                        val updatedBabies = bebeDao.getAll()
                        babies.clear()
                        babies.addAll(updatedBabies)
                    }

                    var showMenuSheet by remember { mutableStateOf(false) }
                    var showConfirmReset by remember { mutableStateOf(false) }
                    val sheetState = rememberModalBottomSheetState()

                    // 💬 Confirmation Reset
                    if (showConfirmReset) {
                        AlertDialog(
                            onDismissRequest = { showConfirmReset = false },
                            title = { Text("Confirmation") },
                            text = { Text("Êtes-vous sûr de vouloir réinitialiser ?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showConfirmReset = false
                                    CoroutineScope(Dispatchers.IO).launch {
                                        bebeDao.deleteAll()
                                        withContext(Dispatchers.Main) {
                                            babies.clear()
                                            val intent = Intent(context, IntroActivity::class.java)
                                            context.startActivity(intent)
                                            (context as? ComponentActivity)?.finish()
                                        }
                                    }
                                }) {
                                    Text("Oui, Réinitialiser")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmReset = false }) {
                                    Text("Annuler")
                                }
                            }
                        )
                    }

                    DisposableEffect(Unit) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event.name == "ON_RESUME") {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val refreshed = bebeDao.getAll()
                                    withContext(Dispatchers.Main) {
                                        babies.clear()
                                        babies.addAll(refreshed)
                                    }
                                }
                            }
                        }
                        val lifecycle = (context as androidx.lifecycle.LifecycleOwner).lifecycle
                        lifecycle.addObserver(observer)
                        onDispose { lifecycle.removeObserver(observer) }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF5DADE2),
                                            Color(0xFFEC407A),
                                            Color(0xFFFDF6E3)
                                        )
                                    )
                                )
                        )

                        RandomStars(count = 50)


                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (babies.isEmpty()) {
                                Text("Aucun bébé trouvé !", modifier = Modifier.padding(16.dp))
                            } else {
                                when (babies.size) {
                                    1 -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(32.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            BebeCard(
                                                BebeData(
                                                    name = babies[0].name,
                                                    level = babies[0].level,
                                                    hp = babies[0].hp,
                                                    gender = babies[0].gender,
                                                    xp = babies[0].xp,
                                                    dateNaissance = babies[0].dateNaissance
                                                ),
                                                big = true
                                            )
                                        }
                                    }

                                    2, 3 -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            LazyColumn(
                                                modifier = Modifier.wrapContentHeight(),
                                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                items(babies) { bebe ->
                                                    BebeCard(
                                                        BebeData(
                                                            name = bebe.name,
                                                            level = bebe.level,
                                                            hp = bebe.hp,
                                                            gender = bebe.gender,
                                                            xp = bebe.xp,
                                                            dateNaissance = bebe.dateNaissance
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    4, 5, 6 -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            LazyColumn(
                                                modifier = Modifier.wrapContentHeight(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                items(babies.chunked(2)) { row ->
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            16.dp
                                                        )
                                                    ) {
                                                        for (bebe in row) {
                                                            BebeCard(
                                                                BebeData(
                                                                    name = bebe.name,
                                                                    level = bebe.level,
                                                                    hp = bebe.hp,
                                                                    gender = bebe.gender,
                                                                    xp = bebe.xp,
                                                                    dateNaissance = bebe.dateNaissance
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            LazyColumn(
                                                modifier = Modifier.wrapContentHeight(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                items(babies) { bebe ->
                                                    BebeCard(
                                                        BebeData(
                                                            name = bebe.name,
                                                            level = bebe.level,
                                                            hp = bebe.hp,
                                                            gender = bebe.gender,
                                                            xp = bebe.xp,
                                                            dateNaissance = bebe.dateNaissance
                                                        ),
                                                        horizontal = true
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = { showMenuSheet = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = Color(0xFF5DADE2), // Couleur pastel cohérente
                            shape = RoundedCornerShape(20.dp),
                            elevation = FloatingActionButtonDefaults.elevation(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.gear_pixel_art), // Ta propre icône pixel art gear que tu ajoutes dans drawable
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // 📱 Bottom Sheet stylé Material 3
                        if (showMenuSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showMenuSheet = false },
                                sheetState = sheetState,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                                containerColor = Color(0xFFFAE5EC)
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            showMenuSheet = false
                                            val intent = Intent(context, IntroActivity::class.java)
                                            launcher.launch(intent)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(30.dp)),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5DADE2)),
                                        shape = RoundedCornerShape(30.dp)
                                    ) {
                                        Text(
                                            "✨ Ajouter un bébé",
                                            fontFamily = PressStart2P,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            showMenuSheet = false
                                            showConfirmReset = true
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(30.dp)),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                                        shape = RoundedCornerShape(30.dp)
                                    ) {
                                        Text(
                                            "🗑 Réinitialiser",
                                            fontFamily = PressStart2P,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    data class BebeData(
        val name: String,
        val level: Int,
        val hp: Int,
        val gender: String,
        val xp: Int,
        val dateNaissance: String
    )

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun BebeCard(bebe: BebeData, big: Boolean = false, horizontal: Boolean = false) {
        val context = LocalContext.current
        val imageLoader = ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()

        val imageRes =
            if (bebe.gender == "fille") R.drawable.bebe_fille_gif else R.drawable.bebe_gif

        Box(
            modifier = Modifier
                .padding(8.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(2.dp)
        ) {
            Card(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF5DADE2), Color(0xFF2C3E50))
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .then(
                        when {
                            big -> Modifier.fillMaxWidth(0.9f)
                            horizontal -> Modifier.fillMaxWidth()
                            else -> Modifier.width(160.dp)
                        }
                    )
                    .clickable {
                        val db = BebeDatabase.getDatabase(context)
                        CoroutineScope(Dispatchers.IO).launch {
                            val bebeEntity = db.bebeDao().getByName(bebe.name)
                            if (bebeEntity != null) {
                                val intent = Intent(context, BebeDetailActivity::class.java).apply {
                                    putExtra("name", bebeEntity.name)
                                    putExtra("level", bebeEntity.level)
                                    putExtra("hp", bebeEntity.hp)
                                    putExtra("xp", bebeEntity.xp)
                                }
                                withContext(Dispatchers.Main) {
                                    context.startActivity(intent)
                                }
                            }
                        }
                    },
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                if (horizontal) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageRes)
                                .decoderFactory(GifDecoder.Factory())
                                .build(),
                            contentDescription = "Bébé",
                            imageLoader = imageLoader,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val age = calculerAgeHumain(bebe.dateNaissance)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    bebe.name.uppercase(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Niv. ${bebe.level}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Text(
                                age,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                color = Color.Gray
                            )


                            LinearProgressIndicator(
                                progress = { bebe.hp / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = Color(0xFF2ECC71)
                            )
                            Text(
                                "HP : ${bebe.hp} / 100",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.End)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { bebe.xp / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = Color(0xFF3498DB)
                            )
                            Text(
                                "XP : ${bebe.xp} / 100",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.End)
                            )

                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (big) 350.dp else 220.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.fond_pokemon),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageRes)
                                    .decoderFactory(GifDecoder.Factory())
                                    .build(),
                                contentDescription = "Bébé",
                                imageLoader = imageLoader,
                                modifier = Modifier.size(if (big) 140.dp else 80.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val age = calculerAgeHumain(bebe.dateNaissance)
                            Text(
                                text = age,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    bebe.name.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(0.6f)
                                )
                                Text(
                                    "Niv. ${bebe.level}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.weight(0.4f),
                                    textAlign = TextAlign.End
                                )
                            }


                            Spacer(modifier = Modifier.height(8.dp))

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "HP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                    Text(
                                        "${bebe.hp} / 100",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { bebe.hp.toFloat() / 100 },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = Color(0xFF2ECC71),
                                    trackColor = Color.White
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                    Text(
                                        "${bebe.xp} / 100",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { bebe.xp / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = Color(0xFF3498DB),
                                    trackColor = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculerAgeHumain(dateNaissance: String): String {
    return try {
        val birth = LocalDate.parse(dateNaissance)
        val today = LocalDate.now()
        val period = Period.between(birth, today)

        when {
            period.years > 0 -> "${period.years} an(s)"
            period.months > 0 -> "${period.months} mois"
            else -> "${period.days} jour(s)"
        }
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun RandomStars(count: Int) {
    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels.dp / LocalContext.current.resources.displayMetrics.density
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels.dp / LocalContext.current.resources.displayMetrics.density

    for (i in 1..count) {
        val x = (0..screenWidth.value.toInt()).random().dp
        val y = (0..screenHeight.value.toInt()).random().dp

        Image(
            painter = painterResource(id = R.drawable.pixel_star),
            contentDescription = null,
            modifier = Modifier
                .size(12.dp)
                .offset(x = x, y = y),
            alpha = 0.5f
        )
    }
}


