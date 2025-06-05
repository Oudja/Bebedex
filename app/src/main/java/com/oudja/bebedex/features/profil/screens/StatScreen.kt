package com.oudja.bebedex.features.profil.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.features.profil.components.StatRadar
import com.oudja.bebedex.features.profil.components.BebeStats

@Composable
fun StatScreen(onBack: () -> Unit) {
    val stats = BebeStats(
        eveil = 7.5f,
        robustesse = 6.0f,
        beaute = 8.0f,
        grace = 5.5f,
        sociabilite = 9.0f
    )

    Column(modifier = Modifier.padding(24.dp)) {
        Button(onClick = onBack) {
            Text("⬅ Retour")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Statistiques :", style = MaterialTheme.typography.titleMedium)
        StatRadar(stats = stats)
    }
}
