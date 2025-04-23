package com.oudja.bebedex

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
                    val team = listOf(
                        Triple("Maël", 5, 34),
                        Triple("Adel", 8, 70),
                        Triple("Noé", 3, 20)
                    )

                    LazyColumn {
                        items(team) { (name, level, hp) ->
                            BebeCard(
                                name = name,
                                level = level,
                                hp = hp,
                                maxHp = 100,
                                imageRes = R.drawable.bebe_gif // ton fichier .gif ici
                            )
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

    // Création du ImageLoader avec le support du GIF
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
