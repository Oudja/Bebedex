package com.oudja.bebedex.features.profil.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.data.BebeDatabase
import com.oudja.bebedex.data.BebeEntity
import com.oudja.bebedex.features.profil.components.CompetenceSelector
import com.oudja.bebedex.features.profil.data.Competence
import com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.loadSavedCompetences
import com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.loadCompetencesFromCSV
import com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.resetProgress

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompetencesScreen(name: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = BebeDatabase.getDatabase(context)
    val bebeDao = db.bebeDao()
    val bebe = remember { mutableStateOf<BebeEntity?>(null) }

    LaunchedEffect(Unit) {
        bebe.value = bebeDao.getByName(name)
    }

    val allCompetences = remember {
        mutableStateListOf<Competence>().apply {
            val saved = loadSavedCompetences(context)
            if (saved.isNotEmpty()) addAll(saved)
            else addAll(loadCompetencesFromCSV(context))
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Button(onClick = onBack) {
            Text("⬅ Retour")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CompetenceSelector(
            competences = allCompetences,
            onCompetenceAdded = { nom ->
                allCompetences.add(Competence(nom = nom, acquise = true))
                CoroutineScope(Dispatchers.IO).launch {
                    bebe.value?.let {
                        val newXp = it.xp + 10
                        val newLevel = if (newXp >= 100) it.level + 1 else it.level
                        val updated = it.copy(
                            xp = if (newXp >= 100) newXp - 100 else newXp,
                            level = newLevel
                        )
                        bebeDao.update(updated)
                        bebe.value = updated
                    }
                }
            },
            onGainXP = {
                CoroutineScope(Dispatchers.IO).launch {
                    bebe.value?.let {
                        val newXp = it.xp + 10
                        val newLevel = if (newXp >= 100) it.level + 1 else it.level
                        val updated = it.copy(
                            xp = if (newXp >= 100) newXp - 100 else newXp,
                            level = newLevel
                        )
                        bebeDao.update(updated)
                        bebe.value = updated
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            resetProgress(context, allCompetences)
            CoroutineScope(Dispatchers.IO).launch {
                bebe.value?.let {
                    val reset = it.copy(level = 1, xp = 0)
                    bebeDao.update(reset)
                    bebe.value = reset
                }
            }
        }) {
            Text("Réinitialiser")
        }
    }
}
