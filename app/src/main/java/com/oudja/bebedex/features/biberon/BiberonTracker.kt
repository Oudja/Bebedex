package com.oudja.bebedex.features.biberon

import android.app.Application
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.oudja.bebedex.data.biberon.BiberonDatabase
import com.oudja.bebedex.data.biberon.BiberonDao
import com.oudja.bebedex.data.biberon.Biberon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oudja.bebedex.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import com.oudja.bebedex.features.profil.pixelTextStyle
import com.oudja.bebedex.features.profil.components.GbaLabel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults

class BiberonRepository(private val dao: BiberonDao) {
    val biberons: Flow<List<Biberon>> = dao.getAll()

    suspend fun addBiberon(biberon: Biberon) {
        dao.insert(biberon)
    }

    suspend fun updateBiberon(biberon: Biberon) {
        dao.update(biberon)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}

class BiberonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BiberonRepository
    val biberons: StateFlow<List<Biberon>>

    init {
        val dao = BiberonDatabase.getDatabase(application).biberonDao()
        repository = BiberonRepository(dao)
        biberons = repository.biberons.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            emptyList()
        )
    }

    fun ajouterBiberon(quantiteMl: Int, heureMillis: Long) {
        viewModelScope.launch {
            repository.addBiberon(Biberon(quantiteMl = quantiteMl, heure = heureMillis))
        }
    }

    fun modifierBiberon(biberon: Biberon) {
        viewModelScope.launch {
            repository.updateBiberon(biberon)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

@Composable
fun EditBiberonDialog(
    quantite: String,
    onQuantiteChange: (String) -> Unit,
    heure: String,
    onHeureClick: () -> Unit,
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
                    GbaLabel("MODIFIER BIBERON")
                    Text("Quantité (ml)", style = pixelTextStyle.copy(color = Color.Black))
                    OutlinedTextField(
                        value = quantite,
                        onValueChange = onQuantiteChange,
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
                    Text("Heure", style = pixelTextStyle.copy(color = Color.Black))
                    Button(
                        onClick = onHeureClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7EC4CF))
                    ) {
                        Icon(Icons.Filled.AccessTime, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(heure, style = pixelTextStyle)
                    }
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
fun BiberonScreen(viewModel: BiberonViewModel = viewModel(), onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val biberons by viewModel.biberons.collectAsState()
    var quantite by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf<Calendar?>(null) }
    var heureTexte by remember { mutableStateOf("Heure") }
    var editingBiberon by remember { mutableStateOf<Biberon?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editQuantite by remember { mutableStateOf("") }
    var editHeure by remember { mutableStateOf("Heure") }
    var editHourCal by remember { mutableStateOf<Calendar?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val hourFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val grouped = biberons.groupBy { dateFormat.format(Date(it.heure)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB2FEFA), Color(0xFF0ED2F7), Color(0xFFFAFFD1)),
                    startY = 0f, endY = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo biberon pixel art
            Image(
                painter = painterResource(id = R.drawable.biberon),
                contentDescription = "Logo biberon pixel art",
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Suivi des biberons",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFF2D2D2D),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8).copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = quantite,
                        onValueChange = { quantite = it },
                        label = { Text("Quantité (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val cal = Calendar.getInstance()
                            val dialog = TimePickerDialog(
                                context,
                                { _, hour: Int, minute: Int ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                    }
                                    selectedHour = newCal
                                    heureTexte = hourFormat.format(newCal.time)
                                },
                                selectedHour?.get(Calendar.HOUR_OF_DAY) ?: cal.get(Calendar.HOUR_OF_DAY),
                                selectedHour?.get(Calendar.MINUTE) ?: cal.get(Calendar.MINUTE),
                                true
                            )
                            dialog.show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7EC4CF))
                    ) {
                        Icon(Icons.Filled.AccessTime, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(heureTexte)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val heureValid = selectedHour?.timeInMillis ?: System.currentTimeMillis()
                            quantite.toIntOrNull()?.takeIf { it > 0 }?.let {
                                viewModel.ajouterBiberon(it, heureValid)
                                quantite = ""
                                heureTexte = "Heure"
                                selectedHour = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = quantite.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8AFF80))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ajouter")
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.resetAll() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A80))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Réinitialiser")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Transparent),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                grouped.forEach { (date, entries) ->
                    val total = entries.sumOf { it.quantiteMl }
                    item {
                        Text(
                            text = "$date  •  Total : ${total} mL",
                            style = pixelTextStyle.copy(fontSize = 11.sp),
                            color = Color(0xFF2D2D2D),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(entries) { biberon ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${hourFormat.format(Date(biberon.heure))} - ${biberon.quantiteMl} mL",
                                    style = pixelTextStyle.copy(fontSize = 10.sp),
                                    color = Color(0xFF2D2D2D),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        editQuantite = biberon.quantiteMl.toString()
                                        editHourCal = Calendar.getInstance().apply { timeInMillis = biberon.heure }
                                        editHeure = hourFormat.format(Date(biberon.heure))
                                        editingBiberon = biberon
                                        showEditDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7EC4CF)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Modifier", style = pixelTextStyle.copy(fontSize = 10.sp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, bottom = 24.dp),
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
        }
    }

    if (showEditDialog && editingBiberon != null) {
        EditBiberonDialog(
            quantite = editQuantite,
            onQuantiteChange = { editQuantite = it },
            heure = editHeure,
            onHeureClick = {
                val cal = editHourCal ?: Calendar.getInstance()
                val dialog = TimePickerDialog(
                    context,
                    { _, hour: Int, minute: Int ->
                        val newCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                        }
                        editHourCal = newCal
                        editHeure = hourFormat.format(newCal.time)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                )
                dialog.show()
            },
            onDismiss = {
                showEditDialog = false
                editingBiberon = null
            },
            onConfirm = {
                val heureValid = editHourCal?.timeInMillis ?: System.currentTimeMillis()
                editQuantite.toIntOrNull()?.takeIf { it > 0 }?.let {
                    viewModel.modifierBiberon(
                        editingBiberon!!.copy(
                            quantiteMl = it,
                            heure = heureValid
                        )
                    )
                }
                showEditDialog = false
                editingBiberon = null
            }
        )
    }
}
