package com.example.bebedex.biberon

import android.app.Application
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
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

@Entity(tableName = "biberons")
data class Biberon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quantiteMl: Int,
    val heure: Long
)

@Dao
interface BiberonDao {
    @Insert
    suspend fun insert(biberon: Biberon)

    @Query("DELETE FROM biberons")
    suspend fun clearAll()

    @Query("SELECT * FROM biberons ORDER BY heure DESC")
    fun getAll(): Flow<List<Biberon>>

    @Update
    suspend fun update(biberon: Biberon)
}

@Database(entities = [Biberon::class], version = 1)
abstract class BiberonDatabase : RoomDatabase() {
    abstract fun biberonDao(): BiberonDao

    companion object {
        @Volatile private var INSTANCE: BiberonDatabase? = null

        fun getDatabase(context: android.content.Context): BiberonDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    BiberonDatabase::class.java,
                    "biberon_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

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
fun BiberonScreen(viewModel: BiberonViewModel = viewModel(), onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val biberons by viewModel.biberons.collectAsState()
    var quantite by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf<Calendar?>(null) }
    var heureTexte by remember { mutableStateOf("Heure") }
    var editingBiberon by remember { mutableStateOf<Biberon?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val hourFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val grouped = biberons.groupBy { dateFormat.format(Date(it.heure)) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Suivi des biberons", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantite,
            onValueChange = { quantite = it },
            label = { Text("Quantité (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

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
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(heureTexte)
        }

        Button(
            onClick = {
                val heureValid = selectedHour?.timeInMillis
                quantite.toIntOrNull()?.takeIf { it > 0 && heureValid != null }?.let {
                    if (editingBiberon != null) {
                        viewModel.modifierBiberon(
                            editingBiberon!!.copy(
                                quantiteMl = it,
                                heure = heureValid!!
                            )
                        )
                    } else {
                        viewModel.ajouterBiberon(it, heureValid!!)
                    }
                    quantite = ""
                    heureTexte = "Heure"
                    selectedHour = null
                    editingBiberon = null
                }
            },
            modifier = Modifier.padding(top = 8.dp),
            enabled = selectedHour != null
        ) {
            Text(if (editingBiberon != null) "Modifier" else "Ajouter")
        }

        Button(
            onClick = {
                viewModel.resetAll()
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Réinitialiser")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            grouped.forEach { (date, entries) ->
                val total = entries.sumOf { it.quantiteMl }
                item {
                    Text(
                        text = "$date  •  Total : ${total} mL",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(entries) { biberon ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${hourFormat.format(Date(biberon.heure))} - ${biberon.quantiteMl} mL",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = {
                            quantite = biberon.quantiteMl.toString()
                            selectedHour = Calendar.getInstance().apply { timeInMillis = biberon.heure }
                            heureTexte = hourFormat.format(Date(biberon.heure))
                            editingBiberon = biberon
                        }) {
                            Text("Modifier")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Retour")
        }
    }
}
