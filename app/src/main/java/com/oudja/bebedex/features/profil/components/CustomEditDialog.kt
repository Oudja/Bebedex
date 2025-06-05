package com.oudja.bebedex.features.profil.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.features.profil.pixelTextStyle

@Composable
fun CustomEditDialog(
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    birthTime: String,
    onBirthTimeChange: (String) -> Unit,
    tailleText: String,
    onTailleChange: (String) -> Unit,
    poidsText: String,
    onPoidsChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        confirmButton = {},
        dismissButton = {},
        text = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC5E1E6)),
                border = BorderStroke(3.dp, Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GbaLabel("MODIFIER")

                    GbaDataRow("Date de naissance", birthDate, onBirthDateChange)
                    GbaDataRow("Heure de naissance", birthTime, onBirthTimeChange)
                    GbaDataRow("Taille (cm)", tailleText, onTailleChange)
                    GbaDataRow("Poids (kg)", poidsText, onPoidsChange)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Annuler", modifier = Modifier.clickable { onDismiss() }, style = pixelTextStyle.copy(color = Color.DarkGray))
                        Text("Enregistrer", modifier = Modifier.clickable { onConfirm() }, style = pixelTextStyle.copy(color = Color.DarkGray))
                    }
                }
            }
        }
    )
}

@Composable
fun GbaDataRow(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(label, style = pixelTextStyle.copy(color = Color.Black))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            textStyle = pixelTextStyle,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
