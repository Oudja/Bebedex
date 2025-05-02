package com.oudja.bebedex

import android.content.Context
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

class MainActivity : ComponentActivity() {
    private val babies = mutableStateListOf<BebeEntity>()
    private lateinit var bebeDao: BebeDao
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


                    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        if (result.resultCode == ComponentActivity.RESULT_OK) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val updatedBabies = bebeDao.getAll()
                                withContext(Dispatchers.Main) {
                                    babies.clear()
                                    babies.addAll(updatedBabies)
                                }
                            }
                        }
                    }


                    LaunchedEffect(Unit) {
                        val updatedBabies = bebeDao.getAll()
                        babies.clear()
                        babies.addAll(updatedBabies)
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
                        Column(modifier = Modifier.fillMaxSize()) {

                            // Bouton Réinitialiser
                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        bebeDao.deleteAll()
                                        withContext(Dispatchers.Main) {
                                            babies.clear()
                                            val intent = Intent(context, IntroActivity::class.java)
                                            context.startActivity(intent)
                                            (context as? ComponentActivity)?.finish()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF607D8B),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Réinitialiser")
                            }

                            if (babies.isEmpty()) {
                                Text("Aucun bébé trouvé !", modifier = Modifier.padding(16.dp))
                            } else {
                                when (babies.size) {
                                    1 -> {
                                        // Un seul bébé -> grosse carte centrée
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
                                        // 2 ou 3 bébés -> cartes centrées verticalement
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 32.dp, vertical = 16.dp),
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

                                    4, 5, 6 -> {
                                        // 4, 5 ou 6 bébés -> 2 colonnes
                                        val rows = babies.chunked(2)
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(rows) { row ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
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
                                                    if (row.size == 1) {
                                                        Spacer(modifier = Modifier.width(160.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    else -> {
                                        // 7 bébés ou plus -> liste horizontale
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
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

                        FloatingActionButton(
                            onClick = {
                                val intent = Intent(context, IntroActivity::class.java)
                                launcher.launch(intent)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Text("+", fontSize = 24.sp)
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

    val imageRes = if (bebe.gender == "fille") R.drawable.bebe_fille_gif else R.drawable.bebe_gif

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

                        Text(
                            "${bebe.name.uppercase()}, $age",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            "Niveau : ${bebe.level}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
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
                        .background(Color.Transparent)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box {
                            // 🔥 Image de fond qui remplit la carte
                            Image(
                                painter = painterResource(id = R.drawable.fond_pokemon),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp), // ajuste la hauteur si besoin
                                contentScale = ContentScale.Crop // 🧠 pour qu'elle remplisse bien
                            )

                            // ✅ Contenu au-dessus de l’image
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        bebe.name.uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        "Niv. ${bebe.level}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
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


