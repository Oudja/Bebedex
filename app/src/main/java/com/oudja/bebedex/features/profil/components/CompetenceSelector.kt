package com.oudja.bebedex.features.profil.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import com.oudja.bebedex.features.profil.data.Competence
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.graphics.Brush
import com.oudja.bebedex.features.profil.pixelTextStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompetenceSelector(
    competences: SnapshotStateList<Competence>,
    onCompetenceAdded: (String) -> Unit,
    onGainXP: () -> Unit,
    bebeViewModel: com.oudja.bebedex.features.profil.data.BebeViewModel,
    onCompetenceUnacquired: (String) -> Unit
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    val bebeState by bebeViewModel.bebe.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val categories = listOf(
        "Toutes" to 0,
        "Motricité" to 15,
        "Motricité fine" to 12,
        "Langage" to 12,
        "Sensoriel / Éveil" to 10,
        "Relationnel / Social" to 10,
        "Jeu / Manipulation" to 12,
        "Autonomie" to 10,
        "Autre" to 10
    )
    var categorieSelectionnee by remember { mutableStateOf("Toutes") }
    var expandedCat by remember { mutableStateOf(false) }

    // FOND DEGRADE
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB2FEFA), Color(0xFF0ED2F7), Color(0xFFFAFFD1)),
                    startY = 0f, endY = 1200f
                )
            )
            .padding(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // TITRE DE LA PAGE
            Text(
                text = "Compétences",
                style = pixelTextStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
            )
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7EC4CF)),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("Ajouter une compétence personnalisée", style = pixelTextStyle.copy(fontSize = 12.sp))
            }
            // CATEGORIES EN LAZYROW
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (cat, _) ->
                    val selected = categorieSelectionnee == cat
                    Button(
                        onClick = { categorieSelectionnee = cat },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Color(0xFF1976D2) else Color(0xFFE6F2F5),
                            contentColor = if (selected) Color.White else Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(if (selected) 6.dp else 0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(cat, maxLines = 2, style = pixelTextStyle.copy(fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal))
                    }
                }
            }
            // BARRE DE RECHERCHE STYLE PIXEL
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Rechercher ou ajouter...", style = pixelTextStyle.copy(fontSize = 11.sp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB2FEFA), RoundedCornerShape(12.dp)),
                textStyle = pixelTextStyle.copy(fontSize = 12.sp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFF7EC4CF),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color(0xFFB2FEFA),
                    unfocusedContainerColor = Color(0xFFB2FEFA)
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(onDone = {
                    val trimmed = searchText.trim()
                    if (trimmed.isNotEmpty() && competences.none { it.nom.equals(trimmed, ignoreCase = true) }) {
                        onCompetenceAdded(trimmed)
                        searchText = ""
                    }
                })
            )
            Spacer(modifier = Modifier.height(8.dp))
            // SUGGESTIONS
            val filtered = competences.filter {
                !it.acquise && it.nom.contains(searchText, ignoreCase = true)
                    && (categorieSelectionnee == "Toutes" || it.theme == categorieSelectionnee)
            }.sortedBy { it.nom }
            if (searchText.isNotBlank()) {
                Text(
                    "Suggestions :",
                    style = pixelTextStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 220.dp)) {
                    items(filtered.take(5)) { competence ->
                        SuggestionCard(
                            competence = competence,
                            searchText = searchText,
                            onClick = {
                                val index = competences.indexOf(competence)
                                if (index != -1) {
                                    competences[index] = competence.copy(acquise = true)
                                    onCompetenceAdded(competence.nom)
                                    searchText = ""
                                }
                            }
                        )
                    }
                }
            }
            // AFFICHAGE DES COMPETENCES ACQUISES PAR CATEGORIE
            val selected = competences.distinctBy { it.nom.trim().lowercase() }.filter { it.acquise }
            val selectedByCat = if (categorieSelectionnee == "Toutes") {
                selected.groupBy { it.theme.ifBlank { "Autre" } }
            } else {
                mapOf(categorieSelectionnee to selected.filter { it.theme == categorieSelectionnee })
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (selectedByCat.any { it.value.isNotEmpty() }) {
                selectedByCat.forEach { (cat, comps) ->
                    if (comps.isNotEmpty()) {
                        Text(cat, fontWeight = FontWeight.Bold, fontSize = 14.sp, style = pixelTextStyle, modifier = Modifier.padding(vertical = 4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            comps.forEach { comp ->
                                AssistChip(
                                    onClick = {
                                        val index = competences.indexOfFirst { it.nom.trim().equals(comp.nom.trim(), ignoreCase = true) }
                                        if (index != -1) {
                                            competences[index] = comp.copy(acquise = false)
                                            onCompetenceUnacquired(comp.nom)
                                        }
                                    },
                                    label = { Text(comp.nom, style = pixelTextStyle.copy(fontSize = 11.sp)) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = when (cat) {
                                            "Motricité" -> Color(0xFFB2DFDB)
                                            "Motricité fine" -> Color(0xFFFFF9C4)
                                            "Langage" -> Color(0xFFFFCCBC)
                                            "Sensoriel / Éveil" -> Color(0xFFD1C4E9)
                                            "Relationnel / Social" -> Color(0xFFFFF59D)
                                            "Jeu / Manipulation" -> Color(0xFFFFE0B2)
                                            "Autonomie" -> Color(0xFFC8E6C9)
                                            else -> Color(0xFFE0E0E0)
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Aucune compétence acquise dans cette catégorie pour l'instant !",
                    style = pixelTextStyle.copy(fontSize = 12.sp),
                    color = Color.DarkGray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    if (showAddDialog) {
        var customNom by remember { mutableStateOf("") }
        var selectedCatIndex by remember { mutableStateOf(0) }
        val xpAuto = categories[selectedCatIndex].second
        var expanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nouvelle compétence") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customNom,
                        onValueChange = { customNom = it },
                        label = { Text("Nom de la compétence") },
                        singleLine = true
                    )
                    Text("Catégorie", style = MaterialTheme.typography.labelMedium)
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(categories[selectedCatIndex].first)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEachIndexed { idx, cat ->
                            DropdownMenuItem(
                                text = { Text(cat.first) },
                                onClick = {
                                    selectedCatIndex = idx
                                    expanded = false
                                }
                            )
                        }
                    }
                    Text("XP attribué : $xpAuto", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customNom.isNotBlank()) {
                            val nom = customNom.trim()
                            val cat = categories[selectedCatIndex].first
                            val xp = categories[selectedCatIndex].second
                            if (competences.none { it.nom.equals(nom, ignoreCase = true) }) {
                                competences.add(
                                    Competence(nom = nom, theme = cat, xp = xp, acquise = true)
                                )
                                onCompetenceAdded(nom)
                            }
                            showAddDialog = false
                        }
                    }
                ) { Text("Ajouter") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Confirmation") },
            text = { Text("Voulez-vous vraiment tout réinitialiser (XP, niveau, compétences) ?") },
            confirmButton = {
                Button(onClick = {
                    showResetDialog = false
                    bebeState?.let {
                        bebeViewModel.updateBebe(it.copy(xp = 0, level = 1))
                    }
                    competences.forEachIndexed { idx, comp ->
                        competences[idx] = comp.copy(acquise = false)
                    }
                    com.oudja.bebedex.features.profil.utils.CompetenceStorageUtils.saveCompetences(
                        context, competences
                    )
                }) { Text("Oui") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun SuggestionCard(competence: Competence, searchText: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = getIconForCategory(competence.theme),
                contentDescription = "Category",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = highlightSearchText(competence.nom, searchText),
                style = pixelTextStyle.copy(fontSize = 12.sp),
            )
        }
    }
}

@Composable
private fun getIconForCategory(category: String): ImageVector {
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

private fun highlightSearchText(fullText: String, searchText: String): AnnotatedString {
    return buildAnnotatedString {
        append(fullText)
        if (searchText.isNotEmpty()) {
            val startIndex = fullText.indexOf(searchText, ignoreCase = true)
            if (startIndex != -1) {
                val endIndex = startIndex + searchText.length
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black),
                    start = startIndex,
                    end = endIndex
                )
            }
        }
    }
}
