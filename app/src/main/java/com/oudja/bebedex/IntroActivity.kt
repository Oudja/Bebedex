package com.oudja.bebedex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import com.oudja.bebedex.ui.theme.BebeDexTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class IntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = BebeDatabase.getDatabase(this)
        val bebeDao = db.bebeDao()


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

    val pixelFont = FontFamily(Font(R.font.press_start_2p))

    val imageLoader = ImageLoader.Builder(context)
        .components { add(GifDecoder.Factory()) }
        .build()

    val showText = remember { mutableStateOf(false) }
    val showGifs = remember { mutableStateOf(false) }
    val showInput = remember { mutableStateOf(false) }

    val isClickable = step == 0 || step == 3

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = isClickable) {
                if (step < 3) {
                    step++
                } else {
                    // Marquer comme terminé, pour que MainActivity sache qu’un bébé a été ajouté
                    (context as? ComponentActivity)?.setResult(ComponentActivity.RESULT_OK)
                    (context as? ComponentActivity)?.finish()
                }
            }
    )
 {
        Image(
            painter = painterResource(id = R.drawable.hopital_fond),
            contentDescription = "Fond hôpital",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    0 -> {
                        LaunchedEffect(Unit) { delay(500); showText.value = true }
                        if (showText.value) {
                            DialoguePanel {
                                AnimatedText("Félicitations pour votre premier enfant !", pixelFont)
                            }
                        }
                    }

                    1 -> {
                        LaunchedEffect(Unit) { delay(500); showText.value = true; delay(1000); showGifs.value = true }
                        if (showText.value) {
                            DialoguePanel {
                                AnimatedText("Est-ce une fille ou un garçon ?", pixelFont)
                            }
                        }
                        if (showGifs.value) {
                            Row(horizontalArrangement = Arrangement.Center) {
                                AsyncImage(
                                    model = R.drawable.bebe_fille_gif,
                                    contentDescription = "Fille",
                                    imageLoader = imageLoader,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clickable { gender = "fille"; step = 2 }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                AsyncImage(
                                    model = R.drawable.bebe_gif,
                                    contentDescription = "Garçon",
                                    imageLoader = imageLoader,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clickable { gender = "garcon"; step = 2 }
                                )
                            }
                        }
                    }

                    2 -> {
                        LaunchedEffect(Unit) { delay(500); showText.value = true; delay(1000); showInput.value = true }
                        if (showText.value) {
                            DialoguePanel {
                                AnimatedText("Quel est le prénom de votre bébé ?", pixelFont)
                            }
                        }
                        if (showInput.value) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            ) {
                                BasicTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.Black,
                                        fontFamily = pixelFont,
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = {
                                if (name.isNotBlank()) {
                                    val db = BebeDatabase.getDatabase(context)
                                    val bebeDao = db.bebeDao()
                                    CoroutineScope(Dispatchers.IO).launch {
                                        bebeDao.insert(
                                            BebeEntity(
                                                name = name,
                                                gender = gender ?: "garcon",
                                                level = 1,
                                                hp = 100,
                                                xp = 0
                                            )
                                        )
                                        withContext(Dispatchers.Main) {
                                            step = 3
                                        }
                                    }
                                }
                            }) {
                                Text("Valider", fontFamily = pixelFont)
                            }
                        }
                    }

                    3 -> {
                        LaunchedEffect(Unit) { delay(500); showText.value = true }
                        if (showText.value) {
                            DialoguePanel {
                                AnimatedText("Bonne chance pour votre aventure en tant que parent !", pixelFont)
                            }
                        }
                    }
                }
            }

            AsyncImage(
                model = R.drawable.professor_talking,
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(width = 250.dp, height = 400.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnimatedText(
    fullText: String,
    font: FontFamily,
    typingSpeed: Long = 40L,
    modifier: Modifier = Modifier
) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(fullText) {
        displayedText = ""
        for (i in fullText.indices) {
            displayedText = fullText.substring(0, i + 1)
            delay(typingSpeed)
        }
    }

    Text(
        text = displayedText,
        fontFamily = font,
        fontSize = 16.sp,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun DialoguePanel(content: @Composable () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}


