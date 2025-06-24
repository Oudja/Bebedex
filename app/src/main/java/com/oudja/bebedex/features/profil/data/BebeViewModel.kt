package com.oudja.bebedex.features.profil.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oudja.bebedex.data.BebeDatabase
import com.oudja.bebedex.data.BebeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BebeViewModel(application: Application, private val bebeName: String) : AndroidViewModel(application) {
    private val bebeDao = BebeDatabase.getDatabase(application).bebeDao()
    private val _bebe = MutableStateFlow<BebeEntity?>(null)
    val bebe: StateFlow<BebeEntity?> = _bebe

    init {
        loadBebe()
    }

    fun loadBebe() {
        viewModelScope.launch {
            _bebe.value = bebeDao.getByName(bebeName)
        }
    }

    fun updateBebe(newBebe: BebeEntity) {
        viewModelScope.launch {
            bebeDao.update(newBebe)
            _bebe.value = newBebe
        }
    }

    // Pour forcer le rechargement depuis la base (si modifié ailleurs)
    fun refresh() = loadBebe()
} 