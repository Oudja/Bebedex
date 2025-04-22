package com.oudja.bebedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.ui.theme.BebeDexTheme
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BebeDexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Notre "équipe de bébés"
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
                                imageRes = R.drawable.bebe
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
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text("Niveau : $level", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = hp.toFloat() / maxHp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("HP : $hp / $maxHp", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

