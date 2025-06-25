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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow

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
    val csvCompetences = remember { loadCompetencesFromCSV(context) }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val categories = listOf(
        "Motricité", "Motricité fine", "Langage", "Sensoriel / Éveil", "Relationnel / Social", "Jeu / Manipulation", "Autonomie", "Autre"
    )
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    // Compétences acquises de la catégorie sélectionnée
    val acquired = allCompetences.filter { it.acquise && it.theme.equals(selectedCategory, ignoreCase = true) && it.nom.contains(searchText, ignoreCase = true) }
    // Suggestions issues du CSV, non acquises, filtrées par catégorie et texte
    val suggestions = csvCompetences.filter {
        it.theme.equals(selectedCategory, ignoreCase = true)
        && !allCompetences.any { c -> c.nom.equals(it.nom, ignoreCase = true) && c.theme.equals(selectedCategory, ignoreCase = true) && c.acquise }
        && it.nom.contains(searchText, ignoreCase = true)
    }.distinctBy { it.nom.trim().lowercase() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB2FEFA), Color(0xFFE0F7FA), Color(0xFFFFFDE4)),
                    startY = 0f, endY = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo centré au-dessus du titre
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.oudja.bebedex.R.drawable.competences),
                contentDescription = "Logo compétences",
                modifier = Modifier.size(60.dp).align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            // Titre principal toujours visible
            Text(
                "Compétences",
                style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(
                    fontSize = 32.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Row(Modifier.fillMaxSize().padding(top = 120.dp)) {
            // Sidebar effet carte arrondie avec ombre
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7EC4CF)),
                elevation = CardDefaults.cardElevation(16.dp),
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .padding(start = 12.dp, end = 0.dp, top = 0.dp, bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 18.dp, horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    categories.forEach { cat ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (cat == selectedCategory) Color(0xFF1976D2) else Color(0xFFB2FEFA)),
                            border = BorderStroke(2.dp, if (cat == selectedCategory) Color.White else Color(0xFF3A6EA5)),
                            elevation = CardDefaults.cardElevation(if (cat == selectedCategory) 8.dp else 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = cat }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                            ) {
                                Icon(getIconForCategory(cat), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(cat.take(10), style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(fontSize = 13.sp, color = Color.White))
                            }
                        }
                    }
                }
            }
            // Séparateur discret
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .padding(vertical = 24.dp)
                    .background(Color(0x33888888))
            )
            // Colonne centrale aérée et centrée
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Titre catégorie
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(getIconForCategory(selectedCategory), contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedCategory, style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(fontSize = 22.sp, color = Color(0xFF1976D2)))
                }
                Spacer(Modifier.height(18.dp))
                // Barre de recherche
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Rechercher...", style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(fontSize = 13.sp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFF7EC4CF),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFE0F7FA),
                        unfocusedContainerColor = Color(0xFFE0F7FA)
                    ),
                    modifier = Modifier.fillMaxWidth(0.98f).shadow(4.dp, RoundedCornerShape(16.dp))
                )
                // Suggestions sous la barre de recherche
                if (searchText.isNotBlank() && suggestions.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(0.98f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.take(5).forEach { suggestion ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                border = BorderStroke(1.dp, Color(0xFF1976D2)),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (allCompetences.none { it.nom.equals(suggestion.nom, ignoreCase = true) && it.theme == selectedCategory && it.acquise }) {
                                            allCompetences.add(suggestion.copy(acquise = true))
                                            com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.saveCompetences(context, allCompetences)
                                        }
                                    }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                                    Icon(getIconForCategory(selectedCategory), contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(suggestion.nom, style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(fontSize = 15.sp, color = Color.Black))
                                    Spacer(Modifier.weight(1f))
                                    Text("Ajouter", style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(fontSize = 12.sp, color = Color(0xFF1976D2)))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                // Liste des compétences acquises
                if (acquired.isEmpty()) {
                    Spacer(Modifier.height(32.dp))
                    Text("Aucune compétence acquise dans cette catégorie !", style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(fontSize = 15.sp), color = Color.DarkGray)
                } else {
                    acquired.forEach { comp ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE4)),
                            elevation = CardDefaults.cardElevation(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFB3B3B3)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Text(
                                    comp.nom,
                                    style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(fontSize = 17.sp, color = Color.Black),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    val idx = allCompetences.indexOfFirst { it.nom == comp.nom && it.theme == comp.theme }
                                    if (idx != -1) {
                                        allCompetences[idx] = comp.copy(acquise = false)
                                        com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.saveCompetences(context, allCompetences)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                // Bouton ajouter compétence personnalisée
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7EC4CF)),
                    elevation = CardDefaults.cardElevation(8.dp),
                    border = BorderStroke(2.dp, Color(0xFF3A6EA5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { showAddDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Ajouter une compétence personnalisée", style = com.oudja.bebedex.features.profil.pixelTextStyle.copy(color = Color.White, fontSize = 15.sp))
                    }
                }
            }
        }
        // Bouton retour flottant en bas à gauche
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            FloatingActionButton(
                onClick = onBack,
                shape = RoundedCornerShape(50),
                containerColor = Color(0xFF7EC4CF),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", modifier = Modifier.size(28.dp))
            }
        }
        // Dialog ajout compétence personnalisée
        if (showAddDialog) {
            var customNom by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nouvelle compétence", style = com.oudja.bebedex.features.profil.pixelTextStyle) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customNom,
                            onValueChange = { customNom = it },
                            label = { Text("Nom de la compétence", style = com.oudja.bebedex.features.profil.pixelTextStyle) },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customNom.isNotBlank()) {
                                val nom = customNom.trim()
                                if (allCompetences.none { it.nom.equals(nom, ignoreCase = true) && it.theme == selectedCategory }) {
                                    allCompetences.add(
                                        Competence(nom = nom, theme = selectedCategory, acquise = true)
                                    )
                                    com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.saveCompetences(context, allCompetences)
                                }
                                showAddDialog = false
                            }
                        }
                    ) { Text("Ajouter", style = com.oudja.bebedex.features.profil.pixelTextStyle) }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddDialog = false }) { Text("Annuler", style = com.oudja.bebedex.features.profil.pixelTextStyle) }
                }
            )
        }
    }
}

@Composable
private fun getIconForCategory(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Motricité" -> Icons.Default.DirectionsRun
        "Motricité fine" -> Icons.Default.Create
        "Langage" -> Icons.Default.RecordVoiceOver
        "Sensoriel / Éveil" -> Icons.Default.Visibility
        "Relationnel / Social" -> Icons.Default.People
        "Jeu / Manipulation" -> Icons.Default.Extension
        "Autonomie" -> Icons.Default.CheckCircleOutline
        else -> Icons.Default.StarBorder
    }
}
