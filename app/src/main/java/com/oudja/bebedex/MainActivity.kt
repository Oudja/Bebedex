package com.oudja.bebedex

import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.*


class MainActivity : ComponentActivity() {
    private val babies = mutableStateListOf<BebeEntity>()
    private lateinit var bebeDao: BebeDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            BebeDexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
                                    .padding(8.dp)
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
                                                    xp = babies[0].xp
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
                                                        xp = bebe.xp
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
                                                                xp = bebe.xp
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
                                                        xp = bebe.xp
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
    val xp: Int
)

@Composable
fun BebeCard(bebe: BebeData, big: Boolean = false, horizontal: Boolean = false) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(GifDecoder.Factory()) }
        .build()

    val imageRes = if (bebe.gender == "fille") R.drawable.bebe_fille_gif else R.drawable.bebe_gif

    Card(
        modifier = Modifier
            .padding(8.dp)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                    Text(bebe.name, style = MaterialTheme.typography.titleMedium)
                    Text("Niveau : ${bebe.level}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { bebe.hp.toFloat() / 100 },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50)
                    )
                    Text("HP : ${bebe.hp} / 100", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { bebe.xp / 100f }, // 100 XP max par niveau
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2196F3) // Couleur bleue pour l'XP
                    )
                    Text("XP : ${bebe.xp} / 100", style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
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
                Text(bebe.name, style = MaterialTheme.typography.titleMedium)
                Text("Niveau : ${bebe.level}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { bebe.hp.toFloat() / 100 },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50)
                )
                Text("HP : ${bebe.hp} / 100", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { bebe.xp / 100f }, // 100 XP max par niveau
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2196F3) // Couleur bleue pour l'XP
                )
                Text("XP : ${bebe.xp} / 100", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}



