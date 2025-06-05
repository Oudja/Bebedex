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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompetenceSelector(
    competences: SnapshotStateList<Competence>,
    onCompetenceAdded: (String) -> Unit,
    onGainXP: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    val filtered = competences.filter {
        !it.acquise && it.nom.contains(searchText, ignoreCase = true)
    }.sortedBy { it.nom }

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
                                searchText = ""
                                onGainXP()
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

        val selected = competences.filter { it.acquise }
        if (selected.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("✅ Compétences acquises :", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selected.forEach { comp ->
                    AssistChip(
                        onClick = {
                            val index = competences.indexOf(comp)
                            competences[index] = comp.copy(acquise = false)
                        },
                        label = { Text(comp.nom) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFB2DFDB))
                    )
                }
            }
        }
    }
}
