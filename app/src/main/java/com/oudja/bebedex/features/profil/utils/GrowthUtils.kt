package com.oudja.bebedex.features.profil.utils

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CroissanceData(val datetime: String, val taille: Float, val poids: Float)

object GrowthUtils {

    val croissanceList = mutableStateListOf<CroissanceData>()

    fun loadGrowthHistory(context: Context) {
        val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("croissance_data", null)
        val gson = Gson()
        val type = object : TypeToken<List<CroissanceData>>() {}.type
        val list = gson.fromJson<List<CroissanceData>>(json, type) ?: emptyList()
        croissanceList.clear()
        croissanceList.addAll(list)
    }

    fun clearGrowthHistory(context: Context) {
        croissanceList.clear()
        context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE).edit()
            .remove("croissance_data")
            .remove("taille")
            .remove("poids")
            .apply()
    }

    fun addGrowthEntry(context: Context, taille: Float, poids: Float) {
        val datetime = java.time.LocalDate.now().toString()
        val entry = CroissanceData(datetime, taille, poids)
        croissanceList.add(entry)
        // Sauvegarde
        val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(croissanceList)
        prefs.edit().putString("croissance_data", json).apply()
    }
}
