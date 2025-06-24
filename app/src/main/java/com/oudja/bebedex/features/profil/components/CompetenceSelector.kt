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

    val filtered = competences.filter {
        !it.acquise && it.nom.contains(searchText, ignoreCase = true)
    }.sortedBy { it.nom }

    val categories = listOf(
        "Motricité" to 15,
        "Motricité fine" to 12,
        "Langage" to 12,
        "Sensoriel / Éveil" to 10,
        "Relationnel / Social" to 10,
        "Jeu / Manipulation" to 12,
        "Autonomie" to 10,
        "Autre" to 10
    )

    Button(
        onClick = { showAddDialog = true },
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text("Ajouter une compétence personnalisée")
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

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Rechercher ou ajouter...") },
            modifier = Modifier.fillMaxWidth(),
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

        if (searchText.isNotBlank()) {
            Text("Suggestions :", style = MaterialTheme.typography.labelMedium)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                filtered.take(5).forEach { competence ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val index = competences.indexOf(competence)
                                competences[index] = competence.copy(acquise = true)
                                onCompetenceAdded(competence.nom)
                                searchText = ""
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                competence.nom,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        val selected = competences.distinctBy { it.nom.trim().lowercase() }.filter { it.acquise }
        if (selected.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("✅ Compétences acquises :", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selected.forEach { comp ->
                    AssistChip(
                        onClick = {
                            val index = competences.indexOfFirst { it.nom.trim().equals(comp.nom.trim(), ignoreCase = true) }
                            if (index != -1) {
                                competences[index] = comp.copy(acquise = false)
                                onCompetenceUnacquired(comp.nom)
                            }
                        },
                        label = { Text(comp.nom) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFB2DFDB))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C83FD), contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Réinitialiser")
            }
        }
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
