package com.oudja.bebedex.features.profil.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.features.profil.components.GbaLabelWithEdit
import com.oudja.bebedex.features.profil.components.XpBar
import com.oudja.bebedex.features.profil.pixelTextStyle

@Composable
fun ResumeCard(
    age: String,
    birthDate: String,
    birthTime: String,
    taille: String,
    poids: String,
    xp: Int,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC5E1E6)),
        border = BorderStroke(3.dp, Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            GbaLabelWithEdit("RESUME", onEdit = onEdit)

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                val ageParts = age.split(", ").filterNot { it.contains("0 ") }
                Text(ageParts.joinToString(" et "), style = pixelTextStyle)
                Text("${birthDate.split("-").reversed().joinToString("/")} à $birthTime", style = pixelTextStyle)
                Text("$taille cm", style = pixelTextStyle)
                Text("$poids kg", style = pixelTextStyle)
            }

            XpBar(xp = xp, maxXp = 100, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
