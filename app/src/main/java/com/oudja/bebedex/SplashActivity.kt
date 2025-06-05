package com.oudja.bebedex

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.ui.theme.BebeDexTheme
import androidx.core.app.ActivityOptionsCompat
import com.oudja.bebedex.data.BebeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BebeDexTheme {
                SplashScreen(onStart = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val appContext = applicationContext
                        val db = BebeDatabase.getDatabase(appContext)
                        val bebeDao = db.bebeDao()
                        val babies = bebeDao.getAll()

                        val nextActivity = if (babies.isEmpty()) {
                            IntroActivity::class.java
                        } else {
                            MainActivity::class.java
                        }

                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@SplashActivity, nextActivity).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }

                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                this@SplashActivity,
                                R.anim.zoom_enter,
                                R.anim.zoom_exit
                            )
                            startActivity(intent, options.toBundle())
                            finish()
                        }
                    }
                })
            }
        }
    }

    @Composable
    fun SplashScreen(onStart: () -> Unit) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onStart() }
        ) {
            // ✅ Image fullscreen exacte
            Image(
                painter = painterResource(id = R.drawable.splash_screen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds // image à la taille exacte de l'écran
            )

            // ✅ Texte animé "Appuyer pour commencer"
            Image(
                painter = painterResource(id = R.drawable.appuyer_pour_commencer_bleu),
                contentDescription = "Appuyer pour commencer",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
                    .scale(scale)
            )
        }
    }
}
