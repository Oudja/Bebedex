package com.oudja.bebedex.features.profil.screens

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.data.BebeDatabase
import com.oudja.bebedex.data.BebeEntity
import com.oudja.bebedex.features.levelup.LevelUpScreen
import com.oudja.bebedex.features.profil.components.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import com.oudja.bebedex.features.profil.utils.ProfilStorageUtils.saveLastSeenLevel
import com.oudja.bebedex.features.profil.utils.ProfilStorageUtils.loadLastSeenLevel
import com.oudja.bebedex.features.profil.utils.GrowthUtils
import com.oudja.bebedex.features.profil.data.BebeViewModel
import androidx.compose.runtime.collectAsState

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfilScreen(bebeViewModel: BebeViewModel, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val bebeState by bebeViewModel.bebe.collectAsState()
    val level = bebeState?.level ?: 1
    val xp = bebeState?.xp ?: 0

    var birthDate by remember(bebeState) {
        mutableStateOf(bebeState?.dateNaissance ?: LocalDate.now().toString())
    }
    var birthTime by remember(bebeState) { mutableStateOf(bebeState?.heureNaissance ?: "12:00") }
    var taille by remember(bebeState) { mutableStateOf(bebeState?.taille ?: 50f) }
    var poids by remember(bebeState) { mutableStateOf(bebeState?.poids ?: 3.5f) }
    var tailleText by remember(bebeState) { mutableStateOf(taille.toString()) }
    var poidsText by remember(bebeState) { mutableStateOf(poids.toString()) }
    var isEditing by remember { mutableStateOf(false) }

    var showLevelUpScreen by remember { mutableStateOf(false) }

    val age = try {
        val birth = LocalDate.parse(birthDate)
        val today = LocalDate.now()
        val period = Period.between(birth, today)
        "${period.years} an(s), ${period.months} mois, ${period.days} jour(s)"
    } catch (e: Exception) {
        "Date invalide"
    }

    LaunchedEffect(level) {
        val lastSeenLevel = loadLastSeenLevel(context)
        if (level > lastSeenLevel) {
            showLevelUpScreen = true
            saveLastSeenLevel(context, level)
        }
    }

    if (showLevelUpScreen) {
        LevelUpScreen(oldLevel = level - 1, newLevel = level) {
            showLevelUpScreen = false
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var showDialog by remember { mutableStateOf(false) }

            if (bebeState != null) {
                val bebeData = bebeState!!

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BebeCardHeader(
                        name = bebeData.name,
                        gender = bebeData.gender,
                        level = bebeData.level,
                        modifier = Modifier.weight(1f)
                    )

                    ResumeCard(
                        age = age,
                        birthDate = bebeData.dateNaissance,
                        birthTime = bebeData.heureNaissance,
                        taille = bebeData.taille.toString(),
                        poids = bebeData.poids.toString(),
                        xp = bebeData.xp,
                        onEdit = { showDialog = true },
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }

            if (showDialog) {
                CustomEditDialog(
                    birthDate = birthDate,
                    onBirthDateChange = { birthDate = it },
                    birthTime = birthTime,
                    onBirthTimeChange = { birthTime = it },
                    tailleText = tailleText,
                    onTailleChange = {
                        tailleText = it
                        taille = it.toFloatOrNull() ?: 0f
                    },
                    poidsText = poidsText,
                    onPoidsChange = {
                        poidsText = it
                        poids = it.toFloatOrNull() ?: 0f
                    },
                    onDismiss = { showDialog = false },
                    onConfirm = {
                        showDialog = false
                        CoroutineScope(Dispatchers.IO).launch {
                            bebeState?.let {
                                val updated = it.copy(
                                    dateNaissance = birthDate,
                                    heureNaissance = birthTime,
                                    taille = taille,
                                    poids = poids
                                )
                                bebeViewModel.updateBebe(updated)
                                GrowthUtils.addGrowthEntry(context, taille, poids)
                            }
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .width(380.dp)
                    .padding(vertical = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color(0xFFC5E1E6))
                    .border(3.dp, Color.White, MaterialTheme.shapes.medium)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuButton("📊 Statistiques") { onNavigate("stats") }
                    MenuButton("🧠 Compétences") { onNavigate("competences") }
                    MenuButton("📈 Courbe de croissance") { onNavigate("courbe") }
                    MenuButton("🍼 Suivi des biberons") { onNavigate("biberons") }
                }
            }
        }
    }
}

