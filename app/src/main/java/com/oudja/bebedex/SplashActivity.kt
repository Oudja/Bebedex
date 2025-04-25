package com.oudja.bebedex

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import com.oudja.bebedex.ui.theme.BebeDexTheme
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BebeDexTheme {
                SplashScreen(onStart = {
                    val prefs = getSharedPreferences("BebeDex", MODE_PRIVATE)
                    val babyName = prefs.getString("baby_name", null)

                    val nextActivity = if (babyName.isNullOrBlank()) {
                        IntroActivity::class.java
                    } else {
                        MainActivity::class.java
                    }

                    val intent = Intent(this, nextActivity).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,
                        R.anim.zoom_enter,
                        R.anim.zoom_exit
                    )
                    startActivity(intent, options.toBundle())
                    finish() // ← Important pour éviter de revenir à Splash
                })
            }
        }
    }

    @Composable
    fun SplashScreen(onStart: () -> Unit) {
        // Animation rebondissante
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
                .clickable { onStart() }
        ) {
            // Fond
            Image(
                painter = painterResource(id = R.drawable.splash_screen),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit // ou .FillHeight selon ce que tu préfères
            )

            // Texte "Appuyer pour commencer" animé
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
