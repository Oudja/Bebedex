package com.example.bebedex.biberon

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    @Query("SELECT * FROM biberons ORDER BY heure DESC")
    fun getAll(): Flow<List<Biberon>>
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

    fun ajouterBiberon(quantiteMl: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.addBiberon(Biberon(quantiteMl = quantiteMl, heure = now))
        }
    }
}

@Composable
fun BiberonScreen(viewModel: BiberonViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), onBack: () -> Unit = {}) {
    val biberons by viewModel.biberons.collectAsState()
    var quantite by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val hourFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Grouper les biberons par date
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
                quantite.toIntOrNull()?.let {
                    viewModel.ajouterBiberon(it)
                    quantite = ""
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Ajouter")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            grouped.forEach { (date, entries) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(entries) { biberon ->
                    Text(
                        text = "${hourFormat.format(Date(biberon.heure))} - ${biberon.quantiteMl} mL",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Retour")
        }
    }
}