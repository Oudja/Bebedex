package com.oudja.bebedex

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.oudja.bebedex.ui.theme.BebeDexTheme

class IntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BebeDexTheme {
                IntroScreen(
                    onFinish = {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun IntroScreen(onFinish: () -> Unit) {
    var gender by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            0 -> {
                AsyncImage(
                    model = R.drawable.professor_talking,
                    contentDescription = null,
                    imageLoader = imageLoader,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Félicitations pour votre premier enfant !")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { step = 1 }) {
                    Text("Continuer")
                }
            }

            1 -> {
                Text("Est-ce une fille ou un garçon ?")
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    AsyncImage(
                        model = R.drawable.bebe_fille_gif,
                        contentDescription = "Fille",
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                gender = "fille"
                                step = 2
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AsyncImage(
                        model = R.drawable.bebe_gif,
                        contentDescription = "Garçon",
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                gender = "garcon"
                                step = 2
                            }
                    )
                }
            }

            2 -> {
                Text("Quel est le prénom de votre bébé ?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Prénom") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { if (name.isNotBlank()) step = 3 }) {
                    Text("Valider")
                }
            }

            3 -> {
                Text("Bonne chance pour votre aventure en tant que parent !")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    context.saveBabyData(name, gender ?: "garcon")
                    onFinish()
                }) {
                    Text("Commencer l’aventure")
                }
            }
        }
    }
}

fun Context.saveBabyData(name: String, gender: String) {
    val prefs = getSharedPreferences("BebeDex", MODE_PRIVATE)
    prefs.edit().apply {
        putString("baby_name", name)
        putString("baby_gender", gender)
        putInt("baby_level", 1)
        putInt("baby_hp", 100)
        apply()
    }
}
