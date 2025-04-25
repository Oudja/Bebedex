package com.oudja.bebedex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.oudja.bebedex.ui.theme.BebeDexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BebeDexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val prefs = context.getSharedPreferences("BebeDex", Context.MODE_PRIVATE)

                    val name = prefs.getString("baby_name", null)
                    val gender = prefs.getString("baby_gender", "garcon")
                    val level = prefs.getInt("baby_level", 1)
                    val hp = prefs.getInt("baby_hp", 100)

                    Column(modifier = Modifier.fillMaxSize()) {

                        // Bouton réinitialiser
                        Button(
                            onClick = {
                                prefs.edit().clear().apply()
                                val intent = Intent(context, IntroActivity::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("Réinitialiser")
                        }

                        if (name != null) {
                            val imageRes = if (gender == "fille") R.drawable.bebe_fille_gif else R.drawable.bebe_gif
                            LazyColumn {
                                items(listOf(Triple(name, level, hp))) { (n, l, h) ->
                                    BebeCard(
                                        name = n,
                                        level = l,
                                        hp = h,
                                        maxHp = 100,
                                        imageRes = imageRes
                                    )
                                }
                            }
                        } else {
                            Text("Aucun bébé trouvé !", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BebeCard(
    name: String,
    level: Int,
    hp: Int,
    maxHp: Int,
    imageRes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()

    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, BebeDetailActivity::class.java).apply {
                    putExtra("name", name)
                    putExtra("level", level)
                    putExtra("hp", hp)
                }
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageRes)
                    .decoderFactory(GifDecoder.Factory())
                    .build(),
                contentDescription = "GIF de bébé",
                modifier = Modifier.size(64.dp),
                imageLoader = imageLoader
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text("Niveau : $level", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = { hp.toFloat() / maxHp },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("HP : $hp / $maxHp", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
