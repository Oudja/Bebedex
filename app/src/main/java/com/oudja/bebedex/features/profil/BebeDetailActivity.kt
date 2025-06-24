// BebeDetailActivity.kt
package com.oudja.bebedex.features.profil

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oudja.bebedex.ui.theme.BebeDexTheme
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.Coil
import com.oudja.bebedex.features.biberon.BiberonScreen
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.oudja.bebedex.R
import com.oudja.bebedex.features.profil.screens.CompetencesScreen
import com.oudja.bebedex.features.profil.screens.CourbeScreen
import com.oudja.bebedex.features.profil.screens.ProfilScreen
import com.oudja.bebedex.features.profil.screens.StatScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.oudja.bebedex.features.profil.data.BebeViewModel
import androidx.compose.ui.platform.LocalContext


val pixelFontFamily = FontFamily(
    Font(R.font.press_start_2p)
)

val pixelTextStyle = TextStyle(
    fontFamily = pixelFontFamily,
    fontSize = 10.sp,
    lineHeight = 14.sp
)



class BebeDetailActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        Coil.setImageLoader(imageLoader)

        val name = intent.getStringExtra("name") ?: "???"
        val level = intent.getIntExtra("level", 1)
        val xp = intent.getIntExtra("xp", 0)
        setContent {
            BebeDexTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF7E57C2),
                                    Color(0xFF26A69A),
                                    Color(0xFFFFF3E0)
                                )
                            )
                        )
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController, name = name, level = level, xp = xp)
                }
            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController, name: String, level: Int, xp: Int) {
    val context = LocalContext.current
    val bebeViewModel: BebeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BebeViewModel(context.applicationContext as android.app.Application, name) as T
            }
        }
    )
    NavHost(navController = navController, startDestination = "profil") {
        composable("profil") {
            ProfilScreen(bebeViewModel = bebeViewModel, onNavigate = { navController.navigate(it) })
        }
        composable("stats") {
            StatScreen(onBack = { navController.popBackStack() })
        }
        composable("competences") {
            CompetencesScreen(bebeViewModel = bebeViewModel, onBack = { navController.popBackStack() })
        }
        composable("courbe") {
            CourbeScreen(onBack = { navController.popBackStack() })
        }
        composable("biberons") {
            BiberonScreen(onBack = { navController.popBackStack() })
        }
    }
}