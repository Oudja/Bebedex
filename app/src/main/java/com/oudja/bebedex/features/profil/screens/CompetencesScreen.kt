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
import com.oudja.bebedex.features.profil.data.BebeViewModel
import androidx.compose.runtime.collectAsState

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompetencesScreen(bebeViewModel: BebeViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val bebeState by bebeViewModel.bebe.collectAsState()

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
                val comp = allCompetences.find { it.nom.equals(nom, ignoreCase = true) }
                    ?: Competence(nom = nom, acquise = true)
                if (allCompetences.none { it.nom.equals(nom, ignoreCase = true) }) {
                    allCompetences.add(comp.copy(acquise = true))
                } else {
                    val idx = allCompetences.indexOfFirst { it.nom.equals(nom, ignoreCase = true) }
                    if (idx != -1) allCompetences[idx] = allCompetences[idx].copy(acquise = true)
                }
                // Sauvegarde après ajout ou sélection
                com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.saveCompetences(context, allCompetences)
                CoroutineScope(Dispatchers.IO).launch {
                    bebeState?.let {
                        val xpToAdd = comp.xp
                        val newXp = it.xp + xpToAdd
                        val newLevel = if (newXp >= 100) it.level + 1 else it.level
                        val updated = it.copy(
                            xp = if (newXp >= 100) newXp - 100 else newXp,
                            level = newLevel
                        )
                        bebeViewModel.updateBebe(updated)
                    }
                }
            },
            onGainXP = {
                CoroutineScope(Dispatchers.IO).launch {
                    bebeState?.let {
                        val newXp = it.xp + 10
                        val newLevel = if (newXp >= 100) it.level + 1 else it.level
                        val updated = it.copy(
                            xp = if (newXp >= 100) newXp - 100 else newXp,
                            level = newLevel
                        )
                        bebeViewModel.updateBebe(updated)
                    }
                }
            },
            bebeViewModel = bebeViewModel,
            onCompetenceUnacquired = { nom ->
                val idx = allCompetences.indexOfFirst { it.nom.equals(nom, ignoreCase = true) }
                if (idx != -1) {
                    val comp = allCompetences[idx]
                    allCompetences[idx] = comp.copy(acquise = false)
                    // Sauvegarde après suppression
                    com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.saveCompetences(context, allCompetences)
                    // Retirer l'XP et ajuster le niveau
                    CoroutineScope(Dispatchers.IO).launch {
                        bebeState?.let {
                            val xpToRemove = comp.xp
                            var totalXp = it.xp - xpToRemove
                            var totalLevel = it.level
                            // Si XP < 0, baisser le niveau si possible
                            while (totalXp < 0 && totalLevel > 1) {
                                totalLevel -= 1
                                totalXp += 100
                            }
                            if (totalXp < 0) totalXp = 0
                            val updated = it.copy(xp = totalXp, level = totalLevel)
                            bebeViewModel.updateBebe(updated)
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SUPPRESSION du bouton rouge Réinitialiser ici
        // Button(onClick = {
        //     resetProgress(context, allCompetences)
        //     CoroutineScope(Dispatchers.IO).launch {
        //         bebeState?.let {
        //             val reset = it.copy(level = 1, xp = 0)
        //             bebeViewModel.updateBebe(reset)
        //         }
        //     }
        //     // Sauvegarde immédiate après reset
        //     com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.saveCompetences(context, allCompetences)
        // }) {
        //     Text("Réinitialiser")
        // }
    }
}
